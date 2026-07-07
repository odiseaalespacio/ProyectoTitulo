package com.cloty.repo;

import com.cloty.domain.ColegioApoderado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColegioApoderadoRepository extends JpaRepository<ColegioApoderado, Integer> {

	List<ColegioApoderado> findByIdColegio(Integer idColegio);

	List<ColegioApoderado> findByIdApoderado(Integer idApoderado);

	boolean existsByIdColegioAndIdApoderado(Integer idColegio, Integer idApoderado);
}
