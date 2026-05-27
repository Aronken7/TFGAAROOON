package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wellness_diario")
data class WellnessDiarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idJugadora: Int,
    val idEntrenador: Int,
    val fecha: Long,
    val fatiga: Int = 5,
    val sueno: Int = 5,
    val motivacion: Int = 5,
    val dolor: Int = 1,
    val rpe: Int = 5,
    val notas: String = ""
)
