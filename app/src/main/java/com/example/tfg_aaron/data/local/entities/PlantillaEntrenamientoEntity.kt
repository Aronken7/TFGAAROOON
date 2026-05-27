package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "plantilla_entrenamiento", indices = [Index("idEntrenador")])
data class PlantillaEntrenamientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idEntrenador: Int,
    val nombre: String,
    val descripcion: String = "",
    val activa: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
)

@Entity(tableName = "plantilla_item", indices = [Index("idPlantilla")])
data class PlantillaItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idPlantilla: Int,
    val diaSemana: Int,       // 1=Lun 2=Mar 3=Mié 4=Jue 5=Vie 6=Sáb 7=Dom
    val tipo: String,         // FISICO, TECNICO, TACTICO, PARTIDO, DESCANSO
    val titulo: String = "",
    val descripcion: String = "",
    val duracionMinutos: Int = 90
)
