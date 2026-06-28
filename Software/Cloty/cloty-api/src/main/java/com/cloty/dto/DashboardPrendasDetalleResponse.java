package com.cloty.dto;

import java.util.List;

public record DashboardPrendasDetalleResponse(
		long prendasEncontradasHoy,
		long prendasEncontradasTotal,
		long prendasEntregadasHoy,
		long prendasEntregadasTotal,
		List<ActividadRecienteResponse> actividad
) {
}
