package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.AsistenciaDao
import com.example.tfg_aaron.data.local.entities.AsistenciaEntity
import kotlinx.coroutines.flow.Flow

class AsistenciaRepository(private val dao: AsistenciaDao) {
    fun getByEvento(tipo: String, referenciaId: Int): Flow<List<AsistenciaEntity>> = dao.getByEvento(tipo, referenciaId)
    fun getByJugadora(jugadoraId: Int, entrenadorId: Int): Flow<List<AsistenciaEntity>> = dao.getByJugadora(jugadoraId, entrenadorId)
    fun getByEntrenador(entrenadorId: Int): Flow<List<AsistenciaEntity>> = dao.getByEntrenador(entrenadorId)
    suspend fun insert(entity: AsistenciaEntity): Long = dao.insert(entity)
    suspend fun update(entity: AsistenciaEntity) = dao.update(entity)
    suspend fun deleteByEvento(tipo: String, referenciaId: Int) = dao.deleteByEvento(tipo, referenciaId)
    suspend fun countByEstado(tipo: String, referenciaId: Int, estado: String): Int = dao.countByEstado(tipo, referenciaId, estado)
}
