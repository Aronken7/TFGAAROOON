package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.AsignacionSesionDao
import com.example.tfg_aaron.data.local.dao.SesionDao
import com.example.tfg_aaron.data.local.entities.AsignacionSesionEntity
import com.example.tfg_aaron.data.local.entities.SesionEntrenamientoEntity
import com.example.tfg_aaron.data.network.RetrofitClient
import com.example.tfg_aaron.data.network.dto.SesionDto
import kotlinx.coroutines.flow.Flow

class SesionRepository(
    private val sesionDao: SesionDao,
    private val asignacionDao: AsignacionSesionDao
) {
    fun getAll(idEntrenador: Int): Flow<List<SesionEntrenamientoEntity>> =
        sesionDao.getAllByEntrenador(idEntrenador)

    fun getBySemana(idEntrenador: Int, semana: Int): Flow<List<SesionEntrenamientoEntity>> =
        sesionDao.getBySemana(idEntrenador, semana)

    suspend fun getById(id: Int): SesionEntrenamientoEntity? = sesionDao.getById(id)

    suspend fun count(idEntrenador: Int): Int = sesionDao.countByEntrenador(idEntrenador)

    suspend fun countCompletadas(idEntrenador: Int): Int = sesionDao.countCompletadas(idEntrenador)

    suspend fun getMaxSemana(idEntrenador: Int): Int = sesionDao.getMaxSemana(idEntrenador) ?: 0

    suspend fun add(sesion: SesionEntrenamientoEntity): Long {
        val id = sesionDao.insert(sesion)
        try {
            RetrofitClient.api.createSesion(
                sesion.idEntrenador.toLong(),
                SesionDto(
                    titulo = sesion.titulo,
                    descripcion = sesion.descripcion,
                    duracion = null
                )
            )
        } catch (_: Exception) {}
        return id
    }

    suspend fun update(sesion: SesionEntrenamientoEntity) {
        sesionDao.update(sesion)
        try {
            RetrofitClient.api.updateSesion(
                sesion.idEntrenador.toLong(),
                sesion.id.toLong(),
                SesionDto(
                    titulo = sesion.titulo,
                    descripcion = sesion.descripcion,
                    duracion = null
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun delete(sesion: SesionEntrenamientoEntity) {
        sesionDao.delete(sesion)
        try {
            RetrofitClient.api.deleteSesion(sesion.idEntrenador.toLong(), sesion.id.toLong())
        } catch (_: Exception) {}
    }

    fun getAsignaciones(idSesion: Int): Flow<List<AsignacionSesionEntity>> =
        asignacionDao.getBySesion(idSesion)

    fun getSesionesByJugadora(idJugadora: Int): Flow<List<AsignacionSesionEntity>> =
        asignacionDao.getByJugadora(idJugadora)

    suspend fun asignarJugadora(idSesion: Int, idJugadora: Int): Long =
        asignacionDao.insert(AsignacionSesionEntity(idSesion = idSesion, idJugadora = idJugadora))

    suspend fun actualizarAsignacion(asignacion: AsignacionSesionEntity) =
        asignacionDao.update(asignacion)

    suspend fun eliminarAsignacion(asignacion: AsignacionSesionEntity) =
        asignacionDao.delete(asignacion)
}
