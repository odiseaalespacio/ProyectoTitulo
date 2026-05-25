package com.cloty.service;

import com.cloty.domain.Evento;
import com.cloty.dto.EventoRequest;
import com.cloty.repo.EventoRepository;
import com.cloty.repo.TarjetaRepository;
import com.cloty.repo.UsuarioRepository;
import com.cloty.web.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

	private final EventoRepository eventoRepository;
	private final TarjetaRepository tarjetaRepository;
	private final UsuarioRepository usuarioRepository;

	@Transactional(readOnly = true)
	public List<Evento> listarPorTarjeta(Integer idTarjeta) {
		if (!tarjetaRepository.existsById(idTarjeta)) {
			throw new ResourceNotFoundException("Tarjeta no encontrada: " + idTarjeta);
		}
		return eventoRepository.findByIdTarjetaOrderByIdEventoDesc(idTarjeta);
	}

	@Transactional(readOnly = true)
	public Evento obtener(Integer id) {
		return eventoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + id));
	}

	@Transactional
	public Evento crear(EventoRequest req) {
		if (!tarjetaRepository.existsById(req.idTarjeta())) {
			throw new ResourceNotFoundException("Tarjeta no encontrada: " + req.idTarjeta());
		}
		if (req.registradoPor() != null && !usuarioRepository.existsById(req.registradoPor())) {
			throw new ResourceNotFoundException("Usuario no encontrado: " + req.registradoPor());
		}
		Evento e = Evento.builder()
				.idTarjeta(req.idTarjeta())
				.tipoEvento(req.tipoEvento())
				.descripcion(req.descripcion())
				.ubicacion(req.ubicacion())
				.registradoPor(req.registradoPor())
				.build();
		return eventoRepository.save(e);
	}

	@Transactional
	public Evento actualizar(Integer id, EventoRequest req) {
		Evento e = obtener(id);
		if (!tarjetaRepository.existsById(req.idTarjeta())) {
			throw new ResourceNotFoundException("Tarjeta no encontrada: " + req.idTarjeta());
		}
		if (req.registradoPor() != null && !usuarioRepository.existsById(req.registradoPor())) {
			throw new ResourceNotFoundException("Usuario no encontrado: " + req.registradoPor());
		}
		e.setIdTarjeta(req.idTarjeta());
		e.setTipoEvento(req.tipoEvento());
		e.setDescripcion(req.descripcion());
		e.setUbicacion(req.ubicacion());
		e.setRegistradoPor(req.registradoPor());
		return eventoRepository.save(e);
	}

	@Transactional
	public void eliminar(Integer id) {
		if (!eventoRepository.existsById(id)) {
			throw new ResourceNotFoundException("Evento no encontrado: " + id);
		}
		eventoRepository.deleteById(id);
	}
}
