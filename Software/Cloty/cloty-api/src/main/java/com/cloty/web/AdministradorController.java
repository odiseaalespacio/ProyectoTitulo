package com.cloty.web;

import com.cloty.domain.Administrador;
import com.cloty.dto.AdministradorCompletoRequest;
import com.cloty.dto.AdministradorRequest;
import com.cloty.security.ClotyRoles;
import com.cloty.service.AdministradorService;
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
@RequestMapping("/api/administradores")
@RequiredArgsConstructor
@PreAuthorize(ClotyRoles.SUPER_USUARIO)
public class AdministradorController {

	private final AdministradorService administradorService;

	@GetMapping
	public List<Administrador> listar() {
		return administradorService.listar();
	}

	@GetMapping("/{id}")
	public Administrador obtener(@PathVariable Integer id) {
		return administradorService.obtener(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Administrador crear(@Valid @RequestBody AdministradorRequest body) {
		return administradorService.crear(body);
	}

	@PostMapping("/completo")
	@ResponseStatus(HttpStatus.CREATED)
	public Administrador crearCompleto(@Valid @RequestBody AdministradorCompletoRequest body) {
		return administradorService.crearCompleto(body);
	}

	@PutMapping("/{id}")
	public Administrador actualizar(@PathVariable Integer id, @Valid @RequestBody AdministradorRequest body) {
		return administradorService.actualizar(id, body);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		administradorService.eliminar(id);
	}
}
