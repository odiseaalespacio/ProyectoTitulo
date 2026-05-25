package com.cloty.dto;

import com.cloty.domain.TipoEvento;

import java.time.LocalDateTime;

public record OperacionPrendaResponse(
		TipoEvento tipoEvento,
		String accion,
		Integer idEvento,
		Integer idNotificacion,
		Integer idTarjeta,
		String uidNfc,
		Integer idAlumno,
		String nombreAlumno,
		String nombreCurso,
		Integer idApoderado,
		String nombreApoderado,
		String tipoPrenda,
		String mensaje,
		LocalDateTime fechaEvento
) {
}
