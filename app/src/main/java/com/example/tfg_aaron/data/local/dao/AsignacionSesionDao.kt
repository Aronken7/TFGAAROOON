package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.AsignacionSesionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AsignacionSesionDao {
    @Query("SELECT * FROM asignaciones_sesion WHERE idSesion = :idSesion")
    fun getBySesion(idSesion: Int): Flow<List<AsignacionSesionEntity>>

    @Query("SELECT * FROM asignaciones_sesion WHERE idJugadora = :idJugadora")
    fun getByJugadora(idJugadora: Int): Flow<List<AsignacionSesionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asignacion: AsignacionSesionEntity): Long

    @Update
    suspend fun update(asignacion: AsignacionSesionEntity)

    @Delete
    suspend fun delete(asignacion: AsignacionSesionEntity)

    @Query("DELETE FROM asignaciones_sesion WHERE idSesion = :idSesion")
    suspend fun deleteBySesion(idSesion: Int)
}
