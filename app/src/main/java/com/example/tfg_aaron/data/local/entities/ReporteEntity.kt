package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reportes",
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
data class ReporteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val tipo: String,       // SCOUTING, ESTADISTICAS, SESIONES, JUGADORAS, COMPLETO
    val titulo: String,
    val contenido: String,
    val fechaGeneracion: Long = System.currentTimeMillis()
)
