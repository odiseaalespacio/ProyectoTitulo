package com.cloty.dto;

import java.util.List;

public record DashboardNotificacionesDetalleResponse(
		long notificacionesEnviadas,
		long notificacionesPendientes,
		List<DashboardNotificacionItem> recientes
) {
}
