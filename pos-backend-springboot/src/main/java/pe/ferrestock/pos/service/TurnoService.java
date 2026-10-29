package pe.ferrestock.pos.service;

import pe.ferrestock.pos.dto.AbrirTurnoRequest;
import pe.ferrestock.pos.dto.CerrarTurnoRequest;
import pe.ferrestock.pos.dto.TurnoResumenResponse;
import pe.ferrestock.pos.dto.TurnoResponse;

public interface TurnoService {

    TurnoResponse obtenerActual(Long idUsuario);

    TurnoResponse abrir(Long idUsuario, AbrirTurnoRequest request);

    TurnoResumenResponse obtenerResumen(Long idTurno, Long idUsuario);

    TurnoResumenResponse cerrar(Long idUsuario, CerrarTurnoRequest request);
}
