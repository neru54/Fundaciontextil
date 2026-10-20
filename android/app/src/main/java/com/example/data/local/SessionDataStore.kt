package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session_prefs")

/** Guarda qué socia tiene la sesión iniciada (solo su id, nunca la contraseña). */
class SessionDataStore(private val context: Context) {

    private object Keys {
        val LOGGED_PARTNER_ID = longPreferencesKey("logged_partner_id")
    }

    /** Id de la socia con sesión activa, o null si se navega como visitante. */
    val loggedPartnerId: Flow<Long?> =
        context.sessionDataStore.data.map { prefs -> prefs[Keys.LOGGED_PARTNER_ID] }

    suspend fun setLoggedPartner(partnerId: Long?) {
        context.sessionDataStore.edit { prefs ->
            if (partnerId == null) prefs.remove(Keys.LOGGED_PARTNER_ID)
            else prefs[Keys.LOGGED_PARTNER_ID] = partnerId
        }
    }
}
