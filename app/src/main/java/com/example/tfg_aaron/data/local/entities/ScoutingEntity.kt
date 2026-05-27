package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scouting",
    foreignKeys = [
        ForeignKey(
            entity = EntrenadorEntity::class,
            parentColumns = ["id"],
            childColumns = ["idEntrenador"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idEntrenador")]
)
data class ScoutingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val idJugadora: Int? = null,
    val rivalNombre: String = "",
    val observaciones: String,
    val tipo: String = "RIVAL",     // RIVAL, PROPIA
    val categoria: String = "GENERAL", // GENERAL, ATAQUE, DEFENSA, FISICO, MENTAL
    val valoracion: Int = 3,        // 1-5
    val fecha: Long = System.currentTimeMillis()
)
