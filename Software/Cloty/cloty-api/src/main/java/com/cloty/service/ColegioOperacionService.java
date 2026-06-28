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
import com.cloty.dto.DashboardAlumnoItem;
import com.cloty.dto.DashboardApoderadoItem;
import com.cloty.dto.DashboardComunidadDetalleResponse;
import com.cloty.dto.DashboardCursoDetalleResponse;
import com.cloty.dto.DashboardNotificacionItem;
import com.cloty.dto.DashboardNotificacionesDetalleResponse;
import com.cloty.dto.DashboardPrendasDetalleResponse;
import com.cloty.dto.DashboardTarjetaItem;
import com.cloty.dto.DashboardTarjetasDetalleResponse;
import com.cloty.dto.EventoRequest;
import com.cloty.dto.NotificacionRequest;
import com.cloty.dto.OperacionPrendaResponse;
import com.cloty.dto.ResumenCursoDashboard;
import com.cloty.dto.ScanPrendaRequest;
import com.cloty.repo.AlumnoRepository;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioApoderadoRepository;
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
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColegioOperacionService {

	private final TarjetaRepository tarjetaRepository;
	private final AlumnoRepository alumnoRepository;
	private final ApoderadoRepository apoderadoRepository;
	private final CursoRepository cursoRepository;
	private final ColegioRepository colegioRepository;
	private final ColegioApoderadoRepository colegioApoderadoRepository;
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

		List<Alumno> alumnos = alumnoRepository.findByIdColegioOrderByApellidosAscNombresAsc(idColegio);
		long totalAlumnos = alumnos.size();
		long alumnosConTarjeta = tarjetaRepository.countAlumnosConTarjeta(idColegio);
		long alumnosSinTarjeta = Math.max(0, totalAlumnos - alumnosConTarjeta);

		List<Integer> idsApoderados = colegioApoderadoRepository.findByIdColegio(idColegio).stream()
				.map(ca -> ca.getIdApoderado())
				.distinct()
				.toList();
		long totalApoderados = idsApoderados.size();
		long apoderadosConCuenta = idsApoderados.isEmpty() ? 0
				: apoderadoRepository.findAllById(idsApoderados).stream()
						.filter(a -> a.getIdUsuario() != null)
						.count();

		List<Curso> cursos = cursoRepository.findByIdColegioOrderByNombreAsc(idColegio);
		List<ResumenCursoDashboard> resumenCursos = cursos.stream()
				.map(curso -> resumenCurso(curso))
				.toList();

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

		List<Notificacion> notificacionesColegio = notificacionRepository.findByColegioId(idColegio);
		long notificacionesEnviadas = notificacionesColegio.stream()
				.filter(n -> n.getEstado() == EstadoNotificacion.ENVIADA)
				.count();
		long notificacionesPendientes = notificacionesColegio.stream()
				.filter(n -> n.getEstado() == EstadoNotificacion.PENDIENTE)
				.count();

		return new ColegioDashboardResponse(
				idColegio,
				colegio.getNombre(),
				totalAlumnos,
				totalApoderados,
				cursos.size(),
				apoderadosConCuenta,
				alumnosConTarjeta,
				alumnosSinTarjeta,
				tarjetaRepository.countByColegioAndEstado(idColegio, EstadoTarjeta.ACTIVA),
				tarjetaRepository.countByColegioAndEstado(idColegio, EstadoTarjeta.PERDIDA),
				tarjetaRepository.countByColegioAndEstado(idColegio, EstadoTarjeta.DESACTIVADA),
				encontradasHoy,
				eventoRepository.countByColegioAndTipo(idColegio, TipoEvento.PRENDA_ENCONTRADA),
				entregadasHoy,
				eventoRepository.countByColegioAndTipo(idColegio, TipoEvento.PRENDA_RECUPERADA),
				notificacionesEnviadas,
				notificacionesPendientes,
				resumenCursos,
				acciones
		);
	}

	private ResumenCursoDashboard resumenCurso(Curso curso) {
		List<Alumno> alumnosCurso = alumnoRepository.findByIdCursoOrderByApellidosAscNombresAsc(curso.getIdCurso());
		long conTarjeta = alumnosCurso.stream()
				.filter(a -> !tarjetaRepository.findByIdAlumnoOrderByFechaAsignacionDesc(a.getIdAlumno()).isEmpty())
				.count();
		long total = alumnosCurso.size();
		return new ResumenCursoDashboard(
				curso.getIdCurso(),
				curso.getNombre(),
				curso.getNivel(),
				total,
				conTarjeta,
				Math.max(0, total - conTarjeta)
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

	@Transactional(readOnly = true)
	public DashboardComunidadDetalleResponse dashboardComunidad(Integer idColegio) {
		asegurarColegio(idColegio);
		List<Alumno> alumnos = alumnoRepository.findByIdColegioOrderByApellidosAscNombresAsc(idColegio);
		Map<Integer, Curso> cursosPorId = cursoRepository.findByIdColegioOrderByNombreAsc(idColegio).stream()
				.collect(Collectors.toMap(Curso::getIdCurso, c -> c));
		Map<Integer, Apoderado> apoderadosPorId = apoderadosDelColegio(idColegio).stream()
				.collect(Collectors.toMap(Apoderado::getIdApoderado, a -> a));

		List<DashboardAlumnoItem> alumnosLista = new ArrayList<>();
		long conTarjeta = 0;
		for (Alumno alumno : alumnos) {
			boolean tieneTarjeta = !tarjetaRepository.findByIdAlumnoOrderByFechaAsignacionDesc(alumno.getIdAlumno()).isEmpty();
			if (tieneTarjeta) {
				conTarjeta++;
			}
			alumnosLista.add(toAlumnoItem(alumno, cursosPorId, apoderadosPorId, tieneTarjeta));
		}

		List<Apoderado> apoderados = apoderadosDelColegio(idColegio);
		List<DashboardApoderadoItem> apoderadosLista = apoderados.stream()
				.map(this::toApoderadoItem)
				.toList();

		return new DashboardComunidadDetalleResponse(
				alumnos.size(),
				apoderados.size(),
				cursosPorId.size(),
				apoderados.stream().filter(a -> a.getIdUsuario() != null).count(),
				conTarjeta,
				Math.max(0, alumnos.size() - conTarjeta),
				alumnosLista,
				apoderadosLista
		);
	}

	@Transactional(readOnly = true)
	public DashboardTarjetasDetalleResponse dashboardTarjetas(Integer idColegio) {
		asegurarColegio(idColegio);
		Map<Integer, Alumno> alumnosPorId = alumnoRepository.findByIdColegioOrderByApellidosAscNombresAsc(idColegio).stream()
				.collect(Collectors.toMap(Alumno::getIdAlumno, a -> a));
		Map<Integer, Curso> cursosPorId = cursoRepository.findByIdColegioOrderByNombreAsc(idColegio).stream()
				.collect(Collectors.toMap(Curso::getIdCurso, c -> c));

		List<DashboardTarjetaItem> items = tarjetaRepository.findByColegioId(idColegio).stream()
				.map(t -> toTarjetaItem(t, alumnosPorId, cursosPorId))
				.toList();

		return new DashboardTarjetasDetalleResponse(
				tarjetaRepository.countByColegioAndEstado(idColegio, EstadoTarjeta.ACTIVA),
				tarjetaRepository.countByColegioAndEstado(idColegio, EstadoTarjeta.PERDIDA),
				tarjetaRepository.countByColegioAndEstado(idColegio, EstadoTarjeta.DESACTIVADA),
				items
		);
	}

	@Transactional(readOnly = true)
	public DashboardPrendasDetalleResponse dashboardPrendas(Integer idColegio) {
		asegurarColegio(idColegio);
		LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
		List<Evento> recientes = eventoRepository.findByColegioId(idColegio, PageRequest.of(0, 50));
		List<ActividadRecienteResponse> actividad = recientes.stream()
				.filter(e -> e.getTipoEvento() == TipoEvento.PRENDA_ENCONTRADA
						|| e.getTipoEvento() == TipoEvento.PRENDA_RECUPERADA)
				.map(this::toActividad)
				.toList();

		return new DashboardPrendasDetalleResponse(
				eventoRepository.countByColegioAndTipoDesde(idColegio, TipoEvento.PRENDA_ENCONTRADA, inicioHoy),
				eventoRepository.countByColegioAndTipo(idColegio, TipoEvento.PRENDA_ENCONTRADA),
				eventoRepository.countByColegioAndTipoDesde(idColegio, TipoEvento.PRENDA_RECUPERADA, inicioHoy),
				eventoRepository.countByColegioAndTipo(idColegio, TipoEvento.PRENDA_RECUPERADA),
				actividad
		);
	}

	@Transactional(readOnly = true)
	public DashboardNotificacionesDetalleResponse dashboardNotificaciones(Integer idColegio) {
		asegurarColegio(idColegio);
		List<Notificacion> notificaciones = notificacionRepository.findByColegioId(idColegio);
		Map<Integer, Apoderado> apoderadosPorId = apoderadosDelColegio(idColegio).stream()
				.collect(Collectors.toMap(Apoderado::getIdApoderado, a -> a));

		List<DashboardNotificacionItem> recientes = notificaciones.stream()
				.limit(50)
				.map(n -> toNotificacionItem(n, apoderadosPorId))
				.toList();

		return new DashboardNotificacionesDetalleResponse(
				notificaciones.stream().filter(n -> n.getEstado() == EstadoNotificacion.ENVIADA).count(),
				notificaciones.stream().filter(n -> n.getEstado() == EstadoNotificacion.PENDIENTE).count(),
				recientes
		);
	}

	@Transactional(readOnly = true)
	public List<DashboardCursoDetalleResponse> dashboardCursos(Integer idColegio) {
		asegurarColegio(idColegio);
		Map<Integer, Apoderado> apoderadosPorId = apoderadosDelColegio(idColegio).stream()
				.collect(Collectors.toMap(Apoderado::getIdApoderado, a -> a));

		return cursoRepository.findByIdColegioOrderByNombreAsc(idColegio).stream()
				.map(curso -> detalleCurso(curso, apoderadosPorId))
				.toList();
	}

	@Transactional(readOnly = true)
	public DashboardCursoDetalleResponse dashboardCurso(Integer idColegio, Integer idCurso) {
		asegurarColegio(idColegio);
		Curso curso = cursoRepository.findById(idCurso)
				.orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + idCurso));
		if (!curso.getIdColegio().equals(idColegio)) {
			throw new BadRequestException("El curso no pertenece a su colegio");
		}
		Map<Integer, Apoderado> apoderadosPorId = apoderadosDelColegio(idColegio).stream()
				.collect(Collectors.toMap(Apoderado::getIdApoderado, a -> a));
		return detalleCurso(curso, apoderadosPorId);
	}

	@Transactional(readOnly = true)
	public List<ActividadRecienteResponse> dashboardActividad(Integer idColegio) {
		asegurarColegio(idColegio);
		return eventoRepository.findByColegioId(idColegio, PageRequest.of(0, 50)).stream()
				.map(this::toActividad)
				.toList();
	}

	private DashboardCursoDetalleResponse detalleCurso(Curso curso, Map<Integer, Apoderado> apoderadosPorId) {
		List<Alumno> alumnosCurso = alumnoRepository.findByIdCursoOrderByApellidosAscNombresAsc(curso.getIdCurso());
		List<DashboardAlumnoItem> items = new ArrayList<>();
		long conTarjeta = 0;
		for (Alumno alumno : alumnosCurso) {
			boolean tieneTarjeta = !tarjetaRepository.findByIdAlumnoOrderByFechaAsignacionDesc(alumno.getIdAlumno()).isEmpty();
			if (tieneTarjeta) {
				conTarjeta++;
			}
			items.add(toAlumnoItem(alumno, Map.of(curso.getIdCurso(), curso), apoderadosPorId, tieneTarjeta));
		}
		long total = alumnosCurso.size();
		return new DashboardCursoDetalleResponse(
				curso.getIdCurso(),
				curso.getNombre(),
				curso.getNivel(),
				total,
				conTarjeta,
				Math.max(0, total - conTarjeta),
				items
		);
	}

	private List<Apoderado> apoderadosDelColegio(Integer idColegio) {
		List<Integer> ids = colegioApoderadoRepository.findByIdColegio(idColegio).stream()
				.map(ca -> ca.getIdApoderado())
				.distinct()
				.toList();
		if (ids.isEmpty()) {
			return List.of();
		}
		return apoderadoRepository.findAllById(ids);
	}

	private DashboardAlumnoItem toAlumnoItem(
			Alumno alumno,
			Map<Integer, Curso> cursosPorId,
			Map<Integer, Apoderado> apoderadosPorId,
			boolean tieneTarjeta) {
		Curso curso = cursosPorId.get(alumno.getIdCurso());
		Apoderado apoderado = apoderadosPorId.get(alumno.getIdApoderado());
		String nombreApoderado = apoderado != null
				? apoderado.getNombres() + " " + apoderado.getApellidos()
				: null;
		return new DashboardAlumnoItem(
				alumno.getIdAlumno(),
				alumno.getRut(),
				alumno.getNombres(),
				alumno.getApellidos(),
				curso != null ? curso.getNombre() : null,
				tieneTarjeta,
				nombreApoderado
		);
	}

	private DashboardApoderadoItem toApoderadoItem(Apoderado apoderado) {
		return new DashboardApoderadoItem(
				apoderado.getIdApoderado(),
				apoderado.getRut(),
				apoderado.getNombres(),
				apoderado.getApellidos(),
				apoderado.getEmail(),
				apoderado.getIdUsuario() != null
		);
	}

	private DashboardTarjetaItem toTarjetaItem(
			Tarjeta tarjeta,
			Map<Integer, Alumno> alumnosPorId,
			Map<Integer, Curso> cursosPorId) {
		Alumno alumno = alumnosPorId.get(tarjeta.getIdAlumno());
		Curso curso = alumno != null ? cursosPorId.get(alumno.getIdCurso()) : null;
		String nombreAlumno = alumno != null ? alumno.getNombres() + " " + alumno.getApellidos() : null;
		return new DashboardTarjetaItem(
				tarjeta.getIdTarjeta(),
				tarjeta.getUidNfc(),
				tarjeta.getEstado(),
				tarjeta.getTipoPrenda(),
				nombreAlumno,
				curso != null ? curso.getNombre() : null
		);
	}

	private DashboardNotificacionItem toNotificacionItem(Notificacion n, Map<Integer, Apoderado> apoderadosPorId) {
		Apoderado apoderado = apoderadosPorId.get(n.getIdApoderado());
		String nombreApoderado = apoderado != null
				? apoderado.getNombres() + " " + apoderado.getApellidos()
				: null;
		return new DashboardNotificacionItem(
				n.getIdNotificacion(),
				n.getTitulo(),
				n.getMensaje(),
				n.getEstado(),
				nombreApoderado,
				n.getFechaEnvio()
		);
	}
}
