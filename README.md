# Calculadora de Nómina

**Estudiante:** Coral Pérez
**Curso:** [Nombre del curso] — [Nombre de la institución]

## Descripción

Aplicación Android (Kotlin + Jetpack Compose) que permite a un trabajador colombiano
estimar el pago mensual que recibirá. A partir del salario básico y de las horas extra
trabajadas en el mes, la aplicación calcula el valor de las horas extra, determina el
derecho al auxilio de transporte, aplica las deducciones de ley (salud, pensión y Fondo
de Solidaridad Pensional) y muestra el salario neto a recibir con el desglose de cada
concepto.

El modelo de cálculo es **simplificado** con fines académicos, basado en la normativa
vigente en septiembre de 2026. No incluye retención en la fuente, recargos nocturnos
ordinarios ni prestaciones sociales.

## Funcionalidades

| Código | Funcionalidad |
|--------|---------------|
| F1–F3 | Campos de salario básico y horas extra (diurnas y nocturnas) con teclado numérico y `imeAction` "Siguiente"/"Listo" |
| F4–F5 | Interruptores de domingo o festivo y de transporte suministrado por la empresa |
| F6 | Botón **Calcular** que ejecuta `calcularNomina()` |
| F7 | Desglose de devengados, deducciones y salario neto |
| F8 | Formato de moneda colombiana (`$ 2.009.505`) |
| F9 | Imagen según el rango salarial, con `contentDescription` |
| F10 | Validaciones con `isError` en el campo afectado y mensaje en pantalla |
| F11 | Botón **Limpiar** que reinicia el formulario |
| F12 | Textos en `strings.xml`, `@Preview` y columna con desplazamiento vertical |

Funcionalidades opcionales implementadas: tema de colores propio con soporte de modo
oscuro, uso de `rememberSaveable` (los datos y el resultado se conservan al rotar) y
equivalencia del salario neto en salarios mínimos (SMMLV).

## Reglas de negocio

Constantes declaradas en `NominaCalculator.kt`:

| Constante | Valor | Fuente |
|-----------|-------|--------|
| SMMLV 2026 | $1.750.905 | Decreto 1469 de 2025 |
| Auxilio de transporte 2026 | $249.095 | Decreto 1470 de 2025 |
| Horas ordinarias mensuales | 210 | Ley 2101 de 2021 |
| Aporte a salud | 4 % | Ley 100 de 1993 |
| Aporte a pensión | 4 % | Ley 100 de 1993 |
| Fondo de Solidaridad Pensional | 1 % | Ley 797 de 2003 |

Factores de las horas extra:

| Tipo | Día hábil | Domingo o festivo |
|------|-----------|-------------------|
| Extra diurna | 1.25 | 2.15 |
| Extra nocturna | 1.75 | 2.65 |

## Estructura del proyecto

```
app/src/main/java/com/coralperez/calculadoradenomina/
    MainActivity.kt          -> Pantalla principal y composables reutilizables
                                 (CampoNumerico, FilaInterruptor, FilaDesglose, DesgloseNomina)
    NominaCalculator.kt      -> Constantes, ResultadoNomina, RangoSalarial,
                                 calcularNomina() y clasificarRango()
    ui/theme/                -> Tema de colores y tipografía
app/src/main/res/
    drawable/                -> Iconos vectoriales de los tres rangos
    values/strings.xml       -> Todos los textos de la aplicación
app/src/test/                -> Pruebas unitarias de los casos A a E
```

## Cómo compilar y ejecutar

1. Abrir el proyecto en Android Studio.
2. `./gradlew :app:assembleDebug` para generar el APK de depuración.
3. `./gradlew :app:testDebugUnitTest` para correr las pruebas de los casos A a E.
4. `./gradlew :app:installDebug` con un dispositivo o emulador conectado.

## Casos de verificación

Las pruebas unitarias en `app/src/test/java/.../NominaCalculatorTest.kt` validan los
cuatro casos numéricos y las validaciones, con una tolerancia de $1 por redondeo.

### Caso A — Salario mínimo con horas extra en día hábil

Entrada: $1.750.905; 10 diurnas; 4 nocturnas; domingo desactivado; transporte desactivado.

| Concepto | Valor |
|----------|-------|
| Valor hora ordinaria | $ 8.338 |
| Horas extra | $ 162.584 |
| Auxilio de transporte | $ 249.095 |
| Total devengado | $ 2.162.584 |
| Salud | $ 76.540 |
| Pensión | $ 76.540 |
| Fondo de Solidaridad | $ 0 |
| Salario neto | $ 2.009.505 |
| Rango | Rango 1 |

### Caso B — Horas extra en domingo o festivo

Entrada: $2.500.000; 6 diurnas; 2 nocturnas; domingo activado; transporte desactivado.

| Concepto | Valor |
|----------|-------|
| Valor hora ordinaria | $ 11.905 |
| Horas extra | $ 216.667 |
| Auxilio de transporte | $ 249.095 |
| Total devengado | $ 2.965.762 |
| Salud | $ 108.667 |
| Pensión | $ 108.667 |
| Fondo de Solidaridad | $ 0 |
| Salario neto | $ 2.748.428 |
| Rango | Rango 1 |

### Caso C — Transporte suministrado por la empresa

Entrada: $3.000.000; 5 diurnas; 0 nocturnas; domingo desactivado; transporte activado.

| Concepto | Valor |
|----------|-------|
| Valor hora ordinaria | $ 14.286 |
| Horas extra | $ 89.286 |
| Auxilio de transporte | $ 0 |
| Total devengado | $ 3.089.286 |
| Salud | $ 123.571 |
| Pensión | $ 123.571 |
| Fondo de Solidaridad | $ 0 |
| Salario neto | $ 2.842.143 |
| Rango | Rango 1 |

### Caso D — Salario alto con Fondo de Solidaridad Pensional

Entrada: $8.000.000; sin horas extra; ambos interruptores desactivados.

| Concepto | Valor |
|----------|-------|
| Valor hora ordinaria | $ 38.095 |
| Horas extra | $ 0 |
| Auxilio de transporte | $ 0 |
| Total devengado | $ 8.000.000 |
| Salud | $ 320.000 |
| Pensión | $ 320.000 |
| Fondo de Solidaridad | $ 80.000 |
| Salario neto | $ 7.280.000 |
| Rango | Rango 3 |

### Caso E — Validaciones

| Entrada | Resultado |
|---------|-----------|
| Salario vacío | Mensaje "Ingrese un salario válido" |
| Salario $1.000.000 | Mensaje de salario inferior al mínimo |
| $2.000.000 con 30 diurnas y 20 nocturnas | Mensaje de exceso de horas extra |
| $2.000.000 con horas extra vacías | Cálculo correcto con 0 horas extra |

## Capturas de pantalla

> Reemplazar las rutas de abajo con las imágenes reales antes de entregar.

| Caso | Captura |
|------|---------|
| Caso A | `capturas/caso_a.png` |
| Caso B | `capturas/caso_b.png` |
| Caso C | `capturas/caso_c.png` |
| Caso D | `capturas/caso_d.png` |
| Caso E (validación) | `capturas/caso_e_validacion.png` |

## Evidencias de la Parte 1

Las capturas de los codelabs (Dice Roller y Tip Time) están en la carpeta
[`evidencias/`](evidencias/).

## Estructura de commits

El historial de commits documenta la avance paso a paso: lógica de negocio primero,
después la interfaz mínima, luego los campos, los interruptores, las validaciones y
finalmente el diseño visual y las funcionalidades opcionales.
