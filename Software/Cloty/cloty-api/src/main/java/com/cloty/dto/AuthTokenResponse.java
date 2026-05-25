package com.cloty.dto;

public record AuthTokenResponse(String token, String tokenType, long expiresInMs) {
	public AuthTokenResponse(String token, long expiresInMs) {
		this(token, "Bearer", expiresInMs);
	}
}
