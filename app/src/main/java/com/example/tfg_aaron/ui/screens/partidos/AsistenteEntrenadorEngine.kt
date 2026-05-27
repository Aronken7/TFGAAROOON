package com.example.tfg_aaron.ui.screens.partidos

import com.example.tfg_aaron.data.local.entities.EventoPartidoEntity
import com.example.tfg_aaron.data.local.entities.EventoTipo
import com.example.tfg_aaron.data.local.entities.JugadoraEntity

// ── Alert Model ──────────────────────────────────────────────────────────────

enum class AlertaPrioridad { CRITICA, ALTA, MEDIA, BAJA }
enum class AlertaCategoria { FALTAS, RENDIMIENTO, TACTICA, TIEMPO, RACHA }

data class AlertaAsistente(
    val id: String,
    val prioridad: AlertaPrioridad,
    val categoria: AlertaCategoria,
    val titulo: String,
    val mensaje: String,
    val icono: String, // icon name hint for UI
    val timestamp: Long = System.currentTimeMillis()
)

// ── Advanced Per-Player Metrics ──────────────────────────────────────────────

data class MetricasAvanzadas(
    val valoracion: Int = 0,          // PIR / Efficiency rating
    val minutosEnCancha: Float = 0f,  // minutes played
    val plusMinus: Int = 0,           // +/- while on court
    val rachaActual: Int = 0          // consecutive scores (0 = none)
)

// ── Engine ───────────────────────────────────────────────────────────────────

object AsistenteEntrenadorEngine {

    /**
     * Compute advanced per-player metrics from event history.
     */
    fun computeMetricasAvanzadas(
        eventos: List<EventoPartidoEntity>,
        jugadoras: List<JugadoraEntity>,
        stats: Map<Int, EstadisticasJugadora>,
        cronometroSegundos: Int
    ): Map<Int, MetricasAvanzadas> {
        val result = mutableMapOf<Int, MetricasAvanzadas>()

        jugadoras.forEach { j ->
            val st = stats[j.id] ?: EstadisticasJugadora()

            // ── Valoración (PIR: Performance Index Rating) ──
            // PIR = (PTS + REB + AST + ROB + TAP + TL_made + FG_made)
            //      - (FG_missed + TL_missed + PERD + faltas)
            val fgMade = st.tirosDosAnotados + st.tirosTresAnotados
            val fgMissed = (st.tirosDos - st.tirosDosAnotados) + (st.tirosTres - st.tirosTresAnotados)
            val tlMissed = st.tirosLibres - st.tirosLibresAnotados
            val valoracion = st.puntos + st.rebotesTotales + st.asistencias +
                    st.robos + st.tapones + st.tirosLibresAnotados + fgMade -
                    fgMissed - tlMissed - st.perdidas - st.faltasTotales

            // ── Minutes on court ──
            val minutos = computeMinutosJugadora(j.id, eventos, cronometroSegundos)

            // ── Plus/Minus ──
            val plusMinus = computePlusMinus(j.id, eventos)

            // ── Racha actual (consecutive own team scores while on court) ──
            val racha = computeRachaActual(j.id, eventos)

            result[j.id] = MetricasAvanzadas(
                valoracion = valoracion,
                minutosEnCancha = minutos,
                plusMinus = plusMinus,
                rachaActual = racha
            )
        }
        return result
    }

    /**
     * Compute minutes played from substitution events.
     * Uses wall-clock diffs for completed stints (capped at 40 min to prevent overflow),
     * and the game chronometer directly for the current ongoing stint.
     */
    private fun computeMinutosJugadora(
        jugadoraId: Int,
        eventos: List<EventoPartidoEntity>,
        cronometroSegundos: Int
    ): Float {
        if (!eventos.any { it.tipoEvento == EventoTipo.QUINTETO_INICIAL }) return 0f
        val hasEnded = eventos.any { it.tipoEvento == EventoTipo.PARTIDO_FINALIZADO }
        val MAX_STINT_MS = 40L * 60 * 1000  // safety cap: 40 real minutes per stint

        var totalMs = 0L
        var lastEntryWallTime: Long? = null
        var isOnCourt = false

        for (e in eventos) {
            when {
                e.tipoEvento == EventoTipo.QUINTETO_INICIAL && e.idJugadora == jugadoraId -> {
                    lastEntryWallTime = e.timestamp
                    isOnCourt = true
                }
                e.tipoEvento == EventoTipo.SUSTITUCION_ENTRA && e.idJugadora == jugadoraId -> {
                    lastEntryWallTime = e.timestamp
                    isOnCourt = true
                }
                e.tipoEvento == EventoTipo.SUSTITUCION_SALE && e.idJugadora == jugadoraId -> {
                    if (lastEntryWallTime != null) {
                        totalMs += (e.timestamp - lastEntryWallTime).coerceIn(0L, MAX_STINT_MS)
                        lastEntryWallTime = null
                        isOnCourt = false
                    }
                }
                e.tipoEvento == EventoTipo.PARTIDO_FINALIZADO && isOnCourt -> {
                    if (lastEntryWallTime != null) {
                        totalMs += (e.timestamp - lastEntryWallTime).coerceIn(0L, MAX_STINT_MS)
                        lastEntryWallTime = null
                        isOnCourt = false
                    }
                }
            }
        }

        // Current stint: use game clock seconds directly (avoids wall-clock drift/overflow)
        if (isOnCourt && !hasEnded) {
            totalMs += (cronometroSegundos * 1000L).coerceAtMost(MAX_STINT_MS)
        }

        return totalMs / 60000f
    }

    /**
     * Compute +/- for a player: team score differential while they are on court.
     */
    private fun computePlusMinus(
        jugadoraId: Int,
        eventos: List<EventoPartidoEntity>
    ): Int {
        var onCourt = false
        var plusMinus = 0

        for (e in eventos) {
            when {
                e.tipoEvento == EventoTipo.QUINTETO_INICIAL && e.idJugadora == jugadoraId -> onCourt = true
                e.tipoEvento == EventoTipo.SUSTITUCION_ENTRA && e.idJugadora == jugadoraId -> onCourt = true
                e.tipoEvento == EventoTipo.SUSTITUCION_SALE && e.idJugadora == jugadoraId -> onCourt = false
            }
            if (onCourt) {
                when (e.tipoEvento) {
                    EventoTipo.PUNTO_2 -> plusMinus += 2
                    EventoTipo.PUNTO_3 -> plusMinus += 3
                    EventoTipo.LIBRE_ANOTADO -> plusMinus += 1
                    EventoTipo.PUNTO_RIVAL -> plusMinus -= e.valor
                }
            }
        }
        return plusMinus
    }

    /**
     * Compute current scoring streak for a specific player.
     */
    private fun computeRachaActual(
        jugadoraId: Int,
        eventos: List<EventoPartidoEntity>
    ): Int {
        var streak = 0
        // Walk events in reverse looking for consecutive scores by this player
        for (e in eventos.reversed()) {
            when {
                e.tipoEvento == EventoTipo.PUNTO_2 && e.idJugadora == jugadoraId -> streak++
                e.tipoEvento == EventoTipo.PUNTO_3 && e.idJugadora == jugadoraId -> streak++
                e.tipoEvento == EventoTipo.LIBRE_ANOTADO && e.idJugadora == jugadoraId -> streak++
                // If another player scores (own or rival), streak breaks
                e.tipoEvento == EventoTipo.PUNTO_2 || e.tipoEvento == EventoTipo.PUNTO_3 ||
                e.tipoEvento == EventoTipo.LIBRE_ANOTADO || e.tipoEvento == EventoTipo.PUNTO_RIVAL -> break
                // Skip non-scoring events
                else -> continue
            }
        }
        return streak
    }

    /**
     * Generate coaching alerts based on current game state.
     */
    fun generarAlertas(
        eventos: List<EventoPartidoEntity>,
        jugadoras: List<JugadoraEntity>,
        stats: Map<Int, EstadisticasJugadora>,
        enCancha: List<Int>,
        puntosPropio: Int,
        puntosRival: Int,
        cuartoActual: Int,
        metricas: Map<Int, MetricasAvanzadas>,
        rivalStats: Map<Int, EstadisticasRival>,
        rivalPlayers: List<RivalPlayerState>
    ): List<AlertaAsistente> {
        val alertas = mutableListOf<AlertaAsistente>()

        // ── 1. FOUL TROUBLE ──
        jugadoras.filter { it.id in enCancha }.forEach { j ->
            val st = stats[j.id] ?: return@forEach
            when {
                st.faltasPersonales == 4 -> alertas.add(
                    AlertaAsistente(
                        id = "falta_4_${j.id}",
                        prioridad = AlertaPrioridad.CRITICA,
                        categoria = AlertaCategoria.FALTAS,
                        titulo = "PELIGRO: #${j.numero} ${j.nombre}",
                        mensaje = "4 faltas personales. Próxima falta = expulsión. Considerar sustituir.",
                        icono = "warning"
                    )
                )
                st.faltasPersonales == 3 && cuartoActual <= 2 -> alertas.add(
                    AlertaAsistente(
                        id = "falta_3_${j.id}",
                        prioridad = AlertaPrioridad.ALTA,
                        categoria = AlertaCategoria.FALTAS,
                        titulo = "Precaución: #${j.numero} ${j.nombre}",
                        mensaje = "3 faltas en ${cuartoLabel(cuartoActual)}. Demasiado pronto.",
                        icono = "shield"
                    )
                )
                st.expulsada -> alertas.add(
                    AlertaAsistente(
                        id = "expulsada_${j.id}",
                        prioridad = AlertaPrioridad.CRITICA,
                        categoria = AlertaCategoria.FALTAS,
                        titulo = "EXPULSADA: #${j.numero} ${j.nombre}",
                        mensaje = "Jugadora eliminada por faltas. Sustituir inmediatamente.",
                        icono = "block"
                    )
                )
            }
        }

        // ── 2. SCORING DROUGHT ──
        val ultimosEventos = eventos.takeLast(15)
        val puntosRecientes = ultimosEventos.count {
            it.tipoEvento == EventoTipo.PUNTO_2 || it.tipoEvento == EventoTipo.PUNTO_3 ||
                    it.tipoEvento == EventoTipo.LIBRE_ANOTADO
        }
        val rivalRecientes = ultimosEventos.count { it.tipoEvento == EventoTipo.PUNTO_RIVAL }
        if (ultimosEventos.size >= 8 && puntosRecientes == 0 && rivalRecientes >= 3) {
            alertas.add(
                AlertaAsistente(
                    id = "sequia_ofensiva",
                    prioridad = AlertaPrioridad.ALTA,
                    categoria = AlertaCategoria.RENDIMIENTO,
                    titulo = "Sequía ofensiva",
                    mensaje = "Sin anotar en los últimos eventos. Rival anotó $rivalRecientes veces. Considerar timeout.",
                    icono = "trending_down"
                )
            )
        }

        // ── 3. RIVAL RUN ──
        val parcialVivo = computeParcialVivo(eventos)
        if (parcialVivo.second >= 8 && parcialVivo.first == 0) {
            alertas.add(
                AlertaAsistente(
                    id = "parcial_rival",
                    prioridad = AlertaPrioridad.CRITICA,
                    categoria = AlertaCategoria.TACTICA,
                    titulo = "Parcial rival: ${parcialVivo.first}-${parcialVivo.second}",
                    mensaje = "El rival lleva un parcial de ${parcialVivo.second}-0. ¡Pedir timeout!",
                    icono = "trending_up"
                )
            )
        } else if (parcialVivo.second >= 6 && parcialVivo.first <= 2) {
            alertas.add(
                AlertaAsistente(
                    id = "parcial_rival_alto",
                    prioridad = AlertaPrioridad.ALTA,
                    categoria = AlertaCategoria.TACTICA,
                    titulo = "Parcial rival: ${parcialVivo.first}-${parcialVivo.second}",
                    mensaje = "El rival domina el parcial en vivo. Considerar cambiar el ritmo.",
                    icono = "warning"
                )
            )
        }

        // ── 4. OWN RUN (positive) ──
        if (parcialVivo.first >= 8 && parcialVivo.second == 0) {
            alertas.add(
                AlertaAsistente(
                    id = "parcial_propio",
                    prioridad = AlertaPrioridad.BAJA,
                    categoria = AlertaCategoria.RACHA,
                    titulo = "Parcial a favor: ${parcialVivo.first}-${parcialVivo.second}",
                    mensaje = "Gran parcial. No cambiar a las jugadoras que están en racha.",
                    icono = "star"
                )
            )
        }

        // ── 5. FATIGUE / MINUTES ──
        jugadoras.filter { it.id in enCancha }.forEach { j ->
            val m = metricas[j.id] ?: return@forEach
            if (m.minutosEnCancha >= 25f) {
                alertas.add(
                    AlertaAsistente(
                        id = "fatiga_${j.id}",
                        prioridad = AlertaPrioridad.MEDIA,
                        categoria = AlertaCategoria.TIEMPO,
                        titulo = "Fatiga: #${j.numero} ${j.nombre}",
                        mensaje = "Lleva ${m.minutosEnCancha.toInt()} min en cancha. Considerar descanso.",
                        icono = "timer"
                    )
                )
            }
        }

        // ── 6. BENCH PLAYERS NOT USED ──
        val noJugaron = jugadoras.filter { j ->
            val m = metricas[j.id]
            j.id !in enCancha && (m == null || m.minutosEnCancha < 1f)
        }
        if (noJugaron.isNotEmpty() && cuartoActual >= 3) {
            alertas.add(
                AlertaAsistente(
                    id = "banquillo_sin_jugar",
                    prioridad = AlertaPrioridad.BAJA,
                    categoria = AlertaCategoria.TIEMPO,
                    titulo = "${noJugaron.size} jugadoras sin minutos",
                    mensaje = noJugaron.joinToString(", ") { "#${it.numero} ${it.nombre}" } +
                            " no han jugado.",
                    icono = "people"
                )
            )
        }

        // ── 7. HOT PLAYER ──
        jugadoras.filter { it.id in enCancha }.forEach { j ->
            val m = metricas[j.id] ?: return@forEach
            if (m.rachaActual >= 3) {
                alertas.add(
                    AlertaAsistente(
                        id = "racha_${j.id}",
                        prioridad = AlertaPrioridad.BAJA,
                        categoria = AlertaCategoria.RACHA,
                        titulo = "EN RACHA: #${j.numero} ${j.nombre}",
                        mensaje = "${m.rachaActual} canastas consecutivas. Buscarla en ataque.",
                        icono = "whatshot"
                    )
                )
            }
        }

        // ── 8. LOW EFFICIENCY PLAYER ON COURT ──
        jugadoras.filter { it.id in enCancha }.forEach { j ->
            val st = stats[j.id] ?: return@forEach
            val m = metricas[j.id] ?: return@forEach
            if (m.minutosEnCancha >= 8f && m.valoracion <= -3) {
                alertas.add(
                    AlertaAsistente(
                        id = "bajo_rendimiento_${j.id}",
                        prioridad = AlertaPrioridad.MEDIA,
                        categoria = AlertaCategoria.RENDIMIENTO,
                        titulo = "Bajo rendimiento: #${j.numero}",
                        mensaje = "VAL ${m.valoracion} en ${m.minutosEnCancha.toInt()} min. " +
                                "Considerar cambio.",
                        icono = "trending_down"
                    )
                )
            }
        }

        // ── 9. BEST BENCH OPTION ──
        val benchWithStats = jugadoras.filter { it.id !in enCancha }.mapNotNull { j ->
            val m = metricas[j.id] ?: return@mapNotNull null
            val st = stats[j.id] ?: EstadisticasJugadora()
            if (m.valoracion > 0 && m.minutosEnCancha >= 3f) Triple(j, m, st) else null
        }.sortedByDescending { it.second.valoracion }

        if (benchWithStats.isNotEmpty()) {
            val best = benchWithStats.first()
            alertas.add(
                AlertaAsistente(
                    id = "mejor_banquillo",
                    prioridad = AlertaPrioridad.BAJA,
                    categoria = AlertaCategoria.TACTICA,
                    titulo = "Mejor opción de banquillo",
                    mensaje = "#${best.first.numero} ${best.first.nombre}: VAL ${best.second.valoracion}, " +
                            "${best.third.puntos} pts, +/- ${if (best.second.plusMinus >= 0) "+" else ""}${best.second.plusMinus}",
                    icono = "swap"
                )
            )
        }

        // ── 10. RIVAL FOUL TROUBLE ──
        rivalPlayers.forEach { rival ->
            val rs = rivalStats[rival.id] ?: return@forEach
            if (rs.faltasPersonales == 4) {
                alertas.add(
                    AlertaAsistente(
                        id = "rival_falta_4_${rival.id}",
                        prioridad = AlertaPrioridad.MEDIA,
                        categoria = AlertaCategoria.TACTICA,
                        titulo = "Rival #${rival.numero} con 4 faltas",
                        mensaje = "Atacar a su zona. Una falta más y será expulsada.",
                        icono = "target"
                    )
                )
            }
        }

        // ── 11. CLOSE GAME ALERT ──
        if (cuartoActual >= 4 && kotlin.math.abs(puntosPropio - puntosRival) <= 3) {
            alertas.add(
                AlertaAsistente(
                    id = "partido_igualado",
                    prioridad = AlertaPrioridad.ALTA,
                    categoria = AlertaCategoria.TACTICA,
                    titulo = "Partido igualado",
                    mensaje = "Diferencia de ${kotlin.math.abs(puntosPropio - puntosRival)} puntos en ${cuartoLabel(cuartoActual)}. " +
                            "Poner a las mejores en cancha.",
                    icono = "sports"
                )
            )
        }

        // ── 12. SHOOTING EFFICIENCY ALERT ──
        val totalTiros = stats.values.sumOf { it.tirosDos + it.tirosTres }
        val totalAnotados = stats.values.sumOf { it.tirosDosAnotados + it.tirosTresAnotados }
        if (totalTiros >= 15) {
            val pctCampo = totalAnotados * 100f / totalTiros
            if (pctCampo < 30f) {
                alertas.add(
                    AlertaAsistente(
                        id = "baja_eficiencia_tiro",
                        prioridad = AlertaPrioridad.ALTA,
                        categoria = AlertaCategoria.RENDIMIENTO,
                        titulo = "Baja eficiencia de tiro: ${pctCampo.toInt()}%",
                        mensaje = "Solo $totalAnotados/$totalTiros en tiros de campo. Buscar mejor selección de tiro.",
                        icono = "sports_basketball"
                    )
                )
            }
        }

        // ── 13. REBOUNDING DEFICIT ──
        val rebPropios = stats.values.sumOf { it.rebotesTotales }
        val rebRivales = rivalStats.values.sumOf { it.rebotesTotales }
        if (rebPropios + rebRivales >= 10 && rebRivales > rebPropios + 5) {
            alertas.add(
                AlertaAsistente(
                    id = "deficit_rebotes",
                    prioridad = AlertaPrioridad.MEDIA,
                    categoria = AlertaCategoria.TACTICA,
                    titulo = "Déficit de rebotes: $rebPropios vs $rebRivales",
                    mensaje = "El rival nos supera en ${rebRivales - rebPropios} rebotes. Reforzar el rebote.",
                    icono = "height"
                )
            )
        }

        // ── 14. TURNOVER PROBLEM ──
        val totalPerdidas = stats.values.sumOf { it.perdidas }
        val totalRobos = stats.values.sumOf { it.robos }
        if (totalPerdidas >= 8 && totalPerdidas > totalRobos * 2) {
            alertas.add(
                AlertaAsistente(
                    id = "muchas_perdidas",
                    prioridad = AlertaPrioridad.MEDIA,
                    categoria = AlertaCategoria.RENDIMIENTO,
                    titulo = "Demasiadas pérdidas: $totalPerdidas",
                    mensaje = "$totalPerdidas pérdidas vs $totalRobos robos. Cuidar más el balón.",
                    icono = "error"
                )
            )
        }

        return alertas.sortedBy { it.prioridad.ordinal }
    }

    /**
     * Compute the "live partial" — the current run since the last own score or timeout.
     * Returns (own points, rival points) since the last interruption.
     */
    fun computeParcialVivo(eventos: List<EventoPartidoEntity>): Pair<Int, Int> {
        var propios = 0
        var rivales = 0
        // Walk backwards from most recent event
        for (e in eventos.reversed()) {
            when (e.tipoEvento) {
                EventoTipo.PUNTO_2 -> { propios += 2; if (rivales > 0) return propios to rivales }
                EventoTipo.PUNTO_3 -> { propios += 3; if (rivales > 0) return propios to rivales }
                EventoTipo.LIBRE_ANOTADO -> { propios += 1; if (rivales > 0) return propios to rivales }
                EventoTipo.PUNTO_RIVAL -> { rivales += e.valor; if (propios > 0) return propios to rivales }
                EventoTipo.TIMEOUT_PROPIO, EventoTipo.TIMEOUT_RIVAL,
                EventoTipo.NUEVO_CUARTO -> return propios to rivales
                else -> continue
            }
        }
        return propios to rivales
    }

    /**
     * Compute team scoring run from both sides for display.
     * Returns formatted string like "8-0" or "0-6"
     */
    fun formatParcialVivo(parcial: Pair<Int, Int>): String {
        return "${parcial.first}-${parcial.second}"
    }
}
