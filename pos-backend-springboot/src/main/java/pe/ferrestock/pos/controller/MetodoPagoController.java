package pe.ferrestock.pos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ferrestock.pos.dto.MetodoPagoResponse;
import pe.ferrestock.pos.service.MetodoPagoService;

import java.util.List;

@RestController
@RequestMapping("/api/metodos-pago")
public class MetodoPagoController {

    private final MetodoPagoService metodoPagoService;

    public MetodoPagoController(MetodoPagoService metodoPagoService) {
        this.metodoPagoService = metodoPagoService;
    }

    @GetMapping
    public List<MetodoPagoResponse> listar() {
        return metodoPagoService.listarActivos();
    }
}
