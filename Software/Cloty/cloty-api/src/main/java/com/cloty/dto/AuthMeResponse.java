package com.cloty.dto;

import com.cloty.domain.RolUsuario;

public record AuthMeResponse(
		Integer idUsuario,
		String username,
		RolUsuario rol,
		Integer idColegio,
		Integer idApoderado
) {
}
