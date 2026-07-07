package com.cloty.service;

import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApoderadoServiceTest {

	@Mock private ApoderadoRepository apoderadoRepository;
	@Mock private ColegioApoderadoRepository colegioApoderadoRepository;
	@Mock private ColegioRepository colegioRepository;
	@Mock private UsuarioRepository usuarioRepository;
	@Mock private CascadeEliminacionService cascadeEliminacionService;
	@Mock private UbicacionService ubicacionService;

	@InjectMocks
	private ApoderadoService apoderadoService;

	@Test
	void eliminarDelegaEnCascade() {
		apoderadoService.eliminar(4);
		verify(cascadeEliminacionService).eliminarApoderadoCompleto(4);
	}
}
