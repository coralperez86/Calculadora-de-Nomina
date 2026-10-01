package com.coralperez.calculadoradenomina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coralperez.calculadoradenomina.ui.theme.CalculadoraDeNominaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculadoraDeNominaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculadoraNominaApp()
                }
            }
        }
    }
}

@Composable
fun CalculadoraNominaApp() {
    var salarioInput by rememberSaveable { mutableStateOf("") }
    var horasDiurnasInput by rememberSaveable { mutableStateOf("") }
    var horasNocturnasInput by rememberSaveable { mutableStateOf("") }
    var esDominical by rememberSaveable { mutableStateOf(false) }
    var transporteEmpresa by rememberSaveable { mutableStateOf(false) }

    var errorMensaje by rememberSaveable { mutableStateOf<Int?>(null) }
    var campoErrorSalario by rememberSaveable { mutableStateOf(false) }
    var campoErrorHoras by rememberSaveable { mutableStateOf(false) }

    var resultado: ResultadoNomina? by rememberSaveable(stateSaver = ResultadoNominaSaver) {
        mutableStateOf(null)
    }

    val focusManager = LocalFocusManager.current
    val focusRequesterDiurnas = remember { FocusRequester() }
    val focusRequesterNocturnas = remember { FocusRequester() }

    fun ejecutarCalculo() {
        errorMensaje = null
        campoErrorSalario = false
        campoErrorHoras = false

        fun mostrarError(@StringRes mensaje: Int, esCampoSalario: Boolean) {
            errorMensaje = mensaje
            campoErrorSalario = esCampoSalario
            campoErrorHoras = !esCampoSalario
            resultado = null
        }

        val salario = salarioInput.toDoubleOrNull()
        if (salario == null) {
            mostrarError(R.string.error_salario_invalido, esCampoSalario = true)
            return
        }

        if (salario < SMMLV_2026) {
            mostrarError(R.string.error_salario_minimo, esCampoSalario = true)
            return
        }

        val hDiurnas = (if (horasDiurnasInput.isBlank()) "0" else horasDiurnasInput).toDoubleOrNull()
        val hNocturnas = (if (horasNocturnasInput.isBlank()) "0" else horasNocturnasInput).toDoubleOrNull()

        if (hDiurnas == null || hNocturnas == null || hDiurnas < 0 || hNocturnas < 0) {
            mostrarError(R.string.error_horas_invalidas, esCampoSalario = false)
            return
        }

        if (hDiurnas + hNocturnas > MAX_HORAS_EXTRA_MES) {
            mostrarError(R.string.error_horas_exceso, esCampoSalario = false)
            return
        }

        resultado = calcularNomina(
            salarioBasico = salario,
            horasDiurnas = hDiurnas,
            horasNocturnas = hNocturnas,
            esDominical = esDominical,
            transporteEmpresa = transporteEmpresa
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.app_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            CampoNumerico(
                etiqueta = R.string.salario_basico_label,
                valor = salarioInput,
                onValueChange = { salarioInput = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                isError = campoErrorSalario,
                alSiguiente = { focusRequesterDiurnas.requestFocus() }
            )

            CampoNumerico(
                etiqueta = R.string.horas_diurnas_label,
                valor = horasDiurnasInput,
                onValueChange = { horasDiurnasInput = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                isError = campoErrorHoras,
                modifier = Modifier.focusRequester(focusRequesterDiurnas),
                alSiguiente = { focusRequesterNocturnas.requestFocus() }
            )

            CampoNumerico(
                etiqueta = R.string.horas_nocturnas_label,
                valor = horasNocturnasInput,
                onValueChange = { horasNocturnasInput = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                isError = campoErrorHoras,
                modifier = Modifier.focusRequester(focusRequesterNocturnas),
                alTerminar = {
                    focusManager.clearFocus()
                    ejecutarCalculo()
                }
            )

            FilaInterruptor(
                etiqueta = R.string.es_dominical_label,
                checked = esDominical,
                onCheckedChange = {
                    esDominical = it
                    if (resultado != null) ejecutarCalculo()
                }
            )

            FilaInterruptor(
                etiqueta = R.string.transporte_empresa_label,
                checked = transporteEmpresa,
                onCheckedChange = {
                    transporteEmpresa = it
                    if (resultado != null) ejecutarCalculo()
                }
            )

            errorMensaje?.let { resError ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = stringResource(resError),
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        ejecutarCalculo()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.btn_calcular))
                }

                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        salarioInput = ""
                        horasDiurnasInput = ""
                        horasNocturnasInput = ""
                        esDominical = false
                        transporteEmpresa = false
                        errorMensaje = null
                        campoErrorSalario = false
                        campoErrorHoras = false
                        resultado = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.btn_limpiar))
                }
            }

            resultado?.let { res ->
                DesgloseNomina(
                    resultado = res,
                    salarioBasico = salarioInput.toDoubleOrNull() ?: 0.0
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.autor_credito, stringResource(R.string.autor)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.nota_academica),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CampoNumerico(
    @StringRes etiqueta: Int,
    valor: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    alSiguiente: (() -> Unit)? = null,
    alTerminar: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(stringResource(etiqueta)) },
        singleLine = true,
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onNext = { alSiguiente?.invoke() },
            onDone = { alTerminar?.invoke() }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun FilaInterruptor(
    @StringRes etiqueta: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(etiqueta),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun FilaDesglose(concepto: String, valor: String, esNegrita: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = concepto,
            fontWeight = if (esNegrita) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            fontWeight = if (esNegrita) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun DesgloseNomina(resultado: ResultadoNomina, salarioBasico: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = resultado.rango.imagenRes),
                contentDescription = stringResource(id = resultado.rango.contentDescriptionRes),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = stringResource(id = resultado.rango.descripcionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.sec_devengado),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        FilaDesglose(stringResource(R.string.lbl_valor_hora), resultado.valorHora.toCopCurrency())
        FilaDesglose(stringResource(R.string.lbl_salario_basico), salarioBasico.toCopCurrency())
        FilaDesglose(stringResource(R.string.lbl_horas_extra), resultado.totalHorasExtra.toCopCurrency())
        FilaDesglose(stringResource(R.string.lbl_aux_transporte), resultado.auxilioTransporte.toCopCurrency())
        FilaDesglose(
            stringResource(R.string.lbl_total_devengado),
            resultado.totalDevengado.toCopCurrency(),
            esNegrita = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.sec_deducciones),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        FilaDesglose(stringResource(R.string.lbl_salud), resultado.aporteSalud.toCopCurrency())
        FilaDesglose(stringResource(R.string.lbl_pension), resultado.aportePension.toCopCurrency())
        FilaDesglose(stringResource(R.string.lbl_fsp), resultado.fondoSolidaridad.toCopCurrency())
        FilaDesglose(
            stringResource(R.string.lbl_total_deducciones),
            resultado.totalDeducciones.toCopCurrency(),
            esNegrita = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.lbl_salario_neto),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = resultado.salarioNeto.toCopCurrency(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(
                        R.string.lbl_equivale_smmlv,
                        resultado.salarioNeto.toSmmlv()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCalculadoraVacia() {
    CalculadoraDeNominaTheme {
        CalculadoraNominaApp()
    }
}
