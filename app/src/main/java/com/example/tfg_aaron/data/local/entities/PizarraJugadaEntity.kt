package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pizarra_jugadas",
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
data class PizarraJugadaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val nombreJugada: String,
    val descripcion: String = "",
    val dibujoData: String = "",    // JSON serializado con los paths
    val categoria: String = "ATAQUE", // ATAQUE, DEFENSA, BLOQUEO, TRANSICION
    val compartidaCon: String = "",   // IDs de jugadoras separados por comas
    val fechaCreacion: Long = System.currentTimeMillis(),
    val ultimaModificacion: Long = System.currentTimeMillis()
)
