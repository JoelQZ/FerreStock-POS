package pe.ferrestock.pos;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.ferrestock.pos.entity.EstadoVenta;
import pe.ferrestock.pos.entity.Producto;
import pe.ferrestock.pos.entity.TipoMovimientoInventario;
import pe.ferrestock.pos.repository.MovimientoInventarioRepository;
import pe.ferrestock.pos.repository.ProductoRepository;
import pe.ferrestock.pos.repository.VentaRepository;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "JWT_SECRET=clave-de-prueba-larga-para-validar-tokens-hs256-extra")
@AutoConfigureMockMvc
@Transactional
class VentaFlowIntegrationTest {

    private static final String JWT_SECRET = "clave-de-prueba-larga-para-validar-tokens-hs256-extra";
    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    private Long usuarioId;
    private Producto productoUno;
    private Producto productoDos;
    private Long efectivoId;
    private Long boletaId;

    @BeforeEach
    void prepararDatosTransaccionales() {
        List<Long> usuariosDisponibles = jdbcTemplate.queryForList("""
                select u.id
                from usuario u
                where not exists (
                    select 1 from turno t
                    where t.id_usuario = u.id and t.estado = 'ABIERTO'
                )
                order by u.id
                limit 1
                """, Long.class);
        assertFalse(usuariosDisponibles.isEmpty(), "Se requiere un usuario sin turno abierto para las pruebas");
        usuarioId = usuariosDisponibles.getFirst();

        jdbcTemplate.update("""
                insert into categoria (id_categoria, nombre, descripcion, estado)
                values (-101, 'Categoría integración', 'Fixture transaccional', 1)
                """);
        jdbcTemplate.update("""
                insert into marca (id_marca, nombre, estado)
                values (-101, 'Marca integración', 1)
                """);
        jdbcTemplate.update("""
                insert into unidad_medida (id_unidad, nombre, abreviatura)
                values (-101, 'Unidad integración', 'uni')
                """);
        jdbcTemplate.update("""
                insert into producto (
                    id_producto, codigo, nombre, descripcion, precio_compra, precio_venta,
                    stock, stock_minimo, estado, id_categoria, id_marca, id_unidad
                )
                values
                    (-101, 'TEST-POS-001', 'Producto integración 1', '', 20.00, 38.90,
                     10.000, 1.000, 1, -101, -101, -101),
                    (-102, 'TEST-POS-002', 'Producto integración 2', '', 1.00, 2.30,
                     10.000, 1.000, 1, -101, -101, -101)
                """);
        jdbcTemplate.update("""
                insert into metodo_pago (id_metodo, nombre, requiere_codigo, estado)
                values (-201, 'Efectivo', 0, 1)
                """);
        jdbcTemplate.update("""
                insert into tipo_comprobante (id_tipo, nombre, serie, correlativo_actual)
                values (-301, 'Boleta', 'B999', 0)
                """);
        jdbcTemplate.update("""
                insert into negocio (
                    id_negocio, nombre, ruc, direccion, telefono, logo, impresora, igv_porcentaje
                )
                values (-401, 'Negocio integración', '20123456789', '', '', '', '', 18.00)
                """);
        jdbcTemplate.update("""
                insert into turno (id_turno, id_usuario, apertura, monto_inicial, cierre,
                    monto_esperado, monto_real, diferencia, estado)
                values (-501, ?, ?, 100.00, null, 100.00, null, null, 'ABIERTO')
                """, usuarioId, LocalDateTime.now(ZONA_LIMA));

        productoUno = productoRepository.findById(-101L).orElseThrow();
        productoDos = productoRepository.findById(-102L).orElseThrow();
        efectivoId = -201L;
        boletaId = -301L;
        entityManager.flush();
    }

    @Test
    void ventaCompletaCalculaIgvDescuentaStockYRegistraKardexYAnulacionRestituyeStock() throws Exception {
        long ventasAntes = ventaRepository.count();
        long movimientosAntes = movimientoInventarioRepository.count();

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + token(usuarioId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cliente": null,
                                  "tipo_comprobante": %d,
                                  "detalles": [
                                    {"producto": %d, "cantidad": 2.000},
                                    {"producto": %d, "cantidad": 3.500}
                                  ],
                                  "pagos": [
                                    {"metodo": %d, "monto": "85.85", "monto_recibido": "85.85"}
                                  ]
                                }
                                """.formatted(boletaId, productoUno.getIdProducto(), productoDos.getIdProducto(),
                                efectivoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(85.85))
                .andExpect(jsonPath("$.subtotal").value(72.75))
                .andExpect(jsonPath("$.igv").value(13.10));

        assertEquals(ventasAntes + 1, ventaRepository.count());
        assertEquals(movimientosAntes + 2, movimientoInventarioRepository.count());
        assertEquals(new BigDecimal("8.000"), productoRepository.findById(productoUno.getIdProducto())
                .orElseThrow().getStock());
        assertEquals(new BigDecimal("6.500"), productoRepository.findById(productoDos.getIdProducto())
                .orElseThrow().getStock());

        Long ventaId = jdbcTemplate.queryForObject(
                "select max(id_venta) from venta where id_usuario = ?",
                Long.class,
                usuarioId
        );
        assertNotNull(ventaId);

        mockMvc.perform(post("/api/ventas/{id}/anular", ventaId)
                        .header("Authorization", "Bearer " + token(usuarioId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Cliente canceló la compra\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ANULADA"));

        assertEquals(EstadoVenta.ANULADA, ventaRepository.findById(ventaId).orElseThrow().getEstado());
        assertEquals(new BigDecimal("10.000"), productoRepository.findById(productoUno.getIdProducto())
                .orElseThrow().getStock());
        assertEquals(new BigDecimal("10.000"), productoRepository.findById(productoDos.getIdProducto())
                .orElseThrow().getStock());
        assertEquals(movimientosAntes + 4, movimientoInventarioRepository.count());
        assertEquals(2, movimientoInventarioRepository.findAll().stream()
                .filter(movimiento -> ventaId.equals(movimiento.getIdVenta()))
                .filter(movimiento -> movimiento.getTipo() == TipoMovimientoInventario.ENTRADA)
                .count());
    }

    @Test
    void ventaSinTurnoAbiertoRetornaBadRequest() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + token(Long.MAX_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cliente": null,
                                  "tipo_comprobante": %d,
                                  "detalles": [{"producto": %d, "cantidad": 1.000}],
                                  "pagos": [{"metodo": %d, "monto": "38.90"}]
                                }
                                """.formatted(boletaId, productoUno.getIdProducto(), efectivoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El usuario no tiene un turno abierto"));
    }

    @Test
    void stockInsuficienteNoPersisteVentaNiMovimientoDeInventario() throws Exception {
        setStock(productoUno, new BigDecimal("1.000"));
        entityManager.flush();
        long ventasAntes = ventaRepository.count();
        long movimientosAntes = movimientoInventarioRepository.count();

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + token(usuarioId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cliente": null,
                                  "tipo_comprobante": %d,
                                  "detalles": [{"producto": %d, "cantidad": 2.000}],
                                  "pagos": [{"metodo": %d, "monto": "77.80"}]
                                }
                                """.formatted(boletaId, productoUno.getIdProducto(), efectivoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "Stock insuficiente para CODIGO: " + productoUno.getCodigo()
                                + ". Disponible: 1.000, Solicitado: 2.000"
                ));

        assertEquals(ventasAntes, ventaRepository.count());
        assertEquals(movimientosAntes, movimientoInventarioRepository.count());
        assertEquals(new BigDecimal("1.000"), productoRepository.findById(productoUno.getIdProducto())
                .orElseThrow().getStock());
    }

    @Test
    void pagosInsuficientesNoPersisteVenta() throws Exception {
        long ventasAntes = ventaRepository.count();

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + token(usuarioId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cliente": null,
                                  "tipo_comprobante": %d,
                                  "detalles": [{"producto": %d, "cantidad": 2.000}],
                                  "pagos": [{"metodo": %d, "monto": "1.00"}]
                                }
                                """.formatted(boletaId, productoUno.getIdProducto(), efectivoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La suma de los pagos no cubre el total de la venta"));

        assertEquals(ventasAntes, ventaRepository.count());
    }

    private String token(Long idUsuario) {
        return Jwts.builder()
                .claim("user_id", idUsuario)
                .claim("usuario", "usuario-prueba")
                .claim("rol", "CAJERO")
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }

    private void setStock(Producto producto, BigDecimal stock) throws ReflectiveOperationException {
        Field field = Producto.class.getDeclaredField("stock");
        field.setAccessible(true);
        field.set(producto, stock);
    }
}
