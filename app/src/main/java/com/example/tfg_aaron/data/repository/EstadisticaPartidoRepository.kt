package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.EstadisticaPartidoDao
import com.example.tfg_aaron.data.local.dao.TopScorerResult
import com.example.tfg_aaron.data.local.entities.EstadisticaPartidoEntity
import kotlinx.coroutines.flow.Flow

class EstadisticaPartidoRepository(private val dao: EstadisticaPartidoDao) {

    suspend fun insertOrUpdate(stat: EstadisticaPartidoEntity) = dao.insertOrUpdate(stat)

    suspend fun delete(stat: EstadisticaPartidoEntity) = dao.delete(stat)

    fun getByPartido(idPartido: Int): Flow<List<EstadisticaPartidoEntity>> =
        dao.getByPartido(idPartido)

    fun getByJugadora(idJugadora: Int): Flow<List<EstadisticaPartidoEntity>> =
        dao.getByJugadora(idJugadora)

    suspend fun getByPartidoAndJugadora(idPartido: Int, idJugadora: Int): EstadisticaPartidoEntity? =
        dao.getByPartidoAndJugadora(idPartido, idJugadora)

    suspend fun getTopScorer(entrenadorId: Int, fechaDesde: Long): TopScorerResult? =
        dao.getTopScorer(entrenadorId, fechaDesde)
}
