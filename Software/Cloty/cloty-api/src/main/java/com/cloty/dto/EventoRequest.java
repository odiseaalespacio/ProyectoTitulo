package com.cloty.dto;

import com.cloty.domain.TipoEvento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventoRequest(
		@NotNull Integer idTarjeta,
		@NotNull TipoEvento tipoEvento,
		@Size(max = 500) String descripcion,
		@Size(max = 255) String ubicacion,
		Integer registradoPor
) {
}
