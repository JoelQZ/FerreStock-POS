package pe.ferrestock.pos.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.ferrestock.pos.entity.TipoMovimientoCaja;
import pe.ferrestock.pos.repository.MovimientoCajaRepository;
import pe.ferrestock.pos.service.MovimientoCajaService;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class MovimientoCajaServiceImpl implements MovimientoCajaService {

    private final MovimientoCajaRepository movimientoCajaRepository;

    public MovimientoCajaServiceImpl(MovimientoCajaRepository movimientoCajaRepository) {
        this.movimientoCajaRepository = movimientoCajaRepository;
    }

    @Override
    public TotalesMovimientoCaja obtenerTotales(Long idTurno) {
        BigDecimal ingresos = movimientoCajaRepository.sumarPorTurnoYTipo(
                idTurno,
                TipoMovimientoCaja.INGRESO.name()
        );
        BigDecimal egresos = movimientoCajaRepository.sumarPorTurnoYTipo(
                idTurno,
                TipoMovimientoCaja.EGRESO.name()
        );
        return new TotalesMovimientoCaja(ingresos, egresos);
    }
}
