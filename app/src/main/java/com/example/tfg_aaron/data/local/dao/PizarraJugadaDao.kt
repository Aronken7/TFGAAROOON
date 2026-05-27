package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.PizarraJugadaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PizarraJugadaDao {
    @Query("SELECT * FROM pizarra_jugadas WHERE idEntrenador = :idEntrenador ORDER BY ultimaModificacion DESC")
    fun getAllByEntrenador(idEntrenador: Int): Flow<List<PizarraJugadaEntity>>

    @Query("SELECT * FROM pizarra_jugadas WHERE idEntrenador = :idEntrenador AND categoria = :categoria ORDER BY ultimaModificacion DESC")
    fun getByCategoria(idEntrenador: Int, categoria: String): Flow<List<PizarraJugadaEntity>>

    @Query("SELECT * FROM pizarra_jugadas WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): PizarraJugadaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jugada: PizarraJugadaEntity): Long

    @Update
    suspend fun update(jugada: PizarraJugadaEntity)

    @Delete
    suspend fun delete(jugada: PizarraJugadaEntity)
}
