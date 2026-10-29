package pe.ferrestock.pos.service;

import pe.ferrestock.pos.dto.CategoriaResponse;

import java.util.List;

public interface CategoriaService {
    List<CategoriaResponse> listarActivas();
}
