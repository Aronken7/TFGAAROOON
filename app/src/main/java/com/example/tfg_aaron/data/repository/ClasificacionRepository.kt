package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.ClasificacionDao
import com.example.tfg_aaron.data.local.entities.ClasificacionEntity
import kotlinx.coroutines.flow.Flow

class ClasificacionRepository(private val dao: ClasificacionDao) {

    fun getByEntrenadorAndTemporada(entrenadorId: Int, temporada: String): Flow<List<ClasificacionEntity>> =
        dao.getByEntrenadorAndTemporada(entrenadorId, temporada)

    fun getTemporadas(entrenadorId: Int): Flow<List<String>> =
        dao.getTemporadas(entrenadorId)

    suspend fun insertOrUpdate(entity: ClasificacionEntity) =
        dao.insertOrUpdate(entity)

    suspend fun update(entity: ClasificacionEntity) =
        dao.update(entity)

    suspend fun delete(entity: ClasificacionEntity) =
        dao.delete(entity)

    suspend fun deleteAllForTemporada(entrenadorId: Int, temporada: String) =
        dao.deleteAllForTemporada(entrenadorId, temporada)
}
