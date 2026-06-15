package com.cloty.dto;

import com.cloty.validation.RutChileno;
import com.cloty.validation.TelefonoChileno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuperUsuarioCompletoRequest(
		@NotBlank @Size(max = 50) String username,
		@NotBlank @Size(min = 4, max = 100) String password,
		@NotBlank @RutChileno @Size(max = 12) String rut,
		@NotBlank @Size(max = 100) String nombres,
		@NotBlank @Size(max = 100) String apellidos,
		@NotBlank @Email @Size(max = 150) String email,
		@TelefonoChileno @Size(max = 20) String telefono
) {
}
