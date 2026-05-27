package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.SkillRatingDao
import com.example.tfg_aaron.data.local.entities.SkillRatingEntity
import kotlinx.coroutines.flow.Flow

class SkillRatingRepository(private val dao: SkillRatingDao) {
    fun getByJugadora(jugadoraId: Int): Flow<List<SkillRatingEntity>> = dao.getByJugadora(jugadoraId)
    fun getByEntrenador(entrenadorId: Int): Flow<List<SkillRatingEntity>> = dao.getByEntrenador(entrenadorId)
    fun getLatestByJugadora(jugadoraId: Int): Flow<SkillRatingEntity?> = dao.getLatestByJugadora(jugadoraId)
    suspend fun insert(entity: SkillRatingEntity): Long = dao.insert(entity)
    suspend fun update(entity: SkillRatingEntity) = dao.update(entity)
    suspend fun deleteById(id: Int) = dao.deleteById(id)
}
