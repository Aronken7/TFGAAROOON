package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skill_rating")
data class SkillRatingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idJugadora: Int,
    val idEntrenador: Int,
    val fecha: Long,
    val tiro: Int = 5,
    val defensa: Int = 5,
    val balonMano: Int = 5,
    val vision: Int = 5,
    val atletismo: Int = 5,
    val mentalidad: Int = 5,
    val liderazgo: Int = 5,
    val notas: String = ""
)
