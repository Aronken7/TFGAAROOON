package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "convocatoria",
    indices = [Index("idPartido"), Index("idEntrenador")]
)
data class ConvocatoriaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idPartido: Int,
    val idJugadora: Int,
    val idEntrenador: Int,
    val esTitular: Boolean = false,
    // 0=PG(Base) 1=SG(Escolta) 2=SF(Alero) 3=PF(Ala-Pívot) 4=C(Pívot), -1=suplente
    val posicionIndice: Int = -1
)
