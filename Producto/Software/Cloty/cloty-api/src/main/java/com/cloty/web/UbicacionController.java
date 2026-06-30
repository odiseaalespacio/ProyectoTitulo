package com.cloty.web;

import com.cloty.dto.ComunaResponse;
import com.cloty.dto.RegionResponse;
import com.cloty.service.UbicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ubicacion")
@RequiredArgsConstructor
public class UbicacionController {

	private final UbicacionService ubicacionService;

	@GetMapping("/regiones")
	public List<RegionResponse> listarRegiones() {
		return ubicacionService.listarRegiones();
	}

	@GetMapping("/regiones/{codigoRegion}/comunas")
	public List<ComunaResponse> listarComunas(@PathVariable String codigoRegion) {
		return ubicacionService.listarComunasPorRegion(codigoRegion);
	}

	@GetMapping("/comunas/{codigoComuna}")
	public ComunaResponse obtenerComuna(@PathVariable String codigoComuna) {
		return ubicacionService.obtenerComuna(codigoComuna);
	}
}
