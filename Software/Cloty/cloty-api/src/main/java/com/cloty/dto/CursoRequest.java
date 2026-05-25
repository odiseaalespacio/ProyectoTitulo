package com.cloty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CursoRequest(
		@NotNull Integer idColegio,
		@NotBlank @Size(max = 50) String nombre,
		@Size(max = 50) String nivel,
		Boolean estado
) {
}
