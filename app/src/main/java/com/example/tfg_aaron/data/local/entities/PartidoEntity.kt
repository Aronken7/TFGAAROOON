package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "partidos",
    indices = [Index("idEntrenador")]
)
data class PartidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val rival: String,
    val fecha: Long = System.currentTimeMillis(),
    val lugar: String = "",
    val puntosPropio: Int = 0,
    val puntosRival: Int = 0,
    val jornada: Int = 0,
    val esLocal: Boolean = true,
    val notas: String = ""
)
