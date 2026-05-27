package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.ClasificacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClasificacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: ClasificacionEntity)

    @Update
    suspend fun update(entity: ClasificacionEntity)

    @Delete
    suspend fun delete(entity: ClasificacionEntity)

    @Query("""
        SELECT * FROM clasificacion
        WHERE idEntrenador = :entrenadorId AND temporada = :temporada
        ORDER BY puntos DESC, (sf - sc) DESC
    """)
    fun getByEntrenadorAndTemporada(entrenadorId: Int, temporada: String): Flow<List<ClasificacionEntity>>

    @Query("SELECT DISTINCT temporada FROM clasificacion WHERE idEntrenador = :entrenadorId ORDER BY temporada DESC")
    fun getTemporadas(entrenadorId: Int): Flow<List<String>>

    @Query("DELETE FROM clasificacion WHERE idEntrenador = :entrenadorId AND temporada = :temporada")
    suspend fun deleteAllForTemporada(entrenadorId: Int, temporada: String)
}
