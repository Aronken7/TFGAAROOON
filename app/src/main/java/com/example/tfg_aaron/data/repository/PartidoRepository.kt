package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.EventoPartidoDao
import com.example.tfg_aaron.data.local.dao.PartidoDao
import com.example.tfg_aaron.data.local.entities.EstadisticaPartidoEntity
import com.example.tfg_aaron.data.local.entities.EventoPartidoEntity
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import com.example.tfg_aaron.data.network.RetrofitClient
import com.example.tfg_aaron.data.network.dto.PartidoDto
import kotlinx.coroutines.flow.Flow

class PartidoRepository(
    private val dao: PartidoDao,
    private val eventoDao: EventoPartidoDao
) {

    fun getPartidosByEntrenador(idEntrenador: Int): Flow<List<PartidoEntity>> =
        dao.getPartidosByEntrenador(idEntrenador)

    suspend fun getPartidoById(id: Int): PartidoEntity? = dao.getPartidoById(id)

    suspend fun insertPartido(partido: PartidoEntity): Int {
        val id = dao.insertPartido(partido).toInt()
        try {
            RetrofitClient.api.createPartido(
                partido.idEntrenador.toLong(),
                PartidoDto(
                    rival = partido.rival,
                    puntosAnotados = partido.puntosPropio,
                    puntosEncajados = partido.puntosRival,
                    esLocal = partido.esLocal,
                    fecha = partido.fecha.toString(),
                    jornada = partido.jornada
                )
            )
        } catch (_: Exception) {}
        return id
    }

    suspend fun savePartido(
        partido: PartidoEntity,
        estadisticas: List<EstadisticaPartidoEntity> = emptyList()
    ) {
        val savedId = if (partido.id == 0) {
            dao.insertPartido(partido).toInt()
        } else {
            dao.updatePartido(partido)
            partido.id
        }
        if (estadisticas.isNotEmpty()) {
            dao.deleteEstadisticasByPartido(savedId)
            estadisticas.forEach { dao.insertEstadistica(it.copy(idPartido = savedId)) }
        }
        try {
            val dto = PartidoDto(
                rival = partido.rival,
                puntosAnotados = partido.puntosPropio,
                puntosEncajados = partido.puntosRival,
                esLocal = partido.esLocal,
                fecha = partido.fecha.toString(),
                jornada = partido.jornada
            )
            if (partido.id == 0) {
                RetrofitClient.api.createPartido(partido.idEntrenador.toLong(), dto)
            } else {
                RetrofitClient.api.updatePartido(partido.idEntrenador.toLong(), partido.id.toLong(), dto)
            }
        } catch (_: Exception) {}
    }

    suspend fun updatePartido(partido: PartidoEntity) {
        dao.updatePartido(partido)
        try {
            RetrofitClient.api.updatePartido(
                partido.idEntrenador.toLong(),
                partido.id.toLong(),
                PartidoDto(
                    rival = partido.rival,
                    puntosAnotados = partido.puntosPropio,
                    puntosEncajados = partido.puntosRival,
                    esLocal = partido.esLocal,
                    fecha = partido.fecha.toString(),
                    jornada = partido.jornada
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun deletePartido(partido: PartidoEntity) {
        dao.deletePartido(partido)
        try {
            RetrofitClient.api.deletePartido(partido.idEntrenador.toLong(), partido.id.toLong())
        } catch (_: Exception) {}
    }

    // Eventos partido en vivo
    suspend fun registrarEvento(evento: EventoPartidoEntity): Long =
        eventoDao.insert(evento)

    fun getEventosByPartido(idPartido: Int): Flow<List<EventoPartidoEntity>> =
        eventoDao.getByPartido(idPartido)

    suspend fun getEventosOnce(idPartido: Int): List<EventoPartidoEntity> =
        eventoDao.getByPartidoOnce(idPartido)

    suspend fun deshacerUltimoEvento(idPartido: Int) {
        val eventos = eventoDao.getByPartidoOnce(idPartido)
        if (eventos.isNotEmpty()) {
            eventoDao.delete(eventos.last())
        }
    }

    fun getEstadisticasByPartido(idPartido: Int): Flow<List<EstadisticaPartidoEntity>> =
        dao.getEstadisticasByPartido(idPartido)

    suspend fun getProximoPartido(idEntrenador: Int) =
        dao.getProximoPartido(idEntrenador, System.currentTimeMillis())

    suspend fun getUltimosResultados(idEntrenador: Int) =
        dao.getUltimosResultados(idEntrenador)

    suspend fun getVictorias(idEntrenador: Int) = dao.getVictorias(idEntrenador)
    suspend fun getDerrotas(idEntrenador: Int) = dao.getDerrotas(idEntrenador)
    suspend fun getTotalPartidos(idEntrenador: Int) = dao.getTotalPartidos(idEntrenador)
    suspend fun getPromedioAnotado(idEntrenador: Int) = dao.getPromedioAnotado(idEntrenador) ?: 0f
    suspend fun getPromedioEncajado(idEntrenador: Int) = dao.getPromedioEncajado(idEntrenador) ?: 0f

    suspend fun getEventosByJugadora(idJugadora: Int): List<EventoPartidoEntity> =
        eventoDao.getByJugadora(idJugadora)

    suspend fun getPartidoIdsByJugadora(idJugadora: Int): List<Int> =
        eventoDao.getPartidoIdsByJugadora(idJugadora)
}
