package pe.ferrestock.pos.service;

import pe.ferrestock.pos.dto.VentaRequest;
import pe.ferrestock.pos.dto.VentaResponse;

import java.util.List;

public interface VentaService {

    VentaResponse registrar(Long idUsuario, VentaRequest request);

    List<VentaResponse> listarDelTurnoActual(Long idUsuario);
}
