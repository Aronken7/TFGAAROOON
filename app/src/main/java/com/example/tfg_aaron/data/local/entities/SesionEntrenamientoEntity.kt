package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sesiones_entrenamiento",
    foreignKeys = [
        ForeignKey(
            entity = EntrenadorEntity::class,
            parentColumns = ["id"],
            childColumns = ["idEntrenador"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idEntrenador")]
)
data class SesionEntrenamientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val titulo: String,
    val descripcion: String,
    val semana: Int,
    val diaSemana: String,  // Lunes, Martes, Miércoles, Jueves, Viernes, Sábado, Domingo
    val horaInicio: String, // HH:mm
    val horaFin: String,
    val lugar: String = "",
    val objetivos: String = "",
    val ejercicios: String = "",
    val completada: Boolean = false,
    val fechaInicio: Long,
    val fechaFin: Long
)
