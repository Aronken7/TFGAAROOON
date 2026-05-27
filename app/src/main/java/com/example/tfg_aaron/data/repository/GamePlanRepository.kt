package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.GamePlanDao
import com.example.tfg_aaron.data.local.entities.GamePlanEntity
import kotlinx.coroutines.flow.Flow

class GamePlanRepository(private val dao: GamePlanDao) {
    fun getByEntrenador(entrenadorId: Int): Flow<List<GamePlanEntity>> = dao.getByEntrenador(entrenadorId)
    suspend fun getById(id: Int): GamePlanEntity? = dao.getById(id)
    suspend fun insert(entity: GamePlanEntity): Long = dao.insert(entity)
    suspend fun update(entity: GamePlanEntity) = dao.update(entity)
    suspend fun deleteById(id: Int) = dao.deleteById(id)
}
