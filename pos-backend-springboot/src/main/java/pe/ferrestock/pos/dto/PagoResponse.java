package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.ferrestock.pos.entity.Pago;

import java.math.BigDecimal;

public record PagoResponse(
        @JsonProperty("id_pago") Long idPago,
        @JsonProperty("metodo") Long metodo,
        @JsonProperty("monto") BigDecimal monto,
        @JsonProperty("monto_recibido") BigDecimal montoRecibido,
        @JsonProperty("vuelto") BigDecimal vuelto,
        @JsonProperty("codigo_operacion") String codigoOperacion
) {
    public static PagoResponse from(Pago pago) {
        return new PagoResponse(
                pago.getIdPago(),
                pago.getIdMetodo(),
                pago.getMonto(),
                pago.getMontoRecibido(),
                pago.getVuelto(),
                pago.getCodigoOperacion()
        );
    }
}
