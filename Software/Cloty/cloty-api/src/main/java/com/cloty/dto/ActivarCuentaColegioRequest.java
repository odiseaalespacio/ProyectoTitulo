package com.cloty.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivarCuentaColegioRequest(
		@NotBlank @Size(max = 12) String rut,
		@NotBlank @Email @Size(max = 150) String email,
		@NotBlank @Size(max = 20) String telefono,
		@NotBlank @Size(min = 4, max = 100) String password
) {
}
