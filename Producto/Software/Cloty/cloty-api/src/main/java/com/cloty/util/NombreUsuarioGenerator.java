package com.cloty.util;

import java.text.Normalizer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Genera {@code usuario.username} a partir del nombre visible (colegio o persona) + RUT, garantizando unicidad.
 */
public final class NombreUsuarioGenerator {

	private static final Pattern NO_ALFANUM = Pattern.compile("[^a-z0-9]+");
	private static final int MAX_TOTAL = 50;

	private NombreUsuarioGenerator() {
	}

	public static String generar(String nombreVisible, String rut, Predicate<String> existeUsername) {
		String slug = slugNombre(nombreVisible);
		String rutClave = limpiarRut(rut);
		if (slug.isEmpty()) {
			slug = "usuario";
		}
		String base = (slug + "_" + rutClave).toLowerCase();
		if (base.length() > MAX_TOTAL) {
			base = base.substring(0, MAX_TOTAL);
		}
		String candidato = base;
		int n = 0;
		while (existeUsername.test(candidato)) {
			n++;
			String sufijo = "_" + n;
			int maxBase = MAX_TOTAL - sufijo.length();
			String trunc = base.length() > maxBase ? base.substring(0, Math.max(1, maxBase)) : base;
			candidato = (trunc + sufijo).toLowerCase();
			if (candidato.length() > MAX_TOTAL) {
				candidato = candidato.substring(0, MAX_TOTAL);
			}
		}
		return candidato;
	}

	public static String paraPersona(String nombres, String apellidos, String rut, Predicate<String> existeUsername) {
		String nombre = ((nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "")).trim();
		return generar(nombre, rut, existeUsername);
	}

	public static String paraColegio(String nombreColegio, String rut, Predicate<String> existeUsername) {
		return generar(nombreColegio != null ? nombreColegio : "colegio", rut, existeUsername);
	}

	private static String slugNombre(String raw) {
		if (raw == null || raw.isBlank()) {
			return "";
		}
		String n = Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
				.replaceAll("\\p{M}+", "");
		n = n.toLowerCase();
		n = NO_ALFANUM.matcher(n).replaceAll("_");
		n = n.replaceAll("_+", "_").replaceAll("^_|_$", "");
		if (n.length() > 36) {
			n = n.substring(0, 36).replaceAll("_+$", "");
		}
		return n;
	}

	private static String limpiarRut(String rut) {
		if (rut == null) {
			return "sinrut";
		}
		String s = rut.replace(".", "").replace("-", "").toLowerCase();
		if (s.length() > 10) {
			s = s.substring(s.length() - 10);
		}
		return s.isEmpty() ? "sinrut" : s;
	}
}
