package com.cloty.repo;

import com.cloty.domain.Evento;
import com.cloty.domain.TipoEvento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Integer> {

	List<Evento> findByIdTarjetaOrderByFechaEventoDesc(Integer idTarjeta);

	@Query("""
			select e from Evento e, Tarjeta t, Alumno a
			where e.idTarjeta = t.idTarjeta and t.idAlumno = a.idAlumno
			and a.idColegio = :idColegio
			order by e.fechaEvento desc
			""")
	List<Evento> findByColegioId(@Param("idColegio") Integer idColegio, Pageable pageable);

	@Query("""
			select count(e) from Evento e, Tarjeta t, Alumno a
			where e.idTarjeta = t.idTarjeta and t.idAlumno = a.idAlumno
			and a.idColegio = :idColegio and e.tipoEvento = :tipo
			""")
	long countByColegioAndTipo(@Param("idColegio") Integer idColegio, @Param("tipo") TipoEvento tipo);

	@Query("""
			select count(e) from Evento e, Tarjeta t, Alumno a
			where e.idTarjeta = t.idTarjeta and t.idAlumno = a.idAlumno
			and a.idColegio = :idColegio and e.tipoEvento = :tipo
			and e.fechaEvento >= :desde
			""")
	long countByColegioAndTipoDesde(
			@Param("idColegio") Integer idColegio,
			@Param("tipo") TipoEvento tipo,
			@Param("desde") LocalDateTime desde);
}
