package com.cloty.repo;

import com.cloty.domain.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {

	Optional<Alumno> findByRut(String rut);

	List<Alumno> findByIdColegioOrderByApellidosAscNombresAsc(Integer idColegio);

	List<Alumno> findByIdApoderadoOrderByApellidosAscNombresAsc(Integer idApoderado);

	List<Alumno> findByIdCursoOrderByApellidosAscNombresAsc(Integer idCurso);
}
