package com.example.tfg_aaron.data.network.dto
data class EventoPartidoDto(val id: Long? = null, val partidoId: Long = 0, val jugadoraId: Long = 0,
    val tipoEvento: String = "", val cuarto: Int = 0, val valor: Int = 0,
    val texto: String = "", val timestamp: Long = 0)
