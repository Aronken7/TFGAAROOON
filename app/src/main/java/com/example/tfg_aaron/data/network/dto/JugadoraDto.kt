package com.example.tfg_aaron.data.network.dto

data class JugadoraDto(
    val id: Long = 0,
    val nombre: String,
    val posicion: String? = null,
    val rol: String? = null,
    val condicionFisica: String? = null,
    val numero: Int? = null
)
