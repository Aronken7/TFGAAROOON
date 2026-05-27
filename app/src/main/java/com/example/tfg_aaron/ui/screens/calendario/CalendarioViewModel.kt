package com.example.tfg_aaron.ui.screens.calendario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import com.example.tfg_aaron.data.local.entities.SesionEntrenamientoEntity
import com.example.tfg_aaron.data.repository.PartidoRepository
import com.example.tfg_aaron.data.repository.SesionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class CalendarioEvento {
    data class Partido(val entity: PartidoEntity) : CalendarioEvento()
    data class Sesion(val entity: SesionEntrenamientoEntity) : CalendarioEvento()
}

data class CalendarioUiState(
    val eventosPorDia: Map<Int, List<CalendarioEvento>> = emptyMap(),
    val mesActual: Int = Calendar.getInstance().get(Calendar.MONTH),
    val anioActual: Int = Calendar.getInstance().get(Calendar.YEAR),
    val diaSeleccionado: Int? = null,
    val partidosGanados: Int = 0,
    val partidosPerdidos: Int = 0,
    val sesionesDelMes: Int = 0
)

class CalendarioViewModel(
    private val entrenadorId: Int,
    private val partidoRepository: PartidoRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarioUiState())
    val uiState: StateFlow<CalendarioUiState> = _uiState.asStateFlow()

    init {
        loadEventos()
    }

    private fun loadEventos() {
        viewModelScope.launch {
            combine(
                partidoRepository.getPartidosByEntrenador(entrenadorId),
                sesionRepository.getAll(entrenadorId)
            ) { partidos, sesiones ->
                Pair(partidos, sesiones)
            }.collect { (partidos, sesiones) ->
                recalcularMes(
                    partidos,
                    sesiones,
                    _uiState.value.mesActual,
                    _uiState.value.anioActual
                )
            }
        }
    }

    private fun recalcularMes(
        partidos: List<PartidoEntity>,
        sesiones: List<SesionEntrenamientoEntity>,
        mes: Int,
        anio: Int
    ) {
        val eventosPorDia = mutableMapOf<Int, MutableList<CalendarioEvento>>()

        partidos.forEach { partido ->
            val cal = Calendar.getInstance().apply { timeInMillis = partido.fecha }
            if (cal.get(Calendar.MONTH) == mes && cal.get(Calendar.YEAR) == anio) {
                val dia = cal.get(Calendar.DAY_OF_MONTH)
                eventosPorDia.getOrPut(dia) { mutableListOf() }
                    .add(CalendarioEvento.Partido(partido))
            }
        }

        sesiones.forEach { sesion ->
            val cal = Calendar.getInstance().apply { timeInMillis = sesion.fechaInicio }
            if (cal.get(Calendar.MONTH) == mes && cal.get(Calendar.YEAR) == anio) {
                val dia = cal.get(Calendar.DAY_OF_MONTH)
                eventosPorDia.getOrPut(dia) { mutableListOf() }
                    .add(CalendarioEvento.Sesion(sesion))
            }
        }

        val ganados = partidos.count { p ->
            val cal = Calendar.getInstance().apply { timeInMillis = p.fecha }
            cal.get(Calendar.MONTH) == mes && cal.get(Calendar.YEAR) == anio &&
                    p.puntosPropio > p.puntosRival
        }
        val perdidos = partidos.count { p ->
            val cal = Calendar.getInstance().apply { timeInMillis = p.fecha }
            cal.get(Calendar.MONTH) == mes && cal.get(Calendar.YEAR) == anio &&
                    p.puntosPropio < p.puntosRival
        }
        val sesionesCount = sesiones.count { s ->
            val cal = Calendar.getInstance().apply { timeInMillis = s.fechaInicio }
            cal.get(Calendar.MONTH) == mes && cal.get(Calendar.YEAR) == anio
        }

        _uiState.value = _uiState.value.copy(
            eventosPorDia = eventosPorDia,
            mesActual = mes,
            anioActual = anio,
            partidosGanados = ganados,
            partidosPerdidos = perdidos,
            sesionesDelMes = sesionesCount
        )
    }

    fun navegarMes(delta: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _uiState.value.anioActual)
            set(Calendar.MONTH, _uiState.value.mesActual)
            add(Calendar.MONTH, delta)
        }
        val nuevoMes = cal.get(Calendar.MONTH)
        val nuevoAnio = cal.get(Calendar.YEAR)
        _uiState.value = _uiState.value.copy(mesActual = nuevoMes, anioActual = nuevoAnio)
        loadEventos()
    }

    fun seleccionarDia(dia: Int?) {
        _uiState.value = _uiState.value.copy(diaSeleccionado = dia)
    }
}
