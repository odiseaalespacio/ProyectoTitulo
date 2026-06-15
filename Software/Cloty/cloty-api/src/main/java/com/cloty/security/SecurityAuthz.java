package com.cloty.security;

import com.cloty.domain.Alumno;
import com.cloty.domain.RolUsuario;
import com.cloty.dto.AlumnoRequest;
import com.cloty.dto.ColegioApoderadoRequest;
import com.cloty.dto.CursoRequest;
import com.cloty.dto.EventoRequest;
import com.cloty.dto.NotificacionRequest;
import com.cloty.repo.AlumnoRepository;
import com.cloty.repo.ColegioApoderadoRepository;
import com.cloty.repo.CursoRepository;
import com.cloty.repo.EventoRepository;
import com.cloty.repo.NotificacionRepository;
import com.cloty.repo.TarjetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authz")
@RequiredArgsConstructor
public class SecurityAuthz {

	private final AlumnoRepository alumnoRepository;
	private final CursoRepository cursoRepository;
	private final TarjetaRepository tarjetaRepository;
	private final EventoRepository eventoRepository;
	private final NotificacionRepository notificacionRepository;
	private final ColegioApoderadoRepository colegioApoderadoRepository;

	private ClotyUserDetails principal() {
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		if (a == null || !(a.getPrincipal() instanceof ClotyUserDetails p)) {
			return null;
		}
		return p;
	}

	public boolean isSelfUser(Integer idUsuario) {
		ClotyUserDetails p = principal();
		return p != null && p.getIdUsuario().equals(idUsuario);
	}

	public boolean ownsColegio(Integer idColegio) {
		if (idColegio == null) {
			return false;
		}
		ClotyUserDetails p = principal();
		return p != null
				&& p.getRol() == RolUsuario.COLEGIO
				&& p.getIdColegio() != null
				&& p.getIdColegio().equals(idColegio);
	}

	public boolean ownsApoderado(Integer idApoderado) {
		if (idApoderado == null) {
			return false;
		}
		ClotyUserDetails p = principal();
		return p != null
				&& p.getRol() == RolUsuario.APODERADO
				&& p.getIdApoderado() != null
				&& p.getIdApoderado().equals(idApoderado);
	}

	public boolean ownsCurso(Integer idCurso) {
		return cursoRepository.findById(idCurso)
				.map(c -> ownsColegio(c.getIdColegio()))
				.orElse(false);
	}

	public boolean ownsAlumno(Integer idAlumno) {
		ClotyUserDetails p = principal();
		if (p == null) {
			return false;
		}
		Alumno a = alumnoRepository.findById(idAlumno).orElse(null);
		if (a == null) {
			return false;
		}
		if (p.getRol() == RolUsuario.COLEGIO && p.getIdColegio() != null) {
			return p.getIdColegio().equals(a.getIdColegio());
		}
		if (p.getRol() == RolUsuario.APODERADO && p.getIdApoderado() != null) {
			return p.getIdApoderado().equals(a.getIdApoderado());
		}
		return false;
	}

	public boolean ownsTarjeta(Integer idTarjeta) {
		return tarjetaRepository.findById(idTarjeta)
				.map(t -> ownsAlumno(t.getIdAlumno()))
				.orElse(false);
	}

	public boolean ownsTarjetaUid(String uidNfc) {
		return tarjetaRepository.findByUidNfc(uidNfc)
				.map(t -> ownsAlumno(t.getIdAlumno()))
				.orElse(false);
	}

	public boolean ownsEvento(Integer idEvento) {
		return eventoRepository.findById(idEvento)
				.map(e -> ownsTarjeta(e.getIdTarjeta()))
				.orElse(false);
	}

	public boolean ownsNotificacion(Integer idNotificacion) {
		ClotyUserDetails p = principal();
		if (p == null) {
			return false;
		}
		var n = notificacionRepository.findById(idNotificacion).orElse(null);
		if (n == null) {
			return false;
		}
		if (p.getRol() == RolUsuario.APODERADO && p.getIdApoderado() != null) {
			return p.getIdApoderado().equals(n.getIdApoderado());
		}
		if (p.getRol() == RolUsuario.COLEGIO && p.getIdColegio() != null) {
			return ownsEvento(n.getIdEvento());
		}
		return false;
	}

	public boolean apoderadoEnColegio(Integer idApoderado, Integer idColegio) {
		if (idApoderado == null || idColegio == null) {
			return false;
		}
		return colegioApoderadoRepository.existsByIdColegioAndIdApoderado(idColegio, idApoderado);
	}

	public boolean apoderadoAsociadoAMiColegio(Integer idApoderado) {
		ClotyUserDetails p = principal();
		if (p == null || p.getRol() != RolUsuario.COLEGIO || p.getIdColegio() == null || idApoderado == null) {
			return false;
		}
		return colegioApoderadoRepository.existsByIdColegioAndIdApoderado(p.getIdColegio(), idApoderado);
	}

	public boolean puedeVerColegioApoderadoPorId(Integer idColegioApoderado) {
		return colegioApoderadoRepository.findById(idColegioApoderado)
				.map(ca -> ownsColegio(ca.getIdColegio()) || ownsApoderado(ca.getIdApoderado()))
				.orElse(false);
	}

	public boolean colegioPuedeGestionarEventoRequest(EventoRequest req) {
		ClotyUserDetails p = principal();
		if (p == null || p.getRol() != RolUsuario.COLEGIO || p.getIdColegio() == null || req == null) {
			return false;
		}
		if (req.registradoPor() != null && !req.registradoPor().equals(p.getIdUsuario())) {
			return false;
		}
		return tarjetaRepository.findById(req.idTarjeta())
				.flatMap(t -> alumnoRepository.findById(t.getIdAlumno()))
				.map(a -> p.getIdColegio().equals(a.getIdColegio()))
				.orElse(false);
	}

	public boolean colegioPuedeGestionarEventoId(Integer idEvento) {
		ClotyUserDetails p = principal();
		return p != null && p.getRol() == RolUsuario.COLEGIO && ownsEvento(idEvento);
	}

	public boolean colegioPuedeGestionarNotificacionRequest(NotificacionRequest req) {
		ClotyUserDetails p = principal();
		if (p == null || p.getRol() != RolUsuario.COLEGIO || p.getIdColegio() == null || req == null) {
			return false;
		}
		return eventoRepository.findById(req.idEvento())
				.flatMap(ev -> tarjetaRepository.findById(ev.getIdTarjeta()))
				.flatMap(t -> alumnoRepository.findById(t.getIdAlumno()))
				.map(a -> p.getIdColegio().equals(a.getIdColegio()) && req.idApoderado().equals(a.getIdApoderado()))
				.orElse(false);
	}

	public boolean colegioPuedeGestionarNotificacionId(Integer idNotificacion) {
		ClotyUserDetails p = principal();
		return p != null && p.getRol() == RolUsuario.COLEGIO && ownsNotificacion(idNotificacion);
	}

	public boolean colegioPuedeGestionarAlumnoRequest(AlumnoRequest req) {
		ClotyUserDetails p = principal();
		if (p == null || p.getRol() != RolUsuario.COLEGIO || p.getIdColegio() == null || req == null) {
			return false;
		}
		if (!p.getIdColegio().equals(req.idColegio())) {
			return false;
		}
		return apoderadoEnColegio(req.idApoderado(), req.idColegio()) && ownsCurso(req.idCurso());
	}

	public boolean colegioPuedeGestionarAlumnoId(Integer idAlumno) {
		ClotyUserDetails p = principal();
		return p != null && p.getRol() == RolUsuario.COLEGIO && ownsAlumno(idAlumno);
	}

	public boolean colegioPuedeGestionarCursoRequest(CursoRequest req) {
		return req != null && ownsColegio(req.idColegio());
	}

	public boolean colegioPuedeGestionarCursoId(Integer idCurso) {
		return ownsCurso(idCurso);
	}

	public boolean colegioPuedeGestionarColegioApoderadoRequest(ColegioApoderadoRequest req) {
		return req != null && ownsColegio(req.idColegio());
	}

	public boolean colegioPuedeEliminarColegioApoderado(Integer idColegioApoderado) {
		return colegioApoderadoRepository.findById(idColegioApoderado)
				.map(ca -> ownsColegio(ca.getIdColegio()))
				.orElse(false);
	}
}
