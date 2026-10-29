package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CerrarTurnoRequest(
        @JsonProperty("monto_real")
        @NotNull(message = "El monto real es obligatorio")
        @DecimalMin(value = "0.00", message = "El monto real debe ser mayor o igual a 0.00")
        BigDecimal montoReal
) {
}
