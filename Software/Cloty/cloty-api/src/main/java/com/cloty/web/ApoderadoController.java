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
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public List<Apoderado> listar() {
		return apoderadoService.listar();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or @authz.ownsApoderado(#id)")
	public Apoderado obtener(@PathVariable Integer id) {
		return apoderadoService.obtener(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@ResponseStatus(HttpStatus.CREATED)
	public Apoderado crear(@Valid @RequestBody ApoderadoRequest body) {
		return apoderadoService.crear(body);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public Apoderado actualizar(@PathVariable Integer id, @Valid @RequestBody ApoderadoRequest body) {
		return apoderadoService.actualizar(id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		apoderadoService.eliminar(id);
	}
}
