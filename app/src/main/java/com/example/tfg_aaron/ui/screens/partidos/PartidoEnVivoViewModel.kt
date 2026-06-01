package com.example.tfg_aaron.ui.screens.partidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.*
import com.example.tfg_aaron.data.local.entities.ShotChartDataEntity
import com.example.tfg_aaron.data.repository.JugadoraRepository
import com.example.tfg_aaron.data.repository.PartidoRepository
import com.example.tfg_aaron.data.repository.ShotChartRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class FasePartido { SETUP, EN_JUEGO, FINALIZADO }

data class EstadisticasJugadora(
    val puntos: Int = 0,
    val tirosDos: Int = 0,
    val tirosDosAnotados: Int = 0,
    val tirosTres: Int = 0,
    val tirosTresAnotados: Int = 0,
    val tirosLibres: Int = 0,
    val tirosLibresAnotados: Int = 0,
    val rebotesOfensivos: Int = 0,
    val rebotesDefensivos: Int = 0,
    val asistencias: Int = 0,
    val robos: Int = 0,
    val perdidas: Int = 0,
    val tapones: Int = 0,
    val faltasPersonales: Int = 0,
    val faltasTecnicas: Int = 0,
    val faltasAntideportivas: Int = 0
) {
    val rebotesTotales get() = rebotesOfensivos + rebotesDefensivos
    val pct2 get() = if (tirosDos == 0) 0f else tirosDosAnotados * 100f / tirosDos
    val pct3 get() = if (tirosTres == 0) 0f else tirosTresAnotados * 100f / tirosTres
    val pctTL get() = if (tirosLibres == 0) 0f else tirosLibresAnotados * 100f / tirosLibres
    val faltasTotales get() = faltasPersonales + faltasTecnicas + faltasAntideportivas
    val expulsada get() = faltasPersonales >= 5 || (faltasTecnicas + faltasAntideportivas) >= 2
}

data class EstadisticasRival(
    val puntos: Int = 0,
    val tirosLibres: Int = 0,
    val tirosLibresAnotados: Int = 0,
    val rebotesOfensivos: Int = 0,
    val rebotesDefensivos: Int = 0,
    val asistencias: Int = 0,
    val robos: Int = 0,
    val perdidas: Int = 0,
    val tapones: Int = 0,
    val faltasPersonales: Int = 0,
    val faltasTecnicas: Int = 0,
    val faltasAntideportivas: Int = 0
) {
    val rebotesTotales get() = rebotesOfensivos + rebotesDefensivos
    val faltasTotales get() = faltasPersonales + faltasTecnicas + faltasAntideportivas
    val pctTL get() = if (tirosLibres == 0) 0f else tirosLibresAnotados * 100f / tirosLibres
    val expulsada get() = faltasPersonales >= 5 || (faltasTecnicas + faltasAntideportivas) >= 2
}

data class RivalPlayerState(val id: Int, val nombre: String, val numero: String) {
    val displayName: String get() = if (numero.isNotEmpty()) "#$numero ${nombre}" else nombre
}

data class ParcialCuarto(val cuarto: Int, val propio: Int, val rival: Int)

data class ShootingStatsEquipo(
    val tirosDos: Int = 0, val tirosDosAnotados: Int = 0,
    val tirosTres: Int = 0, val tirosTresAnotados: Int = 0,
    val tirosLibres: Int = 0, val tirosLibresAnotados: Int = 0,
    val faltasCometidas: Int = 0
) {
    val tirosCampo get() = tirosDos + tirosTres
    val tirosCampoAnotados get() = tirosDosAnotados + tirosTresAnotados
    val pctCampo get() = if (tirosCampo == 0) 0f else tirosCampoAnotados * 100f / tirosCampo
    val pct2 get() = if (tirosDos == 0) 0f else tirosDosAnotados * 100f / tirosDos
    val pct3 get() = if (tirosTres == 0) 0f else tirosTresAnotados * 100f / tirosTres
    val pctTL get() = if (tirosLibres == 0) 0f else tirosLibresAnotados * 100f / tirosLibres
}

data class TimelineItem(
    val timestamp: Long,
    val cuarto: Int,
    val tipo: String,
    val descripcion: String,
    val isRival: Boolean = false,
    val isScore: Boolean = false,
    val isFault: Boolean = false,
    val isQuarterBoundary: Boolean = false
)

data class TirosLibresPendientes(
    val idTirador: Int,
    val totalTiros: Int,
    val tirosLanzados: Int = 0,
    val isRivalFT: Boolean = false    // true when OWN player commits shooting foul → rival shoots
)

data class FaltaTiroConfigState(
    val idFoulante: Int,
    val numTiros: Int = 2,
    val canastaPuntua: Boolean = false,
    val esTresEnTiro: Boolean = false,
    val isRivalFoulant: Boolean = false,  // true = rival fouls on our shooter
    val idShooter: Int = 0
)

data class AsistenciaConfigState(
    val idPasador: Int,
    val idAnotador: Int? = null,
    val puntos: Int = 2
)

data class ShotLocationConfig(
    val idTirador: Int,
    val puntos: Int,  // 2 or 3; 0 = tapón mode (any zone, no restriction)
    val hecha: Boolean = true,
    val taponIdRecibidor: Int? = null  // non-null = this is a tapón blocked-shot location
)

data class AssistAfterShotConfig(
    val idAnotador: Int,
    val puntos: Int,
    val zona: String,
    val zonaX: Float,
    val zonaY: Float
)

data class PerdidaConfig(
    val idJugadora: Int,
    val isRival: Boolean,
    val tipoPerdida: String? = null  // null = not selected yet
)

data class BloqueReboteConfig(
    val idBloqueador: Int,
    val isRivalBlock: Boolean  // true = rival blocked our shot
)

data class TaponConfig(
    val idBloqueador: Int,
    val isRivalBlock: Boolean,
    val idRecibidor: Int? = null  // null = step 1 (select who received block)
)

data class PartidoEnVivoUiState(
    val fase: FasePartido = FasePartido.SETUP,
    val partido: PartidoEntity? = null,
    val jugadoras: List<JugadoraEntity> = emptyList(),
    val posesionPropia: Boolean = true,
    val enCancha: List<Int> = emptyList(),
    val eventos: List<EventoPartidoEntity> = emptyList(),
    val stats: Map<Int, EstadisticasJugadora> = emptyMap(),
    val puntosPropio: Int = 0,
    val puntosRival: Int = 0,
    val cuartoActual: Int = 1,
    val tirosLibresPendientes: TirosLibresPendientes? = null,
    val jugadoraSeleccionada: JugadoraEntity? = null,
    val showAcciones: Boolean = false,
    val showSustitucion: Boolean = false,
    val sustitucionSaleId: Int? = null,
    val sustitucionEntraId: Int? = null,
    val faltaTiroConfig: FaltaTiroConfigState? = null,
    val asistenciaConfig: AsistenciaConfigState? = null,
    val shotLocationConfig: ShotLocationConfig? = null,
    val assistAfterShotConfig: AssistAfterShotConfig? = null,
    val perdidaConfig: PerdidaConfig? = null,
    val bloqueReboteConfig: BloqueReboteConfig? = null,
    val taponConfig: TaponConfig? = null,
    // Rival players
    val rivalPlayers: List<RivalPlayerState> = emptyList(),
    val rivalEnCancha: List<Int> = emptyList(),
    val rivalStats: Map<Int, EstadisticasRival> = emptyMap(),
    val showAddRival: Boolean = false,
    val rivalSeleccionado: RivalPlayerState? = null,
    val showRivalAcciones: Boolean = false,
    val showEditRival: Boolean = false,
    val editingRival: RivalPlayerState? = null,
    val showRivalSustitucion: Boolean = false,
    val rivalSustSaleId: Int? = null,
    val rivalSustEntraId: Int? = null,
    val tecnicaBanqPickerShow: Boolean = false,
    val tecnicaBanqEsPropio: Boolean = false,
    // Computed analytics
    val parciales: List<ParcialCuarto> = emptyList(),
    val shootingPropio: ShootingStatsEquipo = ShootingStatsEquipo(),
    val shootingRival: ShootingStatsEquipo = ShootingStatsEquipo(),
    val timelineItems: List<TimelineItem> = emptyList(),
    // Tab navigation
    val tabIndex: Int = 0,
    val isLoading: Boolean = true,
    val cronometroSegundos: Int = 0,
    val cronometroActivo: Boolean = false,
    val cronometroTerminado: Boolean = false,
    // Team fouls per quarter
    val faltasEquipoCuarto: Int = 0,
    val faltasRivalCuarto: Int = 0,
    val bonusPropio: Boolean = false,
    val bonusRival: Boolean = false,
    // Timeouts
    val timeoutsPropio: Int = 0,
    val timeoutsRival: Int = 0,
    val timeoutsMaxPropio: Int = 2,   // FIBA: 2 first half, 3 second half
    val timeoutsMaxRival: Int = 2,
    // Advanced metrics
    val metricasAvanzadas: Map<Int, MetricasAvanzadas> = emptyMap(),
    val alertasAsistente: List<AlertaAsistente> = emptyList(),
    val parcialVivo: Pair<Int, Int> = 0 to 0,
    // Expulsion tracking
    val expulsionPendiente: JugadoraEntity? = null,
    val expulsionProcesadas: Set<Int> = emptySet(),
    // Shot chart data for this match
    val shotsPartido: List<ShotChartDataEntity> = emptyList(),
    // One-shot snackbar message
    val snackbarMessage: String? = null,
)

class PartidoEnVivoViewModel(
    private val partidoId: Int,
    private val entrenadorId: Int,
    private val repository: PartidoRepository,
    private val jugadoraRepo: JugadoraRepository,
    private val shotRepo: ShotChartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PartidoEnVivoUiState())
    val state: StateFlow<PartidoEnVivoUiState> = _state.asStateFlow()

    private val timeFmt = SimpleDateFormat("mm:ss", Locale.getDefault())

    private var cronometroJob: Job? = null

    fun startCronometro() {
        if (_state.value.cronometroActivo) return
        val maxSecs = if (_state.value.cuartoActual <= 4) 600 else 300
        _state.value = _state.value.copy(cronometroActivo = true, cronometroTerminado = false)
        cronometroJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val newSecs = _state.value.cronometroSegundos + 1
                if (newSecs >= maxSecs) {
                    cronometroJob?.cancel()
                    cronometroJob = null
                    _state.value = _state.value.copy(
                        cronometroSegundos = maxSecs,
                        cronometroActivo = false,
                        cronometroTerminado = true
                    )
                    return@launch
                }
                _state.value = _state.value.copy(cronometroSegundos = newSecs)
            }
        }
    }

    fun stopCronometro() {
        cronometroJob?.cancel()
        cronometroJob = null
        _state.value = _state.value.copy(cronometroActivo = false)
    }

    fun resetCronometro() {
        stopCronometro()
        _state.value = _state.value.copy(cronometroSegundos = 0, cronometroTerminado = false)
    }

    fun toggleCronometro() {
        if (_state.value.cronometroActivo) stopCronometro() else startCronometro()
    }

    fun togglePosesion() {
        _state.update { it.copy(posesionPropia = !it.posesionPropia) }
    }

    override fun onCleared() {
        super.onCleared()
        cronometroJob?.cancel()
    }

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(partido = repository.getPartidoById(partidoId))
        }
        viewModelScope.launch {
            shotRepo.getByPartido(partidoId).collect { shots ->
                _state.update { it.copy(shotsPartido = shots) }
            }
        }
        viewModelScope.launch {
            jugadoraRepo.getActivas(entrenadorId).collect { list ->
                val current = _state.value
                _state.value = current.copy(
                    jugadoras = list,
                    isLoading = false,
                    timelineItems = computeTimeline(current.eventos, list, current.rivalPlayers)
                )
            }
        }
        viewModelScope.launch {
            repository.getEventosByPartido(partidoId).collect { eventos ->
                val jugadoras = _state.value.jugadoras
                val rivalPlayers = computeRivalPlayers(eventos)
                val rivalEnCancha = computeRivalEnCancha(eventos)
                val enCancha = computeEnCancha(eventos)
                val stats = computeStats(eventos)
                val rivalStats = computeRivalStats(eventos)
                val (propio, rival) = computeScore(eventos)
                val cuarto = computeCuarto(eventos)
                val fase = computeFase(eventos)
                val parciales = computeParciales(eventos, cuarto)
                val shooting = computeShootingPropio(eventos)
                val shootingRival = computeShootingRival(eventos)
                val timeline = computeTimeline(eventos, jugadoras, rivalPlayers)
                val (faltasEqCuarto, faltasRivCuarto) = computeFaltasCuarto(eventos, cuarto)
                val bonusP = faltasRivCuarto >= 4
                val bonusR = faltasEqCuarto >= 4
                val (toP, toR) = computeTimeouts(eventos)
                // Advanced metrics
                val metricas = AsistenteEntrenadorEngine.computeMetricasAvanzadas(
                    eventos, jugadoras, stats, _state.value.cronometroSegundos
                )
                val parcialVivo = AsistenteEntrenadorEngine.computeParcialVivo(eventos)
                val alertas = if (fase == FasePartido.EN_JUEGO) {
                    AsistenteEntrenadorEngine.generarAlertas(
                        eventos, jugadoras, stats, enCancha,
                        propio, rival, cuarto, metricas,
                        rivalStats, rivalPlayers
                    )
                } else emptyList()

                // Detect newly expelled on-court players that haven't been processed yet
                val current = _state.value
                val expulsionProcesadas = current.expulsionProcesadas
                val expulsadaEnCancha = jugadoras.firstOrNull { jugadora ->
                    jugadora.id in enCancha &&
                    jugadora.id !in expulsionProcesadas &&
                    (stats[jugadora.id]?.expulsada == true)
                }

                _state.value = current.copy(
                    eventos = eventos,
                    enCancha = enCancha,
                    stats = stats,
                    puntosPropio = propio,
                    puntosRival = rival,
                    cuartoActual = cuarto,
                    fase = fase,
                    rivalPlayers = rivalPlayers,
                    rivalEnCancha = rivalEnCancha,
                    rivalStats = rivalStats,
                    parciales = parciales,
                    shootingPropio = shooting,
                    shootingRival = shootingRival,
                    timelineItems = timeline,
                    faltasEquipoCuarto = faltasEqCuarto,
                    faltasRivalCuarto = faltasRivCuarto,
                    bonusPropio = bonusP,
                    bonusRival = bonusR,
                    timeoutsPropio = computeTimeoutsHalf(eventos, cuarto, true),
                    timeoutsRival = computeTimeoutsHalf(eventos, cuarto, false),
                    timeoutsMaxPropio = computeTimeoutMax(cuarto),
                    timeoutsMaxRival = computeTimeoutMax(cuarto),
                    metricasAvanzadas = metricas,
                    alertasAsistente = alertas,
                    parcialVivo = parcialVivo,
                    expulsionPendiente = expulsadaEnCancha ?: current.expulsionPendiente
                )
            }
        }
    }

    // ── State computation ──────────────────────────────────────────────────────

    private fun computeFase(eventos: List<EventoPartidoEntity>): FasePartido {
        if (eventos.any { it.tipoEvento == EventoTipo.PARTIDO_FINALIZADO }) return FasePartido.FINALIZADO
        if (eventos.any { it.tipoEvento == EventoTipo.QUINTETO_INICIAL }) return FasePartido.EN_JUEGO
        return FasePartido.SETUP
    }

    private fun computeEnCancha(eventos: List<EventoPartidoEntity>): List<Int> {
        val inPlay = mutableSetOf<Int>()
        for (e in eventos) {
            when (e.tipoEvento) {
                EventoTipo.QUINTETO_INICIAL -> inPlay.add(e.idJugadora)
                EventoTipo.SUSTITUCION_SALE -> inPlay.remove(e.idJugadora)
                EventoTipo.SUSTITUCION_ENTRA -> inPlay.add(e.idJugadora)
            }
        }
        return inPlay.toList()
    }

    private fun computeScore(eventos: List<EventoPartidoEntity>): Pair<Int, Int> {
        var propio = 0; var rival = 0
        for (e in eventos) {
            when (e.tipoEvento) {
                EventoTipo.PUNTO_2 -> propio += 2
                EventoTipo.PUNTO_3 -> propio += 3
                EventoTipo.LIBRE_ANOTADO -> propio += 1
                EventoTipo.PUNTO_RIVAL -> rival += e.valor
                EventoTipo.RIVAL_PUNTO_2 -> rival += 2
                EventoTipo.RIVAL_PUNTO_3 -> rival += 3
                EventoTipo.RIVAL_LIBRE_ANOTADO -> rival += 1
            }
        }
        return propio to rival
    }

    private fun computeCuarto(eventos: List<EventoPartidoEntity>): Int =
        eventos.lastOrNull { it.tipoEvento == EventoTipo.NUEVO_CUARTO }?.valor ?: 1

    private fun computeStats(eventos: List<EventoPartidoEntity>): Map<Int, EstadisticasJugadora> {
        val map = mutableMapOf<Int, EstadisticasJugadora>()
        for (e in eventos) {
            if (e.idJugadora <= 0) continue
            val s = map.getOrDefault(e.idJugadora, EstadisticasJugadora())
            map[e.idJugadora] = when (e.tipoEvento) {
                EventoTipo.PUNTO_2 -> s.copy(puntos = s.puntos + 2, tirosDos = s.tirosDos + 1, tirosDosAnotados = s.tirosDosAnotados + 1)
                EventoTipo.PUNTO_3 -> s.copy(puntos = s.puntos + 3, tirosTres = s.tirosTres + 1, tirosTresAnotados = s.tirosTresAnotados + 1)
                EventoTipo.LIBRE_ANOTADO -> s.copy(puntos = s.puntos + 1, tirosLibres = s.tirosLibres + 1, tirosLibresAnotados = s.tirosLibresAnotados + 1)
                EventoTipo.LIBRE_FALLADO -> s.copy(tirosLibres = s.tirosLibres + 1)
                EventoTipo.FALTA_PERSONAL, EventoTipo.FALTA_PERSONAL_TIRO -> s.copy(faltasPersonales = s.faltasPersonales + 1)
                EventoTipo.FALTA_TECNICA -> s.copy(faltasTecnicas = s.faltasTecnicas + 1)
                EventoTipo.FALTA_ANTIDEPORTIVA -> s.copy(faltasAntideportivas = s.faltasAntideportivas + 1)
                EventoTipo.REBOTE_OFENSIVO -> s.copy(rebotesOfensivos = s.rebotesOfensivos + 1)
                EventoTipo.REBOTE_DEFENSIVO -> s.copy(rebotesDefensivos = s.rebotesDefensivos + 1)
                EventoTipo.ASISTENCIA -> s.copy(asistencias = s.asistencias + 1)
                EventoTipo.ROBO -> s.copy(robos = s.robos + 1)
                EventoTipo.PERDIDA -> s.copy(perdidas = s.perdidas + 1)
                EventoTipo.TAPON -> s.copy(tapones = s.tapones + 1)
                EventoTipo.TIRO_DOS_FALLADO -> s.copy(tirosDos = s.tirosDos + 1)
                EventoTipo.TIRO_TRES_FALLADO -> s.copy(tirosTres = s.tirosTres + 1)
                else -> s
            }
        }
        return map
    }

    private fun computeRivalPlayers(eventos: List<EventoPartidoEntity>): List<RivalPlayerState> {
        val latestByRivalId = mutableMapOf<Int, EventoPartidoEntity>()
        for (e in eventos.filter { it.tipoEvento == EventoTipo.RIVAL_JUGADORA_ALTA }) {
            latestByRivalId[e.valor] = e
        }
        return latestByRivalId.values.sortedBy { it.valor }.map { e ->
            val parts = e.texto.split("|")
            RivalPlayerState(id = e.valor, nombre = parts.getOrElse(0) { "Rival" }, numero = parts.getOrElse(1) { "" })
        }
    }

    private fun computeRivalEnCancha(eventos: List<EventoPartidoEntity>): List<Int> {
        val seenIds = mutableSetOf<Int>()
        val inPlay = mutableSetOf<Int>()
        for (e in eventos) {
            when (e.tipoEvento) {
                EventoTipo.RIVAL_JUGADORA_ALTA -> {
                    if (e.valor !in seenIds) {
                        seenIds.add(e.valor)
                        if (inPlay.size < 5) inPlay.add(e.valor)  // first 5 added go to court
                    }
                }
                EventoTipo.RIVAL_SUSTITUCION_SALE -> inPlay.remove(e.valor)
                EventoTipo.RIVAL_SUSTITUCION_ENTRA -> inPlay.add(e.valor)
            }
        }
        return inPlay.toList()
    }

    private fun computeRivalStats(eventos: List<EventoPartidoEntity>): Map<Int, EstadisticasRival> {
        val map = mutableMapOf<Int, EstadisticasRival>()
        for (e in eventos) {
            if (e.idJugadora >= 0) continue
            val rivalId = -e.idJugadora
            if (rivalId == 0) continue
            val s = map.getOrDefault(rivalId, EstadisticasRival())
            map[rivalId] = when (e.tipoEvento) {
                EventoTipo.RIVAL_FALTA_PERSONAL,
                EventoTipo.RIVAL_FALTA_PERSONAL_TIRO -> s.copy(faltasPersonales = s.faltasPersonales + 1)
                EventoTipo.RIVAL_FALTA_TECNICA -> s.copy(faltasTecnicas = s.faltasTecnicas + 1)
                EventoTipo.RIVAL_FALTA_ANTIDEPORTIVA -> s.copy(faltasAntideportivas = s.faltasAntideportivas + 1)
                EventoTipo.RIVAL_REBOTE_OF -> s.copy(rebotesOfensivos = s.rebotesOfensivos + 1)
                EventoTipo.RIVAL_REBOTE_DEF -> s.copy(rebotesDefensivos = s.rebotesDefensivos + 1)
                EventoTipo.RIVAL_ASISTENCIA -> s.copy(asistencias = s.asistencias + 1)
                EventoTipo.RIVAL_ROBO -> s.copy(robos = s.robos + 1)
                EventoTipo.RIVAL_PERDIDA -> s.copy(perdidas = s.perdidas + 1)
                EventoTipo.RIVAL_TAPON -> s.copy(tapones = s.tapones + 1)
                EventoTipo.RIVAL_PUNTO_2 -> s.copy(puntos = s.puntos + 2)
                EventoTipo.RIVAL_PUNTO_3 -> s.copy(puntos = s.puntos + 3)
                EventoTipo.RIVAL_LIBRE_ANOTADO -> s.copy(
                    puntos = s.puntos + 1,
                    tirosLibres = s.tirosLibres + 1,
                    tirosLibresAnotados = s.tirosLibresAnotados + 1
                )
                EventoTipo.RIVAL_LIBRE_FALLADO -> s.copy(tirosLibres = s.tirosLibres + 1)
                else -> s
            }
        }
        return map
    }

    private fun computeParciales(eventos: List<EventoPartidoEntity>, cuartoActual: Int): List<ParcialCuarto> {
        return (1..cuartoActual).map { cuarto ->
            var propio = 0; var rival = 0
            for (e in eventos.filter { it.cuarto == cuarto }) {
                when (e.tipoEvento) {
                    EventoTipo.PUNTO_2 -> propio += 2
                    EventoTipo.PUNTO_3 -> propio += 3
                    EventoTipo.LIBRE_ANOTADO -> propio += 1
                    EventoTipo.PUNTO_RIVAL -> rival += e.valor
                    EventoTipo.RIVAL_PUNTO_2 -> rival += 2
                    EventoTipo.RIVAL_PUNTO_3 -> rival += 3
                    EventoTipo.RIVAL_LIBRE_ANOTADO -> rival += 1
                }
            }
            ParcialCuarto(cuarto, propio, rival)
        }
    }

    private fun computeShootingPropio(eventos: List<EventoPartidoEntity>): ShootingStatsEquipo {
        var t2 = 0; var t2a = 0; var t3 = 0; var t3a = 0
        var tl = 0; var tla = 0; var faltas = 0
        for (e in eventos) {
            when (e.tipoEvento) {
                EventoTipo.PUNTO_2 -> { t2++; t2a++ }
                EventoTipo.PUNTO_3 -> { t3++; t3a++ }
                EventoTipo.TIRO_DOS_FALLADO -> t2++
                EventoTipo.TIRO_TRES_FALLADO -> t3++
                EventoTipo.LIBRE_ANOTADO -> { tl++; tla++ }
                EventoTipo.LIBRE_FALLADO -> tl++
                EventoTipo.FALTA_PERSONAL, EventoTipo.FALTA_PERSONAL_TIRO,
                EventoTipo.FALTA_TECNICA, EventoTipo.FALTA_ANTIDEPORTIVA -> faltas++
            }
        }
        return ShootingStatsEquipo(t2, t2a, t3, t3a, tl, tla, faltas)
    }

    private fun computeShootingRival(eventos: List<EventoPartidoEntity>): ShootingStatsEquipo {
        var t2 = 0; var t2a = 0; var t3 = 0; var t3a = 0; var tl = 0; var tla = 0; var faltas = 0
        for (e in eventos) {
            when {
                e.tipoEvento == EventoTipo.PUNTO_RIVAL -> when (e.valor) {
                    1 -> { tl++; tla++ }   // 1pt = FT (convention)
                    2 -> { t2++; t2a++ }
                    3 -> { t3++; t3a++ }
                }
                e.tipoEvento == EventoTipo.RIVAL_PUNTO_2 -> { t2++; t2a++ }
                e.tipoEvento == EventoTipo.RIVAL_PUNTO_3 -> { t3++; t3a++ }
                e.tipoEvento == EventoTipo.RIVAL_LIBRE_ANOTADO -> { tl++; tla++ }
                e.tipoEvento == EventoTipo.RIVAL_LIBRE_FALLADO -> tl++
                e.tipoEvento == EventoTipo.RIVAL_FALTA_PERSONAL ||
                e.tipoEvento == EventoTipo.RIVAL_FALTA_PERSONAL_TIRO ||
                e.tipoEvento == EventoTipo.RIVAL_FALTA_TECNICA ||
                e.tipoEvento == EventoTipo.RIVAL_FALTA_ANTIDEPORTIVA -> faltas++
            }
        }
        return ShootingStatsEquipo(t2, t2a, t3, t3a, tl, tla, faltas)
    }

    private fun computeTimeline(
        eventos: List<EventoPartidoEntity>,
        jugadoras: List<JugadoraEntity>,
        rivalPlayers: List<RivalPlayerState>
    ): List<TimelineItem> {
        val gameStart = eventos.firstOrNull { it.tipoEvento == EventoTipo.QUINTETO_INICIAL }?.timestamp
        fun jugadoraNombre(id: Int): String {
            val j = jugadoras.find { it.id == id } ?: return ""
            return "${j.nombre} ${j.apellidos}"
        }
        fun rivalNombre(id: Int): String {
            return rivalPlayers.find { it.id == id }?.displayName ?: "Rival"
        }
        fun timeStr(ts: Long): String {
            if (gameStart == null) return ""
            val diff = ts - gameStart
            val min = (diff / 60000).coerceAtLeast(0)
            val sec = ((diff % 60000) / 1000).coerceAtLeast(0)
            return String.format("%02d:%02d", min, sec)
        }
        return eventos.mapNotNull { e ->
            val t = timeStr(e.timestamp)
            val prefix = if (t.isNotEmpty()) "[$t] " else ""
            when (e.tipoEvento) {
                EventoTipo.QUINTETO_INICIAL -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}Quinteto inicial")
                EventoTipo.NUEVO_CUARTO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}▶ Inicio ${cuartoLabel(e.valor)}", isQuarterBoundary = true)
                EventoTipo.PARTIDO_FINALIZADO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}Partido finalizado", isQuarterBoundary = true)
                EventoTipo.PUNTO_2 -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} anota +2", isScore = true)
                EventoTipo.PUNTO_3 -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} anota triple +3", isScore = true)
                EventoTipo.LIBRE_ANOTADO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} anota tiro libre")
                EventoTipo.LIBRE_FALLADO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} falla tiro libre")
                EventoTipo.FALTA_PERSONAL -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} — falta personal", isFault = true)
                EventoTipo.FALTA_PERSONAL_TIRO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} — falta en tiro → libres", isFault = true)
                EventoTipo.FALTA_TECNICA -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} — falta técnica", isFault = true)
                EventoTipo.FALTA_ANTIDEPORTIVA -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} — falta antideportiva", isFault = true)
                EventoTipo.REBOTE_OFENSIVO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} — rebote ofensivo")
                EventoTipo.REBOTE_DEFENSIVO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} — rebote defensivo")
                EventoTipo.ASISTENCIA -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} — asistencia")
                EventoTipo.ROBO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} — robo")
                EventoTipo.PERDIDA -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} — pérdida")
                EventoTipo.TAPON -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} — tapón")
                EventoTipo.SUSTITUCION_ENTRA -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} ↑ entra al campo")
                EventoTipo.SUSTITUCION_SALE -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${jugadoraNombre(e.idJugadora)} ↓ sale al banquillo")
                EventoTipo.PUNTO_RIVAL -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}Rival anota +${e.valor}", isRival = true, isScore = true)
                EventoTipo.RIVAL_JUGADORA_ALTA -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}Rival añadida: ${e.texto.split("|").firstOrNull() ?: ""}", isRival = true)
                EventoTipo.RIVAL_FALTA_PERSONAL -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(-e.idJugadora)} — falta personal", isRival = true, isFault = true)
                EventoTipo.RIVAL_FALTA_PERSONAL_TIRO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(-e.idJugadora)} — falta en tiro → libres", isRival = true, isFault = true)
                EventoTipo.RIVAL_FALTA_TECNICA -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(-e.idJugadora)} — falta técnica", isRival = true, isFault = true)
                EventoTipo.RIVAL_FALTA_ANTIDEPORTIVA -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(-e.idJugadora)} — falta antideportiva", isRival = true, isFault = true)
                EventoTipo.RIVAL_REBOTE_OF -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(-e.idJugadora)} — rebote ofensivo", isRival = true)
                EventoTipo.RIVAL_REBOTE_DEF -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(-e.idJugadora)} — rebote defensivo", isRival = true)
                EventoTipo.RIVAL_ASISTENCIA -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(-e.idJugadora)} — asistencia", isRival = true)
                EventoTipo.RIVAL_ROBO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(-e.idJugadora)} — robo", isRival = true)
                EventoTipo.RIVAL_PERDIDA -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(-e.idJugadora)} — pérdida", isRival = true)
                EventoTipo.RIVAL_TAPON -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(-e.idJugadora)} — tapón", isRival = true)
                EventoTipo.RIVAL_LIBRE_ANOTADO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(-e.idJugadora)} anota tiro libre", isRival = true, isScore = true)
                EventoTipo.RIVAL_LIBRE_FALLADO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(-e.idJugadora)} falla tiro libre", isRival = true)
                EventoTipo.RIVAL_SUSTITUCION_ENTRA -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(e.valor)} ↑ rival entra al campo", isRival = true)
                EventoTipo.RIVAL_SUSTITUCION_SALE -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}${rivalNombre(e.valor)} ↓ rival sale al banquillo", isRival = true)
                EventoTipo.TIMEOUT_PROPIO -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}TIMEOUT — Nuestro equipo")
                EventoTipo.TIMEOUT_RIVAL -> TimelineItem(e.timestamp, e.cuarto, e.tipoEvento,
                    "${prefix}TIMEOUT — Equipo rival", isRival = true)
                else -> null
            }
        }
    }

    // ── Team fouls per quarter ─────────────────────────────────────────────────

    private fun computeFaltasCuarto(eventos: List<EventoPartidoEntity>, cuarto: Int): Pair<Int, Int> {
        var propias = 0; var rivales = 0
        for (e in eventos.filter { it.cuarto == cuarto }) {
            when (e.tipoEvento) {
                EventoTipo.FALTA_PERSONAL, EventoTipo.FALTA_PERSONAL_TIRO,
                EventoTipo.FALTA_TECNICA, EventoTipo.FALTA_ANTIDEPORTIVA -> propias++
                EventoTipo.RIVAL_FALTA_PERSONAL,
                EventoTipo.RIVAL_FALTA_PERSONAL_TIRO,
                EventoTipo.RIVAL_FALTA_TECNICA,
                EventoTipo.RIVAL_FALTA_ANTIDEPORTIVA -> rivales++
            }
        }
        return propias to rivales
    }

    private fun computeTimeouts(eventos: List<EventoPartidoEntity>): Pair<Int, Int> {
        var propios = 0; var rivales = 0
        for (e in eventos) {
            when (e.tipoEvento) {
                EventoTipo.TIMEOUT_PROPIO -> propios++
                EventoTipo.TIMEOUT_RIVAL -> rivales++
            }
        }
        return propios to rivales
    }

    // FIBA: 2 timeouts per first half (Q1-Q2), 3 per second half (Q3-Q4), 1 per OT
    private fun computeTimeoutMax(cuartoActual: Int): Int = when {
        cuartoActual <= 2 -> 2
        cuartoActual <= 4 -> 3
        else -> cuartoActual - 3  // OT1=2, OT2=3... each OT adds 1
    }

    private fun computeTimeoutsHalf(eventos: List<EventoPartidoEntity>, cuartoActual: Int, esPropio: Boolean): Int {
        val halfQuarters = if (cuartoActual <= 2) listOf(1, 2) else if (cuartoActual <= 4) listOf(3, 4) else listOf(cuartoActual)
        val tipo = if (esPropio) EventoTipo.TIMEOUT_PROPIO else EventoTipo.TIMEOUT_RIVAL
        return eventos.count { it.tipoEvento == tipo && it.cuarto in halfQuarters }
    }

    // ── Actions ────────────────────────────────────────────────────────────────

    fun setTab(index: Int) {
        _state.value = _state.value.copy(tabIndex = index)
    }

    fun iniciarPartido(quintetoIds: List<Int>) {
        viewModelScope.launch {
            quintetoIds.forEach { id ->
                repository.registrarEvento(
                    EventoPartidoEntity(
                        idPartido = partidoId, idJugadora = id,
                        tipoEvento = EventoTipo.QUINTETO_INICIAL, cuarto = 1
                    )
                )
            }
        }
        // Timer starts PAUSED — user taps timer to start
        resetCronometro()
    }

    fun pedirTimeout(esPropio: Boolean) {
        val s = _state.value
        val usados = if (esPropio) s.timeoutsPropio else s.timeoutsRival
        val max = if (esPropio) s.timeoutsMaxPropio else s.timeoutsMaxRival
        if (usados >= max) {
            _state.update { it.copy(snackbarMessage = "¡Sin tiempos muertos disponibles!") }
            return
        }
        stopCronometro()
        viewModelScope.launch {
            repository.registrarEvento(
                EventoPartidoEntity(
                    idPartido = partidoId,
                    tipoEvento = if (esPropio) EventoTipo.TIMEOUT_PROPIO else EventoTipo.TIMEOUT_RIVAL,
                    cuarto = _state.value.cuartoActual
                )
            )
        }
    }

    fun registrarEvento(tipoEvento: String, idJugadora: Int = -1) {
        if (tipoEvento.startsWith("__LIBRES_")) {
            val n = tipoEvento.removePrefix("__LIBRES_").toIntOrNull() ?: return
            _state.value = _state.value.copy(
                showAcciones = false,
                jugadoraSeleccionada = null,
                tirosLibresPendientes = TirosLibresPendientes(idTirador = idJugadora, totalTiros = n)
            )
            return
        }
        // Intercept: shots open location picker
        if (tipoEvento == EventoTipo.PUNTO_2 || tipoEvento == EventoTipo.PUNTO_3) {
            val puntos = if (tipoEvento == EventoTipo.PUNTO_2) 2 else 3
            _state.value = _state.value.copy(
                showAcciones = false, jugadoraSeleccionada = null,
                shotLocationConfig = ShotLocationConfig(idTirador = idJugadora, puntos = puntos, hecha = true)
            )
            return
        }
        // Intercept: missed shots — save to DB for stats, then open location picker
        if (tipoEvento == EventoTipo.TIRO_DOS_FALLADO || tipoEvento == EventoTipo.TIRO_TRES_FALLADO) {
            val puntos = if (tipoEvento == EventoTipo.TIRO_TRES_FALLADO) 3 else 2
            viewModelScope.launch {
                repository.registrarEvento(EventoPartidoEntity(
                    idPartido = partidoId, idJugadora = idJugadora,
                    tipoEvento = tipoEvento, cuarto = _state.value.cuartoActual
                ))
            }
            _state.value = _state.value.copy(
                showAcciones = false, jugadoraSeleccionada = null,
                shotLocationConfig = ShotLocationConfig(idTirador = idJugadora, puntos = puntos, hecha = false)
            )
            return
        }
        // Intercept: block opens multi-step tapón flow
        if (tipoEvento == EventoTipo.TAPON) {
            viewModelScope.launch {
                repository.registrarEvento(EventoPartidoEntity(
                    idPartido = partidoId, idJugadora = idJugadora,
                    tipoEvento = EventoTipo.TAPON, cuarto = _state.value.cuartoActual
                ))
            }
            _state.value = _state.value.copy(
                showAcciones = false, jugadoraSeleccionada = null,
                taponConfig = TaponConfig(idBloqueador = idJugadora, isRivalBlock = false)
            )
            return
        }
        // Intercept: turnover opens type picker
        if (tipoEvento == EventoTipo.PERDIDA) {
            _state.value = _state.value.copy(
                showAcciones = false, jugadoraSeleccionada = null,
                perdidaConfig = PerdidaConfig(idJugadora = idJugadora, isRival = false)
            )
            return
        }
        viewModelScope.launch {
            repository.registrarEvento(
                EventoPartidoEntity(
                    idPartido = partidoId, idJugadora = idJugadora,
                    tipoEvento = tipoEvento, cuarto = _state.value.cuartoActual
                )
            )
        }
        when (tipoEvento) {
            EventoTipo.FALTA_TECNICA -> {
                _state.value = _state.value.copy(
                    showAcciones = false, jugadoraSeleccionada = null,
                    tirosLibresPendientes = TirosLibresPendientes(idTirador = 0, totalTiros = 1, isRivalFT = true)
                )
            }
            EventoTipo.FALTA_ANTIDEPORTIVA -> {
                _state.value = _state.value.copy(
                    showAcciones = false, jugadoraSeleccionada = null,
                    tirosLibresPendientes = TirosLibresPendientes(idTirador = 0, totalTiros = 2, isRivalFT = true)
                )
            }
            else -> {
                _state.value = _state.value.copy(showAcciones = false, jugadoraSeleccionada = null)
            }
        }
    }

    fun registrarDobleTecnica(idJugadora: Int) {
        val cuarto = _state.value.cuartoActual
        viewModelScope.launch {
            repository.registrarEvento(EventoPartidoEntity(
                idPartido = partidoId, idJugadora = idJugadora,
                tipoEvento = EventoTipo.FALTA_TECNICA, cuarto = cuarto
            ))
            repository.registrarEvento(EventoPartidoEntity(
                idPartido = partidoId, idJugadora = -1,
                tipoEvento = EventoTipo.RIVAL_FALTA_TECNICA, cuarto = cuarto
            ))
        }
        _state.value = _state.value.copy(showAcciones = false, jugadoraSeleccionada = null)
        // No TL for either team — double technical cancels out
    }

    fun registrarPuntoRival(puntos: Int) {
        viewModelScope.launch {
            repository.registrarEvento(
                EventoPartidoEntity(
                    idPartido = partidoId, tipoEvento = EventoTipo.PUNTO_RIVAL,
                    cuarto = _state.value.cuartoActual, valor = puntos
                )
            )
        }
    }

    // ── Rival Players ──────────────────────────────────────────────────────────

    fun showAddRival() { _state.value = _state.value.copy(showAddRival = true) }
    fun dismissAddRival() { _state.value = _state.value.copy(showAddRival = false) }

    fun agregarRival(nombre: String, numero: String) {
        if (nombre.isBlank()) return
        val nextId = (_state.value.rivalPlayers.maxOfOrNull { it.id } ?: 0) + 1
        viewModelScope.launch {
            repository.registrarEvento(
                EventoPartidoEntity(
                    idPartido = partidoId,
                    idJugadora = -nextId,
                    tipoEvento = EventoTipo.RIVAL_JUGADORA_ALTA,
                    cuarto = _state.value.cuartoActual,
                    valor = nextId,
                    texto = "${nombre.trim()}|${numero.trim()}"
                )
            )
        }
        _state.value = _state.value.copy(showAddRival = false)
    }

    fun seleccionarRival(rival: RivalPlayerState) {
        _state.value = _state.value.copy(rivalSeleccionado = rival, showRivalAcciones = true)
    }

    fun cerrarRivalAcciones() {
        _state.value = _state.value.copy(rivalSeleccionado = null, showRivalAcciones = false)
    }

    fun abrirEditarRival(rival: RivalPlayerState) {
        _state.value = _state.value.copy(
            rivalSeleccionado = null, showRivalAcciones = false,
            showEditRival = true, editingRival = rival
        )
    }

    fun cerrarEditarRival() {
        _state.value = _state.value.copy(showEditRival = false, editingRival = null)
    }

    fun editarRival(rivalId: Int, nombre: String, numero: String) {
        if (nombre.isBlank()) return
        viewModelScope.launch {
            repository.registrarEvento(EventoPartidoEntity(
                idPartido = partidoId,
                idJugadora = -rivalId,
                tipoEvento = EventoTipo.RIVAL_JUGADORA_ALTA,
                cuarto = _state.value.cuartoActual,
                valor = rivalId,
                texto = "${nombre.trim()}|${numero.trim()}"
            ))
        }
        _state.value = _state.value.copy(showEditRival = false, editingRival = null)
    }

    fun abrirSustitucionRivalDesdeCancha(rivalId: Int) {
        _state.value = _state.value.copy(
            showRivalSustitucion = true, rivalSustSaleId = rivalId, rivalSustEntraId = null,
            rivalSeleccionado = null, showRivalAcciones = false
        )
    }

    fun abrirSustitucionRivalDesdeBanquillo(rivalId: Int) {
        _state.value = _state.value.copy(
            showRivalSustitucion = true, rivalSustEntraId = rivalId, rivalSustSaleId = null,
            rivalSeleccionado = null, showRivalAcciones = false
        )
    }

    fun confirmarSustitucionRival(otroId: Int) {
        val s = _state.value
        val realSaleId = s.rivalSustSaleId ?: otroId
        val realEntraId = s.rivalSustEntraId ?: otroId
        val cuarto = s.cuartoActual
        viewModelScope.launch {
            repository.registrarEvento(EventoPartidoEntity(
                idPartido = partidoId, idJugadora = -realSaleId,
                tipoEvento = EventoTipo.RIVAL_SUSTITUCION_SALE, cuarto = cuarto, valor = realSaleId
            ))
            repository.registrarEvento(EventoPartidoEntity(
                idPartido = partidoId, idJugadora = -realEntraId,
                tipoEvento = EventoTipo.RIVAL_SUSTITUCION_ENTRA, cuarto = cuarto, valor = realEntraId
            ))
        }
        cerrarSustitucionRival()
    }

    fun cerrarSustitucionRival() {
        _state.value = _state.value.copy(showRivalSustitucion = false, rivalSustSaleId = null, rivalSustEntraId = null)
    }

    fun registrarTecnicaBanquillo(esPropio: Boolean) {
        // Open shooter picker first so the FT is attributed to a specific player
        _state.value = _state.value.copy(
            tecnicaBanqPickerShow = true,
            tecnicaBanqEsPropio = esPropio
        )
    }

    fun confirmarTecnicaBanqShooter(shooterId: Int) {
        val s = _state.value
        val esPropio = s.tecnicaBanqEsPropio
        val cuarto = s.cuartoActual
        viewModelScope.launch {
            if (esPropio) {
                repository.registrarEvento(EventoPartidoEntity(
                    idPartido = partidoId, idJugadora = -1,
                    tipoEvento = EventoTipo.FALTA_TECNICA, cuarto = cuarto, texto = "BANQUILLO"
                ))
            } else {
                repository.registrarEvento(EventoPartidoEntity(
                    idPartido = partidoId, idJugadora = 0,
                    tipoEvento = EventoTipo.RIVAL_FALTA_TECNICA, cuarto = cuarto, texto = "BANQUILLO"
                ))
            }
        }
        _state.value = _state.value.copy(
            tecnicaBanqPickerShow = false,
            tirosLibresPendientes = TirosLibresPendientes(
                idTirador = shooterId,
                totalTiros = 1,
                isRivalFT = esPropio  // our bench → rival shoots; rival bench → we shoot
            )
        )
    }

    fun cerrarTecnicaBanqPicker() {
        _state.value = _state.value.copy(tecnicaBanqPickerShow = false)
    }

    fun registrarEventoRival(tipo: String, rivalId: Int) {
        // Intercept: turnover — don't save yet, open type picker
        if (tipo == EventoTipo.RIVAL_PERDIDA) {
            _state.value = _state.value.copy(
                rivalSeleccionado = null, showRivalAcciones = false,
                perdidaConfig = PerdidaConfig(idJugadora = rivalId, isRival = true)
            )
            return
        }
        viewModelScope.launch {
            repository.registrarEvento(
                EventoPartidoEntity(
                    idPartido = partidoId,
                    idJugadora = -rivalId,
                    tipoEvento = tipo,
                    cuarto = _state.value.cuartoActual
                )
            )
        }
        when (tipo) {
            EventoTipo.RIVAL_TAPON -> {
                _state.value = _state.value.copy(
                    rivalSeleccionado = null, showRivalAcciones = false,
                    taponConfig = TaponConfig(idBloqueador = rivalId, isRivalBlock = true)
                )
            }
            EventoTipo.RIVAL_FALTA_TECNICA -> {
                _state.value = _state.value.copy(
                    rivalSeleccionado = null, showRivalAcciones = false,
                    tirosLibresPendientes = TirosLibresPendientes(idTirador = 0, totalTiros = 1, isRivalFT = false)
                )
            }
            EventoTipo.RIVAL_FALTA_ANTIDEPORTIVA -> {
                _state.value = _state.value.copy(
                    rivalSeleccionado = null, showRivalAcciones = false,
                    tirosLibresPendientes = TirosLibresPendientes(idTirador = 0, totalTiros = 2, isRivalFT = false)
                )
            }
            else -> {
                _state.value = _state.value.copy(rivalSeleccionado = null, showRivalAcciones = false)
            }
        }
    }

    fun registrarPuntoRivalIndividual(puntos: Int, rivalId: Int) {
        val tipo = if (puntos == 3) EventoTipo.RIVAL_PUNTO_3 else EventoTipo.RIVAL_PUNTO_2
        viewModelScope.launch {
            repository.registrarEvento(
                EventoPartidoEntity(
                    idPartido = partidoId,
                    idJugadora = -rivalId,
                    tipoEvento = tipo,
                    cuarto = _state.value.cuartoActual,
                    valor = puntos
                )
            )
        }
        _state.value = _state.value.copy(rivalSeleccionado = null, showRivalAcciones = false)
    }

    // ── Falta en tiro ─────────────────────────────────────────────────────────

    fun iniciarFaltaTiro(idFoulante: Int) {
        val s = _state.value
        // Our player fouls → rival shoots → pre-select first rival on court
        val defaultRivalShooter = s.rivalEnCancha.firstOrNull() ?: 0
        _state.value = s.copy(
            faltaTiroConfig = FaltaTiroConfigState(idFoulante = idFoulante, idShooter = defaultRivalShooter),
            showAcciones = false, jugadoraSeleccionada = null
        )
    }

    fun iniciarFaltaTiroRival(rivalId: Int) {
        val s = _state.value
        // Rival fouls → our player shoots → pre-select first own player on court
        val defaultOwnShooter = s.enCancha.firstOrNull() ?: 0
        _state.value = s.copy(
            faltaTiroConfig = FaltaTiroConfigState(idFoulante = rivalId, isRivalFoulant = true, idShooter = defaultOwnShooter),
            showRivalAcciones = false, rivalSeleccionado = null
        )
    }

    fun updateFaltaTiroNumTiros(n: Int) {
        val c = _state.value.faltaTiroConfig ?: return
        _state.value = _state.value.copy(faltaTiroConfig = c.copy(numTiros = n))
    }

    fun updateFaltaTiroCanasta(v: Boolean) { _state.update { it.copy(faltaTiroConfig = it.faltaTiroConfig?.copy(canastaPuntua = v)) } }
    fun updateFaltaTiroEsTres(v: Boolean) { _state.update { it.copy(faltaTiroConfig = it.faltaTiroConfig?.copy(esTresEnTiro = v)) } }
    fun updateFaltaTiroShooter(id: Int) { _state.update { it.copy(faltaTiroConfig = it.faltaTiroConfig?.copy(idShooter = id)) } }

    fun confirmarFaltaTiro() {
        val config = _state.value.faltaTiroConfig ?: return
        viewModelScope.launch {
            val eventoType = if (config.isRivalFoulant) EventoTipo.RIVAL_FALTA_PERSONAL_TIRO
                             else EventoTipo.FALTA_PERSONAL_TIRO
            repository.registrarEvento(
                EventoPartidoEntity(
                    idPartido = partidoId,
                    idJugadora = if (config.isRivalFoulant) -config.idFoulante else config.idFoulante,
                    tipoEvento = eventoType, cuarto = _state.value.cuartoActual
                )
            )
            if (config.canastaPuntua) {
                val pts = if (config.esTresEnTiro) 3 else 2
                if (config.isRivalFoulant) {
                    // Rival fouled our player — register our basket
                    val tipo = if (config.esTresEnTiro) EventoTipo.PUNTO_3 else EventoTipo.PUNTO_2
                    repository.registrarEvento(EventoPartidoEntity(
                        idPartido = partidoId,
                        idJugadora = if (config.idShooter > 0) config.idShooter else -1,
                        tipoEvento = tipo, cuarto = _state.value.cuartoActual
                    ))
                } else {
                    // Our player fouled rival — register rival basket
                    val tipo = if (config.esTresEnTiro) EventoTipo.RIVAL_PUNTO_3 else EventoTipo.RIVAL_PUNTO_2
                    repository.registrarEvento(EventoPartidoEntity(
                        idPartido = partidoId,
                        idJugadora = if (config.idShooter > 0) -config.idShooter else -1,
                        tipoEvento = tipo, cuarto = _state.value.cuartoActual, valor = pts
                    ))
                }
            }
        }
        val numTL = when {
            config.canastaPuntua -> 1
            config.esTresEnTiro -> 3
            else -> config.numTiros
        }
        _state.value = _state.value.copy(
            faltaTiroConfig = null,
            tirosLibresPendientes = TirosLibresPendientes(
                idTirador = config.idShooter,
                totalTiros = numTL,
                isRivalFT = !config.isRivalFoulant
            )
        )
    }

    fun cancelarFaltaTiro() { _state.value = _state.value.copy(faltaTiroConfig = null) }

    // ── Shot location + assist flow ───────────────────────────────────────────

    fun confirmarUbicacionTiro(zona: String, zonaX: Float, zonaY: Float) {
        val config = _state.value.shotLocationConfig ?: return
        // Tapón: save blocked shot to recibidor's chart, then move to rebound dialog
        if (config.taponIdRecibidor != null) {
            val taponCfg = _state.value.taponConfig ?: return
            viewModelScope.launch {
                shotRepo.insert(ShotChartDataEntity(
                    idPartido = partidoId,
                    idJugadora = config.taponIdRecibidor,
                    idEntrenador = entrenadorId,
                    zonaX = zonaX, zonaY = zonaY, zona = zona, hecha = false
                ))
            }
            _state.value = _state.value.copy(
                shotLocationConfig = null,
                taponConfig = null,
                bloqueReboteConfig = BloqueReboteConfig(idBloqueador = taponCfg.idBloqueador, isRivalBlock = taponCfg.isRivalBlock)
            )
            return
        }
        if (!config.hecha) {
            // Tiro fallado: guardar en ShotChart y cerrar (sin asistencia)
            viewModelScope.launch {
                shotRepo.insert(ShotChartDataEntity(
                    idPartido = partidoId,
                    idJugadora = config.idTirador,
                    idEntrenador = entrenadorId,
                    zonaX = zonaX, zonaY = zonaY, zona = zona, hecha = false
                ))
            }
            _state.value = _state.value.copy(shotLocationConfig = null)
            return
        }
        // Tiro anotado: mostrar diálogo de asistencia
        _state.value = _state.value.copy(
            shotLocationConfig = null,
            assistAfterShotConfig = AssistAfterShotConfig(
                idAnotador = config.idTirador,
                puntos = config.puntos,
                zona = zona,
                zonaX = zonaX,
                zonaY = zonaY
            )
        )
    }

    fun cancelarShotLocation() {
        _state.value = _state.value.copy(shotLocationConfig = null, taponConfig = null)
    }

    // ── Tapón flow ─────────────────────────────────────────────────────────────

    fun seleccionarRecibidorTapon(recibidorId: Int) {
        val config = _state.value.taponConfig ?: return
        _state.value = _state.value.copy(
            taponConfig = config.copy(idRecibidor = recibidorId),
            shotLocationConfig = ShotLocationConfig(
                idTirador = recibidorId,
                puntos = 0,   // 0 = no zone restriction in tapón mode
                hecha = false,
                taponIdRecibidor = recibidorId
            )
        )
    }

    fun saltarLocationTapon() {
        val config = _state.value.taponConfig ?: return
        _state.value = _state.value.copy(
            taponConfig = null,
            shotLocationConfig = null,
            bloqueReboteConfig = BloqueReboteConfig(idBloqueador = config.idBloqueador, isRivalBlock = config.isRivalBlock)
        )
    }

    fun cancelarTapon() {
        _state.value = _state.value.copy(taponConfig = null, shotLocationConfig = null)
    }

    fun confirmarAsistenciaTiro(asistenteId: Int?) {
        val config = _state.value.assistAfterShotConfig ?: return
        val cuarto = _state.value.cuartoActual
        viewModelScope.launch {
            // Register the basket
            val tipoEvento = if (config.puntos == 3) EventoTipo.PUNTO_3 else EventoTipo.PUNTO_2
            repository.registrarEvento(EventoPartidoEntity(
                idPartido = partidoId, idJugadora = config.idAnotador,
                tipoEvento = tipoEvento, cuarto = cuarto
            ))
            // Register assist if selected
            if (asistenteId != null) {
                repository.registrarEvento(EventoPartidoEntity(
                    idPartido = partidoId, idJugadora = asistenteId,
                    tipoEvento = EventoTipo.ASISTENCIA, cuarto = cuarto
                ))
            }
            // Save shot to ShotChart
            shotRepo.insert(ShotChartDataEntity(
                idPartido = partidoId,
                idJugadora = config.idAnotador,
                idEntrenador = entrenadorId,
                zonaX = config.zonaX,
                zonaY = config.zonaY,
                zona = config.zona,
                hecha = true
            ))
        }
        _state.value = _state.value.copy(assistAfterShotConfig = null)
    }

    fun cancelarAsistenciaTiro() {
        _state.value = _state.value.copy(assistAfterShotConfig = null)
    }

    // ── Block rebound flow ────────────────────────────────────────────────────

    fun confirmarReboteTrasBloqueo(recuperadorId: Int?, esEquipoPropio: Boolean) {
        val config = _state.value.bloqueReboteConfig ?: return
        val cuarto = _state.value.cuartoActual
        if (recuperadorId != null) {
            viewModelScope.launch {
                val tipoEvento = if (esEquipoPropio) EventoTipo.REBOTE_DEFENSIVO else EventoTipo.RIVAL_REBOTE_DEF
                repository.registrarEvento(EventoPartidoEntity(
                    idPartido = partidoId,
                    idJugadora = if (esEquipoPropio) recuperadorId else -recuperadorId,
                    tipoEvento = tipoEvento, cuarto = cuarto
                ))
            }
        }
        _state.value = _state.value.copy(bloqueReboteConfig = null)
    }

    fun cancelarReboteBloqueo() {
        _state.value = _state.value.copy(bloqueReboteConfig = null)
    }

    // ── Turnover type flow ────────────────────────────────────────────────────

    private val TIPOS_PERDIDA_CON_RECUPERADOR = setOf("Mal pase", "Manejo de balón", "Pérdida genérica")

    fun seleccionarTipoPerdida(tipo: String) {
        val config = _state.value.perdidaConfig ?: return
        if (tipo in TIPOS_PERDIDA_CON_RECUPERADOR) {
            // Keep config but store the type; next step is recovery dialog
            _state.value = _state.value.copy(perdidaConfig = config.copy(tipoPerdida = tipo))
        } else {
            // Register immediately and close
            val cuarto = _state.value.cuartoActual
            viewModelScope.launch {
                val evTipo = if (config.isRival) EventoTipo.RIVAL_PERDIDA else EventoTipo.PERDIDA
                repository.registrarEvento(EventoPartidoEntity(
                    idPartido = partidoId,
                    idJugadora = if (config.isRival) -config.idJugadora else config.idJugadora,
                    tipoEvento = evTipo,
                    cuarto = cuarto, texto = tipo
                ))
            }
            _state.value = _state.value.copy(perdidaConfig = null)
        }
    }

    fun confirmarRecuperadorPerdida(recuperadorId: Int?) {
        val config = _state.value.perdidaConfig ?: return
        val tipo = config.tipoPerdida ?: return
        val cuarto = _state.value.cuartoActual
        viewModelScope.launch {
            // Register the turnover
            val evTipo = if (config.isRival) EventoTipo.RIVAL_PERDIDA else EventoTipo.PERDIDA
            repository.registrarEvento(EventoPartidoEntity(
                idPartido = partidoId,
                idJugadora = if (config.isRival) -config.idJugadora else config.idJugadora,
                tipoEvento = evTipo,
                cuarto = cuarto, texto = tipo
            ))
            // Register steal for the recovering player (if selected)
            if (recuperadorId != null) {
                // If our player lost the ball → rival player stole → RIVAL_ROBO
                // If rival lost the ball → our player stole → ROBO
                val roboTipo = if (config.isRival) EventoTipo.ROBO else EventoTipo.RIVAL_ROBO
                repository.registrarEvento(EventoPartidoEntity(
                    idPartido = partidoId,
                    idJugadora = if (config.isRival) recuperadorId else -recuperadorId,
                    tipoEvento = roboTipo,
                    cuarto = cuarto
                ))
            }
        }
        _state.value = _state.value.copy(perdidaConfig = null)
    }

    fun cancelarPerdida() {
        _state.value = _state.value.copy(perdidaConfig = null)
    }

    // ── Tiros libres ──────────────────────────────────────────────────────────

    fun registrarLibre(anotado: Boolean) {
        val pending = _state.value.tirosLibresPendientes ?: return
        viewModelScope.launch {
            if (pending.isRivalFT) {
                if (pending.idTirador > 0) {
                    // Specific rival player shoots — attribute to their stats
                    repository.registrarEvento(EventoPartidoEntity(
                        idPartido = partidoId,
                        idJugadora = -pending.idTirador,
                        tipoEvento = if (anotado) EventoTipo.RIVAL_LIBRE_ANOTADO else EventoTipo.RIVAL_LIBRE_FALLADO,
                        cuarto = _state.value.cuartoActual
                    ))
                } else if (anotado) {
                    // Generic rival FT (bench technical, no specific player)
                    repository.registrarEvento(EventoPartidoEntity(
                        idPartido = partidoId, tipoEvento = EventoTipo.PUNTO_RIVAL,
                        cuarto = _state.value.cuartoActual, valor = 1
                    ))
                }
            } else {
                repository.registrarEvento(
                    EventoPartidoEntity(
                        idPartido = partidoId, idJugadora = pending.idTirador,
                        tipoEvento = if (anotado) EventoTipo.LIBRE_ANOTADO else EventoTipo.LIBRE_FALLADO,
                        cuarto = _state.value.cuartoActual
                    )
                )
            }
        }
        val nuevos = pending.tirosLanzados + 1
        _state.value = _state.value.copy(
            tirosLibresPendientes = if (nuevos >= pending.totalTiros) null
            else pending.copy(tirosLanzados = nuevos)
        )
    }

    // ── Asistencia ────────────────────────────────────────────────────────────

    fun iniciarAsistencia(idPasador: Int) {
        _state.value = _state.value.copy(
            showAcciones = false, jugadoraSeleccionada = null,
            asistenciaConfig = AsistenciaConfigState(idPasador = idPasador)
        )
    }

    fun updateAsistenciaAnotador(id: Int) {
        val c = _state.value.asistenciaConfig ?: return
        _state.value = _state.value.copy(asistenciaConfig = c.copy(idAnotador = id))
    }

    fun updateAsistenciaPuntos(pts: Int) {
        val c = _state.value.asistenciaConfig ?: return
        _state.value = _state.value.copy(asistenciaConfig = c.copy(puntos = pts))
    }

    fun confirmarAsistencia() {
        val config = _state.value.asistenciaConfig ?: return
        val idAnotador = config.idAnotador ?: return
        viewModelScope.launch {
            repository.registrarEvento(EventoPartidoEntity(
                idPartido = partidoId, idJugadora = config.idPasador,
                tipoEvento = EventoTipo.ASISTENCIA, cuarto = _state.value.cuartoActual
            ))
            repository.registrarEvento(EventoPartidoEntity(
                idPartido = partidoId, idJugadora = idAnotador,
                tipoEvento = if (config.puntos == 3) EventoTipo.PUNTO_3 else EventoTipo.PUNTO_2,
                cuarto = _state.value.cuartoActual
            ))
        }
        _state.value = _state.value.copy(asistenciaConfig = null)
    }

    fun cancelarAsistencia() { _state.value = _state.value.copy(asistenciaConfig = null) }

    // ── Sustituciones ─────────────────────────────────────────────────────────

    fun abrirSustitucionDesdCancha(idJugadoraQueSale: Int) {
        _state.value = _state.value.copy(
            showSustitucion = true, sustitucionSaleId = idJugadoraQueSale,
            sustitucionEntraId = null, showAcciones = false, jugadoraSeleccionada = null
        )
    }

    fun abrirSustitucionDesdesBanquillo(idJugadoraQueEntra: Int) {
        _state.value = _state.value.copy(
            showSustitucion = true, sustitucionEntraId = idJugadoraQueEntra, sustitucionSaleId = null
        )
    }

    fun confirmarSustitucion(otroId: Int) {
        val s = _state.value
        val realSaleId = if (s.sustitucionSaleId != null) s.sustitucionSaleId else otroId
        val realEntraId = if (s.sustitucionEntraId != null) s.sustitucionEntraId else otroId
        viewModelScope.launch {
            repository.registrarEvento(EventoPartidoEntity(idPartido = partidoId, idJugadora = realSaleId, tipoEvento = EventoTipo.SUSTITUCION_SALE, cuarto = _state.value.cuartoActual))
            repository.registrarEvento(EventoPartidoEntity(idPartido = partidoId, idJugadora = realEntraId, tipoEvento = EventoTipo.SUSTITUCION_ENTRA, cuarto = _state.value.cuartoActual))
        }
        cerrarSustitucion()
    }

    fun cerrarSustitucion() {
        _state.value = _state.value.copy(showSustitucion = false, sustitucionSaleId = null, sustitucionEntraId = null)
    }

    // Mark expelled player as processed and open substitution dialog
    fun procesarExpulsion(jugadoraId: Int) {
        val s = _state.value
        _state.value = s.copy(
            expulsionPendiente = null,
            expulsionProcesadas = s.expulsionProcesadas + jugadoraId,
            showSustitucion = true,
            sustitucionSaleId = jugadoraId
        )
    }

    // ── Cuarto / Fin ──────────────────────────────────────────────────────────

    fun siguienteCuarto() {
        val cuarto = _state.value.cuartoActual
        if (cuarto >= 6) return  // max OT2
        viewModelScope.launch {
            repository.registrarEvento(
                EventoPartidoEntity(
                    idPartido = partidoId, tipoEvento = EventoTipo.NUEVO_CUARTO,
                    cuarto = cuarto + 1, valor = cuarto + 1
                )
            )
        }
        // Timer starts PAUSED for new quarter
        resetCronometro()
    }

    fun iniciarProrroga() {
        val cuarto = _state.value.cuartoActual
        viewModelScope.launch {
            repository.registrarEvento(
                EventoPartidoEntity(
                    idPartido = partidoId, tipoEvento = EventoTipo.NUEVO_CUARTO,
                    cuarto = cuarto + 1, valor = cuarto + 1
                )
            )
        }
        resetCronometro()
    }

    fun finalizarPartido() {
        viewModelScope.launch {
            repository.registrarEvento(
                EventoPartidoEntity(
                    idPartido = partidoId, tipoEvento = EventoTipo.PARTIDO_FINALIZADO,
                    cuarto = _state.value.cuartoActual
                )
            )
            val partido = _state.value.partido ?: return@launch
            repository.updatePartido(
                partido.copy(puntosPropio = _state.value.puntosPropio, puntosRival = _state.value.puntosRival)
            )
        }
    }

    fun anteriorCuarto() {
        val cuarto = _state.value.cuartoActual
        if (cuarto <= 1) return
        viewModelScope.launch {
            repository.registrarEvento(
                EventoPartidoEntity(
                    idPartido = partidoId, tipoEvento = EventoTipo.NUEVO_CUARTO,
                    cuarto = cuarto - 1, valor = cuarto - 1
                )
            )
        }
        resetCronometro()
    }

    fun deshacer() {
        if (_state.value.tirosLibresPendientes != null) {
            _state.value = _state.value.copy(tirosLibresPendientes = null); return
        }
        viewModelScope.launch { repository.deshacerUltimoEvento(partidoId) }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    fun seleccionarJugadora(jugadora: JugadoraEntity) {
        _state.value = _state.value.copy(jugadoraSeleccionada = jugadora, showAcciones = true)
    }

    fun cerrarAcciones() {
        _state.value = _state.value.copy(showAcciones = false, jugadoraSeleccionada = null)
    }

    fun clearSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }
}

fun cuartoLabel(n: Int): String = when (n) {
    1 -> "1er Cuarto"
    2 -> "2do Cuarto"
    3 -> "3er Cuarto"
    4 -> "4to Cuarto"
    else -> "Prórroga"
}
