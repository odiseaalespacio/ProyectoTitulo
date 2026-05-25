package com.cloty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarContrasenaRequest(
		@NotBlank String contrasenaActual,
		@NotBlank @Size(min = 4, max = 100) String contrasenaNueva
) {
}
