package com.example.tfg_aaron.ui.screens.shotchart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.SesionTiroEntity
import com.example.tfg_aaron.data.local.entities.ShotChartDataEntity
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.SesionTiroRepository
import com.example.tfg_aaron.data.repository.ShotChartRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ShotChartUiState(
    val shots: List<ShotChartDataEntity> = emptyList(),
    val pendingShots: List<ShotChartDataEntity> = emptyList(),
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val sesiones: List<SesionTiroEntity> = emptyList(),
    val sesionShots: Map<Int, List<ShotChartDataEntity>> = emptyMap(),
    val selectedJugadoraId: Int = -1,
    val showMade: Boolean = true,
    val showMissed: Boolean = true,
    val currentSesion: SesionTiroEntity? = null,
    val historialSesionId: Int? = null,
    val tab: ShotChartTab = ShotChartTab.SESION
)

enum class ShotChartTab { SESION, HISTORIAL }

class ShotChartViewModel(
    private val entrenadorId: Int,
    private val shotRepo: ShotChartRepository,
    private val jugadoraRepo: JugadoraRepository,
    private val sesionTiroRepo: SesionTiroRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ShotChartUiState())
    val state: StateFlow<ShotChartUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                jugadoraRepo.getAllJugadoras(entrenadorId),
                sesionTiroRepo.getByEntrenador(entrenadorId)
            ) { jugadoras, sesiones -> jugadoras to sesiones }
            .collect { (jugadoras, sesiones) ->
                _state.update { it.copy(jugadoras = jugadoras, sesiones = sesiones) }
                loadHistorialShots(sesiones)
            }
        }

        viewModelScope.launch {
            _state
                .map { it.currentSesion?.id ?: it.historialSesionId ?: -1 }
                .distinctUntilChanged()
                .collect { sesionId ->
                    if (sesionId > 0) {
                        shotRepo.getBySesion(sesionId).collect { shots ->
                            _state.update { it.copy(shots = shots, pendingShots = emptyList()) }
                        }
                    } else {
                        _state.update { it.copy(shots = emptyList(), pendingShots = emptyList()) }
                    }
                }
        }
    }

    private fun loadHistorialShots(sesiones: List<SesionTiroEntity>) {
        viewModelScope.launch {
            val map = mutableMapOf<Int, List<ShotChartDataEntity>>()
            sesiones.forEach { s -> map[s.id] = shotRepo.getBySesionSync(s.id) }
            _state.update { it.copy(sesionShots = map) }
        }
    }

    fun startNewSession(jugadoraId: Int) {
        val titulo = _state.value.jugadoras.find { it.id == jugadoraId }?.nombre ?: "Equipo"
        viewModelScope.launch {
            val id = sesionTiroRepo.insert(
                SesionTiroEntity(
                    idEntrenador = entrenadorId, idJugadora = jugadoraId,
                    titulo = titulo, fecha = System.currentTimeMillis()
                )
            ).toInt()
            val sesion = sesionTiroRepo.getById(id)
            _state.update { it.copy(
                currentSesion = sesion, historialSesionId = null,
                selectedJugadoraId = jugadoraId, tab = ShotChartTab.SESION
            ) }
        }
    }

    fun endSession() {
        val sesiones = _state.value.sesiones
        _state.update { it.copy(currentSesion = null, shots = emptyList(), pendingShots = emptyList()) }
        loadHistorialShots(sesiones)
    }

    fun viewHistorialSesion(sesionId: Int) {
        val sesion = _state.value.sesiones.find { it.id == sesionId }
        _state.update { it.copy(
            historialSesionId = sesionId, currentSesion = null,
            selectedJugadoraId = sesion?.idJugadora ?: -1, tab = ShotChartTab.SESION
        ) }
    }

    fun exitHistorialView() {
        _state.update { it.copy(historialSesionId = null, shots = emptyList()) }
    }

    fun deleteSesion(sesionId: Int) {
        viewModelScope.launch {
            shotRepo.getBySesionSync(sesionId).forEach { shotRepo.deleteById(it.id) }
            sesionTiroRepo.deleteById(sesionId)
            if (_state.value.currentSesion?.id == sesionId) endSession()
            if (_state.value.historialSesionId == sesionId) exitHistorialView()
        }
    }

    fun selectJugadora(id: Int) { _state.update { it.copy(selectedJugadoraId = id) } }
    fun toggleShowMade() { _state.update { it.copy(showMade = !it.showMade) } }
    fun toggleShowMissed() { _state.update { it.copy(showMissed = !it.showMissed) } }
    fun setTab(tab: ShotChartTab) { _state.update { it.copy(tab = tab) } }

    fun addShot(x: Float, y: Float, zona: String, hecha: Boolean) {
        val sesionId = _state.value.currentSesion?.id ?: return
        val jugadoraId = _state.value.selectedJugadoraId.let { if (it <= 0) 0 else it }
        val tempId = -(System.currentTimeMillis().toInt())
        _state.update { it.copy(pendingShots = it.pendingShots + ShotChartDataEntity(
            id = tempId, idPartido = 0, idJugadora = jugadoraId,
            idEntrenador = entrenadorId, zonaX = x, zonaY = y,
            zona = zona, hecha = hecha, sesionId = sesionId
        )) }
        viewModelScope.launch {
            shotRepo.insert(ShotChartDataEntity(
                idPartido = 0, idJugadora = jugadoraId,
                idEntrenador = entrenadorId, zonaX = x, zonaY = y,
                zona = zona, hecha = hecha, sesionId = sesionId
            ))
        }
    }

    fun deleteShotById(id: Int) {
        if (id < 0) _state.update { it.copy(pendingShots = it.pendingShots.filter { s -> s.id != id }) }
        else if (id > 0) viewModelScope.launch { shotRepo.deleteById(id) }
    }

    fun clearAll() {
        val sesionId = _state.value.currentSesion?.id ?: return
        _state.update { it.copy(pendingShots = emptyList()) }
        viewModelScope.launch { shotRepo.getBySesionSync(sesionId).forEach { shotRepo.deleteById(it.id) } }
    }

    fun getFilteredShots(): List<ShotChartDataEntity> {
        val s = _state.value
        return (s.shots + s.pendingShots)
            .filter { if (s.selectedJugadoraId > 0) it.idJugadora == s.selectedJugadoraId else true }
            .filter { if (!s.showMade) !it.hecha else true }
            .filter { if (!s.showMissed) it.hecha else true }
    }

    fun zoneStats(shots: List<ShotChartDataEntity>): Map<String, Pair<Int, Int>> =
        shots.groupBy { it.zona }.mapValues { (_, l) -> l.count { it.hecha } to l.size }

    fun overallPct(shots: List<ShotChartDataEntity>): Int =
        if (shots.isEmpty()) 0 else shots.count { it.hecha } * 100 / shots.size
}
