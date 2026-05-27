package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.WellnessDiarioDao
import com.example.tfg_aaron.data.local.entities.WellnessDiarioEntity
import kotlinx.coroutines.flow.Flow

class WellnessDiarioRepository(private val dao: WellnessDiarioDao) {
    fun getByJugadora(jugadoraId: Int): Flow<List<WellnessDiarioEntity>> = dao.getByJugadora(jugadoraId)
    fun getByEntrenador(entrenadorId: Int): Flow<List<WellnessDiarioEntity>> = dao.getByEntrenador(entrenadorId)
    fun getByEntrenadorSince(entrenadorId: Int, desde: Long): Flow<List<WellnessDiarioEntity>> = dao.getByEntrenadorSince(entrenadorId, desde)
    suspend fun insert(entity: WellnessDiarioEntity): Long = dao.insert(entity)
    suspend fun update(entity: WellnessDiarioEntity) = dao.update(entity)
    suspend fun deleteById(id: Int) = dao.deleteById(id)
}
