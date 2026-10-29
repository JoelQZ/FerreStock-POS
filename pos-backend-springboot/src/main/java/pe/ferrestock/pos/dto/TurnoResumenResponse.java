package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.ferrestock.pos.entity.Turno;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TurnoResumenResponse(
        @JsonProperty("id_turno") Long idTurno,
        @JsonProperty("id_usuario") Long idUsuario,
        @JsonProperty("apertura") LocalDateTime apertura,
        @JsonProperty("monto_inicial") BigDecimal montoInicial,
        @JsonProperty("cierre") LocalDateTime cierre,
        @JsonProperty("ventas_efectivo") BigDecimal ventasEfectivo,
        @JsonProperty("otros_pagos") BigDecimal otrosPagos,
        @JsonProperty("ingresos_caja") BigDecimal ingresosCaja,
        @JsonProperty("egresos_caja") BigDecimal egresosCaja,
        @JsonProperty("monto_esperado") BigDecimal montoEsperado,
        @JsonProperty("monto_real") BigDecimal montoReal,
        @JsonProperty("diferencia") BigDecimal diferencia,
        @JsonProperty("estado") String estado
) {
    public static TurnoResumenResponse from(
            Turno turno,
            BigDecimal ventasEfectivo,
            BigDecimal otrosPagos,
            BigDecimal ingresosCaja,
            BigDecimal egresosCaja,
            BigDecimal montoEsperado
    ) {
        return new TurnoResumenResponse(
                turno.getIdTurno(),
                turno.getIdUsuario(),
                turno.getApertura(),
                turno.getMontoInicial(),
                turno.getCierre(),
                ventasEfectivo,
                otrosPagos,
                ingresosCaja,
                egresosCaja,
                montoEsperado,
                turno.getMontoReal(),
                turno.getDiferencia(),
                turno.getEstado().name()
        );
    }
}
