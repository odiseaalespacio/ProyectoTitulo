package com.cloty.validation;

import java.util.regex.Pattern;

/**
 * Validaciones de formato chileno (RUT, teléfono, correo).
 */
// esto es nuevo
public final class ChileValidacion {

	private static final Pattern EMAIL = Pattern.compile(
			"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private ChileValidacion() {
	}

	public static String normalizarRut(String rut) {
		if (rut == null) {
			return "";
		}
		return rut.replace(".", "").replace(" ", "").trim().toUpperCase();
	}

	public static boolean esRutValido(String rut) {
		String clean = normalizarRut(rut);
		if (clean.length() < 8 || clean.length() > 9) {
			return false;
		}
		String cuerpo = clean.substring(0, clean.length() - 1);
		char dv = clean.charAt(clean.length() - 1);
		if (!cuerpo.chars().allMatch(Character::isDigit)) {
			return false;
		}
		if (dv != '0' && dv != 'K' && !Character.isDigit(dv)) {
			return false;
		}
		int suma = 0;
		int factor = 2;
		for (int i = cuerpo.length() - 1; i >= 0; i--) {
			suma += Character.getNumericValue(cuerpo.charAt(i)) * factor;
			factor = factor == 7 ? 2 : factor + 1;
		}
		int resto = 11 - (suma % 11);
		char esperado = switch (resto) {
			case 11 -> '0';
			case 10 -> 'K';
			default -> Character.forDigit(resto, 10);
		};
		return dv == esperado;
	}

	public static String formatearRutConGuion(String rut) {
		String clean = normalizarRut(rut);
		if (clean.length() < 2) {
			return clean;
		}
		return clean.substring(0, clean.length() - 1) + "-" + clean.charAt(clean.length() - 1);
	}

	public static boolean esEmailValido(String email) {
		if (email == null || email.isBlank()) {
			return true;
		}
		return EMAIL.matcher(email.trim()).matches();
	}

	public static boolean esTelefonoChilenoValido(String telefono) {
		if (telefono == null || telefono.isBlank()) {
			return true;
		}
		String digits = telefono.replaceAll("\\D", "");
		if (digits.length() == 9 && digits.startsWith("9")) {
			return true;
		}
		if (digits.length() == 11 && digits.startsWith("569")) {
			return true;
		}
		if (digits.length() == 8 && digits.charAt(0) >= '2' && digits.charAt(0) <= '9') {
			return true;
		}
		return false;
	}
}
