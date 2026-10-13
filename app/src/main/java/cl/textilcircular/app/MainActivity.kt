package cl.textilcircular.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cl.textilcircular.app.platform.NotificationHelper
import cl.textilcircular.app.ui.navigation.AppRaiz
import cl.textilcircular.app.ui.navigation.DestinoExterno
import cl.textilcircular.app.ui.theme.TextilTheme

class MainActivity : ComponentActivity() {

    private var destinoExterno by mutableStateOf<DestinoExterno?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) leerDestino(intent)
        setContent {
            TextilTheme {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    AppRaiz(destinoExterno = destinoExterno, alConsumirDestino = { destinoExterno = null })
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        leerDestino(intent)
    }

    /** Destino al tocar una notificación (RF04.1, RF04.5). */
    private fun leerDestino(intent: Intent?) {
        val conversacion = intent?.getLongExtra(NotificationHelper.EXTRA_CONVERSACION_ID, -1L) ?: -1L
        destinoExterno = when {
            conversacion > 0 -> DestinoExterno.Chat(conversacion)
            intent?.getBooleanExtra(NotificationHelper.EXTRA_ABRIR_NOVEDADES, false) == true -> DestinoExterno.Novedades
            else -> destinoExterno
        }
    }
}
