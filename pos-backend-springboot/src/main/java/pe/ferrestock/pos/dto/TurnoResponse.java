package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.ferrestock.pos.entity.Turno;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TurnoResponse(
        @JsonProperty("id_turno") Long idTurno,
        @JsonProperty("id_usuario") Long idUsuario,
        @JsonProperty("apertura") LocalDateTime apertura,
        @JsonProperty("monto_inicial") BigDecimal montoInicial,
        @JsonProperty("cierre") LocalDateTime cierre,
        @JsonProperty("monto_esperado") BigDecimal montoEsperado,
        @JsonProperty("monto_real") BigDecimal montoReal,
        @JsonProperty("diferencia") BigDecimal diferencia,
        @JsonProperty("estado") String estado
) {
    public static TurnoResponse from(Turno turno) {
        return new TurnoResponse(
                turno.getIdTurno(),
                turno.getIdUsuario(),
                turno.getApertura(),
                turno.getMontoInicial(),
                turno.getCierre(),
                turno.getMontoEsperado(),
                turno.getMontoReal(),
                turno.getDiferencia(),
                turno.getEstado().name()
        );
    }
}
