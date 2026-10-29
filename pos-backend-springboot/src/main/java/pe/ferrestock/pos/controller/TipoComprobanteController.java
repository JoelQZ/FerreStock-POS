package pe.ferrestock.pos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.ferrestock.pos.dto.TipoComprobanteResponse;
import pe.ferrestock.pos.service.TipoComprobanteService;

import java.util.List;

@RestController
@RequestMapping("/api/comprobantes")
public class TipoComprobanteController {

    private final TipoComprobanteService tipoComprobanteService;

    public TipoComprobanteController(TipoComprobanteService tipoComprobanteService) {
        this.tipoComprobanteService = tipoComprobanteService;
    }

    @GetMapping
    public List<TipoComprobanteResponse> listar() {
        return tipoComprobanteService.listarActivos();
    }
}
