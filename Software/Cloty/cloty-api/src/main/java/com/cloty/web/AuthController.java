package com.cloty.web;

import com.cloty.dto.ActivarCuentaApoderadoRequest;
import com.cloty.dto.ActivarCuentaColegioRequest;
import com.cloty.dto.AuthMeResponse;
import com.cloty.dto.AuthTokenResponse;
import com.cloty.dto.CambiarContrasenaRequest;
import com.cloty.dto.LoginRequest;
import com.cloty.dto.PupiloResumenResponse;
import com.cloty.dto.RegistroApoderadoRequest;
import com.cloty.security.ClotyUserDetails;
import com.cloty.service.AlumnoService;
import com.cloty.service.AuthService;
import com.cloty.web.error.BadRequestException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final AlumnoService alumnoService;

	@PostMapping("/login")
	public AuthTokenResponse login(@Valid @RequestBody LoginRequest body) {
		return authService.login(body);
	}

	@PostMapping("/registro-apoderado")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthTokenResponse registroApoderado(@Valid @RequestBody RegistroApoderadoRequest body) {
		return authService.registroApoderado(body);
	}

	/**
	 * Apoderado pre-cargado por el admin (sin usuario). Solo valida el RUT; el nombre de usuario se arma con nombres + apellidos + RUT.
	 */
	@PostMapping("/activar-cuenta-apoderado")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthTokenResponse activarCuentaApoderado(@Valid @RequestBody ActivarCuentaApoderadoRequest body) {
		return authService.activarCuentaApoderado(body);
	}

	/**
	 * Colegio pre-cargado por el admin (sin usuario). RUT + contraseña + correo y teléfono del establecimiento; el nombre de usuario se arma con el nombre + RUT.
	 */
	@PostMapping("/activar-cuenta-colegio")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthTokenResponse activarCuentaColegio(@Valid @RequestBody ActivarCuentaColegioRequest body) {
		return authService.activarCuentaColegio(body);
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public AuthMeResponse me() {
		return authService.me();
	}

	@PostMapping("/cambiar-contrasena")
	@PreAuthorize("isAuthenticated()")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cambiarContrasena(@Valid @RequestBody CambiarContrasenaRequest body) {
		authService.cambiarContrasena(body);
	}

	@GetMapping("/mis-pupilos")
	@PreAuthorize("hasRole('APODERADO')")
	public List<PupiloResumenResponse> misPupilos(Authentication authentication) {
		if (!(authentication.getPrincipal() instanceof ClotyUserDetails p)) {
			throw new BadRequestException("Sesión inválida");
		}
		return alumnoService.listarPupilosResumen(p.getIdApoderado());
	}
}
