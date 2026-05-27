package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.EventoPartidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoPartidoDao {
    @Insert
    suspend fun insert(evento: EventoPartidoEntity): Long

    @Delete
    suspend fun delete(evento: EventoPartidoEntity)

    @Query("SELECT * FROM eventos_partido WHERE idPartido = :idPartido ORDER BY timestamp ASC")
    fun getByPartido(idPartido: Int): Flow<List<EventoPartidoEntity>>

    @Query("SELECT * FROM eventos_partido WHERE idPartido = :idPartido ORDER BY timestamp ASC")
    suspend fun getByPartidoOnce(idPartido: Int): List<EventoPartidoEntity>

    @Query("DELETE FROM eventos_partido WHERE idPartido = :idPartido")
    suspend fun deleteByPartido(idPartido: Int)

    @Query("SELECT * FROM eventos_partido WHERE idJugadora = :idJugadora ORDER BY timestamp ASC")
    suspend fun getByJugadora(idJugadora: Int): List<EventoPartidoEntity>

    @Query("SELECT DISTINCT idPartido FROM eventos_partido WHERE idJugadora = :idJugadora")
    suspend fun getPartidoIdsByJugadora(idJugadora: Int): List<Int>
}
