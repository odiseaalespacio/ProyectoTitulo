package com.cloty.repo;

import com.cloty.domain.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Integer> {

	List<Curso> findByIdColegioOrderByNombreAsc(Integer idColegio);

	Optional<Curso> findByIdColegioAndNombre(Integer idColegio, String nombre);

	Optional<Curso> findByIdColegioAndNivel(Integer idColegio, String nivel);
}
