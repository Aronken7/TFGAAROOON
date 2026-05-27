package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.SkillRatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillRatingDao {
    @Query("SELECT * FROM skill_rating WHERE idJugadora = :jugadoraId ORDER BY fecha DESC")
    fun getByJugadora(jugadoraId: Int): Flow<List<SkillRatingEntity>>

    @Query("SELECT * FROM skill_rating WHERE idEntrenador = :entrenadorId ORDER BY fecha DESC")
    fun getByEntrenador(entrenadorId: Int): Flow<List<SkillRatingEntity>>

    @Query("SELECT * FROM skill_rating WHERE idJugadora = :jugadoraId ORDER BY fecha DESC LIMIT 1")
    fun getLatestByJugadora(jugadoraId: Int): Flow<SkillRatingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SkillRatingEntity): Long

    @Update
    suspend fun update(entity: SkillRatingEntity)

    @Delete
    suspend fun delete(entity: SkillRatingEntity)

    @Query("DELETE FROM skill_rating WHERE id = :id")
    suspend fun deleteById(id: Int)
}
