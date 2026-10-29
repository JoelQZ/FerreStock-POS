package pe.ferrestock.pos.service;

import java.math.BigDecimal;

public interface MovimientoCajaService {

    TotalesMovimientoCaja obtenerTotales(Long idTurno);

    record TotalesMovimientoCaja(BigDecimal ingresos, BigDecimal egresos) {
    }
}
