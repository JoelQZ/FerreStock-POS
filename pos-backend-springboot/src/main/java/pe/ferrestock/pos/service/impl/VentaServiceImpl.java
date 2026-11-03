package pe.ferrestock.pos.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.ferrestock.pos.dto.ClienteVentaRequest;
import pe.ferrestock.pos.dto.DetalleVentaRequest;
import pe.ferrestock.pos.dto.DetalleVentaResponse;
import pe.ferrestock.pos.dto.PagoResponse;
import pe.ferrestock.pos.dto.PagoVentaRequest;
import pe.ferrestock.pos.dto.VentaRequest;
import pe.ferrestock.pos.dto.VentaResponse;
import pe.ferrestock.pos.entity.DetalleVenta;
import pe.ferrestock.pos.entity.EstadoTurno;
import pe.ferrestock.pos.entity.MetodoPago;
import pe.ferrestock.pos.entity.Pago;
import pe.ferrestock.pos.entity.Producto;
import pe.ferrestock.pos.entity.TipoComprobante;
import pe.ferrestock.pos.entity.Turno;
import pe.ferrestock.pos.entity.Venta;
import pe.ferrestock.pos.exception.ResourceNotFoundException;
import pe.ferrestock.pos.repository.DetalleVentaRepository;
import pe.ferrestock.pos.repository.MetodoPagoRepository;
import pe.ferrestock.pos.repository.NegocioRepository;
import pe.ferrestock.pos.repository.PagoRepository;
import pe.ferrestock.pos.repository.ProductoRepository;
import pe.ferrestock.pos.repository.TipoComprobanteRepository;
import pe.ferrestock.pos.repository.TurnoRepository;
import pe.ferrestock.pos.repository.VentaRepository;
import pe.ferrestock.pos.service.ClienteService;
import pe.ferrestock.pos.service.MovimientoInventarioService;
import pe.ferrestock.pos.service.VentaService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
public class VentaServiceImpl implements VentaService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal CERO = BigDecimal.ZERO;
    private final TurnoRepository turnoRepository;
    private final TipoComprobanteRepository tipoComprobanteRepository;
    private final ProductoRepository productoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final PagoRepository pagoRepository;
    private final NegocioRepository negocioRepository;
    private final ClienteService clienteService;
    private final MovimientoInventarioService movimientoInventarioService;

    public VentaServiceImpl(
            TurnoRepository turnoRepository,
            TipoComprobanteRepository tipoComprobanteRepository,
            ProductoRepository productoRepository,
            MetodoPagoRepository metodoPagoRepository,
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository,
            PagoRepository pagoRepository,
            NegocioRepository negocioRepository,
            ClienteService clienteService,
            MovimientoInventarioService movimientoInventarioService
    ) {
        this.turnoRepository = turnoRepository;
        this.tipoComprobanteRepository = tipoComprobanteRepository;
        this.productoRepository = productoRepository;
        this.metodoPagoRepository = metodoPagoRepository;
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.pagoRepository = pagoRepository;
        this.negocioRepository = negocioRepository;
        this.clienteService = clienteService;
        this.movimientoInventarioService = movimientoInventarioService;
    }

    @Override
    @Transactional
    public VentaResponse registrar(Long idUsuario, VentaRequest request) {
        Turno turno = turnoRepository.bloquearTurnoAbiertoParaVenta(idUsuario)
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene un turno abierto"));

        TipoComprobante tipoComprobante = tipoComprobanteRepository.bloquearPorId(request.tipoComprobante())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de comprobante no encontrado"));

        Map<Long, BigDecimal> cantidadesPorProducto = agruparCantidades(request.detalles());
        List<Producto> productos = productoRepository.bloquearPorIdsOrdenados(
                new TreeSet<>(cantidadesPorProducto.keySet())
        );
        validarProductos(productos, cantidadesPorProducto);

        List<DetalleCalculado> detallesCalculados = calcularDetalles(request.detalles(), productos);
        BigDecimal total = detallesCalculados.stream()
                .map(DetalleCalculado::subtotal)
                .reduce(CERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal porcentajeIgv = obtenerPorcentajeIgv();
        BigDecimal operacionGravada = total
                .divide(BigDecimal.ONE.add(porcentajeIgv.divide(CIEN, 8, RoundingMode.HALF_UP)), 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(operacionGravada).setScale(2, RoundingMode.HALF_UP);

        boolean factura = tipoComprobante.getNombre() != null
                && tipoComprobante.getNombre().trim().equalsIgnoreCase("Factura");
        var cliente = clienteService.resolverParaVenta(
                ClienteVentaRequest.from(request.cliente()),
                factura
        );
        List<PagoCalculado> pagosCalculados = validarPagos(request.pagos(), total);
        Integer numero = tipoComprobante.incrementarCorrelativo();
        LocalDateTime fechaHora = LocalDateTime.now(ZONA_LIMA);

        Venta venta = ventaRepository.save(Venta.completar(
                fechaHora,
                operacionGravada,
                igv,
                total,
                tipoComprobante.getSerie(),
                numero,
                cliente == null ? null : cliente.getIdCliente(),
                tipoComprobante.getIdTipo(),
                turno.getIdTurno(),
                idUsuario
        ));

        List<DetalleVenta> detalles = detalleVentaRepository.saveAll(
                detallesCalculados.stream()
                        .map(detalle -> DetalleVenta.crear(
                                venta.getIdVenta(),
                                detalle.producto().getIdProducto(),
                                detalle.cantidad(),
                                detalle.producto().getPrecioVenta(),
                                detalle.subtotal()
                        ))
                        .toList()
        );

        List<Pago> pagos = pagoRepository.saveAll(
                pagosCalculados.stream()
                        .map(pago -> Pago.crear(
                                venta.getIdVenta(),
                                pago.metodo().getIdMetodo(),
                                pago.solicitud().monto(),
                                pago.montoRecibido(),
                                pago.vuelto(),
                                pago.solicitud().codigoOperacion()
                        ))
                        .toList()
        );

        movimientoInventarioService.registrarSalidas(
                idUsuario,
                venta.getIdVenta(),
                productos,
                cantidadesPorProducto,
                fechaHora
        );
        tipoComprobanteRepository.save(tipoComprobante);

        return VentaResponse.from(
                venta,
                detalles.stream().map(DetalleVentaResponse::from).toList(),
                pagos.stream().map(PagoResponse::from).toList()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponse> listarDelTurnoActual(Long idUsuario) {
        Turno turno = turnoRepository.findFirstByIdUsuarioAndEstadoOrderByAperturaDesc(
                        idUsuario,
                        EstadoTurno.ABIERTO
                )
                .orElseThrow(() -> new ResourceNotFoundException("No tienes un turno abierto"));

        return ventaRepository.findAllByIdTurnoOrderByFechaHoraDesc(turno.getIdTurno()).stream()
                .map(this::crearRespuestaCompleta)
                .toList();
    }

    private Map<Long, BigDecimal> agruparCantidades(List<DetalleVentaRequest> detalles) {
        Map<Long, BigDecimal> cantidades = new LinkedHashMap<>();
        for (DetalleVentaRequest detalle : detalles) {
            if (detalle.producto() == null || detalle.cantidad() == null
                    || detalle.cantidad().compareTo(CERO) <= 0) {
                throw new IllegalArgumentException("Cada detalle debe tener producto y cantidad mayor que 0");
            }
            cantidades.merge(detalle.producto(), detalle.cantidad(), BigDecimal::add);
        }
        return cantidades;
    }

    private void validarProductos(List<Producto> productos, Map<Long, BigDecimal> cantidades) {
        Map<Long, Producto> porId = productos.stream()
                .collect(LinkedHashMap::new, (mapa, producto) -> mapa.put(producto.getIdProducto(), producto),
                        LinkedHashMap::putAll);
        for (Long idProducto : cantidades.keySet()) {
            Producto producto = porId.get(idProducto);
            if (producto == null) {
                throw new ResourceNotFoundException("Producto no encontrado: " + idProducto);
            }
            if (!Boolean.TRUE.equals(producto.getEstado())) {
                throw new IllegalArgumentException("El producto " + producto.getCodigo() + " está inactivo");
            }
            BigDecimal solicitado = cantidades.get(idProducto);
            if (producto.getStock().compareTo(solicitado) < 0) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para CODIGO: " + producto.getCodigo()
                                + ". Disponible: " + formatoCantidad(producto.getStock())
                                + ", Solicitado: " + formatoCantidad(solicitado)
                );
            }
        }
    }

    private List<DetalleCalculado> calcularDetalles(
            List<DetalleVentaRequest> solicitudes,
            List<Producto> productos
    ) {
        Map<Long, Producto> porId = productos.stream()
                .collect(LinkedHashMap::new, (mapa, producto) -> mapa.put(producto.getIdProducto(), producto),
                        LinkedHashMap::putAll);
        List<DetalleCalculado> calculados = new ArrayList<>();
        for (DetalleVentaRequest solicitud : solicitudes) {
            Producto producto = porId.get(solicitud.producto());
            BigDecimal subtotal = producto.getPrecioVenta()
                    .multiply(solicitud.cantidad())
                    .setScale(2, RoundingMode.HALF_UP);
            calculados.add(new DetalleCalculado(producto, solicitud.cantidad(), subtotal));
        }
        return calculados;
    }

    private BigDecimal obtenerPorcentajeIgv() {
        return negocioRepository.obtenerPorcentajesIgv().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró el porcentaje de IGV del negocio"));
    }

    private List<PagoCalculado> validarPagos(List<PagoVentaRequest> solicitudes, BigDecimal total) {
        if (solicitudes == null || solicitudes.isEmpty()) {
            throw new IllegalArgumentException("La venta debe incluir al menos un pago");
        }

        List<PagoCalculado> calculados = new ArrayList<>();
        BigDecimal totalPagos = CERO;
        boolean incluyeEfectivo = false;
        for (PagoVentaRequest solicitud : solicitudes) {
            if (solicitud.metodo() == null || solicitud.monto() == null
                    || solicitud.monto().compareTo(CERO) <= 0) {
                throw new IllegalArgumentException("Cada pago debe tener método y monto mayor que 0");
            }
            MetodoPago metodo = metodoPagoRepository.findById(solicitud.metodo())
                    .filter(pago -> Boolean.TRUE.equals(pago.getEstado()))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Método de pago no encontrado o inactivo: " + solicitud.metodo()
                    ));
            String codigo = solicitud.codigoOperacion() == null ? "" : solicitud.codigoOperacion().trim();
            if (Boolean.TRUE.equals(metodo.getRequiereCodigo()) && codigo.isEmpty()) {
                throw new IllegalArgumentException(
                        "El código de operación es obligatorio para el método " + metodo.getNombre()
                );
            }

            boolean efectivo = metodo.getNombre() != null
                    && metodo.getNombre().trim().equalsIgnoreCase("Efectivo");
            incluyeEfectivo |= efectivo;
            BigDecimal recibido = solicitud.montoRecibido() == null
                    ? solicitud.monto()
                    : solicitud.montoRecibido();
            if (recibido.compareTo(solicitud.monto()) < 0) {
                throw new IllegalArgumentException("El monto recibido no puede ser menor que el monto pagado");
            }
            totalPagos = totalPagos.add(solicitud.monto());
            calculados.add(new PagoCalculado(
                    solicitud,
                    metodo,
                    recibido,
                    efectivo ? recibido.subtract(solicitud.monto()) : CERO
            ));
        }

        if (totalPagos.compareTo(total) < 0) {
            throw new IllegalArgumentException("La suma de los pagos no cubre el total de la venta");
        }
        if (totalPagos.compareTo(total) > 0) {
            if (!incluyeEfectivo) {
                throw new IllegalArgumentException("Los pagos no efectivos deben ser iguales al total de la venta");
            }
            BigDecimal vueltoAdicional = totalPagos.subtract(total);
            for (int indice = 0; indice < calculados.size(); indice++) {
                PagoCalculado pago = calculados.get(indice);
                if (pago.efectivo()) {
                    if (pago.montoRecibido().compareTo(pago.solicitud().monto().add(vueltoAdicional)) < 0) {
                        throw new IllegalArgumentException(
                                "El efectivo recibido no cubre el total de la venta y el vuelto"
                        );
                    }
                    calculados.set(indice, new PagoCalculado(
                            pago.solicitud(),
                            pago.metodo(),
                            pago.montoRecibido(),
                            pago.vuelto().add(vueltoAdicional)
                    ));
                    break;
                }
            }
        }
        return calculados;
    }

    private VentaResponse crearRespuestaCompleta(Venta venta) {
        List<DetalleVentaResponse> detalles = detalleVentaRepository
                .findAllByIdVentaOrderByIdDetalleAsc(venta.getIdVenta()).stream()
                .map(DetalleVentaResponse::from)
                .toList();
        List<PagoResponse> pagos = pagoRepository.findAllByIdVentaOrderByIdPagoAsc(venta.getIdVenta()).stream()
                .map(PagoResponse::from)
                .toList();
        return VentaResponse.from(venta, detalles, pagos);
    }

    private String formatoCantidad(BigDecimal cantidad) {
        return cantidad.setScale(3, RoundingMode.HALF_UP).toPlainString();
    }

    private record DetalleCalculado(Producto producto, BigDecimal cantidad, BigDecimal subtotal) {
    }

    private record PagoCalculado(
            PagoVentaRequest solicitud,
            MetodoPago metodo,
            BigDecimal montoRecibido,
            BigDecimal vuelto
    ) {
        private boolean efectivo() {
            return metodo.getNombre() != null && metodo.getNombre().trim().equalsIgnoreCase("Efectivo");
        }
    }
}
