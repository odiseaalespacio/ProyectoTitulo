package com.cloty.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @param identificador Nombre de usuario (slug), RUT, o correo (apoderado o colegio con cuenta activa).
 */
public record LoginRequest(
		@NotBlank String identificador,
		@NotBlank String password
) {
}
