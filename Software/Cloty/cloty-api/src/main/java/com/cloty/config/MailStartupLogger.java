package com.cloty.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailStartupLogger {

	@Value("${cloty.mail.enabled:false}")
	private boolean mailEnabled;

	@Value("${cloty.mail.from:}")
	private String mailFrom;

	@Value("${spring.mail.host:}")
	private String smtpHost;

	@Value("${spring.mail.username:}")
	private String smtpUser;

	@Value("${spring.mail.password:}")
	private String smtpPassword;

	@Value("${spring.mail.port:587}")
	private int smtpPort;

	@PostConstruct
	void logEstadoCorreo() {
		if (!mailEnabled) {
			log.warn("Correo Cloty DESACTIVADO (cloty.mail.enabled=false).");
			return;
		}
		log.info("Correo Cloty ACTIVO: from={}, smtp={}:{} user={}", mailFrom, smtpHost, smtpPort, smtpUser);
		if (smtpUser == null || smtpUser.isBlank()) {
			log.error("Correo activo pero SPRING_MAIL_USERNAME está vacío. No se enviarán correos.");
		}
		if (smtpPassword == null || smtpPassword.isBlank()) {
			log.error("Correo activo pero SPRING_MAIL_PASSWORD está vacío. No se enviarán correos.");
		}
		if (mailFrom != null && smtpUser != null
				&& !mailFrom.isBlank() && !smtpUser.isBlank()
				&& !mailFrom.trim().equalsIgnoreCase(smtpUser.trim())) {
			log.error("CLOTY_MAIL_FROM ({}) debe coincidir con SPRING_MAIL_USERNAME ({}) para Gmail.", mailFrom, smtpUser);
		}
	}
}
