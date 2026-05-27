package com.example.tfg_aaron.ui.screens.jugadoras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.EventoPartidoEntity
import com.example.tfg_aaron.data.local.entities.EventoTipo
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.repository.EstadisticaRepository
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.PartidoRepository
import com.example.tfg_aaron.data.repository.ScoutingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class JugadorasUiState(
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

data class HistorialPartidoStat(
    val partidoId: Int,
    val rival: String,
    val fecha: Long,
    val puntos: Int,
    val rebotes: Int,
    val asistencias: Int,
    val robos: Int,
    val perdidas: Int,
    val tapones: Int,
    val faltas: Int,
    val valoracion: Int
)

data class PromediosHistoricos(
    val partidosJugados: Int = 0,
    val ptsMedia: Float = 0f,
    val rebMedia: Float = 0f,
    val astMedia: Float = 0f,
    val robMedia: Float = 0f,
    val perMedia: Float = 0f,
    val tapMedia: Float = 0f,
    val falMedia: Float = 0f,
    val valMedia: Float = 0f,
    val pct2: Float = 0f,
    val pct3: Float = 0f,
    val pctTL: Float = 0f
)

data class JugadoraDetailState(
    val jugadora: JugadoraEntity? = null,
    val promedioTiros: Float = 0f,
    val totalIntentados: Int = 0,
    val totalAcertados: Int = 0,
    val isLoading: Boolean = true,
    val historial: List<HistorialPartidoStat> = emptyList(),
    val promedios: PromediosHistoricos = PromediosHistoricos()
)

class JugadorasViewModel(
    private val entrenadorId: Int,
    private val jugadoraRepository: JugadoraRepository,
    private val estadisticaRepository: EstadisticaRepository? = null,
    private val scoutingRepository: ScoutingRepository? = null,
    private val partidoRepository: PartidoRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(JugadorasUiState())
    val uiState: StateFlow<JugadorasUiState> = _uiState.asStateFlow()

    private val _detailState = MutableStateFlow(JugadoraDetailState())
    val detailState: StateFlow<JugadoraDetailState> = _detailState.asStateFlow()

    val posiciones = listOf("Base", "Escolta", "Alero", "Ala-Pívot", "Pívot")
    val roles = listOf("Titular", "Suplente", "Reserva")

    init {
        loadJugadoras()
    }

    private fun loadJugadoras() {
        viewModelScope.launch {
            jugadoraRepository.getAllJugadoras(entrenadorId).collect { list ->
                _uiState.update { it.copy(jugadoras = list) }
            }
        }
    }

    fun loadDetail(jugadoraId: Int) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            val jugadora = jugadoraRepository.getById(jugadoraId)
            val promedio = estadisticaRepository?.getPromedio(jugadoraId) ?: 0f
            val intentados = estadisticaRepository?.getTotalIntentados(jugadoraId) ?: 0
            val acertados = estadisticaRepository?.getTotalAcertados(jugadoraId) ?: 0

            // Load historical match stats
            val (historial, promedios) = loadHistorialPartidos(jugadoraId)

            _detailState.update {
                it.copy(
                    jugadora = jugadora,
                    promedioTiros = promedio,
                    totalIntentados = intentados,
                    totalAcertados = acertados,
                    isLoading = false,
                    historial = historial,
                    promedios = promedios
                )
            }
        }
    }

    private suspend fun loadHistorialPartidos(jugadoraId: Int): Pair<List<HistorialPartidoStat>, PromediosHistoricos> {
        val repo = partidoRepository ?: return emptyList<HistorialPartidoStat>() to PromediosHistoricos()

        val partidoIds = repo.getPartidoIdsByJugadora(jugadoraId)
        if (partidoIds.isEmpty()) return emptyList<HistorialPartidoStat>() to PromediosHistoricos()

        val historial = mutableListOf<HistorialPartidoStat>()
        var totalT2 = 0; var totalT2a = 0; var totalT3 = 0; var totalT3a = 0
        var totalTL = 0; var totalTLa = 0

        for (pId in partidoIds) {
            val partido = repo.getPartidoById(pId) ?: continue
            val eventos = repo.getEventosOnce(pId)
            val playerEvents = eventos.filter { it.idJugadora == jugadoraId }

            var pts = 0; var t2 = 0; var t2a = 0; var t3 = 0; var t3a = 0
            var tl = 0; var tla = 0
            var rebOf = 0; var rebDef = 0; var ast = 0; var rob = 0
            var per = 0; var tap = 0; var fal = 0

            for (e in playerEvents) {
                when (e.tipoEvento) {
                    EventoTipo.PUNTO_2 -> { pts += 2; t2++; t2a++ }
                    EventoTipo.PUNTO_3 -> { pts += 3; t3++; t3a++ }
                    EventoTipo.LIBRE_ANOTADO -> { pts += 1; tl++; tla++ }
                    EventoTipo.LIBRE_FALLADO -> tl++
                    EventoTipo.REBOTE_OFENSIVO -> rebOf++
                    EventoTipo.REBOTE_DEFENSIVO -> rebDef++
                    EventoTipo.ASISTENCIA -> ast++
                    EventoTipo.ROBO -> rob++
                    EventoTipo.PERDIDA -> per++
                    EventoTipo.TAPON -> tap++
                    EventoTipo.FALTA_PERSONAL, EventoTipo.FALTA_PERSONAL_TIRO,
                    EventoTipo.FALTA_TECNICA, EventoTipo.FALTA_ANTIDEPORTIVA -> fal++
                }
            }

            // Only count matches where the player had events (excluding just QUINTETO_INICIAL)
            val hadAction = playerEvents.any {
                it.tipoEvento != EventoTipo.QUINTETO_INICIAL &&
                it.tipoEvento != EventoTipo.SUSTITUCION_ENTRA &&
                it.tipoEvento != EventoTipo.SUSTITUCION_SALE
            }
            // Even if no stats, if she was in quinteto, count the match
            val played = playerEvents.any {
                it.tipoEvento == EventoTipo.QUINTETO_INICIAL || it.tipoEvento == EventoTipo.SUSTITUCION_ENTRA
            }
            if (!played) continue

            val reb = rebOf + rebDef
            val fgMade = t2a + t3a
            val fgMissed = (t2 - t2a) + (t3 - t3a)
            val tlMissed = tl - tla
            val valoracion = pts + reb + ast + rob + tap + tla + fgMade - fgMissed - tlMissed - per - fal

            historial.add(
                HistorialPartidoStat(
                    partidoId = pId, rival = partido.rival,
                    fecha = partido.fecha, puntos = pts,
                    rebotes = reb, asistencias = ast,
                    robos = rob, perdidas = per, tapones = tap,
                    faltas = fal, valoracion = valoracion
                )
            )

            totalT2 += t2; totalT2a += t2a; totalT3 += t3; totalT3a += t3a
            totalTL += tl; totalTLa += tla
        }

        val n = historial.size
        val promedios = if (n > 0) {
            PromediosHistoricos(
                partidosJugados = n,
                ptsMedia = historial.sumOf { it.puntos } / n.toFloat(),
                rebMedia = historial.sumOf { it.rebotes } / n.toFloat(),
                astMedia = historial.sumOf { it.asistencias } / n.toFloat(),
                robMedia = historial.sumOf { it.robos } / n.toFloat(),
                perMedia = historial.sumOf { it.perdidas } / n.toFloat(),
                tapMedia = historial.sumOf { it.tapones } / n.toFloat(),
                falMedia = historial.sumOf { it.faltas } / n.toFloat(),
                valMedia = historial.sumOf { it.valoracion } / n.toFloat(),
                pct2 = if (totalT2 > 0) totalT2a * 100f / totalT2 else 0f,
                pct3 = if (totalT3 > 0) totalT3a * 100f / totalT3 else 0f,
                pctTL = if (totalTL > 0) totalTLa * 100f / totalTL else 0f
            )
        } else PromediosHistoricos()

        return historial.sortedByDescending { it.fecha } to promedios
    }

    fun addJugadora(
        nombre: String, apellidos: String, numero: Int,
        posicion: String, rol: String, edad: Int,
        altura: Float, notas: String, fotoUri: String = ""
    ) {
        if (nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        viewModelScope.launch {
            jugadoraRepository.add(
                JugadoraEntity(
                    idEntrenador = entrenadorId,
                    nombre = nombre.trim(),
                    apellidos = apellidos.trim(),
                    numero = numero,
                    posicion = posicion,
                    rol = rol,
                    edad = edad,
                    altura = altura,
                    notas = notas.trim(),
                    fotoUri = fotoUri
                )
            )
            _uiState.update { it.copy(successMessage = "Jugadora añadida correctamente", error = null) }
        }
    }

    fun updateJugadora(jugadora: JugadoraEntity) {
        viewModelScope.launch {
            jugadoraRepository.update(jugadora)
            _uiState.update { it.copy(successMessage = "Jugadora actualizada", error = null) }
            loadDetail(jugadora.id)
        }
    }

    fun deleteJugadora(jugadora: JugadoraEntity) {
        viewModelScope.launch {
            jugadoraRepository.delete(jugadora)
            _uiState.update { it.copy(successMessage = "Jugadora eliminada") }
        }
    }

    fun updateAreasMejora(jugadoraId: Int, areas: String) {
        viewModelScope.launch {
            val jugadora = jugadoraRepository.getById(jugadoraId) ?: return@launch
            jugadoraRepository.update(jugadora.copy(areasMejora = areas))
            loadDetail(jugadoraId)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
