package com.cloty.web;

import com.cloty.domain.Apoderado;
import com.cloty.dto.ApoderadoRequest;
import com.cloty.service.ApoderadoService;
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
@RequestMapping("/api/apoderados")
@RequiredArgsConstructor
public class ApoderadoController {

	private final ApoderadoService apoderadoService;

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO')")
	public List<Apoderado> listar() {
		return apoderadoService.listar();
	}

	// esta parte es nueva
	@GetMapping("/colegio/{idColegio}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or (hasRole('COLEGIO') and @authz.ownsColegio(#idColegio))")
	public List<Apoderado> listarPorColegio(@PathVariable Integer idColegio) {
		return apoderadoService.listarPorColegio(idColegio);
	}

	// esta parte es nueva
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or @authz.ownsApoderado(#id) or (hasRole('COLEGIO') and @authz.apoderadoAsociadoAMiColegio(#id))")
	public Apoderado obtener(@PathVariable Integer id) {
		return apoderadoService.obtener(id);
	}

	// esta parte es nueva
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or hasRole('COLEGIO')")
	@ResponseStatus(HttpStatus.CREATED)
	public Apoderado crear(@Valid @RequestBody ApoderadoRequest body) {
		return apoderadoService.crear(body);
	}

	// esta parte es nueva
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or @authz.ownsApoderado(#id) or (hasRole('COLEGIO') and @authz.apoderadoAsociadoAMiColegio(#id))")
	public Apoderado actualizar(@PathVariable Integer id, @Valid @RequestBody ApoderadoRequest body) {
		return apoderadoService.actualizar(id, body);
	}

	// esta parte es nueva
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or (hasRole('COLEGIO') and @authz.apoderadoAsociadoAMiColegio(#id))")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		apoderadoService.eliminar(id);
	}
}
