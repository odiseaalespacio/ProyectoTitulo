package com.cloty.service;

import com.cloty.dto.EstadoCorreoResponse;
import com.cloty.dto.ProbarCorreoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailDiagnosticService {

	private final JavaMailSender mailSender;

	@Value("${cloty.mail.enabled:false}")
	private boolean enabled;

	@Value("${cloty.mail.from:}")
	private String from;

	@Value("${spring.mail.host:}")
	private String smtpHost;

	@Value("${spring.mail.port:587}")
	private int smtpPort;

	@Value("${spring.mail.username:}")
	private String smtpUser;

	@Value("${spring.mail.password:}")
	private String smtpPassword;

	public EstadoCorreoResponse estado() {
		boolean passwordOk = smtpPassword != null && !smtpPassword.isBlank();
		boolean userOk = smtpUser != null && !smtpUser.isBlank();
		boolean fromOk = from != null && !from.isBlank();
		boolean fromMatch = fromOk && userOk && from.trim().equalsIgnoreCase(smtpUser.trim());
		String advertencia = null;
		if (!enabled) {
			advertencia = "Correo deshabilitado (cloty.mail.enabled=false).";
		} else if (!userOk || !passwordOk) {
			advertencia = "Faltan SPRING_MAIL_USERNAME o SPRING_MAIL_PASSWORD en el servidor.";
		} else if (!fromMatch) {
			advertencia = "CLOTY_MAIL_FROM debe ser el mismo Gmail que SPRING_MAIL_USERNAME.";
		}
		return new EstadoCorreoResponse(enabled, from, smtpHost, smtpPort, smtpUser, passwordOk, fromMatch, advertencia);
	}

	public ProbarCorreoResponse probarEnvio(String destino) {
		EstadoCorreoResponse estado = estado();
		if (!estado.habilitado()) {
			return new ProbarCorreoResponse(false, "Correo deshabilitado en el servidor.");
		}
		if (estado.advertencia() != null) {
			return new ProbarCorreoResponse(false, estado.advertencia());
		}
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setFrom(from.trim());
			msg.setTo(destino.trim());
			msg.setSubject("Cloty — Prueba de correo");
			msg.setText("""
					Hola,

					Este es un correo de prueba enviado desde la API Cloty.

					Si lo recibió, el envío SMTP está configurado correctamente.

					Saludos,
					Equipo Cloty
					""");
			mailSender.send(msg);
			log.info("Correo de prueba enviado a {}", destino);
			return new ProbarCorreoResponse(true, "Correo de prueba enviado a " + destino + ". Revise bandeja y spam.");
		} catch (Exception e) {
			log.error("Fallo correo de prueba a {}: {}", destino, e.getMessage(), e);
			return new ProbarCorreoResponse(false, "Error SMTP: " + e.getMessage());
		}
	}
}
