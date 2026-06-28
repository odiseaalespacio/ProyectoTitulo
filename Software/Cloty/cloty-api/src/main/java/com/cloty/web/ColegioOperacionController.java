package com.cloty.web;

import com.cloty.dto.ColegioDashboardResponse;
import com.cloty.dto.DashboardComunidadDetalleResponse;
import com.cloty.dto.DashboardCursoDetalleResponse;
import com.cloty.dto.DashboardNotificacionesDetalleResponse;
import com.cloty.dto.DashboardPrendasDetalleResponse;
import com.cloty.dto.DashboardTarjetasDetalleResponse;
import com.cloty.dto.ActividadRecienteResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

	@GetMapping("/dashboard/comunidad")
	public DashboardComunidadDetalleResponse dashboardComunidad(Authentication authentication) {
		ClotyUserDetails user = requireColegio(authentication);
		return colegioOperacionService.dashboardComunidad(user.getIdColegio());
	}

	@GetMapping("/dashboard/tarjetas")
	public DashboardTarjetasDetalleResponse dashboardTarjetas(Authentication authentication) {
		ClotyUserDetails user = requireColegio(authentication);
		return colegioOperacionService.dashboardTarjetas(user.getIdColegio());
	}

	@GetMapping("/dashboard/prendas")
	public DashboardPrendasDetalleResponse dashboardPrendas(Authentication authentication) {
		ClotyUserDetails user = requireColegio(authentication);
		return colegioOperacionService.dashboardPrendas(user.getIdColegio());
	}

	@GetMapping("/dashboard/notificaciones")
	public DashboardNotificacionesDetalleResponse dashboardNotificaciones(Authentication authentication) {
		ClotyUserDetails user = requireColegio(authentication);
		return colegioOperacionService.dashboardNotificaciones(user.getIdColegio());
	}

	@GetMapping("/dashboard/cursos")
	public List<DashboardCursoDetalleResponse> dashboardCursos(Authentication authentication) {
		ClotyUserDetails user = requireColegio(authentication);
		return colegioOperacionService.dashboardCursos(user.getIdColegio());
	}

	@GetMapping("/dashboard/cursos/{idCurso}")
	public DashboardCursoDetalleResponse dashboardCurso(
			Authentication authentication,
			@PathVariable Integer idCurso) {
		ClotyUserDetails user = requireColegio(authentication);
		return colegioOperacionService.dashboardCurso(user.getIdColegio(), idCurso);
	}

	@GetMapping("/dashboard/actividad")
	public List<ActividadRecienteResponse> dashboardActividad(Authentication authentication) {
		ClotyUserDetails user = requireColegio(authentication);
		return colegioOperacionService.dashboardActividad(user.getIdColegio());
	}

	private static ClotyUserDetails requireColegio(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (!(principal instanceof ClotyUserDetails)) {
			throw new BadRequestException("Sesión inválida");
		}
		ClotyUserDetails user = (ClotyUserDetails) principal;
		if (user.getIdColegio() == null) {
			throw new BadRequestException("La cuenta no tiene perfil de colegio");
		}
		return user;
	}
}
