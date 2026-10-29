package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.ferrestock.pos.entity.MetodoPago;

public record MetodoPagoResponse(
        @JsonProperty("id_metodo") Long idMetodo,
        @JsonProperty("nombre") String nombre,
        @JsonProperty("requiere_codigo") Boolean requiereCodigo,
        @JsonProperty("estado") Boolean estado
) {
    public static MetodoPagoResponse from(MetodoPago metodoPago) {
        return new MetodoPagoResponse(
                metodoPago.getIdMetodo(),
                metodoPago.getNombre(),
                metodoPago.getRequiereCodigo(),
                metodoPago.getEstado()
        );
    }
}
