package com.cloty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlumnoRequest(
		@NotNull Integer idColegio,
		@NotNull Integer idApoderado,
		@NotNull Integer idCurso,
		@NotBlank @Size(max = 12) String rut,
		@NotBlank @Size(max = 100) String nombres,
		@NotBlank @Size(max = 100) String apellidos,
		Boolean estado
) {
}
