package com.cloty.service;

import com.cloty.domain.RolUsuario;
import com.cloty.domain.SuperUsuario;
import com.cloty.domain.Usuario;
import com.cloty.dto.SuperUsuarioCompletoRequest;
import com.cloty.dto.SuperUsuarioRequest;
import com.cloty.dto.UsuarioCreateRequest;
import com.cloty.repo.SuperUsuarioRepository;
import com.cloty.repo.UsuarioRepository;
import com.cloty.web.error.BadRequestException;
import com.cloty.web.error.ConflictException;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperUsuarioService {

	private final SuperUsuarioRepository superUsuarioRepository;
	private final UsuarioRepository usuarioRepository;
	private final UsuarioService usuarioService;
	private final EmailService emailService;
	private final CascadeEliminacionService cascadeEliminacionService;

	@Value("${cloty.bootstrap.super-username:superadmin}")
	private String bootstrapSuperUsername;

	@Value("${cloty.bootstrap.super-rut:00000000-0}")
	private String bootstrapSuperRut;

	@Transactional(readOnly = true)
	public List<SuperUsuario> listar() {
		return superUsuarioRepository.findAll();
	}

	@Transactional(readOnly = true)
	public SuperUsuario obtener(Integer id) {
		return superUsuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Super usuario no encontrado: " + id));
	}

	@Transactional
	public SuperUsuario crearCompleto(SuperUsuarioCompletoRequest req) {
		Usuario usuario = usuarioService.crear(new UsuarioCreateRequest(
				req.username(),
				req.rut(),
				req.password(),
				RolUsuario.SUPER_USUARIO,
				true,
				null
		));
		return crear(new SuperUsuarioRequest(
				usuario.getIdUsuario(),
				req.rut(),
				req.nombres(),
				req.apellidos(),
				req.email(),
				req.telefono()
		), usuario.getUsername(), req.password());
	}

	@Transactional
	public SuperUsuario crear(SuperUsuarioRequest req, String username, String passwordPlano) {
		asegurarUsuarioSuper(req.idUsuario());
		superUsuarioRepository.findByIdUsuario(req.idUsuario()).ifPresent(s -> {
			throw new ConflictException("El usuario ya está asociado a un super usuario");
		});
		superUsuarioRepository.findByRut(req.rut()).ifPresent(s -> {
			throw new ConflictException("El RUT ya está registrado");
		});
		SuperUsuario s = SuperUsuario.builder()
				.idUsuario(req.idUsuario())
				.rut(req.rut())
				.nombres(req.nombres())
				.apellidos(req.apellidos())
				.email(req.email())
				.telefono(req.telefono())
				.build();
		SuperUsuario guardado = superUsuarioRepository.save(s);
		if (username != null && !username.isBlank()) {
			emailService.enviarBienvenidaSuperUsuario(guardado, username, passwordPlano);
		}
		return guardado;
	}

	@Transactional
	public SuperUsuario actualizar(Integer id, SuperUsuarioRequest req) {
		SuperUsuario s = obtener(id);
		validarNoEditarSuperRoot(s, req);
		if (!req.idUsuario().equals(s.getIdUsuario())) {
			asegurarUsuarioSuper(req.idUsuario());
			superUsuarioRepository.findByIdUsuario(req.idUsuario()).ifPresent(otro -> {
				if (!otro.getIdSuperUsuario().equals(id)) {
					throw new ConflictException("El usuario ya está asociado a otro super usuario");
				}
			});
			s.setIdUsuario(req.idUsuario());
		}
		superUsuarioRepository.findByRut(req.rut()).ifPresent(otro -> {
			if (!otro.getIdSuperUsuario().equals(id)) {
				throw new ConflictException("El RUT ya está registrado");
			}
		});
		s.setRut(req.rut());
		s.setNombres(req.nombres());
		s.setApellidos(req.apellidos());
		s.setEmail(req.email());
		s.setTelefono(req.telefono());
		return superUsuarioRepository.save(s);
	}

	@Transactional
	public void eliminar(Integer id) {
		SuperUsuario superUsuario = obtener(id);
		validarNoEliminarSuperRoot(superUsuario);
		Integer idUsuario = superUsuario.getIdUsuario();
		superUsuarioRepository.deleteById(id);
		cascadeEliminacionService.eliminarSuperUsuarioCompleto(id, idUsuario);
	}

	private void asegurarUsuarioSuper(Integer idUsuario) {
		Usuario u = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + idUsuario));
		if (u.getRol() != RolUsuario.SUPER_USUARIO) {
			throw new BadRequestException("El usuario debe tener rol SUPER_USUARIO");
		}
	}

	private void validarNoEliminarSuperRoot(SuperUsuario s) {
		Usuario u = usuarioRepository.findById(s.getIdUsuario()).orElse(null);
		if (u != null && bootstrapSuperUsername.equals(u.getUsername()) && bootstrapSuperRut.equals(u.getRut())) {
			throw new BadRequestException("No se puede eliminar el super usuario principal del sistema");
		}
	}

	private void validarNoEditarSuperRoot(SuperUsuario s, SuperUsuarioRequest req) {
		Usuario u = usuarioRepository.findById(s.getIdUsuario()).orElse(null);
		if (u == null || !bootstrapSuperUsername.equals(u.getUsername()) || !bootstrapSuperRut.equals(u.getRut())) {
			return;
		}
		if (!bootstrapSuperRut.equals(req.rut())) {
			throw new BadRequestException("No se puede modificar el RUT del super usuario principal del sistema");
		}
	}
}
