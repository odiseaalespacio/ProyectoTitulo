package com.cloty.dto;

public record ResumenCursoDashboard(
		Integer idCurso,
		String nombre,
		String nivel,
		long totalAlumnos,
		long alumnosConTarjeta,
		long alumnosSinTarjeta
) {
}
