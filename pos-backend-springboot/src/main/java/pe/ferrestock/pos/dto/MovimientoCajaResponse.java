package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.ferrestock.pos.entity.MovimientoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoCajaResponse(
        @JsonProperty("id_mov_caja") Long idMovCaja,
        @JsonProperty("id_turno") Long idTurno,
        @JsonProperty("id_usuario") Long idUsuario,
        @JsonProperty("tipo") String tipo,
        @JsonProperty("monto") BigDecimal monto,
        @JsonProperty("motivo") String motivo,
        @JsonProperty("fecha") LocalDateTime fecha
) {
    public static MovimientoCajaResponse from(MovimientoCaja movimiento) {
        return new MovimientoCajaResponse(
                movimiento.getIdMovCaja(),
                movimiento.getTurno().getIdTurno(),
                movimiento.getIdUsuario(),
                movimiento.getTipo().name(),
                movimiento.getMonto(),
                movimiento.getMotivo(),
                movimiento.getFecha()
        );
    }
}
