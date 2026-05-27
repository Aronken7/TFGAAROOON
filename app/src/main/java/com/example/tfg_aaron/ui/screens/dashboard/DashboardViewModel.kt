package com.example.tfg_aaron.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import com.example.tfg_aaron.data.preferences.UserPreferences
import com.example.tfg_aaron.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardStats(
    val totalJugadoras: Int = 0,
    val totalSesiones: Int = 0,
    val sesionesCompletadas: Int = 0,
    val totalScouting: Int = 0,
    val nombreEquipo: String = "",
    val nombreEntrenador: String = "",
    // Match stats
    val totalPartidos: Int = 0,
    val victorias: Int = 0,
    val derrotas: Int = 0,
    val promedioAnotado: Float = 0f,
    val promedioEncajado: Float = 0f,
    // Jugadora destacada del mes
    val jugadoraDestacada: JugadoraEntity? = null,
    val puntosDestacada: Int = 0,
    val partidosDestacada: Int = 0,
    val diasHastaPartido: Int? = null
) {
    val empates get() = totalPartidos - victorias - derrotas
    val winRate get() = if (totalPartidos == 0) 0f else victorias * 100f / totalPartidos
}

class DashboardViewModel(
    private val entrenadorId: Int,
    private val jugadoraRepository: JugadoraRepository,
    private val sesionRepository: SesionRepository,
    private val scoutingRepository: ScoutingRepository,
    private val userPreferences: UserPreferences,
    private val partidoRepository: PartidoRepository? = null,
    private val estadisticaPartidoRepository: EstadisticaPartidoRepository? = null
) : ViewModel() {

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _proximoPartido = MutableStateFlow<PartidoEntity?>(null)
    val proximoPartido: StateFlow<PartidoEntity?> = _proximoPartido.asStateFlow()

    private val _ultimosResultados = MutableStateFlow<List<PartidoEntity>>(emptyList())
    val ultimosResultados: StateFlow<List<PartidoEntity>> = _ultimosResultados.asStateFlow()

    init {
        loadStats()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                userPreferences.entrenadorNombre,
                userPreferences.equipoNombre
            ) { nombre, equipo -> Pair(nombre, equipo) }.collect { (nombre, equipo) ->
                _stats.update { it.copy(nombreEntrenador = nombre, nombreEquipo = equipo) }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            val jugadoras = jugadoraRepository.countActivas(entrenadorId)
            val sesiones = sesionRepository.count(entrenadorId)
            val completadas = sesionRepository.countCompletadas(entrenadorId)
            val scouting = scoutingRepository.count(entrenadorId)

            // Match stats
            val partidos = partidoRepository?.getTotalPartidos(entrenadorId) ?: 0
            val victorias = partidoRepository?.getVictorias(entrenadorId) ?: 0
            val derrotas = partidoRepository?.getDerrotas(entrenadorId) ?: 0
            val ptsFor = partidoRepository?.getPromedioAnotado(entrenadorId) ?: 0f
            val ptsAgainst = partidoRepository?.getPromedioEncajado(entrenadorId) ?: 0f

            // Next match & recent results
            val proximoPartido = partidoRepository?.getProximoPartido(entrenadorId)
            _proximoPartido.value = proximoPartido
            _ultimosResultados.value = partidoRepository?.getUltimosResultados(entrenadorId) ?: emptyList()

            // Days until next match
            val diasHastaPartido = proximoPartido?.let { partido ->
                val diff = partido.fecha - System.currentTimeMillis()
                if (diff > 0) (diff / (1000 * 60 * 60 * 24)).toInt() else null
            }

            // Top scorer of current month
            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val fechaDesde = calendar.timeInMillis
            val topScorer = estadisticaPartidoRepository?.getTopScorer(entrenadorId, fechaDesde)
            val jugadoraDestacada = topScorer?.let { jugadoraRepository.getById(it.idJugadora) }

            _stats.update {
                it.copy(
                    totalJugadoras = jugadoras,
                    totalSesiones = sesiones,
                    sesionesCompletadas = completadas,
                    totalScouting = scouting,
                    totalPartidos = partidos,
                    victorias = victorias,
                    derrotas = derrotas,
                    promedioAnotado = ptsFor,
                    promedioEncajado = ptsAgainst,
                    jugadoraDestacada = jugadoraDestacada,
                    puntosDestacada = topScorer?.totalPuntos ?: 0,
                    partidosDestacada = topScorer?.partidosJugados ?: 0,
                    diasHastaPartido = diasHastaPartido
                )
            }
        }
    }

    fun refresh() = loadStats()
}
