package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_fisico")
data class TestFisicoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idJugadora: Int,
    val idEntrenador: Int,
    val tipo: String,
    val nombreCustom: String = "",
    val valor: Double,
    val unidad: String,
    val fecha: Long,
    val notas: String = ""
)
