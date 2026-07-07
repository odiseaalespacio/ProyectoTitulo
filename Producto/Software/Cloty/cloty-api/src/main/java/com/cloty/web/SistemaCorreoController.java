package com.cloty.web;

import com.cloty.dto.EstadoCorreoResponse;
import com.cloty.dto.ProbarCorreoRequest;
import com.cloty.dto.ProbarCorreoResponse;
import com.cloty.security.ClotyRoles;
import com.cloty.service.MailDiagnosticService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sistema/correo")
@RequiredArgsConstructor
@PreAuthorize(ClotyRoles.SUPER_USUARIO)
public class SistemaCorreoController {

	private final MailDiagnosticService mailDiagnosticService;

	@GetMapping("/estado")
	public EstadoCorreoResponse estado() {
		return mailDiagnosticService.estado();
	}

	@PostMapping("/probar")
	public ProbarCorreoResponse probar(@Valid @RequestBody ProbarCorreoRequest body) {
		return mailDiagnosticService.probarEnvio(body.email());
	}
}
