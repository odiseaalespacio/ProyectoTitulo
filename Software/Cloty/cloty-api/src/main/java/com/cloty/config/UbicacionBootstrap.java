package com.cloty.config;

import com.cloty.domain.Comuna;
import com.cloty.domain.Region;
import com.cloty.repo.ComunaRepository;
import com.cloty.repo.RegionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
@Profile("!test")
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class UbicacionBootstrap implements ApplicationRunner {

	private final RegionRepository regionRepository;
	private final ComunaRepository comunaRepository;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	@Transactional
	public void run(ApplicationArguments args) throws Exception {
		if (regionRepository.count() > 0) {
			return;
		}
		cargarRegionesYComunas();
		log.info("Catálogo geográfico de Chile cargado (regiones y comunas)");
	}

	private void cargarRegionesYComunas() throws Exception {
		Map<String, Region> regiones = new HashMap<>();
		try (InputStream in = new ClassPathResource("data/chile-regiones.json").getInputStream()) {
			JsonNode array = OBJECT_MAPPER.readTree(in);
			for (JsonNode node : array) {
				String codigo = node.get("codigo_region").asText();
				String nombre = node.get("nombre_region").asText();
				Region region = regionRepository.save(Region.builder()
						.codigoRegion(codigo)
						.nombre(nombre)
						.build());
				regiones.put(codigo, region);
			}
		}

		try (InputStream in = new ClassPathResource("data/chile-comunas.json").getInputStream()) {
			JsonNode array = OBJECT_MAPPER.readTree(in);
			for (JsonNode node : array) {
				String codigoRegion = node.get("codigo_region").asText();
				Region region = regiones.get(codigoRegion);
				if (region == null) {
					continue;
				}
				comunaRepository.save(Comuna.builder()
						.codigoComuna(node.get("codigo_comuna").asText())
						.region(region)
						.nombre(node.get("nombre_comuna").asText())
						.build());
			}
		}
	}
}
