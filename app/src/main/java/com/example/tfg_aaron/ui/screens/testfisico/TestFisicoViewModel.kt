package com.example.tfg_aaron.ui.screens.testfisico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.TestFisicoEntity
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.TestFisicoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TestFisicoUiState(
    val tests: List<TestFisicoEntity> = emptyList(),
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val tipoFiltro: String = "TODOS",
    val isSaving: Boolean = false
)

class TestFisicoViewModel(
    private val entrenadorId: Int,
    private val jugadoraId: Int = -1,
    private val repo: TestFisicoRepository,
    private val jugadoraRepo: JugadoraRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TestFisicoUiState())
    val state: StateFlow<TestFisicoUiState> = _state.asStateFlow()

    init {
        val testFlow = if (jugadoraId > 0) repo.getByJugadora(jugadoraId) else repo.getByEntrenador(entrenadorId)
        viewModelScope.launch {
            combine(
                testFlow,
                jugadoraRepo.getAllJugadoras(entrenadorId)
            ) { tests, jugadoras -> tests to jugadoras }
            .collect { (tests, jugadoras) ->
                _state.update { it.copy(tests = tests, jugadoras = jugadoras) }
            }
        }
    }

    fun setTipoFiltro(tipo: String) { _state.update { it.copy(tipoFiltro = tipo) } }

    fun saveTest(
        jugadoraIdArg: Int, tipo: String, nombreCustom: String,
        valor: Double, unidad: String, notas: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repo.insert(TestFisicoEntity(
                idJugadora = jugadoraIdArg,
                idEntrenador = entrenadorId,
                tipo = tipo,
                nombreCustom = nombreCustom,
                valor = valor,
                unidad = unidad,
                fecha = System.currentTimeMillis(),
                notas = notas
            ))
            _state.update { it.copy(isSaving = false) }
        }
    }

    fun deleteTest(id: Int) {
        viewModelScope.launch { repo.deleteById(id) }
    }

    fun updateTest(entity: com.example.tfg_aaron.data.local.entities.TestFisicoEntity) {
        viewModelScope.launch { repo.update(entity) }
    }
}
