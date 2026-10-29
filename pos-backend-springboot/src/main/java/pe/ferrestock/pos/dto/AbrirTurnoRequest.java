package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AbrirTurnoRequest(
        @JsonProperty("monto_inicial")
        @NotNull(message = "El monto inicial es obligatorio")
        @DecimalMin(value = "0.00", message = "El monto inicial debe ser mayor o igual a 0.00")
        BigDecimal montoInicial
) {
}
