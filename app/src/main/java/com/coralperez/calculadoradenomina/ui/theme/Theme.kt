package com.coralperez.calculadoradenomina.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AmarilloOro,
    onPrimary = AmarilloOroOscuro,
    secondary = AzulColombiaClaro,
    onSecondary = AzulColombia,
    secondaryContainer = AzulColombia,
    onSecondaryContainer = AzulColombiaClaro,
    tertiary = VerdeBogotaClaro,
    onTertiary = VerdeBogota
)

private val LightColorScheme = lightColorScheme(
    primary = AzulColombia,
    onPrimary = Color.White,
    primaryContainer = AzulColombiaClaro,
    onPrimaryContainer = AzulColombia,
    secondary = AmarilloOroOscuro,
    onSecondary = AmarilloOro,
    secondaryContainer = AmarilloOro,
    onSecondaryContainer = AmarilloOroOscuro,
    tertiary = VerdeBogota,
    onTertiary = Color.White,
    tertiaryContainer = VerdeBogotaClaro,
    onTertiaryContainer = VerdeBogota
)

@Composable
fun CalculadoraDeNominaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
