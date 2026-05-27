package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.ScoutingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoutingDao {
    @Query("SELECT * FROM scouting WHERE idEntrenador = :idEntrenador ORDER BY fecha DESC")
    fun getAllByEntrenador(idEntrenador: Int): Flow<List<ScoutingEntity>>

    @Query("SELECT * FROM scouting WHERE idJugadora = :idJugadora ORDER BY fecha DESC")
    fun getByJugadora(idJugadora: Int): Flow<List<ScoutingEntity>>

    @Query("SELECT * FROM scouting WHERE idEntrenador = :idEntrenador AND tipo = 'RIVAL' ORDER BY fecha DESC")
    fun getRivales(idEntrenador: Int): Flow<List<ScoutingEntity>>

    @Query("SELECT * FROM scouting WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ScoutingEntity?

    @Query("SELECT COUNT(*) FROM scouting WHERE idEntrenador = :idEntrenador")
    suspend fun countByEntrenador(idEntrenador: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scouting: ScoutingEntity): Long

    @Update
    suspend fun update(scouting: ScoutingEntity)

    @Delete
    suspend fun delete(scouting: ScoutingEntity)
}
