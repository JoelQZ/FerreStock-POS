package pe.ferrestock.pos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ferrestock.pos.dto.ProductoResponse;
import pe.ferrestock.pos.service.ProductoService;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoResponse> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Long categoria
    ) {
        return productoService.buscarActivos(buscar, categoria);
    }
}
