package com.cloty.dto;

public record EstadoCorreoResponse(
		boolean habilitado,
		String from,
		String smtpHost,
		int smtpPort,
		String smtpUser,
		boolean smtpPasswordConfigurada,
		boolean fromCoincideConUsuario,
		String advertencia) {
}
