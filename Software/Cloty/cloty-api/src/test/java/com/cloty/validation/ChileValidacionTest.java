package com.cloty.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChileValidacionTest {

	@Test
	void normalizarRutEliminaPuntosYEspacios() {
		assertEquals("12345678-5", ChileValidacion.formatearRutConGuion("12.345.678-5"));
	}

	@Test
	void esRutValidoAceptaRutCorrecto() {
		assertTrue(ChileValidacion.esRutValido("12345678-5"));
	}

	@Test
	void esRutValidoRechazaRutIncorrecto() {
		assertFalse(ChileValidacion.esRutValido("12345678-0"));
	}

	@Test
	void esTelefonoValidoAceptaMovilChileno() {
		assertTrue(ChileValidacion.esTelefonoChilenoValido("+56912345678"));
	}

	@Test
	void esEmailValidoRechazaSinDominio() {
		assertFalse(ChileValidacion.esEmailValido("correo@"));
	}
}
