package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.ReporteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReporteDao {
    @Query("SELECT * FROM reportes WHERE idEntrenador = :idEntrenador ORDER BY fechaGeneracion DESC")
    fun getAllByEntrenador(idEntrenador: Int): Flow<List<ReporteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reporte: ReporteEntity): Long

    @Delete
    suspend fun delete(reporte: ReporteEntity)

    @Query("DELETE FROM reportes WHERE idEntrenador = :idEntrenador")
    suspend fun deleteAllByEntrenador(idEntrenador: Int)
}
