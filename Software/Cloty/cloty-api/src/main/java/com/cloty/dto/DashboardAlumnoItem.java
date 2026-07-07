package com.cloty.dto;

public record DashboardAlumnoItem(
		Integer idAlumno,
		String rut,
		String nombres,
		String apellidos,
		String nombreCurso,
		boolean tieneTarjeta,
		String nombreApoderado
) {
}
