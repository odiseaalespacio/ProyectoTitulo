package com.cloty.dto;

public record PupiloResumenResponse(
		Integer idAlumno,
		String rut,
		String nombres,
		String apellidos,
		Boolean estado,
		Integer idCurso,
		String nombreCurso,
		Integer idColegio,
		String nombreColegio
) {
}
