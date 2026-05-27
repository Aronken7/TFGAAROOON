package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.SesionTiroDao
import com.example.tfg_aaron.data.local.entities.SesionTiroEntity
import kotlinx.coroutines.flow.Flow

class SesionTiroRepository(private val dao: SesionTiroDao) {
    fun getByEntrenador(entrenadorId: Int): Flow<List<SesionTiroEntity>> = dao.getByEntrenador(entrenadorId)
    fun getByJugadora(entrenadorId: Int, jugadoraId: Int): Flow<List<SesionTiroEntity>> = dao.getByJugadora(entrenadorId, jugadoraId)
    suspend fun insert(sesion: SesionTiroEntity): Long = dao.insert(sesion)
    suspend fun delete(sesion: SesionTiroEntity) = dao.delete(sesion)
    suspend fun deleteById(id: Int) = dao.deleteById(id)
    suspend fun getById(id: Int): SesionTiroEntity? = dao.getById(id)
}
