package com.example.tfg_aaron.data.network.dto
data class SkillRatingDto(val id: Long? = null, val jugadoraId: Long = 0, val fecha: Long = 0,
    val tiro: Int = 0, val defensa: Int = 0, val balonMano: Int = 0,
    val vision: Int = 0, val atletismo: Int = 0, val mentalidad: Int = 0,
    val liderazgo: Int = 0, val notas: String = "")
