package com.cloty.service;

import com.cloty.domain.Colegio;
import com.cloty.dto.ColegioRequest;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.UsuarioRepository;
import com.cloty.web.error.ConflictException;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ColegioService {

	private final ColegioRepository colegioRepository;
	private final UsuarioRepository usuarioRepository;

	@Transactional(readOnly = true)
	public List<Colegio> listar() {
		return colegioRepository.findAllByOrderByNombreAsc();
	}

	@Transactional(readOnly = true)
	public Colegio obtener(Integer id) {
		return colegioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Colegio no encontrado: " + id));
	}

	@Transactional
	public Colegio crear(ColegioRequest req) {
		String rut = req.rut().trim();
		if (colegioRepository.existsByRut(rut)) {
			throw new ConflictException("El RUT del colegio ya está registrado");
		}
		if (req.idUsuario() != null) {
			asegurarUsuario(req.idUsuario());
			colegioRepository.findByIdUsuario(req.idUsuario()).ifPresent(c -> {
				throw new ConflictException("El usuario ya está asociado a un colegio");
			});
		}
		Colegio c = Colegio.builder()
				.idUsuario(req.idUsuario())
				.rut(rut)
				.nombre(req.nombre())
				.email(StringUtils.hasText(req.email()) ? req.email().trim() : null)
				.telefono(req.telefono())
				.direccion(req.direccion())
				.build();
		return colegioRepository.save(c);
	}

	@Transactional
	public Colegio actualizar(Integer id, ColegioRequest req) {
		Colegio c = obtener(id);
		if (req.idUsuario() != null) {
			if (!Objects.equals(req.idUsuario(), c.getIdUsuario())) {
				asegurarUsuario(req.idUsuario());
				colegioRepository.findByIdUsuario(req.idUsuario()).ifPresent(otro -> {
					if (!otro.getIdColegio().equals(id)) {
						throw new ConflictException("El usuario ya está asociado a otro colegio");
					}
				});
				c.setIdUsuario(req.idUsuario());
			}
		}
		String rut = req.rut().trim();
		if (!rut.equals(c.getRut()) && colegioRepository.existsByRut(rut)) {
			throw new ConflictException("El RUT del colegio ya está registrado");
		}
		c.setRut(rut);
		c.setNombre(req.nombre());
		if (StringUtils.hasText(req.email())) {
			c.setEmail(req.email().trim());
		}
		c.setTelefono(req.telefono());
		c.setDireccion(req.direccion());
		return colegioRepository.save(c);
	}

	@Transactional
	public void eliminar(Integer id) {
		if (!colegioRepository.existsById(id)) {
			throw new ResourceNotFoundException("Colegio no encontrado: " + id);
		}
		colegioRepository.deleteById(id);
	}

	private void asegurarUsuario(Integer idUsuario) {
		if (!usuarioRepository.existsById(idUsuario)) {
			throw new ResourceNotFoundException("Usuario no encontrado: " + idUsuario);
		}
	}
}
