package com.example.tfg_aaron.ui.screens.estadisticaspartido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.EstadisticaPartidoEntity
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import com.example.tfg_aaron.data.repository.EstadisticaPartidoRepository
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.PartidoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EstadisticasPartidoUiState(
    val partido: PartidoEntity? = null,
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val stats: List<EstadisticaPartidoEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null
)

data class JugadoraConStats(
    val jugadora: JugadoraEntity,
    val stat: EstadisticaPartidoEntity?
)

class EstadisticasPartidoViewModel(
    private val entrenadorId: Int,
    private val idPartido: Int,
    private val estadisticaPartidoRepository: EstadisticaPartidoRepository,
    private val jugadoraRepository: JugadoraRepository,
    private val partidoRepository: PartidoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadisticasPartidoUiState())
    val uiState: StateFlow<EstadisticasPartidoUiState> = _uiState.asStateFlow()

    val jugadorasConStats: StateFlow<List<JugadoraConStats>> = combine(
        jugadoraRepository.getActivas(entrenadorId),
        estadisticaPartidoRepository.getByPartido(idPartido)
    ) { jugadoras, stats ->
        jugadoras
            .sortedBy { it.numero }
            .map { jugadora ->
                JugadoraConStats(
                    jugadora = jugadora,
                    stat = stats.find { it.idJugadora == jugadora.id }
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        loadPartido()
        observeStats()
    }

    private fun loadPartido() {
        viewModelScope.launch {
            val partido = partidoRepository.getPartidoById(idPartido)
            _uiState.update { it.copy(partido = partido, isLoading = false) }
        }
    }

    private fun observeStats() {
        viewModelScope.launch {
            combine(
                jugadoraRepository.getActivas(entrenadorId),
                estadisticaPartidoRepository.getByPartido(idPartido)
            ) { jugadoras, stats ->
                Pair(jugadoras, stats)
            }.collect { (jugadoras, stats) ->
                _uiState.update { it.copy(jugadoras = jugadoras, stats = stats) }
            }
        }
    }

    fun saveOrUpdateStat(
        idJugadora: Int,
        puntos: Int,
        rebotes: Int,
        asistencias: Int,
        faltas: Int,
        minutos: Int
    ) {
        viewModelScope.launch {
            try {
                val existing = estadisticaPartidoRepository.getByPartidoAndJugadora(idPartido, idJugadora)
                val stat = EstadisticaPartidoEntity(
                    id = existing?.id ?: 0,
                    idPartido = idPartido,
                    idJugadora = idJugadora,
                    puntos = puntos,
                    rebotes = rebotes,
                    asistencias = asistencias,
                    faltas = faltas,
                    minutos = minutos
                )
                estadisticaPartidoRepository.insertOrUpdate(stat)
                _uiState.update { it.copy(successMessage = "Estadísticas guardadas", error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar: ${e.message}") }
            }
        }
    }

    fun deleteStat(stat: EstadisticaPartidoEntity) {
        viewModelScope.launch {
            try {
                estadisticaPartidoRepository.delete(stat)
                _uiState.update { it.copy(successMessage = "Estadística eliminada", error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al eliminar: ${e.message}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
