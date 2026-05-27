package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.EstadisticaPartidoEntity
import kotlinx.coroutines.flow.Flow

data class TopScorerResult(
    val idJugadora: Int,
    val totalPuntos: Int,
    val totalRebotes: Int,
    val totalAsistencias: Int,
    val partidosJugados: Int
)

@Dao
interface EstadisticaPartidoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stat: EstadisticaPartidoEntity): Long

    @Update
    suspend fun update(stat: EstadisticaPartidoEntity)

    @Delete
    suspend fun delete(stat: EstadisticaPartidoEntity)

    @Query("SELECT * FROM estadisticas_partido WHERE idPartido = :idPartido")
    fun getByPartido(idPartido: Int): Flow<List<EstadisticaPartidoEntity>>

    @Query("SELECT * FROM estadisticas_partido WHERE idJugadora = :idJugadora")
    fun getByJugadora(idJugadora: Int): Flow<List<EstadisticaPartidoEntity>>

    @Query("SELECT * FROM estadisticas_partido WHERE idPartido = :idPartido AND idJugadora = :idJugadora LIMIT 1")
    suspend fun getByPartidoAndJugadora(idPartido: Int, idJugadora: Int): EstadisticaPartidoEntity?

    @Query("""
        SELECT ep.idJugadora,
               SUM(ep.puntos)      AS totalPuntos,
               SUM(ep.rebotes)     AS totalRebotes,
               SUM(ep.asistencias) AS totalAsistencias,
               COUNT(ep.id)        AS partidosJugados
        FROM estadisticas_partido ep
        INNER JOIN partidos p ON ep.idPartido = p.id
        WHERE p.idEntrenador = :entrenadorId AND p.fecha >= :fechaDesde
        GROUP BY ep.idJugadora
        ORDER BY totalPuntos DESC
        LIMIT 1
    """)
    suspend fun getTopScorer(entrenadorId: Int, fechaDesde: Long): TopScorerResult?

    @Query("SELECT SUM(puntos) FROM estadisticas_partido WHERE idPartido = :idPartido")
    suspend fun getTotalPuntosByPartido(idPartido: Int): Int?
}
