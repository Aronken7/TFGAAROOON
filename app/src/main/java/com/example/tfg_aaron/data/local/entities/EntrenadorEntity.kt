package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "entrenadores",
    indices = [Index(value = ["email"], unique = true)]
)
data class EntrenadorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val email: String,
    val password: String,
    val equipoNombre: String,
    val fechaRegistro: Long = System.currentTimeMillis()
)
