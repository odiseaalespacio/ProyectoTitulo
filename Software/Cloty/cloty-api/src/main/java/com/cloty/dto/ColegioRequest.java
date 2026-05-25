package com.cloty.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ColegioRequest(
		/** Si es null, el colegio queda sin cuenta hasta {@code /api/auth/activar-cuenta-colegio}. */
		Integer idUsuario,
		@NotBlank @Size(max = 12) String rut,
		@NotBlank @Size(max = 150) String nombre,
		/** Opcional al crear; el colegio lo define al activar su cuenta. */
		@Nullable @Email @Size(max = 150) String email,
		@Size(max = 20) String telefono,
		@Size(max = 255) String direccion
) {
}
