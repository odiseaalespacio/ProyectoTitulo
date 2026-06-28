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

	@PostConstruct
	void logEstadoCorreo() {
		if (mailEnabled) {
			log.info("Correo Cloty ACTIVO: from={}, smtp={} user={}", mailFrom, smtpHost, smtpUser);
		} else {
			log.warn("Correo Cloty DESACTIVADO (cloty.mail.enabled=false). Revise application-local.properties.");
		}
	}
}
