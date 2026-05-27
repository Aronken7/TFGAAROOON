package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_plan")
data class GamePlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val rival: String,
    val fecha: Long,
    val descripcion: String = "",
    val focoOfensivo: String = "",
    val focoDefensivo: String = "",
    val jugadasClave: String = "",
    val sistemaDefensivo: String = "",
    val ajustesIndividuales: String = "",
    val notasFinales: String = ""
)
