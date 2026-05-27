package com.example.tfg_aaron.ui.screens.rivalprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.RivalPerfilEntity
import com.example.tfg_aaron.data.repository.RivalPerfilRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RivalPerfilUiState(
    val rivales: List<RivalPerfilEntity> = emptyList(),
    val isSaving: Boolean = false
)

class RivalPerfilViewModel(
    private val entrenadorId: Int,
    private val repo: RivalPerfilRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RivalPerfilUiState())
    val state: StateFlow<RivalPerfilUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getByEntrenador(entrenadorId).collect { rivales ->
                _state.update { it.copy(rivales = rivales) }
            }
        }
    }

    fun saveRival(
        id: Int, nombre: String, temporada: String,
        sistemaOfensivo: String, sistemaDefensivo: String,
        jugadoresClave: String, fortalezas: String, debilidades: String, notas: String,
        victorias: Int = 0, derrotas: Int = 0
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val entity = RivalPerfilEntity(
                id = id, idEntrenador = entrenadorId, nombre = nombre,
                temporada = temporada, sistemaOfensivo = sistemaOfensivo,
                sistemaDefensivo = sistemaDefensivo, jugadoresClave = jugadoresClave,
                fortalezas = fortalezas, debilidades = debilidades, notas = notas,
                victorias = victorias, derrotas = derrotas
            )
            if (id == 0) repo.insert(entity) else repo.update(entity)
            _state.update { it.copy(isSaving = false) }
        }
    }

    fun deleteRival(id: Int) {
        viewModelScope.launch { repo.deleteById(id) }
    }
}
