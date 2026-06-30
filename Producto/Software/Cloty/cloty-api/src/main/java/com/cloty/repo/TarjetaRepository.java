package com.cloty.repo;

import com.cloty.domain.EstadoTarjeta;
import com.cloty.domain.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TarjetaRepository extends JpaRepository<Tarjeta, Integer> {

	Optional<Tarjeta> findByUidNfc(String uidNfc);

	List<Tarjeta> findByIdAlumnoOrderByFechaAsignacionDesc(Integer idAlumno);

	@Query("""
			select count(t) from Tarjeta t, Alumno a
			where t.idAlumno = a.idAlumno and a.idColegio = :idColegio and t.estado = :estado
			""")
	long countByColegioAndEstado(
			@Param("idColegio") Integer idColegio,
			@Param("estado") EstadoTarjeta estado);

	@Query("""
			select count(distinct a.idAlumno) from Tarjeta t, Alumno a
			where t.idAlumno = a.idAlumno and a.idColegio = :idColegio
			""")
	long countAlumnosConTarjeta(@Param("idColegio") Integer idColegio);

	@Query("""
			select t from Tarjeta t, Alumno a
			where t.idAlumno = a.idAlumno and a.idColegio = :idColegio
			order by t.fechaAsignacion desc
			""")
	List<Tarjeta> findByColegioId(@Param("idColegio") Integer idColegio);
}
