package com.cloty.repo;

import com.cloty.domain.Colegio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColegioRepository extends JpaRepository<Colegio, Integer> {

	Optional<Colegio> findByIdUsuario(Integer idUsuario);

	Optional<Colegio> findByRut(String rut);

	Optional<Colegio> findByEmailIgnoreCase(String email);

	boolean existsByRut(String rut);

	List<Colegio> findAllByOrderByNombreAsc();
}
