package com.example.tfg_aaron.data.network.dto
data class EstadisticaPartidoDto(
    val id: Long? = null,
    val jugadoraId: Long = 0,
    val partidoId: Long = 0,
    val puntos: Int = 0,
    val rebotes: Int = 0,
    val asistencias: Int = 0,
    val faltas: Int = 0,
    val minutos: Int = 0
)
