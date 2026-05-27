package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.EstadisticaTirosDao
import com.example.tfg_aaron.data.local.entities.EstadisticaTirosEntity
import kotlinx.coroutines.flow.Flow

class EstadisticaRepository(private val estadisticaDao: EstadisticaTirosDao) {

    fun getByJugadora(idJugadora: Int): Flow<List<EstadisticaTirosEntity>> =
        estadisticaDao.getByJugadora(idJugadora)

    fun getByJugadoraAndTipo(idJugadora: Int, tipo: String): Flow<List<EstadisticaTirosEntity>> =
        estadisticaDao.getByJugadoraAndTipo(idJugadora, tipo)

    suspend fun getPromedio(idJugadora: Int, tipo: String = "LIBRE"): Float =
        estadisticaDao.getPromedioPorcentaje(idJugadora, tipo) ?: 0f

    suspend fun getTotalIntentados(idJugadora: Int): Int =
        estadisticaDao.getTotalIntentados(idJugadora) ?: 0

    suspend fun getTotalAcertados(idJugadora: Int): Int =
        estadisticaDao.getTotalAcertados(idJugadora) ?: 0

    suspend fun add(
        idJugadora: Int,
        intentados: Int,
        acertados: Int,
        tipo: String = "LIBRE",
        notas: String = ""
    ): Long {
        val porcentaje = if (intentados > 0) (acertados.toFloat() / intentados) * 100f else 0f
        return estadisticaDao.insert(
            EstadisticaTirosEntity(
                idJugadora = idJugadora,
                tirosIntentados = intentados,
                tirosAcertados = acertados,
                porcentaje = porcentaje,
                tipoTiro = tipo,
                notas = notas
            )
        )
    }

    suspend fun update(estadistica: EstadisticaTirosEntity) = estadisticaDao.update(estadistica)

    suspend fun delete(estadistica: EstadisticaTirosEntity) = estadisticaDao.delete(estadistica)
}
