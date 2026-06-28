package com.cloty.service;

import com.cloty.domain.Comuna;
import com.cloty.domain.Region;
import com.cloty.dto.ComunaResponse;
import com.cloty.dto.RegionResponse;
import com.cloty.repo.ComunaRepository;
import com.cloty.repo.RegionRepository;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UbicacionService {

	private final RegionRepository regionRepository;
	private final ComunaRepository comunaRepository;

	@Transactional(readOnly = true)
	public List<RegionResponse> listarRegiones() {
		return regionRepository.findAllByOrderByNombreAsc().stream()
				.map(UbicacionService::toRegionResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ComunaResponse> listarComunasPorRegion(String codigoRegion) {
		if (!regionRepository.existsById(codigoRegion)) {
			throw new ResourceNotFoundException("Región no encontrada: " + codigoRegion);
		}
		return comunaRepository.findByRegionCodigoRegionOrderByNombreAsc(codigoRegion).stream()
				.map(UbicacionService::toComunaResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ComunaResponse obtenerComuna(String codigoComuna) {
		Comuna comuna = comunaRepository.findWithRegionByCodigoComuna(codigoComuna)
				.orElseThrow(() -> new ResourceNotFoundException("Comuna no encontrada: " + codigoComuna));
		return toComunaResponse(comuna);
	}

	@Transactional(readOnly = true)
	public void validarComuna(String codigoComuna) {
		if (codigoComuna == null || codigoComuna.isBlank()) {
			return;
		}
		if (!comunaRepository.existsById(codigoComuna.trim())) {
			throw new ResourceNotFoundException("Comuna no encontrada: " + codigoComuna);
		}
	}

	static RegionResponse toRegionResponse(Region r) {
		return new RegionResponse(r.getCodigoRegion(), r.getNombre());
	}

	static ComunaResponse toComunaResponse(Comuna c) {
		return new ComunaResponse(
				c.getCodigoComuna(),
				c.getRegion().getCodigoRegion(),
				c.getNombre()
		);
	}
}
