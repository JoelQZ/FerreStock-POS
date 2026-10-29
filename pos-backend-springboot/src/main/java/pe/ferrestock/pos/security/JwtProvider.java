package pe.ferrestock.pos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtProvider {

    private final SecretKey claveFirma;

    public JwtProvider(@Value("${ferrestock.jwt.secret}") String secreto) {
        this.claveFirma = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    public UsuarioAutenticado validarToken(String token) {
        Jws<Claims> tokenFirmado = Jwts.parser()
                .verifyWith(claveFirma)
                .build()
                .parseSignedClaims(token);

        if (!Jwts.SIG.HS256.getId().equals(tokenFirmado.getHeader().getAlgorithm())) {
            throw new JwtException("El algoritmo del token no es válido");
        }

        Claims claims = tokenFirmado.getPayload();
        Long userId = obtenerUserId(claims.get("user_id"));
        String usuario = claims.get("usuario", String.class);
        String rol = claims.get("rol", String.class);

        if (usuario == null || usuario.isBlank()) {
            throw new JwtException("El token no contiene un usuario válido");
        }
        if (rol == null || rol.isBlank()) {
            throw new JwtException("El token no contiene un rol válido");
        }

        return new UsuarioAutenticado(userId, usuario, rol);
    }

    private Long obtenerUserId(Object valor) {
        if (valor instanceof Number numero) {
            return numero.longValue();
        }
        if (valor instanceof String texto) {
            try {
                return Long.valueOf(texto);
            } catch (NumberFormatException exception) {
                throw new JwtException("El token no contiene un user_id válido", exception);
            }
        }
        throw new JwtException("El token no contiene un user_id válido");
    }
}
