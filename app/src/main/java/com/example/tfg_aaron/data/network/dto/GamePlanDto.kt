package com.example.tfg_aaron.data.network.dto
data class GamePlanDto(val id: Long? = null, val rival: String = "", val fecha: Long = 0,
    val descripcion: String = "", val focoOfensivo: String = "", val focoDefensivo: String = "",
    val jugadasClave: String = "", val sistemaDefensivo: String = "",
    val ajustesIndividuales: String = "", val notasFinales: String = "")
