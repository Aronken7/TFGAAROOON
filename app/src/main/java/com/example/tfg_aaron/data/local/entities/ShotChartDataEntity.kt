package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shot_chart_data")
data class ShotChartDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idPartido: Int,
    val idJugadora: Int,
    val idEntrenador: Int = 0,
    val zonaX: Float,
    val zonaY: Float,
    val zona: String,
    val hecha: Boolean,
    val tipo: String = "CAMPO",
    val cuarto: Int = 1,
    val sesionId: Int = 0  // 0 = partido / sin sesión; >0 = sesión de entrenamiento
)
