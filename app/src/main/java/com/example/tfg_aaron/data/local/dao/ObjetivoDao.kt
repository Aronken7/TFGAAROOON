package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.ObjetivoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjetivoDao {
    @Query("SELECT * FROM objetivo WHERE idEntrenador = :entrenadorId ORDER BY completado ASC, fechaFin ASC")
    fun getByEntrenador(entrenadorId: Int): Flow<List<ObjetivoEntity>>

    @Query("SELECT * FROM objetivo WHERE idJugadora = :jugadoraId ORDER BY completado ASC, fechaFin ASC")
    fun getByJugadora(jugadoraId: Int): Flow<List<ObjetivoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ObjetivoEntity): Long

    @Update
    suspend fun update(entity: ObjetivoEntity)

    @Query("DELETE FROM objetivo WHERE id = :id")
    suspend fun deleteById(id: Int)
}
