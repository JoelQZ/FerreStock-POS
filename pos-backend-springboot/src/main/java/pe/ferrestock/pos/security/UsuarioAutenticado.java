package pe.ferrestock.pos.security;

import java.security.Principal;

public record UsuarioAutenticado(Long userId, String usuario, String rol) implements Principal {

    @Override
    public String getName() {
        return usuario;
    }
}
