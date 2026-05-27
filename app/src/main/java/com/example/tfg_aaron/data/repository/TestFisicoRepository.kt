package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.TestFisicoDao
import com.example.tfg_aaron.data.local.entities.TestFisicoEntity
import kotlinx.coroutines.flow.Flow

class TestFisicoRepository(private val dao: TestFisicoDao) {
    fun getByJugadora(jugadoraId: Int): Flow<List<TestFisicoEntity>> = dao.getByJugadora(jugadoraId)
    fun getByEntrenador(entrenadorId: Int): Flow<List<TestFisicoEntity>> = dao.getByEntrenador(entrenadorId)
    fun getByJugadoraYTipo(jugadoraId: Int, tipo: String): Flow<List<TestFisicoEntity>> = dao.getByJugadoraYTipo(jugadoraId, tipo)
    suspend fun insert(entity: TestFisicoEntity): Long = dao.insert(entity)
    suspend fun update(entity: TestFisicoEntity) = dao.update(entity)
    suspend fun deleteById(id: Int) = dao.deleteById(id)
}
