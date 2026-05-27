package com.example.tfg_aaron.ui.screens.clasificacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.ClasificacionEntity
import com.example.tfg_aaron.data.repository.ClasificacionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ClasificacionUiState(
    val equipos: List<ClasificacionEntity> = emptyList(),
    val temporadaActual: String = "2024-25",
    val temporadas: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val savedMessage: String? = null,
    val errorMessage: String? = null
)

class ClasificacionViewModel(
    private val entrenadorId: Int,
    private val repository: ClasificacionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClasificacionUiState())
    val state: StateFlow<ClasificacionUiState> = _state.asStateFlow()

    init {
        // Collect temporadas first, then equipos for the current temporada
        viewModelScope.launch {
            repository.getTemporadas(entrenadorId).collect { temporadas ->
                val current = _state.value.temporadaActual
                val effectiveTemporada = if (temporadas.contains(current)) current
                                        else temporadas.firstOrNull() ?: "2024-25"
                _state.update { it.copy(temporadas = temporadas, temporadaActual = effectiveTemporada) }
            }
        }
        viewModelScope.launch {
            _state.flatMapLatest { s ->
                repository.getByEntrenadorAndTemporada(entrenadorId, s.temporadaActual)
            }.collect { equipos ->
                _state.update { it.copy(equipos = equipos, isLoading = false) }
            }
        }
    }

    fun setTemporada(t: String) {
        _state.update { it.copy(temporadaActual = t, isLoading = true) }
    }

    fun saveEquipo(entity: ClasificacionEntity) {
        viewModelScope.launch {
            try {
                repository.insertOrUpdate(entity)
                _state.update { it.copy(savedMessage = "Equipo guardado") }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al guardar: ${e.message}") }
            }
        }
    }

    fun deleteEquipo(entity: ClasificacionEntity) {
        viewModelScope.launch {
            try {
                repository.delete(entity)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error al eliminar: ${e.message}") }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(savedMessage = null, errorMessage = null) }
    }

    fun recalcularPuntos(entity: ClasificacionEntity): ClasificacionEntity =
        entity.copy(puntos = entity.pg * 2 + entity.pp)
}
