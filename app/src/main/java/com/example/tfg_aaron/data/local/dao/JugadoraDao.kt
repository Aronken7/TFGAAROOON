package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JugadoraDao {
    @Query("SELECT * FROM jugadoras WHERE idEntrenador = :idEntrenador ORDER BY numero ASC")
    fun getAllByEntrenador(idEntrenador: Int): Flow<List<JugadoraEntity>>

    @Query("SELECT * FROM jugadoras WHERE idEntrenador = :idEntrenador AND activa = 1 ORDER BY numero ASC")
    fun getActivasByEntrenador(idEntrenador: Int): Flow<List<JugadoraEntity>>

    @Query("SELECT * FROM jugadoras WHERE idEntrenador = :idEntrenador AND rol = 'Titular' ORDER BY numero ASC")
    fun getTitulares(idEntrenador: Int): Flow<List<JugadoraEntity>>

    @Query("SELECT * FROM jugadoras WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): JugadoraEntity?

    @Query("SELECT COUNT(*) FROM jugadoras WHERE idEntrenador = :idEntrenador AND activa = 1")
    suspend fun countActivas(idEntrenador: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jugadora: JugadoraEntity): Long

    @Update
    suspend fun update(jugadora: JugadoraEntity)

    @Delete
    suspend fun delete(jugadora: JugadoraEntity)

    @Query("DELETE FROM jugadoras WHERE id = :id")
    suspend fun deleteById(id: Int)
}
