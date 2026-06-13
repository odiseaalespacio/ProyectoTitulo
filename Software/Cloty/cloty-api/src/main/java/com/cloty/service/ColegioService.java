package com.cloty.service;

import com.cloty.domain.Colegio;
import com.cloty.dto.ColegioRequest;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.UsuarioRepository;
import com.cloty.validation.ChileValidacion;
import com.cloty.web.error.ConflictException;
import com.cloty.web.error.ResourceNotFoundException;
import com.cloty.web.error.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ColegioService {

	private final ColegioRepository colegioRepository;
	private final UsuarioRepository usuarioRepository;
	private final EmailService emailService;
	private final CascadeEliminacionService cascadeEliminacionService;

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
		String rut = ChileValidacion.formatearRutConGuion(req.rut());
		if (colegioRepository.existsByRut(rut)) {
			throw new ConflictException("El RUT del colegio ya está registrado");
		}
		if (req.idUsuario() != null) {
			asegurarUsuario(req.idUsuario());
			colegioRepository.findByIdUsuario(req.idUsuario()).ifPresent(c -> {
				throw new ConflictException("El usuario ya está asociado a un colegio");
			});
		}
		String emailNorm = normalizarEmailRequerido(req.email());
		asegurarEmailUnico(emailNorm, null);
		Colegio c = Colegio.builder()
				.idUsuario(req.idUsuario())
				.rut(rut)
				.nombre(req.nombre())
				.email(emailNorm)
				.telefono(req.telefono())
				.direccion(req.direccion())
				.build();
		c = colegioRepository.save(c);
		if (c.getIdUsuario() == null) {
			emailService.enviarInstructivoActivacionColegio(c);
		}
		return c;
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
		String rut = ChileValidacion.formatearRutConGuion(req.rut());
		if (!rut.equals(c.getRut()) && colegioRepository.existsByRut(rut)) {
			throw new ConflictException("El RUT del colegio ya está registrado");
		}
		c.setRut(rut);
		c.setNombre(req.nombre());
		String emailNorm = normalizarEmailRequerido(req.email());
		asegurarEmailUnico(emailNorm, id);
		c.setEmail(emailNorm);
		c.setTelefono(req.telefono());
		c.setDireccion(req.direccion());
		return colegioRepository.save(c);
	}

	@Transactional
	public void eliminar(Integer id) {
		cascadeEliminacionService.eliminarColegioCompleto(id);
	}

	private void asegurarUsuario(Integer idUsuario) {
		if (!usuarioRepository.existsById(idUsuario)) {
			throw new ResourceNotFoundException("Usuario no encontrado: " + idUsuario);
		}
	}

	private void asegurarEmailUnico(String email, Integer idColegioExcluir) {
		colegioRepository.findByEmailIgnoreCase(email)
				.filter(c -> idColegioExcluir == null || !c.getIdColegio().equals(idColegioExcluir))
				.ifPresent(c -> {
					throw new ConflictException("El correo ya está registrado en otro colegio");
				});
	}

	private static String normalizarEmailRequerido(String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("El correo es obligatorio");
		}
		String t = email.trim();
		if (!t.contains("@") || t.indexOf('@') == 0 || t.endsWith("@")) {
			throw new BadRequestException("El formato del correo no es válido");
		}
		return t.toLowerCase(Locale.ROOT);
	}
}
