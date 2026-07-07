package com.cloty.repo;

import com.cloty.domain.CodigoActivacion;
import com.cloty.domain.TipoEntidadActivacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodigoActivacionRepository extends JpaRepository<CodigoActivacion, Integer> {

	Optional<CodigoActivacion> findTopByTipoAndIdEntidadAndUsadoFalseOrderByIdCodigoActivacionDesc(
			TipoEntidadActivacion tipo, Integer idEntidad);

	List<CodigoActivacion> findByTipoAndIdEntidad(TipoEntidadActivacion tipo, Integer idEntidad);

	void deleteByTipoAndIdEntidad(TipoEntidadActivacion tipo, Integer idEntidad);
}
