package com.cloty.service;

import com.cloty.domain.Apoderado;
import com.cloty.domain.Colegio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// esto es nuevo
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${cloty.mail.from}")
	private String from;

	@Value("${cloty.mail.enabled:false}")
	private boolean enabled;

	@Async
	public void enviarCargaApoderadoEnColegio(Apoderado apoderado, Colegio colegio) {
		if (apoderado.getIdUsuario() != null) {
			return;
		}
		String destino = apoderado.getEmail();
		if (destino == null || destino.isBlank()) {
			log.debug("Sin correo para aviso de carga del apoderado id={}", apoderado.getIdApoderado());
			return;
		}
		String cuerpo = """
				Hola %s %s,

				El colegio "%s" lo registró en el sistema Cloty.

				Para activar su cuenta, abra la app Cloty Apoderado y use la opción "Activar cuenta" con su RUT: %s

				Saludos,
				Equipo Cloty
				""".formatted(
				apoderado.getNombres(),
				apoderado.getApellidos(),
				colegio.getNombre(),
				apoderado.getRut());
		enviar(destino, "Cloty — Registro en el sistema", cuerpo);
	}

	@Async
	public void enviarActivacionApoderado(Apoderado apoderado, String username) {
		String destino = apoderado.getEmail();
		if (destino == null || destino.isBlank()) {
			return;
		}
		String cuerpo = """
				Hola %s %s,

				Su cuenta en Cloty fue activada correctamente.

				Usuario: %s
				Puede iniciar sesión en la app Cloty Apoderado con su RUT (%s) o su usuario.

				Saludos,
				Equipo Cloty
				""".formatted(
				apoderado.getNombres(),
				apoderado.getApellidos(),
				username,
				apoderado.getRut());
		enviar(destino, "Cloty — Cuenta activada", cuerpo);
	}

	@Async
	public void enviarActivacionColegio(Colegio colegio, String username) {
		String destino = colegio.getEmail();
		if (destino == null || destino.isBlank()) {
			return;
		}
		String cuerpo = """
				Hola,

				La cuenta del colegio "%s" fue activada en Cloty.

				Usuario: %s
				RUT: %s

				Ya puede iniciar sesión en la app Cloty Colegio.

				Saludos,
				Equipo Cloty
				""".formatted(colegio.getNombre(), username, colegio.getRut());
		enviar(destino, "Cloty — Cuenta de colegio activada", cuerpo);
	}

	@Async
	public void enviarNotificacionApoderado(Apoderado apoderado, String titulo, String mensaje) {
		String destino = apoderado.getEmail();
		if (destino == null || destino.isBlank()) {
			log.debug("Sin correo para notificación al apoderado id={}", apoderado.getIdApoderado());
			return;
		}
		String cuerpo = """
				Hola %s %s,

				%s

				%s

				Revise también la app Cloty Apoderado para más detalles.

				Saludos,
				Equipo Cloty
				""".formatted(
				apoderado.getNombres(),
				apoderado.getApellidos(),
				titulo,
				mensaje);
		enviar(destino, "Cloty — " + titulo, cuerpo);
	}

	private void enviar(String destino, String asunto, String cuerpo) {
		if (!enabled) {
			log.debug("Correo deshabilitado (cloty.mail.enabled=false), no se envía a {}", destino);
			return;
		}
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setFrom(from);
			msg.setTo(destino.trim());
			msg.setSubject(asunto);
			msg.setText(cuerpo);
			mailSender.send(msg);
			log.info("Correo enviado a {}", destino);
		} catch (Exception e) {
			log.error("Error al enviar correo a {}: {}", destino, e.getMessage());
		}
	}
}
