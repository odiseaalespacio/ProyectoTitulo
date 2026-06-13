package com.cloty.dto;

import com.cloty.validation.RutChileno;
import com.cloty.validation.TelefonoChileno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ColegioRequest(
		/** Si es null, el colegio queda sin cuenta hasta {@code /api/auth/activar-cuenta-colegio}. */
		Integer idUsuario,
		@NotBlank @RutChileno @Size(max = 12) String rut,
		@NotBlank @Size(max = 150) String nombre,
		@NotBlank @Email @Size(max = 150) String email,
		@TelefonoChileno @Size(max = 20) String telefono,
		@Size(max = 255) String direccion
) {
}
