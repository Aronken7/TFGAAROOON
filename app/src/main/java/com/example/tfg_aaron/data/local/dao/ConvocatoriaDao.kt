package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.ConvocatoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConvocatoriaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(convocatoria: ConvocatoriaEntity): Long

    @Update
    suspend fun update(convocatoria: ConvocatoriaEntity)

    @Delete
    suspend fun delete(convocatoria: ConvocatoriaEntity)

    @Query("SELECT * FROM convocatoria WHERE idPartido = :idPartido AND idEntrenador = :idEntrenador")
    fun getByPartido(idPartido: Int, idEntrenador: Int): Flow<List<ConvocatoriaEntity>>

    @Query("SELECT * FROM convocatoria WHERE idPartido = :idPartido AND idEntrenador = :idEntrenador")
    suspend fun getByPartidoSync(idPartido: Int, idEntrenador: Int): List<ConvocatoriaEntity>

    @Query("DELETE FROM convocatoria WHERE idPartido = :idPartido AND idJugadora = :idJugadora")
    suspend fun deleteByPartidoAndJugadora(idPartido: Int, idJugadora: Int)

    @Query("SELECT COUNT(*) FROM convocatoria WHERE idPartido = :idPartido AND esTitular = 1 AND idEntrenador = :idEntrenador")
    suspend fun countTitulares(idPartido: Int, idEntrenador: Int): Int

    @Query("DELETE FROM convocatoria WHERE idPartido = :idPartido AND idEntrenador = :idEntrenador")
    suspend fun clearPartido(idPartido: Int, idEntrenador: Int)
}
