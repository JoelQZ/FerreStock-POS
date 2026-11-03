package pe.ferrestock.pos.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.ferrestock.pos.dto.DetalleVentaRequest;
import pe.ferrestock.pos.dto.PagoVentaRequest;
import pe.ferrestock.pos.dto.VentaRequest;
import pe.ferrestock.pos.entity.MetodoPago;
import pe.ferrestock.pos.entity.DetalleVenta;
import pe.ferrestock.pos.entity.EstadoVenta;
import pe.ferrestock.pos.entity.Producto;
import pe.ferrestock.pos.entity.MovimientoInventario;
import pe.ferrestock.pos.entity.TipoComprobante;
import pe.ferrestock.pos.entity.Turno;
import pe.ferrestock.pos.entity.Venta;
import pe.ferrestock.pos.repository.DetalleVentaRepository;
import pe.ferrestock.pos.repository.MovimientoInventarioRepository;
import pe.ferrestock.pos.repository.MetodoPagoRepository;
import pe.ferrestock.pos.repository.NegocioRepository;
import pe.ferrestock.pos.repository.PagoRepository;
import pe.ferrestock.pos.repository.ProductoRepository;
import pe.ferrestock.pos.repository.TipoComprobanteRepository;
import pe.ferrestock.pos.repository.TurnoRepository;
import pe.ferrestock.pos.repository.VentaRepository;
import pe.ferrestock.pos.service.ClienteService;
import pe.ferrestock.pos.service.MovimientoInventarioService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VentaServiceImplTest {

    private TurnoRepository turnoRepository;
    private TipoComprobanteRepository tipoComprobanteRepository;
    private ProductoRepository productoRepository;
    private MetodoPagoRepository metodoPagoRepository;
    private VentaRepository ventaRepository;
    private DetalleVentaRepository detalleVentaRepository;
    private PagoRepository pagoRepository;
    private NegocioRepository negocioRepository;
    private ClienteService clienteService;
    private MovimientoInventarioService movimientoInventarioService;
    private VentaServiceImpl ventaService;

    @BeforeEach
    void setUp() {
        turnoRepository = mock(TurnoRepository.class);
        tipoComprobanteRepository = mock(TipoComprobanteRepository.class);
        productoRepository = mock(ProductoRepository.class);
        metodoPagoRepository = mock(MetodoPagoRepository.class);
        ventaRepository = mock(VentaRepository.class);
        detalleVentaRepository = mock(DetalleVentaRepository.class);
        pagoRepository = mock(PagoRepository.class);
        negocioRepository = mock(NegocioRepository.class);
        clienteService = mock(ClienteService.class);
        movimientoInventarioService = mock(MovimientoInventarioService.class);
        ventaService = new VentaServiceImpl(
                turnoRepository,
                tipoComprobanteRepository,
                productoRepository,
                metodoPagoRepository,
                ventaRepository,
                detalleVentaRepository,
                pagoRepository,
                negocioRepository,
                clienteService,
                movimientoInventarioService
        );
    }

    @Test
    void calculaIgvBloqueaProductoYGuardaKardexEnLaVenta() throws Exception {
        Producto producto = producto(8L, "FER-0008", "10.00", "5.000", true);
        Turno turno = mock(Turno.class);
        when(turno.getIdTurno()).thenReturn(3L);
        TipoComprobante comprobante = tipoComprobante("Boleta", 0);
        MetodoPago efectivo = metodoPago("Efectivo", false);

        when(turnoRepository.bloquearTurnoAbiertoParaVenta(41L)).thenReturn(Optional.of(turno));
        when(tipoComprobanteRepository.bloquearPorId(2L)).thenReturn(Optional.of(comprobante));
        when(productoRepository.bloquearPorIdsOrdenados(anyCollection())).thenReturn(List.of(producto));
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(efectivo));
        when(negocioRepository.obtenerPorcentajesIgv()).thenReturn(List.of(new BigDecimal("18.00")));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta venta = invocation.getArgument(0);
            asignarCampo(venta, "idVenta", 99L);
            return venta;
        });
        when(detalleVentaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pagoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = ventaService.registrar(41L, new VentaRequest(
                null,
                2L,
                List.of(new DetalleVentaRequest(8L, new BigDecimal("2.000"))),
                List.of(new PagoVentaRequest(1L, new BigDecimal("20.00"), new BigDecimal("25.00"), null))
        ));

        assertEquals(new BigDecimal("16.95"), response.subtotal());
        assertEquals(new BigDecimal("3.05"), response.igv());
        assertEquals(new BigDecimal("20.00"), response.total());
        assertEquals(1, response.numero());
        assertEquals(new BigDecimal("5.00"), response.pagos().getFirst().vuelto());
        verify(turnoRepository).bloquearTurnoAbiertoParaVenta(41L);
        verify(tipoComprobanteRepository).bloquearPorId(2L);
        verify(productoRepository).bloquearPorIdsOrdenados(anyCollection());
        verify(movimientoInventarioService).registrarSalidas(
                org.mockito.ArgumentMatchers.eq(41L),
                org.mockito.ArgumentMatchers.eq(99L),
                any(),
                any(),
                any()
        );
    }

    @Test
    void calculaIgvDeVentaConVariosProductos() throws Exception {
        Producto productoUno = producto(1L, "FER-0001", "38.90", "10.000", true);
        Producto productoDos = producto(2L, "FER-0002", "2.30", "10.000", true);
        Turno turno = mock(Turno.class);
        when(turno.getIdTurno()).thenReturn(3L);
        TipoComprobante comprobante = tipoComprobante("Boleta", 0);

        when(turnoRepository.bloquearTurnoAbiertoParaVenta(41L)).thenReturn(Optional.of(turno));
        when(tipoComprobanteRepository.bloquearPorId(2L)).thenReturn(Optional.of(comprobante));
        when(productoRepository.bloquearPorIdsOrdenados(anyCollection()))
                .thenReturn(List.of(productoUno, productoDos));
        MetodoPago efectivo = metodoPago("Efectivo", false);
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(efectivo));
        when(negocioRepository.obtenerPorcentajesIgv()).thenReturn(List.of(new BigDecimal("18.00")));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta venta = invocation.getArgument(0);
            asignarCampo(venta, "idVenta", 99L);
            return venta;
        });
        when(detalleVentaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pagoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = ventaService.registrar(41L, new VentaRequest(
                null,
                2L,
                List.of(
                        new DetalleVentaRequest(1L, new BigDecimal("2.000")),
                        new DetalleVentaRequest(2L, new BigDecimal("3.500"))
                ),
                List.of(new PagoVentaRequest(1L, new BigDecimal("85.85"), null, null))
        ));

        assertEquals(new BigDecimal("85.85"), response.total());
        assertEquals(new BigDecimal("72.75"), response.subtotal());
        assertEquals(new BigDecimal("13.10"), response.igv());
    }

    @Test
    void descuentaStockYRegistraElMovimientoDeInventario() throws Exception {
        MovimientoInventarioRepository repository = mock(MovimientoInventarioRepository.class);
        Producto producto = producto(8L, "FER-0008", "10.00", "5.000", true);
        MovimientoInventarioServiceImpl service = new MovimientoInventarioServiceImpl(repository);

        service.registrarSalidas(
                41L,
                99L,
                List.of(producto),
                Map.of(8L, new BigDecimal("2.000")),
                LocalDateTime.now()
        );

        assertEquals(new BigDecimal("3.000"), producto.getStock());
        verify(repository).saveAll(any());
    }

    @Test
    void devuelveStockYRegistraMovimientoDeEntrada() throws Exception {
        MovimientoInventarioRepository repository = mock(MovimientoInventarioRepository.class);
        Producto producto = producto(8L, "FER-0008", "10.00", "3.000", true);
        MovimientoInventarioServiceImpl service = new MovimientoInventarioServiceImpl(repository);
        when(repository.saveAll(any())).thenAnswer(invocation -> {
            List<MovimientoInventario> movimientos = invocation.getArgument(0);
            assertEquals(1, movimientos.size());
            assertEquals(pe.ferrestock.pos.entity.TipoMovimientoInventario.ENTRADA,
                    movimientos.getFirst().getTipo());
            assertEquals(new BigDecimal("5.000"), movimientos.getFirst().getStockNuevo());
            return movimientos;
        });

        service.registrarEntradas(
                41L,
                99L,
                List.of(producto),
                Map.of(8L, new BigDecimal("2.000")),
                LocalDateTime.now()
        );

        assertEquals(new BigDecimal("5.000"), producto.getStock());
        verify(repository).saveAll(any());
    }

    @Test
    void rechazaPagosQueNoCubrenElTotal() throws Exception {
        Producto producto = producto(8L, "FER-0008", "10.00", "5.000", true);
        Turno turno = mock(Turno.class);
        when(turno.getIdTurno()).thenReturn(3L);
        when(turnoRepository.bloquearTurnoAbiertoParaVenta(41L)).thenReturn(Optional.of(turno));
        when(tipoComprobanteRepository.bloquearPorId(2L)).thenReturn(Optional.of(tipoComprobante("Boleta", 0)));
        when(productoRepository.bloquearPorIdsOrdenados(anyCollection())).thenReturn(List.of(producto));
        MetodoPago yape = metodoPago("Yape", true);
        when(metodoPagoRepository.findById(1L)).thenReturn(Optional.of(yape));
        when(negocioRepository.obtenerPorcentajesIgv()).thenReturn(List.of(new BigDecimal("18.00")));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrar(41L, new VentaRequest(
                        null,
                        2L,
                        List.of(new DetalleVentaRequest(8L, new BigDecimal("2.000"))),
                        List.of(new PagoVentaRequest(1L, new BigDecimal("1.00"), null, "OP-123"))
                ))
        );

        assertEquals("La suma de los pagos no cubre el total de la venta", error.getMessage());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void anulaVentaYSolicitaDevolucionDeStockConKardex() throws Exception {
        Venta venta = Venta.completar(
                LocalDateTime.now(),
                new BigDecimal("16.95"),
                new BigDecimal("3.05"),
                new BigDecimal("20.00"),
                "B001",
                1,
                null,
                2L,
                3L,
                7L
        );
        asignarCampo(venta, "idVenta", 99L);
        Producto producto = producto(8L, "FER-0008", "10.00", "3.000", true);
        when(ventaRepository.bloquearPorId(99L)).thenReturn(Optional.of(venta));
        when(detalleVentaRepository.findAllByIdVentaOrderByIdDetalleAsc(99L))
                .thenReturn(List.of(DetalleVenta.crear(99L, 8L, new BigDecimal("2.000"),
                        new BigDecimal("10.00"), new BigDecimal("20.00"))));
        when(productoRepository.bloquearPorIdsOrdenados(anyCollection())).thenReturn(List.of(producto));
        when(pagoRepository.findAllByIdVentaOrderByIdPagoAsc(99L)).thenReturn(List.of());

        var response = ventaService.anular(41L, 99L, "Cliente canceló la compra");

        assertEquals(EstadoVenta.ANULADA.name(), response.estado());
        assertEquals("Cliente canceló la compra", venta.getMotivoAnulacion());
        assertEquals(41L, venta.getIdUsuarioAnula());
        org.junit.jupiter.api.Assertions.assertNotNull(venta.getFechaAnulacion());
        verify(ventaRepository).bloquearPorId(99L);
        verify(productoRepository).bloquearPorIdsOrdenados(anyCollection());
        verify(movimientoInventarioService).registrarEntradas(
                org.mockito.ArgumentMatchers.eq(41L),
                org.mockito.ArgumentMatchers.eq(99L),
                any(),
                any(),
                any()
        );
    }

    @Test
    void rechazaVentaSinTurnoAbiertoAntesDeBloquearProductos() {
        when(turnoRepository.bloquearTurnoAbiertoParaVenta(41L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> ventaService.registrar(41L, new VentaRequest(
                        null,
                        2L,
                        List.of(new DetalleVentaRequest(8L, BigDecimal.ONE)),
                        List.of(new PagoVentaRequest(1L, BigDecimal.ONE, BigDecimal.ONE, null))
                ))
        );

        verify(tipoComprobanteRepository, never()).bloquearPorId(any());
        verify(productoRepository, never()).bloquearPorIdsOrdenados(anyCollection());
    }

    @Test
    void rechazaStockInsuficienteSinPersistirLaVenta() throws Exception {
        Producto producto = producto(8L, "FER-0008", "10.00", "1.000", true);
        Turno turno = mock(Turno.class);
        when(turnoRepository.bloquearTurnoAbiertoParaVenta(41L)).thenReturn(Optional.of(turno));
        when(tipoComprobanteRepository.bloquearPorId(2L))
                .thenReturn(Optional.of(tipoComprobante("Boleta", 0)));
        when(productoRepository.bloquearPorIdsOrdenados(anyCollection())).thenReturn(List.of(producto));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrar(41L, new VentaRequest(
                        null,
                        2L,
                        List.of(new DetalleVentaRequest(8L, new BigDecimal("2.000"))),
                        List.of(new PagoVentaRequest(1L, new BigDecimal("20.00"), null, null))
                ))
        );

        assertEquals(
                "Stock insuficiente para CODIGO: FER-0008. Disponible: 1.000, Solicitado: 2.000",
                error.getMessage()
        );
        verify(ventaRepository, never()).save(any());
        verify(movimientoInventarioService, never()).registrarSalidas(
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    private Producto producto(
            Long id,
            String codigo,
            String precioVenta,
            String stock,
            boolean activo
    ) throws Exception {
        var constructor = Producto.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Producto producto = constructor.newInstance();
        asignarCampo(producto, "idProducto", id);
        asignarCampo(producto, "codigo", codigo);
        asignarCampo(producto, "precioVenta", new BigDecimal(precioVenta));
        asignarCampo(producto, "stock", new BigDecimal(stock));
        asignarCampo(producto, "estado", activo);
        return producto;
    }

    private TipoComprobante tipoComprobante(String nombre, Integer correlativo) throws Exception {
        TipoComprobante tipo = mock(TipoComprobante.class, CALLS_REAL_METHODS);
        asignarCampo(tipo, "idTipo", 2L);
        asignarCampo(tipo, "nombre", nombre);
        asignarCampo(tipo, "serie", "B001");
        asignarCampo(tipo, "correlativoActual", correlativo);
        return tipo;
    }

    private MetodoPago metodoPago(String nombre, boolean requiereCodigo) {
        MetodoPago metodo = mock(MetodoPago.class);
        when(metodo.getIdMetodo()).thenReturn(1L);
        when(metodo.getNombre()).thenReturn(nombre);
        when(metodo.getRequiereCodigo()).thenReturn(requiereCodigo);
        when(metodo.getEstado()).thenReturn(true);
        return metodo;
    }

    private void asignarCampo(Object entidad, String nombreCampo, Object valor) throws Exception {
        Class<?> tipo = entidad.getClass();
        Field campo;
        while (true) {
            try {
                campo = tipo.getDeclaredField(nombreCampo);
                break;
            } catch (NoSuchFieldException exception) {
                tipo = tipo.getSuperclass();
                if (tipo == null) {
                    throw exception;
                }
            }
        }
        campo.setAccessible(true);
        campo.set(entidad, valor);
    }
}
