package pe.ferrestock.pos.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.ferrestock.pos.dto.TipoComprobanteResponse;
import pe.ferrestock.pos.repository.TipoComprobanteRepository;
import pe.ferrestock.pos.service.TipoComprobanteService;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TipoComprobanteServiceImpl implements TipoComprobanteService {

    private final TipoComprobanteRepository tipoComprobanteRepository;

    public TipoComprobanteServiceImpl(TipoComprobanteRepository tipoComprobanteRepository) {
        this.tipoComprobanteRepository = tipoComprobanteRepository;
    }

    @Override
    public List<TipoComprobanteResponse> listarActivos() {
        return tipoComprobanteRepository.findAll().stream()
                .map(TipoComprobanteResponse::from)
                .toList();
    }
}
