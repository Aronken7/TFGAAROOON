package com.example.tfg_aaron.ui.screens.asistencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.AsistenciaEntity
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.repository.AsistenciaRepository
import com.example.tfg_aaron.data.repository.JugadoraRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AsistenciaUiState(
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val asistencias: List<AsistenciaEntity> = emptyList(),
    val isLoading: Boolean = true
)

class AsistenciaViewModel(
    private val entrenadorId: Int,
    private val tipo: String,
    private val referenciaId: Int,
    private val fecha: Long,
    private val asistenciaRepo: AsistenciaRepository,
    private val jugadoraRepo: JugadoraRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AsistenciaUiState())
    val state: StateFlow<AsistenciaUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                jugadoraRepo.getAllJugadoras(entrenadorId),
                asistenciaRepo.getByEvento(tipo, referenciaId)
            ) { jugadoras, asistencias -> jugadoras to asistencias }
            .collect { (jugadoras, asistencias) ->
                _state.update { it.copy(jugadoras = jugadoras, asistencias = asistencias, isLoading = false) }
            }
        }
    }

    fun initAsistencias() {
        viewModelScope.launch {
            val jugadoras = _state.value.jugadoras
            val existentes = _state.value.asistencias.map { it.idJugadora }.toSet()
            jugadoras.filter { it.id !in existentes }.forEach { j ->
                asistenciaRepo.insert(AsistenciaEntity(
                    idJugadora = j.id, idEntrenador = entrenadorId,
                    tipo = tipo, referenciaId = referenciaId,
                    estado = "PRESENTE", fecha = fecha
                ))
            }
        }
    }

    fun updateEstado(jugadoraId: Int, estado: String) {
        viewModelScope.launch {
            val existing = _state.value.asistencias.find { it.idJugadora == jugadoraId }
            if (existing != null) {
                asistenciaRepo.update(existing.copy(estado = estado))
            } else {
                asistenciaRepo.insert(AsistenciaEntity(
                    idJugadora = jugadoraId, idEntrenador = entrenadorId,
                    tipo = tipo, referenciaId = referenciaId,
                    estado = estado, fecha = fecha
                ))
            }
        }
    }
}
