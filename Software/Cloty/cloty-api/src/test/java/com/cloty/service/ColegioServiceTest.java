package com.cloty.service;

import com.cloty.domain.Colegio;
import com.cloty.dto.ColegioRequest;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.UsuarioRepository;
import com.cloty.web.error.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColegioServiceTest {

	@Mock private ColegioRepository colegioRepository;
	@Mock private UsuarioRepository usuarioRepository;
	@Mock private EmailService emailService;
	@Mock private CascadeEliminacionService cascadeEliminacionService;
	@Mock private UbicacionService ubicacionService;

	@InjectMocks
	private ColegioService colegioService;

	@Test
	void crearColegioNormalizaRutYEmail() {
		when(colegioRepository.existsByRut("76543210-1")).thenReturn(false);
		when(colegioRepository.save(any(Colegio.class))).thenAnswer(inv -> {
			Colegio c = inv.getArgument(0);
			c.setIdColegio(1);
			return c;
		});

		ColegioRequest req = new ColegioRequest(null, "76.543.210-1", "Escuela Demo",
				"Contacto@Colegio.CL", "+56912345678", null, "Av. Central 100");

		Colegio creado = colegioService.crear(req);

		assertEquals("76543210-1", creado.getRut());
		assertEquals("contacto@colegio.cl", creado.getEmail());
		verify(emailService).enviarInstructivoActivacionColegio(creado);
	}

	@Test
	void crearColegioRutDuplicadoLanzaConflicto() {
		when(colegioRepository.existsByRut("76543210-1")).thenReturn(true);

		ColegioRequest req = new ColegioRequest(null, "76543210-1", "Escuela", "a@b.cl", null, null, null);

		assertThrows(ConflictException.class, () -> colegioService.crear(req));
	}

	@Test
	void eliminarDelegaEnCascade() {
		colegioService.eliminar(3);
		verify(cascadeEliminacionService).eliminarColegioCompleto(3);
	}
}
