package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.ferrestock.pos.entity.TipoComprobante;

public record TipoComprobanteResponse(
        @JsonProperty("id_tipo") Long idTipo,
        @JsonProperty("nombre") String nombre,
        @JsonProperty("serie") String serie,
        @JsonProperty("correlativo_actual") Integer correlativoActual
) {
    public static TipoComprobanteResponse from(TipoComprobante tipoComprobante) {
        return new TipoComprobanteResponse(
                tipoComprobante.getIdTipo(),
                tipoComprobante.getNombre(),
                tipoComprobante.getSerie(),
                tipoComprobante.getCorrelativoActual()
        );
    }
}
