package com.example.domain.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

/**
 * Notificaciones nativas de Android para avisar a la socia de nuevas consultas.
 * Al tocar la notificación se abre directamente la conversación correspondiente.
 */
object NotificationHelper {
    const val CHANNEL_ID = "channel_consultas_clientas"
    const val EXTRA_CHAT_ID = "extra_chat_id"
    private const val CHANNEL_NAME = "Consultas de Clientas"
    private const val CHANNEL_DESC = "Avisos cuando una clienta pregunta por tus prendas"
    private const val BRAND_COLOR = 0xFF00A896.toInt()

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    /**
     * Muestra la notificación de una consulta. Si el permiso no está concedido no hace nada
     * (la pantalla se encarga de pedirlo antes con NotificationPermissionGate).
     */
    fun showClientMessageNotification(
        context: Context,
        notificationId: Int,
        clientName: String,
        messagePreview: String,
        productTitle: String? = null,
        chatId: Long? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        createNotificationChannel(context)

        // Al tocarla abre la app en el chat (MainActivity lee EXTRA_CHAT_ID)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (chatId != null) putExtra(EXTRA_CHAT_ID, chatId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (productTitle != null) "Consulta de $clientName sobre $productTitle"
        else "Nuevo mensaje de $clientName"

        // Color de acento sin "colorized": el sistema usa su fondo estándar y el texto mantiene contraste
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_textil)
            .setColor(BRAND_COLOR)
            .setColorized(false)
            .setContentTitle(title)
            .setContentText(messagePreview)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(messagePreview)
                    .setSummaryText("Fundación Textil Circular")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // El permiso fue revocado mientras la app estaba abierta
        }
    }
}
