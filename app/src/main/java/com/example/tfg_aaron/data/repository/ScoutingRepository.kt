package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.ScoutingDao
import com.example.tfg_aaron.data.local.entities.ScoutingEntity
import com.example.tfg_aaron.data.network.RetrofitClient
import com.example.tfg_aaron.data.network.dto.ScoutingDto
import kotlinx.coroutines.flow.Flow

class ScoutingRepository(private val scoutingDao: ScoutingDao) {

    fun getAll(idEntrenador: Int): Flow<List<ScoutingEntity>> =
        scoutingDao.getAllByEntrenador(idEntrenador)

    fun getByJugadora(idJugadora: Int): Flow<List<ScoutingEntity>> =
        scoutingDao.getByJugadora(idJugadora)

    fun getRivales(idEntrenador: Int): Flow<List<ScoutingEntity>> =
        scoutingDao.getRivales(idEntrenador)

    suspend fun getById(id: Int): ScoutingEntity? = scoutingDao.getById(id)

    suspend fun count(idEntrenador: Int): Int = scoutingDao.countByEntrenador(idEntrenador)

    suspend fun add(scouting: ScoutingEntity): Long {
        val id = scoutingDao.insert(scouting)
        try {
            RetrofitClient.api.createScouting(
                scouting.idEntrenador.toLong(),
                ScoutingDto(
                    rival = scouting.rivalNombre,
                    tipo = scouting.tipo,
                    observacion = scouting.observaciones,
                    valoracion = scouting.valoracion
                )
            )
        } catch (_: Exception) {}
        return id
    }

    suspend fun update(scouting: ScoutingEntity) {
        scoutingDao.update(scouting)
        try {
            RetrofitClient.api.updateScouting(
                scouting.idEntrenador.toLong(),
                scouting.id.toLong(),
                ScoutingDto(
                    rival = scouting.rivalNombre,
                    tipo = scouting.tipo,
                    observacion = scouting.observaciones,
                    valoracion = scouting.valoracion
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun delete(scouting: ScoutingEntity) {
        scoutingDao.delete(scouting)
        try {
            RetrofitClient.api.deleteScouting(scouting.idEntrenador.toLong(), scouting.id.toLong())
        } catch (_: Exception) {}
    }

}
