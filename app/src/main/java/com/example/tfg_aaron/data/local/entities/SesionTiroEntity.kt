package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sesion_tiro",
    indices = [Index("idEntrenador"), Index("idJugadora")]
)
data class SesionTiroEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val idJugadora: Int,   // -1 = sesión de equipo
    val titulo: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val notas: String = ""
)
