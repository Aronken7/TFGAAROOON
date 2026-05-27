package com.example.tfg_aaron.data.network.dto
data class RivalPerfilDto(val id: Long? = null, val nombre: String = "", val temporada: String = "",
    val sistemaOfensivo: String = "", val sistemaDefensivo: String = "",
    val jugadoresClave: String = "", val fortalezas: String = "", val debilidades: String = "",
    val puntosAFavor: Int = 0, val puntosEnContra: Int = 0,
    val victorias: Int = 0, val derrotas: Int = 0, val notas: String = "")
