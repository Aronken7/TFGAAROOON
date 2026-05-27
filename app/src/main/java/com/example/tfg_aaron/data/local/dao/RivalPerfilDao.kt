package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.RivalPerfilEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RivalPerfilDao {
    @Query("SELECT * FROM rival_perfil WHERE idEntrenador = :entrenadorId ORDER BY nombre ASC")
    fun getByEntrenador(entrenadorId: Int): Flow<List<RivalPerfilEntity>>

    @Query("SELECT * FROM rival_perfil WHERE id = :id")
    suspend fun getById(id: Int): RivalPerfilEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RivalPerfilEntity): Long

    @Update
    suspend fun update(entity: RivalPerfilEntity)

    @Delete
    suspend fun delete(entity: RivalPerfilEntity)

    @Query("DELETE FROM rival_perfil WHERE id = :id")
    suspend fun deleteById(id: Int)
}
