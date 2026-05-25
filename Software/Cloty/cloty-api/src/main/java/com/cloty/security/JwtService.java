package com.cloty.security;

import com.cloty.domain.RolUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

	private final SecretKey secretKey;
	private final long expirationMs;

	public JwtService(
			@Value("${cloty.jwt.secret}") String secret,
			@Value("${cloty.jwt.expiration-ms:86400000}") long expirationMs) {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		if (keyBytes.length < 32) {
			byte[] padded = new byte[32];
			System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
			keyBytes = padded;
		}
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
		this.expirationMs = expirationMs;
	}

	public String generateToken(ClotyUserDetails user) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.subject(String.valueOf(user.getIdUsuario()))
				.claim("rol", user.getRol().name())
				.claim("col", user.getIdColegio())
				.claim("apo", user.getIdApoderado())
				.issuedAt(now)
				.expiration(exp)
				.signWith(secretKey)
				.compact();
	}

	public ClotyUserDetails parseToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		Integer idUsuario = Integer.parseInt(claims.getSubject());
		RolUsuario rol = RolUsuario.valueOf(claims.get("rol", String.class));
		Integer idColegio = toInteger(claims.get("col"));
		Integer idApoderado = toInteger(claims.get("apo"));
		return new ClotyUserDetails(idUsuario, null, "", true, rol, idColegio, idApoderado);
	}

	private static Integer toInteger(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Integer i) {
			return i;
		}
		if (value instanceof Long l) {
			return l.intValue();
		}
		if (value instanceof Number n) {
			return n.intValue();
		}
		return null;
	}
}
