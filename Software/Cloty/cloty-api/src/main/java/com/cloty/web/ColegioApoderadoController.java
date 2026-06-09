package com.cloty.web;

import com.cloty.domain.ColegioApoderado;
import com.cloty.dto.ColegioApoderadoRequest;
import com.cloty.service.ColegioApoderadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/colegio-apoderados")
@RequiredArgsConstructor
public class ColegioApoderadoController {

	private final ColegioApoderadoService colegioApoderadoService;

	@GetMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public List<ColegioApoderado> listarTodos() {
		return colegioApoderadoService.listarTodos();
	}

	@GetMapping("/colegio/{idColegio}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.ownsColegio(#idColegio))")
	public List<ColegioApoderado> listarPorColegio(@PathVariable Integer idColegio) {
		return colegioApoderadoService.listarPorColegio(idColegio);
	}

	@GetMapping("/apoderado/{idApoderado}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or @authz.ownsApoderado(#idApoderado) or (hasRole('COLEGIO') and @authz.apoderadoAsociadoAMiColegio(#idApoderado))")
	public List<ColegioApoderado> listarPorApoderado(@PathVariable Integer idApoderado) {
		return colegioApoderadoService.listarPorApoderado(idApoderado);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or @authz.puedeVerColegioApoderadoPorId(#id)")
	public ColegioApoderado obtener(@PathVariable Integer id) {
		return colegioApoderadoService.obtener(id);
	}

	// esta parte es nueva
	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarColegioApoderadoRequest(#body))")
	@ResponseStatus(HttpStatus.CREATED)
	public ColegioApoderado crear(@Valid @RequestBody ColegioApoderadoRequest body) {
		return colegioApoderadoService.crear(body);
	}

	// esta parte es nueva
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.colegioPuedeEliminarColegioApoderado(#id))")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		colegioApoderadoService.eliminar(id);
	}
}
