package com.cloty.web;

import com.cloty.config.DatabaseBootstrap;
import com.cloty.config.UbicacionBootstrap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DatabaseBootstrap databaseBootstrap;

	@MockitoBean
	private UbicacionBootstrap ubicacionBootstrap;

	@Test
	void loginConCredencialesInvalidasRetorna400() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"identificador":"noexiste","password":"mal"}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void registroApoderadoValidaPasswordMinima() throws Exception {
		mockMvc.perform(post("/api/auth/registro-apoderado")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "password":"abc",
								  "rut":"12345678-5",
								  "nombres":"Ana",
								  "apellidos":"López"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());
	}
}
