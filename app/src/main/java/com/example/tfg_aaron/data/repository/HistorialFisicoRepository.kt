package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.HistorialFisicoDao
import com.example.tfg_aaron.data.local.entities.HistorialFisicoEntity
import kotlinx.coroutines.flow.Flow

class HistorialFisicoRepository(private val dao: HistorialFisicoDao) {

    fun getByJugadora(idJugadora: Int): Flow<List<HistorialFisicoEntity>> =
        dao.getByJugadora(idJugadora)

    suspend fun insert(registro: HistorialFisicoEntity): Long =
        dao.insert(registro)

    suspend fun delete(registro: HistorialFisicoEntity) =
        dao.delete(registro)
}
