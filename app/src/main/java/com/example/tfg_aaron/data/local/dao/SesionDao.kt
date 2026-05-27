package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.SesionEntrenamientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SesionDao {
    @Query("SELECT * FROM sesiones_entrenamiento WHERE idEntrenador = :idEntrenador ORDER BY fechaInicio ASC")
    fun getAllByEntrenador(idEntrenador: Int): Flow<List<SesionEntrenamientoEntity>>

    @Query("SELECT * FROM sesiones_entrenamiento WHERE idEntrenador = :idEntrenador AND semana = :semana ORDER BY fechaInicio ASC")
    fun getBySemana(idEntrenador: Int, semana: Int): Flow<List<SesionEntrenamientoEntity>>

    @Query("SELECT * FROM sesiones_entrenamiento WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): SesionEntrenamientoEntity?

    @Query("SELECT COUNT(*) FROM sesiones_entrenamiento WHERE idEntrenador = :idEntrenador")
    suspend fun countByEntrenador(idEntrenador: Int): Int

    @Query("SELECT COUNT(*) FROM sesiones_entrenamiento WHERE idEntrenador = :idEntrenador AND completada = 1")
    suspend fun countCompletadas(idEntrenador: Int): Int

    @Query("SELECT MAX(semana) FROM sesiones_entrenamiento WHERE idEntrenador = :idEntrenador")
    suspend fun getMaxSemana(idEntrenador: Int): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sesion: SesionEntrenamientoEntity): Long

    @Update
    suspend fun update(sesion: SesionEntrenamientoEntity)

    @Delete
    suspend fun delete(sesion: SesionEntrenamientoEntity)
}
