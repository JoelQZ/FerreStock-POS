package pe.ferrestock.pos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @Column(name = "id_venta", nullable = false)
    private Long idVenta;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "cantidad", nullable = false)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    protected DetalleVenta() {
    }

    public static DetalleVenta crear(
            Long idVenta,
            Long idProducto,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {
        DetalleVenta detalle = new DetalleVenta();
        detalle.idVenta = idVenta;
        detalle.idProducto = idProducto;
        detalle.cantidad = cantidad;
        detalle.precioUnitario = precioUnitario;
        detalle.subtotal = subtotal;
        return detalle;
    }

    public Long getIdDetalle() {
        return idDetalle;
    }

    public Long getIdVenta() {
        return idVenta;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
