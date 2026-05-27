package com.example.tfg_aaron.data.network.dto
data class ObjetivoDto(val id: Long? = null, val jugadoraId: Long = 0, val titulo: String = "",
    val descripcion: String = "", val tipo: String = "", val metrica: String = "",
    val valorObjetivo: Double = 0.0, val valorActual: Double = 0.0, val unidad: String = "",
    val fechaInicio: Long = 0, val fechaFin: Long = 0, val completado: Boolean = false,
    val notas: String = "")
