package com.example.tfg_aaron.ui.screens.skillrating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.SkillRatingEntity
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.SkillRatingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SkillRatingUiState(
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val ratings: List<SkillRatingEntity> = emptyList(),
    val selectedJugadoraId: Int = -1,
    val isSaving: Boolean = false
)

class SkillRatingViewModel(
    private val entrenadorId: Int,
    private val jugadoraRepo: JugadoraRepository,
    private val skillRepo: SkillRatingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SkillRatingUiState())
    val state: StateFlow<SkillRatingUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            jugadoraRepo.getAllJugadoras(entrenadorId).collect { jugadoras ->
                val first = jugadoras.firstOrNull()
                _state.update { it.copy(jugadoras = jugadoras, selectedJugadoraId = first?.id ?: -1) }
                if (first != null) loadRatings(first.id)
            }
        }
    }

    fun selectJugadora(id: Int) {
        _state.update { it.copy(selectedJugadoraId = id) }
        loadRatings(id)
    }

    private fun loadRatings(jugadoraId: Int) {
        viewModelScope.launch {
            skillRepo.getByJugadora(jugadoraId).collect { ratings ->
                _state.update { it.copy(ratings = ratings) }
            }
        }
    }

    fun saveRating(
        jugadoraId: Int, tiro: Int, defensa: Int, balonMano: Int,
        vision: Int, atletismo: Int, mentalidad: Int, liderazgo: Int, notas: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            skillRepo.insert(SkillRatingEntity(
                idJugadora = jugadoraId, idEntrenador = entrenadorId,
                fecha = System.currentTimeMillis(),
                tiro = tiro, defensa = defensa, balonMano = balonMano,
                vision = vision, atletismo = atletismo,
                mentalidad = mentalidad, liderazgo = liderazgo, notas = notas
            ))
            _state.update { it.copy(isSaving = false) }
        }
    }

    fun deleteRating(id: Int) {
        viewModelScope.launch { skillRepo.deleteById(id) }
    }
}
