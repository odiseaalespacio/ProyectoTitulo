package com.cloty.service;

import com.cloty.domain.Administrador;
import com.cloty.domain.Apoderado;
import com.cloty.domain.Colegio;
import com.cloty.domain.RolUsuario;
import com.cloty.domain.SuperUsuario;
import com.cloty.domain.Usuario;
import com.cloty.dto.ActivarCuentaApoderadoRequest;
import com.cloty.dto.ActivarCuentaColegioRequest;
import com.cloty.dto.AuthMeResponse;
import com.cloty.dto.AuthTokenResponse;
import com.cloty.dto.CambiarContrasenaRequest;
import com.cloty.dto.LoginRequest;
import com.cloty.dto.RegistroApoderadoRequest;
import com.cloty.dto.RestablecerContrasenaRequest;
import com.cloty.dto.SolicitarCodigoActivacionRequest;
import com.cloty.dto.SolicitarCodigoActivacionResponse;
import com.cloty.dto.ValidarCodigoActivacionRequest;
import com.cloty.repo.AdministradorRepository;
import com.cloty.repo.SuperUsuarioRepository;
import com.cloty.repo.ApoderadoRepository;
import com.cloty.repo.ColegioApoderadoRepository;
import com.cloty.repo.ColegioRepository;
import com.cloty.repo.UsuarioRepository;
import com.cloty.security.ClotyUserDetails;
import com.cloty.security.ClotyUserDetailsService;
import com.cloty.security.JwtService;
import com.cloty.util.NombreUsuarioGenerator;
import com.cloty.validation.ChileValidacion;
import com.cloty.web.error.BadRequestException;
import com.cloty.web.error.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final ApoderadoRepository apoderadoRepository;
	private final ColegioRepository colegioRepository;
	private final ColegioApoderadoRepository colegioApoderadoRepository;
	private final AdministradorRepository administradorRepository;
	private final SuperUsuarioRepository superUsuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final ClotyUserDetailsService userDetailsService;
	private final JwtService jwtService;
	private final EmailService emailService;
	private final ActivacionCodigoService activacionCodigoService;

	@Value("${cloty.jwt.expiration-ms:86400000}")
	private long expirationMs;

	public AuthTokenResponse login(LoginRequest req) {
		String id = req.identificador().trim();
		Usuario usuario = resolverUsuarioPorIdentificador(id);
		ClotyUserDetails user = (ClotyUserDetails) userDetailsService.loadUserByUsername(usuario.getUsername());
		if (!user.isEnabled()) {
			throw new BadRequestException("La cuenta estÃ¡ deshabilitada");
		}
		if (!passwordEncoder.matches(req.password(), user.getPassword())) {
			throw new BadRequestException("Credenciales invÃ¡lidas");
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
				.orElseThrow(() -> new BadRequestException("Credenciales invÃ¡lidas"));
	}

	@Transactional
	public AuthTokenResponse registroApoderado(RegistroApoderadoRequest req) {
		String rutTrim = ChileValidacion.formatearRutConGuion(req.rut());
		validarEmailFormatoSiPresente(req.email());

		apoderadoRepository.findByRut(rutTrim).ifPresent(apo -> {
			if (apo.getIdUsuario() != null) {
				throw new ConflictException("Ya existe una cuenta activa con este RUT. Use el inicio de sesiÃ³n.");
			}
			throw new BadRequestException(
					"Este RUT ya fue registrado por un colegio. Active su cuenta con POST /api/auth/activar-cuenta-apoderado.");
		});

		String emailNorm = normalizarEmail(req.email());
		if (emailNorm != null) {
			apoderadoRepository.findByEmailIgnoreCase(emailNorm).ifPresent(apo -> {
				if (!apo.getRut().equals(rutTrim)) {
					throw new ConflictException("El correo ya estÃ¡ asociado a otro apoderado");
				}
			});
		}

		if (usuarioRepository.existsByRut(rutTrim)) {
			throw new ConflictException("El RUT ya estÃ¡ asociado a una cuenta");
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
	public SolicitarCodigoActivacionResponse solicitarCodigoApoderado(SolicitarCodigoActivacionRequest req) {
		String rutTrim = ChileValidacion.formatearRutConGuion(req.rut());
		Apoderado a = apoderadoRepository.findByRut(rutTrim)
				.orElseThrow(() -> new BadRequestException(
						"No hay datos de apoderado con ese RUT. Si no fue cargado por un colegio, use el registro de apoderado."));
		if (a.getIdUsuario() != null) {
			throw new ConflictException("Esta cuenta ya fue activada. Use el inicio de sesiÃ³n.");
		}
		if (a.getEmail() == null || a.getEmail().isBlank()) {
			throw new BadRequestException("No hay correo registrado para este apoderado. Contacte al colegio.");
		}
		var vinculos = colegioApoderadoRepository.findByIdApoderado(a.getIdApoderado());
		if (vinculos.isEmpty()) {
			throw new BadRequestException("El apoderado no estÃ¡ asociado a ningÃºn colegio. Contacte al colegio.");
		}
		Colegio colegio = colegioRepository.findById(vinculos.get(0).getIdColegio())
				.orElseThrow(() -> new BadRequestException("No se encontrÃ³ el colegio asociado."));
		activacionCodigoService.emitirParaApoderado(a, colegio);
		String correo = enmascararCorreo(a.getEmail());
		return new SolicitarCodigoActivacionResponse(
				correo,
				"Se enviÃ³ un cÃ³digo de activaciÃ³n al correo registrado (" + correo + ").");
	}

	@Transactional
	public SolicitarCodigoActivacionResponse solicitarCodigoColegio(SolicitarCodigoActivacionRequest req) {
		String rutTrim = ChileValidacion.formatearRutConGuion(req.rut());
		Colegio col = colegioRepository.findByRut(rutTrim)
				.orElseThrow(() -> new BadRequestException(
						"No hay colegio con ese RUT. Verifique los datos cargados por el administrador."));
		if (col.getIdUsuario() != null) {
			throw new ConflictException("Esta cuenta ya fue activada. Use el inicio de sesiÃ³n.");
		}
		if (col.getEmail() == null || col.getEmail().isBlank()) {
			throw new BadRequestException("No hay correo registrado para este colegio. Contacte al administrador.");
		}
		activacionCodigoService.emitirParaColegio(col);
		String correo = enmascararCorreo(col.getEmail());
		return new SolicitarCodigoActivacionResponse(
				correo,
				"Se enviÃ³ un cÃ³digo de activaciÃ³n al correo registrado (" + correo + ").");
	}

	@Transactional
	public SolicitarCodigoActivacionResponse solicitarRecuperacionContrasena(SolicitarCodigoActivacionRequest req) {
		String rutTrim = ChileValidacion.formatearRutConGuion(req.rut());
		Usuario u = usuarioRepository.findByRut(rutTrim)
				.orElseThrow(() -> new BadRequestException(
						"No hay cuenta activa con ese RUT. Si aÃºn no activÃ³ su cuenta, use la opciÃ³n Activar cuenta."));
		if (!Boolean.TRUE.equals(u.getEstado())) {
			throw new BadRequestException("La cuenta estÃ¡ deshabilitada. Contacte al administrador.");
		}
		String email = resolverCorreoPorUsuario(u);
		if (email == null || email.isBlank()) {
			throw new BadRequestException(
					"No hay correo registrado para recuperar la contraseÃ±a. Contacte al administrador.");
		}
		activacionCodigoService.emitirRecuperacionContrasena(u.getIdUsuario(), email);
		String correo = enmascararCorreo(email);
		return new SolicitarCodigoActivacionResponse(
				correo,
				"Se enviÃ³ un cÃ³digo de recuperaciÃ³n al correo registrado (" + correo + ").");
	}

	@Transactional
	public void restablecerContrasena(RestablecerContrasenaRequest req) {
		String rutTrim = ChileValidacion.formatearRutConGuion(req.rut());
		Usuario u = usuarioRepository.findByRut(rutTrim)
				.orElseThrow(() -> new BadRequestException("No hay cuenta con ese RUT."));
		if (!Boolean.TRUE.equals(u.getEstado())) {
			throw new BadRequestException("La cuenta estÃ¡ deshabilitada.");
		}
		activacionCodigoService.validarYConsumirRecuperacionContrasena(u.getIdUsuario(), req.codigo());
		if (passwordEncoder.matches(req.password(), u.getPasswordHash())) {
			throw new BadRequestException("La nueva contraseÃ±a debe ser distinta a la actual.");
		}
		u.setPasswordHash(passwordEncoder.encode(req.password()));
		usuarioRepository.save(u);
	}

	@Transactional(readOnly = true)
	public void validarCodigoApoderado(ValidarCodigoActivacionRequest req) {
		String rutTrim = ChileValidacion.formatearRutConGuion(req.rut());
		Apoderado a = apoderadoRepository.findByRut(rutTrim)
				.orElseThrow(() -> new BadRequestException("No hay datos de apoderado con ese RUT."));
		if (a.getIdUsuario() != null) {
			throw new ConflictException("Esta cuenta ya fue activada. Use el inicio de sesiÃ³n.");
		}
		activacionCodigoService.verificarCodigoApoderado(a.getIdApoderado(), req.codigo());
	}

	@Transactional(readOnly = true)
	public void validarCodigoColegio(ValidarCodigoActivacionRequest req) {
		String rutTrim = ChileValidacion.formatearRutConGuion(req.rut());
		Colegio col = colegioRepository.findByRut(rutTrim)
				.orElseThrow(() -> new BadRequestException(
						"No hay colegio con ese RUT. Verifique los datos cargados por el administrador."));
		if (col.getIdUsuario() != null) {
			throw new ConflictException("Esta cuenta ya fue activada. Use el inicio de sesiÃ³n.");
		}
		activacionCodigoService.verificarCodigoColegio(col.getIdColegio(), req.codigo());
	}

	@Transactional
	public AuthTokenResponse activarCuentaApoderado(ActivarCuentaApoderadoRequest req) {
		String rutTrim = ChileValidacion.formatearRutConGuion(req.rut());
		Apoderado a = apoderadoRepository.findByRut(rutTrim)
				.orElseThrow(() -> new BadRequestException(
						"No hay datos de apoderado con ese RUT. Si no fue cargado por un colegio, use POST /api/auth/registro-apoderado."));
		if (a.getIdUsuario() != null) {
			throw new ConflictException("Esta cuenta ya fue activada. Use el inicio de sesiÃ³n.");
		}
		if (usuarioRepository.existsByRut(rutTrim)) {
			throw new ConflictException("El RUT ya estÃ¡ asociado a una cuenta");
		}
		activacionCodigoService.validarYConsumirApoderado(a.getIdApoderado(), req.codigo());
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
		emailService.enviarActivacionApoderado(a, username);
		ClotyUserDetails principal = (ClotyUserDetails) userDetailsService.loadUserByUsername(u.getUsername());
		return new AuthTokenResponse(jwtService.generateToken(principal), expirationMs);
	}

	@Transactional
	public AuthTokenResponse activarCuentaColegio(ActivarCuentaColegioRequest req) {
		String rutTrim = ChileValidacion.formatearRutConGuion(req.rut());
		Colegio col = colegioRepository.findByRut(rutTrim)
				.orElseThrow(() -> new BadRequestException(
						"No hay colegio con ese RUT. Verifique los datos cargados por el administrador."));
		if (col.getIdUsuario() != null) {
			throw new ConflictException("Esta cuenta ya fue activada. Use el inicio de sesiÃ³n.");
		}
		if (usuarioRepository.existsByRut(rutTrim)) {
			throw new ConflictException("El RUT ya estÃ¡ asociado a una cuenta");
		}
		if (col.getEmail() == null || col.getEmail().isBlank()) {
			throw new BadRequestException("El colegio no tiene correo registrado. Contacte al administrador.");
		}
		activacionCodigoService.validarYConsumirColegio(col.getIdColegio(), req.codigo());
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
		colegioRepository.save(col);
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
			throw new BadRequestException("La contraseÃ±a actual no es correcta");
		}
		if (req.contrasenaActual().equals(req.contrasenaNueva())) {
			throw new BadRequestException("La nueva contraseÃ±a debe ser distinta a la actual");
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

	private String resolverCorreoPorUsuario(Usuario u) {
		return switch (u.getRol()) {
			case APODERADO -> apoderadoRepository.findByIdUsuario(u.getIdUsuario())
					.map(Apoderado::getEmail)
					.orElse(null);
			case COLEGIO -> colegioRepository.findByIdUsuario(u.getIdUsuario())
					.map(Colegio::getEmail)
					.orElse(null);
			case ADMINISTRADOR -> administradorRepository.findByIdUsuario(u.getIdUsuario())
					.map(Administrador::getEmail)
					.orElse(null);
			case SUPER_USUARIO -> superUsuarioRepository.findByIdUsuario(u.getIdUsuario())
					.map(SuperUsuario::getEmail)
					.orElse(null);
		};
	}

	private static String enmascararCorreo(String email) {
		if (email == null || email.isBlank()) {
			return "";
		}
		String t = email.trim();
		int arroba = t.indexOf('@');
		if (arroba <= 0) {
			return "***";
		}
		String local = t.substring(0, arroba);
		String dominio = t.substring(arroba);
		if (local.length() == 1) {
			return local.charAt(0) + "***" + dominio;
		}
		return local.charAt(0) + "***" + local.charAt(local.length() - 1) + dominio;
	}

	private static void validarEmailFormatoSiPresente(String email) {
		if (email == null || email.isBlank()) {
			return;
		}
		String t = email.trim();
		if (!t.contains("@") || t.indexOf('@') == 0 || t.endsWith("@")) {
			throw new BadRequestException("El formato del correo no es vÃ¡lido");
		}
	}
}
