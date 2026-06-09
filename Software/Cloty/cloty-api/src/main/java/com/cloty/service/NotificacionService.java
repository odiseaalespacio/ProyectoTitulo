package com.cloty.service;

import com.cloty.domain.EstadoNotificacion;
import com.cloty.domain.Notificacion;
import com.cloty.dto.NotificacionRequest;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.EventoRepository;
import com.cloty.repo.NotificacionRepository;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

	private final NotificacionRepository notificacionRepository;
	private final EventoRepository eventoRepository;
	private final ApoderadoRepository apoderadoRepository;
	private final ColegioRepository colegioRepository;
	// esto es nuevo
	private final EmailService emailService;

	@Transactional(readOnly = true)
	public List<Notificacion> listarPorColegio(Integer idColegio) {
		if (!colegioRepository.existsById(idColegio)) {
			throw new ResourceNotFoundException("Colegio no encontrado: " + idColegio);
		}
		return notificacionRepository.findByColegioId(idColegio);
	}

	@Transactional(readOnly = true)
	public List<Notificacion> listarPorApoderado(Integer idApoderado) {
		if (!apoderadoRepository.existsById(idApoderado)) {
			throw new ResourceNotFoundException("Apoderado no encontrado: " + idApoderado);
		}
		return notificacionRepository.findByIdApoderadoOrderByFechaEnvioDesc(idApoderado);
	}

	@Transactional(readOnly = true)
	public List<Notificacion> listarPorEvento(Integer idEvento) {
		if (!eventoRepository.existsById(idEvento)) {
			throw new ResourceNotFoundException("Evento no encontrado: " + idEvento);
		}
		return notificacionRepository.findByIdEventoOrderByFechaEnvioDesc(idEvento);
	}

	@Transactional(readOnly = true)
	public Notificacion obtener(Integer id) {
		return notificacionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada: " + id));
	}

	@Transactional
	public Notificacion crear(NotificacionRequest req) {
		if (!eventoRepository.existsById(req.idEvento())) {
			throw new ResourceNotFoundException("Evento no encontrado: " + req.idEvento());
		}
		if (!apoderadoRepository.existsById(req.idApoderado())) {
			throw new ResourceNotFoundException("Apoderado no encontrado: " + req.idApoderado());
		}
		Notificacion n = Notificacion.builder()
				.idEvento(req.idEvento())
				.idApoderado(req.idApoderado())
				.titulo(req.titulo())
				.mensaje(req.mensaje())
				.estado(req.estado() != null ? req.estado() : EstadoNotificacion.PENDIENTE)
				.leida(req.leida() != null ? req.leida() : Boolean.FALSE)
				.build();
		n = notificacionRepository.save(n);
		// esto es nuevo
		apoderadoRepository.findById(req.idApoderado()).ifPresent(apoderado ->
				emailService.enviarNotificacionApoderado(apoderado, req.titulo(), req.mensaje()));
		return n;
	}

	@Transactional
	public Notificacion actualizar(Integer id, NotificacionRequest req) {
		Notificacion n = obtener(id);
		if (!eventoRepository.existsById(req.idEvento())) {
			throw new ResourceNotFoundException("Evento no encontrado: " + req.idEvento());
		}
		if (!apoderadoRepository.existsById(req.idApoderado())) {
			throw new ResourceNotFoundException("Apoderado no encontrado: " + req.idApoderado());
		}
		n.setIdEvento(req.idEvento());
		n.setIdApoderado(req.idApoderado());
		n.setTitulo(req.titulo());
		n.setMensaje(req.mensaje());
		if (req.estado() != null) {
			n.setEstado(req.estado());
		}
		if (req.leida() != null) {
			n.setLeida(req.leida());
		}
		return notificacionRepository.save(n);
	}

	@Transactional
	public Notificacion marcarLeida(Integer id) {
		Notificacion n = obtener(id);
		n.setLeida(true);
		return notificacionRepository.save(n);
	}

	@Transactional
	public void eliminar(Integer id) {
		if (!notificacionRepository.existsById(id)) {
			throw new ResourceNotFoundException("Notificación no encontrada: " + id);
		}
		notificacionRepository.deleteById(id);
	}
}
