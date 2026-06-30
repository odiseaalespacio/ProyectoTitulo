package com.cloty.util;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class NivelesCurso {

	private static final Pattern DIGITOS = Pattern.compile("(\\d+)");

	public static final List<String> TODOS = List.of(
			"1° Básico", "2° Básico", "3° Básico", "4° Básico",
			"5° Básico", "6° Básico", "7° Básico", "8° Básico",
			"1° Medio", "2° Medio", "3° Medio", "4° Medio"
	);

	private static final Map<String, Integer> ORDINALES = Map.ofEntries(
			Map.entry("primero", 1),
			Map.entry("primer", 1),
			Map.entry("segundo", 2),
			Map.entry("tercero", 3),
			Map.entry("cuarto", 4),
			Map.entry("quinto", 5),
			Map.entry("sexto", 6),
			Map.entry("septimo", 7),
			Map.entry("octavo", 8)
	);

	private NivelesCurso() {
	}

	public static String normalizar(String input) {
		if (input == null || input.isBlank()) {
			return null;
		}
		String t = sinAcentos(input.trim().toLowerCase(Locale.ROOT));
		for (String nivel : TODOS) {
			String canon = sinAcentos(nivel.toLowerCase(Locale.ROOT));
			if (t.equals(canon) || t.startsWith(canon + " ")) {
				return nivel;
			}
		}
		Integer numero = extraerNumero(t);
		String tipo = t.contains("medio") ? "Medio" : t.contains("basico") ? "Básico" : null;
		if (numero != null && tipo != null) {
			String candidato = numero + "° " + tipo;
			if (TODOS.contains(candidato)) {
				return candidato;
			}
		}
		for (var entry : ORDINALES.entrySet()) {
			if (!t.contains(entry.getKey())) {
				continue;
			}
			if (t.contains("medio")) {
				String candidato = entry.getValue() + "° Medio";
				if (TODOS.contains(candidato)) {
					return candidato;
				}
			}
			if (t.contains("basico")) {
				String candidato = entry.getValue() + "° Básico";
				if (TODOS.contains(candidato)) {
					return candidato;
				}
			}
		}
		return null;
	}

	private static Integer extraerNumero(String t) {
		for (var entry : ORDINALES.entrySet()) {
			if (t.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		var m = DIGITOS.matcher(t);
		if (m.find()) {
			try {
				return Integer.parseInt(m.group(1));
			} catch (NumberFormatException ignored) {
				return null;
			}
		}
		return null;
	}

	private static String sinAcentos(String s) {
		return Normalizer.normalize(s, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
	}
}
