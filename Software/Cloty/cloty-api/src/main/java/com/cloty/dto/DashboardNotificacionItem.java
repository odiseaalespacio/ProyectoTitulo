package com.cloty.dto;

import com.cloty.domain.EstadoNotificacion;

import java.time.LocalDateTime;

public record DashboardNotificacionItem(
		Integer idNotificacion,
		String titulo,
		String mensaje,
		EstadoNotificacion estado,
		String nombreApoderado,
		LocalDateTime fechaEnvio
) {
}
