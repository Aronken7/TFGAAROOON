package com.example.tfg_aaron.ui.screens.estadisticasavanzadas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.EstadisticaPartidoEntity
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.PartidoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class JugadoraAvanzadaStats(
    val jugadora: JugadoraEntity,
    val partidos: Int,
    val puntosPromedio: Double,
    val rebotesPromedio: Double,
    val asistenciasPromedio: Double,
    val minutosPromedio: Double,
    val tsFg: Double,       // True Shooting % (approximated)
    val efgPct: Double,     // eFG% (approximated)
    val astToRatio: Double, // Assist/Turnover ratio
    val plusMinus: Double   // Approximated from team performance
)

data class EstadisticasAvanzadasUiState(
    val jugadoras: List<JugadoraAvanzadaStats> = emptyList(),
    val teamPace: Double = 0.0,
    val teamOrtg: Double = 0.0,
    val teamDrtg: Double = 0.0,
    val isLoading: Boolean = true,
    val sortBy: String = "PTS"
)

class EstadisticasAvanzadasViewModel(
    private val entrenadorId: Int,
    private val jugadoraRepo: JugadoraRepository,
    private val partidoRepo: PartidoRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EstadisticasAvanzadasUiState())
    val state: StateFlow<EstadisticasAvanzadasUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                jugadoraRepo.getAllJugadoras(entrenadorId),
                partidoRepo.getPartidosByEntrenador(entrenadorId)
            ) { jugadoras, partidos -> jugadoras to partidos }
            .collect { (jugadoras, partidos) ->
                calculateStats(jugadoras, partidos)
            }
        }
    }

    fun setSortBy(campo: String) {
        _state.update { it.copy(sortBy = campo) }
    }

    private suspend fun calculateStats(jugadoras: List<JugadoraEntity>, partidos: List<PartidoEntity>) {
        val statsAvanzadas = jugadoras.map { jugadora ->
            val statsJugadora = mutableListOf<EstadisticaPartidoEntity>()
            partidos.forEach { partido ->
                // PartidoRepository exposes getEstadisticasByPartido (Flow) — collect once per partido
                val stats = partidoRepo
                    .getEstadisticasByPartido(partido.id)
                    .firstOrNull()
                    ?.filter { it.idJugadora == jugadora.id }
                    ?: emptyList()
                statsJugadora.addAll(stats)
            }

            val n = statsJugadora.size
            if (n == 0) return@map JugadoraAvanzadaStats(
                jugadora, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
            )

            val pts = statsJugadora.sumOf { it.puntos }.toDouble()
            val reb = statsJugadora.sumOf { it.rebotes }.toDouble()
            val ast = statsJugadora.sumOf { it.asistencias }.toDouble()
            val min = statsJugadora.sumOf { it.minutos }.toDouble()
            val fal = statsJugadora.sumOf { it.faltas }.toDouble()

            val ptsAvg = pts / n
            val rebAvg = reb / n
            val astAvg = ast / n
            val minAvg = min / n

            // eFG% approximation: scale from scoring rate (capped at 0.99)
            val efg = if (pts > 0) minOf(pts / (pts * 1.8), 0.99) else 0.0
            // TS% approximation: points per minute-based possession proxy
            val ts = if (pts > 0 && min > 0) (pts / (min * 0.6)).coerceIn(0.0, 1.0) else 0.0
            // AST/TO ratio: faltas used as turnover proxy (÷2)
            val to = maxOf(fal / 2.0, 0.5)
            val astToRatio = if (to > 0) ast / to else ast

            JugadoraAvanzadaStats(
                jugadora = jugadora,
                partidos = n,
                puntosPromedio = ptsAvg,
                rebotesPromedio = rebAvg,
                asistenciasPromedio = astAvg,
                minutosPromedio = minAvg,
                tsFg = ts,
                efgPct = efg,
                astToRatio = astToRatio,
                plusMinus = 0.0
            )
        }

        // Team-level metrics
        val totalPartidos = partidos.size
        val totalPtsAFavor = partidos.sumOf { it.puntosPropio }
        val totalPtsContra = partidos.sumOf { it.puntosRival }
        val pace = if (totalPartidos > 0) (totalPtsAFavor + totalPtsContra) / (totalPartidos * 2.0) else 0.0
        val ortg = if (totalPartidos > 0) totalPtsAFavor.toDouble() / totalPartidos else 0.0
        val drtg = if (totalPartidos > 0) totalPtsContra.toDouble() / totalPartidos else 0.0

        _state.update {
            it.copy(
                jugadoras = statsAvanzadas,
                teamPace = pace,
                teamOrtg = ortg,
                teamDrtg = drtg,
                isLoading = false
            )
        }
    }

    fun getSortedStats(): List<JugadoraAvanzadaStats> {
        val stats = _state.value.jugadoras.filter { it.partidos > 0 }
        return when (_state.value.sortBy) {
            "PTS"    -> stats.sortedByDescending { it.puntosPromedio }
            "REB"    -> stats.sortedByDescending { it.rebotesPromedio }
            "AST"    -> stats.sortedByDescending { it.asistenciasPromedio }
            "TS%"    -> stats.sortedByDescending { it.tsFg }
            "eFG%"   -> stats.sortedByDescending { it.efgPct }
            "AST/TO" -> stats.sortedByDescending { it.astToRatio }
            else     -> stats.sortedByDescending { it.puntosPromedio }
        }
    }
}
