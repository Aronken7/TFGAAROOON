package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ejercicios")
data class EjercicioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entrenadorId: Int,
    val nombre: String,
    val descripcion: String = "",
    val categoria: String,  // "TIRO", "FINALIZACION", "TECNICA_INDIVIDUAL", "TRANSICION", "DEFENSA", "ATAQUE", "FISICO", "CALENTAMIENTO", "OTRO"
    val duracionMinutos: Int = 15,
    val intensidad: String = "MEDIA",  // "BAJA", "MEDIA", "ALTA"
    val materialesNecesarios: String = "",
    val jugadoresMinimos: Int = 1,
    val notas: String = "",
    val esFavorito: Boolean = false,
    val timestampCreacion: Long = System.currentTimeMillis()
)
