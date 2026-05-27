package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.SesionTiroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SesionTiroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sesion: SesionTiroEntity): Long

    @Delete
    suspend fun delete(sesion: SesionTiroEntity)

    @Query("DELETE FROM sesion_tiro WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM sesion_tiro WHERE idEntrenador = :entrenadorId ORDER BY fecha DESC")
    fun getByEntrenador(entrenadorId: Int): Flow<List<SesionTiroEntity>>

    @Query("SELECT * FROM sesion_tiro WHERE idEntrenador = :entrenadorId AND idJugadora = :jugadoraId ORDER BY fecha DESC")
    fun getByJugadora(entrenadorId: Int, jugadoraId: Int): Flow<List<SesionTiroEntity>>

    @Query("SELECT * FROM sesion_tiro WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): SesionTiroEntity?
}
