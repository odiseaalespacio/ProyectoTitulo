package com.cloty.security;

import com.cloty.domain.RolUsuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class ClotyUserDetails implements UserDetails {

	private final Integer idUsuario;
	private final String username;
	private final String password;
	private final boolean enabled;
	private final RolUsuario rol;
	private final Integer idColegio;
	private final Integer idApoderado;

	public ClotyUserDetails(Integer idUsuario, String username, String password, boolean enabled,
			RolUsuario rol, Integer idColegio, Integer idApoderado) {
		this.idUsuario = idUsuario;
		this.username = username;
		this.password = password;
		this.enabled = enabled;
		this.rol = rol;
		this.idColegio = idColegio;
		this.idApoderado = idApoderado;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
}
