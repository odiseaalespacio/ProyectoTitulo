package com.cloty.service;

import com.cloty.domain.Alumno;
import com.cloty.domain.Apoderado;
import com.cloty.domain.Colegio;
import com.cloty.domain.ColegioApoderado;
import com.cloty.repo.AlumnoRepository;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.CodigoActivacionRepository;
import com.cloty.repo.CursoRepository;
import com.cloty.repo.EventoRepository;
import com.cloty.repo.NotificacionRepository;
import com.cloty.repo.SuperUsuarioRepository;
import com.cloty.repo.TarjetaRepository;
import com.cloty.repo.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CascadeEliminacionServiceTest {

	@Mock private ColegioRepository colegioRepository;
	@Mock private CursoRepository cursoRepository;
	@Mock private AlumnoRepository alumnoRepository;
	@Mock private ApoderadoRepository apoderadoRepository;
	@Mock private ColegioApoderadoRepository colegioApoderadoRepository;
	@Mock private TarjetaRepository tarjetaRepository;
	@Mock private EventoRepository eventoRepository;
	@Mock private NotificacionRepository notificacionRepository;
	@Mock private CodigoActivacionRepository codigoActivacionRepository;
	@Mock private SuperUsuarioRepository superUsuarioRepository;
	@Mock private UsuarioRepository usuarioRepository;

	@InjectMocks
	private CascadeEliminacionService cascadeEliminacionService;

	@Test
	void eliminarApoderadoCompletoBorraUsuario() {
		Apoderado apoderado = Apoderado.builder()
				.idApoderado(5)
				.idUsuario(99)
				.rut("12345678-5")
				.nombres("Ana")
				.apellidos("Pérez")
				.build();
		when(apoderadoRepository.existsById(5)).thenReturn(true);
		when(apoderadoRepository.findById(5)).thenReturn(Optional.of(apoderado));
		when(alumnoRepository.findByIdApoderadoOrderByApellidosAscNombresAsc(5)).thenReturn(List.of());
		when(notificacionRepository.findByIdApoderadoOrderByFechaEnvioDesc(5)).thenReturn(List.of());
		when(colegioApoderadoRepository.findByIdApoderado(5)).thenReturn(List.of());
		when(usuarioRepository.existsById(99)).thenReturn(true);

		cascadeEliminacionService.eliminarApoderadoCompleto(5);

		verify(apoderadoRepository).deleteById(5);
		verify(usuarioRepository).deleteById(99);
	}

	@Test
	void eliminarAlumnoCompletoRevisaApoderadoHuerfano() {
		Alumno alumno = Alumno.builder()
				.idAlumno(10)
				.idApoderado(5)
				.idColegio(1)
				.idCurso(2)
				.rut("11111111-1")
				.nombres("Pedro")
				.apellidos("Gómez")
				.build();
		Apoderado apoderado = Apoderado.builder()
				.idApoderado(5)
				.idUsuario(99)
				.rut("12345678-5")
				.nombres("Ana")
				.apellidos("Pérez")
				.build();

		when(alumnoRepository.existsById(10)).thenReturn(true);
		when(alumnoRepository.findById(10)).thenReturn(Optional.of(alumno));
		when(tarjetaRepository.findByIdAlumnoOrderByFechaAsignacionDesc(10)).thenReturn(List.of());
		when(apoderadoRepository.existsById(5)).thenReturn(true);
		when(colegioApoderadoRepository.findByIdApoderado(5)).thenReturn(List.of());
		when(alumnoRepository.findByIdApoderadoOrderByApellidosAscNombresAsc(5)).thenReturn(List.of());
		when(apoderadoRepository.findById(5)).thenReturn(Optional.of(apoderado));
		when(usuarioRepository.existsById(99)).thenReturn(true);

		cascadeEliminacionService.eliminarAlumnoCompleto(10);

		verify(alumnoRepository).deleteById(10);
		verify(apoderadoRepository).deleteById(5);
		verify(usuarioRepository).deleteById(99);
	}

	@Test
	void eliminarColegioCompletoBorraUsuarioDelColegio() {
		Colegio colegio = Colegio.builder()
				.idColegio(1)
				.idUsuario(50)
				.nombre("Colegio Test")
				.rut("76543210-1")
				.email("colegio@test.cl")
				.build();
		when(colegioRepository.findById(1)).thenReturn(Optional.of(colegio));
		when(colegioApoderadoRepository.findByIdColegio(1)).thenReturn(List.of());
		when(alumnoRepository.findByIdColegioOrderByApellidosAscNombresAsc(1)).thenReturn(List.of());
		when(cursoRepository.findByIdColegioOrderByNombreAsc(1)).thenReturn(List.of());
		when(usuarioRepository.existsById(50)).thenReturn(true);

		cascadeEliminacionService.eliminarColegioCompleto(1);

		verify(colegioRepository).deleteById(1);
		verify(usuarioRepository).deleteById(50);
	}

	@Test
	void revisarApoderadoHuerfanoNoEliminaSiTieneAlumnos() {
		when(apoderadoRepository.existsById(5)).thenReturn(true);
		when(colegioApoderadoRepository.findByIdApoderado(5)).thenReturn(List.of());
		when(alumnoRepository.findByIdApoderadoOrderByApellidosAscNombresAsc(5))
				.thenReturn(List.of(Alumno.builder().idAlumno(1).idApoderado(5).idColegio(1).idCurso(1)
						.rut("1-9").nombres("A").apellidos("B").build()));

		cascadeEliminacionService.revisarApoderadoHuerfano(5);

		verify(apoderadoRepository, never()).deleteById(anyInt());
		verify(usuarioRepository, never()).deleteById(anyInt());
	}

	@Test
	void eliminarColegioIncluyeApoderadosDeAlumnosAlRevisarHuerfanos() {
		Colegio colegio = Colegio.builder().idColegio(1).nombre("Test").email("a@b.cl").build();
		Alumno alumno = Alumno.builder().idAlumno(7).idApoderado(5).idColegio(1).idCurso(2)
				.rut("2-7").nombres("Lu").apellidos("Ma").build();
		Apoderado apoderado = Apoderado.builder().idApoderado(5).idUsuario(99)
				.rut("3-3").nombres("Apo").apellidos("Do").build();

		when(colegioRepository.findById(1)).thenReturn(Optional.of(colegio));
		when(colegioApoderadoRepository.findByIdColegio(1)).thenReturn(List.of());
		when(alumnoRepository.findByIdColegioOrderByApellidosAscNombresAsc(1)).thenReturn(List.of(alumno));
		when(alumnoRepository.existsById(7)).thenReturn(true);
		when(alumnoRepository.findById(7)).thenReturn(Optional.of(alumno));
		when(tarjetaRepository.findByIdAlumnoOrderByFechaAsignacionDesc(7)).thenReturn(List.of());
		when(cursoRepository.findByIdColegioOrderByNombreAsc(1)).thenReturn(List.of());
		when(apoderadoRepository.existsById(5)).thenReturn(true, true, false);
		when(colegioApoderadoRepository.findByIdApoderado(5)).thenReturn(List.of());
		when(alumnoRepository.findByIdApoderadoOrderByApellidosAscNombresAsc(5)).thenReturn(List.of());
		when(apoderadoRepository.findById(5)).thenReturn(Optional.of(apoderado));
		when(notificacionRepository.findByIdApoderadoOrderByFechaEnvioDesc(5)).thenReturn(List.of());
		when(usuarioRepository.existsById(99)).thenReturn(true);

		cascadeEliminacionService.eliminarColegioCompleto(1);

		verify(apoderadoRepository).deleteById(5);
		verify(usuarioRepository).deleteById(99);
	}
}
