package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.EstadisticaTirosEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EstadisticaTirosDao {
    @Query("SELECT * FROM estadisticas_tiros WHERE idJugadora = :idJugadora ORDER BY fecha DESC")
    fun getByJugadora(idJugadora: Int): Flow<List<EstadisticaTirosEntity>>

    @Query("SELECT * FROM estadisticas_tiros WHERE idJugadora = :idJugadora AND tipoTiro = :tipo ORDER BY fecha ASC")
    fun getByJugadoraAndTipo(idJugadora: Int, tipo: String): Flow<List<EstadisticaTirosEntity>>

    @Query("SELECT AVG(porcentaje) FROM estadisticas_tiros WHERE idJugadora = :idJugadora AND tipoTiro = :tipo")
    suspend fun getPromedioPorcentaje(idJugadora: Int, tipo: String): Float?

    @Query("SELECT SUM(tirosIntentados) FROM estadisticas_tiros WHERE idJugadora = :idJugadora")
    suspend fun getTotalIntentados(idJugadora: Int): Int?

    @Query("SELECT SUM(tirosAcertados) FROM estadisticas_tiros WHERE idJugadora = :idJugadora")
    suspend fun getTotalAcertados(idJugadora: Int): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(estadistica: EstadisticaTirosEntity): Long

    @Update
    suspend fun update(estadistica: EstadisticaTirosEntity)

    @Delete
    suspend fun delete(estadistica: EstadisticaTirosEntity)
}
