package pe.ferrestock.pos.service;

import pe.ferrestock.pos.dto.TipoComprobanteResponse;

import java.util.List;

public interface TipoComprobanteService {
    List<TipoComprobanteResponse> listarActivos();
}
