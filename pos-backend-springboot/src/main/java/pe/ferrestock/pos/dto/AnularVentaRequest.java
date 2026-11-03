package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AnularVentaRequest(
        @JsonProperty("motivo")
        @NotBlank(message = "El motivo de anulación es obligatorio")
        String motivo
) {
}
