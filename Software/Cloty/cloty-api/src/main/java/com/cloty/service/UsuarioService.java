package com.cloty.service;

import com.cloty.domain.RolUsuario;
import com.cloty.domain.Usuario;
import com.cloty.dto.UsuarioCreateRequest;
import com.cloty.dto.UsuarioUpdateRequest;
import com.cloty.repo.UsuarioRepository;
import com.cloty.security.SecurityCurrentUser;
import com.cloty.web.error.BadRequestException;
import com.cloty.web.error.ConflictException;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecurityCurrentUser securityCurrentUser;
	private final EmailService emailService;
	private final CascadeEliminacionService cascadeEliminacionService;

	@Value("${cloty.bootstrap.super-username:superadmin}")
	private String bootstrapSuperUsername;

	@Value("${cloty.bootstrap.super-rut:00000000-0}")
	private String bootstrapSuperRut;

	@Transactional(readOnly = true)
	public List<Usuario> listar() {
		return usuarioRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Usuario obtener(Integer id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
	}

	@Transactional
	public Usuario crear(UsuarioCreateRequest req) {
		validarAsignacionRolPrivilegiado(req.rol(), null);
		if (req.rol() == RolUsuario.SUPER_USUARIO && (req.email() == null || req.email().isBlank())) {
			throw new BadRequestException("El correo es obligatorio para super usuarios");
		}
		if (usuarioRepository.existsByUsername(req.username().trim())) {
			throw new ConflictException("El nombre de usuario ya existe");
		}
		String rut = req.rut().trim();
		if (usuarioRepository.existsByRut(rut)) {
			throw new ConflictException("El RUT ya está asociado a otro usuario");
		}
		Usuario u = Usuario.builder()
				.username(req.username().trim())
				.rut(rut)
				.passwordHash(passwordEncoder.encode(req.password()))
				.rol(req.rol())
				.estado(req.estado() != null ? req.estado() : Boolean.TRUE)
				.build();
		u = usuarioRepository.save(u);
		if (req.rol() == RolUsuario.SUPER_USUARIO && req.email() != null && !req.email().isBlank()) {
			emailService.enviarBienvenidaSuperUsuario(
					req.email().trim(), u.getUsername(), u.getRut(), req.password());
		}
		return u;
	}

	@Transactional
	public Usuario actualizar(Integer id, UsuarioUpdateRequest req) {
		Usuario u = obtener(id);
		if (StringUtils.hasText(req.username()) && !req.username().trim().equals(u.getUsername())) {
			if (usuarioRepository.existsByUsername(req.username().trim())) {
				throw new ConflictException("El nombre de usuario ya existe");
			}
			u.setUsername(req.username().trim());
		}
		if (StringUtils.hasText(req.rut())) {
			String nr = req.rut().trim();
			if (!nr.equals(Optional.ofNullable(u.getRut()).orElse(""))) {
				if (usuarioRepository.existsByRut(nr)) {
					throw new ConflictException("El RUT ya está asociado a otro usuario");
				}
				u.setRut(nr);
			}
		}
		if (StringUtils.hasText(req.password())) {
			if (req.password().length() < 4) {
				throw new BadRequestException("La contraseña debe tener al menos 4 caracteres");
			}
			u.setPasswordHash(passwordEncoder.encode(req.password()));
		}
		if (req.rol() != null) {
			validarAsignacionRolPrivilegiado(req.rol(), u.getRol());
			u.setRol(req.rol());
		}
		if (req.estado() != null) {
			u.setEstado(req.estado());
		}
		return usuarioRepository.save(u);
	}

	@Transactional
	public void eliminar(Integer id) {
		Usuario u = obtener(id);
		validarNoEliminarSuperRoot(u);
		cascadeEliminacionService.eliminarUsuarioCompleto(id);
	}

	private void validarNoEliminarSuperRoot(Usuario u) {
		if (u.getRol() != RolUsuario.SUPER_USUARIO) {
			return;
		}
		if (bootstrapSuperUsername.equals(u.getUsername()) && bootstrapSuperRut.equals(u.getRut())) {
			throw new BadRequestException("No se puede eliminar el super usuario principal del sistema");
		}
	}

	private void validarAsignacionRolPrivilegiado(RolUsuario nuevoRol, RolUsuario rolActual) {
		if (nuevoRol != RolUsuario.ADMINISTRADOR && nuevoRol != RolUsuario.SUPER_USUARIO) {
			return;
		}
		if (!securityCurrentUser.isSuperUsuario()) {
			throw new BadRequestException("Solo un super usuario puede asignar el rol " + nuevoRol);
		}
		if (rolActual == RolUsuario.SUPER_USUARIO && nuevoRol != RolUsuario.SUPER_USUARIO) {
			throw new BadRequestException("No se puede degradar un super usuario desde este endpoint");
		}
	}
}
