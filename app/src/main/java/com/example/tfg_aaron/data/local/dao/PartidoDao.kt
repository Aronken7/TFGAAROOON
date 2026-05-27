package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.EstadisticaPartidoEntity
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartidoDao {

    @Query("SELECT * FROM partidos WHERE idEntrenador = :idEntrenador ORDER BY fecha DESC")
    fun getPartidosByEntrenador(idEntrenador: Int): Flow<List<PartidoEntity>>

    @Query("SELECT * FROM partidos WHERE id = :id")
    suspend fun getPartidoById(id: Int): PartidoEntity?

    @Insert
    suspend fun insertPartido(partido: PartidoEntity): Long

    @Update
    suspend fun updatePartido(partido: PartidoEntity)

    @Delete
    suspend fun deletePartido(partido: PartidoEntity)

    @Query("SELECT * FROM estadisticas_partido WHERE idPartido = :idPartido")
    fun getEstadisticasByPartido(idPartido: Int): Flow<List<EstadisticaPartidoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstadistica(estadistica: EstadisticaPartidoEntity)

    @Query("DELETE FROM estadisticas_partido WHERE idPartido = :idPartido")
    suspend fun deleteEstadisticasByPartido(idPartido: Int)

    @Query("SELECT COUNT(*) FROM partidos WHERE idEntrenador = :idEntrenador AND puntosPropio > puntosRival")
    suspend fun getVictorias(idEntrenador: Int): Int

    @Query("SELECT COUNT(*) FROM partidos WHERE idEntrenador = :idEntrenador AND puntosPropio < puntosRival")
    suspend fun getDerrotas(idEntrenador: Int): Int

    @Query("SELECT COUNT(*) FROM partidos WHERE idEntrenador = :idEntrenador")
    suspend fun getTotalPartidos(idEntrenador: Int): Int

    @Query("SELECT AVG(puntosPropio) FROM partidos WHERE idEntrenador = :idEntrenador")
    suspend fun getPromedioAnotado(idEntrenador: Int): Float?

    @Query("SELECT AVG(puntosRival) FROM partidos WHERE idEntrenador = :idEntrenador")
    suspend fun getPromedioEncajado(idEntrenador: Int): Float?

    @Query("SELECT * FROM partidos WHERE idEntrenador = :idEntrenador AND fecha > :now ORDER BY fecha ASC LIMIT 1")
    suspend fun getProximoPartido(idEntrenador: Int, now: Long): com.example.tfg_aaron.data.local.entities.PartidoEntity?

    @Query("SELECT * FROM partidos WHERE idEntrenador = :idEntrenador AND (puntosPropio > 0 OR puntosRival > 0) ORDER BY fecha DESC LIMIT 10")
    suspend fun getUltimosResultados(idEntrenador: Int): List<com.example.tfg_aaron.data.local.entities.PartidoEntity>
}
