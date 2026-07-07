package com.cloty.repo;

import com.cloty.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

	List<Notificacion> findByIdApoderadoOrderByFechaEnvioDesc(Integer idApoderado);

	List<Notificacion> findByIdEventoOrderByFechaEnvioDesc(Integer idEvento);

	@Query("""
			select n from Notificacion n, Evento e, Tarjeta t, Alumno a
			where n.idEvento = e.idEvento and e.idTarjeta = t.idTarjeta and t.idAlumno = a.idAlumno
			and a.idColegio = :idColegio
			order by n.fechaEnvio desc
			""")
	List<Notificacion> findByColegioId(@Param("idColegio") Integer idColegio);
}
