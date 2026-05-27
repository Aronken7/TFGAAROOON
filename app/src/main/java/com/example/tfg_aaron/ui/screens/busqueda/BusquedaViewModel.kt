package com.example.tfg_aaron.ui.screens.busqueda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import com.example.tfg_aaron.data.local.entities.ScoutingEntity
import com.example.tfg_aaron.data.local.entities.SesionEntrenamientoEntity
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.PartidoRepository
import com.example.tfg_aaron.data.repository.ScoutingRepository
import com.example.tfg_aaron.data.repository.SesionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SearchResult {
    data class Jugadora(val entity: JugadoraEntity) : SearchResult()
    data class Partido(val entity: PartidoEntity) : SearchResult()
    data class Sesion(val entity: SesionEntrenamientoEntity) : SearchResult()
    data class Scouting(val entity: ScoutingEntity) : SearchResult()
}

data class BusquedaUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false
)

class BusquedaViewModel(
    private val entrenadorId: Int,
    private val jugadoraRepo: JugadoraRepository,
    private val partidoRepo: PartidoRepository,
    private val sesionRepo: SesionRepository,
    private val scoutingRepo: ScoutingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusquedaUiState())
    val uiState: StateFlow<BusquedaUiState> = _uiState.asStateFlow()

    // Local snapshot lists, kept up to date via Flow collection
    private val _jugadoras = MutableStateFlow<List<JugadoraEntity>>(emptyList())
    private val _partidos = MutableStateFlow<List<PartidoEntity>>(emptyList())
    private val _sesiones = MutableStateFlow<List<SesionEntrenamientoEntity>>(emptyList())
    private val _scoutings = MutableStateFlow<List<ScoutingEntity>>(emptyList())

    init {
        viewModelScope.launch {
            launch { jugadoraRepo.getAllJugadoras(entrenadorId).collect { _jugadoras.value = it } }
            launch { partidoRepo.getPartidosByEntrenador(entrenadorId).collect { _partidos.value = it } }
            launch { sesionRepo.getAll(entrenadorId).collect { _sesiones.value = it } }
            launch { scoutingRepo.getAll(entrenadorId).collect { _scoutings.value = it } }
        }
    }

    fun search(query: String) {
        val trimmed = query.trim()
        _uiState.update { it.copy(query = trimmed, isSearching = true, hasSearched = false) }

        if (trimmed.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false, hasSearched = false) }
            return
        }

        val lower = trimmed.lowercase()

        val jugadoraResults = _jugadoras.value
            .filter { j ->
                j.nombre.lowercase().contains(lower) ||
                j.apellidos.lowercase().contains(lower) ||
                j.posicion.lowercase().contains(lower)
            }
            .map { SearchResult.Jugadora(it) }

        val partidoResults = _partidos.value
            .filter { p -> p.rival.lowercase().contains(lower) }
            .map { SearchResult.Partido(it) }

        val sesionResults = _sesiones.value
            .filter { s ->
                s.titulo.lowercase().contains(lower) ||
                s.descripcion.lowercase().contains(lower)
            }
            .map { SearchResult.Sesion(it) }

        val scoutingResults = _scoutings.value
            .filter { sc ->
                sc.rivalNombre.lowercase().contains(lower) ||
                sc.observaciones.lowercase().contains(lower) ||
                sc.tipo.lowercase().contains(lower) ||
                sc.categoria.lowercase().contains(lower)
            }
            .map { SearchResult.Scouting(it) }

        val combined = (jugadoraResults + partidoResults + sesionResults + scoutingResults)
            .take(30)

        _uiState.update { it.copy(results = combined, isSearching = false, hasSearched = true) }
    }

    fun clearSearch() {
        _uiState.update { BusquedaUiState() }
    }
}
