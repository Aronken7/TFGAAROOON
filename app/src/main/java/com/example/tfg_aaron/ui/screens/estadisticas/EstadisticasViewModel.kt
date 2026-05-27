package com.example.tfg_aaron.ui.screens.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.EstadisticaTirosEntity
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.repository.EstadisticaRepository
import com.example.tfg_aaron.data.repository.JugadoraRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class JugadoraStats(
    val jugadora: JugadoraEntity,
    val promedio: Float,
    val intentados: Int,
    val acertados: Int,
    val registros: List<EstadisticaTirosEntity> = emptyList()
)

data class EstadisticasUiState(
    val jugadorasStats: List<JugadoraStats> = emptyList(),
    val jugadoraSeleccionada: JugadoraEntity? = null,
    val registrosJugadora: List<EstadisticaTirosEntity> = emptyList(),
    val tipoFiltro: String = "LIBRE",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class EstadisticasViewModel(
    private val entrenadorId: Int,
    private val jugadoraRepository: JugadoraRepository,
    private val estadisticaRepository: EstadisticaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadisticasUiState())
    val uiState: StateFlow<EstadisticasUiState> = _uiState.asStateFlow()

    val tiposTiro = listOf("LIBRE", "CAMPO", "TRIPLE")

    init {
        loadJugadoras()
    }

    private fun loadJugadoras() {
        viewModelScope.launch {
            jugadoraRepository.getActivas(entrenadorId).collect { jugadoras ->
                val stats = jugadoras.map { j ->
                    JugadoraStats(
                        jugadora = j,
                        promedio = estadisticaRepository.getPromedio(j.id, _uiState.value.tipoFiltro),
                        intentados = estadisticaRepository.getTotalIntentados(j.id),
                        acertados = estadisticaRepository.getTotalAcertados(j.id)
                    )
                }.sortedByDescending { it.promedio }
                _uiState.update { it.copy(jugadorasStats = stats) }
            }
        }
    }

    fun selectJugadora(jugadora: JugadoraEntity) {
        _uiState.update { it.copy(jugadoraSeleccionada = jugadora) }
        viewModelScope.launch {
            estadisticaRepository.getByJugadoraAndTipo(jugadora.id, _uiState.value.tipoFiltro).collect { registros ->
                _uiState.update { it.copy(registrosJugadora = registros) }
            }
        }
    }

    fun setTipoFiltro(tipo: String) {
        _uiState.update { it.copy(tipoFiltro = tipo) }
        loadJugadoras()
        _uiState.value.jugadoraSeleccionada?.let { selectJugadora(it) }
    }

    fun addEstadistica(idJugadora: Int, intentados: Int, acertados: Int, notas: String) {
        if (intentados <= 0) {
            _uiState.update { it.copy(error = "Los tiros intentados deben ser mayores que 0") }
            return
        }
        if (acertados > intentados) {
            _uiState.update { it.copy(error = "Los acertados no pueden superar los intentados") }
            return
        }
        viewModelScope.launch {
            estadisticaRepository.add(
                idJugadora = idJugadora,
                intentados = intentados,
                acertados = acertados,
                tipo = _uiState.value.tipoFiltro,
                notas = notas
            )
            loadJugadoras()
            _uiState.value.jugadoraSeleccionada?.let { selectJugadora(it) }
            _uiState.update { it.copy(successMessage = "Estadística añadida", error = null) }
        }
    }

    fun deleteEstadistica(estadistica: EstadisticaTirosEntity) {
        viewModelScope.launch {
            estadisticaRepository.delete(estadistica)
            loadJugadoras()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
