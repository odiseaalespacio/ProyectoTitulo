package com.cloty.dto;

import com.cloty.domain.EstadoTarjeta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TarjetaRequest(
		@NotNull Integer idAlumno,
		@NotBlank @Size(max = 100) String uidNfc,
		@Size(max = 100) String codigoVisual,
		@Size(max = 100) String tipoPrenda,
		EstadoTarjeta estado
) {
}
