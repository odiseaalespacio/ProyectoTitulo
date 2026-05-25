package com.cloty.service;

import com.cloty.domain.Alumno;
import com.cloty.domain.Colegio;
import com.cloty.domain.Curso;
import com.cloty.dto.AlumnoRequest;
import com.cloty.dto.PupiloResumenResponse;
import com.cloty.repo.AlumnoRepository;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.CursoRepository;
import com.cloty.web.error.BadRequestException;
import com.cloty.web.error.ConflictException;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlumnoService {

	private final AlumnoRepository alumnoRepository;
	private final ColegioRepository colegioRepository;
	private final ApoderadoRepository apoderadoRepository;
	private final CursoRepository cursoRepository;

	@Transactional(readOnly = true)
	public List<Alumno> listarPorColegio(Integer idColegio) {
		if (!colegioRepository.existsById(idColegio)) {
			throw new ResourceNotFoundException("Colegio no encontrado: " + idColegio);
		}
		return alumnoRepository.findByIdColegioOrderByApellidosAscNombresAsc(idColegio);
	}

	@Transactional(readOnly = true)
	public List<Alumno> listarPorApoderado(Integer idApoderado) {
		if (!apoderadoRepository.existsById(idApoderado)) {
			throw new ResourceNotFoundException("Apoderado no encontrado: " + idApoderado);
		}
		return alumnoRepository.findByIdApoderadoOrderByApellidosAscNombresAsc(idApoderado);
	}

	@Transactional(readOnly = true)
	public List<Alumno> listarPorCurso(Integer idCurso) {
		if (!cursoRepository.existsById(idCurso)) {
			throw new ResourceNotFoundException("Curso no encontrado: " + idCurso);
		}
		return alumnoRepository.findByIdCursoOrderByApellidosAscNombresAsc(idCurso);
	}

	@Transactional(readOnly = true)
	public Alumno obtener(Integer id) {
		return alumnoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado: " + id));
	}

	@Transactional(readOnly = true)
	public List<PupiloResumenResponse> listarPupilosResumen(Integer idApoderado) {
		if (idApoderado == null) {
			throw new BadRequestException("La cuenta no tiene perfil de apoderado");
		}
		if (!apoderadoRepository.existsById(idApoderado)) {
			throw new ResourceNotFoundException("Apoderado no encontrado: " + idApoderado);
		}
		List<Alumno> alumnos = alumnoRepository.findByIdApoderadoOrderByApellidosAscNombresAsc(idApoderado);
		List<PupiloResumenResponse> resultado = new ArrayList<>();
		for (Alumno al : alumnos) {
			Curso curso = cursoRepository.findById(al.getIdCurso()).orElse(null);
			Colegio colegio = colegioRepository.findById(al.getIdColegio()).orElse(null);
			resultado.add(new PupiloResumenResponse(
					al.getIdAlumno(),
					al.getRut(),
					al.getNombres(),
					al.getApellidos(),
					al.getEstado(),
					al.getIdCurso(),
					curso != null ? curso.getNombre() : null,
					al.getIdColegio(),
					colegio != null ? colegio.getNombre() : null
			));
		}
		return resultado;
	}

	@Transactional
	public Alumno crear(AlumnoRequest req) {
		validarReferencias(req);
		alumnoRepository.findByRut(req.rut()).ifPresent(a -> {
			throw new ConflictException("El RUT del alumno ya está registrado");
		});
		Alumno a = Alumno.builder()
				.idColegio(req.idColegio())
				.idApoderado(req.idApoderado())
				.idCurso(req.idCurso())
				.rut(req.rut())
				.nombres(req.nombres())
				.apellidos(req.apellidos())
				.estado(req.estado() != null ? req.estado() : Boolean.TRUE)
				.build();
		return alumnoRepository.save(a);
	}

	@Transactional
	public Alumno actualizar(Integer id, AlumnoRequest req) {
		Alumno a = obtener(id);
		validarReferencias(req);
		if (!req.rut().equals(a.getRut())) {
			alumnoRepository.findByRut(req.rut()).ifPresent(otro -> {
				if (!otro.getIdAlumno().equals(id)) {
					throw new ConflictException("El RUT del alumno ya está registrado");
				}
			});
		}
		a.setIdColegio(req.idColegio());
		a.setIdApoderado(req.idApoderado());
		a.setIdCurso(req.idCurso());
		a.setRut(req.rut());
		a.setNombres(req.nombres());
		a.setApellidos(req.apellidos());
		if (req.estado() != null) {
			a.setEstado(req.estado());
		}
		return alumnoRepository.save(a);
	}

	@Transactional
	public void eliminar(Integer id) {
		if (!alumnoRepository.existsById(id)) {
			throw new ResourceNotFoundException("Alumno no encontrado: " + id);
		}
		alumnoRepository.deleteById(id);
	}

	private void validarReferencias(AlumnoRequest req) {
		if (!colegioRepository.existsById(req.idColegio())) {
			throw new ResourceNotFoundException("Colegio no encontrado: " + req.idColegio());
		}
		if (!apoderadoRepository.existsById(req.idApoderado())) {
			throw new ResourceNotFoundException("Apoderado no encontrado: " + req.idApoderado());
		}
		Curso curso = cursoRepository.findById(req.idCurso())
				.orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + req.idCurso()));
		if (!curso.getIdColegio().equals(req.idColegio())) {
			throw new ConflictException("El curso no pertenece al colegio indicado");
		}
	}
}
