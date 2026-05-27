package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.JugadoraDao
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.network.RetrofitClient
import com.example.tfg_aaron.data.network.dto.JugadoraDto
import kotlinx.coroutines.flow.Flow

class JugadoraRepository(private val jugadoraDao: JugadoraDao) {

    fun getAllJugadoras(idEntrenador: Int): Flow<List<JugadoraEntity>> =
        jugadoraDao.getAllByEntrenador(idEntrenador)

    fun getActivas(idEntrenador: Int): Flow<List<JugadoraEntity>> =
        jugadoraDao.getActivasByEntrenador(idEntrenador)

    fun getTitulares(idEntrenador: Int): Flow<List<JugadoraEntity>> =
        jugadoraDao.getTitulares(idEntrenador)

    suspend fun getById(id: Int): JugadoraEntity? = jugadoraDao.getById(id)

    suspend fun countActivas(idEntrenador: Int): Int = jugadoraDao.countActivas(idEntrenador)

    suspend fun add(jugadora: JugadoraEntity): Long {
        val id = jugadoraDao.insert(jugadora)
        try {
            RetrofitClient.api.createJugadora(
                jugadora.idEntrenador.toLong(),
                JugadoraDto(
                    nombre = "${jugadora.nombre} ${jugadora.apellidos}".trim(),
                    posicion = jugadora.posicion,
                    rol = jugadora.rol,
                    condicionFisica = jugadora.condicionFisica,
                    numero = jugadora.numero
                )
            )
        } catch (_: Exception) {}
        return id
    }

    suspend fun update(jugadora: JugadoraEntity) {
        jugadoraDao.update(jugadora)
        try {
            RetrofitClient.api.updateJugadora(
                jugadora.idEntrenador.toLong(),
                jugadora.id.toLong(),
                JugadoraDto(
                    nombre = "${jugadora.nombre} ${jugadora.apellidos}".trim(),
                    posicion = jugadora.posicion,
                    rol = jugadora.rol,
                    condicionFisica = jugadora.condicionFisica,
                    numero = jugadora.numero
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun delete(jugadora: JugadoraEntity) {
        jugadoraDao.delete(jugadora)
        try {
            RetrofitClient.api.deleteJugadora(jugadora.idEntrenador.toLong(), jugadora.id.toLong())
        } catch (_: Exception) {}
    }

    suspend fun deleteById(id: Int) = jugadoraDao.deleteById(id)
}
