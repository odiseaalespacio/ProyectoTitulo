package com.cloty.dto;

import com.cloty.domain.TipoEvento;

import java.time.LocalDateTime;

public record ActividadRecienteResponse(
		Integer idEvento,
		TipoEvento tipoEvento,
		String accion,
		LocalDateTime fecha,
		String nombreAlumno,
		String nombreCurso,
		String uidNfc,
		String tipoPrenda,
		String descripcion
) {
}
