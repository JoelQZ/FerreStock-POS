package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DetalleVentaRequest(
        @JsonProperty("producto")
        @NotNull(message = "El producto es obligatorio")
        Long producto,
        @JsonProperty("cantidad")
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que 0")
        BigDecimal cantidad
) {
}
