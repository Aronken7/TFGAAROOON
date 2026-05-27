package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.HistorialFisicoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialFisicoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(registro: HistorialFisicoEntity): Long

    @Query("SELECT * FROM historial_fisico WHERE idJugadora = :idJugadora ORDER BY fecha DESC")
    fun getByJugadora(idJugadora: Int): Flow<List<HistorialFisicoEntity>>

    @Delete
    suspend fun delete(registro: HistorialFisicoEntity)
}
