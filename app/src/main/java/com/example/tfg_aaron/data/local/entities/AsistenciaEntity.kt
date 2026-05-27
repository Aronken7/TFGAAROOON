package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asistencia")
data class AsistenciaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idJugadora: Int,
    val idEntrenador: Int,
    val tipo: String,
    val referenciaId: Int,
    val estado: String = "PRESENTE",
    val notas: String = "",
    val fecha: Long
)
