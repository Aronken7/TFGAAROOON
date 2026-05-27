package com.example.tfg_aaron.data.network.dto

data class PartidoDto(
    val id: Long = 0,
    val rival: String,
    val puntosAnotados: Int? = null,
    val puntosEncajados: Int? = null,
    val esLocal: Boolean? = null,
    val fecha: String? = null,
    val jornada: Int? = null
)
