package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.EjercicioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EjercicioDao {

    @Query("SELECT * FROM ejercicios WHERE entrenadorId = :entrenadorId ORDER BY timestampCreacion DESC")
    fun getAll(entrenadorId: Int): Flow<List<EjercicioEntity>>

    @Query("SELECT * FROM ejercicios WHERE entrenadorId = :entrenadorId AND categoria = :categoria ORDER BY timestampCreacion DESC")
    fun getByCategoria(entrenadorId: Int, categoria: String): Flow<List<EjercicioEntity>>

    @Query("SELECT * FROM ejercicios WHERE entrenadorId = :entrenadorId AND esFavorito = 1 ORDER BY timestampCreacion DESC")
    fun getFavoritos(entrenadorId: Int): Flow<List<EjercicioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ejercicio: EjercicioEntity): Long

    @Update
    suspend fun update(ejercicio: EjercicioEntity)

    @Query("DELETE FROM ejercicios WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Delete
    suspend fun delete(ejercicio: EjercicioEntity)
}
