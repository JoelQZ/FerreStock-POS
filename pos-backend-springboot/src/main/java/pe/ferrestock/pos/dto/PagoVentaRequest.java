package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PagoVentaRequest(
        @JsonProperty("metodo")
        @NotNull(message = "El método de pago es obligatorio")
        Long metodo,
        @JsonProperty("monto")
        @NotNull(message = "El monto del pago es obligatorio")
        @Positive(message = "El monto del pago debe ser mayor que 0")
        BigDecimal monto,
        @JsonProperty("monto_recibido")
        BigDecimal montoRecibido,
        @JsonProperty("codigo_operacion")
        String codigoOperacion
) {
}
