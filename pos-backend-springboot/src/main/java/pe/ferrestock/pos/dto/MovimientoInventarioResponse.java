package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.ferrestock.pos.entity.MovimientoInventario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoInventarioResponse(
        @JsonProperty("id_movimiento") Long idMovimiento,
        @JsonProperty("id_producto") Long idProducto,
        @JsonProperty("id_usuario") Long idUsuario,
        @JsonProperty("id_venta") Long idVenta,
        @JsonProperty("tipo") String tipo,
        @JsonProperty("cantidad") BigDecimal cantidad,
        @JsonProperty("stock_anterior") BigDecimal stockAnterior,
        @JsonProperty("stock_nuevo") BigDecimal stockNuevo,
        @JsonProperty("fecha") LocalDateTime fecha
) {
    public static MovimientoInventarioResponse from(MovimientoInventario movimiento) {
        return new MovimientoInventarioResponse(
                movimiento.getIdMovimiento(),
                movimiento.getIdProducto(),
                movimiento.getIdUsuario(),
                movimiento.getIdVenta(),
                movimiento.getTipo().name(),
                movimiento.getCantidad(),
                movimiento.getStockAnterior(),
                movimiento.getStockNuevo(),
                movimiento.getFecha()
        );
    }
}
