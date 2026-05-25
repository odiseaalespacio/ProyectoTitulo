package com.cloty.security;

/**
 * Expresiones SpEL reutilizables para {@link org.springframework.security.access.prepost.PreAuthorize}.
 */
public final class ClotyRoles {

	private ClotyRoles() {
	}

	public static final String SUPER_USUARIO = "hasRole('SUPER_USUARIO')";
	public static final String ADMINISTRADOR = "hasRole('ADMINISTRADOR')";
	public static final String PANEL_ADMIN = "hasAnyRole('ADMINISTRADOR','SUPER_USUARIO')";
}
