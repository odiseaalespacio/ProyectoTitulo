package com.cloty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@Profile("!test")
public class MailConfig {

	@Bean
	JavaMailSender javaMailSender(
			@Value("${spring.mail.host:localhost}") String host,
			@Value("${spring.mail.port:587}") int port,
			@Value("${spring.mail.username:}") String username,
			@Value("${spring.mail.password:}") String password) {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(host);
		sender.setPort(port);
		if (username != null && !username.isBlank()) {
			sender.setUsername(username.trim());
			sender.setPassword(normalizarPassword(password));
		}
		Properties props = sender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.connectiontimeout", "15000");
		props.put("mail.smtp.timeout", "15000");
		props.put("mail.smtp.writetimeout", "15000");
		props.put("mail.smtp.ssl.trust", host);
		if (port == 465) {
			props.put("mail.smtp.ssl.enable", "true");
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		} else {
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.starttls.required", "true");
		}
		return sender;
	}

	static String normalizarPassword(String password) {
		if (password == null) {
			return "";
		}
		String limpio = password.trim();
		if (limpio.length() >= 2 && limpio.startsWith("\"") && limpio.endsWith("\"")) {
			limpio = limpio.substring(1, limpio.length() - 1);
		}
		return limpio.replace(" ", "");
	}
}
