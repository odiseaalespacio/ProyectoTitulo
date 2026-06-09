package com.cloty.web;

import com.cloty.domain.Alumno;
import com.cloty.dto.AlumnoRequest;
import com.cloty.service.AlumnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
public class AlumnoController {

	private final AlumnoService alumnoService;

	@GetMapping("/colegio/{idColegio}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.ownsColegio(#idColegio))")
	public List<Alumno> listarPorColegio(@PathVariable Integer idColegio) {
		return alumnoService.listarPorColegio(idColegio);
	}

	@GetMapping("/apoderado/{idApoderado}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or @authz.ownsApoderado(#idApoderado)")
	public List<Alumno> listarPorApoderado(@PathVariable Integer idApoderado) {
		return alumnoService.listarPorApoderado(idApoderado);
	}

	@GetMapping("/curso/{idCurso}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.ownsCurso(#idCurso))")
	public List<Alumno> listarPorCurso(@PathVariable Integer idCurso) {
		return alumnoService.listarPorCurso(idCurso);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or @authz.ownsAlumno(#id)")
	public Alumno obtener(@PathVariable Integer id) {
		return alumnoService.obtener(id);
	}

	// esta parte es nueva
	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarAlumnoRequest(#body))")
	@ResponseStatus(HttpStatus.CREATED)
	public Alumno crear(@Valid @RequestBody AlumnoRequest body) {
		return alumnoService.crear(body);
	}

	// esta parte es nueva
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarAlumnoId(#id) and @authz.colegioPuedeGestionarAlumnoRequest(#body))")
	public Alumno actualizar(@PathVariable Integer id, @Valid @RequestBody AlumnoRequest body) {
		return alumnoService.actualizar(id, body);
	}

	// esta parte es nueva
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarAlumnoId(#id))")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		alumnoService.eliminar(id);
	}
}
