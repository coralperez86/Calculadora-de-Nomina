package com.coralperez.calculadoradenomina

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.saveable.Saver
import java.text.NumberFormat
import java.util.Locale

// Salario mínimo mensual legal vigente 2026 
const val SMMLV_2026 = 1_750_905.0

// Auxilio de transporte 2026 — Decreto 1470 de 2025
const val AUX_TRANSPORTE_2026 = 249_095.0

// Horas ordinarias mensuales: 42 h semanales 
const val HORAS_MES = 210.0

// Aporte a salud del trabajador 
const val PORC_SALUD = 0.04

// Aporte a pensión del trabajador 
const val PORC_PENSION = 0.04

// Fondo de Solidaridad Pensional 
const val PORC_FSP = 0.01

// Límite máximo de horas extra en el mes (12 semanales × 4 semanas) 
const val MAX_HORAS_EXTRA_MES = 48.0

// Factores de recargo de las horas extra, en día hábil se aplica el recargo legal,
// en domingo o festivo se suma el recargo dominical del 90 %.
const val FACTOR_EXTRA_DIURNA_HABIL = 1.25   // 25 % de recargo
const val FACTOR_EXTRA_DIURNA_DOMINICAL = 2.15 // 25 % + 90 % dominical
const val FACTOR_EXTRA_NOCTURNA_HABIL = 1.75   // 75 % de recargo
const val FACTOR_EXTRA_NOCTURNA_DOMINICAL = 2.65 // 75 % + 90 % dominical

enum class RangoSalarial(
    @StringRes val descripcionRes: Int,
    @DrawableRes val imagenRes: Int,
    @StringRes val contentDescriptionRes: Int
) {
    RANGO_1(
        descripcionRes = R.string.rango_1_desc,
        imagenRes = R.drawable.ic_rango_1,
        contentDescriptionRes = R.string.cd_rango_1
    ),
    RANGO_2(
        descripcionRes = R.string.rango_2_desc,
        imagenRes = R.drawable.ic_rango_2,
        contentDescriptionRes = R.string.cd_rango_2
    ),
    RANGO_3(
        descripcionRes = R.string.rango_3_desc,
        imagenRes = R.drawable.ic_rango_3,
        contentDescriptionRes = R.string.cd_rango_3
    )
}

data class ResultadoNomina(
    val valorHora: Double,
    val totalHorasExtra: Double,
    val auxilioTransporte: Double,
    val totalDevengado: Double,
    val aporteSalud: Double,
    val aportePension: Double,
    val fondoSolidaridad: Double,
    val totalDeducciones: Double,
    val salarioNeto: Double,
    val rango: RangoSalarial
)

/**
 * Regla 8 — Clasificación del trabajador según su salario básico.
 */
fun clasificarRango(salarioBasico: Double): RangoSalarial = when {
    salarioBasico <= 2 * SMMLV_2026 -> RangoSalarial.RANGO_1
    salarioBasico < 4 * SMMLV_2026 -> RangoSalarial.RANGO_2
    else -> RangoSalarial.RANGO_3
}

/**
 * Reglas 1 a 7 — Cálculo completo de la nómina.
 */
fun calcularNomina(
    salarioBasico: Double,
    horasDiurnas: Double,
    horasNocturnas: Double,
    esDominical: Boolean,
    transporteEmpresa: Boolean
): ResultadoNomina {
    // Regla 1 — valor de la hora ordinaria
    val valorHora = salarioBasico / HORAS_MES

    // Regla 2 — valor de las horas extra
    val factorDiurno = if (esDominical) FACTOR_EXTRA_DIURNA_DOMINICAL else FACTOR_EXTRA_DIURNA_HABIL
    val factorNocturno = if (esDominical) FACTOR_EXTRA_NOCTURNA_DOMINICAL else FACTOR_EXTRA_NOCTURNA_HABIL

    val pagoExtrasDiurnas = horasDiurnas * valorHora * factorDiurno
    val pagoExtrasNocturnas = horasNocturnas * valorHora * factorNocturno
    val totalHorasExtra = pagoExtrasDiurnas + pagoExtrasNocturnas

    // Regla 3 — Ingreso Base de Cotización (el auxilio de transporte no hace parte)
    val ibc = salarioBasico + totalHorasExtra

    // Regla 4 — auxilio de transporte
    val auxilioTransporte = if (salarioBasico <= 2 * SMMLV_2026 && !transporteEmpresa) {
        AUX_TRANSPORTE_2026
    } else {
        0.0
    }

    // Regla 5 — total devengado
    val totalDevengado = ibc + auxilioTransporte

    // Regla 6 — deducciones de ley
    val aporteSalud = ibc * PORC_SALUD
    val aportePension = ibc * PORC_PENSION
    val fondoSolidaridad = if (ibc >= 4 * SMMLV_2026) ibc * PORC_FSP else 0.0
    val totalDeducciones = aporteSalud + aportePension + fondoSolidaridad

    // Regla 7 — salario neto
    val salarioNeto = totalDevengado - totalDeducciones

    // Regla 8 — rango salarial
    val rango = clasificarRango(salarioBasico)

    return ResultadoNomina(
        valorHora = valorHora,
        totalHorasExtra = totalHorasExtra,
        auxilioTransporte = auxilioTransporte,
        totalDevengado = totalDevengado,
        aporteSalud = aporteSalud,
        aportePension = aportePension,
        fondoSolidaridad = fondoSolidaridad,
        totalDeducciones = totalDeducciones,
        salarioNeto = salarioNeto,
        rango = rango
    )
}

/**
 * Permite conservar el resultado al rotar el dispositivo.
 */
val ResultadoNominaSaver: Saver<ResultadoNomina?, List<Double>> = Saver(
    save = { resultado ->
        resultado?.let { r ->
            listOf(
                r.valorHora,
                r.totalHorasExtra,
                r.auxilioTransporte,
                r.totalDevengado,
                r.aporteSalud,
                r.aportePension,
                r.fondoSolidaridad,
                r.totalDeducciones,
                r.salarioNeto,
                r.rango.ordinal.toDouble()
            )
        }
    },
    restore = { datos ->
        ResultadoNomina(
            valorHora = datos[0],
            totalHorasExtra = datos[1],
            auxilioTransporte = datos[2],
            totalDevengado = datos[3],
            aporteSalud = datos[4],
            aportePension = datos[5],
            fondoSolidaridad = datos[6],
            totalDeducciones = datos[7],
            salarioNeto = datos[8],
            rango = RangoSalarial.entries[datos[9].toInt()]
        )
    }
)

/**
 * F8 — Formato de moneda colombiano
 */
fun Double.toCopCurrency(): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(this)

/**
 * Funcionalidad opcional — cantidad de salarios mínimos que representa el valor.
 */
fun Double.toSmmlv(): String = String.format(Locale("es", "CO"), "%.2f", this / SMMLV_2026)
