package com.example.cloty_colegio.data.api

import org.junit.Assert.assertEquals
import org.junit.Test

class ColegioDashboardModelTest {

    @Test
    fun resumenCursoCalculaSinTarjeta() {
        val curso = ResumenCursoDashboard(
            idCurso = 1,
            nombre = "3°A",
            nivel = "3° Medio",
            totalAlumnos = 30,
            alumnosConTarjeta = 25,
            alumnosSinTarjeta = 5
        )
        assertEquals(5, curso.alumnosSinTarjeta)
        assertEquals("3°A", curso.nombre)
    }

    @Test
    fun dashboardContieneSeccionesPrincipales() {
        val dashboard = ColegioDashboard(
            idColegio = 1,
            nombreColegio = "Liceo Demo",
            totalAlumnos = 100,
            totalApoderados = 80,
            totalCursos = 12,
            apoderadosConCuenta = 60,
            alumnosConTarjeta = 90,
            alumnosSinTarjeta = 10,
            tarjetasActivas = 95,
            tarjetasPerdidas = 2,
            tarjetasDesactivadas = 1,
            prendasEncontradasHoy = 3,
            prendasEncontradasTotal = 40,
            prendasEntregadasHoy = 1,
            prendasEntregadasTotal = 35,
            notificacionesEnviadas = 50,
            notificacionesPendientes = 2,
            resumenCursos = listOf(
                ResumenCursoDashboard(1, "1°A", "1° Medio", 25, 20, 5)
            ),
            ultimasAcciones = emptyList()
        )
        assertEquals(100, dashboard.totalAlumnos)
        assertEquals(1, dashboard.resumenCursos?.size)
        assertEquals(10, dashboard.alumnosSinTarjeta)
    }
}
