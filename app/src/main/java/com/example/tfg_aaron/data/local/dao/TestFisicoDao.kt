package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.TestFisicoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestFisicoDao {
    @Query("SELECT * FROM test_fisico WHERE idJugadora = :jugadoraId ORDER BY fecha DESC")
    fun getByJugadora(jugadoraId: Int): Flow<List<TestFisicoEntity>>

    @Query("SELECT * FROM test_fisico WHERE idEntrenador = :entrenadorId ORDER BY fecha DESC")
    fun getByEntrenador(entrenadorId: Int): Flow<List<TestFisicoEntity>>

    @Query("SELECT * FROM test_fisico WHERE idJugadora = :jugadoraId AND tipo = :tipo ORDER BY fecha ASC")
    fun getByJugadoraYTipo(jugadoraId: Int, tipo: String): Flow<List<TestFisicoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TestFisicoEntity): Long

    @Update
    suspend fun update(entity: TestFisicoEntity)

    @Delete
    suspend fun delete(entity: TestFisicoEntity)

    @Query("DELETE FROM test_fisico WHERE id = :id")
    suspend fun deleteById(id: Int)
}
