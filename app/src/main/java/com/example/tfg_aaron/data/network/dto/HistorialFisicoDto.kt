package com.example.tfg_aaron.data.network.dto
data class HistorialFisicoDto(val id: Long? = null, val jugadoraId: Long = 0, val fecha: Long = 0,
    val condicionFisica: String = "", val peso: Double = 0.0, val altura: Double = 0.0,
    val masaGrasa: Double = 0.0, val masaMuscular: Double = 0.0, val envergadura: Double = 0.0,
    val alturaSentado: Double = 0.0, val perimetroBrazo: Double = 0.0, val perimetroCintura: Double = 0.0,
    val perimetroMuslo: Double = 0.0, val perimetroCadera: Double = 0.0, val perimetroPecho: Double = 0.0,
    val saltoVertical: Double = 0.0, val saltoHorizontal: Double = 0.0, val sprint10m: Double = 0.0,
    val observaciones: String = "")
