package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.LesionDao
import com.example.tfg_aaron.data.local.entities.LesionEntity
import kotlinx.coroutines.flow.Flow

class LesionRepository(private val dao: LesionDao) {
    fun getByEntrenador(entrenadorId: Int): Flow<List<LesionEntity>> = dao.getByEntrenador(entrenadorId)
    fun getByJugadora(jugadoraId: Int): Flow<List<LesionEntity>> = dao.getByJugadora(jugadoraId)
    fun getActivasByEntrenador(entrenadorId: Int): Flow<List<LesionEntity>> = dao.getActivasByEntrenador(entrenadorId)
    suspend fun insert(entity: LesionEntity): Long = dao.insert(entity)
    suspend fun update(entity: LesionEntity) = dao.update(entity)
    suspend fun deleteById(id: Int) = dao.deleteById(id)
}
