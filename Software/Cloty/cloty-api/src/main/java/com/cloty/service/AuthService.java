package com.cloty.service;

import com.cloty.domain.Apoderado;
import com.cloty.domain.Colegio;
import com.cloty.domain.RolUsuario;
import com.cloty.domain.Usuario;
import com.cloty.dto.ActivarCuentaApoderadoRequest;
import com.cloty.dto.ActivarCuentaColegioRequest;
import com.cloty.dto.AuthMeResponse;
import com.cloty.dto.AuthTokenResponse;
import com.cloty.dto.CambiarContrasenaRequest;
import com.cloty.dto.LoginRequest;
import com.cloty.dto.RegistroApoderadoRequest;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.UsuarioRepository;
import com.cloty.security.ClotyUserDetails;
import com.cloty.security.ClotyUserDetailsService;
import com.cloty.security.JwtService;
import com.cloty.util.NombreUsuarioGenerator;
import com.cloty.web.error.BadRequestException;
import com.cloty.web.error.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final ApoderadoRepository apoderadoRepository;
	private final ColegioRepository colegioRepository;
	private final PasswordEncoder passwordEncoder;
	private final ClotyUserDetailsService userDetailsService;
	private final JwtService jwtService;
	// esto es nuevo
	private final EmailService emailService;

	@Value("${cloty.jwt.expiration-ms:86400000}")
	private long expirationMs;

	public AuthTokenResponse login(LoginRequest req) {
		String id = req.identificador().trim();
		Usuario usuario = resolverUsuarioPorIdentificador(id);
		ClotyUserDetails user = (ClotyUserDetails) userDetailsService.loadUserByUsername(usuario.getUsername());
		if (!user.isEnabled()) {
			throw new BadRequestException("La cuenta está deshabilitada");
		}
		if (!passwordEncoder.matches(req.password(), user.getPassword())) {
			throw new BadRequestException("Credenciales inválidas");
		}
		return new AuthTokenResponse(jwtService.generateToken(user), expirationMs);
	}

	private Usuario resolverUsuarioPorIdentificador(String id) {
		return usuarioRepository.findByUsername(id)
				.or(() -> usuarioRepository.findByRut(id))
				.or(() -> apoderadoRepository.findByRut(id)
						.filter(a -> a.getIdUsuario() != null)
						.map(Apoderado::getIdUsuario)
						.flatMap(usuarioRepository::findById))
				.or(() -> apoderadoRepository.findByEmailIgnoreCase(id)
						.filter(a -> a.getIdUsuario() != null)
						.map(Apoderado::getIdUsuario)
						.flatMap(usuarioRepository::findById))
				.or(() -> colegioRepository.findByRut(id)
						.filter(c -> c.getIdUsuario() != null)
						.map(Colegio::getIdUsuario)
						.flatMap(usuarioRepository::findById))
				.or(() -> loginPorEmailColegio(id))
				.orElseThrow(() -> new BadRequestException("Credenciales inválidas"));
	}

	@Transactional
	public AuthTokenResponse registroApoderado(RegistroApoderadoRequest req) {
		String rutTrim = req.rut().trim();
		validarEmailFormatoSiPresente(req.email());

		apoderadoRepository.findByRut(rutTrim).ifPresent(apo -> {
			if (apo.getIdUsuario() != null) {
				throw new ConflictException("Ya existe una cuenta activa con este RUT. Use el inicio de sesión.");
			}
			throw new BadRequestException(
					"Este RUT ya fue registrado por un colegio. Active su cuenta con POST /api/auth/activar-cuenta-apoderado.");
		});

		String emailNorm = normalizarEmail(req.email());
		if (emailNorm != null) {
			apoderadoRepository.findByEmailIgnoreCase(emailNorm).ifPresent(apo -> {
				if (!apo.getRut().equals(rutTrim)) {
					throw new ConflictException("El correo ya está asociado a otro apoderado");
				}
			});
		}

		if (usuarioRepository.existsByRut(rutTrim)) {
			throw new ConflictException("El RUT ya está asociado a una cuenta");
		}

		String username = NombreUsuarioGenerator.paraPersona(req.nombres(), req.apellidos(), rutTrim, usuarioRepository::existsByUsername);

		Usuario u = Usuario.builder()
				.username(username)
				.rut(rutTrim)
				.passwordHash(passwordEncoder.encode(req.password()))
				.rol(RolUsuario.APODERADO)
				.estado(Boolean.TRUE)
				.build();
		u = usuarioRepository.save(u);
		Apoderado a = Apoderado.builder()
				.idUsuario(u.getIdUsuario())
				.rut(rutTrim)
				.nombres(req.nombres())
				.apellidos(req.apellidos())
				.email(emailNorm != null ? req.email().trim() : null)
				.telefono(blancoANull(req.telefono()))
				.direccion(blancoANull(req.direccion()))
				.build();
		apoderadoRepository.save(a);
		ClotyUserDetails principal = (ClotyUserDetails) userDetailsService.loadUserByUsername(u.getUsername());
		return new AuthTokenResponse(jwtService.generateToken(principal), expirationMs);
	}

	@Transactional
	public AuthTokenResponse activarCuentaApoderado(ActivarCuentaApoderadoRequest req) {
		String rutTrim = req.rut().trim();
		Apoderado a = apoderadoRepository.findByRut(rutTrim)
				.orElseThrow(() -> new BadRequestException(
						"No hay datos de apoderado con ese RUT. Si no fue cargado por un colegio, use POST /api/auth/registro-apoderado."));
		if (a.getIdUsuario() != null) {
			throw new ConflictException("Esta cuenta ya fue activada. Use el inicio de sesión.");
		}
		if (usuarioRepository.existsByRut(rutTrim)) {
			throw new ConflictException("El RUT ya está asociado a una cuenta");
		}
		String username = NombreUsuarioGenerator.paraPersona(a.getNombres(), a.getApellidos(), rutTrim, usuarioRepository::existsByUsername);
		Usuario u = Usuario.builder()
				.username(username)
				.rut(rutTrim)
				.passwordHash(passwordEncoder.encode(req.password()))
				.rol(RolUsuario.APODERADO)
				.estado(Boolean.TRUE)
				.build();
		u = usuarioRepository.save(u);
		a.setIdUsuario(u.getIdUsuario());
		apoderadoRepository.save(a);
		// esto es nuevo
		emailService.enviarActivacionApoderado(a, username);
		ClotyUserDetails principal = (ClotyUserDetails) userDetailsService.loadUserByUsername(u.getUsername());
		return new AuthTokenResponse(jwtService.generateToken(principal), expirationMs);
	}

	@Transactional
	public AuthTokenResponse activarCuentaColegio(ActivarCuentaColegioRequest req) {
		String rutTrim = req.rut().trim();
		validarEmailFormatoSiPresente(req.email());
		String emailNorm = normalizarEmail(req.email());
		if (emailNorm == null) {
			throw new BadRequestException("El correo es obligatorio");
		}
		Colegio col = colegioRepository.findByRut(rutTrim)
				.orElseThrow(() -> new BadRequestException(
						"No hay colegio con ese RUT. Verifique los datos cargados por el administrador."));
		if (col.getIdUsuario() != null) {
			throw new ConflictException("Esta cuenta ya fue activada. Use el inicio de sesión.");
		}
		if (usuarioRepository.existsByRut(rutTrim)) {
			throw new ConflictException("El RUT ya está asociado a una cuenta");
		}
		colegioRepository.findByEmailIgnoreCase(emailNorm).filter(c -> !c.getIdColegio().equals(col.getIdColegio()))
				.ifPresent(c -> {
					throw new ConflictException("El correo ya está registrado en otro colegio");
				});
		String username = NombreUsuarioGenerator.paraColegio(col.getNombre(), rutTrim, usuarioRepository::existsByUsername);
		Usuario u = Usuario.builder()
				.username(username)
				.rut(rutTrim)
				.passwordHash(passwordEncoder.encode(req.password()))
				.rol(RolUsuario.COLEGIO)
				.estado(Boolean.TRUE)
				.build();
		u = usuarioRepository.save(u);
		col.setIdUsuario(u.getIdUsuario());
		col.setEmail(emailNorm);
		col.setTelefono(req.telefono().trim());
		colegioRepository.save(col);
		// esto es nuevo
		emailService.enviarActivacionColegio(col, username);
		ClotyUserDetails principal = (ClotyUserDetails) userDetailsService.loadUserByUsername(u.getUsername());
		return new AuthTokenResponse(jwtService.generateToken(principal), expirationMs);
	}

	private Optional<Usuario> loginPorEmailColegio(String id) {
		if (!id.contains("@")) {
			return Optional.empty();
		}
		String email = normalizarEmail(id);
		if (email == null) {
			return Optional.empty();
		}
		return colegioRepository.findByEmailIgnoreCase(email)
				.filter(c -> c.getIdUsuario() != null)
				.map(Colegio::getIdUsuario)
				.flatMap(usuarioRepository::findById);
	}

	@Transactional
	public void cambiarContrasena(CambiarContrasenaRequest req) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof ClotyUserDetails p)) {
			throw new BadRequestException("No autenticado");
		}
		Usuario u = usuarioRepository.findById(p.getIdUsuario())
				.orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
		if (!passwordEncoder.matches(req.contrasenaActual(), u.getPasswordHash())) {
			throw new BadRequestException("La contraseña actual no es correcta");
		}
		if (req.contrasenaActual().equals(req.contrasenaNueva())) {
			throw new BadRequestException("La nueva contraseña debe ser distinta a la actual");
		}
		u.setPasswordHash(passwordEncoder.encode(req.contrasenaNueva()));
		usuarioRepository.save(u);
	}

	public AuthMeResponse me() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof ClotyUserDetails p)) {
			throw new BadRequestException("No autenticado");
		}
		return new AuthMeResponse(p.getIdUsuario(), p.getUsername(), p.getRol(), p.getIdColegio(), p.getIdApoderado());
	}

	private static String normalizarEmail(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		return email.trim().toLowerCase();
	}

	private static String blancoANull(String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		return s.trim();
	}

	private static void validarEmailFormatoSiPresente(String email) {
		if (email == null || email.isBlank()) {
			return;
		}
		String t = email.trim();
		if (!t.contains("@") || t.indexOf('@') == 0 || t.endsWith("@")) {
			throw new BadRequestException("El formato del correo no es válido");
		}
	}
}
