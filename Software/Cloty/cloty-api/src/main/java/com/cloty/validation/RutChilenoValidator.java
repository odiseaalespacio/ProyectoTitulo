package com.cloty.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RutChilenoValidator implements ConstraintValidator<RutChileno, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}
		return ChileValidacion.esRutValido(value);
	}
}
