package com.cloty.security;

import com.cloty.domain.RolUsuario;
import com.cloty.web.error.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityCurrentUser {

	public Optional<ClotyUserDetails> current() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof ClotyUserDetails p)) {
			return Optional.empty();
		}
		return Optional.of(p);
	}

	public ClotyUserDetails require() {
		return current().orElseThrow(() -> new BadRequestException("No autenticado"));
	}

	public boolean isSuperUsuario() {
		return current().map(ClotyUserDetails::getRol).filter(r -> r == RolUsuario.SUPER_USUARIO).isPresent();
	}
}
