package com.example.tfg_aaron.ui.screens.partidos

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.tfg_aaron.ui.screens.shotchart.calcularZonaAuto
import kotlin.math.*
import android.content.res.Configuration
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.EventoTipo
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidoEnVivoScreen(
    navController: NavController,
    entrenadorId: Int,
    partidoId: Int
) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val vm: PartidoEnVivoViewModel = viewModel(factory = viewModelFactory {
        initializer {
            PartidoEnVivoViewModel(
                partidoId = partidoId,
                entrenadorId = entrenadorId,
                repository = app.partidoRepository,
                jugadoraRepo = app.jugadoraRepository,
                shotRepo = app.shotChartRepository
            )
        }
    })
    val s by vm.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showFinalizarDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for timeout warning
    LaunchedEffect(s.snackbarMessage) {
        s.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            vm.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF1A0A0A),
                    contentColor = RedError,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = NavyDark,
        topBar = {
            when (s.fase) {
                FasePartido.SETUP -> TopAppBar(
                    title = { Text("Quinteto Inicial", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)
                )

                FasePartido.EN_JUEGO -> {
                    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                    Column(modifier = Modifier.background(NavyDark).statusBarsPadding()) {

                    // ══ CONTROL BAR ══════════════════════════════════════════════
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF050E1A))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Back
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavyElevated)
                                .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                                .clickable { navController.popBackStack() }
                        ) {
                            Icon(Icons.Filled.ArrowBack, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }

                        // Quarter back (only when Q>1)
                        if (s.cuartoActual > 1) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NavyElevated)
                                    .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                                    .clickable { vm.anteriorCuarto() }
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    "◀ Q${s.cuartoActual - 1}",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        // Undo
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavyElevated)
                                .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                                .clickable { vm.deshacer() }
                        ) {
                            Icon(Icons.Filled.Undo, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }

                        // Next quarter / OT / FIN
                        when {
                            s.cuartoActual < 4 -> Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (s.cronometroTerminado) NeonGreen else TealAccent.copy(0.25f))
                                    .border(1.dp, if (s.cronometroTerminado) NeonGreen else TealAccent, RoundedCornerShape(10.dp))
                                    .clickable { vm.siguienteCuarto() }
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    "Q${s.cuartoActual + 1} ▶",
                                    color = if (s.cronometroTerminado) Color.Black else TealAccent,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                            s.cuartoActual == 4 && s.puntosPropio == s.puntosRival -> Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PurpleAccent.copy(0.25f))
                                    .border(1.dp, PurpleAccent, RoundedCornerShape(10.dp))
                                    .clickable { vm.iniciarProrroga() }
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text("OT ▶", color = PurpleAccent, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                            else -> Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RedError.copy(0.22f))
                                    .border(1.dp, RedError, RoundedCornerShape(10.dp))
                                    .clickable { showFinalizarDialog = true }
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text("■ FINAL", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                        }
                        // Always-visible FIN button (except when already showing FIN)
                        if (s.cuartoActual < 4 || (s.cuartoActual == 4 && s.puntosPropio == s.puntosRival)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RedError.copy(0.15f))
                                    .border(1.dp, RedError.copy(0.6f), RoundedCornerShape(10.dp))
                                    .clickable { showFinalizarDialog = true }
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text("FIN", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                        }
                    }

                    // ══ SPLIT SCOREBOARD ══════════════════════════════════════════
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isLandscape) 118.dp else 156.dp)
                            .background(Color(0xFF040D17))
                    ) {
                        // Background glows
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.45f)
                                .align(Alignment.CenterStart)
                                .background(
                                    Brush.horizontalGradient(listOf(NeonGreen.copy(0.20f), Color.Transparent))
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.45f)
                                .align(Alignment.CenterEnd)
                                .background(
                                    Brush.horizontalGradient(listOf(Color.Transparent, RedError.copy(0.22f)))
                                )
                        )

                        Row(modifier = Modifier.fillMaxSize()) {
                            // ── Our team side ──
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(horizontal = 12.dp, vertical = if (isLandscape) 6.dp else 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "NOSOTROS",
                                    color = NeonGreen.copy(0.75f),
                                    fontSize = if (isLandscape) 11.sp else 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                                if (!isLandscape) Spacer(Modifier.height(2.dp))
                                Text(
                                    "${s.puntosPropio}",
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isLandscape) 64.sp else 88.sp,
                                    lineHeight = if (isLandscape) 64.sp else 88.sp,
                                    letterSpacing = (-4).sp
                                )
                            }

                            // ── Center column: Timer + Quarter ──
                            Column(
                                modifier = Modifier
                                    .width(if (isLandscape) 130.dp else 120.dp)
                                    .fillMaxHeight()
                                    .clickable {
                                        vm.toggleCronometro()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Quarter badge
                                val cuartoLabel = when (s.cuartoActual) {
                                    5 -> "OT"; else -> "Q${s.cuartoActual}"
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (s.cuartoActual >= 5) PurpleAccent.copy(0.2f)
                                            else NeonGreen.copy(0.12f)
                                        )
                                        .border(
                                            1.dp,
                                            if (s.cuartoActual >= 5) PurpleAccent.copy(0.7f)
                                            else NeonGreen.copy(0.5f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        cuartoLabel,
                                        color = if (s.cuartoActual >= 5) PurpleAccent else NeonGreen,
                                        fontSize = if (isLandscape) 12.sp else 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Spacer(Modifier.height(if (isLandscape) 2.dp else 6.dp))
                                // Timer
                                val mins = s.cronometroSegundos / 60
                                val secs = s.cronometroSegundos % 60
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (s.cronometroActivo) NeonGreen.copy(0.08f)
                                            else Color(0xFF0A1520)
                                        )
                                        .border(
                                            1.5.dp,
                                            if (s.cronometroActivo) NeonGreen.copy(0.6f)
                                            else TextTertiary.copy(0.2f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "%02d:%02d".format(mins, secs),
                                        color = if (s.cronometroActivo) NeonGreen else TextTertiary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = if (isLandscape) 26.sp else 30.sp,
                                        letterSpacing = 2.sp
                                    )
                                }
                                if (!isLandscape) {
                                    Spacer(Modifier.height(5.dp))
                                    // Tap hint
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(5.dp).clip(CircleShape)
                                                .background(
                                                    if (s.cronometroActivo) NeonGreen.copy(0.9f)
                                                    else TextTertiary.copy(0.4f)
                                                )
                                        )
                                        Text(
                                            if (s.cronometroActivo) "EN JUEGO" else "PAUSADO",
                                            color = if (s.cronometroActivo) NeonGreen.copy(0.7f) else TextTertiary.copy(0.5f),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }

                            // ── Rival side ──
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(horizontal = 12.dp, vertical = if (isLandscape) 6.dp else 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    (s.partido?.rival ?: "RIVAL").take(11).uppercase(),
                                    color = RedError.copy(0.75f),
                                    fontSize = if (isLandscape) 11.sp else 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                if (!isLandscape) Spacer(Modifier.height(2.dp))
                                Text(
                                    "${s.puntosRival}",
                                    color = if (s.puntosRival > s.puntosPropio) RedError
                                            else if (s.puntosRival == s.puntosPropio) TextSecondary
                                            else TextTertiary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isLandscape) 64.sp else 88.sp,
                                    lineHeight = if (isLandscape) 64.sp else 88.sp,
                                    letterSpacing = (-4).sp
                                )
                            }
                        }
                    }

                    // ══ INFO STRIP (fouls + timeouts + possession) — hidden in landscape ════════════════
                    val toRemainingPropio = (s.timeoutsMaxPropio - s.timeoutsPropio).coerceAtLeast(0)
                    val toRemainingRival = (s.timeoutsMaxRival - s.timeoutsRival).coerceAtLeast(0)
                    if (!isLandscape) Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavySurface)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ── Left: Our foul dots + BON + timeout button ──
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    (1..5).forEach { i ->
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape)
                                            .background(if (i <= s.faltasEquipoCuarto) NeonGreen else NavyElevated))
                                    }
                                }
                                Text("FLT", color = TextTertiary, fontSize = 6.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                            }
                            if (s.bonusRival) {
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp))
                                        .background(RedError.copy(0.18f))
                                        .border(1.dp, RedError.copy(0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) { Text("BON", color = RedError, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold) }
                            }
                            // Timeout button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (toRemainingPropio > 0) NeonGreen.copy(0.15f) else NavyElevated)
                                    .border(1.5.dp, if (toRemainingPropio > 0) NeonGreen.copy(0.8f) else NavyBorder, RoundedCornerShape(10.dp))
                                    .clickable { vm.pedirTimeout(true); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("T.OUT", color = if (toRemainingPropio > 0) NeonGreen else TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        (1..s.timeoutsMaxPropio).forEach { i ->
                                            Box(modifier = Modifier.size(11.dp).clip(CircleShape)
                                                .background(if (i <= toRemainingPropio) NeonGreen else NavyElevated))
                                        }
                                    }
                                }
                            }
                            // Bench technical — our bench
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PinkAccent.copy(0.08f))
                                    .border(1.dp, PinkAccent.copy(0.35f), RoundedCornerShape(8.dp))
                                    .clickable { vm.registrarTecnicaBanquillo(true) }
                                    .padding(horizontal = 7.dp, vertical = 5.dp)
                            ) {
                                Text("T.BNQ", color = PinkAccent, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp)
                            }
                        }

                        // ── Right: Rival timeout button + BON + foul dots ──
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Bench technical — rival bench
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PinkAccent.copy(0.08f))
                                    .border(1.dp, PinkAccent.copy(0.35f), RoundedCornerShape(8.dp))
                                    .clickable { vm.registrarTecnicaBanquillo(false) }
                                    .padding(horizontal = 7.dp, vertical = 5.dp)
                            ) {
                                Text("T.BNQ", color = PinkAccent, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp)
                            }
                            // Timeout button rival
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (toRemainingRival > 0) RedError.copy(0.15f) else NavyElevated)
                                    .border(1.5.dp, if (toRemainingRival > 0) RedError.copy(0.8f) else NavyBorder, RoundedCornerShape(10.dp))
                                    .clickable { vm.pedirTimeout(false); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("T.OUT", color = if (toRemainingRival > 0) RedError else TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        (1..s.timeoutsMaxRival).forEach { i ->
                                            Box(modifier = Modifier.size(11.dp).clip(CircleShape)
                                                .background(if (i <= toRemainingRival) RedError else NavyElevated))
                                        }
                                    }
                                }
                            }
                            if (s.bonusPropio) {
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp))
                                        .background(GreenSuccess.copy(0.18f))
                                        .border(1.dp, GreenSuccess.copy(0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) { Text("BON", color = GreenSuccess, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold) }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    (1..5).forEach { i ->
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape)
                                            .background(if (i <= s.faltasRivalCuarto) RedError else NavyElevated))
                                    }
                                }
                                Text("FLT", color = TextTertiary, fontSize = 6.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = NavyBorder)
                    LiveGameTabBar(selectedTab = s.tabIndex, onTabSelected = { vm.setTab(it) })
                } // end Column
                } // end FasePartido.EN_JUEGO

                FasePartido.FINALIZADO -> TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GreenSuccess))
                            Text("Partido Finalizado", color = GreenSuccess, fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (s.fase) {
                FasePartido.SETUP -> SetupContent(
                    jugadoras = s.jugadoras, rivalPlayers = s.rivalPlayers,
                    onShowAddRival = { vm.showAddRival() }, onIniciar = { vm.iniciarPartido(it) }
                )
                FasePartido.EN_JUEGO -> when (s.tabIndex) {
                    0 -> JuegoContent(state = s, vm = vm)
                    1 -> TimelineTab(state = s)
                    2 -> StatsTab(state = s, onAddRival = { vm.showAddRival() })
                    3 -> ComparativaTab(state = s)
                    4 -> AsistenteTab(state = s)
                    else -> JuegoContent(state = s, vm = vm)
                }
                FasePartido.FINALIZADO -> FinalizadoContent(state = s, onBack = { navController.popBackStack() })
            }

            // Overlays
            s.tirosLibresPendientes?.let { pending ->
                val nombre = if (pending.isRivalFT) {
                    if (pending.idTirador > 0) s.rivalPlayers.find { it.id == pending.idTirador }?.displayName ?: "RIVAL"
                    else "RIVAL"
                } else {
                    s.jugadoras.find { it.id == pending.idTirador }
                        ?.let { "${it.nombre} ${it.apellidos}" } ?: "Tiro Libre"
                }
                TirosLibresDialog(
                    nombre = nombre, tiroActual = pending.tirosLanzados + 1,
                    totalTiros = pending.totalTiros, isRivalFT = pending.isRivalFT,
                    onAnotado = { vm.registrarLibre(true) }, onFallado = { vm.registrarLibre(false) }
                )
            }
            s.faltaTiroConfig?.let { config ->
                FaltaTiroConfigDialog(
                    canastaPuntua = config.canastaPuntua, onCanastaPuntuaChange = { vm.updateFaltaTiroCanasta(it) },
                    esTresEnTiro = config.esTresEnTiro, onEsTresEnTiroChange = { vm.updateFaltaTiroEsTres(it) },
                    isRivalFoulant = config.isRivalFoulant,
                    idShooter = config.idShooter,
                    jugadorasEnCancha = s.jugadoras.filter { it.id in s.enCancha },
                    rivalPlayers = s.rivalPlayers.filter { it.id in s.rivalEnCancha },
                    onShooterChange = { vm.updateFaltaTiroShooter(it) },
                    onConfirm = { vm.confirmarFaltaTiro() }, onDismiss = { vm.cancelarFaltaTiro() }
                )
            }
            s.shotLocationConfig?.let { config ->
                ShotLocationDialog(
                    puntos = config.puntos,
                    onConfirm = { zona, x, y -> vm.confirmarUbicacionTiro(zona, x, y) },
                    onSkip = { vm.cancelarShotLocation() }
                )
            }
            s.assistAfterShotConfig?.let { config ->
                AssistAfterShotDialog(
                    jugadorasEnCancha = s.jugadoras.filter { it.id in s.enCancha && it.id != config.idAnotador },
                    onAsistente = { vm.confirmarAsistenciaTiro(it) },
                    onSinAsistencia = { vm.confirmarAsistenciaTiro(null) },
                    onDismiss = { vm.cancelarAsistenciaTiro() }
                )
            }
            s.taponConfig?.let { config ->
                if (config.idRecibidor == null) {
                    TaponRecibidorDialog(
                        isRivalBlock = config.isRivalBlock,
                        propioEnCancha = s.jugadoras.filter { it.id in s.enCancha },
                        rivalPlayers = s.rivalPlayers.filter { it.id in s.rivalEnCancha },
                        onSeleccionarRecibidor = { vm.seleccionarRecibidorTapon(it) },
                        onSkipLocation = { vm.saltarLocationTapon() },
                        onDismiss = { vm.cancelarTapon() }
                    )
                }
            }
            s.bloqueReboteConfig?.let { config ->
                BloqueReboteDialog(
                    propioEnCancha = s.jugadoras.filter { it.id in s.enCancha },
                    rivalEnCancha = s.rivalPlayers.filter { it.id in s.rivalEnCancha },
                    isRivalBlock = config.isRivalBlock,
                    onConfirm = { id, esPropio -> vm.confirmarReboteTrasBloqueo(id, esPropio) },
                    onDismiss = { vm.cancelarReboteBloqueo() }
                )
            }
            s.perdidaConfig?.let { config ->
                if (config.tipoPerdida == null) {
                    TipoPerdidaDialog(
                        isRival = config.isRival,
                        onTipo = { vm.seleccionarTipoPerdida(it) },
                        onDismiss = { vm.cancelarPerdida() }
                    )
                } else {
                    RecuperadorPerdidaDialog(
                        tipoPerdida = config.tipoPerdida,
                        isRival = config.isRival,
                        jugadoras = s.jugadoras.filter { it.id in s.enCancha },
                        rivalPlayers = s.rivalPlayers.filter { it.id in s.rivalEnCancha },
                        onConfirm = { vm.confirmarRecuperadorPerdida(it) },
                        onSinRecuperacion = { vm.confirmarRecuperadorPerdida(null) },
                        onDismiss = { vm.cancelarPerdida() }
                    )
                }
            }
            if (s.showSustitucion) {
                SustitucionDialog(
                    jugadoras = s.jugadoras, enCancha = s.enCancha,
                    expulsadasIds = s.stats.filter { (_, st) -> st.expulsada }.keys.toSet(),
                    sustitucionSaleId = s.sustitucionSaleId, sustitucionEntraId = s.sustitucionEntraId,
                    onConfirm = { vm.confirmarSustitucion(it) }, onDismiss = { vm.cerrarSustitucion() }
                )
            }
            if (s.showAddRival) {
                AddRivalDialog(
                    onConfirm = { nombre, numero -> vm.agregarRival(nombre, numero) },
                    onDismiss = { vm.dismissAddRival() }
                )
            }
            if (s.showEditRival) {
                s.editingRival?.let { rival ->
                    EditarRivalDialog(
                        rival = rival,
                        onConfirm = { nombre, numero -> vm.editarRival(rival.id, nombre, numero) },
                        onDismiss = { vm.cerrarEditarRival() }
                    )
                }
            }
            if (s.tecnicaBanqPickerShow) {
                TecnicaBanqPickerDialog(
                    esPropio = s.tecnicaBanqEsPropio,
                    propioEnCancha = s.jugadoras.filter { it.id in s.enCancha },
                    rivalEnCancha = s.rivalPlayers.filter { it.id in s.rivalEnCancha },
                    onConfirm = { vm.confirmarTecnicaBanqShooter(it) },
                    onDismiss = { vm.cerrarTecnicaBanqPicker() }
                )
            }
            if (s.showRivalSustitucion) {
                RivalSustitucionDialog(
                    rivalEnCancha = s.rivalPlayers.filter { it.id in s.rivalEnCancha },
                    rivalBanquillo = s.rivalPlayers.filter { it.id !in s.rivalEnCancha },
                    saleId = s.rivalSustSaleId,
                    entraId = s.rivalSustEntraId,
                    onConfirm = { vm.confirmarSustitucionRival(it) },
                    onDismiss = { vm.cerrarSustitucionRival() }
                )
            }
            // ── Mandatory expulsion substitution dialog (non-dismissible) ──
            s.expulsionPendiente?.let { expulsada ->
                val expStats = s.stats[expulsada.id] ?: EstadisticasJugadora()
                val reason = if (expStats.faltasPersonales >= 5) "5 faltas personales" else "2 faltas técnicas/antideportivas"
                AlertDialog(
                    onDismissRequest = { /* Non-dismissible — must make a substitution */ },
                    containerColor = NavyCard,
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(RedError))
                            Text("¡EXPULSIÓN!", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "${expulsada.nombre} ${expulsada.apellidos}",
                                color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                            )
                            Text(
                                "Ha sido expulsada por $reason y debe abandonar el partido inmediatamente.",
                                color = TextSecondary, fontSize = 13.sp
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RedError.copy(0.08f))
                                    .border(1.dp, RedError.copy(0.2f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    "Debes realizar una sustitución obligatoria antes de continuar.",
                                    color = RedError, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { vm.procesarExpulsion(expulsada.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBase),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Realizar sustitución", fontWeight = FontWeight.Bold, color = Color.White) }
                    },
                    dismissButton = {}
                )
            }

            if (showFinalizarDialog) {
                AlertDialog(
                    onDismissRequest = { showFinalizarDialog = false },
                    containerColor = NavyCard,
                    shape = RoundedCornerShape(20.dp),
                    title = { Text("¿Finalizar partido?", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Resultado final:", color = TextSecondary)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${s.puntosPropio}", color = OrangeBase, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                                Text("-", color = TextTertiary, fontSize = 20.sp)
                                Text("${s.puntosRival}", color = TextSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { vm.finalizarPartido(); showFinalizarDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBase),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Finalizar", fontWeight = FontWeight.Bold, color = Color.White) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFinalizarDialog = false }) { Text("Cancelar", color = TextSecondary) }
                    }
                )
            }
        }
    }

    if (s.showAcciones) {
        s.jugadoraSeleccionada?.let { jugadora ->
            val stats = s.stats[jugadora.id] ?: EstadisticasJugadora()
            AccionesBottomSheet(
                jugadora = jugadora, stats = stats,
                onDismiss = { vm.cerrarAcciones() },
                onEvento = { tipo -> vm.registrarEvento(tipo, jugadora.id); haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                onFaltaTiro = { vm.iniciarFaltaTiro(jugadora.id) },
                onSustitucion = { vm.abrirSustitucionDesdCancha(jugadora.id) }
            )
        }
    }

    if (s.showRivalAcciones) {
        s.rivalSeleccionado?.let { rival ->
            val stats = s.rivalStats[rival.id] ?: EstadisticasRival()
            RivalAccionesBottomSheet(
                rival = rival, stats = stats,
                onDismiss = { vm.cerrarRivalAcciones() },
                onEvento = { tipo -> vm.registrarEventoRival(tipo, rival.id); haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                onPuntos = { pts -> vm.registrarPuntoRivalIndividual(pts, rival.id); haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                onFaltaTiroRival = { vm.iniciarFaltaTiroRival(rival.id) },
                onEditar = { vm.abrirEditarRival(rival) },
                onSustituir = {
                    if (rival.id in s.rivalEnCancha) vm.abrirSustitucionRivalDesdeCancha(rival.id)
                    else vm.abrirSustitucionRivalDesdeBanquillo(rival.id)
                }
            )
        }
    }
}

// ── Tab Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun LiveGameTabBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("JUEGO", "TIMELINE", "STATS", "COMPARAR", "AI")

    Row(
        modifier = Modifier.fillMaxWidth().background(NavySurface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        tabs.forEachIndexed { i, label ->
            val isSelected = selectedTab == i
            Column(
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelected(i) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    label, fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) NeonGreen else TextTertiary,
                    letterSpacing = if (isSelected) 0.5.sp else 0.sp
                )
                if (isSelected) {
                    Spacer(Modifier.height(3.dp))
                    Box(modifier = Modifier.width(16.dp).height(2.dp).clip(RoundedCornerShape(1.dp)).background(NeonGreen))
                }
            }
        }
    }
    HorizontalDivider(color = NavyBorder)
}

// ── Setup Phase ─────────────────────────────────────────────────────────────

@Composable
private fun SetupContent(
    jugadoras: List<JugadoraEntity>, rivalPlayers: List<RivalPlayerState>,
    onShowAddRival: () -> Unit, onIniciar: (List<Int>) -> Unit
) {
    var selected by remember { mutableStateOf(setOf<Int>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxWidth().background(NavySurface).padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("QUINTETO INICIAL", color = GoldAccent, fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp, letterSpacing = 2.5.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { i ->
                        Box(modifier = Modifier.size(if (i <= selected.size) 12.dp else 10.dp).clip(CircleShape)
                            .background(if (i <= selected.size) GoldAccent else NavyBorder))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("${selected.size}/5 seleccionadas",
                    color = if (selected.size == 5) GreenSuccess else TextSecondary,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SectionLabel("MI EQUIPO", GoldAccent)
            if (jugadoras.isEmpty()) {
                Text("No hay jugadoras activas", color = TextTertiary, fontSize = 13.sp)
            } else {
                jugadoras.forEach { j ->
                    val isSelected = j.id in selected
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selected = if (isSelected) selected - j.id
                            else if (selected.size < 5) selected + j.id else selected
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) GoldAccent.copy(0.08f) else NavyCard
                        ),
                        border = BorderStroke(1.dp, if (isSelected) GoldAccent.copy(0.4f) else NavyBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) GoldAccent else NavyElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("#${j.numero}", color = if (isSelected) NavyDark else TextSecondary,
                                    fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${j.nombre} ${j.apellidos}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${j.posicion} · ${j.rol}", color = TextTertiary, fontSize = 12.sp)
                            }
                            if (isSelected) Icon(Icons.Filled.CheckCircle, null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionLabel("EQUIPO RIVAL", RedError)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(30.dp).clip(RoundedCornerShape(10.dp))
                        .background(RedError.copy(0.08f)).clickable { onShowAddRival() }
                ) { Icon(Icons.Filled.Add, null, tint = RedError, modifier = Modifier.size(16.dp)) }
            }
            if (rivalPlayers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(NavyCard).border(1.dp, NavyBorder, RoundedCornerShape(14.dp)).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Añade jugadoras del rival antes de empezar", color = TextTertiary, fontSize = 12.sp, textAlign = TextAlign.Center) }
            } else {
                rivalPlayers.forEach { rival ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(NavyCard).border(1.dp, RedError.copy(0.15f), RoundedCornerShape(12.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(RedError.copy(0.1f)), contentAlignment = Alignment.Center) {
                            Text(if (rival.numero.isNotEmpty()) "#${rival.numero}" else "${rival.id}",
                                color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                        Text(rival.nombre, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Button(
                onClick = { onIniciar(selected.toList()) }, enabled = selected.size == 5,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, disabledContainerColor = NavyElevated)
            ) {
                Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Iniciar Partido", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = NavyDark)
            }
        }
    }
}

// ── Game Phase ──────────────────────────────────────────────────────────────

@Composable
private fun JuegoContent(state: PartidoEnVivoUiState, vm: PartidoEnVivoViewModel) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val jugadorasMap = state.jugadoras.associateBy { it.id }
    val enCanchaJugadoras = state.enCancha.mapNotNull { jugadorasMap[it] }
    val banquilloJugadoras = state.jugadoras.filter { it.id !in state.enCancha }
    var banquilloExpanded by remember { mutableStateOf(false) }
    var banquilloRivalExpanded by remember { mutableStateOf(false) }

    if (isLandscape) {
        // ── LANDSCAPE: side-by-side teams layout ──
        Row(modifier = Modifier.fillMaxSize()) {

            // ── Left column: Our team ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // EN CANCHA header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(4.dp, 18.dp).clip(RoundedCornerShape(2.dp)).background(NeonGreen))
                        Text("EN CANCHA", color = NeonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                    }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(NeonGreen.copy(0.1f))
                            .border(1.dp, NeonGreen.copy(0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text("${enCanchaJugadoras.size}/5", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                // Player grid
                if (enCanchaJugadoras.isNotEmpty()) {
                    val maxPuntos = enCanchaJugadoras.maxOfOrNull { state.stats[it.id]?.puntos ?: 0 } ?: 0
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        enCanchaJugadoras.take(5).forEach { jugadora ->
                            val st = state.stats[jugadora.id] ?: EstadisticasJugadora()
                            PlayerCardCancha(
                                jugadora = jugadora, stats = st,
                                isTopScorer = maxPuntos > 0 && (state.stats[jugadora.id]?.puntos ?: 0) == maxPuntos,
                                onClick = { vm.seleccionarJugadora(jugadora) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (enCanchaJugadoras.size < 5) repeat(5 - enCanchaJugadoras.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                // Banquillo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (banquilloExpanded) TealAccent.copy(0.08f) else NavyCard)
                        .border(1.dp, TealAccent.copy(if (banquilloExpanded) 0.4f else 0.2f), RoundedCornerShape(10.dp))
                        .clickable { banquilloExpanded = !banquilloExpanded }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(4.dp, 18.dp).clip(RoundedCornerShape(2.dp)).background(TealAccent))
                        Text("BANQUILLO", color = TealAccent, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                        if (banquilloJugadoras.isNotEmpty()) {
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                    .background(TealAccent.copy(0.15f))
                                    .border(1.dp, TealAccent.copy(0.3f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("${banquilloJugadoras.size}", color = TealAccent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                    Icon(if (banquilloExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = TealAccent, modifier = Modifier.size(20.dp))
                }
                if (banquilloExpanded) {
                    if (banquilloJugadoras.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(NavyCard).border(1.dp, NavyBorder, RoundedCornerShape(12.dp)).padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Todas en cancha", color = TextTertiary, fontSize = 12.sp) }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(banquilloJugadoras, key = { it.id }) { jugadora ->
                                val st = state.stats[jugadora.id] ?: EstadisticasJugadora()
                                PlayerCardBanquillo(jugadora = jugadora, stats = st,
                                    onClick = { vm.abrirSustitucionDesdesBanquillo(jugadora.id) })
                            }
                        }
                    }
                }
            }

            // Vertical divider
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(NavyBorder))

            // ── Right column: Rivals ──
            val rivalEnCanchaList = state.rivalPlayers.filter { it.id in state.rivalEnCancha }
            val banquilloRival = state.rivalPlayers.filter { it.id !in state.rivalEnCancha }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // RIVALES header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(4.dp, 18.dp).clip(RoundedCornerShape(2.dp)).background(RedError))
                        Text("RIVALES", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(10.dp))
                            .background(RedError.copy(0.1f))
                            .border(1.dp, RedError.copy(0.3f), RoundedCornerShape(10.dp))
                            .clickable { vm.showAddRival() }
                    ) { Icon(Icons.Filled.Add, null, tint = RedError, modifier = Modifier.size(16.dp)) }
                }
                // Rival grid
                if (state.rivalPlayers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(RedError.copy(0.05f))
                            .border(1.dp, RedError.copy(0.15f), RoundedCornerShape(12.dp)).padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("Toca + para registrar jugadoras rivales", color = TextTertiary, fontSize = 12.sp) }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        rivalEnCanchaList.forEach { rival ->
                            val st = state.rivalStats[rival.id] ?: EstadisticasRival()
                            val maxRivPuntos = rivalEnCanchaList.maxOfOrNull { state.rivalStats[it.id]?.puntos ?: 0 } ?: 0
                            val isTopRival = maxRivPuntos > 0 && st.puntos == maxRivPuntos
                            RivalCardGrid(rival = rival, stats = st, isTopScorer = isTopRival,
                                onClick = { vm.seleccionarRival(rival) }, modifier = Modifier.weight(1f))
                        }
                        if (rivalEnCanchaList.size < 5) repeat(5 - rivalEnCanchaList.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                // Banquillo rival
                if (banquilloRival.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (banquilloRivalExpanded) RedError.copy(0.06f) else NavyCard)
                            .border(1.dp, RedError.copy(if (banquilloRivalExpanded) 0.3f else 0.15f), RoundedCornerShape(10.dp))
                            .clickable { banquilloRivalExpanded = !banquilloRivalExpanded }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(4.dp, 18.dp).clip(RoundedCornerShape(2.dp)).background(RedError.copy(0.6f)))
                            Text("BANQUILLO RIVAL", color = RedError.copy(0.8f), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                    .background(RedError.copy(0.12f))
                                    .border(1.dp, RedError.copy(0.25f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("${banquilloRival.size}", color = RedError.copy(0.8f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Icon(if (banquilloRivalExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            null, tint = RedError.copy(0.7f), modifier = Modifier.size(20.dp))
                    }
                    if (banquilloRivalExpanded) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(banquilloRival, key = { it.id }) { rival ->
                                val st = state.rivalStats[rival.id] ?: EstadisticasRival()
                                RivalCard(rival = rival, stats = st, onClick = { vm.seleccionarRival(rival) })
                            }
                        }
                    }
                }
            }
        }
    } else {
        // ── PORTRAIT: original vertical layout ──
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // ── EN CANCHA header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(4.dp, 18.dp).clip(RoundedCornerShape(2.dp)).background(NeonGreen))
                    Text("EN CANCHA", color = NeonGreen, fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp, letterSpacing = 2.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(NeonGreen.copy(0.1f))
                        .border(1.dp, NeonGreen.copy(0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text("${enCanchaJugadoras.size}/5", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // ── Player grid — all 5 equal width ──
            if (enCanchaJugadoras.isNotEmpty()) {
                val maxPuntos = enCanchaJugadoras.maxOfOrNull { state.stats[it.id]?.puntos ?: 0 } ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    enCanchaJugadoras.take(5).forEach { jugadora ->
                        val st = state.stats[jugadora.id] ?: EstadisticasJugadora()
                        PlayerCardCancha(
                            jugadora = jugadora, stats = st,
                            isTopScorer = maxPuntos > 0 && (state.stats[jugadora.id]?.puntos ?: 0) == maxPuntos,
                            onClick = { vm.seleccionarJugadora(jugadora) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill empty slots
                    if (enCanchaJugadoras.size < 5) {
                        repeat(5 - enCanchaJugadoras.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            // ── BANQUILLO (collapsible) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (banquilloExpanded) TealAccent.copy(0.08f) else NavyCard)
                    .border(1.dp, TealAccent.copy(if (banquilloExpanded) 0.4f else 0.2f), RoundedCornerShape(10.dp))
                    .clickable { banquilloExpanded = !banquilloExpanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(4.dp, 18.dp).clip(RoundedCornerShape(2.dp)).background(TealAccent))
                    Text(
                        "BANQUILLO",
                        color = TealAccent, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp
                    )
                    if (banquilloJugadoras.isNotEmpty()) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(TealAccent.copy(0.15f))
                                .border(1.dp, TealAccent.copy(0.3f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("${banquilloJugadoras.size}", color = TealAccent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    // Compact faults summary when collapsed
                    if (!banquilloExpanded && banquilloJugadoras.isNotEmpty()) {
                        val withFaults = banquilloJugadoras.count { (state.stats[it.id]?.faltasTotales ?: 0) > 0 }
                        if (withFaults > 0) {
                            Text(
                                "· $withFaults con faltas",
                                color = YellowWarning.copy(0.8f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Icon(
                    if (banquilloExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null, tint = TealAccent, modifier = Modifier.size(20.dp)
                )
            }

            if (banquilloExpanded) {
                if (banquilloJugadoras.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                            .clip(RoundedCornerShape(12.dp)).background(NavyCard)
                            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp)).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("Todas en cancha", color = TextTertiary, fontSize = 12.sp) }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(banquilloJugadoras, key = { it.id }) { jugadora ->
                            val st = state.stats[jugadora.id] ?: EstadisticasJugadora()
                            PlayerCardBanquillo(jugadora = jugadora, stats = st,
                                onClick = { vm.abrirSustitucionDesdesBanquillo(jugadora.id) })
                        }
                    }
                }
            }

            // ── RIVALES ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(4.dp, 18.dp).clip(RoundedCornerShape(2.dp)).background(RedError))
                    Text("RIVALES", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp).clip(RoundedCornerShape(10.dp))
                        .background(RedError.copy(0.1f))
                        .border(1.dp, RedError.copy(0.3f), RoundedCornerShape(10.dp))
                        .clickable { vm.showAddRival() }
                ) { Icon(Icons.Filled.Add, null, tint = RedError, modifier = Modifier.size(16.dp)) }
            }

            if (state.rivalPlayers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RedError.copy(0.05f))
                        .border(1.dp, RedError.copy(0.15f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Toca + para registrar jugadoras rivales", color = TextTertiary, fontSize = 12.sp) }
            } else {
                val rivalEnCanchaPortrait = state.rivalPlayers.filter { it.id in state.rivalEnCancha }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rivalEnCanchaPortrait.forEach { rival ->
                        val st = state.rivalStats[rival.id] ?: EstadisticasRival()
                        val maxRivPuntos = rivalEnCanchaPortrait.maxOfOrNull { state.rivalStats[it.id]?.puntos ?: 0 } ?: 0
                        val isTopRival = maxRivPuntos > 0 && st.puntos == maxRivPuntos
                        RivalCardGrid(
                            rival = rival, stats = st,
                            isTopScorer = isTopRival,
                            onClick = { vm.seleccionarRival(rival) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rivalEnCanchaPortrait.size < 5) {
                        repeat(5 - rivalEnCanchaPortrait.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            // ── BANQUILLO RIVAL (collapsible) ──
            val banquilloRival = state.rivalPlayers.filter { it.id !in state.rivalEnCancha }
            if (banquilloRival.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (banquilloRivalExpanded) RedError.copy(0.06f) else NavyCard)
                        .border(1.dp, RedError.copy(if (banquilloRivalExpanded) 0.3f else 0.15f), RoundedCornerShape(10.dp))
                        .clickable { banquilloRivalExpanded = !banquilloRivalExpanded }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(4.dp, 18.dp).clip(RoundedCornerShape(2.dp)).background(RedError.copy(0.6f)))
                        Text("BANQUILLO RIVAL", color = RedError.copy(0.8f), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(RedError.copy(0.12f))
                                .border(1.dp, RedError.copy(0.25f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("${banquilloRival.size}", color = RedError.copy(0.8f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        if (!banquilloRivalExpanded) {
                            val rivalWithFaults = banquilloRival.count { (state.rivalStats[it.id]?.faltasTotales ?: 0) > 0 }
                            if (rivalWithFaults > 0) {
                                Text("· $rivalWithFaults con faltas", color = YellowWarning.copy(0.8f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Icon(
                        if (banquilloRivalExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        null, tint = RedError.copy(0.7f), modifier = Modifier.size(20.dp)
                    )
                }
                if (banquilloRivalExpanded) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(banquilloRival, key = { it.id }) { rival ->
                            val st = state.rivalStats[rival.id] ?: EstadisticasRival()
                            RivalCard(rival = rival, stats = st, onClick = { vm.seleccionarRival(rival) })
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ── Quarter Indicator ───────────────────────────────────────────────────────

@Composable
private fun CuartoIndicator(cuarto: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        (1..4).forEach { q ->
            val isActive = q == cuarto; val isDone = q < cuarto
            Box(
                modifier = Modifier.size(if (isActive) 26.dp else 20.dp)
                    .clip(RoundedCornerShape(if (isActive) 8.dp else 6.dp))
                    .background(when {
                        isActive -> NeonGreen
                        isDone -> GreenSuccess.copy(0.2f)
                        else -> NavyElevated
                    }),
                contentAlignment = Alignment.Center
            ) {
                Text("Q$q", color = when {
                    isActive -> NavyDark; isDone -> GreenSuccess; else -> TextTertiary
                }, fontSize = if (isActive) 10.sp else 8.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        if (cuarto == 5) {
            Box(modifier = Modifier.size(26.dp).clip(RoundedCornerShape(8.dp)).background(PurpleAccent),
                contentAlignment = Alignment.Center) {
                Text("OT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// ── Player Cards ────────────────────────────────────────────────────────────

@Composable
private fun PlayerCardCancha(
    jugadora: JugadoraEntity, stats: EstadisticasJugadora,
    isTopScorer: Boolean = false,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val isExpulsada = stats.expulsada
    val faultColor = when {
        isExpulsada -> RedError
        stats.faltasTotales >= 4 -> YellowWarning
        stats.faltasTotales >= 3 -> OrangeBase.copy(0.9f)
        else -> TextTertiary
    }
    // Top scorer gets NeonGreen accent, expulsada gets RedError, otherwise OrangeBase
    val accentColor = when {
        isExpulsada -> RedError
        isTopScorer && stats.puntos > 0 -> NeonGreen
        else -> NeonGreen
    }

    // Hexagonal clip using GenericShape
    val hexShape = GenericShape { size, _ ->
        val w = size.width; val h = size.height
        val cx = w / 2f; val cy = h / 2f
        val rx = w / 2f
        // Flat-top hexagon (6 points)
        moveTo(cx, 0f)
        lineTo(cx + rx * 0.866f, cy * 0.5f)
        lineTo(cx + rx * 0.866f, cy * 1.5f)
        lineTo(cx, h)
        lineTo(cx - rx * 0.866f, cy * 1.5f)
        lineTo(cx - rx * 0.866f, cy * 0.5f)
        close()
    }

    val displayName = jugadora.apellidos.ifEmpty { jugadora.nombre }.split(" ").first()

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(hexShape)
                .background(
                    if (isExpulsada) RedError.copy(0.15f)
                    else if (isTopScorer && stats.puntos > 0) NeonGreen.copy(0.12f)
                    else Color(0xFF0D1F35)
                )
                .border(
                    1.5.dp,
                    if (isExpulsada) RedError.copy(0.7f)
                    else if (isTopScorer && stats.puntos > 0) NeonGreen.copy(0.7f)
                    else NeonGreen.copy(0.30f),
                    hexShape
                )
                .clickable(enabled = !isExpulsada) { onClick() }
                .aspectRatio(0.87f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Jersey number — large, dominant
                Text(
                    "${jugadora.numero}",
                    color = accentColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    lineHeight = 26.sp,
                    letterSpacing = (-1).sp
                )
                // Fault dots inside hex
                if (stats.faltasTotales > 0 || isExpulsada) {
                    Spacer(Modifier.height(3.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        (1..4).forEach { i ->
                            Box(
                                modifier = Modifier.size(4.dp)
                                    .clip(CircleShape)
                                    .background(if (i <= stats.faltasTotales) faultColor else NavyElevated)
                            )
                        }
                    }
                }
            }
            if (isExpulsada) {
                Box(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
                        .clip(RoundedCornerShape(3.dp)).background(RedError)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) { Text("EXP", color = Color.White, fontSize = 6.sp, fontWeight = FontWeight.ExtraBold) }
            }
        }
        // Name and points BELOW the hex
        Text(
            displayName,
            color = if (isExpulsada) RedError else TextPrimary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            "${stats.puntos} PTS",
            color = accentColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun MiniStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = color.copy(0.6f), fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun PlayerCardBanquillo(jugadora: JugadoraEntity, stats: EstadisticasJugadora, onClick: () -> Unit) {
    val faultColor = when {
        stats.faltasTotales >= 4 -> YellowWarning
        stats.faltasTotales >= 3 -> OrangeBase
        else -> TealAccent.copy(0.5f)
    }
    Box(
        modifier = Modifier
            .width(104.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, TealAccent.copy(0.3f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Jersey number
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(TealAccent.copy(0.12f)).padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("#${jugadora.numero}", color = TealAccent, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            // Name
            Text(
                jugadora.nombre, color = TextPrimary, fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 1,
                overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
            )
            // Stats row: pts + faltas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${stats.puntos}", color = GoldAccent, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    Text("PTS", color = TextTertiary, fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${stats.faltasTotales}", color = faultColor, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    Text("FLT", color = faultColor.copy(0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
            }
            // Faults dots
            if (stats.faltasTotales > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    (1..4).forEach { i ->
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape)
                            .background(if (i <= stats.faltasTotales) faultColor else NavyElevated))
                    }
                }
            }
            // SUB button
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                    .background(TealAccent.copy(0.1f)).padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.SwapHoriz, null, tint = TealAccent, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(3.dp))
                Text("SUSTITUIR", color = TealAccent, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp)
            }
        }
    }
}

@Composable
private fun RivalCard(rival: RivalPlayerState, stats: EstadisticasRival, onClick: () -> Unit) {
    val isExpulsada = stats.expulsada
    val faultColor = when {
        isExpulsada -> RedError
        stats.faltasTotales >= 4 -> YellowWarning
        stats.faltasTotales >= 3 -> OrangeBase
        else -> TextTertiary
    }
    Box(
        modifier = Modifier
            .width(96.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isExpulsada) RedError.copy(0.1f) else NavyCard)
            .border(1.dp, if (isExpulsada) RedError.copy(0.5f) else RedError.copy(0.2f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(RedError.copy(if (isExpulsada) 0.25f else 0.1f))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (rival.numero.isNotEmpty()) "#${rival.numero}" else "#${rival.id}",
                    color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                rival.nombre, color = if (isExpulsada) RedError.copy(0.7f) else TextPrimary,
                fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            if (isExpulsada) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(RedError.copy(0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) { Text("EXP", color = RedError, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold) }
            } else {
                Text("${stats.puntos}p", color = GoldAccent.copy(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    (1..4).forEach { i ->
                        Box(
                            modifier = Modifier.size(7.dp).clip(CircleShape)
                                .background(if (i <= stats.faltasTotales) faultColor else NavyElevated)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RivalCardGrid(
    rival: RivalPlayerState, stats: EstadisticasRival,
    isTopScorer: Boolean = false,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val isExpulsada = stats.expulsada
    val faultColor = when {
        isExpulsada -> RedError
        stats.faltasTotales >= 4 -> YellowWarning
        stats.faltasTotales >= 3 -> OrangeBase.copy(0.9f)
        else -> TextTertiary
    }
    val accentColor = when {
        isExpulsada -> RedError
        isTopScorer && stats.puntos > 0 -> Color(0xFFFF6B6B) // lighter red for top rival
        else -> RedError.copy(0.8f)
    }

    val hexShape = GenericShape { size, _ ->
        val w = size.width; val h = size.height
        val cx = w / 2f; val cy = h / 2f
        val rx = w / 2f
        moveTo(cx, 0f)
        lineTo(cx + rx * 0.866f, cy * 0.5f)
        lineTo(cx + rx * 0.866f, cy * 1.5f)
        lineTo(cx, h)
        lineTo(cx - rx * 0.866f, cy * 1.5f)
        lineTo(cx - rx * 0.866f, cy * 0.5f)
        close()
    }

    val rivalDisplayName = rival.nombre.split(" ").first()
    val num = rival.numero.ifEmpty { "${rival.id}" }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(hexShape)
                .background(
                    if (isExpulsada) RedError.copy(0.15f)
                    else if (isTopScorer && stats.puntos > 0) Color(0xFFFF6B6B).copy(0.12f)
                    else Color(0xFF1F0D10)
                )
                .border(
                    1.5.dp,
                    if (isExpulsada) RedError.copy(0.7f)
                    else if (isTopScorer && stats.puntos > 0) Color(0xFFFF6B6B).copy(0.7f)
                    else RedError.copy(0.35f),
                    hexShape
                )
                .clickable { onClick() }
                .aspectRatio(0.87f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(num, color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 26.sp, letterSpacing = (-1).sp)
                // Fault dots inside hex
                if (stats.faltasTotales > 0 || isExpulsada) {
                    Spacer(Modifier.height(3.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        (1..4).forEach { i ->
                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (i <= stats.faltasTotales) faultColor else NavyElevated))
                        }
                    }
                }
            }
        }
        // Name and points BELOW the hex
        Text(
            rivalDisplayName,
            color = if (isExpulsada) RedError else TextPrimary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            "${stats.puntos} PTS",
            color = accentColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(3.dp, 12.dp).clip(RoundedCornerShape(1.5.dp)).background(color))
        Text(text, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 1.5.sp)
    }
}

// ── Finished Phase ──────────────────────────────────────────────────────────

@Composable
private fun FinalizadoContent(state: PartidoEnVivoUiState, onBack: () -> Unit) {
    val result = when {
        state.puntosPropio > state.puntosRival -> "VICTORIA" to ColorVictoria
        state.puntosPropio < state.puntosRival -> "DERROTA" to ColorDerrota
        else -> "EMPATE" to ColorEmpate
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                .background(result.second.copy(alpha = 0.1f))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(result.first, color = result.second, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${state.puntosPropio}", color = result.second, fontSize = 56.sp, fontWeight = FontWeight.ExtraBold)
            Text("–", color = TextTertiary, fontSize = 24.sp)
            Text("${state.puntosRival}", color = TextSecondary, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        }
        Text("vs ${state.partido?.rival ?: ""}", color = TextSecondary, fontSize = 14.sp)

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = NavyBorder)
        Spacer(Modifier.height(12.dp))

        Text("ESTADÍSTICAS FINALES", color = GoldAccent, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))

        val jugadorasConStats = state.jugadoras.filter { it.id in state.stats }
            .sortedByDescending { state.stats[it.id]?.puntos ?: 0 }

        jugadorasConStats.forEach { j ->
            val st = state.stats[j.id] ?: EstadisticasJugadora()
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                        .background(NeonGreen.copy(0.1f)), contentAlignment = Alignment.Center) {
                        Text("#${j.numero}", color = NeonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("${j.nombre} ${j.apellidos}", color = TextPrimary, fontSize = 13.sp,
                        modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FinalStatCell("${st.puntos}", "PTS", GoldAccent)
                        FinalStatCell("${st.rebotesTotales}", "REB", TealAccent)
                        FinalStatCell("${st.asistencias}", "AST", GreenSuccess)
                        FinalStatCell("${st.faltasTotales}", "FAL", YellowWarning)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
        ) { Text("Volver", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = NavyDark) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FinalStatCell(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        Text(label, color = TextTertiary, fontSize = 8.sp)
    }
}

// ── Bottom Sheets ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccionesBottomSheet(
    jugadora: JugadoraEntity, stats: EstadisticasJugadora,
    onDismiss: () -> Unit, onEvento: (String) -> Unit,
    onFaltaTiro: () -> Unit, onSustitucion: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = NavySurface, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(modifier = Modifier.padding(vertical = 10.dp).size(40.dp, 4.dp)
                .clip(RoundedCornerShape(2.dp)).background(NavyBorder))
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(NeonGreen),
                    contentAlignment = Alignment.Center) {
                    Text("#${jugadora.numero}", color = NavyDark, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Column {
                    Text("${jugadora.nombre} ${jugadora.apellidos}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("${stats.puntos} PTS", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${stats.rebotesTotales} REB", color = TealAccent, fontSize = 12.sp)
                        Text("${stats.asistencias} AST", color = GreenSuccess, fontSize = 12.sp)
                        Text("${stats.faltasTotales} FAL", color = if (stats.faltasTotales >= 4) RedError else TextSecondary, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = NavyBorder)
            Spacer(Modifier.height(12.dp))

            ActionSectionTitle("PUNTOS")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionBtn("+2 Puntos", NeonGreen, Modifier.weight(1f)) { onEvento(EventoTipo.PUNTO_2) }
                ActionBtn("+3 Puntos", OrangeLight, Modifier.weight(1f)) { onEvento(EventoTipo.PUNTO_3) }
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedActionBtn("Falla 2", NeonGreen.copy(0.5f), Modifier.weight(1f)) { onEvento(EventoTipo.TIRO_DOS_FALLADO) }
                OutlinedActionBtn("Falla 3", OrangeLight.copy(0.5f), Modifier.weight(1f)) { onEvento(EventoTipo.TIRO_TRES_FALLADO) }
            }
            Spacer(Modifier.height(8.dp))
            ActionSectionTitle("TIROS LIBRES")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3).forEach { n ->
                    OutlinedActionBtn("$n libre${if (n > 1) "s" else ""}", NeonGreen.copy(0.8f), Modifier.weight(1f)) {
                        onEvento("__LIBRES_$n")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            ActionSectionTitle("ESTADÍSTICAS")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionBtn("Reb. Of.", TealAccent, Modifier.weight(1f)) { onEvento(EventoTipo.REBOTE_OFENSIVO) }
                ActionBtn("Reb. Def.", TealAccent.copy(0.7f), Modifier.weight(1f)) { onEvento(EventoTipo.REBOTE_DEFENSIVO) }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionBtn("Robo", GreenSuccess, Modifier.weight(1f)) { onEvento(EventoTipo.ROBO) }
                ActionBtn("Pérdida", RedError.copy(0.7f), Modifier.weight(1f)) { onEvento(EventoTipo.PERDIDA) }
                ActionBtn("Tapón", PurpleAccent, Modifier.weight(1f)) { onEvento(EventoTipo.TAPON) }
            }
            Spacer(Modifier.height(8.dp))
            ActionSectionTitle("FALTAS")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionBtn("Personal", YellowWarning, Modifier.weight(1f)) { onEvento(EventoTipo.FALTA_PERSONAL) }
                ActionBtn("En Tiro", RedError, Modifier.weight(1f)) { onFaltaTiro() }
                ActionBtn("Técnica", PinkAccent, Modifier.weight(1f)) { onEvento(EventoTipo.FALTA_TECNICA) }
                ActionBtn("Antidep.", RedError, Modifier.weight(1f)) { onEvento(EventoTipo.FALTA_ANTIDEPORTIVA) }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = NavyBorder)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onSustitucion, modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TealAccent.copy(0.3f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TealAccent)
            ) {
                Icon(Icons.Filled.SwapHoriz, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sustituir a ${jugadora.nombre}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RivalAccionesBottomSheet(
    rival: RivalPlayerState, stats: EstadisticasRival,
    onDismiss: () -> Unit, onEvento: (String) -> Unit,
    onPuntos: (Int) -> Unit, onFaltaTiroRival: () -> Unit,
    onEditar: () -> Unit = {}, onSustituir: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = NavySurface, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(modifier = Modifier.padding(vertical = 10.dp).size(40.dp, 4.dp)
                .clip(RoundedCornerShape(2.dp)).background(NavyBorder))
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                    .background(RedError.copy(0.12f)).border(1.dp, RedError.copy(0.2f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center) {
                    Text(if (rival.numero.isNotEmpty()) "#${rival.numero}" else "${rival.id}",
                        color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Column {
                    Text(rival.nombre, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("${stats.puntos} PTS", color = RedError, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${stats.faltasTotales} FAL", color = YellowWarning, fontSize = 12.sp)
                        if (stats.expulsada) Text("EXPULSADA", color = RedError, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = NavyBorder)
            Spacer(Modifier.height(12.dp))

            ActionSectionTitle("ANOTACIÓN")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionBtn("+2 Puntos", RedError, Modifier.weight(1f)) { onPuntos(2) }
                ActionBtn("+3 Puntos", RedError.copy(0.75f), Modifier.weight(1f)) { onPuntos(3) }
            }
            Spacer(Modifier.height(8.dp))
            ActionSectionTitle("FALTAS")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionBtn("Personal", YellowWarning, Modifier.weight(1f)) { onEvento(EventoTipo.RIVAL_FALTA_PERSONAL) }
                ActionBtn("En Tiro", NeonGreen, Modifier.weight(1f)) { onFaltaTiroRival() }
                ActionBtn("Técnica", PinkAccent, Modifier.weight(1f)) { onEvento(EventoTipo.RIVAL_FALTA_TECNICA) }
                ActionBtn("Antidep.", RedError, Modifier.weight(1f)) { onEvento(EventoTipo.RIVAL_FALTA_ANTIDEPORTIVA) }
            }
            Spacer(Modifier.height(8.dp))
            ActionSectionTitle("ESTADÍSTICAS")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionBtn("Reb. Of.", TealAccent, Modifier.weight(1f)) { onEvento(EventoTipo.RIVAL_REBOTE_OF) }
                ActionBtn("Reb. Def.", TealAccent.copy(0.7f), Modifier.weight(1f)) { onEvento(EventoTipo.RIVAL_REBOTE_DEF) }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionBtn("Asist.", GoldAccent, Modifier.weight(1f)) { onEvento(EventoTipo.RIVAL_ASISTENCIA) }
                ActionBtn("Robo", GreenSuccess, Modifier.weight(1f)) { onEvento(EventoTipo.RIVAL_ROBO) }
                ActionBtn("Pérdida", RedError.copy(0.7f), Modifier.weight(1f)) { onEvento(EventoTipo.RIVAL_PERDIDA) }
                ActionBtn("Tapón", PurpleAccent, Modifier.weight(1f)) { onEvento(EventoTipo.RIVAL_TAPON) }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = NavyBorder)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onSustituir,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TealAccent.copy(0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealAccent)
                ) {
                    Icon(Icons.Filled.SwapHoriz, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sustituir", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onEditar,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TextSecondary.copy(0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Icon(Icons.Filled.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Editar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

// ── Dialogs ─────────────────────────────────────────────────────────────────

@Composable
private fun AddRivalDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF120608)),
            border = BorderStroke(1.5.dp, RedError.copy(0.4f))
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(RedError.copy(0.15f))
                            .border(1.dp, RedError.copy(0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PersonAdd, null, tint = RedError, modifier = Modifier.size(18.dp))
                    }
                    Text("AÑADIR RIVAL", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                }
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre de la jugadora") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedError, unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = RedError, unfocusedLabelColor = TextTertiary,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = RedError, focusedContainerColor = NavyElevated, unfocusedContainerColor = NavySurface
                    ), singleLine = true
                )
                OutlinedTextField(
                    value = numero, onValueChange = { numero = it },
                    label = { Text("Dorsal (opcional)") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedError, unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = RedError, unfocusedLabelColor = TextTertiary,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = RedError, focusedContainerColor = NavyElevated, unfocusedContainerColor = NavySurface
                    ), singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, NavyBorder), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)) { Text("Cancelar") }
                    Button(onClick = { if (nombre.isNotBlank()) onConfirm(nombre, numero) },
                        modifier = Modifier.weight(1f), enabled = nombre.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = RedError), shape = RoundedCornerShape(12.dp)
                    ) { Text("Añadir", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun EditarRivalDialog(rival: RivalPlayerState, onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf(rival.nombre) }
    var numero by remember { mutableStateOf(rival.numero) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF120608)),
            border = BorderStroke(1.5.dp, RedError.copy(0.4f))
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(RedError.copy(0.15f))
                            .border(1.dp, RedError.copy(0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Edit, null, tint = RedError, modifier = Modifier.size(18.dp))
                    }
                    Text("EDITAR RIVAL", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                }
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre de la jugadora") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedError, unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = RedError, unfocusedLabelColor = TextTertiary,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = RedError, focusedContainerColor = NavyElevated, unfocusedContainerColor = NavySurface
                    ), singleLine = true
                )
                OutlinedTextField(
                    value = numero, onValueChange = { numero = it },
                    label = { Text("Dorsal") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedError, unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = RedError, unfocusedLabelColor = TextTertiary,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = RedError, focusedContainerColor = NavyElevated, unfocusedContainerColor = NavySurface
                    ), singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, NavyBorder), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)) { Text("Cancelar") }
                    Button(onClick = { if (nombre.isNotBlank()) onConfirm(nombre, numero) },
                        modifier = Modifier.weight(1f), enabled = nombre.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = RedError), shape = RoundedCornerShape(12.dp)
                    ) { Text("Guardar", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun TecnicaBanqPickerDialog(
    esPropio: Boolean,
    propioEnCancha: List<JugadoraEntity>,
    rivalEnCancha: List<RivalPlayerState>,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // esPropio = our bench tech → rival shoots (pick from rivalEnCancha)
    // !esPropio = rival bench tech → we shoot (pick from propioEnCancha)
    val titulo = if (esPropio) "¿Qué rival lanza el libre?" else "¿Quién lanza el libre?"
    val subtitulo = if (esPropio) "Técnica a nuestro banquillo — el rival tira"
                    else "Técnica al banquillo rival — nosotros tiramos"
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = BorderStroke(1.dp, PinkAccent.copy(0.4f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(PinkAccent.copy(0.15f))
                            .border(1.dp, PinkAccent.copy(0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("T", color = PinkAccent, fontWeight = FontWeight.Black, fontSize = 16.sp) }
                    Column {
                        Text("T. BANQUILLO", color = PinkAccent, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 2.sp)
                        Text(titulo, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Text(subtitulo, color = TextSecondary, fontSize = 12.sp)
                HorizontalDivider(color = NavyBorder)
                if (esPropio) {
                    // Pick rival shooter
                    if (rivalEnCancha.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(NavyElevated).padding(14.dp), contentAlignment = Alignment.Center) {
                            Text("Sin rivales en cancha", color = TextTertiary, fontSize = 12.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            rivalEnCancha.forEach { rival ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                        .background(RedError.copy(0.08f)).border(1.dp, RedError.copy(0.2f), RoundedCornerShape(10.dp))
                                        .clickable { onConfirm(rival.id) }.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))
                                        .background(RedError.copy(0.12f)).border(1.dp, RedError.copy(0.2f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center) {
                                        Text(if (rival.numero.isNotEmpty()) "#${rival.numero}" else "${rival.id}",
                                            color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    }
                                    Text(rival.nombre, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Pick own player shooter
                    if (propioEnCancha.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(NavyElevated).padding(14.dp), contentAlignment = Alignment.Center) {
                            Text("Sin jugadoras en cancha", color = TextTertiary, fontSize = 12.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            propioEnCancha.forEach { j ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                        .background(NeonGreen.copy(0.08f)).border(1.dp, NeonGreen.copy(0.2f), RoundedCornerShape(10.dp))
                                        .clickable { onConfirm(j.id) }.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))
                                        .background(NeonGreen.copy(0.12f)).border(1.dp, NeonGreen.copy(0.25f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center) {
                                        Text("#${j.numero}", color = NeonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    }
                                    Text("${j.nombre} ${j.apellidos}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, NavyBorder), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)) { Text("Cancelar") }
            }
        }
    }
}

@Composable
private fun RivalSustitucionDialog(
    rivalEnCancha: List<RivalPlayerState>,
    rivalBanquillo: List<RivalPlayerState>,
    saleId: Int?,
    entraId: Int?,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // saleId set → user picked who leaves, now pick who enters
    // entraId set → user picked who enters, now pick who leaves
    val (titulo, listaOpciones, instruccion) = when {
        saleId != null -> Triple(
            "¿Quién entra?",
            rivalBanquillo,
            "Selecciona la rival que entra al campo"
        )
        else -> Triple(
            "¿Quién sale?",
            rivalEnCancha,
            "Selecciona la rival que sale al banquillo"
        )
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = BorderStroke(1.dp, TealAccent.copy(0.4f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(TealAccent.copy(0.15f))
                            .border(1.dp, TealAccent.copy(0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.SwapHoriz, null, tint = TealAccent, modifier = Modifier.size(18.dp)) }
                    Column {
                        Text("CAMBIO RIVAL", color = TealAccent, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                        Text(titulo, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Text(instruccion, color = TextSecondary, fontSize = 12.sp)
                if (listaOpciones.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(NavyElevated).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("No hay rivales disponibles", color = TextTertiary, fontSize = 12.sp) }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listaOpciones.forEach { rival ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RedError.copy(0.08f))
                                    .border(1.dp, RedError.copy(0.2f), RoundedCornerShape(10.dp))
                                    .clickable { onConfirm(rival.id) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                                        .background(RedError.copy(0.12f))
                                        .border(1.dp, RedError.copy(0.2f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (rival.numero.isNotEmpty()) "#${rival.numero}" else "${rival.id}",
                                        color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp
                                    )
                                }
                                Text(rival.nombre, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = onDismiss, modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, NavyBorder), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) { Text("Cancelar") }
            }
        }
    }
}

@Composable
private fun TirosLibresDialog(
    nombre: String, tiroActual: Int, totalTiros: Int,
    isRivalFT: Boolean = false, onAnotado: () -> Unit, onFallado: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF071220)),
            border = BorderStroke(1.5.dp, if (isRivalFT) RedError.copy(0.5f) else OrangeBase.copy(0.5f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape)
                        .background(if (isRivalFT) RedError.copy(0.15f) else OrangeBase.copy(0.15f))
                        .border(1.dp, if (isRivalFT) RedError.copy(0.4f) else OrangeBase.copy(0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("FT", color = if (isRivalFT) RedError else OrangeBase, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isRivalFT) RedError else OrangeBase))
                    Text("TIRO LIBRE", color = if (isRivalFT) RedError else OrangeBase, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.5.sp)
                    if (isRivalFT) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(RedError.copy(0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("RIVAL", color = RedError, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Text(nombre, color = if (isRivalFT) RedError else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..totalTiros).forEach { i ->
                        Box(modifier = Modifier.size(14.dp).clip(CircleShape)
                            .background(if (i < tiroActual) GreenSuccess else if (i == tiroActual) OrangeBase else NavyElevated))
                    }
                }
                Text("Tiro $tiroActual de $totalTiros", color = TextSecondary, fontSize = 14.sp)
                HorizontalDivider(color = NavyBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onFallado, modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✗", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            Text("FALLA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(onClick = onAnotado, modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✓", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            Text("METE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FaltaTiroConfigDialog(
    canastaPuntua: Boolean, onCanastaPuntuaChange: (Boolean) -> Unit,
    esTresEnTiro: Boolean, onEsTresEnTiroChange: (Boolean) -> Unit,
    isRivalFoulant: Boolean = false,
    idShooter: Int = 0,
    jugadorasEnCancha: List<JugadoraEntity> = emptyList(),
    rivalPlayers: List<RivalPlayerState> = emptyList(),
    onShooterChange: (Int) -> Unit = {},
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    val accentColor = if (isRivalFoulant) NeonGreen else RedError
    val numTL = when {
        canastaPuntua -> 1
        esTresEnTiro -> 3
        else -> 2
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("FALTA EN TIRO", color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(0.05f)).border(1.dp, accentColor.copy(0.15f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (isRivalFoulant) "El rival comete falta en tiro" else "Mi jugadora comete falta en tiro",
                            color = TextSecondary, fontSize = 12.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.ArrowForward, null, tint = accentColor, modifier = Modifier.size(14.dp))
                            Text(
                                if (isRivalFoulant) "NOSOTROS tiramos los libres" else "El RIVAL tira los libres",
                                color = accentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp
                            )
                        }
                    }
                }

                // Shooter selection
                val shooterLabel = if (isRivalFoulant) "¿Quién tiraba? (tu jugadora)" else "¿Qué rival tiraba?"
                Text(shooterLabel, color = TextSecondary, fontSize = 13.sp)
                if (isRivalFoulant) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(jugadorasEnCancha) { j ->
                            val isSelected = j.id == idShooter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) accentColor.copy(0.15f) else NavyElevated)
                                    .border(1.dp, if (isSelected) accentColor else NavyBorder, RoundedCornerShape(10.dp))
                                    .clickable { onShooterChange(j.id) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("#${j.numero}", color = if (isSelected) accentColor else TextSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    Text(j.nombre.take(6), color = if (isSelected) TextPrimary else TextTertiary, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(rivalPlayers) { rival ->
                            val isSelected = rival.id == idShooter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) accentColor.copy(0.15f) else NavyElevated)
                                    .border(1.dp, if (isSelected) accentColor else NavyBorder, RoundedCornerShape(10.dp))
                                    .clickable { onShooterChange(rival.id) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (rival.numero.isNotEmpty()) "#${rival.numero}" else "#${rival.id}", color = if (isSelected) accentColor else TextSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    Text(rival.nombre.take(6), color = if (isSelected) TextPrimary else TextTertiary, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))

                // Toggle: ¿Entró la canasta?
                Text("¿Entró la canasta?", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onCanastaPuntuaChange(true) }, modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canastaPuntua) accentColor else NavyElevated,
                            contentColor = if (canastaPuntua) Color.White else TextSecondary
                        )
                    ) { Text("SÍ", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
                    Button(
                        onClick = { onCanastaPuntuaChange(false) }, modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!canastaPuntua) NavySurface else NavyElevated,
                            contentColor = if (!canastaPuntua) TextPrimary else TextSecondary
                        )
                    ) { Text("NO", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
                }

                // Toggle: ¿Fue de triple? (only when canasta didn't count)
                if (!canastaPuntua) {
                    Text("¿Fue tiro de triple?", color = TextSecondary, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onEsTresEnTiroChange(true) }, modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (esTresEnTiro) OrangeBase else NavyElevated,
                                contentColor = if (esTresEnTiro) Color.White else TextSecondary
                            )
                        ) { Text("SÍ", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
                        Button(
                            onClick = { onEsTresEnTiroChange(false) }, modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!esTresEnTiro) NavySurface else NavyElevated,
                                contentColor = if (!esTresEnTiro) TextPrimary else TextSecondary
                            )
                        ) { Text("NO", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
                    }
                }

                // Summary
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(NavyElevated).padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when {
                            canastaPuntua && esTresEnTiro -> "3 puntos + 1 tiro libre"
                            canastaPuntua -> "2 puntos + 1 tiro libre"
                            else -> "$numTL tiros libres"
                        },
                        color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                    )
                }

                HorizontalDivider(color = NavyBorder)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, NavyBorder), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)) { Text("Cancelar") }
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor), shape = RoundedCornerShape(12.dp)) {
                        Text("Iniciar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SustitucionDialog(
    jugadoras: List<JugadoraEntity>, enCancha: List<Int>,
    expulsadasIds: Set<Int> = emptySet(),
    sustitucionSaleId: Int?, sustitucionEntraId: Int?,
    onConfirm: (Int) -> Unit, onDismiss: () -> Unit
) {
    val pickFromBench = sustitucionSaleId != null
    val title = if (pickFromBench) "¿Quién entra?" else "¿Por quién sale?"
    val candidates = if (pickFromBench)
        jugadoras.filter { it.id !in enCancha && it.id !in expulsadasIds }
    else jugadoras.filter { it.id in enCancha && it.id !in expulsadasIds }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF061520)),
            border = BorderStroke(1.5.dp, TealAccent.copy(0.4f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(TealAccent.copy(0.15f))
                            .border(1.dp, TealAccent.copy(0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.SwapHoriz, null, tint = TealAccent, modifier = Modifier.size(18.dp))
                    }
                    Text("SUSTITUCIÓN", color = TealAccent, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                }
                Text(title, color = TextSecondary, fontSize = 13.sp)
                if (candidates.isEmpty()) {
                    Text("No hay jugadoras disponibles", color = TextTertiary, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp))
                } else {
                    candidates.forEach { j ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(NavySurface)
                                .border(1.dp, TealAccent.copy(0.15f), RoundedCornerShape(12.dp))
                                .clickable { onConfirm(j.id) }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                                .background(TealAccent.copy(0.08f)), contentAlignment = Alignment.Center) {
                                Text("#${j.numero}", color = TealAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Column {
                                Text("${j.nombre} ${j.apellidos}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(j.posicion, color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun AsistenciaConfigDialog(
    jugadorasEnCancha: List<JugadoraEntity>, config: AsistenciaConfigState,
    onAnotadorChange: (Int) -> Unit, onPuntosChange: (Int) -> Unit,
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ASISTENCIA", color = GoldAccent, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                Text("¿2 o 3 puntos?", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 3).forEach { pts ->
                        Button(
                            onClick = { onPuntosChange(pts) }, modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (config.puntos == pts) GoldAccent else NavyElevated,
                                contentColor = if (config.puntos == pts) NavyDark else TextSecondary
                            )
                        ) { Text("+$pts", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) }
                    }
                }
                Text("¿Quién anotó?", color = TextSecondary, fontSize = 13.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    jugadorasEnCancha.forEach { j ->
                        val isSelected = j.id == config.idAnotador
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GoldAccent.copy(0.08f) else Color.Transparent)
                                .border(1.dp, if (isSelected) GoldAccent.copy(0.3f) else NavyBorder, RoundedCornerShape(12.dp))
                                .clickable { onAnotadorChange(j.id) }.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("#${j.numero}", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(32.dp))
                            Text("${j.nombre} ${j.apellidos}", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            if (isSelected) Icon(Icons.Filled.CheckCircle, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                HorizontalDivider(color = NavyBorder)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, NavyBorder), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)) { Text("Cancelar") }
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f), enabled = config.idAnotador != null,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Confirmar", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// ── Action Helpers ──────────────────────────────────────────────────────────

@Composable
private fun ActionSectionTitle(text: String) {
    Text(text, color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 6.dp))
}

@Composable
private fun ActionBtn(label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.1f), contentColor = color)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun OutlinedActionBtn(label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 1)
    }
}

// ── Shot Location Dialog ─────────────────────────────────────────────────────

@Composable
private fun ShotLocationDialog(
    puntos: Int,
    onConfirm: (zona: String, x: Float, y: Float) -> Unit,
    onSkip: () -> Unit
) {
    val accentColor = when (puntos) { 3 -> OrangeLight; 0 -> PurpleAccent; else -> NeonGreen }
    val hintText = when (puntos) {
        3 -> "Toca FUERA del arco de triple"
        0 -> "Toca dónde fue tapado el tiro"
        else -> "Toca DENTRO del arco"
    }
    var selectedZona by remember { mutableStateOf<String?>(null) }
    var selectedX by remember { mutableStateOf(0f) }
    var selectedY by remember { mutableStateOf(0f) }
    var lastTapInvalid by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = BorderStroke(1.dp, accentColor.copy(0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                    Text(
                        when (puntos) {
                            0 -> "¿DESDE DÓNDE? · TAPÓN"
                            3 -> "¿DESDE DÓNDE? · +3 TRIPLE"
                            else -> "¿DESDE DÓNDE? · +$puntos PTS"
                        },
                        color = accentColor, fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp, letterSpacing = 1.5.sp
                    )
                }

                // Half-court canvas — grande y fácil de tocar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(460.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF091420))
                        .border(1.5.dp, accentColor.copy(0.35f), RoundedCornerShape(12.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val margin = 16.dp.toPx()
                                    val courtAspect = 15f / 14f
                                    val canvasAspect = w / h
                                    val scale = if (canvasAspect >= courtAspect) (h - 2 * margin) / 14f else (w - 2 * margin) / 15f
                                    val courtOffX = if (canvasAspect >= courtAspect) (w - 15f * scale) / 2f else margin
                                    val courtOffY = if (canvasAspect >= courtAspect) margin else (h - 14f * scale) / 2f
                                    val courtW = 15f * scale
                                    val courtH = 14f * scale
                                    val nx = ((offset.x - courtOffX) / courtW).coerceIn(0f, 1f)
                                    val ny = ((offset.y - courtOffY) / courtH).coerceIn(0f, 1f)
                                    val zona = calcularZonaAuto(nx, ny, courtW, courtH)
                                    val isTriple = zona.startsWith("TRIPLE_")
                                    val isValid = when (puntos) {
                                        0 -> true   // tapón: any zone valid
                                        3 -> isTriple
                                        else -> !isTriple
                                    }
                                    if (isValid) {
                                        selectedZona = zona
                                        selectedX = nx
                                        selectedY = ny
                                        lastTapInvalid = false
                                    } else {
                                        lastTapInvalid = true
                                    }
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val lineColor = Color(0xFF4A7AB5)
                        val lineColorBright = Color(0xFF6A9ADA)
                        val strokeW = 2.5.dp.toPx()
                        val margin = 16.dp.toPx()

                        val courtAspect = 15f / 14f
                        val canvasAspect = w / h
                        val scale: Float; val courtOffX: Float; val courtOffY: Float
                        if (canvasAspect >= courtAspect) {
                            scale = (h - 2 * margin) / 14f
                            courtOffX = (w - 15f * scale) / 2f
                            courtOffY = margin
                        } else {
                            scale = (w - 2 * margin) / 15f
                            courtOffX = margin
                            courtOffY = (h - 14f * scale) / 2f
                        }
                        val courtW = 15f * scale
                        val courtH = 14f * scale
                        val baselineY = courtOffY + courtH
                        val midcourtY = courtOffY
                        val basketX = courtOffX + courtW / 2f
                        val basketY = baselineY - 1.575f * scale
                        val threeR = 6.75f * scale
                        val cornerX = courtOffX + 0.9f * scale
                        val cornerXR = courtOffX + courtW - 0.9f * scale
                        val dxCorner = cornerX - basketX
                        val cornerArcY = basketY - sqrt((threeR * threeR - dxCorner * dxCorner).coerceAtLeast(0f))

                        drawRect(Color(0xFF091420))
                        val courtPath = Path().apply {
                            addRoundRect(androidx.compose.ui.geometry.RoundRect(
                                courtOffX, courtOffY, courtOffX + courtW, courtOffY + courtH,
                                8.dp.toPx(), 8.dp.toPx()
                            ))
                        }
                        drawPath(courtPath, Color(0xFF0D1E30))

                        // Zone highlights — valid zone gets a subtle tint (not for tapón mode)
                        if (puntos == 2) {
                            // Highlight inside arc (paint + mid-range)
                            drawRect(
                                color = NeonGreen.copy(0.07f),
                                topLeft = Offset(courtOffX, cornerArcY),
                                size = Size(courtW, baselineY - cornerArcY)
                            )
                        } else {
                            // Highlight outside arc (corners + beyond arc)
                            drawRect(
                                color = OrangeLight.copy(0.07f),
                                topLeft = Offset(courtOffX, courtOffY),
                                size = Size(courtW, cornerArcY - courtOffY)
                            )
                            // Corners
                            drawRect(color = OrangeLight.copy(0.07f), topLeft = Offset(courtOffX, cornerArcY), size = Size(cornerX - courtOffX, baselineY - cornerArcY))
                            drawRect(color = OrangeLight.copy(0.07f), topLeft = Offset(cornerXR, cornerArcY), size = Size(courtOffX + courtW - cornerXR, baselineY - cornerArcY))
                        }

                        drawRect(color = lineColorBright, topLeft = Offset(courtOffX, courtOffY), size = Size(courtW, courtH), style = Stroke(strokeW))
                        drawLine(color = lineColor.copy(0.6f), start = Offset(courtOffX, midcourtY), end = Offset(courtOffX + courtW, midcourtY), strokeWidth = strokeW)
                        drawLine(lineColor, Offset(cornerX, cornerArcY), Offset(cornerX, baselineY), strokeW)
                        drawLine(lineColor, Offset(cornerXR, cornerArcY), Offset(cornerXR, baselineY), strokeW)
                        val arcStartDeg = Math.toDegrees(atan2((cornerArcY - basketY).toDouble(), (cornerX - basketX).toDouble())).toFloat()
                        val arcEndDeg = Math.toDegrees(atan2((cornerArcY - basketY).toDouble(), (cornerXR - basketX).toDouble())).toFloat()
                        drawArc(color = lineColor, startAngle = arcStartDeg, sweepAngle = arcEndDeg - arcStartDeg, useCenter = false, topLeft = Offset(basketX - threeR, basketY - threeR), size = Size(threeR * 2f, threeR * 2f), style = Stroke(strokeW))

                        val paintW = 4.9f * scale; val paintH = 5.8f * scale
                        val paintL = basketX - paintW / 2f; val paintTop = baselineY - paintH
                        drawRect(color = Color(0xFF0B1E35), topLeft = Offset(paintL, paintTop), size = Size(paintW, paintH))
                        drawRect(color = lineColor, topLeft = Offset(paintL, paintTop), size = Size(paintW, paintH), style = Stroke(strokeW))

                        val ftR = 1.8f * scale; val ftCy = paintTop
                        drawArc(color = lineColor, startAngle = 0f, sweepAngle = 180f, useCenter = false, topLeft = Offset(basketX - ftR, ftCy - ftR), size = Size(ftR * 2f, ftR * 2f), style = Stroke(strokeW))
                        drawArc(color = lineColor.copy(0.5f), startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(basketX - ftR, ftCy - ftR), size = Size(ftR * 2f, ftR * 2f), style = Stroke(strokeW))
                        val raR = 1.25f * scale
                        drawArc(color = lineColor, startAngle = 180f, sweepAngle = 180f, useCenter = false, topLeft = Offset(basketX - raR, basketY - raR), size = Size(raR * 2f, raR * 2f), style = Stroke(strokeW * 0.8f))
                        val boardW = 0.915f * scale
                        drawLine(color = lineColorBright, start = Offset(basketX - boardW, baselineY), end = Offset(basketX + boardW, baselineY), strokeWidth = strokeW * 2.5f)
                        drawCircle(color = Color(0xFFFF7A00), radius = 8.dp.toPx(), center = Offset(basketX, basketY), style = Stroke(3.dp.toPx()))

                        // Selected tap point
                        if (selectedZona != null) {
                            drawCircle(color = accentColor, radius = 7.dp.toPx(), center = Offset(courtOffX + selectedX * courtW, courtOffY + selectedY * courtH))
                            drawCircle(color = Color.White.copy(0.7f), radius = 7.dp.toPx(), center = Offset(courtOffX + selectedX * courtW, courtOffY + selectedY * courtH), style = Stroke(2.dp.toPx()))
                        }
                    }
                }

                // Feedback label
                val zoneLabelText = when (selectedZona) {
                    "PINTURA" -> "Zona / Pintura"
                    "MEDIA_DIST" -> "Media distancia"
                    "TRIPLE_IZQ" -> "Triple Izquierda"
                    "TRIPLE_CENT" -> "Triple Centro"
                    "TRIPLE_DER" -> "Triple Derecha"
                    null -> if (lastTapInvalid && puntos != 0) "Zona incorrecta — toca ${if (puntos == 3) "fuera" else "dentro"} del arco" else hintText
                    else -> selectedZona ?: ""
                }
                val feedbackColor = when {
                    lastTapInvalid && selectedZona == null -> RedError
                    selectedZona != null -> accentColor
                    else -> TextTertiary
                }
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(feedbackColor.copy(0.08f))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(zoneLabelText, color = feedbackColor, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                        Text("Sin ubicación", color = TextSecondary, fontSize = 13.sp)
                    }
                    Button(
                        onClick = { selectedZona?.let { onConfirm(it, selectedX, selectedY) } },
                        enabled = selectedZona != null,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = NavyDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("Confirmar", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) }
                }
            }
        }
    }
}

// ── Assist After Shot Dialog ──────────────────────────────────────────────────

@Composable
private fun AssistAfterShotDialog(
    jugadorasEnCancha: List<JugadoraEntity>,
    onAsistente: (Int) -> Unit,
    onSinAsistencia: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = BorderStroke(1.dp, GoldAccent.copy(0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GoldAccent))
                    Text("¿QUIÉN ASISTIÓ?", color = GoldAccent, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                }
                if (jugadorasEnCancha.isEmpty()) {
                    Text("No hay otras jugadoras en cancha", color = TextTertiary, fontSize = 13.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(8.dp))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        jugadorasEnCancha.forEach { j ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NavySurface)
                                    .border(1.dp, GoldAccent.copy(0.15f), RoundedCornerShape(12.dp))
                                    .clickable { onAsistente(j.id) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                    .background(GoldAccent.copy(0.1f)), contentAlignment = Alignment.Center) {
                                    Text("#${j.numero}", color = GoldAccent, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                }
                                Text("${j.nombre} ${j.apellidos}", color = TextPrimary, fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                HorizontalDivider(color = NavyBorder)
                Button(
                    onClick = onSinAsistencia,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyElevated, contentColor = TextSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Sin asistencia", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

// ── Bloqueo → Rebote Dialog ───────────────────────────────────────────────────

@Composable
private fun BloqueReboteDialog(
    propioEnCancha: List<JugadoraEntity>,
    rivalEnCancha: List<RivalPlayerState>,
    isRivalBlock: Boolean,
    onConfirm: (recuperadorId: Int?, esEquipoPropio: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = BorderStroke(1.dp, PurpleAccent.copy(0.3f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PurpleAccent))
                    Text("REBOTE TRAS TAPÓN", color = PurpleAccent, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                }
                Text("¿Quién coge el rebote?", color = TextSecondary, fontSize = 13.sp)

                Text("NUESTRO EQUIPO", color = NeonGreen.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                propioEnCancha.forEach { j ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(NavySurface).border(1.dp, NeonGreen.copy(0.15f), RoundedCornerShape(10.dp))
                            .clickable { onConfirm(j.id, true) }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                            .background(NeonGreen.copy(0.1f)), contentAlignment = Alignment.Center) {
                            Text("#${j.numero}", color = NeonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                        Text("${j.nombre} ${j.apellidos}", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    }
                }

                if (rivalEnCancha.isNotEmpty()) {
                    Text("RIVAL", color = RedError.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    rivalEnCancha.forEach { rival ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(NavySurface).border(1.dp, RedError.copy(0.15f), RoundedCornerShape(10.dp))
                                .clickable { onConfirm(rival.id, false) }.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                .background(RedError.copy(0.1f)), contentAlignment = Alignment.Center) {
                                Text(if (rival.numero.isNotEmpty()) "#${rival.numero}" else "${rival.id}",
                                    color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                            Text(rival.nombre, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }

                HorizontalDivider(color = NavyBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onConfirm(null, true) }, modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, NeonGreen.copy(0.3f)), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen)
                    ) { Text("F.B. Nuestro", fontSize = 11.sp) }
                    OutlinedButton(onClick = { onConfirm(null, false) }, modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, RedError.copy(0.3f)), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError)
                    ) { Text("F.B. Rival", fontSize = 11.sp) }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Sin registrar", color = TextTertiary, fontSize = 12.sp)
                }
            }
        }
    }
}

// ── Tapón Recibidor Dialog ────────────────────────────────────────────────────

@Composable
private fun TaponRecibidorDialog(
    isRivalBlock: Boolean,
    propioEnCancha: List<JugadoraEntity>,
    rivalPlayers: List<RivalPlayerState>,
    onSeleccionarRecibidor: (Int) -> Unit,
    onSkipLocation: () -> Unit,
    onDismiss: () -> Unit
) {
    // isRivalBlock=true → rival blocked our player → recibidor is one of propioEnCancha
    // isRivalBlock=false → we blocked rival → recibidor is one of rivalPlayers
    val titulo = if (isRivalBlock) "¿QUIÉN RECIBIÓ EL TAPÓN?" else "¿A QUIÉN TAPASTE?"
    val accentColor = if (isRivalBlock) RedError else PurpleAccent

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.94f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = BorderStroke(1.dp, accentColor.copy(0.35f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                    Text(titulo, color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                }
                Text(
                    "Selecciona la jugadora que intentó el tiro bloqueado:",
                    color = TextSecondary, fontSize = 13.sp
                )

                if (isRivalBlock) {
                    // Our players on court received the block
                    Text("NUESTRO EQUIPO EN PISTA", color = NeonGreen.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    propioEnCancha.forEach { j ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(NavySurface).border(1.dp, NeonGreen.copy(0.15f), RoundedCornerShape(10.dp))
                                .clickable { onSeleccionarRecibidor(j.id) }.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                .background(NeonGreen.copy(0.1f)), contentAlignment = Alignment.Center) {
                                Text("#${j.numero}", color = NeonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                            Text("${j.nombre} ${j.apellidos}", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                    }
                } else {
                    // Rival players received our block
                    if (rivalPlayers.isNotEmpty()) {
                        Text("EQUIPO RIVAL", color = RedError.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        rivalPlayers.forEach { rival ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                    .background(NavySurface).border(1.dp, RedError.copy(0.15f), RoundedCornerShape(10.dp))
                                    .clickable { onSeleccionarRecibidor(rival.id) }.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                    .background(RedError.copy(0.1f)), contentAlignment = Alignment.Center) {
                                    Text(if (rival.numero.isNotEmpty()) "#${rival.numero}" else "?",
                                        color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                }
                                Text(rival.nombre, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        Text("No hay rivales registrados", color = TextTertiary, fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().padding(8.dp), textAlign = TextAlign.Center)
                    }
                }

                HorizontalDivider(color = NavyBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onSkipLocation, modifier = Modifier.weight(1f)) {
                        Text("Saltar ubicación", color = TextTertiary, fontSize = 12.sp)
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancelar", color = TextTertiary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ── Tipo Pérdida Dialog ───────────────────────────────────────────────────────

@Composable
private fun TipoPerdidaDialog(
    isRival: Boolean,
    onTipo: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val tipos = listOf(
        "Violación 3s", "Violación 5s", "Violación 8s", "Violación 24s",
        "Fuera de banda", "Pasos", "Dobles", "Campo atrás",
        "Mal pase", "Manejo de balón", "Pérdida genérica"
    )
    val accentColor = if (isRival) RedError else OrangeBase
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = BorderStroke(1.dp, accentColor.copy(0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                    Text("TIPO DE PÉRDIDA", color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    tipos.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { tipo ->
                                Button(
                                    onClick = { onTipo(tipo) },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(0.1f), contentColor = accentColor)
                                ) {
                                    Text(tipo, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 2)
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        }
    }
}

// ── Recuperador Pérdida Dialog ────────────────────────────────────────────────

@Composable
private fun RecuperadorPerdidaDialog(
    tipoPerdida: String,
    isRival: Boolean,
    jugadoras: List<JugadoraEntity>,
    rivalPlayers: List<RivalPlayerState>,
    onConfirm: (Int) -> Unit,
    onSinRecuperacion: () -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = if (isRival) NeonGreen else RedError
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = BorderStroke(1.dp, accentColor.copy(0.3f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                    Text("¿QUIÉN RECUPERÓ?", color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 2.sp)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(NavySurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(tipoPerdida, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                if (isRival) {
                    // Rival lost → our players may have stolen it
                    Text("NUESTRO EQUIPO", color = NeonGreen.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    jugadoras.forEach { j ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(NavySurface).border(1.dp, NeonGreen.copy(0.15f), RoundedCornerShape(10.dp))
                                .clickable { onConfirm(j.id) }.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                .background(NeonGreen.copy(0.1f)), contentAlignment = Alignment.Center) {
                                Text("#${j.numero}", color = NeonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                            Text("${j.nombre} ${j.apellidos}", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                    }
                } else {
                    // We lost → rival players may have stolen it
                    Text("RIVAL", color = RedError.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    rivalPlayers.forEach { rival ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(NavySurface).border(1.dp, RedError.copy(0.15f), RoundedCornerShape(10.dp))
                                .clickable { onConfirm(rival.id) }.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                .background(RedError.copy(0.1f)), contentAlignment = Alignment.Center) {
                                Text(if (rival.numero.isNotEmpty()) "#${rival.numero}" else "${rival.id}",
                                    color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                            Text(rival.nombre, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }

                HorizontalDivider(color = NavyBorder)
                Button(
                    onClick = onSinRecuperacion,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyElevated, contentColor = TextSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Sin recuperación", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}
