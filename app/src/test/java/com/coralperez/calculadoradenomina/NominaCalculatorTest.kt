package com.coralperez.calculadoradenomina

import org.junit.Assert.assertEquals
import org.junit.Test

class NominaCalculatorTest {

    private fun assertValor(esperado: Double, obtenido: Double) {
        assertEquals(esperado, obtenido, 1.0)
    }

    @Test
    fun casoA_salarioMinimo_HorasExtraDiaHabil() {
        val r = calcularNomina(1_750_905.0, 10.0, 4.0, esDominical = false, transporteEmpresa = false)

        assertValor(8_338.0, r.valorHora)
        assertValor(162_584.0, r.totalHorasExtra)
        assertValor(249_095.0, r.auxilioTransporte)
        assertValor(2_162_584.0, r.totalDevengado)
        assertValor(76_540.0, r.aporteSalud)
        assertValor(76_540.0, r.aportePension)
        assertValor(0.0, r.fondoSolidaridad)
        assertValor(2_009_505.0, r.salarioNeto)
        assertEquals(RangoSalarial.RANGO_1, r.rango)
    }

    @Test
    fun casoB_horasExtraDomingoFestivo() {
        val r = calcularNomina(2_500_000.0, 6.0, 2.0, esDominical = true, transporteEmpresa = false)

        assertValor(11_905.0, r.valorHora)
        assertValor(216_667.0, r.totalHorasExtra)
        assertValor(249_095.0, r.auxilioTransporte)
        assertValor(2_965_762.0, r.totalDevengado)
        assertValor(108_667.0, r.aporteSalud)
        assertValor(108_667.0, r.aportePension)
        assertValor(0.0, r.fondoSolidaridad)
        assertValor(2_748_428.0, r.salarioNeto)
        assertEquals(RangoSalarial.RANGO_1, r.rango)
    }

    @Test
    fun casoC_transporteSuministradoPorLaEmpresa() {
        val r = calcularNomina(3_000_000.0, 5.0, 0.0, esDominical = false, transporteEmpresa = true)

        assertValor(14_286.0, r.valorHora)
        assertValor(89_286.0, r.totalHorasExtra)
        assertValor(0.0, r.auxilioTransporte)
        assertValor(3_089_286.0, r.totalDevengado)
        assertValor(123_571.0, r.aporteSalud)
        assertValor(123_571.0, r.aportePension)
        assertValor(0.0, r.fondoSolidaridad)
        assertValor(2_842_143.0, r.salarioNeto)
        assertEquals(RangoSalarial.RANGO_1, r.rango)
    }

    @Test
    fun casoD_salarioAlto_ConFondoDeSolidaridadPensional() {
        val r = calcularNomina(8_000_000.0, 0.0, 0.0, esDominical = false, transporteEmpresa = false)

        assertValor(38_095.0, r.valorHora)
        assertValor(0.0, r.totalHorasExtra)
        assertValor(0.0, r.auxilioTransporte)
        assertValor(8_000_000.0, r.totalDevengado)
        assertValor(320_000.0, r.aporteSalud)
        assertValor(320_000.0, r.aportePension)
        assertValor(80_000.0, r.fondoSolidaridad)
        assertValor(7_280_000.0, r.salarioNeto)
        assertEquals(RangoSalarial.RANGO_3, r.rango)
    }

    @Test
    fun casoE_horasExtraVaciasSeTomanComoCero() {
        val r = calcularNomina(2_000_000.0, 0.0, 0.0, esDominical = false, transporteEmpresa = false)

        assertValor(0.0, r.totalHorasExtra)
        // $ 2.000.000 está por debajo de 2 SMMLV, así que sí recibe auxilio de transporte
        assertValor(249_095.0, r.auxilioTransporte)
        assertValor(2_249_095.0, r.totalDevengado)
        assertValor(80_000.0, r.aporteSalud)
        assertValor(80_000.0, r.aportePension)
        assertValor(0.0, r.fondoSolidaridad)
        assertValor(2_089_095.0, r.salarioNeto)
    }

    @Test
    fun clasificarRango_limites() {
        assertEquals(RangoSalarial.RANGO_1, clasificarRango(SMMLV_2026))
        assertEquals(RangoSalarial.RANGO_1, clasificarRango(2 * SMMLV_2026))
        assertEquals(RangoSalarial.RANGO_2, clasificarRango(2 * SMMLV_2026 + 1))
        assertEquals(RangoSalarial.RANGO_2, clasificarRango(4 * SMMLV_2026 - 1))
        assertEquals(RangoSalarial.RANGO_3, clasificarRango(4 * SMMLV_2026))
    }

    @Test
    fun conversionesSeguras_noRevientanConTextoInvalido() {
        assertEquals(null, "abc".toDoubleOrNull())
        assertEquals(0.0, "".toDoubleOrNull() ?: 0.0, 0.0)
        assertEquals(3.0, "3".toDoubleOrNull() ?: 0.0, 0.0)
    }
}
