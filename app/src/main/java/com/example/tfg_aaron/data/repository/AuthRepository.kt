package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.EntrenadorDao
import com.example.tfg_aaron.data.local.database.AppDatabase
import com.example.tfg_aaron.data.local.entities.EntrenadorEntity
import com.example.tfg_aaron.data.network.RetrofitClient
import com.example.tfg_aaron.data.network.SyncManager
import com.example.tfg_aaron.data.network.dto.LoginRequest
import com.example.tfg_aaron.data.network.dto.RegisterRequest
import com.example.tfg_aaron.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AuthRepository(
    private val entrenadorDao: EntrenadorDao,
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) {

    suspend fun login(email: String, password: String): Result<EntrenadorEntity> {
        return try {
            val response = RetrofitClient.api.login(LoginRequest(email.trim().lowercase(), password))
            RetrofitClient.setToken(response.token)
            val newId = response.entrenadorId.toInt()

            // Si cambia de cuenta, limpiar datos locales del anterior usuario
            val previousId = userPreferences.entrenadorId.first()
            if (previousId != -1 && previousId != newId) {
                withContext(Dispatchers.IO) { database.clearAllTables() }
            }

            userPreferences.saveSession(newId, response.nombre, response.nombreEquipo ?: "", response.email, response.token)
            val entity = EntrenadorEntity(
                id = newId,
                nombre = response.nombre,
                email = response.email,
                password = "",
                equipoNombre = response.nombreEquipo ?: ""
            )
            withContext(Dispatchers.IO) { entrenadorDao.insert(entity) }
            // Sync all data from server to local DB
            try { withContext(Dispatchers.IO) { SyncManager(database).syncAll(newId) } } catch (_: Exception) {}
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(Exception("Email o contraseña incorrectos"))
        }
    }

    suspend fun register(
        nombre: String,
        email: String,
        password: String,
        equipoNombre: String
    ): Result<EntrenadorEntity> {
        return try {
            val response = RetrofitClient.api.register(
                RegisterRequest(nombre.trim(), email.trim().lowercase(), password, equipoNombre.trim())
            )
            RetrofitClient.setToken(response.token)
            val id = response.entrenadorId.toInt()

            // Limpiar datos locales de cualquier sesión anterior
            withContext(Dispatchers.IO) { database.clearAllTables() }

            userPreferences.saveSession(id, response.nombre, response.nombreEquipo ?: "", response.email, response.token)
            val entity = EntrenadorEntity(
                id = id,
                nombre = response.nombre,
                email = response.email,
                password = "",
                equipoNombre = response.nombreEquipo ?: ""
            )
            withContext(Dispatchers.IO) { entrenadorDao.insert(entity) }
            // No data to sync on fresh register, but call anyway for consistency
            try { withContext(Dispatchers.IO) { SyncManager(database).syncAll(id) } } catch (_: Exception) {}
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Error al registrar"))
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) { database.clearAllTables() }
        RetrofitClient.clearToken()
        userPreferences.clearSession()
    }

    suspend fun updatePerfil(id: Int, nombre: String, equipoNombre: String): Result<Unit> {
        return try {
            userPreferences.saveSession(id, nombre.trim(), equipoNombre.trim(), "")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
