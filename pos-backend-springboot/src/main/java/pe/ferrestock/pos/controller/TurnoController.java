package pe.ferrestock.pos.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.ferrestock.pos.dto.AbrirTurnoRequest;
import pe.ferrestock.pos.dto.CerrarTurnoRequest;
import pe.ferrestock.pos.dto.TurnoResumenResponse;
import pe.ferrestock.pos.dto.TurnoResponse;
import pe.ferrestock.pos.security.UsuarioAutenticado;
import pe.ferrestock.pos.service.TurnoService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @GetMapping("/actual")
    public TurnoResponse obtenerActual(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            HttpServletResponse response
    ) throws IOException {
        TurnoResponse turno = turnoService.obtenerActual(usuario.userId());
        if (turno == null) {
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("null");
            response.flushBuffer();
        }
        return turno;
    }

    @PostMapping("/abrir")
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoResponse abrir(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody AbrirTurnoRequest request
    ) {
        return turnoService.abrir(usuario.userId(), request);
    }

    @GetMapping("/{id}/resumen")
    public TurnoResumenResponse obtenerResumen(
            @PathVariable("id") Long idTurno,
            @AuthenticationPrincipal UsuarioAutenticado usuario
    ) {
        return turnoService.obtenerResumen(idTurno, usuario.userId());
    }

    @PostMapping("/cerrar")
    public TurnoResumenResponse cerrar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody CerrarTurnoRequest request
    ) {
        return turnoService.cerrar(usuario.userId(), request);
    }
}
