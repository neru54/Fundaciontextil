package cl.textilcircular.app.platform

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
import cl.textilcircular.app.MainActivity
import cl.textilcircular.app.R
import cl.textilcircular.app.data.repository.Notificador

/**
 * Recurso nativo 2: notificaciones (RF04.1, RNF09). Sin servidor se lanzan como notificaciones
 * locales usando el mismo canal que usaría una push (sección 1.1 del ERS).
 */
class NotificationHelper(private val context: Context) : Notificador {

    fun crearCanales() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CANAL_MENSAJES, "Mensajes de clientas", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos cuando una clienta te escribe"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CANAL_NOVEDADES, "Novedades de la Fundación", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Avisos, eventos y noticias de la Fundación"
            }
        )
    }

    fun tienePermiso(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    override fun nuevaConsulta(conversacionId: Long, clienteNombre: String, texto: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CONVERSACION_ID, conversacionId)
        }
        val pending = PendingIntent.getActivity(
            context, conversacionId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        mostrar(
            id = conversacionId.toInt(),
            NotificationCompat.Builder(context, CANAL_MENSAJES)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setContentTitle("Nueva consulta de $clienteNombre")
                .setContentText(texto)
                .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pending)
                .setAutoCancel(true)
        )
    }

    override fun nuevaNovedad(titulo: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ABRIR_NOVEDADES, true)
        }
        val pending = PendingIntent.getActivity(
            context, ID_NOVEDADES, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        mostrar(
            id = ID_NOVEDADES,
            NotificationCompat.Builder(context, CANAL_NOVEDADES)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setContentTitle("Novedad de la Fundación")
                .setContentText(titulo)
                .setContentIntent(pending)
                .setAutoCancel(true)
        )
    }

    private fun mostrar(id: Int, builder: NotificationCompat.Builder) {
        // Si la usuaria no dio permiso, la app sigue funcionando y el aviso queda en la bandeja interna.
        if (!tienePermiso()) return
        try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        } catch (_: SecurityException) {
            // El permiso se revocó entre la verificación y el aviso.
        }
    }

    companion object {
        const val CANAL_MENSAJES = "mensajes"
        const val CANAL_NOVEDADES = "novedades"
        const val EXTRA_CONVERSACION_ID = "conversacion_id"
        const val EXTRA_ABRIR_NOVEDADES = "abrir_novedades"
        private const val ID_NOVEDADES = 9_000
    }
}
