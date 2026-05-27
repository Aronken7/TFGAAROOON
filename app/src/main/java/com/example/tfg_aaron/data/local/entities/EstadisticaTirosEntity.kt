package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "estadisticas_tiros",
    foreignKeys = [
        ForeignKey(
            entity = JugadoraEntity::class,
            parentColumns = ["id"],
            childColumns = ["idJugadora"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("idJugadora")]
)
data class EstadisticaTirosEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idJugadora: Int,
    val tirosIntentados: Int,
    val tirosAcertados: Int,
    val porcentaje: Float,
    val tipoTiro: String = "LIBRE", // LIBRE, CAMPO, TRIPLE
    val fecha: Long = System.currentTimeMillis(),
    val notas: String = ""
)
