package com.cloty.dto;

import com.cloty.validation.RutChileno;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SolicitarCodigoActivacionRequest(
		@NotBlank @RutChileno @Size(max = 12) String rut
) {
}
