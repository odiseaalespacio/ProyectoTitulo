package com.cloty.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = TelefonoChilenoValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface TelefonoChileno {

	String message() default "Teléfono chileno inválido (móvil: 9 dígitos comenzando en 9)";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
