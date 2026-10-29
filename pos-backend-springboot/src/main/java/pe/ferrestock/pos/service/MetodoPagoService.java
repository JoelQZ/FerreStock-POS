package pe.ferrestock.pos.service;

import pe.ferrestock.pos.dto.MetodoPagoResponse;

import java.util.List;

public interface MetodoPagoService {
    List<MetodoPagoResponse> listarActivos();
}
