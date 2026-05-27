package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.ShotChartDao
import com.example.tfg_aaron.data.local.entities.ShotChartDataEntity
import kotlinx.coroutines.flow.Flow

class ShotChartRepository(private val dao: ShotChartDao) {
    fun getByPartido(partidoId: Int): Flow<List<ShotChartDataEntity>> = dao.getByPartido(partidoId)
    fun getByJugadora(jugadoraId: Int): Flow<List<ShotChartDataEntity>> = dao.getByJugadora(jugadoraId)
    fun getByJugadoraAndEntrenador(jugadoraId: Int, entrenadorId: Int): Flow<List<ShotChartDataEntity>> = dao.getByJugadoraAndEntrenador(jugadoraId, entrenadorId)
    fun getByEntrenador(entrenadorId: Int): Flow<List<ShotChartDataEntity>> = dao.getByEntrenador(entrenadorId)
    suspend fun insert(entity: ShotChartDataEntity): Long = dao.insert(entity)
    suspend fun delete(entity: ShotChartDataEntity) = dao.delete(entity)
    suspend fun deleteById(id: Int) = dao.deleteById(id)
    suspend fun deleteByPartido(partidoId: Int) = dao.deleteByPartido(partidoId)
    fun getBySesion(sesionId: Int) = dao.getBySesion(sesionId)
    suspend fun getBySesionSync(sesionId: Int) = dao.getBySesionSync(sesionId)
}
