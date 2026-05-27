package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.ObjetivoDao
import com.example.tfg_aaron.data.local.entities.ObjetivoEntity
import kotlinx.coroutines.flow.Flow

class ObjetivoRepository(private val dao: ObjetivoDao) {
    fun getByEntrenador(entrenadorId: Int): Flow<List<ObjetivoEntity>> = dao.getByEntrenador(entrenadorId)
    fun getByJugadora(jugadoraId: Int): Flow<List<ObjetivoEntity>> = dao.getByJugadora(jugadoraId)
    suspend fun insert(entity: ObjetivoEntity): Long = dao.insert(entity)
    suspend fun update(entity: ObjetivoEntity) = dao.update(entity)
    suspend fun deleteById(id: Int) = dao.deleteById(id)
}
