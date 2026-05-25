package com.cloty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Activa cuenta solo con el mismo RUT que cargó el colegio (no se valida el correo del CSV).
 */
public record ActivarCuentaApoderadoRequest(
		@NotBlank @Size(max = 12) String rut,
		@NotBlank @Size(min = 4, max = 100) String password
) {
}
