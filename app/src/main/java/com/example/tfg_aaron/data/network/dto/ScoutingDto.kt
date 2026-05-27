package com.example.tfg_aaron.data.network.dto

data class ScoutingDto(
    val id: Long = 0,
    val rival: String,
    val tipo: String? = null,
    val observacion: String? = null,
    val valoracion: Int? = null
)
