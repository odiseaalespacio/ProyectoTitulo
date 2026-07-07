package com.cloty.repo;

import com.cloty.domain.Apoderado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApoderadoRepository extends JpaRepository<Apoderado, Integer> {

	Optional<Apoderado> findByIdUsuario(Integer idUsuario);

	Optional<Apoderado> findByRut(String rut);

	boolean existsByRut(String rut);

	boolean existsByEmailIgnoreCase(String email);

	Optional<Apoderado> findByEmailIgnoreCase(String email);

	List<Apoderado> findAllByOrderByApellidosAscNombresAsc();
}
