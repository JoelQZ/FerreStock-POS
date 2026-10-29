package pe.ferrestock.pos.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.ferrestock.pos.dto.CategoriaResponse;
import pe.ferrestock.pos.repository.CategoriaRepository;
import pe.ferrestock.pos.service.CategoriaService;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<CategoriaResponse> listarActivas() {
        return categoriaRepository.findByEstadoTrue().stream()
                .map(CategoriaResponse::from)
                .toList();
    }
}
