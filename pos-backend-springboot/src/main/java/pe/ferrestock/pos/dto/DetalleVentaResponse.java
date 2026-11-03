package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.ferrestock.pos.entity.DetalleVenta;

import java.math.BigDecimal;

public record DetalleVentaResponse(
        @JsonProperty("id_detalle") Long idDetalle,
        @JsonProperty("producto") Long producto,
        @JsonProperty("cantidad") BigDecimal cantidad,
        @JsonProperty("precio_unitario") BigDecimal precioUnitario,
        @JsonProperty("subtotal") BigDecimal subtotal
) {
    public static DetalleVentaResponse from(DetalleVenta detalle) {
        return new DetalleVentaResponse(
                detalle.getIdDetalle(),
                detalle.getIdProducto(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
        );
    }
}
