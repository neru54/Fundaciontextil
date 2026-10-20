package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.validation.ApplicationValidator
import com.example.ui.components.FormErrorBanner
import com.example.ui.components.ValidatedTextField
import com.example.ui.theme.NegroCarbon
import com.example.ui.theme.TurquesaOscuro
import com.example.ui.theme.TurquesaTextil
import com.example.ui.viewmodel.ApplicationFormState

/** 38990 -> "$38.990" */
fun formatClp(amount: Int): String = "$" + "%,d".format(amount).replace(',', '.')

@Composable
fun ApplicationDialog(
    form: ApplicationFormState,
    onMotivationChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    val announcement = form.announcement ?: return
    AlertDialog(
        onDismissRequest = { if (!form.isSubmitting) onDismiss() },
        icon = { Icon(Icons.Default.EditNote, contentDescription = null, tint = TurquesaTextil) },
        title = { Text("Postular a la Convocatoria", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(announcement.title, fontWeight = FontWeight.SemiBold, color = TurquesaOscuro, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📅 ${announcement.date}", fontSize = 12.sp, color = NegroCarbon.copy(alpha = 0.8f))
                }
                Text(
                    "Cupos disponibles: ${announcement.availableSpots}",
                    fontSize = 12.sp,
                    color = NegroCarbon.copy(alpha = 0.8f)
                )

                form.generalError?.let { FormErrorBanner(it) }

                ValidatedTextField(
                    field = form.motivation,
                    onValueChange = onMotivationChange,
                    label = "¿Por qué quieres participar? *",
                    placeholder = "Cuéntanos qué productos llevarías o qué esperas aprender...",
                    helperText = "Entre ${ApplicationValidator.MOTIVATION_MIN} y ${ApplicationValidator.MOTIVATION_MAX} caracteres",
                    maxLength = ApplicationValidator.MOTIVATION_MAX,
                    singleLine = false,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !form.isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil)
            ) {
                if (form.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviando...")
                } else {
                    Text("Enviar Postulación")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !form.isSubmitting) {
                Text("Cancelar")
            }
        }
    )
}
