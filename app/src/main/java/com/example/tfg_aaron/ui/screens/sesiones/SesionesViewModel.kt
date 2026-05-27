package com.example.tfg_aaron.ui.screens.sesiones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.SesionEntrenamientoEntity
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.SesionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SesionesUiState(
    val sesiones: List<SesionEntrenamientoEntity> = emptyList(),
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val semanaActual: Int = 1,
    val maxSemana: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val lastSavedSesionId: Int? = null
)

class SesionesViewModel(
    private val entrenadorId: Int,
    private val sesionRepository: SesionRepository,
    private val jugadoraRepository: JugadoraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SesionesUiState())
    val uiState: StateFlow<SesionesUiState> = _uiState.asStateFlow()

    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                sesionRepository.getAll(entrenadorId).collect { list ->
                    val maxSem = list.maxOfOrNull { it.semana } ?: 1
                    _uiState.update { it.copy(sesiones = list, maxSemana = maxSem) }
                }
            }
            launch {
                jugadoraRepository.getActivas(entrenadorId).collect { list ->
                    _uiState.update { it.copy(jugadoras = list) }
                }
            }
        }
    }

    fun setSemana(semana: Int) {
        _uiState.update { it.copy(semanaActual = semana) }
    }

    fun getSesionesDeSemana(): List<SesionEntrenamientoEntity> {
        return _uiState.value.sesiones.filter { it.semana == _uiState.value.semanaActual }
    }

    fun addSesion(
        titulo: String,
        descripcion: String,
        semana: Int,
        diaSemana: String,
        horaInicio: String,
        horaFin: String,
        lugar: String,
        objetivos: String,
        ejercicios: String,
        fecha: Long
    ) {
        if (titulo.isBlank()) {
            _uiState.update { it.copy(error = "El título es obligatorio") }
            return
        }
        viewModelScope.launch {
            val insertedId = sesionRepository.add(
                SesionEntrenamientoEntity(
                    idEntrenador = entrenadorId,
                    titulo = titulo.trim(),
                    descripcion = descripcion.trim(),
                    semana = semana,
                    diaSemana = diaSemana,
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    lugar = lugar.trim(),
                    objetivos = objetivos.trim(),
                    ejercicios = ejercicios.trim(),
                    fechaInicio = fecha,
                    fechaFin = fecha
                )
            )
            _uiState.update {
                it.copy(
                    successMessage = "Sesión creada",
                    error = null,
                    lastSavedSesionId = insertedId.toInt()
                )
            }
        }
    }

    fun toggleCompletada(sesion: SesionEntrenamientoEntity) {
        viewModelScope.launch {
            sesionRepository.update(sesion.copy(completada = !sesion.completada))
        }
    }

    fun deleteSesion(sesion: SesionEntrenamientoEntity) {
        viewModelScope.launch {
            sesionRepository.delete(sesion)
            _uiState.update { it.copy(successMessage = "Sesión eliminada") }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null, lastSavedSesionId = null) }
    }
}
