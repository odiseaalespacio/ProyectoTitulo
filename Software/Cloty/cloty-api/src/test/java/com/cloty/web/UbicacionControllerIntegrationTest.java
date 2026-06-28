package com.cloty.web;

import com.cloty.config.UbicacionBootstrap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UbicacionControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UbicacionBootstrap ubicacionBootstrap;

	@Test
	void listarRegionesDevuelveJson() throws Exception {
		mockMvc.perform(get("/api/ubicacion/regiones"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}
}
