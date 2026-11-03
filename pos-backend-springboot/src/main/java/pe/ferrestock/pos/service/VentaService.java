package pe.ferrestock.pos.service;

import pe.ferrestock.pos.dto.VentaRequest;
import pe.ferrestock.pos.dto.VentaResponse;

import java.util.List;

public interface VentaService {

    VentaResponse registrar(Long idUsuario, VentaRequest request);

    VentaResponse anular(Long idUsuario, Long idVenta, String motivo);

    List<VentaResponse> listarDelTurnoActual(Long idUsuario);
}
