package com.example.tfg_aaron.ui.screens.scouting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.ScoutingEntity
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.ScoutingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ScoutingUiState(
    val scoutingList: List<ScoutingEntity> = emptyList(),
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ScoutingViewModel(
    private val entrenadorId: Int,
    private val scoutingRepository: ScoutingRepository,
    private val jugadoraRepository: JugadoraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoutingUiState())
    val uiState: StateFlow<ScoutingUiState> = _uiState.asStateFlow()

    val categorias = listOf("GENERAL", "ATAQUE", "DEFENSA", "FISICO", "MENTAL", "TECNICO")
    val tipos = listOf("RIVAL", "PROPIA")

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                scoutingRepository.getAll(entrenadorId).collect { list ->
                    _uiState.update { it.copy(scoutingList = list) }
                }
            }
            launch {
                jugadoraRepository.getActivas(entrenadorId).collect { list ->
                    _uiState.update { it.copy(jugadoras = list) }
                }
            }
        }
    }

    fun addScouting(
        rivalNombre: String,
        observaciones: String,
        tipo: String,
        categoria: String,
        valoracion: Int,
        idJugadora: Int?
    ) {
        if (observaciones.isBlank()) {
            _uiState.update { it.copy(error = "Las observaciones son obligatorias") }
            return
        }
        viewModelScope.launch {
            scoutingRepository.add(
                ScoutingEntity(
                    idEntrenador = entrenadorId,
                    idJugadora = if (idJugadora != null && idJugadora > 0) idJugadora else null,
                    rivalNombre = rivalNombre.trim(),
                    observaciones = observaciones.trim(),
                    tipo = tipo,
                    categoria = categoria,
                    valoracion = valoracion
                )
            )
            _uiState.update { it.copy(successMessage = "Scouting guardado", error = null) }
        }
    }

    fun deleteScouting(scouting: ScoutingEntity) {
        viewModelScope.launch {
            scoutingRepository.delete(scouting)
            _uiState.update { it.copy(successMessage = "Scouting eliminado") }
        }
    }

    fun getJugadoraNombre(idJugadora: Int?): String {
        if (idJugadora == null) return ""
        return _uiState.value.jugadoras.find { it.id == idJugadora }
            ?.let { "${it.nombre} ${it.apellidos}" } ?: ""
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
