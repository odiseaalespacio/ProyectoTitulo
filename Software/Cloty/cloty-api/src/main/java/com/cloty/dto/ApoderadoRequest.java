package com.cloty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApoderadoRequest(
		/** Si es null, el apoderado queda sin cuenta hasta que active vía {@code /api/auth/activar-cuenta-apoderado}. */
		Integer idUsuario,
		@NotBlank @Size(max = 12) String rut,
		@NotBlank @Size(max = 100) String nombres,
		@NotBlank @Size(max = 100) String apellidos,
		/** Opcional. */
		@Size(max = 150) String email,
		@Size(max = 20) String telefono,
		@Size(max = 255) String direccion
) {
}
