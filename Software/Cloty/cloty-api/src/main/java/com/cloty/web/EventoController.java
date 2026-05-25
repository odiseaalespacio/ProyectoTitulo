package com.cloty.web;

import com.cloty.domain.Evento;
import com.cloty.dto.EventoRequest;
import com.cloty.service.EventoService;
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
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

	private final EventoService eventoService;

	@GetMapping("/tarjeta/{idTarjeta}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or @authz.ownsTarjeta(#idTarjeta)")
	public List<Evento> listarPorTarjeta(@PathVariable Integer idTarjeta) {
		return eventoService.listarPorTarjeta(idTarjeta);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or @authz.ownsEvento(#id)")
	public Evento obtener(@PathVariable Integer id) {
		return eventoService.obtener(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarEventoRequest(#body))")
	@ResponseStatus(HttpStatus.CREATED)
	public Evento crear(@Valid @RequestBody EventoRequest body) {
		return eventoService.crear(body);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarEventoId(#id) and @authz.colegioPuedeGestionarEventoRequest(#body))")
	public Evento actualizar(@PathVariable Integer id, @Valid @RequestBody EventoRequest body) {
		return eventoService.actualizar(id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR') or (hasRole('COLEGIO') and @authz.colegioPuedeGestionarEventoId(#id))")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		eventoService.eliminar(id);
	}
}
