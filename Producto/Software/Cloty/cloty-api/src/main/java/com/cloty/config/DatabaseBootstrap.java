package com.cloty.config;

import com.cloty.domain.RolUsuario;
import com.cloty.domain.SuperUsuario;
import com.cloty.domain.Usuario;
import com.cloty.repo.SuperUsuarioRepository;
import com.cloty.repo.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Desarrollo: si la base está vacía, crea el super usuario inicial.
 * El esquema lo genera Hibernate ({@code ddl-auto=update}); la BD {@code cloty} se crea vía JDBC si no existe.
 */
@Component
@Profile("!test")
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DatabaseBootstrap implements ApplicationRunner {

	private final UsuarioRepository usuarioRepository;
	private final SuperUsuarioRepository superUsuarioRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${cloty.bootstrap.super-username:superadmin}")
	private String superUsername;

	@Value("${cloty.bootstrap.super-password:super123}")
	private String superPassword;

	@Value("${cloty.bootstrap.super-rut:00000000-0}")
	private String superRut;

	@Value("${cloty.bootstrap.super-email:superadmin@cloty.local}")
	private String superEmail;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (usuarioRepository.count() == 0) {
			Usuario superUser = Usuario.builder()
					.username(superUsername)
					.rut(superRut)
					.passwordHash(passwordEncoder.encode(superPassword))
					.rol(RolUsuario.SUPER_USUARIO)
					.estado(Boolean.TRUE)
					.build();
			superUser = usuarioRepository.save(superUser);
			superUsuarioRepository.save(SuperUsuario.builder()
					.idUsuario(superUser.getIdUsuario())
					.rut(superRut)
					.nombres("Super")
					.apellidos("Administrador")
					.email(superEmail)
					.build());
			log.warn("Base vacía: creado super usuario '{}' (cambiar contraseña en producción)", superUsername);
			return;
		}
		asegurarPerfilSuperRoot();
	}

	private void asegurarPerfilSuperRoot() {
		usuarioRepository.findByUsername(superUsername).ifPresent(u -> {
			if (u.getRol() != RolUsuario.SUPER_USUARIO) {
				return;
			}
			if (superUsuarioRepository.findByIdUsuario(u.getIdUsuario()).isPresent()) {
				return;
			}
			superUsuarioRepository.save(SuperUsuario.builder()
					.idUsuario(u.getIdUsuario())
					.rut(superRut)
					.nombres("Super")
					.apellidos("Administrador")
					.email(superEmail)
					.build());
			log.info("Perfil de super usuario creado para '{}'", superUsername);
		});
	}
}
