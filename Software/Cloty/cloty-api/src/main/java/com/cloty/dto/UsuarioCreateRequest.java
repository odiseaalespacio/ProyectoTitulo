package com.cloty.dto;

import com.cloty.domain.RolUsuario;
import com.cloty.validation.RutChileno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequest(
		/** Nombre visible: administrador, nombre del colegio o nombre del apoderado (según rol). */
		@NotBlank @Size(max = 50) String username,
		@NotBlank @RutChileno @Size(max = 12) String rut,
		@NotBlank @Size(min = 4, max = 100) String password,
		@NotNull RolUsuario rol,
		Boolean estado,
		@Email @Size(max = 150) String email
) {
}
