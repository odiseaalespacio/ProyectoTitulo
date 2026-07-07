package com.cloty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ScanPrendaRequest(
		@NotBlank @Size(max = 100) String uidNfc,
		@Size(max = 255) String ubicacion,
		@Size(max = 500) String descripcion
) {
}
