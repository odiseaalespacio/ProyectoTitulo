package com.cloty.service;



import com.cloty.domain.Administrador;

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
	public void enviarInstructivoActivacionColegio(Colegio colegio) {
		String destino = colegio.getEmail();
		if (destino == null || destino.isBlank()) {
			log.debug("Sin correo instructivo para colegio id={}", colegio.getIdColegio());
			return;
		}
		String cuerpo = """
				Hola,

				El colegio "%s" fue registrado en Cloty.

				Para activar su cuenta:
				1. Abra la app Cloty Colegio.
				2. Elija "Activar cuenta".
				3. Ingrese su RUT (%s).
				4. Recibirá un código de 6 dígitos en este correo.
				5. Ingrese el código y cree su contraseña.

				Saludos,
				Equipo Cloty
				""".formatted(colegio.getNombre(), colegio.getRut());
		enviar(destino, "Cloty — Active su cuenta de colegio", cuerpo);
	}

	@Async
	public void enviarInstructivoActivacionApoderado(Apoderado apoderado, Colegio colegio) {
		String destino = apoderado.getEmail();
		if (destino == null || destino.isBlank()) {
			log.debug("Sin correo instructivo para apoderado id={}", apoderado.getIdApoderado());
			return;
		}
		String cuerpo = """
				Hola %s %s,

				El colegio "%s" lo registró en Cloty.

				Para activar su cuenta:
				1. Abra la app Cloty Apoderado.
				2. Elija "Activar cuenta".
				3. Ingrese su RUT (%s).
				4. Recibirá un código de 6 dígitos en este correo.
				5. Ingrese el código y cree su contraseña.

				Saludos,
				Equipo Cloty
				""".formatted(
				apoderado.getNombres(),
				apoderado.getApellidos(),
				colegio.getNombre(),
				apoderado.getRut());
		enviar(destino, "Cloty — Active su cuenta de apoderado", cuerpo);
	}

	@Async

	public void enviarCodigoActivacionApoderado(Apoderado apoderado, Colegio colegio, String codigo, int minutosValidez) {

		String destino = apoderado.getEmail();

		if (destino == null || destino.isBlank()) {

			log.debug("Sin correo para código de activación del apoderado id={}", apoderado.getIdApoderado());

			return;

		}

		String cuerpo = """

				Hola %s %s,



				El colegio "%s" lo registró en Cloty.



				Su código de activación es: %s



				Abra la app Cloty Apoderado, elija "Activar cuenta" e ingrese su RUT (%s), este código y la contraseña que desee.



				El código vence en %d minutos.



				Saludos,

				Equipo Cloty

				""".formatted(

				apoderado.getNombres(),

				apoderado.getApellidos(),

				colegio.getNombre(),

				codigo,

				apoderado.getRut(),

				minutosValidez);

		enviar(destino, "Cloty — Código de activación", cuerpo);

	}



	@Async

	public void enviarCodigoActivacionColegio(Colegio colegio, String codigo, int minutosValidez) {

		String destino = colegio.getEmail();

		if (destino == null || destino.isBlank()) {

			log.debug("Sin correo para código de activación del colegio id={}", colegio.getIdColegio());

			return;

		}

		String cuerpo = """

				Hola,



				El colegio "%s" fue registrado en Cloty.



				Su código de activación es: %s



				Abra la app Cloty Colegio, elija "Activar cuenta" e ingrese su RUT (%s), este código, teléfono y contraseña.



				El código vence en %d minutos.



				Saludos,

				Equipo Cloty

				""".formatted(colegio.getNombre(), codigo, colegio.getRut(), minutosValidez);

		enviar(destino, "Cloty — Código de activación", cuerpo);

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

	public void enviarBienvenidaAdministrador(Administrador administrador, String username, String password) {

		String destino = administrador.getEmail();

		if (destino == null || destino.isBlank()) {

			return;

		}

		String cuerpo = """

				Hola %s %s,



				Se creó su cuenta de administrador en Cloty.



				Usuario: %s

				RUT: %s

				Contraseña: %s



				Inicie sesión en la app Cloty Administrador con su usuario o RUT y la contraseña indicada.



				Saludos,

				Equipo Cloty

				""".formatted(

				administrador.getNombres(),

				administrador.getApellidos(),

				username,

				administrador.getRut(),

				password != null ? password : "(definida al crear la cuenta)");

		enviar(destino, "Cloty — Cuenta de administrador creada", cuerpo);

	}



	@Async

	public void enviarBienvenidaSuperUsuario(String email, String username, String rut, String password) {

		if (email == null || email.isBlank()) {

			return;

		}

		String cuerpo = """

				Hola,



				Se creó su cuenta de super usuario en Cloty.



				Usuario: %s

				RUT: %s

				Contraseña: %s



				Inicie sesión en la app Cloty Administrador con su usuario o RUT y la contraseña indicada.



				Saludos,

				Equipo Cloty

				""".formatted(username, rut, password);

		enviar(email, "Cloty — Cuenta de super usuario creada", cuerpo);

	}



	@Async
	public void enviarCodigoRecuperacionContrasena(String email, String codigo, int minutosValidez) {
		if (email == null || email.isBlank()) {
			return;
		}
		String cuerpo = """
				Hola,

				Recibimos una solicitud para restablecer su contraseña en Cloty.

				Su código de recuperación es: %s

				Ingrese este código en la app junto con su RUT y la nueva contraseña.

				El código vence en %d minutos. Si no solicitó este cambio, ignore este correo.

				Saludos,
				Equipo Cloty
				""".formatted(codigo, minutosValidez);
		enviar(email, "Cloty — Recuperar contraseña", cuerpo);
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

