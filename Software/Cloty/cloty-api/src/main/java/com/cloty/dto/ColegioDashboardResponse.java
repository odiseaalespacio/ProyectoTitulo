package com.cloty.dto;

import java.util.List;

public record ColegioDashboardResponse(
		Integer idColegio,
		String nombreColegio,
		long tarjetasActivas,
		long alumnosConTarjeta,
		long prendasEncontradasHoy,
		long prendasEncontradasTotal,
		long prendasEntregadasHoy,
		long prendasEntregadasTotal,
		long notificacionesEnviadas,
		List<ActividadRecienteResponse> ultimasAcciones
) {
}
