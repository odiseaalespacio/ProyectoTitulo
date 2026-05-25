package com.cloty.dto;

import java.util.List;

public record CargaMasivaResult(
		int filasLeidas,
		int creados,
		int omitidos,
		int errores,
		List<String> mensajes
) {
}
