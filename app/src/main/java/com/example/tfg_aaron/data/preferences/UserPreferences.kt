package com.example.tfg_aaron.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val ENTRENADOR_ID = intPreferencesKey("entrenador_id")
        val ENTRENADOR_NOMBRE = stringPreferencesKey("entrenador_nombre")
        val EQUIPO_NOMBRE = stringPreferencesKey("equipo_nombre")
        val ENTRENADOR_EMAIL = stringPreferencesKey("entrenador_email")
        val JWT_TOKEN = stringPreferencesKey("jwt_token")

        // Appearance
        val THEME_MODE = stringPreferencesKey("theme_mode") // "DARK", "LIGHT", "SYSTEM"

        // Notifications
        val NOTIF_PARTIDO = booleanPreferencesKey("notif_partido")
        val NOTIF_ENTRENAMIENTO = booleanPreferencesKey("notif_entrenamiento")
        val NOTIF_RESUMEN = booleanPreferencesKey("notif_resumen")
    }

    val entrenadorId: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[ENTRENADOR_ID] ?: -1  // -1 = no hay sesión activa
    }

    val entrenadorNombre: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ENTRENADOR_NOMBRE] ?: ""
    }

    val equipoNombre: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[EQUIPO_NOMBRE] ?: ""
    }

    val entrenadorEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[ENTRENADOR_EMAIL] ?: ""
    }

    val jwtToken: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[JWT_TOKEN] ?: ""
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "SYSTEM"
    }

    val notifPartido: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIF_PARTIDO] ?: true
    }

    val notifEntrenamiento: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIF_ENTRENAMIENTO] ?: false
    }

    val notifResumen: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIF_RESUMEN] ?: false
    }

    suspend fun saveSession(id: Int, nombre: String, equipo: String, email: String, token: String = "") {
        context.dataStore.edit { prefs ->
            prefs[ENTRENADOR_ID] = id
            prefs[ENTRENADOR_NOMBRE] = nombre
            prefs[EQUIPO_NOMBRE] = equipo
            prefs[ENTRENADOR_EMAIL] = email
            if (token.isNotEmpty()) prefs[JWT_TOKEN] = token
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[THEME_MODE] = mode }
    }

    suspend fun setNotifPartido(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[NOTIF_PARTIDO] = enabled }
    }

    suspend fun setNotifEntrenamiento(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[NOTIF_ENTRENAMIENTO] = enabled }
    }

    suspend fun setNotifResumen(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[NOTIF_RESUMEN] = enabled }
    }
}
