package com.example.tfg_aaron.data.network.dto

data class SesionDto(
    val id: Long = 0,
    val titulo: String,
    val fecha: String? = null,
    val duracion: Int? = null,
    val descripcion: String? = null
)
