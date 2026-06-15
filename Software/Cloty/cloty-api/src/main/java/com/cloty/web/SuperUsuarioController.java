package com.cloty.web;

import com.cloty.domain.SuperUsuario;
import com.cloty.dto.SuperUsuarioCompletoRequest;
import com.cloty.dto.SuperUsuarioRequest;
import com.cloty.security.ClotyRoles;
import com.cloty.service.SuperUsuarioService;
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
@RequestMapping("/api/super-usuarios")
@RequiredArgsConstructor
@PreAuthorize(ClotyRoles.SUPER_USUARIO)
public class SuperUsuarioController {

	private final SuperUsuarioService superUsuarioService;

	@GetMapping
	public List<SuperUsuario> listar() {
		return superUsuarioService.listar();
	}

	@GetMapping("/{id}")
	public SuperUsuario obtener(@PathVariable Integer id) {
		return superUsuarioService.obtener(id);
	}

	@PostMapping("/completo")
	@ResponseStatus(HttpStatus.CREATED)
	public SuperUsuario crearCompleto(@Valid @RequestBody SuperUsuarioCompletoRequest body) {
		return superUsuarioService.crearCompleto(body);
	}

	@PutMapping("/{id}")
	public SuperUsuario actualizar(@PathVariable Integer id, @Valid @RequestBody SuperUsuarioRequest body) {
		return superUsuarioService.actualizar(id, body);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		superUsuarioService.eliminar(id);
	}
}
