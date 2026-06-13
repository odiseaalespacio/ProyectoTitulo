package com.cloty.web;

import com.cloty.dto.CargaMasivaResult;
import com.cloty.service.CargaMasivaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/carga-masiva")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPER_USUARIO')")
public class CargaMasivaController {

	private final CargaMasivaService cargaMasivaService;

	@PostMapping(value = "/colegio/{idColegio}/apoderados", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public CargaMasivaResult importarApoderados(
			@PathVariable Integer idColegio,
			@RequestParam("archivo") MultipartFile archivo) throws IOException {
		return cargaMasivaService.importarApoderados(idColegio, archivo);
	}

	@PostMapping(value = "/colegio/{idColegio}/alumnos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public CargaMasivaResult importarAlumnos(
			@PathVariable Integer idColegio,
			@RequestParam("archivo") MultipartFile archivo) throws IOException {
		return cargaMasivaService.importarAlumnos(idColegio, archivo);
	}
}
