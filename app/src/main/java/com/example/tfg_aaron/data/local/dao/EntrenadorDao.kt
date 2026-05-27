package com.example.tfg_aaron.data.local.dao

import androidx.room.*
import com.example.tfg_aaron.data.local.entities.EntrenadorEntity

@Dao
interface EntrenadorDao {
    @Query("SELECT * FROM entrenadores WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): EntrenadorEntity?

    @Query("SELECT * FROM entrenadores WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): EntrenadorEntity?

    @Query("SELECT * FROM entrenadores WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): EntrenadorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entrenador: EntrenadorEntity): Long

    @Update
    suspend fun update(entrenador: EntrenadorEntity)
}
