package com.cloty.service;

import com.cloty.domain.Comuna;
import com.cloty.domain.Region;
import com.cloty.repo.ComunaRepository;
import com.cloty.repo.RegionRepository;
import com.cloty.web.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UbicacionServiceTest {

	@Mock
	private RegionRepository regionRepository;

	@Mock
	private ComunaRepository comunaRepository;

	@InjectMocks
	private UbicacionService ubicacionService;

	@Test
	void listarRegionesOrdenadas() {
		Region rm = Region.builder().codigoRegion("13").nombre("Metropolitana").build();
		when(regionRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(rm));

		assertEquals(1, ubicacionService.listarRegiones().size());
		assertEquals("13", ubicacionService.listarRegiones().get(0).codigoRegion());
	}

	@Test
	void validarComunaInexistenteLanzaExcepcion() {
		when(comunaRepository.existsById("99999")).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> ubicacionService.validarComuna("99999"));
	}

	@Test
	void validarComunaNullNoHaceNada() {
		ubicacionService.validarComuna(null);
		ubicacionService.validarComuna("  ");
	}

	@Test
	void obtenerComunaDevuelveDatos() {
		Region region = Region.builder().codigoRegion("13").nombre("Metropolitana").build();
		Comuna comuna = Comuna.builder().codigoComuna("13101").region(region).nombre("Santiago").build();
		when(comunaRepository.findWithRegionByCodigoComuna("13101")).thenReturn(Optional.of(comuna));

		var resp = ubicacionService.obtenerComuna("13101");
		assertEquals("Santiago", resp.nombre());
		assertEquals("13", resp.codigoRegion());
	}

	@Test
	void listarComunasPorRegionValidaExistencia() {
		when(regionRepository.existsById("99")).thenReturn(false);
		assertThrows(ResourceNotFoundException.class, () -> ubicacionService.listarComunasPorRegion("99"));
	}

	@Test
	void validarComunaExistente() {
		when(comunaRepository.existsById("13101")).thenReturn(true);
		ubicacionService.validarComuna("13101");
		verify(comunaRepository).existsById("13101");
	}
}
