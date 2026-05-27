package com.example.tfg_aaron.ui.screens.objetivo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.ObjetivoEntity
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.ObjetivoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ObjetivoUiState(
    val objetivos: List<ObjetivoEntity> = emptyList(),
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val filtroTipo: String = "TODOS" // TODOS, EQUIPO, JUGADORA, completados
)

class ObjetivoViewModel(
    private val entrenadorId: Int,
    private val repo: ObjetivoRepository,
    private val jugadoraRepo: JugadoraRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ObjetivoUiState())
    val state: StateFlow<ObjetivoUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.getByEntrenador(entrenadorId),
                jugadoraRepo.getAllJugadoras(entrenadorId)
            ) { objetivos, jugadoras -> objetivos to jugadoras }
            .collect { (objetivos, jugadoras) ->
                _state.update { it.copy(objetivos = objetivos, jugadoras = jugadoras) }
            }
        }
    }

    fun setFiltro(tipo: String) { _state.update { it.copy(filtroTipo = tipo) } }

    fun filteredObjetivos(): List<ObjetivoEntity> {
        val s = _state.value
        return when (s.filtroTipo) {
            "EQUIPO" -> s.objetivos.filter { it.idJugadora == -1 && !it.completado }
            "JUGADORA" -> s.objetivos.filter { it.idJugadora > 0 && !it.completado }
            "COMPLETADOS" -> s.objetivos.filter { it.completado }
            else -> s.objetivos.filter { !it.completado }
        }
    }

    fun saveObjetivo(
        id: Int, jugadoraId: Int, titulo: String, descripcion: String,
        tipo: String, metrica: String, valorObj: Float, unidad: String,
        fechaFin: Long, notas: String
    ) {
        viewModelScope.launch {
            val entity = ObjetivoEntity(
                id = id, idEntrenador = entrenadorId, idJugadora = jugadoraId,
                titulo = titulo, descripcion = descripcion, tipo = tipo,
                metrica = metrica, valorObjetivo = valorObj, unidad = unidad,
                fechaFin = fechaFin, notas = notas,
                fechaInicio = System.currentTimeMillis()
            )
            if (id == 0) repo.insert(entity) else repo.update(entity)
        }
    }

    fun updateProgreso(objetivo: ObjetivoEntity, nuevoValor: Float) {
        viewModelScope.launch {
            val completado = if (objetivo.valorObjetivo > 0) nuevoValor >= objetivo.valorObjetivo else false
            repo.update(objetivo.copy(valorActual = nuevoValor, completado = completado))
        }
    }

    fun toggleCompletado(objetivo: ObjetivoEntity) {
        viewModelScope.launch { repo.update(objetivo.copy(completado = !objetivo.completado)) }
    }

    fun deleteObjetivo(id: Int) {
        viewModelScope.launch { repo.deleteById(id) }
    }
}
