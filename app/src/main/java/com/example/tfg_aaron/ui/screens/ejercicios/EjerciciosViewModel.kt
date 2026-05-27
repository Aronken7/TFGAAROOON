package com.example.tfg_aaron.ui.screens.ejercicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.EjercicioEntity
import com.example.tfg_aaron.data.repository.EjercicioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EjerciciosUiState(
    val ejercicios: List<EjercicioEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedCategoria: String = "TODOS",
    val searchQuery: String = ""
)

val CATEGORIAS = listOf(
    "TODOS", "TIRO", "FINALIZACION", "TECNICA_INDIVIDUAL",
    "TRANSICION", "DEFENSA", "ATAQUE", "FISICO", "CALENTAMIENTO", "OTRO"
)

val CATEGORIAS_LABEL = mapOf(
    "TODOS" to "Todos",
    "TIRO" to "Tiro",
    "FINALIZACION" to "Finalización",
    "TECNICA_INDIVIDUAL" to "Técnica",
    "TRANSICION" to "Transición",
    "DEFENSA" to "Defensa",
    "ATAQUE" to "Ataque",
    "FISICO" to "Físico",
    "CALENTAMIENTO" to "Calentamiento",
    "OTRO" to "Otro"
)

class EjerciciosViewModel(
    private val entrenadorId: Int,
    private val repository: EjercicioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EjerciciosUiState())
    val uiState: StateFlow<EjerciciosUiState> = _uiState.asStateFlow()

    init {
        loadEjercicios()
    }

    private fun loadEjercicios() {
        viewModelScope.launch {
            repository.getAll(entrenadorId).collect { list ->
                _uiState.update { it.copy(ejercicios = list, isLoading = false) }
            }
        }
    }

    fun setCategoria(categoria: String) {
        _uiState.update { it.copy(selectedCategoria = categoria) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getFilteredEjercicios(): List<EjercicioEntity> {
        val state = _uiState.value
        return state.ejercicios
            .filter { ej ->
                (state.selectedCategoria == "TODOS" || ej.categoria == state.selectedCategoria) &&
                (state.searchQuery.isBlank() || ej.nombre.contains(state.searchQuery, ignoreCase = true) ||
                        ej.descripcion.contains(state.searchQuery, ignoreCase = true))
            }
    }

    fun saveEjercicio(
        id: Int,
        nombre: String,
        descripcion: String,
        categoria: String,
        duracionMinutos: Int,
        intensidad: String,
        materialesNecesarios: String,
        jugadoresMinimos: Int,
        notas: String,
        esFavorito: Boolean
    ) {
        if (nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        viewModelScope.launch {
            val entity = EjercicioEntity(
                id = if (id > 0) id else 0,
                entrenadorId = entrenadorId,
                nombre = nombre.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                duracionMinutos = duracionMinutos,
                intensidad = intensidad,
                materialesNecesarios = materialesNecesarios.trim(),
                jugadoresMinimos = jugadoresMinimos,
                notas = notas.trim(),
                esFavorito = esFavorito
            )
            repository.insert(entity)
            _uiState.update { it.copy(successMessage = if (id > 0) "Ejercicio actualizado" else "Ejercicio añadido", error = null) }
        }
    }

    fun toggleFavorito(ejercicio: EjercicioEntity) {
        viewModelScope.launch {
            repository.update(ejercicio.copy(esFavorito = !ejercicio.esFavorito))
        }
    }

    fun deleteEjercicio(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
            _uiState.update { it.copy(successMessage = "Ejercicio eliminado") }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
