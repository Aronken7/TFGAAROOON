package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.WellnessDiarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessDiarioDao {
    @Query("SELECT * FROM wellness_diario WHERE idJugadora = :jugadoraId ORDER BY fecha DESC")
    fun getByJugadora(jugadoraId: Int): Flow<List<WellnessDiarioEntity>>

    @Query("SELECT * FROM wellness_diario WHERE idEntrenador = :entrenadorId ORDER BY fecha DESC")
    fun getByEntrenador(entrenadorId: Int): Flow<List<WellnessDiarioEntity>>

    @Query("SELECT * FROM wellness_diario WHERE idEntrenador = :entrenadorId AND fecha >= :desde ORDER BY fecha DESC")
    fun getByEntrenadorSince(entrenadorId: Int, desde: Long): Flow<List<WellnessDiarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WellnessDiarioEntity): Long

    @Update
    suspend fun update(entity: WellnessDiarioEntity)

    @Delete
    suspend fun delete(entity: WellnessDiarioEntity)

    @Query("DELETE FROM wellness_diario WHERE id = :id")
    suspend fun deleteById(id: Int)
}
