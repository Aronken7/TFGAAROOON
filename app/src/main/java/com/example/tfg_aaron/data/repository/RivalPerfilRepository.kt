package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.RivalPerfilDao
import com.example.tfg_aaron.data.local.entities.RivalPerfilEntity
import kotlinx.coroutines.flow.Flow

class RivalPerfilRepository(private val dao: RivalPerfilDao) {
    fun getByEntrenador(entrenadorId: Int): Flow<List<RivalPerfilEntity>> = dao.getByEntrenador(entrenadorId)
    suspend fun getById(id: Int): RivalPerfilEntity? = dao.getById(id)
    suspend fun insert(entity: RivalPerfilEntity): Long = dao.insert(entity)
    suspend fun update(entity: RivalPerfilEntity) = dao.update(entity)
    suspend fun deleteById(id: Int) = dao.deleteById(id)
}
