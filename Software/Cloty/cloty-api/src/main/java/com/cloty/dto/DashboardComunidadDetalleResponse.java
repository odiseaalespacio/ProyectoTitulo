package com.cloty.dto;

import java.util.List;

public record DashboardComunidadDetalleResponse(
		long totalAlumnos,
		long totalApoderados,
		long totalCursos,
		long apoderadosConCuenta,
		long alumnosConTarjeta,
		long alumnosSinTarjeta,
		List<DashboardAlumnoItem> alumnosLista,
		List<DashboardApoderadoItem> apoderadosLista
) {
}
