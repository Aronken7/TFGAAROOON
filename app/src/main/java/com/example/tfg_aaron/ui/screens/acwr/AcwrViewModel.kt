package com.example.tfg_aaron.ui.screens.acwr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.WellnessDiarioRepository
import kotlinx.coroutines.flow.*
import java.util.Calendar

// ── Data models ──────────────────────────────────────────────────────────────

enum class AcwrZone { SAFE, WARNING, DANGER, NO_DATA }

data class PlayerAcwrData(
    val jugadora: JugadoraEntity,
    val acuteLoad: Int,        // sum of rpe*60 last 7 days
    val chronicLoad: Double,   // sum of rpe*60 last 28 days / 4
    val acwr: Double?,         // null if no chronic data
    val zone: AcwrZone,
    val dailyLoads: List<Pair<Long, Int>>  // (fechaMs, load) for last 28 days, chronological
)

data class AcwrUiState(
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val playerData: List<PlayerAcwrData> = emptyList(),
    val selectedJugadoraId: Int? = null,   // null = show equipo (all)
    val isLoading: Boolean = true
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class AcwrViewModel(
    private val wellnessRepo: WellnessDiarioRepository,
    private val jugadoraRepo: JugadoraRepository,
    private val entrenadorId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(AcwrUiState())
    val state: StateFlow<AcwrUiState> = _state.asStateFlow()

    init {
        val since28 = System.currentTimeMillis() - 28L * 24 * 60 * 60 * 1000

        combine(
            jugadoraRepo.getActivas(entrenadorId),
            wellnessRepo.getByEntrenadorSince(entrenadorId, since28)
        ) { jugadoras, allWellness ->
            Pair(jugadoras, allWellness)
        }.onEach { (jugadoras, allWellness) ->
            val nowMs = System.currentTimeMillis()
            val cutoff7  = nowMs - 7L  * 24 * 60 * 60 * 1000
            val cutoff28 = nowMs - 28L * 24 * 60 * 60 * 1000

            val playerData = jugadoras.map { jugadora ->
                val playerWellness = allWellness.filter { it.idJugadora == jugadora.id }

                // Daily loads for last 28 days — group by calendar day
                val dailyMap = mutableMapOf<Long, Int>()
                playerWellness
                    .filter { it.fecha >= cutoff28 }
                    .forEach { entry ->
                        val dayKey = startOfDay(entry.fecha)
                        dailyMap[dayKey] = (dailyMap[dayKey] ?: 0) + (entry.rpe * 60)
                    }
                val dailyLoads = dailyMap.entries
                    .sortedBy { it.key }
                    .map { Pair(it.key, it.value) }

                // Acute = last 7 days
                val acuteLoad = playerWellness
                    .filter { it.fecha >= cutoff7 }
                    .sumOf { it.rpe * 60 }

                // Chronic = last 28 days total / 4 (weekly average)
                val total28 = playerWellness
                    .filter { it.fecha >= cutoff28 }
                    .sumOf { it.rpe * 60 }
                val chronicLoad = total28.toDouble() / 4.0

                val acwr: Double? = if (chronicLoad > 0.0) acuteLoad.toDouble() / chronicLoad else null
                val zone = acwrZone(acwr)

                PlayerAcwrData(
                    jugadora = jugadora,
                    acuteLoad = acuteLoad,
                    chronicLoad = chronicLoad,
                    acwr = acwr,
                    zone = zone,
                    dailyLoads = dailyLoads
                )
            }.sortedByDescending { it.acwr ?: -1.0 }

            _state.update {
                it.copy(
                    jugadoras = jugadoras,
                    playerData = playerData,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }

    fun selectJugadora(id: Int?) {
        _state.update { it.copy(selectedJugadoraId = id) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun startOfDay(epochMs: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMs
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    companion object {
        fun acwrZone(acwr: Double?): AcwrZone = when {
            acwr == null -> AcwrZone.NO_DATA
            acwr > 1.5 || acwr < 0.5 -> AcwrZone.DANGER
            acwr > 1.3 -> AcwrZone.WARNING
            acwr >= 0.8 -> AcwrZone.SAFE
            else -> AcwrZone.DANGER
        }

        fun factory(
            wellnessRepo: WellnessDiarioRepository,
            jugadoraRepo: JugadoraRepository,
            entrenadorId: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AcwrViewModel(wellnessRepo, jugadoraRepo, entrenadorId) as T
            }
        }
    }
}
