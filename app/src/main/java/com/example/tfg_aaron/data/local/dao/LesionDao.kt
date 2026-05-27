package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.LesionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LesionDao {
    @Query("SELECT * FROM lesion WHERE idEntrenador = :entrenadorId ORDER BY fechaInicio DESC")
    fun getByEntrenador(entrenadorId: Int): Flow<List<LesionEntity>>

    @Query("SELECT * FROM lesion WHERE idJugadora = :jugadoraId ORDER BY fechaInicio DESC")
    fun getByJugadora(jugadoraId: Int): Flow<List<LesionEntity>>

    @Query("SELECT * FROM lesion WHERE idEntrenador = :entrenadorId AND estado = 'ACTIVA' ORDER BY fechaInicio DESC")
    fun getActivasByEntrenador(entrenadorId: Int): Flow<List<LesionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LesionEntity): Long

    @Update
    suspend fun update(entity: LesionEntity)

    @Query("DELETE FROM lesion WHERE id = :id")
    suspend fun deleteById(id: Int)
}
