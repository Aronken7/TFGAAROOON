package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.GamePlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GamePlanDao {
    @Query("SELECT * FROM game_plan WHERE idEntrenador = :entrenadorId ORDER BY fecha DESC")
    fun getByEntrenador(entrenadorId: Int): Flow<List<GamePlanEntity>>

    @Query("SELECT * FROM game_plan WHERE id = :id")
    suspend fun getById(id: Int): GamePlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GamePlanEntity): Long

    @Update
    suspend fun update(entity: GamePlanEntity)

    @Delete
    suspend fun delete(entity: GamePlanEntity)

    @Query("DELETE FROM game_plan WHERE id = :id")
    suspend fun deleteById(id: Int)
}
