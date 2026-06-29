package com.cloty.service;

import com.cloty.domain.Apoderado;
import com.cloty.domain.CodigoActivacion;
import com.cloty.domain.Colegio;
import com.cloty.domain.TipoEntidadActivacion;
import com.cloty.repo.CodigoActivacionRepository;
import com.cloty.web.error.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivacionCodigoService {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final CodigoActivacionRepository codigoActivacionRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;

	@Value("${cloty.activacion.codigo-expiracion-minutos:30}")
	private int expiracionMinutos;

	@Transactional
	public void emitirParaApoderado(Apoderado apoderado, Colegio colegio) {
		if (apoderado.getIdUsuario() != null) {
			return;
		}
		String email = apoderado.getEmail();
		if (email == null || email.isBlank()) {
			throw new BadRequestException("El apoderado debe tener correo para enviar el código de activación");
		}
		String codigo = generarCodigo();
		guardarCodigo(TipoEntidadActivacion.APODERADO, apoderado.getIdApoderado(), codigo);
		enviarCodigoTrasCommit(() -> emailService.enviarCodigoActivacionApoderado(
				apoderado, colegio, codigo, expiracionMinutos));
	}

	@Transactional
	public void emitirParaColegio(Colegio colegio) {
		if (colegio.getIdUsuario() != null) {
			return;
		}
		String email = colegio.getEmail();
		if (email == null || email.isBlank()) {
			throw new BadRequestException("El colegio debe tener correo para enviar el código de activación");
		}
		String codigo = generarCodigo();
		guardarCodigo(TipoEntidadActivacion.COLEGIO, colegio.getIdColegio(), codigo);
		enviarCodigoTrasCommit(() -> emailService.enviarCodigoActivacionColegio(colegio, codigo, expiracionMinutos));
	}

	@Transactional
	public void validarYConsumirApoderado(Integer idApoderado, String codigo) {
		validarYConsumir(TipoEntidadActivacion.APODERADO, idApoderado, codigo);
	}

	@Transactional
	public void validarYConsumirColegio(Integer idColegio, String codigo) {
		validarYConsumir(TipoEntidadActivacion.COLEGIO, idColegio, codigo);
	}

	@Transactional
	public void emitirRecuperacionContrasena(Integer idUsuario, String email) {
		String codigo = generarCodigo();
		guardarCodigo(TipoEntidadActivacion.RECUPERACION_CONTRASENA, idUsuario, codigo);
		enviarCodigoTrasCommit(() -> emailService.enviarCodigoRecuperacionContrasena(email, codigo, expiracionMinutos));
	}

	@Transactional
	public void validarYConsumirRecuperacionContrasena(Integer idUsuario, String codigo) {
		validarYConsumir(TipoEntidadActivacion.RECUPERACION_CONTRASENA, idUsuario, codigo);
	}

	@Transactional(readOnly = true)
	public void verificarCodigoColegio(Integer idColegio, String codigo) {
		verificarSinConsumir(TipoEntidadActivacion.COLEGIO, idColegio, codigo);
	}

	@Transactional(readOnly = true)
	public void verificarCodigoApoderado(Integer idApoderado, String codigo) {
		verificarSinConsumir(TipoEntidadActivacion.APODERADO, idApoderado, codigo);
	}

	private void validarYConsumir(TipoEntidadActivacion tipo, Integer idEntidad, String codigoIngresado) {
		if (codigoIngresado == null || codigoIngresado.isBlank()) {
			throw new BadRequestException("Debe ingresar el código de activación enviado a su correo");
		}
		String codigo = codigoIngresado.trim();
		CodigoActivacion activo = codigoActivacionRepository
				.findTopByTipoAndIdEntidadAndUsadoFalseOrderByIdCodigoActivacionDesc(tipo, idEntidad)
				.orElseThrow(() -> new BadRequestException("No hay código de activación vigente. Vuelva a solicitar uno desde activar cuenta."));
		if (activo.getExpiraEn().isBefore(LocalDateTime.now())) {
			throw new BadRequestException("El código de activación expiró. Vuelva a solicitar uno desde activar cuenta.");
		}
		if (!passwordEncoder.matches(codigo, activo.getCodigoHash())) {
			throw new BadRequestException("El código de activación no es válido");
		}
		activo.setUsado(true);
		codigoActivacionRepository.save(activo);
	}

	private void verificarSinConsumir(TipoEntidadActivacion tipo, Integer idEntidad, String codigoIngresado) {
		if (codigoIngresado == null || codigoIngresado.isBlank()) {
			throw new BadRequestException("Debe ingresar el código de activación enviado a su correo");
		}
		String codigo = codigoIngresado.trim();
		CodigoActivacion activo = codigoActivacionRepository
				.findTopByTipoAndIdEntidadAndUsadoFalseOrderByIdCodigoActivacionDesc(tipo, idEntidad)
				.orElseThrow(() -> new BadRequestException("No hay código de activación vigente. Vuelva a solicitar uno desde activar cuenta."));
		if (activo.getExpiraEn().isBefore(LocalDateTime.now())) {
			throw new BadRequestException("El código de activación expiró. Vuelva a solicitar uno desde activar cuenta.");
		}
		if (!passwordEncoder.matches(codigo, activo.getCodigoHash())) {
			throw new BadRequestException("El código de activación no es válido");
		}
	}

	private void guardarCodigo(TipoEntidadActivacion tipo, Integer idEntidad, String codigoPlano) {
		codigoActivacionRepository.findTopByTipoAndIdEntidadAndUsadoFalseOrderByIdCodigoActivacionDesc(tipo, idEntidad)
				.ifPresent(anterior -> {
					anterior.setUsado(true);
					codigoActivacionRepository.save(anterior);
				});
		CodigoActivacion codigo = CodigoActivacion.builder()
				.tipo(tipo)
				.idEntidad(idEntidad)
				.codigoHash(passwordEncoder.encode(codigoPlano))
				.expiraEn(LocalDateTime.now().plusMinutes(expiracionMinutos))
				.usado(false)
				.build();
		codigoActivacionRepository.save(codigo);
	}

	private static String generarCodigo() {
		int n = RANDOM.nextInt(1_000_000);
		return String.format("%06d", n);
	}

	private void enviarCodigoTrasCommit(Runnable envio) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					envio.run();
				}
			});
		} else {
			envio.run();
		}
	}
}
