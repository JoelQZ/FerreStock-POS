package pe.ferrestock.pos;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "JWT_SECRET=clave-de-prueba-larga-para-validar-tokens-hs256-extra")
@AutoConfigureMockMvc
class PosBackendSpringbootApplicationTests {

	private static final String JWT_SECRET = "clave-de-prueba-larga-para-validar-tokens-hs256-extra";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void rechazaSolicitudesSinTokenConElContratoDjango() throws Exception {
		mockMvc.perform(get("/api/categorias"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().json("{\"detail\":\"Token JWT inválido o expirado\"}"));

		mockMvc.perform(get("/api/categorias").header("Authorization", "Bearer token-invalido"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().json("{\"detail\":\"Token JWT inválido o expirado\"}"));
	}

	@Test
	void permiteRolesAutorizadosYRestringeLosDemas() throws Exception {
		mockMvc.perform(get("/api/categorias")
						.header("Authorization", "Bearer " + token("CAJERO"))
						.header("Origin", "http://localhost:5174"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5174"));

		mockMvc.perform(get("/api/categorias")
						.header("Authorization", "Bearer " + token("INVITADO")))
				.andExpect(status().isForbidden())
				.andExpect(content().json("{\"detail\":\"No tienes permisos para realizar esta acción\"}"));
	}

	private String token(String rol) {
		return Jwts.builder()
				.claim("user_id", 42)
				.claim("usuario", "usuario-prueba")
				.claim("rol", rol)
				.signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
				.compact();
	}
}
