package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VentaRequest(
        @JsonProperty("cliente") Object cliente,
        @JsonProperty("tipo_comprobante")
        @NotNull(message = "El tipo de comprobante es obligatorio")
        Long tipoComprobante,
        @JsonProperty("detalles")
        @NotEmpty(message = "La venta debe incluir al menos un detalle")
        List<@Valid DetalleVentaRequest> detalles,
        @JsonProperty("pagos")
        @NotEmpty(message = "La venta debe incluir al menos un pago")
        List<@Valid PagoVentaRequest> pagos
) {
}
