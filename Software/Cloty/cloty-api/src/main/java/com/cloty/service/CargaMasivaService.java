package com.cloty.service;

import com.cloty.domain.Apoderado;
import com.cloty.domain.Curso;
import com.cloty.dto.ApoderadoRequest;
import com.cloty.dto.CargaMasivaResult;
import com.cloty.dto.ColegioApoderadoRequest;
import com.cloty.dto.CursoRequest;
import com.cloty.repo.AlumnoRepository;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.CursoRepository;
import com.cloty.util.CsvParser;
import com.cloty.validation.ChileValidacion;
import com.cloty.util.NivelesCurso;
import com.cloty.web.error.BadRequestException;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class CargaMasivaService {

	private final ColegioRepository colegioRepository;
	private final ApoderadoService apoderadoService;
	private final ColegioApoderadoService colegioApoderadoService;
	private final CursoService cursoService;
	private final AlumnoService alumnoService;
	private final ApoderadoRepository apoderadoRepository;
	private final CursoRepository cursoRepository;
	private final AlumnoRepository alumnoRepository;

	@Transactional
	public CargaMasivaResult importarApoderados(Integer idColegio, MultipartFile archivo) throws IOException {
		asegurarColegio(idColegio);
		List<Map<String, String>> filas = CsvParser.parse(archivo.getInputStream());
		int creados = 0;
		int omitidos = 0;
		int errores = 0;
		List<String> mensajes = new ArrayList<>();

		for (Map<String, String> fila : filas) {
			String linea = fila.get("_linea");
			try {
				String rut = ChileValidacion.formatearRutConGuion(valor(fila, "rut"));
				if (rut.isBlank()) {
					throw new BadRequestException("RUT vacío");
				}
				String nombres = valor(fila, "nombres");
				String apellidos = valor(fila, "apellidos");
				if (nombres.isBlank() || apellidos.isBlank()) {
					throw new BadRequestException("Nombres y apellidos son obligatorios");
				}

				boolean apoderadoNuevo = apoderadoRepository.findByRut(rut).isEmpty();
				Apoderado apoderado = apoderadoRepository.findByRut(rut).orElseGet(() ->
						apoderadoService.crear(new ApoderadoRequest(
								null,
								rut,
								nombres,
								apellidos,
								valorOpcional(fila, "email"),
								valorOpcional(fila, "telefono"),
								valorOpcional(fila, "codigo_comuna"),
								valorOpcional(fila, "calle_numero", "direccion")
						)));

				try {
					colegioApoderadoService.crear(new ColegioApoderadoRequest(idColegio, apoderado.getIdApoderado()));
					creados++;
					String detalle = apoderadoNuevo ? "creado y asociado" : "asociado al colegio";
					mensajes.add("Línea " + linea + ": apoderado " + rut + " " + detalle);
				} catch (com.cloty.web.error.ConflictException e) {
					omitidos++;
					mensajes.add("Línea " + linea + ": apoderado " + rut + " ya asociado al colegio");
				}
			} catch (Exception e) {
				errores++;
				mensajes.add("Línea " + linea + ": " + e.getMessage());
			}
		}

		return new CargaMasivaResult(filas.size(), creados, omitidos, errores, mensajes);
	}

	@Transactional
	public CargaMasivaResult importarAlumnos(Integer idColegio, MultipartFile archivo) throws IOException {
		asegurarColegio(idColegio);
		List<Map<String, String>> filas = CsvParser.parse(archivo.getInputStream());
		int creados = 0;
		int omitidos = 0;
		int errores = 0;
		List<String> mensajes = new ArrayList<>();

		for (Map<String, String> fila : filas) {
			String linea = fila.get("_linea");
			try {
				String rutAlumno = ChileValidacion.formatearRutConGuion(valor(fila, "rut_alumno", "rut"));
				String nombres = valor(fila, "nombres");
				String apellidos = valor(fila, "apellidos");
				String nombreCurso = valor(fila, "nombre_curso", "curso");
				String nivelCurso = valor(fila, "nivel");
				String rutApoderado = ChileValidacion.formatearRutConGuion(valor(fila, "rut_apoderado"));

				if (rutAlumno.isBlank() || nombres.isBlank() || apellidos.isBlank()) {
					throw new BadRequestException("RUT, nombres y apellidos del alumno son obligatorios");
				}
				if (nombreCurso.isBlank() && nivelCurso.isBlank()) {
					throw new BadRequestException("Debe indicar el nivel del curso");
				}
				if (rutApoderado.isBlank()) {
					throw new BadRequestException("rut_apoderado es obligatorio");
				}

				if (alumnoRepository.findByRut(rutAlumno).isPresent()) {
					omitidos++;
					mensajes.add("Línea " + linea + ": alumno " + rutAlumno + " ya existe");
					continue;
				}

				Apoderado apoderado = apoderadoRepository.findByRut(rutApoderado)
						.orElseThrow(() -> new BadRequestException(
								"Apoderado no encontrado: " + rutApoderado + ". Cargue apoderados primero."));

				Curso curso = resolverCurso(idColegio, nivelCurso, nombreCurso);

				boolean estado = parseEstado(valorOpcional(fila, "estado"));

				alumnoService.crear(new com.cloty.dto.AlumnoRequest(
						idColegio,
						apoderado.getIdApoderado(),
						curso.getIdCurso(),
						rutAlumno,
						nombres,
						apellidos,
						estado
				));
				creados++;
				mensajes.add("Línea " + linea + ": alumno " + rutAlumno + " → " + curso.getNombre());
			} catch (Exception e) {
				errores++;
				mensajes.add("Línea " + linea + ": " + e.getMessage());
			}
		}

		return new CargaMasivaResult(filas.size(), creados, omitidos, errores, mensajes);
	}

	private Curso resolverCurso(Integer idColegio, String nivelRaw, String nombreRaw) {
		String nivel = NivelesCurso.normalizar(!nivelRaw.isBlank() ? nivelRaw : nombreRaw);
		if (nivel == null) {
			throw new BadRequestException(
					"Nivel inválido. Use 1° Básico a 8° Básico o 1° Medio a 4° Medio.");
		}
		return cursoRepository.findByIdColegioAndNivel(idColegio, nivel)
				.orElseGet(() -> {
					String nombre = !nombreRaw.isBlank() ? nombreRaw : nivel;
					return cursoRepository.findByIdColegioAndNombre(idColegio, nombre)
							.orElseGet(() -> cursoService.crear(new CursoRequest(idColegio, nombre, nivel, true)));
				});
	}

	private void asegurarColegio(Integer idColegio) {
		if (!colegioRepository.existsById(idColegio)) {
			throw new ResourceNotFoundException("Colegio no encontrado: " + idColegio);
		}
	}

	private static String valor(Map<String, String> fila, String... keys) {
		for (String key : keys) {
			String v = fila.get(key);
			if (v != null && !v.isBlank()) {
				return v.trim();
			}
		}
		return "";
	}

	private static String valorOpcional(Map<String, String> fila, String key) {
		String v = fila.get(key);
		return v == null || v.isBlank() ? null : v.trim();
	}

	private static String valorOpcional(Map<String, String> fila, String key, String fallbackKey) {
		String v = valor(fila, key, fallbackKey);
		return v.isBlank() ? null : v;
	}

	private static boolean parseEstado(String raw) {
		if (raw == null || raw.isBlank()) {
			return true;
		}
		return switch (raw.trim().toLowerCase(Locale.ROOT)) {
			case "false", "0", "no", "inactivo" -> false;
			default -> true;
		};
	}
}
