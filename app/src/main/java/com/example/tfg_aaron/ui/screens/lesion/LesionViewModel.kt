package com.example.tfg_aaron.ui.screens.lesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.LesionEntity
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.LesionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LesionUiState(
    val lesiones: List<LesionEntity> = emptyList(),
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val filtroEstado: String = "TODAS" // TODAS, ACTIVA, RECUPERADA
)

class LesionViewModel(
    private val entrenadorId: Int,
    private val repo: LesionRepository,
    private val jugadoraRepo: JugadoraRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LesionUiState())
    val state: StateFlow<LesionUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.getByEntrenador(entrenadorId),
                jugadoraRepo.getAllJugadoras(entrenadorId)
            ) { lesiones, jugadoras -> lesiones to jugadoras }
            .collect { (lesiones, jugadoras) ->
                _state.update { it.copy(lesiones = lesiones, jugadoras = jugadoras) }
            }
        }
    }

    fun setFiltro(estado: String) { _state.update { it.copy(filtroEstado = estado) } }

    fun lesionesFiltered(): List<LesionEntity> {
        val s = _state.value
        return when (s.filtroEstado) {
            "ACTIVA" -> s.lesiones.filter { it.estado == "ACTIVA" }
            "RECUPERADA" -> s.lesiones.filter { it.estado == "RECUPERADA" }
            else -> s.lesiones
        }
    }

    fun saveLesion(
        id: Int, jugadoraId: Int, tipo: String, zonaCorpo: String,
        gravedad: Int, fechaInicio: Long, fechaEstimada: Long,
        protocolo: String, notas: String
    ) {
        viewModelScope.launch {
            val entity = LesionEntity(
                id = id, idJugadora = jugadoraId, idEntrenador = entrenadorId,
                tipo = tipo, zonaCorpo = zonaCorpo, gravedad = gravedad,
                fechaInicio = fechaInicio, fechaEstimadaVuelta = fechaEstimada,
                protocolo = protocolo, notas = notas
            )
            if (id == 0) repo.insert(entity) else repo.update(entity)
        }
    }

    fun marcarRecuperada(lesion: LesionEntity) {
        viewModelScope.launch {
            repo.update(lesion.copy(estado = "RECUPERADA", fechaVueltaReal = System.currentTimeMillis()))
        }
    }

    fun deleteLesion(id: Int) {
        viewModelScope.launch { repo.deleteById(id) }
    }
}
