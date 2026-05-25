package com.cloty.service;

import com.cloty.domain.Administrador;
import com.cloty.domain.RolUsuario;
import com.cloty.domain.Usuario;
import com.cloty.dto.AdministradorCompletoRequest;
import com.cloty.dto.AdministradorRequest;
import com.cloty.dto.UsuarioCreateRequest;
import com.cloty.repo.AdministradorRepository;
import com.cloty.repo.UsuarioRepository;
import com.cloty.web.error.BadRequestException;
import com.cloty.web.error.ConflictException;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdministradorService {

	private final AdministradorRepository administradorRepository;
	private final UsuarioRepository usuarioRepository;
	private final UsuarioService usuarioService;

	@Transactional(readOnly = true)
	public List<Administrador> listar() {
		return administradorRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Administrador obtener(Integer id) {
		return administradorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Administrador no encontrado: " + id));
	}

	@Transactional
	public Administrador crearCompleto(AdministradorCompletoRequest req) {
		Usuario usuario = usuarioService.crear(new UsuarioCreateRequest(
				req.username(),
				req.rut(),
				req.password(),
				RolUsuario.ADMINISTRADOR,
				true
		));
		return crear(new AdministradorRequest(
				usuario.getIdUsuario(),
				req.rut(),
				req.nombres(),
				req.apellidos(),
				req.email(),
				req.telefono()
		));
	}

	@Transactional
	public Administrador crear(AdministradorRequest req) {
		asegurarUsuario(req.idUsuario());
		administradorRepository.findByIdUsuario(req.idUsuario()).ifPresent(a -> {
			throw new ConflictException("El usuario ya está asociado a un administrador");
		});
		administradorRepository.findByRut(req.rut()).ifPresent(a -> {
			throw new ConflictException("El RUT ya está registrado");
		});
		Administrador a = Administrador.builder()
				.idUsuario(req.idUsuario())
				.rut(req.rut())
				.nombres(req.nombres())
				.apellidos(req.apellidos())
				.email(req.email())
				.telefono(req.telefono())
				.build();
		return administradorRepository.save(a);
	}

	@Transactional
	public Administrador actualizar(Integer id, AdministradorRequest req) {
		Administrador a = obtener(id);
		if (!req.idUsuario().equals(a.getIdUsuario())) {
			asegurarUsuario(req.idUsuario());
			administradorRepository.findByIdUsuario(req.idUsuario()).ifPresent(otro -> {
				if (!otro.getIdAdministrador().equals(id)) {
					throw new ConflictException("El usuario ya está asociado a otro administrador");
				}
			});
			a.setIdUsuario(req.idUsuario());
		}
		administradorRepository.findByRut(req.rut()).ifPresent(otro -> {
			if (!otro.getIdAdministrador().equals(id)) {
				throw new ConflictException("El RUT ya está registrado");
			}
		});
		a.setRut(req.rut());
		a.setNombres(req.nombres());
		a.setApellidos(req.apellidos());
		a.setEmail(req.email());
		a.setTelefono(req.telefono());
		return administradorRepository.save(a);
	}

	@Transactional
	public void eliminar(Integer id) {
		if (!administradorRepository.existsById(id)) {
			throw new ResourceNotFoundException("Administrador no encontrado: " + id);
		}
		administradorRepository.deleteById(id);
	}

	private void asegurarUsuario(Integer idUsuario) {
		Usuario u = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + idUsuario));
		if (u.getRol() != RolUsuario.ADMINISTRADOR) {
			throw new BadRequestException("El usuario debe tener rol ADMINISTRADOR");
		}
	}
}
