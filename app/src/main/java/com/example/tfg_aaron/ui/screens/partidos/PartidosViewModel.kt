package com.example.tfg_aaron.ui.screens.partidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import com.example.tfg_aaron.data.repository.PartidoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PartidosUiState(
    val partidos: List<PartidoEntity> = emptyList(),
    val victorias: Int = 0,
    val derrotas: Int = 0,
    val promedioAnotado: Float = 0f,
    val promedioEncajado: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class PartidosViewModel(
    private val entrenadorId: Int,
    private val repository: PartidoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartidosUiState())
    val uiState: StateFlow<PartidosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getPartidosByEntrenador(entrenadorId).collect { list ->
                val jugados = list.filter { it.puntosPropio > 0 || it.puntosRival > 0 }
                val wins = jugados.count { it.puntosPropio > it.puntosRival }
                val losses = jugados.count { it.puntosPropio < it.puntosRival }
                val avgFor = if (jugados.isEmpty()) 0f else jugados.map { it.puntosPropio }.average().toFloat()
                val avgAgainst = if (jugados.isEmpty()) 0f else jugados.map { it.puntosRival }.average().toFloat()
                _uiState.value = _uiState.value.copy(
                    partidos = list,
                    victorias = wins,
                    derrotas = losses,
                    promedioAnotado = avgFor,
                    promedioEncajado = avgAgainst
                )
            }
        }
    }

    fun deletePartido(partido: PartidoEntity) {
        viewModelScope.launch { repository.deletePartido(partido) }
    }

    fun savePartido(
        id: Int,
        rival: String,
        lugar: String,
        jornada: Int,
        esLocal: Boolean,
        notas: String,
        puntosPropio: Int,
        puntosRival: Int,
        fecha: Long
    ) {
        if (rival.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "El nombre del rival es obligatorio")
            return
        }
        viewModelScope.launch {
            val partido = PartidoEntity(
                id = if (id > 0) id else 0,
                idEntrenador = entrenadorId,
                rival = rival,
                lugar = lugar,
                jornada = jornada,
                esLocal = esLocal,
                notas = notas,
                puntosPropio = puntosPropio,
                puntosRival = puntosRival,
                fecha = fecha
            )
            repository.savePartido(partido)
            _uiState.value = _uiState.value.copy(successMessage = "Partido guardado", error = null)
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
