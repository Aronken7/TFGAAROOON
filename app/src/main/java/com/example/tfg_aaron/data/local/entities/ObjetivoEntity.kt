package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "objetivo", indices = [Index("idEntrenador")])
data class ObjetivoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val idJugadora: Int = -1,        // -1 = equipo, >0 = jugadora específica
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = "RENDIMIENTO", // RENDIMIENTO, FISICO, TECNICO, EQUIPO, PERSONAL
    val metrica: String = "",         // e.g. "% Tiro libre", "Salto vertical", "Puntos por partido"
    val valorObjetivo: Float = 0f,    // target value
    val valorActual: Float = 0f,      // current value (updated manually)
    val unidad: String = "",          // "cm", "%", "pts", etc.
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaFin: Long = 0L,          // deadline, 0 = no deadline
    val completado: Boolean = false,
    val notas: String = ""
)
