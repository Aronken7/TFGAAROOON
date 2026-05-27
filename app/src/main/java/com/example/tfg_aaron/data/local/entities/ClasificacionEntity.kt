package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "clasificacion", indices = [Index("idEntrenador")])
data class ClasificacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val temporada: String = "2024-25",
    val equipoNombre: String,
    val pj: Int = 0,
    val pg: Int = 0,
    val pp: Int = 0,
    val sf: Int = 0,
    val sc: Int = 0,
    val puntos: Int = 0
)
