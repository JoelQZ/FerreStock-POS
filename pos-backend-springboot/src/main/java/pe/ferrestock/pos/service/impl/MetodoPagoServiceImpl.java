package pe.ferrestock.pos.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.ferrestock.pos.dto.MetodoPagoResponse;
import pe.ferrestock.pos.repository.MetodoPagoRepository;
import pe.ferrestock.pos.service.MetodoPagoService;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MetodoPagoServiceImpl implements MetodoPagoService {

    private final MetodoPagoRepository metodoPagoRepository;

    public MetodoPagoServiceImpl(MetodoPagoRepository metodoPagoRepository) {
        this.metodoPagoRepository = metodoPagoRepository;
    }

    @Override
    public List<MetodoPagoResponse> listarActivos() {
        return metodoPagoRepository.findByEstadoTrue().stream()
                .map(MetodoPagoResponse::from)
                .toList();
    }
}
