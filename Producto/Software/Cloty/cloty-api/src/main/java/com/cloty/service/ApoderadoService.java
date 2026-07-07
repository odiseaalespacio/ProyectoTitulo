package com.cloty.service;

import com.cloty.domain.Apoderado;
import com.cloty.dto.ApoderadoRequest;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.UsuarioRepository;
import com.cloty.validation.ChileValidacion;
import com.cloty.web.error.ConflictException;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApoderadoService {

	private final ApoderadoRepository apoderadoRepository;
	private final ColegioApoderadoRepository colegioApoderadoRepository;
	private final ColegioRepository colegioRepository;
	private final UsuarioRepository usuarioRepository;
	private final CascadeEliminacionService cascadeEliminacionService;
	private final UbicacionService ubicacionService;

	@Transactional(readOnly = true)
	public List<Apoderado> listar() {
		return apoderadoRepository.findAllByOrderByApellidosAscNombresAsc();
	}

	@Transactional(readOnly = true)
	public Apoderado obtener(Integer id) {
		return apoderadoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Apoderado no encontrado: " + id));
	}

	@Transactional(readOnly = true)
	public List<Apoderado> listarPorColegio(Integer idColegio) {
		if (!colegioRepository.existsById(idColegio)) {
			throw new ResourceNotFoundException("Colegio no encontrado: " + idColegio);
		}
		List<Integer> ids = colegioApoderadoRepository.findByIdColegio(idColegio).stream()
				.map(ca -> ca.getIdApoderado())
				.toList();
		if (ids.isEmpty()) {
			return List.of();
		}
		return apoderadoRepository.findAllById(ids).stream()
				.sorted(Comparator.comparing(Apoderado::getApellidos).thenComparing(Apoderado::getNombres))
				.toList();
	}

	@Transactional
	public Apoderado crear(ApoderadoRequest req) {
		if (req.idUsuario() != null) {
			asegurarUsuario(req.idUsuario());
			apoderadoRepository.findByIdUsuario(req.idUsuario()).ifPresent(a -> {
				throw new ConflictException("El usuario ya está asociado a un apoderado");
			});
		}
		String rutNorm = ChileValidacion.formatearRutConGuion(req.rut());
		if (apoderadoRepository.existsByRut(rutNorm)) {
			throw new ConflictException("El RUT ya está registrado");
		}
		ubicacionService.validarComuna(req.codigoComuna());
		Apoderado a = Apoderado.builder()
				.idUsuario(req.idUsuario())
				.rut(rutNorm)
				.nombres(req.nombres())
				.apellidos(req.apellidos())
				.email(emailObligatorioONull(req.email()))
				.telefono(blancoANull(req.telefono()))
				.codigoComuna(blancoANull(req.codigoComuna()))
				.calleNumero(blancoANull(req.calleNumero()))
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
		String rutNorm = ChileValidacion.formatearRutConGuion(req.rut());
		if (!rutNorm.equals(a.getRut()) && apoderadoRepository.existsByRut(rutNorm)) {
			throw new ConflictException("El RUT ya está registrado");
		}
		a.setRut(rutNorm);
		a.setNombres(req.nombres());
		a.setApellidos(req.apellidos());
		a.setEmail(emailObligatorioONull(req.email()));
		a.setTelefono(blancoANull(req.telefono()));
		ubicacionService.validarComuna(req.codigoComuna());
		a.setCodigoComuna(blancoANull(req.codigoComuna()));
		a.setCalleNumero(blancoANull(req.calleNumero()));
		return apoderadoRepository.save(a);
	}

	@Transactional
	public void eliminar(Integer id) {
		cascadeEliminacionService.eliminarApoderadoCompleto(id);
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
