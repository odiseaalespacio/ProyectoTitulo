package com.cloty.domain;

public enum RolUsuario {
	COLEGIO,
	APODERADO,
	/** Operaciones del panel: colegios, CSV, tarjetas NFC, etc. */
	ADMINISTRADOR,
	/** Solo puede gestionar cuentas de administradores. */
	SUPER_USUARIO
}
