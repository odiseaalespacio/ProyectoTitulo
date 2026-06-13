package com.cloty.web;

import com.cloty.domain.Tarjeta;
import com.cloty.dto.TarjetaRequest;
import com.cloty.service.TarjetaService;
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
@RequestMapping("/api/tarjetas")
@RequiredArgsConstructor
public class TarjetaController {

	private final TarjetaService tarjetaService;

	@GetMapping("/alumno/{idAlumno}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or @authz.ownsAlumno(#idAlumno)")
	public List<Tarjeta> listarPorAlumno(@PathVariable Integer idAlumno) {
		return tarjetaService.listarPorAlumno(idAlumno);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or @authz.ownsTarjeta(#id)")
	public Tarjeta obtener(@PathVariable Integer id) {
		return tarjetaService.obtener(id);
	}

	@GetMapping("/uid/{uidNfc}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO') or @authz.ownsTarjetaUid(#uidNfc)")
	public Tarjeta obtenerPorUid(@PathVariable String uidNfc) {
		return tarjetaService.obtenerPorUid(uidNfc);
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO')")
	@ResponseStatus(HttpStatus.CREATED)
	public Tarjeta crear(@Valid @RequestBody TarjetaRequest body) {
		return tarjetaService.crear(body);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO')")
	public Tarjeta actualizar(@PathVariable Integer id, @Valid @RequestBody TarjetaRequest body) {
		return tarjetaService.actualizar(id, body);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void eliminar(@PathVariable Integer id) {
		tarjetaService.eliminar(id);
	}
}
