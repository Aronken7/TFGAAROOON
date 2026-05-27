package com.example.tfg_aaron.ui.screens.gameplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.GamePlanEntity
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.repository.GamePlanRepository
import com.example.tfg_aaron.data.repository.JugadoraRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GamePlanUiState(
    val planes: List<GamePlanEntity> = emptyList(),
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val isSaving: Boolean = false
)

class GamePlanViewModel(
    private val entrenadorId: Int,
    private val repo: GamePlanRepository,
    private val jugadoraRepo: JugadoraRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GamePlanUiState())
    val state: StateFlow<GamePlanUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.getByEntrenador(entrenadorId),
                jugadoraRepo.getAllJugadoras(entrenadorId)
            ) { planes, jugadoras -> planes to jugadoras }
            .collect { (planes, jugadoras) ->
                _state.update { it.copy(planes = planes, jugadoras = jugadoras) }
            }
        }
    }

    fun savePlan(
        id: Int, rival: String, descripcion: String,
        focoOfensivo: String, focoDefensivo: String,
        jugadasClave: String, sistemaDefensivo: String,
        ajustesIndividuales: String, notasFinales: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val entity = GamePlanEntity(
                id = id, idEntrenador = entrenadorId,
                rival = rival, fecha = System.currentTimeMillis(),
                descripcion = descripcion, focoOfensivo = focoOfensivo,
                focoDefensivo = focoDefensivo, jugadasClave = jugadasClave,
                sistemaDefensivo = sistemaDefensivo, ajustesIndividuales = ajustesIndividuales,
                notasFinales = notasFinales
            )
            if (id == 0) repo.insert(entity) else repo.update(entity)
            _state.update { it.copy(isSaving = false) }
        }
    }

    fun deletePlan(id: Int) {
        viewModelScope.launch { repo.deleteById(id) }
    }
}
