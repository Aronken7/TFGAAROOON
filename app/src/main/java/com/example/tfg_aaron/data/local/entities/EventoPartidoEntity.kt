package com.example.tfg_aaron.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object EventoTipo {
    // Own team scoring
    const val PUNTO_2 = "PUNTO_2"
    const val PUNTO_3 = "PUNTO_3"
    const val LIBRE_ANOTADO = "LIBRE_ANOTADO"
    const val LIBRE_FALLADO = "LIBRE_FALLADO"
    // Own team fouls
    const val FALTA_PERSONAL = "FALTA_PERSONAL"
    const val FALTA_PERSONAL_TIRO = "FALTA_PERSONAL_TIRO"
    const val FALTA_TECNICA = "FALTA_TECNICA"
    const val FALTA_ANTIDEPORTIVA = "FALTA_ANTIDEPORTIVA"
    // Own team other stats
    const val SUSTITUCION_ENTRA = "SUSTITUCION_ENTRA"
    const val SUSTITUCION_SALE = "SUSTITUCION_SALE"
    const val REBOTE_OFENSIVO = "REBOTE_OFENSIVO"
    const val REBOTE_DEFENSIVO = "REBOTE_DEFENSIVO"
    const val ASISTENCIA = "ASISTENCIA"
    const val ROBO = "ROBO"
    const val PERDIDA = "PERDIDA"
    const val TAPON = "TAPON"
    const val TIRO_DOS_FALLADO = "TIRO_DOS_FALLADO"
    const val TIRO_TRES_FALLADO = "TIRO_TRES_FALLADO"
    // Rival generic scoring (idJugadora = -1)
    const val PUNTO_RIVAL = "PUNTO_RIVAL"
    // Rival individual player events (idJugadora = -rivalId, valor = rivalId, texto for ALTA)
    const val RIVAL_JUGADORA_ALTA = "RIVAL_JUGADORA_ALTA"   // texto = "nombre|numero"
    const val RIVAL_FALTA_PERSONAL = "RIVAL_FALTA_PERSONAL"
    const val RIVAL_FALTA_TECNICA = "RIVAL_FALTA_TECNICA"
    const val RIVAL_FALTA_ANTIDEPORTIVA = "RIVAL_FALTA_ANTIDEPORTIVA"
    const val RIVAL_FALTA_PERSONAL_TIRO = "RIVAL_FALTA_PERSONAL_TIRO"
    const val RIVAL_REBOTE_OF = "RIVAL_REBOTE_OF"
    const val RIVAL_REBOTE_DEF = "RIVAL_REBOTE_DEF"
    const val RIVAL_ASISTENCIA = "RIVAL_ASISTENCIA"
    const val RIVAL_ROBO = "RIVAL_ROBO"
    const val RIVAL_PERDIDA = "RIVAL_PERDIDA"
    const val RIVAL_TAPON = "RIVAL_TAPON"
    // Game flow
    const val QUINTETO_INICIAL = "QUINTETO_INICIAL"
    const val NUEVO_CUARTO = "NUEVO_CUARTO"
    const val PARTIDO_FINALIZADO = "PARTIDO_FINALIZADO"
    // Timeouts
    const val TIMEOUT_PROPIO = "TIMEOUT_PROPIO"
    const val TIMEOUT_RIVAL = "TIMEOUT_RIVAL"
    // Rival individual player scoring (idJugadora = -rivalId, valor = 2 or 3)
    const val RIVAL_PUNTO_2 = "RIVAL_PUNTO_2"
    const val RIVAL_PUNTO_3 = "RIVAL_PUNTO_3"
}

@Entity(
    tableName = "eventos_partido",
    indices = [Index("idPartido")]
)
data class EventoPartidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idPartido: Int,
    val idJugadora: Int = -1,
    val tipoEvento: String,
    val cuarto: Int = 1,
    val valor: Int = 0,
    val texto: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
