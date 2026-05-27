package com.example.tfg_aaron.data.network.dto
data class WellnessDiarioDto(val id: Long? = null, val jugadoraId: Long = 0, val fecha: Long = 0,
    val fatiga: Int = 0, val sueno: Int = 0, val motivacion: Int = 0,
    val dolor: Int = 0, val rpe: Int = 0, val notas: String = "")
