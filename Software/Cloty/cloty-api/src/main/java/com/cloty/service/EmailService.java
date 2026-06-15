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
				4. RecibirÃ¡ un cÃ³digo de 6 dÃ­gitos en este correo.
				5. Ingrese el cÃ³digo y cree su contraseÃ±a.

				Saludos,
				Equipo Cloty
				""".formatted(colegio.getNombre(), colegio.getRut());
		enviar(destino, "Cloty â€” Active su cuenta de colegio", cuerpo);
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

				El colegio "%s" lo registrÃ³ en Cloty.

				Para activar su cuenta:
				1. Abra la app Cloty Apoderado.
				2. Elija "Activar cuenta".
				3. Ingrese su RUT (%s).
				4. RecibirÃ¡ un cÃ³digo de 6 dÃ­gitos en este correo.
				5. Ingrese el cÃ³digo y cree su contraseÃ±a.

				Saludos,
				Equipo Cloty
				""".formatted(
				apoderado.getNombres(),
				apoderado.getApellidos(),
				colegio.getNombre(),
				apoderado.getRut());
		enviar(destino, "Cloty â€” Active su cuenta de apoderado", cuerpo);
	}

	@Async

	public void enviarCodigoActivacionApoderado(Apoderado apoderado, Colegio colegio, String codigo, int minutosValidez) {

		String destino = apoderado.getEmail();

		if (destino == null || destino.isBlank()) {

			log.debug("Sin correo para cÃ³digo de activaciÃ³n del apoderado id={}", apoderado.getIdApoderado());

			return;

		}

		String cuerpo = """

				Hola %s %s,


				El colegio "%s" lo registrÃ³ en Cloty.


				Su cÃ³digo de activaciÃ³n es: %s


				Abra la app Cloty Apoderado, elija "Activar cuenta" e ingrese su RUT (%s), este cÃ³digo y la contraseÃ±a que desee.


				El cÃ³digo vence en %d minutos.


				Saludos,

				Equipo Cloty

				""".formatted(

				apoderado.getNombres(),

				apoderado.getApellidos(),

				colegio.getNombre(),

				codigo,

				apoderado.getRut(),

				minutosValidez);

		enviar(destino, "Cloty â€” CÃ³digo de activaciÃ³n", cuerpo);

	}


	@Async

	public void enviarCodigoActivacionColegio(Colegio colegio, String codigo, int minutosValidez) {

		String destino = colegio.getEmail();

		if (destino == null || destino.isBlank()) {

			log.debug("Sin correo para cÃ³digo de activaciÃ³n del colegio id={}", colegio.getIdColegio());

			return;

		}

		String cuerpo = """

				Hola,


				El colegio "%s" fue registrado en Cloty.


				Su cÃ³digo de activaciÃ³n es: %s


				Abra la app Cloty Colegio, elija "Activar cuenta" e ingrese su RUT (%s), este cÃ³digo, telÃ©fono y contraseÃ±a.


				El cÃ³digo vence en %d minutos.


				Saludos,

				Equipo Cloty

				""".formatted(colegio.getNombre(), codigo, colegio.getRut(), minutosValidez);

		enviar(destino, "Cloty â€” CÃ³digo de activaciÃ³n", cuerpo);

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

				Puede iniciar sesiÃ³n en la app Cloty Apoderado con su RUT (%s) o su usuario.


				Saludos,

				Equipo Cloty

				""".formatted(

				apoderado.getNombres(),

				apoderado.getApellidos(),

				username,

				apoderado.getRut());

		enviar(destino, "Cloty â€” Cuenta activada", cuerpo);

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


				Ya puede iniciar sesiÃ³n en la app Cloty Colegio.


				Saludos,

				Equipo Cloty

				""".formatted(colegio.getNombre(), username, colegio.getRut());

		enviar(destino, "Cloty â€” Cuenta de colegio activada", cuerpo);

	}


	@Async

	public void enviarBienvenidaAdministrador(Administrador administrador, String username, String password) {

		String destino = administrador.getEmail();

		if (destino == null || destino.isBlank()) {

			return;

		}

		String cuerpo = """

				Hola %s %s,


				Se creÃ³ su cuenta de administrador en Cloty.


				Usuario: %s

				RUT: %s

				ContraseÃ±a: %s


				Inicie sesiÃ³n en la app Cloty Administrador con su usuario o RUT y la contraseÃ±a indicada.


				Saludos,

				Equipo Cloty

				""".formatted(

				administrador.getNombres(),

				administrador.getApellidos(),

				username,

				administrador.getRut(),

				password != null ? password : "(definida al crear la cuenta)");

		enviar(destino, "Cloty â€” Cuenta de administrador creada", cuerpo);

	}


	@Async
	public void enviarBienvenidaSuperUsuario(com.cloty.domain.SuperUsuario superUsuario, String username, String password) {
		String destino = superUsuario.getEmail();
		if (destino == null || destino.isBlank()) {
			return;
		}
		String cuerpo = """
				Hola %s %s,

				Se creÃ³ su cuenta de super usuario en Cloty.

				Usuario: %s
				RUT: %s
				ContraseÃ±a: %s

				Inicie sesiÃ³n en la app Cloty Administrador con su usuario o RUT y la contraseÃ±a indicada.

				Saludos,
				Equipo Cloty
				""".formatted(
				superUsuario.getNombres(),
				superUsuario.getApellidos(),
				username,
				superUsuario.getRut(),
				password != null ? password : "(definida al crear la cuenta)");
		enviar(destino, "Cloty â€” Cuenta de super usuario creada", cuerpo);
	}


	@Async
	public void enviarCodigoRecuperacionContrasena(String email, String codigo, int minutosValidez) {
		if (email == null || email.isBlank()) {
			return;
		}
		String cuerpo = """
				Hola,

				Recibimos una solicitud para restablecer su contraseÃ±a en Cloty.

				Su cÃ³digo de recuperaciÃ³n es: %s

				Ingrese este cÃ³digo en la app junto con su RUT y la nueva contraseÃ±a.

				El cÃ³digo vence en %d minutos. Si no solicitÃ³ este cambio, ignore este correo.

				Saludos,
				Equipo Cloty
				""".formatted(codigo, minutosValidez);
		enviar(email, "Cloty â€” Recuperar contraseÃ±a", cuerpo);
	}

	@Async

	public void enviarNotificacionApoderado(Apoderado apoderado, String titulo, String mensaje) {

		String destino = apoderado.getEmail();

		if (destino == null || destino.isBlank()) {

			log.debug("Sin correo para notificaciÃ³n al apoderado id={}", apoderado.getIdApoderado());

			return;

		}

		String cuerpo = """

				Hola %s %s,


				%s


				%s


				Revise tambiÃ©n la app Cloty Apoderado para mÃ¡s detalles.


				Saludos,

				Equipo Cloty

				""".formatted(

				apoderado.getNombres(),

				apoderado.getApellidos(),

				titulo,

				mensaje);

		enviar(destino, "Cloty â€” " + titulo, cuerpo);

	}


	private void enviar(String destino, String asunto, String cuerpo) {

		if (!enabled) {

			log.debug("Correo deshabilitado (cloty.mail.enabled=false), no se envÃ­a a {}", destino);

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

