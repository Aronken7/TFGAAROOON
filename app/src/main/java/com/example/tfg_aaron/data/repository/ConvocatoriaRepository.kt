package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.ConvocatoriaDao
import com.example.tfg_aaron.data.local.entities.ConvocatoriaEntity
import kotlinx.coroutines.flow.Flow

class ConvocatoriaRepository(private val dao: ConvocatoriaDao) {

    fun getByPartido(idPartido: Int, idEntrenador: Int): Flow<List<ConvocatoriaEntity>> =
        dao.getByPartido(idPartido, idEntrenador)

    suspend fun getByPartidoSync(idPartido: Int, idEntrenador: Int): List<ConvocatoriaEntity> =
        dao.getByPartidoSync(idPartido, idEntrenador)

    suspend fun insert(c: ConvocatoriaEntity) = dao.insert(c)

    suspend fun update(c: ConvocatoriaEntity) = dao.update(c)

    suspend fun delete(c: ConvocatoriaEntity) = dao.delete(c)

    suspend fun deleteByPartidoAndJugadora(idPartido: Int, idJugadora: Int) =
        dao.deleteByPartidoAndJugadora(idPartido, idJugadora)

    suspend fun countTitulares(idPartido: Int, idEntrenador: Int) =
        dao.countTitulares(idPartido, idEntrenador)

    suspend fun clearPartido(idPartido: Int, idEntrenador: Int) =
        dao.clearPartido(idPartido, idEntrenador)
}
