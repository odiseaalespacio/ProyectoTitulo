package com.cloty.dto;

import com.cloty.validation.RutChileno;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivarCuentaApoderadoRequest(
		@NotBlank @RutChileno @Size(max = 12) String rut,
		@NotBlank @Size(min = 6, max = 6) String codigo,
		@NotBlank @Size(min = 4, max = 100) String password
) {
}
