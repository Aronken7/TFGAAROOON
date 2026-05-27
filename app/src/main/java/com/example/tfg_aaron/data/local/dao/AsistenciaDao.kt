package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.AsistenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AsistenciaDao {
    @Query("SELECT * FROM asistencia WHERE tipo = :tipo AND referenciaId = :referenciaId")
    fun getByEvento(tipo: String, referenciaId: Int): Flow<List<AsistenciaEntity>>

    @Query("SELECT * FROM asistencia WHERE idJugadora = :jugadoraId AND idEntrenador = :entrenadorId ORDER BY fecha DESC")
    fun getByJugadora(jugadoraId: Int, entrenadorId: Int): Flow<List<AsistenciaEntity>>

    @Query("SELECT * FROM asistencia WHERE idEntrenador = :entrenadorId ORDER BY fecha DESC")
    fun getByEntrenador(entrenadorId: Int): Flow<List<AsistenciaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AsistenciaEntity): Long

    @Update
    suspend fun update(entity: AsistenciaEntity)

    @Delete
    suspend fun delete(entity: AsistenciaEntity)

    @Query("DELETE FROM asistencia WHERE tipo = :tipo AND referenciaId = :referenciaId")
    suspend fun deleteByEvento(tipo: String, referenciaId: Int)

    @Query("SELECT COUNT(*) FROM asistencia WHERE tipo = :tipo AND referenciaId = :referenciaId AND estado = :estado")
    suspend fun countByEstado(tipo: String, referenciaId: Int, estado: String): Int
}
