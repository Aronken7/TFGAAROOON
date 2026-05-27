package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "estadisticas_partido",
    foreignKeys = [
        ForeignKey(
            entity = PartidoEntity::class,
            parentColumns = ["id"],
            childColumns = ["idPartido"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JugadoraEntity::class,
            parentColumns = ["id"],
            childColumns = ["idJugadora"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idPartido"), Index("idJugadora")]
)
data class EstadisticaPartidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idPartido: Int,
    val idJugadora: Int,
    val puntos: Int = 0,
    val rebotes: Int = 0,
    val asistencias: Int = 0,
    val faltas: Int = 0,
    val minutos: Int = 0
)
