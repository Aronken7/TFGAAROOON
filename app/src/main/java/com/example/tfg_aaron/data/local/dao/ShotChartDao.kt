package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.ShotChartDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShotChartDao {
    @Query("SELECT * FROM shot_chart_data WHERE idPartido = :partidoId")
    fun getByPartido(partidoId: Int): Flow<List<ShotChartDataEntity>>

    @Query("SELECT * FROM shot_chart_data WHERE idJugadora = :jugadoraId")
    fun getByJugadora(jugadoraId: Int): Flow<List<ShotChartDataEntity>>

    @Query("SELECT * FROM shot_chart_data WHERE idJugadora = :jugadoraId AND idPartido IN (SELECT id FROM partidos WHERE idEntrenador = :entrenadorId)")
    fun getByJugadoraAndEntrenador(jugadoraId: Int, entrenadorId: Int): Flow<List<ShotChartDataEntity>>

    @Query("SELECT * FROM shot_chart_data WHERE idEntrenador = :entrenadorId OR idPartido IN (SELECT id FROM partidos WHERE idEntrenador = :entrenadorId)")
    fun getByEntrenador(entrenadorId: Int): Flow<List<ShotChartDataEntity>>

    @Query("SELECT * FROM shot_chart_data WHERE sesionId = :sesionId")
    fun getBySesion(sesionId: Int): Flow<List<ShotChartDataEntity>>

    @Query("SELECT * FROM shot_chart_data WHERE sesionId = :sesionId")
    suspend fun getBySesionSync(sesionId: Int): List<ShotChartDataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ShotChartDataEntity): Long

    @Delete
    suspend fun delete(entity: ShotChartDataEntity)

    @Query("DELETE FROM shot_chart_data WHERE idPartido = :partidoId")
    suspend fun deleteByPartido(partidoId: Int)

    @Query("DELETE FROM shot_chart_data WHERE id = :id")
    suspend fun deleteById(id: Int)
}
