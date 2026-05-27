package com.example.tfg_aaron.data.repository

import com.example.tfg_aaron.data.local.dao.ReporteDao
import com.example.tfg_aaron.data.local.entities.ReporteEntity
import kotlinx.coroutines.flow.Flow

class ReporteRepository(private val reporteDao: ReporteDao) {

    fun getAll(idEntrenador: Int): Flow<List<ReporteEntity>> =
        reporteDao.getAllByEntrenador(idEntrenador)

    suspend fun add(reporte: ReporteEntity): Long = reporteDao.insert(reporte)

    suspend fun delete(reporte: ReporteEntity) = reporteDao.delete(reporte)

    suspend fun deleteAll(idEntrenador: Int) = reporteDao.deleteAllByEntrenador(idEntrenador)
}
