package com.example.tfg_aaron.data.network.dto
data class ShotChartDataDto(
    val id: Long? = null,
    val partidoId: Long = 0,
    val jugadoraId: Long = 0,
    val zonaX: Float = 0f,
    val zonaY: Float = 0f,
    val zona: String = "",
    val hecha: Boolean = false,
    val tipo: String = "CAMPO",
    val cuarto: Int = 1
)
