package com.cloty.dto;

import com.cloty.domain.EstadoNotificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificacionRequest(
		@NotNull Integer idEvento,
		@NotNull Integer idApoderado,
		@NotBlank @Size(max = 200) String titulo,
		@NotBlank @Size(max = 500) String mensaje,
		EstadoNotificacion estado,
		Boolean leida
) {
}
