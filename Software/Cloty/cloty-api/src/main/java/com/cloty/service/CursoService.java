package com.cloty.service;

import com.cloty.domain.Curso;
import com.cloty.dto.CursoRequest;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.CursoRepository;
import com.cloty.web.error.ConflictException;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CursoService {

	private final CursoRepository cursoRepository;
	private final ColegioRepository colegioRepository;
	private final CascadeEliminacionService cascadeEliminacionService;

	@Transactional(readOnly = true)
	public List<Curso> listarPorColegio(Integer idColegio) {
		if (!colegioRepository.existsById(idColegio)) {
			throw new ResourceNotFoundException("Colegio no encontrado: " + idColegio);
		}
		return cursoRepository.findByIdColegioOrderByNombreAsc(idColegio);
	}

	@Transactional(readOnly = true)
	public Curso obtener(Integer id) {
		return cursoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + id));
	}

	@Transactional
	public Curso crear(CursoRequest req) {
		if (!colegioRepository.existsById(req.idColegio())) {
			throw new ResourceNotFoundException("Colegio no encontrado: " + req.idColegio());
		}
		cursoRepository.findByIdColegioAndNombre(req.idColegio(), req.nombre()).ifPresent(c -> {
			throw new ConflictException("Ya existe un curso con ese nombre en el colegio");
		});
		Curso c = Curso.builder()
				.idColegio(req.idColegio())
				.nombre(req.nombre())
				.nivel(req.nivel())
				.estado(req.estado() != null ? req.estado() : Boolean.TRUE)
				.build();
		return cursoRepository.save(c);
	}

	@Transactional
	public Curso actualizar(Integer id, CursoRequest req) {
		Curso c = obtener(id);
		if (!req.idColegio().equals(c.getIdColegio()) || !req.nombre().equals(c.getNombre())) {
			if (!colegioRepository.existsById(req.idColegio())) {
				throw new ResourceNotFoundException("Colegio no encontrado: " + req.idColegio());
			}
			cursoRepository.findByIdColegioAndNombre(req.idColegio(), req.nombre()).ifPresent(otro -> {
				if (!otro.getIdCurso().equals(id)) {
					throw new ConflictException("Ya existe un curso con ese nombre en el colegio");
				}
			});
		}
		c.setIdColegio(req.idColegio());
		c.setNombre(req.nombre());
		c.setNivel(req.nivel());
		if (req.estado() != null) {
			c.setEstado(req.estado());
		}
		return cursoRepository.save(c);
	}

	@Transactional
	public void eliminar(Integer id) {
		cascadeEliminacionService.eliminarCursoCompleto(id);
	}
}
