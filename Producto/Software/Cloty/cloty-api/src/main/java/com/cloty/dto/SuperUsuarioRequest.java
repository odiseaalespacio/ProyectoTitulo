package com.cloty.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SuperUsuarioRequest(
		@NotNull Integer idUsuario,
		@NotBlank @Size(max = 12) String rut,
		@NotBlank @Size(max = 100) String nombres,
		@NotBlank @Size(max = 100) String apellidos,
		@NotBlank @Email @Size(max = 150) String email,
		@Size(max = 20) String telefono
) {
}
