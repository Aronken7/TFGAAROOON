package com.example.tfg_aaron.data.network.dto
data class EjercicioDto(val id: Long? = null, val nombre: String = "", val descripcion: String = "",
    val categoria: String = "", val duracionMinutos: Int = 0, val intensidad: String = "",
    val materialesNecesarios: String = "", val jugadoresMinimos: Int = 0,
    val notas: String = "", val esFavorito: Boolean = false, val timestampCreacion: Long = 0)
