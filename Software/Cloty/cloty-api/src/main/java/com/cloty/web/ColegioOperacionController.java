package com.cloty.web;

import com.cloty.dto.ColegioDashboardResponse;
import com.cloty.dto.OperacionPrendaResponse;
import com.cloty.dto.ScanPrendaRequest;
import com.cloty.security.ClotyUserDetails;
import com.cloty.service.ColegioOperacionService;
import com.cloty.web.error.BadRequestException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/colegio/operaciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COLEGIO')")
public class ColegioOperacionController {

	private final ColegioOperacionService colegioOperacionService;

	@PostMapping("/escanear")
	public OperacionPrendaResponse escanear(
			Authentication authentication,
			@Valid @RequestBody ScanPrendaRequest body) {
		ClotyUserDetails user = requireColegio(authentication);
		return colegioOperacionService.procesarEscaneo(user.getIdColegio(), user.getIdUsuario(), body);
	}

	@GetMapping("/dashboard")
	public ColegioDashboardResponse dashboard(Authentication authentication) {
		ClotyUserDetails user = requireColegio(authentication);
		return colegioOperacionService.dashboard(user.getIdColegio());
	}

	private static ClotyUserDetails requireColegio(Authentication authentication) {
		if (!(authentication.getPrincipal() instanceof ClotyUserDetails user)) {
			throw new BadRequestException("Sesión inválida");
		}
		if (user.getIdColegio() == null) {
			throw new BadRequestException("La cuenta no tiene perfil de colegio");
		}
		return user;
	}
}
