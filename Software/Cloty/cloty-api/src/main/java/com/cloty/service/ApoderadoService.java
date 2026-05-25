package com.cloty.service;

import com.cloty.domain.Apoderado;
import com.cloty.dto.ApoderadoRequest;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.UsuarioRepository;
import com.cloty.web.error.ConflictException;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApoderadoService {

	private final ApoderadoRepository apoderadoRepository;
	private final UsuarioRepository usuarioRepository;

	@Transactional(readOnly = true)
	public List<Apoderado> listar() {
		return apoderadoRepository.findAllByOrderByApellidosAscNombresAsc();
	}

	@Transactional(readOnly = true)
	public Apoderado obtener(Integer id) {
		return apoderadoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Apoderado no encontrado: " + id));
	}

	@Transactional
	public Apoderado crear(ApoderadoRequest req) {
		if (req.idUsuario() != null) {
			asegurarUsuario(req.idUsuario());
			apoderadoRepository.findByIdUsuario(req.idUsuario()).ifPresent(a -> {
				throw new ConflictException("El usuario ya está asociado a un apoderado");
			});
		}
		if (apoderadoRepository.existsByRut(req.rut())) {
			throw new ConflictException("El RUT ya está registrado");
		}
		Apoderado a = Apoderado.builder()
				.idUsuario(req.idUsuario())
				.rut(req.rut().trim())
				.nombres(req.nombres())
				.apellidos(req.apellidos())
				.email(emailObligatorioONull(req.email()))
				.telefono(blancoANull(req.telefono()))
				.direccion(blancoANull(req.direccion()))
				.build();
		return apoderadoRepository.save(a);
	}

	@Transactional
	public Apoderado actualizar(Integer id, ApoderadoRequest req) {
		Apoderado a = obtener(id);
		if (req.idUsuario() != null) {
			if (!Objects.equals(req.idUsuario(), a.getIdUsuario())) {
				asegurarUsuario(req.idUsuario());
				apoderadoRepository.findByIdUsuario(req.idUsuario()).ifPresent(otro -> {
					if (!otro.getIdApoderado().equals(id)) {
						throw new ConflictException("El usuario ya está asociado a otro apoderado");
					}
				});
				a.setIdUsuario(req.idUsuario());
			}
		}
		if (!req.rut().equals(a.getRut()) && apoderadoRepository.existsByRut(req.rut())) {
			throw new ConflictException("El RUT ya está registrado");
		}
		a.setRut(req.rut().trim());
		a.setNombres(req.nombres());
		a.setApellidos(req.apellidos());
		a.setEmail(emailObligatorioONull(req.email()));
		a.setTelefono(blancoANull(req.telefono()));
		a.setDireccion(blancoANull(req.direccion()));
		return apoderadoRepository.save(a);
	}

	@Transactional
	public void eliminar(Integer id) {
		if (!apoderadoRepository.existsById(id)) {
			throw new ResourceNotFoundException("Apoderado no encontrado: " + id);
		}
		apoderadoRepository.deleteById(id);
	}

	private void asegurarUsuario(Integer idUsuario) {
		if (!usuarioRepository.existsById(idUsuario)) {
			throw new ResourceNotFoundException("Usuario no encontrado: " + idUsuario);
		}
	}

	private static String blancoANull(String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		return s.trim();
	}

	private static String emailObligatorioONull(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		return email.trim();
	}
}
