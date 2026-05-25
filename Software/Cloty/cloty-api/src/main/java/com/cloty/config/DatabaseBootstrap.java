package com.cloty.config;

import com.cloty.domain.RolUsuario;
import com.cloty.domain.Usuario;
import com.cloty.repo.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Desarrollo: si la base está vacía, crea el super usuario inicial.
 * El esquema lo genera Hibernate ({@code ddl-auto=update}); la BD {@code cloty} se crea vía JDBC si no existe.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DatabaseBootstrap implements ApplicationRunner {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${cloty.bootstrap.super-username:superadmin}")
	private String superUsername;

	@Value("${cloty.bootstrap.super-password:super123}")
	private String superPassword;

	@Value("${cloty.bootstrap.super-rut:00000000-0}")
	private String superRut;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (usuarioRepository.count() > 0) {
			return;
		}
		Usuario superUser = Usuario.builder()
				.username(superUsername)
				.rut(superRut)
				.passwordHash(passwordEncoder.encode(superPassword))
				.rol(RolUsuario.SUPER_USUARIO)
				.estado(Boolean.TRUE)
				.build();
		usuarioRepository.save(superUser);
		log.warn("Base vacía: creado super usuario '{}' (cambiar contraseña en producción)", superUsername);
	}
}
