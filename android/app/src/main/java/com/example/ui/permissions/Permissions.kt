package com.example.ui.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import com.example.ui.theme.TurquesaTextil

/** Abre los Ajustes de la app. */
fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun hasPermission(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

/** Ejecuta acciones que requieren permiso de notificaciones. */
class NotificationPermissionGate internal constructor(
    private val isGranted: () -> Boolean,
    private val askWithRationale: (onGranted: () -> Unit) -> Unit
) {
    fun runWithPermission(action: () -> Unit) {
        if (isGranted()) action() else askWithRationale(action)
    }
}

/** Pide POST_NOTIFICATIONS explicando antes su uso; onDenied permite ofrecer Ajustes. */
@Composable
fun rememberNotificationPermissionGate(onDenied: () -> Unit): NotificationPermissionGate {
    val context = LocalContext.current
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showRationale by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val action = pendingAction
        pendingAction = null
        if (granted) action?.invoke() else onDenied()
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
                pendingAction = null
            },
            icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = TurquesaTextil) },
            title = { Text("Activar notificaciones", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Textil Circular usa las notificaciones solo para avisarte cuando una clienta " +
                        "te escribe sobre tus prendas. Puedes desactivarlas cuando quieras en Ajustes."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRationale = false
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil)
                ) { Text("Continuar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    pendingAction = null
                }) { Text("Ahora no") }
            }
        )
    }

    return remember(context) {
        NotificationPermissionGate(
            isGranted = {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            },
            askWithRationale = { action ->
                pendingAction = action
                showRationale = true
            }
        )
    }
}
