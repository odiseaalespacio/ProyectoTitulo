package com.cloty.service;

import com.cloty.domain.Alumno;
import com.cloty.domain.Colegio;
import com.cloty.domain.ColegioApoderado;
import com.cloty.domain.Curso;
import com.cloty.domain.EstadoNotificacion;
import com.cloty.domain.EstadoTarjeta;
import com.cloty.domain.Notificacion;
import com.cloty.domain.TipoEvento;
import com.cloty.repo.AlumnoRepository;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.CursoRepository;
import com.cloty.repo.EventoRepository;
import com.cloty.repo.NotificacionRepository;
import com.cloty.repo.TarjetaRepository;
import com.cloty.web.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColegioOperacionServiceTest {

	@Mock private TarjetaRepository tarjetaRepository;
	@Mock private AlumnoRepository alumnoRepository;
	@Mock private ApoderadoRepository apoderadoRepository;
	@Mock private CursoRepository cursoRepository;
	@Mock private ColegioRepository colegioRepository;
	@Mock private ColegioApoderadoRepository colegioApoderadoRepository;
	@Mock private EventoRepository eventoRepository;
	@Mock private EventoService eventoService;
	@Mock private NotificacionService notificacionService;
	@Mock private NotificacionRepository notificacionRepository;

	@InjectMocks
	private ColegioOperacionService colegioOperacionService;

	@Test
	void dashboardAgregaMetricasDetalladas() {
		Colegio colegio = Colegio.builder().idColegio(1).nombre("Liceo Norte").email("a@b.cl").build();
		Curso curso = Curso.builder().idCurso(2).idColegio(1).nombre("3°A").nivel("3° Medio").build();
		Alumno alumno = Alumno.builder().idAlumno(10).idColegio(1).idCurso(2).idApoderado(5)
				.rut("1-9").nombres("Ana").apellidos("López").build();
		Notificacion enviada = Notificacion.builder().idNotificacion(1).idEvento(1).idApoderado(5)
				.titulo("Aviso").mensaje("Prenda encontrada").estado(EstadoNotificacion.ENVIADA).build();
		Notificacion pendiente = Notificacion.builder().idNotificacion(2).idEvento(2).idApoderado(5)
				.titulo("Pendiente").mensaje("Por enviar").estado(EstadoNotificacion.PENDIENTE).build();

		when(colegioRepository.findById(1)).thenReturn(Optional.of(colegio));
		when(alumnoRepository.findByIdColegioOrderByApellidosAscNombresAsc(1)).thenReturn(List.of(alumno));
		when(tarjetaRepository.countAlumnosConTarjeta(1)).thenReturn(1L);
		when(colegioApoderadoRepository.findByIdColegio(1))
				.thenReturn(List.of(ColegioApoderado.builder().idColegioApoderado(1).idColegio(1).idApoderado(5).build()));
		when(apoderadoRepository.findAllById(List.of(5))).thenReturn(List.of());
		when(cursoRepository.findByIdColegioOrderByNombreAsc(1)).thenReturn(List.of(curso));
		when(alumnoRepository.findByIdCursoOrderByApellidosAscNombresAsc(2)).thenReturn(List.of(alumno));
		when(tarjetaRepository.findByIdAlumnoOrderByFechaAsignacionDesc(10)).thenReturn(List.of());
		when(eventoRepository.countByColegioAndTipoDesde(eq(1), eq(TipoEvento.PRENDA_ENCONTRADA), any())).thenReturn(2L);
		when(eventoRepository.countByColegioAndTipoDesde(eq(1), eq(TipoEvento.PRENDA_RECUPERADA), any())).thenReturn(1L);
		when(eventoRepository.findByColegioId(eq(1), any(Pageable.class))).thenReturn(List.of());
		when(notificacionRepository.findByColegioId(1)).thenReturn(List.of(enviada, pendiente));
		when(tarjetaRepository.countByColegioAndEstado(1, EstadoTarjeta.ACTIVA)).thenReturn(3L);
		when(tarjetaRepository.countByColegioAndEstado(1, EstadoTarjeta.PERDIDA)).thenReturn(1L);
		when(tarjetaRepository.countByColegioAndEstado(1, EstadoTarjeta.DESACTIVADA)).thenReturn(0L);
		when(eventoRepository.countByColegioAndTipo(1, TipoEvento.PRENDA_ENCONTRADA)).thenReturn(10L);
		when(eventoRepository.countByColegioAndTipo(1, TipoEvento.PRENDA_RECUPERADA)).thenReturn(4L);

		var dash = colegioOperacionService.dashboard(1);

		assertEquals("Liceo Norte", dash.nombreColegio());
		assertEquals(1, dash.totalAlumnos());
		assertEquals(1, dash.totalApoderados());
		assertEquals(1, dash.totalCursos());
		assertEquals(1, dash.notificacionesEnviadas());
		assertEquals(1, dash.notificacionesPendientes());
		assertEquals(1, dash.resumenCursos().size());
		assertEquals(0, dash.alumnosSinTarjeta());
	}

	@Test
	void dashboardColegioInexistente() {
		when(colegioRepository.findById(99)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> colegioOperacionService.dashboard(99));
	}
}
