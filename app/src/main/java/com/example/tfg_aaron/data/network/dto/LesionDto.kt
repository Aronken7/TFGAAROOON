package com.example.tfg_aaron.data.network.dto
data class LesionDto(val id: Long? = null, val jugadoraId: Long = 0, val tipo: String = "",
    val zonaCorpo: String = "", val gravedad: Int = 0, val fechaInicio: Long = 0,
    val fechaEstimadaVuelta: Long = 0, val fechaVueltaReal: Long = 0,
    val protocolo: String = "", val notas: String = "", val estado: String = "")
