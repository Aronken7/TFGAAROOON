package com.example.tfg_aaron.ui.screens.historialfisico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.HistorialFisicoEntity
import com.example.tfg_aaron.data.repository.HistorialFisicoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistorialFisicoUiState(
    val registros: List<HistorialFisicoEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class HistorialFisicoViewModel(
    private val jugadoraId: Int,
    private val repository: HistorialFisicoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialFisicoUiState())
    val uiState: StateFlow<HistorialFisicoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getByJugadora(jugadoraId).collect { list ->
                _uiState.value = _uiState.value.copy(registros = list)
            }
        }
    }

    fun addRegistro(
        condicionFisica: String,
        peso: Float,
        altura: Float,
        masaGrasa: Float,
        masaMuscular: Float,
        envergadura: Float,
        alturaSentado: Float,
        perimetroBrazo: Float,
        perimetroCintura: Float,
        perimetroMuslo: Float,
        perimetroCadera: Float,
        perimetroPecho: Float,
        saltoVertical: Float,
        saltoHorizontal: Float,
        sprint10m: Float,
        observaciones: String
    ) {
        viewModelScope.launch {
            val registro = HistorialFisicoEntity(
                idJugadora = jugadoraId,
                condicionFisica = condicionFisica,
                peso = peso,
                altura = altura,
                masaGrasa = masaGrasa,
                masaMuscular = masaMuscular,
                envergadura = envergadura,
                alturaSentado = alturaSentado,
                perimetroBrazo = perimetroBrazo,
                perimetroCintura = perimetroCintura,
                perimetroMuslo = perimetroMuslo,
                perimetroCadera = perimetroCadera,
                perimetroPecho = perimetroPecho,
                saltoVertical = saltoVertical,
                saltoHorizontal = saltoHorizontal,
                sprint10m = sprint10m,
                observaciones = observaciones
            )
            repository.insert(registro)
            _uiState.value = _uiState.value.copy(successMessage = "Registro guardado")
        }
    }

    fun deleteRegistro(registro: HistorialFisicoEntity) {
        viewModelScope.launch { repository.delete(registro) }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
