package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asignaciones_sesion",
    foreignKeys = [
        ForeignKey(
            entity = SesionEntrenamientoEntity::class,
            parentColumns = ["id"],
            childColumns = ["idSesion"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JugadoraEntity::class,
            parentColumns = ["id"],
            childColumns = ["idJugadora"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idSesion"), Index("idJugadora")]
)
data class AsignacionSesionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idSesion: Int,
    val idJugadora: Int,
    val completada: Boolean = false,
    val notas: String = ""
)
