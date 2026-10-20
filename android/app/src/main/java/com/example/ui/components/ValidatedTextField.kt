package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TurquesaTextil
import com.example.ui.viewmodel.form.FieldState

/**
 * Campo de texto con retroalimentación visual estándar para toda la app:
 * - borde y texto rojo + ícono de error cuando el valor no es válido
 * - ícono de check verde cuando el valor fue editado y es válido
 * - mensaje de error o de ayuda bajo el campo, y contador de caracteres opcional
 */
@Composable
fun ValidatedTextField(
    field: FieldState,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    placeholder: String? = null,
    helperText: String? = null,
    maxLength: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 4,
    enabled: Boolean = true
) {
    val errorMessage = field.error.takeIf { field.touched }
    val isError = errorMessage != null

    // Animación de "sacudida" cuando el campo pasa de válido a error (ej: al presionar Enviar)
    val shakeOffset = remember { Animatable(0f) }
    var wasError by remember { mutableStateOf(false) }
    LaunchedEffect(isError) {
        if (isError && !wasError) {
            for (target in listOf(12f, -12f, 8f, -8f, 4f, 0f)) {
                shakeOffset.animateTo(target, animationSpec = tween(durationMillis = 45))
            }
        }
        wasError = isError
    }

    // Tipos explícitos para que Compose reconozca los lambdas como @Composable
    val placeholderContent: (@Composable () -> Unit)? =
        if (placeholder != null) { { Text(placeholder) } } else null
    val leadingContent: (@Composable () -> Unit)? =
        if (leadingIcon != null) { { Icon(leadingIcon, contentDescription = null) } } else null
    var passwordVisible by remember { mutableStateOf(false) }
    val trailingContent: (@Composable () -> Unit)? = when {
        // En contraseñas el ícono final permite mostrar/ocultar el texto
        isPassword -> {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = if (isError) MaterialTheme.colorScheme.error else androidx.compose.material3.LocalContentColor.current
                    )
                }
            }
        }
        isError -> { { PopIn { Icon(Icons.Filled.Error, contentDescription = "Campo con error", tint = MaterialTheme.colorScheme.error) } } }
        field.showSuccess -> { { PopIn { Icon(Icons.Filled.CheckCircle, contentDescription = "Campo válido", tint = TurquesaTextil) } } }
        else -> null
    }

    OutlinedTextField(
        value = field.value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = shakeOffset.value }
            .semantics { if (errorMessage != null) error(errorMessage) },
        enabled = enabled,
        label = { Text(label) },
        placeholder = placeholderContent,
        leadingIcon = leadingContent,
        trailingIcon = trailingContent,
        isError = isError,
        supportingText = {
            Row {
                // El mensaje cambia con una transición suave entre ayuda y error
                AnimatedContent(
                    targetState = errorMessage ?: helperText.orEmpty(),
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInVertically(tween(220)) { -it / 2 }) togetherWith
                            fadeOut(tween(120))
                    },
                    modifier = Modifier.weight(1f),
                    label = "supportingText"
                ) { message ->
                    Text(text = message, fontSize = 11.sp)
                }
                if (maxLength != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${field.value.length}/$maxLength", fontSize = 11.sp)
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        singleLine = singleLine,
        maxLines = maxLines
    )
}
