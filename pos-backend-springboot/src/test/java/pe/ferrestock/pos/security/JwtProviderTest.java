package pe.ferrestock.pos.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtProviderTest {

    private static final String SECRET = "clave-de-prueba-larga-para-validar-tokens-hs256-extra";
    private final JwtProvider jwtProvider = new JwtProvider(SECRET);

    @Test
    void extraeIdentidadYRolDeUnTokenHs256Valido() {
        String token = Jwts.builder()
                .claim("user_id", 42)
                .claim("usuario", "cajero")
                .claim("rol", "CAJERO")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        UsuarioAutenticado usuario = jwtProvider.validarToken(token);

        assertEquals(42L, usuario.userId());
        assertEquals("cajero", usuario.usuario());
        assertEquals("CAJERO", usuario.rol());
    }

    @Test
    void rechazaAlgoritmoDistintoDeHs256() {
        String token = Jwts.builder()
                .claim("user_id", 42)
                .claim("usuario", "cajero")
                .claim("rol", "CAJERO")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS384)
                .compact();

        assertThrows(io.jsonwebtoken.JwtException.class, () -> jwtProvider.validarToken(token));
    }

}
