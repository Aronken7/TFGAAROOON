package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.EjercicioDao
import com.example.tfg_aaron.data.local.entities.EjercicioEntity
import kotlinx.coroutines.flow.Flow

class EjercicioRepository(private val dao: EjercicioDao) {

    fun getAll(entrenadorId: Int): Flow<List<EjercicioEntity>> =
        dao.getAll(entrenadorId)

    fun getByCategoria(entrenadorId: Int, categoria: String): Flow<List<EjercicioEntity>> =
        dao.getByCategoria(entrenadorId, categoria)

    fun getFavoritos(entrenadorId: Int): Flow<List<EjercicioEntity>> =
        dao.getFavoritos(entrenadorId)

    suspend fun insert(ejercicio: EjercicioEntity): Long =
        dao.insert(ejercicio)

    suspend fun update(ejercicio: EjercicioEntity) =
        dao.update(ejercicio)

    suspend fun deleteById(id: Int) =
        dao.deleteById(id)

    suspend fun delete(ejercicio: EjercicioEntity) =
        dao.delete(ejercicio)
}
