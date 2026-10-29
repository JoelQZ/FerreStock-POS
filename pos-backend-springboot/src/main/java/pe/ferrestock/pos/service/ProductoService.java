package pe.ferrestock.pos.service;

import pe.ferrestock.pos.dto.ProductoResponse;

import java.util.List;

public interface ProductoService {
    List<ProductoResponse> buscarActivos(String buscar, Long categoria);
}
