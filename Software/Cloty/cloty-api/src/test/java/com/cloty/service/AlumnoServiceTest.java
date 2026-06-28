package com.cloty.service;

import com.cloty.domain.Alumno;
import com.cloty.domain.Curso;
import com.cloty.dto.AlumnoRequest;
import com.cloty.repo.AlumnoRepository;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.CursoRepository;
import com.cloty.web.error.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlumnoServiceTest {

	@Mock private AlumnoRepository alumnoRepository;
	@Mock private ColegioRepository colegioRepository;
	@Mock private ApoderadoRepository apoderadoRepository;
	@Mock private CursoRepository cursoRepository;
	@Mock private CascadeEliminacionService cascadeEliminacionService;

	@InjectMocks
	private AlumnoService alumnoService;

	@Test
	void crearAlumnoValidaCursoDelColegio() {
		when(colegioRepository.existsById(1)).thenReturn(true);
		when(apoderadoRepository.existsById(2)).thenReturn(true);
		when(cursoRepository.findById(3)).thenReturn(Optional.of(
				Curso.builder().idCurso(3).idColegio(99).nombre("1°A").build()));

		AlumnoRequest req = new AlumnoRequest(1, 2, 3, "12345678-5", "Ana", "Pérez", true);

		assertThrows(ConflictException.class, () -> alumnoService.crear(req));
	}

	@Test
	void eliminarDelegaEnCascade() {
		alumnoService.eliminar(8);
		verify(cascadeEliminacionService).eliminarAlumnoCompleto(8);
	}
}
