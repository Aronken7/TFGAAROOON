package com.example.tfg_aaron.ui.screens.wellness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.WellnessDiarioEntity
import com.example.tfg_aaron.data.repository.WellnessDiarioRepository
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class WellnessUiState(
    val registros: List<WellnessDiarioEntity> = emptyList(),
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) // 0-based
)

class WellnessViewModel(
    private val entrenadorId: Int,
    private val wellnessRepo: WellnessDiarioRepository,
    private val jugadoraRepo: JugadoraRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WellnessUiState())
    val state: StateFlow<WellnessUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                wellnessRepo.getByEntrenador(entrenadorId), // ALL data, no date filter
                jugadoraRepo.getAllJugadoras(entrenadorId)
            ) { registros, jugadoras -> registros to jugadoras }
            .collect { (registros, jugadoras) ->
                _state.update { it.copy(registros = registros, jugadoras = jugadoras) }
            }
        }
    }

    // ── Month navigation ──────────────────────────────────────────────────────

    fun prevMonth() {
        val s = _state.value
        val cal = Calendar.getInstance()
        cal.set(s.selectedYear, s.selectedMonth, 1)
        cal.add(Calendar.MONTH, -1)
        _state.update { it.copy(selectedYear = cal.get(Calendar.YEAR), selectedMonth = cal.get(Calendar.MONTH)) }
    }

    fun nextMonth() {
        val s = _state.value
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance()
        cal.set(s.selectedYear, s.selectedMonth, 1)
        cal.add(Calendar.MONTH, 1)
        // Don't go beyond current month
        if (cal.get(Calendar.YEAR) < now.get(Calendar.YEAR) ||
            (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) && cal.get(Calendar.MONTH) <= now.get(Calendar.MONTH))) {
            _state.update { it.copy(selectedYear = cal.get(Calendar.YEAR), selectedMonth = cal.get(Calendar.MONTH)) }
        }
    }

    fun selectMonth(year: Int, month: Int) {
        _state.update { it.copy(selectedYear = year, selectedMonth = month) }
    }

    // ── Derived helpers (called from composable inside remember(state) { ... }) ──

    fun registrosDelMes(): List<WellnessDiarioEntity> {
        val s = _state.value
        val cal = Calendar.getInstance()
        cal.set(s.selectedYear, s.selectedMonth, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return s.registros.filter { it.fecha in start until end }
    }

    fun promediosDelMes(): Map<String, Double> {
        val list = registrosDelMes()
        if (list.isEmpty()) return emptyMap()
        return mapOf(
            "fatiga" to list.map { it.fatiga }.average(),
            "sueno" to list.map { it.sueno }.average(),
            "motivacion" to list.map { it.motivacion }.average(),
            "dolor" to list.map { it.dolor }.average(),
            "rpe" to list.map { it.rpe }.average()
        )
    }

    /** Returns (year, month, count) sorted newest first */
    fun mesesConDatos(): List<Triple<Int, Int, Int>> {
        val groups = _state.value.registros.groupBy { reg ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = reg.fecha
            Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }
        return groups.entries
            .sortedWith(compareByDescending<Map.Entry<Pair<Int,Int>, List<WellnessDiarioEntity>>> { it.key.first }
                .thenByDescending { it.key.second })
            .map { (ym, list) -> Triple(ym.first, ym.second, list.size) }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    fun saveWellness(
        jugadoraId: Int, fatiga: Int, sueno: Int,
        motivacion: Int, dolor: Int, rpe: Int, notas: String,
        fecha: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            wellnessRepo.insert(WellnessDiarioEntity(
                idJugadora = jugadoraId, idEntrenador = entrenadorId,
                fecha = fecha, fatiga = fatiga, sueno = sueno,
                motivacion = motivacion, dolor = dolor, rpe = rpe, notas = notas
            ))
        }
    }

    fun updateWellness(entity: WellnessDiarioEntity) {
        viewModelScope.launch { wellnessRepo.update(entity) }
    }

    fun deleteRegistro(id: Int) {
        viewModelScope.launch { wellnessRepo.deleteById(id) }
    }
}
