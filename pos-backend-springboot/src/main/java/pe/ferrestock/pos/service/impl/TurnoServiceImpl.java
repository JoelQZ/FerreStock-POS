package pe.ferrestock.pos.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.ferrestock.pos.dto.AbrirTurnoRequest;
import pe.ferrestock.pos.dto.CerrarTurnoRequest;
import pe.ferrestock.pos.dto.TurnoResumenResponse;
import pe.ferrestock.pos.dto.TurnoResponse;
import pe.ferrestock.pos.entity.EstadoTurno;
import pe.ferrestock.pos.entity.Turno;
import pe.ferrestock.pos.exception.ResourceNotFoundException;
import pe.ferrestock.pos.repository.TurnoRepository;
import pe.ferrestock.pos.service.MovimientoCajaService;
import pe.ferrestock.pos.service.TurnoService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TurnoServiceImpl implements TurnoService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final String MENSAJE_TURNO_ABIERTO =
            "El usuario ya tiene un turno abierto";

    private final TurnoRepository turnoRepository;
    private final MovimientoCajaService movimientoCajaService;

    public TurnoServiceImpl(
            TurnoRepository turnoRepository,
            MovimientoCajaService movimientoCajaService
    ) {
        this.turnoRepository = turnoRepository;
        this.movimientoCajaService = movimientoCajaService;
    }

    @Override
    @Transactional(readOnly = true)
    public TurnoResponse obtenerActual(Long idUsuario) {
        return turnoRepository.findFirstByIdUsuarioAndEstadoOrderByAperturaDesc(
                        idUsuario,
                        EstadoTurno.ABIERTO
                )
                .map(TurnoResponse::from)
                .orElse(null);
    }

    @Override
    @Transactional
    public TurnoResponse abrir(Long idUsuario, AbrirTurnoRequest request) {
        Long usuarioBloqueado = turnoRepository.bloquearUsuario(idUsuario);
        if (usuarioBloqueado == null) {
            throw new ResourceNotFoundException("El usuario autenticado no existe");
        }
        if (turnoRepository.existsByIdUsuarioAndEstado(idUsuario, EstadoTurno.ABIERTO)) {
            throw new IllegalStateException(MENSAJE_TURNO_ABIERTO);
        }

        Turno turno = Turno.abrir(idUsuario, request.montoInicial(), LocalDateTime.now(ZONA_LIMA));
        return TurnoResponse.from(turnoRepository.save(turno));
    }

    @Override
    @Transactional(readOnly = true)
    public TurnoResumenResponse obtenerResumen(Long idTurno, Long idUsuario) {
        Turno turno = turnoRepository.findByIdTurnoAndIdUsuarioAndEstado(
                        idTurno,
                        idUsuario,
                        EstadoTurno.ABIERTO
                )
                .or(() -> turnoRepository.findByIdTurnoAndIdUsuarioAndEstado(
                        idTurno,
                        idUsuario,
                        EstadoTurno.CERRADO
                ))
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));
        return crearResumen(turno);
    }

    @Override
    @Transactional
    public TurnoResumenResponse cerrar(Long idUsuario, CerrarTurnoRequest request) {
        Turno turno = turnoRepository.buscarTurnoAbiertoParaCerrar(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes un turno abierto"));

        TurnoResumenResponse resumen = crearResumen(turno);
        turno.cerrar(resumen.montoEsperado(), request.montoReal(), LocalDateTime.now(ZONA_LIMA));
        turnoRepository.save(turno);
        return crearResumen(turno);
    }

    private TurnoResumenResponse crearResumen(Turno turno) {
        BigDecimal ventasEfectivo = turnoRepository.sumarVentasEfectivo(turno.getIdTurno());
        BigDecimal otrosPagos = turnoRepository.sumarOtrosPagos(turno.getIdTurno());
        MovimientoCajaService.TotalesMovimientoCaja movimientos =
                movimientoCajaService.obtenerTotales(turno.getIdTurno());
        BigDecimal montoEsperado = turno.getMontoInicial()
                .add(ventasEfectivo)
                .add(movimientos.ingresos())
                .subtract(movimientos.egresos());

        return TurnoResumenResponse.from(
                turno,
                ventasEfectivo,
                otrosPagos,
                movimientos.ingresos(),
                movimientos.egresos(),
                montoEsperado
        );
    }
}
