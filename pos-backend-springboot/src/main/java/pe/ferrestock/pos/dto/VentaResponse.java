package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.ferrestock.pos.entity.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(
        @JsonProperty("id_venta") Long idVenta,
        @JsonProperty("fecha_hora") LocalDateTime fechaHora,
        @JsonProperty("subtotal") BigDecimal subtotal,
        @JsonProperty("igv") BigDecimal igv,
        @JsonProperty("total") BigDecimal total,
        @JsonProperty("estado") String estado,
        @JsonProperty("serie") String serie,
        @JsonProperty("numero") Integer numero,
        @JsonProperty("cliente") Long cliente,
        @JsonProperty("tipo_comprobante") Long tipoComprobante,
        @JsonProperty("turno") Long turno,
        @JsonProperty("detalles") List<DetalleVentaResponse> detalles,
        @JsonProperty("pagos") List<PagoResponse> pagos
) {
    public static VentaResponse from(
            Venta venta,
            List<DetalleVentaResponse> detalles,
            List<PagoResponse> pagos
    ) {
        return new VentaResponse(
                venta.getIdVenta(),
                venta.getFechaHora(),
                venta.getSubtotal(),
                venta.getIgv(),
                venta.getTotal(),
                venta.getEstado().name(),
                venta.getSerie(),
                venta.getNumero(),
                venta.getIdCliente(),
                venta.getIdTipoComprobante(),
                venta.getIdTurno(),
                detalles,
                pagos
        );
    }
}
