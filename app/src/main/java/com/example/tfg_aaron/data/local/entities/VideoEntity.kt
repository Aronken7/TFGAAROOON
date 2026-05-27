package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "videos",
    indices = [Index("idEntrenador")]
)
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val titulo: String,
    val url: String,
    val descripcion: String = "",
    val categoria: String = "ANÁLISIS",  // ANÁLISIS/TÁCTICA/ENTRENAMIENTO
    val fecha: Long = System.currentTimeMillis()
)
