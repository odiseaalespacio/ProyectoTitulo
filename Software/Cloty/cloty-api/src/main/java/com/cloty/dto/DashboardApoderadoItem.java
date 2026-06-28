package com.cloty.dto;

public record DashboardApoderadoItem(
		Integer idApoderado,
		String rut,
		String nombres,
		String apellidos,
		String email,
		boolean tieneCuenta
) {
}
