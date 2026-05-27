package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "lesion", indices = [Index("idJugadora"), Index("idEntrenador")])
data class LesionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idJugadora: Int,
    val idEntrenador: Int,
    val tipo: String = "",           // e.g. "Esguince", "Rotura fibrilar", "Contusión", etc.
    val zonaCorpo: String = "",      // e.g. "Tobillo derecho", "Rodilla izquierda", "Isquiotibial"
    val gravedad: Int = 1,           // 1=Leve, 2=Moderada, 3=Grave, 4=Muy grave, 5=Crítica
    val fechaInicio: Long = System.currentTimeMillis(),
    val fechaEstimadaVuelta: Long = 0L,
    val fechaVueltaReal: Long = 0L,  // 0 = not yet recovered
    val protocolo: String = "",      // recovery steps/notes
    val notas: String = "",
    val estado: String = "ACTIVA"    // ACTIVA, RECUPERADA, RECAIDA
)
