package pe.ferrestock.pos.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.ferrestock.pos.dto.ProductoResponse;
import pe.ferrestock.pos.repository.ProductoRepository;
import pe.ferrestock.pos.service.ProductoService;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public List<ProductoResponse> buscarActivos(String buscar, Long categoria) {
        return productoRepository.buscarActivos(buscar, categoria).stream()
                .map(ProductoResponse::from)
                .toList();
    }
}
