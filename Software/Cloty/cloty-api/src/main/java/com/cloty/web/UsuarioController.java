package com.cloty.web;

import com.cloty.domain.Usuario;
import com.cloty.dto.UsuarioCreateRequest;
import com.cloty.dto.UsuarioUpdateRequest;
import com.cloty.security.ClotyRoles;
import com.cloty.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

	private final UsuarioService usuarioService;

	@GetMapping
	@PreAuthorize(ClotyRoles.SUPER_USUARIO)
	public List<Usuario> listar() {
		return usuarioService.listar();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or " + ClotyRoles.SUPER_USUARIO + " or @authz.isSelfUser(#id)")
	public Usuario obtener(@PathVariable Integer id) {
		return usuarioService.obtener(id);
	}

	@PostMapping
	@PreAuthorize(ClotyRoles.SUPER_USUARIO)
	@ResponseStatus(HttpStatus.CREATED)
	public Usuario crear(@Valid @RequestBody UsuarioCreateRequest body) {
		return usuarioService.crear(body);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or " + ClotyRoles.SUPER_USUARIO)
	public Usuario actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioUpdateRequest body) {
		return usuarioService.actualizar(id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize(ClotyRoles.SUPER_USUARIO)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		usuarioService.eliminar(id);
	}
}
