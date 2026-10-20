package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session_prefs")

/** Sesión: solo guarda el id de la socia. */
class SessionDataStore(private val context: Context) {

    private object Keys {
        val LOGGED_PARTNER_ID = longPreferencesKey("logged_partner_id")
    }

    val loggedPartnerId: Flow<Long?> =
        context.sessionDataStore.data.map { prefs -> prefs[Keys.LOGGED_PARTNER_ID] }

    suspend fun setLoggedPartner(partnerId: Long?) {
        context.sessionDataStore.edit { prefs ->
            if (partnerId == null) prefs.remove(Keys.LOGGED_PARTNER_ID)
            else prefs[Keys.LOGGED_PARTNER_ID] = partnerId
        }
    }
}
