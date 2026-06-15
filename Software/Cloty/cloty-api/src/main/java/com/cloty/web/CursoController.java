package com.cloty.web;

import com.cloty.domain.Curso;
import com.cloty.dto.CursoRequest;
import com.cloty.service.CursoService;
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
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
public class CursoController {

	private final CursoService cursoService;

	@GetMapping("/colegio/{idColegio}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or (hasRole('COLEGIO') and @authz.ownsColegio(#idColegio))")
	public List<Curso> listarPorColegio(@PathVariable Integer idColegio) {
		return cursoService.listarPorColegio(idColegio);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or (hasRole('COLEGIO') and @authz.ownsCurso(#id))")
	public Curso obtener(@PathVariable Integer id) {
		return cursoService.obtener(id);
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarCursoRequest(#body))")
	@ResponseStatus(HttpStatus.CREATED)
	public Curso crear(@Valid @RequestBody CursoRequest body) {
		return cursoService.crear(body);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarCursoId(#id) and @authz.colegioPuedeGestionarCursoRequest(#body))")
	public Curso actualizar(@PathVariable Integer id, @Valid @RequestBody CursoRequest body) {
		return cursoService.actualizar(id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarCursoId(#id))")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		cursoService.eliminar(id);
	}
}
