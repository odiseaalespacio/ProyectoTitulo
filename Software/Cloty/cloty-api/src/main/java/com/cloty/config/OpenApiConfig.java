package com.cloty.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	private static final String SECURITY_SCHEME = "bearerAuth";

	@Bean
	public OpenAPI clotyOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Cloty API")
						.description("API REST para gestión de colegios, apoderados, alumnos, tarjetas NFC y notificaciones.")
						.version("1.0.0")
						.contact(new Contact().name("Cloty").email("soporte@cloty.local")))
				.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
				.components(new Components().addSecuritySchemes(SECURITY_SCHEME,
						new SecurityScheme()
								.name(SECURITY_SCHEME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("Token JWT obtenido en POST /api/auth/login")));
	}
}
