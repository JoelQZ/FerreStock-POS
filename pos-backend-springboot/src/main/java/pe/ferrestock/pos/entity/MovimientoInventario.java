package pe.ferrestock.pos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long idMovimiento;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_venta")
    private Long idVenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMovimientoInventario tipo;

    @Column(name = "cantidad", nullable = false)
    private BigDecimal cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private BigDecimal stockAnterior;

    @Column(name = "stock_nuevo", nullable = false)
    private BigDecimal stockNuevo;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    protected MovimientoInventario() {
    }

    public static MovimientoInventario salida(
            Long idProducto,
            Long idUsuario,
            Long idVenta,
            BigDecimal cantidad,
            BigDecimal stockAnterior,
            BigDecimal stockNuevo,
            LocalDateTime fecha
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.idProducto = idProducto;
        movimiento.idUsuario = idUsuario;
        movimiento.idVenta = idVenta;
        movimiento.tipo = TipoMovimientoInventario.SALIDA;
        movimiento.cantidad = cantidad;
        movimiento.stockAnterior = stockAnterior;
        movimiento.stockNuevo = stockNuevo;
        movimiento.fecha = fecha;
        return movimiento;
    }

    public Long getIdMovimiento() {
        return idMovimiento;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public Long getIdVenta() {
        return idVenta;
    }

    public TipoMovimientoInventario getTipo() {
        return tipo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getStockAnterior() {
        return stockAnterior;
    }

    public BigDecimal getStockNuevo() {
        return stockNuevo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
