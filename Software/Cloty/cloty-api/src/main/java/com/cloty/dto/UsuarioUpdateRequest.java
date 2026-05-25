package com.cloty.dto;

import com.cloty.domain.RolUsuario;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(
		@Size(max = 50) String username,
		@Size(max = 12) String rut,
		@Size(max = 100) String password,
		RolUsuario rol,
		Boolean estado
) {
}
