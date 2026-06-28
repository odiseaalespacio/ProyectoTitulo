package com.cloty.web;

import com.cloty.config.DatabaseBootstrap;
import com.cloty.config.UbicacionBootstrap;
import com.cloty.domain.RolUsuario;
import com.cloty.domain.Usuario;
import com.cloty.dto.ColegioRequest;
import com.cloty.repo.UsuarioRepository;
import com.cloty.security.JwtService;
import com.cloty.security.ClotyUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ColegioControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtService jwtService;

	@MockitoBean
	private DatabaseBootstrap databaseBootstrap;

	@MockitoBean
	private UbicacionBootstrap ubicacionBootstrap;

	private String tokenAdmin;

	@BeforeEach
	void setUp() {
		usuarioRepository.deleteAll();
		Usuario admin = usuarioRepository.save(Usuario.builder()
				.username("adminit")
				.rut("11111111-1")
				.passwordHash(passwordEncoder.encode("admin123"))
				.rol(RolUsuario.ADMINISTRADOR)
				.estado(true)
				.build());
		ClotyUserDetails principal = new ClotyUserDetails(
				admin.getIdUsuario(),
				admin.getUsername(),
				admin.getPasswordHash(),
				true,
				admin.getRol(),
				null,
				null);
		tokenAdmin = jwtService.generateToken(principal);
	}

	@Test
	void crearYListarColegioConTokenAdmin() throws Exception {
		String body = """
				{
				  "rut": "12345678-5",
				  "nombre": "Colegio Integración",
				  "email": "colegio@integracion.cl",
				  "telefono": "+56912345678",
				  "calleNumero": "Av. Prueba 100"
				}
				""";

		mockMvc.perform(post("/api/colegios")
						.header("Authorization", "Bearer " + tokenAdmin)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nombre").value("Colegio Integración"));

		mockMvc.perform(get("/api/colegios")
						.header("Authorization", "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].email").value("colegio@integracion.cl"));
	}

	@Test
	void crearColegioSinTokenRetorna401() throws Exception {
		mockMvc.perform(post("/api/colegios")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "rut": "12345678-5",
								  "nombre": "X",
								  "email": "x@y.cl",
								  "telefono": "+56987654321",
								  "calleNumero": "Calle 1"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}
}
