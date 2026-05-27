package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rival_perfil")
data class RivalPerfilEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val nombre: String,
    val temporada: String = "",
    val sistemaOfensivo: String = "",
    val sistemaDefensivo: String = "",
    val jugadoresClave: String = "",
    val fortalezas: String = "",
    val debilidades: String = "",
    val puntosAFavor: Int = 0,
    val puntosEnContra: Int = 0,
    val victorias: Int = 0,
    val derrotas: Int = 0,
    val notas: String = ""
)
