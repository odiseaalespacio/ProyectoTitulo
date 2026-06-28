package com.cloty.dto;

import com.cloty.domain.EstadoTarjeta;

public record DashboardTarjetaItem(
		Integer idTarjeta,
		String uidNfc,
		EstadoTarjeta estado,
		String tipoPrenda,
		String nombreAlumno,
		String nombreCurso
) {
}
