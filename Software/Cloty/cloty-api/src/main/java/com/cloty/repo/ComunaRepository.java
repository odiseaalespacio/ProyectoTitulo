package com.cloty.repo;

import com.cloty.domain.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComunaRepository extends JpaRepository<Comuna, String> {

	List<Comuna> findByRegionCodigoRegionOrderByNombreAsc(String codigoRegion);

	List<Comuna> findAllByOrderByNombreAsc();

	@Query("SELECT c FROM Comuna c JOIN FETCH c.region WHERE c.codigoComuna = :codigoComuna")
	Optional<Comuna> findWithRegionByCodigoComuna(@Param("codigoComuna") String codigoComuna);
}
