package cl.textilcircular.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cl.textilcircular.app.ui.theme.VerdeOk
import kotlin.math.roundToInt

/**
 * Animación de "sacudida" para campos inválidos (RNF07). Se dispara cuando cambia [disparador]
 * (un contador de intentos de envío) y el campo tiene error.
 */
@Composable
fun Modifier.sacudirSiError(error: String?, disparador: Int): Modifier {
    val desplazamiento = remember { Animatable(0f) }
    LaunchedEffect(disparador) {
        if (disparador > 0 && error != null) {
            for (x in listOf(14f, -12f, 9f, -6f, 3f, 0f)) desplazamiento.animateTo(x, tween(45))
        }
    }
    return this.offset { IntOffset(desplazamiento.value.roundToInt(), 0) }
}

/** Mensaje de error bajo un campo, con ícono y aparición animada. */
@Composable
fun MensajeError(error: String?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp, start = 4.dp)) {
            Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * Campo de texto validado (RF01.2): ícono de error o de "correcto", mensaje bajo el campo y
 * sacudida al intentar avanzar con errores. La regla vive fuera del componente (RNF08).
 */
@Composable
fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    error: String?,
    disparador: Int,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    ayuda: String? = null,
    teclado: KeyboardOptions = KeyboardOptions.Default,
    esPassword: Boolean = false,
    lineas: Int = 1,
    maxCaracteres: Int? = null,
    prefijo: String? = null,
    habilitado: Boolean = true,
    mostrarOk: Boolean = true
) {
    var verPassword by rememberSaveable { mutableStateOf(false) }
    val tocado = valor.isNotEmpty()
    Column(modifier.fillMaxWidth().sacudirSiError(error, disparador)) {
        OutlinedTextField(
            value = valor,
            onValueChange = { nuevo -> if (maxCaracteres == null || nuevo.length <= maxCaracteres) alCambiar(nuevo) },
            label = { Text(etiqueta) },
            leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
            prefix = prefijo?.let { { Text(it) } },
            trailingIcon = {
                when {
                    esPassword -> IconButton(onClick = { verPassword = !verPassword }) {
                        Icon(
                            if (verPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (verPassword) "Ocultar contraseña" else "Ver contraseña"
                        )
                    }
                    error != null -> Icon(Icons.Filled.Error, contentDescription = "Dato con error", tint = MaterialTheme.colorScheme.error)
                    tocado && mostrarOk -> Icon(Icons.Filled.CheckCircle, contentDescription = "Dato correcto", tint = VerdeOk)
                }
            },
            isError = error != null,
            singleLine = lineas == 1,
            minLines = lineas,
            enabled = habilitado,
            keyboardOptions = teclado,
            visualTransformation = if (esPassword && !verPassword) PasswordVisualTransformation() else VisualTransformation.None,
            supportingText = when {
                error == null && ayuda != null -> { { Text(ayuda) } }
                error == null && maxCaracteres != null && lineas > 1 -> { { Text("${valor.length}/$maxCaracteres") } }
                else -> null
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { if (error != null) this.error(error) }
        )
        MensajeError(error)
    }
}

/** Lista desplegable cerrada (RNF06): nunca texto libre. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CampoLista(
    etiqueta: String,
    opciones: List<T>,
    seleccion: T?,
    alElegir: (T) -> Unit,
    error: String?,
    disparador: Int,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    texto: (T) -> String = { it.toString() },
    habilitado: Boolean = true,
    ayuda: String? = null
) {
    var abierto by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth().sacudirSiError(error, disparador)) {
        ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { if (habilitado) abierto = it }) {
            OutlinedTextField(
                value = seleccion?.let(texto).orEmpty(),
                onValueChange = {},
                readOnly = true,
                enabled = habilitado,
                label = { Text(etiqueta) },
                leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
                trailingIcon = {
                    if (error != null) Icon(Icons.Filled.Error, contentDescription = "Dato con error", tint = MaterialTheme.colorScheme.error)
                    else ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto)
                },
                supportingText = if (error == null && ayuda != null) { { Text(ayuda) } } else null,
                isError = error != null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, habilitado)
            )
            ExposedDropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(texto(opcion)) },
                        leadingIcon = if (opcion == seleccion) { { Icon(Icons.Filled.Check, contentDescription = null) } } else null,
                        onClick = {
                            alElegir(opcion)
                            abierto = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        MensajeError(error)
    }
}

/** Selección múltiple con chips y tope (RN02: hasta 2 rubros). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> SelectorChips(
    opciones: List<T>,
    seleccionados: Set<T>,
    alAlternar: (T) -> Unit,
    error: String?,
    disparador: Int,
    texto: (T) -> String,
    modifier: Modifier = Modifier,
    maximo: Int = Int.MAX_VALUE
) {
    Column(modifier.fillMaxWidth().sacudirSiError(error, disparador)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            opciones.forEach { opcion ->
                val marcado = opcion in seleccionados
                val bloqueado = !marcado && seleccionados.size >= maximo
                FilterChip(
                    selected = marcado,
                    onClick = { alAlternar(opcion) },
                    enabled = !bloqueado,
                    label = { Text(texto(opcion)) },
                    leadingIcon = if (marcado) { { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) } } else null
                )
            }
        }
        MensajeError(error)
    }
}

/** Casilla con texto, accesible como un solo elemento. */
@Composable
fun CasillaVerificacion(
    marcada: Boolean,
    alCambiar: (Boolean) -> Unit,
    texto: String,
    error: String? = null,
    disparador: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(modifier.sacudirSiError(error, disparador)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = marcada, role = Role.Checkbox, onValueChange = alCambiar)
                .semantics { contentDescription = texto }
                .padding(vertical = 4.dp)
        ) {
            Checkbox(checked = marcada, onCheckedChange = null)
            Spacer(Modifier.width(8.dp))
            Text(texto, style = MaterialTheme.typography.bodyMedium)
        }
        MensajeError(error)
    }
}

val TecladoEmail = KeyboardOptions(keyboardType = KeyboardType.Email)
val TecladoNumero = KeyboardOptions(keyboardType = KeyboardType.Number)
val TecladoTelefono = KeyboardOptions(keyboardType = KeyboardType.Phone)
val TecladoPassword = KeyboardOptions(keyboardType = KeyboardType.Password)
