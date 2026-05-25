package com.cloty.web;

import com.cloty.domain.Notificacion;
import com.cloty.dto.NotificacionRequest;
import com.cloty.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

	private final NotificacionService notificacionService;

	@GetMapping("/colegio/{idColegio}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.ownsColegio(#idColegio))")
	public List<Notificacion> listarPorColegio(@PathVariable Integer idColegio) {
		return notificacionService.listarPorColegio(idColegio);
	}

	@GetMapping("/apoderado/{idApoderado}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or @authz.ownsApoderado(#idApoderado)")
	public List<Notificacion> listarPorApoderado(@PathVariable Integer idApoderado) {
		return notificacionService.listarPorApoderado(idApoderado);
	}

	@GetMapping("/evento/{idEvento}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or @authz.ownsEvento(#idEvento)")
	public List<Notificacion> listarPorEvento(@PathVariable Integer idEvento) {
		return notificacionService.listarPorEvento(idEvento);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or @authz.ownsNotificacion(#id)")
	public Notificacion obtener(@PathVariable Integer id) {
		return notificacionService.obtener(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarNotificacionRequest(#body))")
	@ResponseStatus(HttpStatus.CREATED)
	public Notificacion crear(@Valid @RequestBody NotificacionRequest body) {
		return notificacionService.crear(body);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarNotificacionId(#id) and @authz.colegioPuedeGestionarNotificacionRequest(#body))")
	public Notificacion actualizar(@PathVariable Integer id, @Valid @RequestBody NotificacionRequest body) {
		return notificacionService.actualizar(id, body);
	}

	@PatchMapping("/{id}/leida")
	@PreAuthorize("hasRole('ADMINISTRADOR') or @authz.ownsNotificacion(#id)")
	public Notificacion marcarLeida(@PathVariable Integer id) {
		return notificacionService.marcarLeida(id);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarNotificacionId(#id))")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		notificacionService.eliminar(id);
	}
}
