package com.cloty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Autoinscripción sin carga previa del colegio.
 * Sin correo: el username del usuario será el RUT; puede iniciar sesión con RUT.
 * Con correo: el username será el correo en minúsculas; puede iniciar sesión con correo o RUT.
 */
public record RegistroApoderadoRequest(
		@NotBlank @Size(min = 4, max = 100) String password,
		@NotBlank @Size(max = 12) String rut,
		@NotBlank @Size(max = 100) String nombres,
		@NotBlank @Size(max = 100) String apellidos,
		@Size(max = 150) String email,
		@Size(max = 20) String telefono,
		@Size(max = 255) String direccion
) {
}
