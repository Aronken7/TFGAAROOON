package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.PizarraJugadaDao
import com.example.tfg_aaron.data.local.entities.PizarraJugadaEntity
import com.example.tfg_aaron.data.network.RetrofitClient
import com.example.tfg_aaron.data.network.dto.PizarraDto
import kotlinx.coroutines.flow.Flow

class PizarraRepository(private val pizarraDao: PizarraJugadaDao) {

    fun getAll(idEntrenador: Int): Flow<List<PizarraJugadaEntity>> =
        pizarraDao.getAllByEntrenador(idEntrenador)

    fun getByCategoria(idEntrenador: Int, categoria: String): Flow<List<PizarraJugadaEntity>> =
        pizarraDao.getByCategoria(idEntrenador, categoria)

    suspend fun getById(id: Int): PizarraJugadaEntity? = pizarraDao.getById(id)

    suspend fun save(jugada: PizarraJugadaEntity): Long {
        val id = pizarraDao.insert(jugada)
        try {
            RetrofitClient.api.createPizarra(
                jugada.idEntrenador.toLong(),
                PizarraDto(nombre = jugada.nombreJugada, datosJson = jugada.dibujoData)
            )
        } catch (_: Exception) {}
        return id
    }

    suspend fun update(jugada: PizarraJugadaEntity) {
        pizarraDao.update(jugada)
        try {
            RetrofitClient.api.updatePizarra(
                jugada.idEntrenador.toLong(),
                jugada.id.toLong(),
                PizarraDto(nombre = jugada.nombreJugada, datosJson = jugada.dibujoData)
            )
        } catch (_: Exception) {}
    }

    suspend fun delete(jugada: PizarraJugadaEntity) {
        pizarraDao.delete(jugada)
        try {
            RetrofitClient.api.deletePizarra(jugada.idEntrenador.toLong(), jugada.id.toLong())
        } catch (_: Exception) {}
    }
}
