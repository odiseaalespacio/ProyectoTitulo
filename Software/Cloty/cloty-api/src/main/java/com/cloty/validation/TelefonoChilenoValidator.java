package com.cloty.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// esto es nuevo
public class TelefonoChilenoValidator implements ConstraintValidator<TelefonoChileno, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}
		return ChileValidacion.esTelefonoChilenoValido(value);
	}
}
