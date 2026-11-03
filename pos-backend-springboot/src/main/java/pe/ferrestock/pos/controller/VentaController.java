package pe.ferrestock.pos.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.ferrestock.pos.dto.VentaRequest;
import pe.ferrestock.pos.dto.VentaResponse;
import pe.ferrestock.pos.security.UsuarioAutenticado;
import pe.ferrestock.pos.service.VentaService;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VentaResponse registrar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody VentaRequest request
    ) {
        return ventaService.registrar(usuario.userId(), request);
    }

    @GetMapping("/turno-actual")
    public List<VentaResponse> listarDelTurnoActual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ventaService.listarDelTurnoActual(usuario.userId());
    }
}
