package com.cloty.dto;

import jakarta.validation.constraints.NotNull;

public record ColegioApoderadoRequest(
		@NotNull Integer idColegio,
		@NotNull Integer idApoderado
) {
}
