package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "jugadoras",
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
data class JugadoraEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val nombre: String,
    val apellidos: String,
    val numero: Int,
    val posicion: String,   // Base, Escolta, Alero, Ala-Pívot, Pívot
    val rol: String,        // Titular, Suplente, Reserva
    val areasMejora: String = "",
    val altura: Float = 0f,
    val edad: Int = 0,
    val fotoUri: String = "",
    val activa: Boolean = true,
    val notas: String = "",
    val fechaAlta: Long = System.currentTimeMillis(),
    val condicionFisica: String = "DISPONIBLE"  // DISPONIBLE, LESIONADA, DESCANSANDO
)
