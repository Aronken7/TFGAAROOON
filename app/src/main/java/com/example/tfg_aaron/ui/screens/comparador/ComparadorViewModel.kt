package com.example.tfg_aaron.ui.screens.comparador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.SkillRatingEntity
import com.example.tfg_aaron.data.repository.EstadisticaPartidoRepository
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.SkillRatingRepository
import com.example.tfg_aaron.data.repository.TestFisicoRepository
import com.example.tfg_aaron.data.repository.WellnessDiarioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// ── Data models ─────────────────────────────────────────────────────────────

data class StatsComparacion(
    val jugadora: JugadoraEntity,
    val nPartidos: Int = 0,
    val puntosPorPartido: Float = 0f,
    val rebotesPorPartido: Float = 0f,
    val asistenciasPorPartido: Float = 0f,
    val faltasPorPartido: Float = 0f,
    val minutosPorPartido: Float = 0f
)

data class WellnessComparacion(
    val jugadora: JugadoraEntity,
    val mediaRpe: Float = 0f,
    val mediaSueno: Float = 0f,
    val mediaFatiga: Float = 0f,
    val mediaMotivacion: Float = 0f,
    val mediaDolor: Float = 0f,
    val nRegistros: Int = 0
)

data class TestFisicoResumen(
    val tipo: String,
    val valor: Double,
    val unidad: String,
    val nombreCustom: String = ""
)

data class FisicoComparacion(
    val jugadora: JugadoraEntity,
    val tests: Map<String, TestFisicoResumen> = emptyMap()
)

data class SkillComparacion(
    val jugadora: JugadoraEntity,
    val rating: SkillRatingEntity? = null
)

enum class ComparadorTab { ESTADISTICAS, FISICO, WELLNESS, SKILLS }

data class ComparadorUiState(
    val allJugadoras: List<JugadoraEntity> = emptyList(),
    val selectedIds: List<Int> = emptyList(),
    val activeTab: ComparadorTab = ComparadorTab.ESTADISTICAS,
    val statsData: List<StatsComparacion> = emptyList(),
    val wellnessData: List<WellnessComparacion> = emptyList(),
    val fisicoData: List<FisicoComparacion> = emptyList(),
    val skillsData: List<SkillComparacion> = emptyList(),
    val isLoading: Boolean = false
)

// ── ViewModel ────────────────────────────────────────────────────────────────

class ComparadorViewModel(
    private val jugadoraRepo: JugadoraRepository,
    private val estadisticaRepo: EstadisticaPartidoRepository,
    private val wellnessRepo: WellnessDiarioRepository,
    private val skillRepo: SkillRatingRepository,
    private val testFisicoRepo: TestFisicoRepository,
    private val entrenadorId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComparadorUiState())
    val uiState: StateFlow<ComparadorUiState> = _uiState.asStateFlow()

    init {
        // Load all active players
        viewModelScope.launch {
            jugadoraRepo.getActivas(entrenadorId).collect { jugadoras ->
                val sorted = jugadoras.sortedBy { it.nombre }
                _uiState.update { it.copy(allJugadoras = sorted) }
            }
        }
    }

    fun selectJugadora(slot: Int, jugadoraId: Int?) {
        val current = _uiState.value.selectedIds.toMutableList()
        // Ensure list has at least `slot+1` elements
        while (current.size <= slot) current.add(-1)
        if (jugadoraId == null || jugadoraId == -1) {
            current[slot] = -1
        } else {
            current[slot] = jugadoraId
        }
        val cleaned = current.dropLastWhile { it == -1 }
        _uiState.update { it.copy(selectedIds = cleaned) }
        refreshData()
    }

    fun addSlot() {
        val current = _uiState.value.selectedIds
        if (current.size < 3) {
            _uiState.update { it.copy(selectedIds = current + (-1)) }
        }
    }

    fun removeSlot() {
        val current = _uiState.value.selectedIds
        if (current.size > 2) {
            _uiState.update { it.copy(selectedIds = current.dropLast(1)) }
            refreshData()
        }
    }

    fun setTab(tab: ComparadorTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    private fun refreshData() {
        val selectedIds = _uiState.value.selectedIds.filter { it != -1 }
        if (selectedIds.isEmpty()) {
            _uiState.update {
                it.copy(
                    statsData = emptyList(),
                    wellnessData = emptyList(),
                    fisicoData = emptyList(),
                    skillsData = emptyList()
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val jugadoras = selectedIds.mapNotNull { id ->
                _uiState.value.allJugadoras.find { it.id == id }
            }

            // Stats
            val statsData = jugadoras.map { jugadora ->
                val stats = estadisticaRepo.getByJugadora(jugadora.id).first()
                val n = stats.size
                if (n == 0) {
                    StatsComparacion(jugadora = jugadora)
                } else {
                    StatsComparacion(
                        jugadora = jugadora,
                        nPartidos = n,
                        puntosPorPartido = stats.sumOf { it.puntos }.toFloat() / n,
                        rebotesPorPartido = stats.sumOf { it.rebotes }.toFloat() / n,
                        asistenciasPorPartido = stats.sumOf { it.asistencias }.toFloat() / n,
                        faltasPorPartido = stats.sumOf { it.faltas }.toFloat() / n,
                        minutosPorPartido = stats.sumOf { it.minutos }.toFloat() / n
                    )
                }
            }

            // Wellness — last 7 days
            val since = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            val wellnessData = jugadoras.map { jugadora ->
                val all = wellnessRepo.getByJugadora(jugadora.id).first()
                val recent = all.filter { it.fecha >= since }
                if (recent.isEmpty()) {
                    WellnessComparacion(jugadora = jugadora)
                } else {
                    WellnessComparacion(
                        jugadora = jugadora,
                        mediaRpe = recent.map { it.rpe }.average().toFloat(),
                        mediaSueno = recent.map { it.sueno }.average().toFloat(),
                        mediaFatiga = recent.map { it.fatiga }.average().toFloat(),
                        mediaMotivacion = recent.map { it.motivacion }.average().toFloat(),
                        mediaDolor = recent.map { it.dolor }.average().toFloat(),
                        nRegistros = recent.size
                    )
                }
            }

            // TestFisico — latest value per tipo per player
            val fisicoData = jugadoras.map { jugadora ->
                val allTests = testFisicoRepo.getByJugadora(jugadora.id).first()
                // Group by tipo, take the most recent entry of each
                val latestPerTipo = allTests
                    .groupBy { it.tipo }
                    .mapValues { (_, list) ->
                        val latest = list.maxByOrNull { it.fecha }!!
                        TestFisicoResumen(
                            tipo = latest.tipo,
                            valor = latest.valor,
                            unidad = latest.unidad,
                            nombreCustom = latest.nombreCustom
                        )
                    }
                FisicoComparacion(jugadora = jugadora, tests = latestPerTipo)
            }

            // Skills — latest rating per player
            val skillsData = jugadoras.map { jugadora ->
                val latest = skillRepo.getLatestByJugadora(jugadora.id).first()
                SkillComparacion(jugadora = jugadora, rating = latest)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    statsData = statsData,
                    wellnessData = wellnessData,
                    fisicoData = fisicoData,
                    skillsData = skillsData
                )
            }
        }
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    companion object {
        fun factory(
            jugadoraRepo: JugadoraRepository,
            estadisticaRepo: EstadisticaPartidoRepository,
            wellnessRepo: WellnessDiarioRepository,
            skillRepo: SkillRatingRepository,
            testFisicoRepo: TestFisicoRepository,
            entrenadorId: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                ComparadorViewModel(
                    jugadoraRepo = jugadoraRepo,
                    estadisticaRepo = estadisticaRepo,
                    wellnessRepo = wellnessRepo,
                    skillRepo = skillRepo,
                    testFisicoRepo = testFisicoRepo,
                    entrenadorId = entrenadorId
                ) as T
        }
    }
}
