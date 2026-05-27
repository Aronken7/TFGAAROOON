package com.example.tfg_aaron.data.network.dto
data class AsistenciaDto(
    val id: Long? = null,
    val jugadoraId: Long = 0,
    val tipo: String = "",
    val referenciaId: Long = 0,
    val estado: String = "PRESENTE",
    val notas: String = "",
    val fecha: Long = 0
)
