package com.cloty.dto;

import java.util.List;

public record ColegioDashboardResponse(
		Integer idColegio,
		String nombreColegio,
		long totalAlumnos,
		long totalApoderados,
		long totalCursos,
		long apoderadosConCuenta,
		long alumnosConTarjeta,
		long alumnosSinTarjeta,
		long tarjetasActivas,
		long tarjetasPerdidas,
		long tarjetasDesactivadas,
		long prendasEncontradasHoy,
		long prendasEncontradasTotal,
		long prendasEntregadasHoy,
		long prendasEntregadasTotal,
		long notificacionesEnviadas,
		long notificacionesPendientes,
		List<ResumenCursoDashboard> resumenCursos,
		List<ActividadRecienteResponse> ultimasAcciones
) {
}
