package com.example.cloty_apoderado.data.api

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiModelsTest {

    @Test
    fun pupiloResumenExponeColegioYCurso() {
        val pupilo = PupiloResumen(
            idAlumno = 10,
            rut = "12345678-5",
            nombres = "Pedro",
            apellidos = "Gómez",
            estado = true,
            idCurso = 3,
            nombreCurso = "2°A",
            idColegio = 1,
            nombreColegio = "Colegio Demo"
        )
        assertEquals("2°A", pupilo.nombreCurso)
        assertEquals("Colegio Demo", pupilo.nombreColegio)
    }

    @Test
    fun regionComunaModelosCompatiblesConApi() {
        val region = Region(codigoRegion = "13", nombre = "Metropolitana")
        val comuna = Comuna(codigoComuna = "13101", codigoRegion = "13", nombre = "Santiago")
        assertEquals(region.codigoRegion, comuna.codigoRegion)
    }
}
