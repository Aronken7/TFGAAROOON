package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "historial_fisico",
    indices = [Index("idJugadora")]
)
data class HistorialFisicoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idJugadora: Int,
    val fecha: Long = System.currentTimeMillis(),
    // Condition
    val condicionFisica: String = "DISPONIBLE",  // DISPONIBLE/LESIONADA/DESCANSANDO
    // Basic measurements
    val peso: Float = 0f,           // kg
    val altura: Float = 0f,         // cm standing height
    // Body composition
    val masaGrasa: Float = 0f,      // body fat %
    val masaMuscular: Float = 0f,   // muscle mass %
    // Size measurements
    val envergadura: Float = 0f,    // wingspan cm
    val alturaSentado: Float = 0f,  // sitting height cm
    // Circumferences
    val perimetroBrazo: Float = 0f,   // bicep circumference cm
    val perimetroCintura: Float = 0f, // waist circumference cm
    val perimetroMuslo: Float = 0f,   // thigh circumference cm
    val perimetroCadera: Float = 0f,  // hip circumference cm
    val perimetroPecho: Float = 0f,   // chest circumference cm
    // Athletic performance
    val saltoVertical: Float = 0f,    // vertical jump cm
    val saltoHorizontal: Float = 0f,  // broad jump cm
    val sprint10m: Float = 0f,        // 10m sprint seconds
    // Notes
    val observaciones: String = ""
)
