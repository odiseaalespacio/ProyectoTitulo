package com.cloty.service;

import com.cloty.domain.EstadoTarjeta;
import com.cloty.domain.Tarjeta;
import com.cloty.dto.TarjetaRequest;
import com.cloty.repo.AlumnoRepository;
import com.cloty.repo.TarjetaRepository;
import com.cloty.web.error.ConflictException;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TarjetaService {

	private final TarjetaRepository tarjetaRepository;
	private final AlumnoRepository alumnoRepository;
	private final CascadeEliminacionService cascadeEliminacionService;

	@Transactional(readOnly = true)
	public List<Tarjeta> listarPorAlumno(Integer idAlumno) {
		if (!alumnoRepository.existsById(idAlumno)) {
			throw new ResourceNotFoundException("Alumno no encontrado: " + idAlumno);
		}
		return tarjetaRepository.findByIdAlumnoOrderByFechaAsignacionDesc(idAlumno);
	}

	@Transactional(readOnly = true)
	public Tarjeta obtener(Integer id) {
		return tarjetaRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada: " + id));
	}

	@Transactional(readOnly = true)
	public Tarjeta obtenerPorUid(String uidNfc) {
		return tarjetaRepository.findByUidNfc(uidNfc)
				.orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada para UID: " + uidNfc));
	}

	@Transactional
	public Tarjeta crear(TarjetaRequest req) {
		if (!alumnoRepository.existsById(req.idAlumno())) {
			throw new ResourceNotFoundException("Alumno no encontrado: " + req.idAlumno());
		}
		tarjetaRepository.findByUidNfc(req.uidNfc()).ifPresent(t -> {
			throw new ConflictException("El UID NFC ya está registrado");
		});
		Tarjeta t = Tarjeta.builder()
				.idAlumno(req.idAlumno())
				.uidNfc(req.uidNfc())
				.codigoVisual(req.codigoVisual())
				.tipoPrenda(req.tipoPrenda())
				.estado(req.estado() != null ? req.estado() : EstadoTarjeta.ACTIVA)
				.build();
		return tarjetaRepository.save(t);
	}

	@Transactional
	public Tarjeta actualizar(Integer id, TarjetaRequest req) {
		Tarjeta t = obtener(id);
		if (!req.idAlumno().equals(t.getIdAlumno()) && !alumnoRepository.existsById(req.idAlumno())) {
			throw new ResourceNotFoundException("Alumno no encontrado: " + req.idAlumno());
		}
		if (!req.uidNfc().equals(t.getUidNfc())) {
			tarjetaRepository.findByUidNfc(req.uidNfc()).ifPresent(otra -> {
				if (!otra.getIdTarjeta().equals(id)) {
					throw new ConflictException("El UID NFC ya está registrado");
				}
			});
		}
		t.setIdAlumno(req.idAlumno());
		t.setUidNfc(req.uidNfc());
		t.setCodigoVisual(req.codigoVisual());
		t.setTipoPrenda(req.tipoPrenda());
		if (req.estado() != null) {
			t.setEstado(req.estado());
		}
		return tarjetaRepository.save(t);
	}

	@Transactional
	public void eliminar(Integer id) {
		cascadeEliminacionService.eliminarTarjetaCompleta(id);
	}
}
