package pe.ferrestock.pos;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

	@Test
	void turnoActualDevuelveNullCuandoElUsuarioNoTieneTurnoAbierto() throws Exception {
		mockMvc.perform(get("/api/turnos/actual")
						.header("Authorization", "Bearer " + token("CAJERO", Long.MAX_VALUE)))
				.andExpect(status().isOk())
				.andExpect(content().string("null"));
	}

	@Test
	void validaMontoInicialConNombreDeCampoSnakeCase() throws Exception {
		mockMvc.perform(post("/api/turnos/abrir")
						.header("Authorization", "Bearer " + token("CAJERO"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"monto_inicial\":\"-1.00\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().json(
						"{\"monto_inicial\":\"El monto inicial debe ser mayor o igual a 0.00\"}"
				));
	}

	private String token(String rol) {
		return token(rol, 42L);
	}

	private String token(String rol, Long userId) {
		return Jwts.builder()
				.claim("user_id", userId)
				.claim("usuario", "usuario-prueba")
				.claim("rol", rol)
				.signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
				.compact();
	}
}
