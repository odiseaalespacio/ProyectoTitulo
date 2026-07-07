package com.cloty.dto;

import java.util.List;

public record DashboardCursoDetalleResponse(
		Integer idCurso,
		String nombre,
		String nivel,
		long totalAlumnos,
		long alumnosConTarjeta,
		long alumnosSinTarjeta,
		List<DashboardAlumnoItem> alumnos
) {
}
