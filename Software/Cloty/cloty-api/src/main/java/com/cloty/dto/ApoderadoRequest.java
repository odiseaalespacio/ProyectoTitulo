package com.cloty.dto;

import com.cloty.validation.RutChileno;
import com.cloty.validation.TelefonoChileno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApoderadoRequest(
		/** Si es null, el apoderado queda sin cuenta hasta que active vía {@code /api/auth/activar-cuenta-apoderado}. */
		Integer idUsuario,
		@NotBlank @RutChileno @Size(max = 12) String rut,
		@NotBlank @Size(max = 100) String nombres,
		@NotBlank @Size(max = 100) String apellidos,
		/** Opcional. */
		@Email @Size(max = 150) String email,
		@TelefonoChileno @Size(max = 20) String telefono,
		@Size(max = 5) String codigoComuna,
		@Size(max = 255) String calleNumero
) {
}
