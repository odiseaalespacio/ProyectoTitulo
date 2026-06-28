package com.cloty.service;

import com.cloty.domain.Alumno;
import com.cloty.domain.Apoderado;
import com.cloty.domain.Colegio;
import com.cloty.domain.TipoEntidadActivacion;
import com.cloty.repo.AlumnoRepository;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.CodigoActivacionRepository;
import com.cloty.repo.ColegioApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.CursoRepository;
import com.cloty.repo.EventoRepository;
import com.cloty.repo.NotificacionRepository;
import com.cloty.repo.TarjetaRepository;
import com.cloty.repo.SuperUsuarioRepository;
import com.cloty.repo.UsuarioRepository;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CascadeEliminacionService {

	private final ColegioRepository colegioRepository;
	private final CursoRepository cursoRepository;
	private final AlumnoRepository alumnoRepository;
	private final ApoderadoRepository apoderadoRepository;
	private final ColegioApoderadoRepository colegioApoderadoRepository;
	private final TarjetaRepository tarjetaRepository;
	private final EventoRepository eventoRepository;
	private final NotificacionRepository notificacionRepository;
	private final CodigoActivacionRepository codigoActivacionRepository;
	private final SuperUsuarioRepository superUsuarioRepository;
	private final UsuarioRepository usuarioRepository;

	@Transactional
	public void eliminarColegioCompleto(Integer idColegio) {
		Colegio colegio = colegioRepository.findById(idColegio)
				.orElseThrow(() -> new ResourceNotFoundException("Colegio no encontrado: " + idColegio));
		Integer idUsuarioColegio = colegio.getIdUsuario();

		List<Integer> apoderadosVinculados = colegioApoderadoRepository.findByIdColegio(idColegio).stream()
				.map(ca -> ca.getIdApoderado())
				.distinct()
				.toList();

		Set<Integer> apoderadosARevisar = new HashSet<>(apoderadosVinculados);
		alumnoRepository.findByIdColegioOrderByApellidosAscNombresAsc(idColegio)
				.forEach(a -> apoderadosARevisar.add(a.getIdApoderado()));

		alumnoRepository.findByIdColegioOrderByApellidosAscNombresAsc(idColegio)
				.forEach(a -> eliminarAlumnoCompleto(a.getIdAlumno()));

		cursoRepository.findByIdColegioOrderByNombreAsc(idColegio)
				.forEach(c -> cursoRepository.deleteById(c.getIdCurso()));

		colegioApoderadoRepository.findByIdColegio(idColegio)
				.forEach(ca -> colegioApoderadoRepository.deleteById(ca.getIdColegioApoderado()));

		eliminarCodigosActivacion(TipoEntidadActivacion.COLEGIO, idColegio);
		colegioRepository.deleteById(idColegio);

		if (idUsuarioColegio != null) {
			eliminarUsuarioSiExiste(idUsuarioColegio);
		}

		apoderadosARevisar.forEach(this::eliminarApoderadoSiHuerfano);
	}

	@Transactional
	public void eliminarApoderadoCompleto(Integer idApoderado) {
		if (!apoderadoRepository.existsById(idApoderado)) {
			throw new ResourceNotFoundException("Apoderado no encontrado: " + idApoderado);
		}
		Apoderado apoderado = apoderadoRepository.findById(idApoderado).orElseThrow();
		Integer idUsuario = apoderado.getIdUsuario();

		alumnoRepository.findByIdApoderadoOrderByApellidosAscNombresAsc(idApoderado)
				.forEach(a -> eliminarAlumnoCompleto(a.getIdAlumno()));

		notificacionRepository.findByIdApoderadoOrderByFechaEnvioDesc(idApoderado)
				.forEach(n -> notificacionRepository.deleteById(n.getIdNotificacion()));

		colegioApoderadoRepository.findByIdApoderado(idApoderado)
				.forEach(ca -> colegioApoderadoRepository.deleteById(ca.getIdColegioApoderado()));

		eliminarCodigosActivacion(TipoEntidadActivacion.APODERADO, idApoderado);
		apoderadoRepository.deleteById(idApoderado);

		if (idUsuario != null) {
			eliminarUsuarioSiExiste(idUsuario);
		}
	}

	@Transactional
	public void eliminarCursoCompleto(Integer idCurso) {
		if (!cursoRepository.existsById(idCurso)) {
			throw new ResourceNotFoundException("Curso no encontrado: " + idCurso);
		}
		alumnoRepository.findByIdCursoOrderByApellidosAscNombresAsc(idCurso)
				.forEach(a -> eliminarAlumnoCompleto(a.getIdAlumno()));
		cursoRepository.deleteById(idCurso);
	}

	@Transactional
	public void eliminarAlumnoCompleto(Integer idAlumno) {
		if (!alumnoRepository.existsById(idAlumno)) {
			throw new ResourceNotFoundException("Alumno no encontrado: " + idAlumno);
		}
		Alumno alumno = alumnoRepository.findById(idAlumno).orElseThrow();
		Integer idApoderado = alumno.getIdApoderado();
		tarjetaRepository.findByIdAlumnoOrderByFechaAsignacionDesc(idAlumno)
				.forEach(t -> eliminarTarjetaCompleta(t.getIdTarjeta()));
		alumnoRepository.deleteById(idAlumno);
		eliminarApoderadoSiHuerfano(idApoderado);
	}

	@Transactional
	public void eliminarTarjetaCompleta(Integer idTarjeta) {
		if (!tarjetaRepository.existsById(idTarjeta)) {
			throw new ResourceNotFoundException("Tarjeta no encontrada: " + idTarjeta);
		}
		eventoRepository.findByIdTarjetaOrderByIdEventoDesc(idTarjeta)
				.forEach(e -> eliminarEventoCompleto(e.getIdEvento()));
		tarjetaRepository.deleteById(idTarjeta);
	}

	@Transactional
	public void eliminarEventoCompleto(Integer idEvento) {
		if (!eventoRepository.existsById(idEvento)) {
			throw new ResourceNotFoundException("Evento no encontrado: " + idEvento);
		}
		notificacionRepository.findByIdEventoOrderByFechaEnvioDesc(idEvento)
				.forEach(n -> notificacionRepository.deleteById(n.getIdNotificacion()));
		eventoRepository.deleteById(idEvento);
	}

	@Transactional
	public void eliminarAdministradorCompleto(Integer idAdministrador, Integer idUsuario) {
		eliminarCodigosRecuperacionUsuario(idUsuario);
		if (idUsuario != null) {
			eliminarUsuarioSiExiste(idUsuario);
		}
	}

	@Transactional
	public void eliminarSuperUsuarioCompleto(Integer idSuperUsuario, Integer idUsuario) {
		eliminarCodigosRecuperacionUsuario(idUsuario);
		if (idUsuario != null) {
			eliminarUsuarioSiExiste(idUsuario);
		}
	}

	@Transactional
	public void eliminarUsuarioCompleto(Integer idUsuario) {
		if (!usuarioRepository.existsById(idUsuario)) {
			throw new ResourceNotFoundException("Usuario no encontrado: " + idUsuario);
		}
		superUsuarioRepository.findByIdUsuario(idUsuario).ifPresent(s -> superUsuarioRepository.delete(s));
		eliminarCodigosRecuperacionUsuario(idUsuario);
		usuarioRepository.deleteById(idUsuario);
	}

	@Transactional
	public void revisarApoderadoHuerfano(Integer idApoderado) {
		eliminarApoderadoSiHuerfano(idApoderado);
	}

	private void eliminarApoderadoSiHuerfano(Integer idApoderado) {
		if (!apoderadoRepository.existsById(idApoderado)) {
			return;
		}
		boolean tieneVinculos = !colegioApoderadoRepository.findByIdApoderado(idApoderado).isEmpty();
		boolean tieneAlumnos = !alumnoRepository.findByIdApoderadoOrderByApellidosAscNombresAsc(idApoderado).isEmpty();
		if (!tieneVinculos && !tieneAlumnos) {
			eliminarApoderadoCompleto(idApoderado);
		}
	}

	private void eliminarCodigosActivacion(TipoEntidadActivacion tipo, Integer idEntidad) {
		codigoActivacionRepository.deleteByTipoAndIdEntidad(tipo, idEntidad);
	}

	private void eliminarCodigosRecuperacionUsuario(Integer idUsuario) {
		if (idUsuario == null) {
			return;
		}
		eliminarCodigosActivacion(TipoEntidadActivacion.RECUPERACION_CONTRASENA, idUsuario);
	}

	private void eliminarUsuarioSiExiste(Integer idUsuario) {
		if (idUsuario != null && usuarioRepository.existsById(idUsuario)) {
			superUsuarioRepository.findByIdUsuario(idUsuario).ifPresent(superUsuarioRepository::delete);
			eliminarCodigosRecuperacionUsuario(idUsuario);
			usuarioRepository.deleteById(idUsuario);
		}
	}
}
