package com.cloty.service;

import com.cloty.domain.ColegioApoderado;
import com.cloty.dto.ColegioApoderadoRequest;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.web.error.ConflictException;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColegioApoderadoService {

	private final ColegioApoderadoRepository colegioApoderadoRepository;
	private final ColegioRepository colegioRepository;
	private final ApoderadoRepository apoderadoRepository;

	@Transactional(readOnly = true)
	public List<ColegioApoderado> listarTodos() {
		return colegioApoderadoRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<ColegioApoderado> listarPorColegio(Integer idColegio) {
		if (!colegioRepository.existsById(idColegio)) {
			throw new ResourceNotFoundException("Colegio no encontrado: " + idColegio);
		}
		return colegioApoderadoRepository.findByIdColegio(idColegio);
	}

	@Transactional(readOnly = true)
	public List<ColegioApoderado> listarPorApoderado(Integer idApoderado) {
		if (!apoderadoRepository.existsById(idApoderado)) {
			throw new ResourceNotFoundException("Apoderado no encontrado: " + idApoderado);
		}
		return colegioApoderadoRepository.findByIdApoderado(idApoderado);
	}

	@Transactional(readOnly = true)
	public ColegioApoderado obtener(Integer id) {
		return colegioApoderadoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Asociación no encontrada: " + id));
	}

	@Transactional
	public ColegioApoderado crear(ColegioApoderadoRequest req) {
		if (!colegioRepository.existsById(req.idColegio())) {
			throw new ResourceNotFoundException("Colegio no encontrado: " + req.idColegio());
		}
		if (!apoderadoRepository.existsById(req.idApoderado())) {
			throw new ResourceNotFoundException("Apoderado no encontrado: " + req.idApoderado());
		}
		if (colegioApoderadoRepository.existsByIdColegioAndIdApoderado(req.idColegio(), req.idApoderado())) {
			throw new ConflictException("La asociación colegio-apoderado ya existe");
		}
		ColegioApoderado ca = ColegioApoderado.builder()
				.idColegio(req.idColegio())
				.idApoderado(req.idApoderado())
				.build();
		return colegioApoderadoRepository.save(ca);
	}

	@Transactional
	public void eliminar(Integer id) {
		if (!colegioApoderadoRepository.existsById(id)) {
			throw new ResourceNotFoundException("Asociación no encontrada: " + id);
		}
		colegioApoderadoRepository.deleteById(id);
	}
}
