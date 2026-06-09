package com.cloty.service;

import com.cloty.domain.Alumno;
import com.cloty.domain.Apoderado;
import com.cloty.domain.Colegio;
import com.cloty.domain.Curso;
import com.cloty.domain.EstadoNotificacion;
import com.cloty.domain.EstadoTarjeta;
import com.cloty.domain.Evento;
import com.cloty.domain.Notificacion;
import com.cloty.domain.Tarjeta;
import com.cloty.domain.TipoEvento;
import com.cloty.dto.ActividadRecienteResponse;
import com.cloty.dto.ColegioDashboardResponse;
import com.cloty.dto.EventoRequest;
import com.cloty.dto.NotificacionRequest;
import com.cloty.dto.OperacionPrendaResponse;
import com.cloty.dto.ScanPrendaRequest;
import com.cloty.repo.AlumnoRepository;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.CursoRepository;
import com.cloty.repo.EventoRepository;
import com.cloty.repo.NotificacionRepository;
import com.cloty.repo.TarjetaRepository;
import com.cloty.web.error.BadRequestException;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColegioOperacionService {

	private final TarjetaRepository tarjetaRepository;
	private final AlumnoRepository alumnoRepository;
	private final ApoderadoRepository apoderadoRepository;
	private final CursoRepository cursoRepository;
	private final ColegioRepository colegioRepository;
	private final EventoRepository eventoRepository;
	private final EventoService eventoService;
	private final NotificacionService notificacionService;
	private final NotificacionRepository notificacionRepository;

	@Transactional
	public OperacionPrendaResponse procesarEscaneo(Integer idColegio, Integer idUsuario, ScanPrendaRequest req) {
		asegurarColegio(idColegio);
		Tarjeta tarjeta = tarjetaRepository.findByUidNfc(req.uidNfc().trim().toUpperCase())
				.or(() -> tarjetaRepository.findByUidNfc(req.uidNfc().trim()))
				.orElseThrow(() -> new ResourceNotFoundException(
						"Tarjeta NFC no registrada. Cárguela desde Cloty Administrador."));

		Alumno alumno = alumnoRepository.findById(tarjeta.getIdAlumno())
				.orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado para la tarjeta"));
		if (!alumno.getIdColegio().equals(idColegio)) {
			throw new BadRequestException("Esta tarjeta no pertenece a su colegio");
		}
		if (tarjeta.getEstado() == EstadoTarjeta.DESACTIVADA) {
			throw new BadRequestException("La tarjeta está desactivada");
		}

		Apoderado apoderado = apoderadoRepository.findById(alumno.getIdApoderado())
				.orElseThrow(() -> new ResourceNotFoundException("Apoderado no encontrado"));
		Curso curso = cursoRepository.findById(alumno.getIdCurso()).orElse(null);
		String nombreCurso = curso != null ? curso.getNombre() : null;
		String nombreAlumno = alumno.getNombres() + " " + alumno.getApellidos();
		String tipoPrenda = tarjeta.getTipoPrenda() != null ? tarjeta.getTipoPrenda() : "Prenda";

		List<Evento> historial = eventoRepository.findByIdTarjetaOrderByIdEventoDesc(tarjeta.getIdTarjeta());
		Evento ultimo = historial.isEmpty() ? null : historial.get(0);

		log.info("=== SCAN uid={} tarjetaId={} totalEventos={} ultimoTipo={} ===",
				req.uidNfc(), tarjeta.getIdTarjeta(),
				historial.size(),
				ultimo != null ? ultimo.getTipoEvento() : "NINGUNO");

		if (ultimo != null && ultimo.getFechaEvento() != null) {
			long diffMs = java.time.Duration.between(ultimo.getFechaEvento(), LocalDateTime.now()).toMillis();
			log.info("  diffMs={} (debounce if < 5000)", diffMs);
			if (diffMs < 5000) {
				String accion = ultimo.getTipoEvento() == TipoEvento.PRENDA_ENCONTRADA ? "ENCONTRADA" : "ENTREGADA";
				return new OperacionPrendaResponse(
						ultimo.getTipoEvento(),
						accion,
						ultimo.getIdEvento(),
						null,
						tarjeta.getIdTarjeta(),
						tarjeta.getUidNfc(),
						alumno.getIdAlumno(),
						nombreAlumno,
						nombreCurso,
						apoderado.getIdApoderado(),
						apoderado.getNombres() + " " + apoderado.getApellidos(),
						tipoPrenda,
						accion.equals("ENCONTRADA")
								? "Prenda ya registrada como encontrada"
								: "Prenda ya registrada como entregada",
						ultimo.getFechaEvento()
				);
			}
		}

		boolean pendienteEntrega = ultimo != null && ultimo.getTipoEvento() == TipoEvento.PRENDA_ENCONTRADA;
		log.info("  pendienteEntrega={} -> {}", pendienteEntrega, pendienteEntrega ? "ENTREGA" : "ENCONTRADA");

		if (pendienteEntrega) {
			return registrarEntrega(idColegio, idUsuario, req, tarjeta, alumno, apoderado, nombreAlumno, nombreCurso, tipoPrenda);
		}
		return registrarEncontrada(idColegio, idUsuario, req, tarjeta, alumno, apoderado, nombreAlumno, nombreCurso, tipoPrenda);
	}

	private OperacionPrendaResponse registrarEncontrada(
			Integer idColegio,
			Integer idUsuario,
			ScanPrendaRequest req,
			Tarjeta tarjeta,
			Alumno alumno,
			Apoderado apoderado,
			String nombreAlumno,
			String nombreCurso,
			String tipoPrenda) {
		String ubicacion = req.ubicacion() != null && !req.ubicacion().isBlank()
				? req.ubicacion().trim()
				: "Colegio";
		String descripcion = req.descripcion() != null && !req.descripcion().isBlank()
				? req.descripcion().trim()
				: tipoPrenda + " de " + nombreAlumno + " encontrada en " + ubicacion;

		Evento evento = eventoService.crear(new EventoRequest(
				tarjeta.getIdTarjeta(),
				TipoEvento.PRENDA_ENCONTRADA,
				descripcion,
				ubicacion,
				idUsuario
		));

		String titulo = "Prenda encontrada — " + nombreAlumno;
		String mensaje = "Se encontró la " + tipoPrenda.toLowerCase() + " de su pupilo(a) " + nombreAlumno
				+ (nombreCurso != null ? " (" + nombreCurso + ")" : "")
				+ ". Puede retirarla en el colegio (" + ubicacion + ").";

		Notificacion notificacion = notificacionService.crear(new NotificacionRequest(
				evento.getIdEvento(),
				apoderado.getIdApoderado(),
				titulo,
				mensaje,
				EstadoNotificacion.ENVIADA,
				false
		));

		return new OperacionPrendaResponse(
				TipoEvento.PRENDA_ENCONTRADA,
				"ENCONTRADA",
				evento.getIdEvento(),
				notificacion.getIdNotificacion(),
				tarjeta.getIdTarjeta(),
				tarjeta.getUidNfc(),
				alumno.getIdAlumno(),
				nombreAlumno,
				nombreCurso,
				apoderado.getIdApoderado(),
				apoderado.getNombres() + " " + apoderado.getApellidos(),
				tipoPrenda,
				"Notificación enviada al apoderado",
				evento.getFechaEvento()
		);
	}

	private OperacionPrendaResponse registrarEntrega(
			Integer idColegio,
			Integer idUsuario,
			ScanPrendaRequest req,
			Tarjeta tarjeta,
			Alumno alumno,
			Apoderado apoderado,
			String nombreAlumno,
			String nombreCurso,
			String tipoPrenda) {
		String descripcion = req.descripcion() != null && !req.descripcion().isBlank()
				? req.descripcion().trim()
				: tipoPrenda + " de " + nombreAlumno + " entregada al apoderado";

		Evento evento = eventoService.crear(new EventoRequest(
				tarjeta.getIdTarjeta(),
				TipoEvento.PRENDA_RECUPERADA,
				descripcion,
				req.ubicacion(),
				idUsuario
		));

		String titulo = "Prenda entregada — " + nombreAlumno;
		String mensaje = "Se registró la entrega de la " + tipoPrenda.toLowerCase() + " de su pupilo(a) " + nombreAlumno
				+ (nombreCurso != null ? " (" + nombreCurso + ")" : "")
				+ " en el colegio.";

		Notificacion notificacion = notificacionService.crear(new NotificacionRequest(
				evento.getIdEvento(),
				apoderado.getIdApoderado(),
				titulo,
				mensaje,
				EstadoNotificacion.ENVIADA,
				false
		));

		return new OperacionPrendaResponse(
				TipoEvento.PRENDA_RECUPERADA,
				"ENTREGADA",
				evento.getIdEvento(),
				notificacion.getIdNotificacion(),
				tarjeta.getIdTarjeta(),
				tarjeta.getUidNfc(),
				alumno.getIdAlumno(),
				nombreAlumno,
				nombreCurso,
				apoderado.getIdApoderado(),
				apoderado.getNombres() + " " + apoderado.getApellidos(),
				tipoPrenda,
				"Notificación enviada al apoderado",
				evento.getFechaEvento()
		);
	}

	@Transactional(readOnly = true)
	public ColegioDashboardResponse dashboard(Integer idColegio) {
		Colegio colegio = colegioRepository.findById(idColegio)
				.orElseThrow(() -> new ResourceNotFoundException("Colegio no encontrado: " + idColegio));

		LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
		long encontradasHoy = eventoRepository.countByColegioAndTipoDesde(
				idColegio, TipoEvento.PRENDA_ENCONTRADA, inicioHoy);
		long entregadasHoy = eventoRepository.countByColegioAndTipoDesde(
				idColegio, TipoEvento.PRENDA_RECUPERADA, inicioHoy);

		List<Evento> recientes = eventoRepository.findByColegioId(idColegio, PageRequest.of(0, 20));
		List<ActividadRecienteResponse> acciones = new ArrayList<>();
		for (Evento e : recientes) {
			acciones.add(toActividad(e));
		}

		long notificaciones = notificacionRepository.findByColegioId(idColegio).stream()
				.filter(n -> n.getEstado() == EstadoNotificacion.ENVIADA)
				.count();

		return new ColegioDashboardResponse(
				idColegio,
				colegio.getNombre(),
				tarjetaRepository.countByColegioAndEstado(idColegio, EstadoTarjeta.ACTIVA),
				tarjetaRepository.countAlumnosConTarjeta(idColegio),
				encontradasHoy,
				eventoRepository.countByColegioAndTipo(idColegio, TipoEvento.PRENDA_ENCONTRADA),
				entregadasHoy,
				eventoRepository.countByColegioAndTipo(idColegio, TipoEvento.PRENDA_RECUPERADA),
				notificaciones,
				acciones
		);
	}

	private ActividadRecienteResponse toActividad(Evento e) {
		Tarjeta tarjeta = tarjetaRepository.findById(e.getIdTarjeta()).orElse(null);
		Alumno alumno = tarjeta != null
				? alumnoRepository.findById(tarjeta.getIdAlumno()).orElse(null)
				: null;
		Curso curso = alumno != null
				? cursoRepository.findById(alumno.getIdCurso()).orElse(null)
				: null;
		String accion = switch (e.getTipoEvento()) {
			case PRENDA_ENCONTRADA -> "Encontrada";
			case PRENDA_RECUPERADA -> "Entregada";
			case NOTIFICACION_ENVIADA -> "Notificación";
			case TARJETA_DESACTIVADA -> "Tarjeta desactivada";
		};
		return new ActividadRecienteResponse(
				e.getIdEvento(),
				e.getTipoEvento(),
				accion,
				e.getFechaEvento(),
				alumno != null ? alumno.getNombres() + " " + alumno.getApellidos() : null,
				curso != null ? curso.getNombre() : null,
				tarjeta != null ? tarjeta.getUidNfc() : null,
				tarjeta != null ? tarjeta.getTipoPrenda() : null,
				e.getDescripcion()
		);
	}

	private void asegurarColegio(Integer idColegio) {
		if (!colegioRepository.existsById(idColegio)) {
			throw new ResourceNotFoundException("Colegio no encontrado: " + idColegio);
		}
	}
}
