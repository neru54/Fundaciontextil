package cl.textilcircular.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.textilcircular.app.domain.model.Clienta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sesion")

/**
 * Sesión persistente (RF01.9, RNF11): la socia queda conectada para operar sin conexión.
 * También recuerda el registro simple de la clienta (RF06.6).
 */
class SessionStore(private val context: Context) {

    private val sociaIdKey = longPreferencesKey("socia_id")
    private val clientaNombreKey = stringPreferencesKey("clienta_nombre")
    private val clientaEmailKey = stringPreferencesKey("clienta_email")

    val sociaId: Flow<Long?> = context.dataStore.data.map { it[sociaIdKey] }

    val clienta: Flow<Clienta?> = context.dataStore.data.map { prefs ->
        val nombre = prefs[clientaNombreKey]
        val email = prefs[clientaEmailKey]
        if (nombre != null && email != null) Clienta(nombre, email) else null
    }

    suspend fun sociaIdActual(): Long? = sociaId.first()

    suspend fun iniciar(sociaId: Long) {
        context.dataStore.edit { it[sociaIdKey] = sociaId }
    }

    suspend fun cerrar() {
        context.dataStore.edit { it.remove(sociaIdKey) }
    }

    suspend fun guardarClienta(clienta: Clienta) {
        context.dataStore.edit {
            it[clientaNombreKey] = clienta.nombre
            it[clientaEmailKey] = clienta.email
        }
    }
}
