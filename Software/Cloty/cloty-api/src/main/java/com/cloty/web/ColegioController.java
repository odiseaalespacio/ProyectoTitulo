package com.cloty.web;

import com.cloty.domain.Colegio;
import com.cloty.dto.ColegioRequest;
import com.cloty.service.ColegioService;
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
@RequestMapping("/api/colegios")
@RequiredArgsConstructor
public class ColegioController {

	private final ColegioService colegioService;

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO')")
	public List<Colegio> listar() {
		return colegioService.listar();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or (hasRole('COLEGIO') and @authz.ownsColegio(#id))")
	public Colegio obtener(@PathVariable Integer id) {
		return colegioService.obtener(id);
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO')")
	@ResponseStatus(HttpStatus.CREATED)
	public Colegio crear(@Valid @RequestBody ColegioRequest body) {
		return colegioService.crear(body);
	}

	// esta parte es nueva
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or (hasRole('COLEGIO') and @authz.ownsColegio(#id))")
	public Colegio actualizar(@PathVariable Integer id, @Valid @RequestBody ColegioRequest body) {
		return colegioService.actualizar(id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		colegioService.eliminar(id);
	}
}
