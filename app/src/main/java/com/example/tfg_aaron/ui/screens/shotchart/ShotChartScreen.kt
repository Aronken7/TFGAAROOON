package com.example.tfg_aaron.ui.screens.shotchart

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.SesionTiroEntity
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ShotChartScreen(navController: NavController, entrenadorId: Int) {
    val app = LocalContext.current.applicationContext as TFGApplication
    val viewModel: ShotChartViewModel = viewModel(factory = viewModelFactory {
        initializer {
            ShotChartViewModel(
                entrenadorId,
                app.shotChartRepository,
                app.jugadoraRepository,
                app.sesionTiroRepository
            )
        }
    })
    val state by viewModel.state.collectAsState()

    val filteredShots = remember(
        state.shots, state.pendingShots, state.selectedJugadoraId, state.showMade, state.showMissed
    ) {
        (state.shots + state.pendingShots)
            .filter { state.selectedJugadoraId <= 0 || it.idJugadora == state.selectedJugadoraId }
            .filter { state.showMade || !it.hecha }
            .filter { state.showMissed || it.hecha }
    }
    val zoneStats = remember(filteredShots) {
        filteredShots.groupBy { it.zona }
            .mapValues { (_, list) -> list.count { it.hecha } to list.size }
    }

    val isTablet = LocalIsTablet.current

    var addingHecha by remember { mutableStateOf<Boolean?>(null) }
    val addingHechaState = rememberUpdatedState(addingHecha)

    // Delete confirmation dialog state
    var deleteSesionConfirm by remember { mutableStateOf<Int?>(null) }

    Scaffold(containerColor = NavyDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Header ──────────────────────────────────────────────────────
            ShotChartHeader(
                state = state,
                filteredShotsCount = filteredShots.size,
                navController = navController,
                viewModel = viewModel
            )

            // ── Tab row ─────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = if (state.tab == ShotChartTab.SESION) 0 else 1,
                containerColor = NavyCard,
                contentColor = OrangeBase,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (state.tab == ShotChartTab.SESION) 0 else 1]),
                        color = OrangeBase
                    )
                }
            ) {
                Tab(
                    selected = state.tab == ShotChartTab.SESION,
                    onClick = { viewModel.setTab(ShotChartTab.SESION) },
                    icon = {
                        Icon(
                            Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    text = {
                        Text(
                            "SESIÓN",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    },
                    selectedContentColor = OrangeBase,
                    unselectedContentColor = TextSecondary
                )
                Tab(
                    selected = state.tab == ShotChartTab.HISTORIAL,
                    onClick = { viewModel.setTab(ShotChartTab.HISTORIAL) },
                    icon = {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    text = {
                        Text(
                            "HISTORIAL",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    },
                    selectedContentColor = OrangeBase,
                    unselectedContentColor = TextSecondary
                )
            }

            // ── Tab content ─────────────────────────────────────────────────
            when (state.tab) {
                ShotChartTab.SESION -> {
                    when {
                        // Case C: Viewing historial session
                        state.historialSesionId != null -> {
                            HistorialViewMode(
                                state = state,
                                filteredShots = filteredShots,
                                zoneStats = zoneStats,
                                viewModel = viewModel,
                                isTablet = isTablet
                            )
                        }
                        // Case B: Active session
                        state.currentSesion != null -> {
                            ActiveSessionMode(
                                state = state,
                                filteredShots = filteredShots,
                                zoneStats = zoneStats,
                                viewModel = viewModel,
                                isTablet = isTablet,
                                addingHecha = addingHecha,
                                addingHechaState = addingHechaState,
                                onAddingHechaChange = { addingHecha = it }
                            )
                        }
                        // Case A: No active session
                        else -> {
                            NoSessionMode(
                                state = state,
                                viewModel = viewModel
                            )
                        }
                    }
                }
                ShotChartTab.HISTORIAL -> {
                    HistorialTab(
                        state = state,
                        viewModel = viewModel,
                        onDeleteRequest = { sesionId -> deleteSesionConfirm = sesionId }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (deleteSesionConfirm != null) {
        AlertDialog(
            onDismissRequest = { deleteSesionConfirm = null },
            containerColor = NavyCard,
            title = {
                Text("Eliminar sesión", color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            },
            text = {
                Text(
                    "¿Seguro que quieres eliminar esta sesión y todos sus tiros? Esta acción no se puede deshacer.",
                    color = TextSecondary, fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteSesionConfirm?.let { viewModel.deleteSesion(it) }
                        deleteSesionConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedError)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { deleteSesionConfirm = null },
                    border = BorderStroke(1.dp, NavyBorder)
                ) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ShotChartHeader(
    state: ShotChartUiState,
    filteredShotsCount: Int,
    navController: NavController,
    viewModel: ShotChartViewModel
) {
    val title = when {
        state.historialSesionId != null -> "Viendo sesión"
        state.currentSesion != null -> "Sesión activa"
        else -> "Mapa de tiros"
    }
    val subtitle = when {
        state.historialSesionId != null -> {
            val sesion = state.sesiones.find { it.id == state.historialSesionId }
            if (sesion != null) {
                val jugadora = state.jugadoras.find { it.id == sesion.idJugadora }
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(sesion.fecha))
                "${jugadora?.nombre ?: "Equipo"} · $dateStr"
            } else ""
        }
        state.currentSesion != null -> {
            val jugadora = state.jugadoras.find { it.id == state.selectedJugadoraId }
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(state.currentSesion.fecha))
            "${jugadora?.nombre ?: "Equipo"} · $dateStr"
        }
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyCard)
            .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "SHOT CHART",
                    color = NeonGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    title,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (subtitle != null) {
                    Text(subtitle, color = TextSecondary, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            if (state.currentSesion != null || state.historialSesionId != null) {
                Text(
                    "$filteredShotsCount tiros",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Case A — No session (start screen)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoSessionMode(
    state: ShotChartUiState,
    viewModel: ShotChartViewModel
) {
    val selectedJugadoraName = when {
        state.selectedJugadoraId <= 0 -> "el equipo"
        else -> state.jugadoras.find { it.id == state.selectedJugadoraId }?.nombre ?: "la jugadora"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Player selector chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PlayerChip(
                    nombre = "EQUIPO",
                    selected = state.selectedJugadoraId == -1,
                    onClick = { viewModel.selectJugadora(-1) }
                )
            }
            items(state.jugadoras, key = { it.id }) { j ->
                PlayerChip(
                    nombre = j.nombre.split(" ").first(),
                    selected = state.selectedJugadoraId == j.id,
                    onClick = { viewModel.selectJugadora(j.id) }
                )
            }
        }

        // Start session card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NavyCard)
                .border(1.dp, OrangeBase.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.GpsFixed,
                contentDescription = null,
                tint = OrangeBase,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Nueva sesión",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Registra los tiros de entrenamiento de $selectedJugadoraName y guarda el historial de progresión.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { viewModel.startNewSession(state.selectedJugadoraId) },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeBase),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "INICIAR SESIÓN",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Recent sessions
        if (state.sesiones.isNotEmpty()) {
            Text(
                "o continúa una sesión reciente",
                color = TextTertiary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            val recent = state.sesiones.sortedByDescending { it.fecha }.take(5)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recent.forEach { sesion ->
                    val jugadora = state.jugadoras.find { it.id == sesion.idJugadora }
                    val shots = state.sesionShots[sesion.id] ?: emptyList()
                    val pct = if (shots.isEmpty()) 0 else shots.count { it.hecha } * 100 / shots.size
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(sesion.fecha))
                    RecentSessionCard(
                        fecha = dateStr,
                        jugadoraNombre = jugadora?.nombre ?: "Equipo",
                        totalTiros = shots.size,
                        pct = pct,
                        onClick = {
                            viewModel.viewHistorialSesion(sesion.id)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RecentSessionCard(
    fecha: String,
    jugadoraNombre: String,
    totalTiros: Int,
    pct: Int,
    onClick: () -> Unit
) {
    val pctColor = when {
        pct >= 55 -> NeonGreen
        pct >= 40 -> GoldAccent
        else -> RedError
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(fecha, color = TextSecondary, fontSize = 12.sp)
            Text(jugadoraNombre, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Text("$totalTiros tiros", color = TextTertiary, fontSize = 12.sp)
        Text(
            "$pct%",
            color = pctColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Case B — Active session
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActiveSessionMode(
    state: ShotChartUiState,
    filteredShots: List<com.example.tfg_aaron.data.local.entities.ShotChartDataEntity>,
    zoneStats: Map<String, Pair<Int, Int>>,
    viewModel: ShotChartViewModel,
    isTablet: Boolean,
    addingHecha: Boolean?,
    addingHechaState: State<Boolean?>,
    onAddingHechaChange: (Boolean?) -> Unit
) {
    val sesion = state.currentSesion ?: return
    val jugadora = state.jugadoras.find { it.id == state.selectedJugadoraId }
    val allSessionShots = state.shots + state.pendingShots
    val sessionPct = if (allSessionShots.isEmpty()) 0
    else allSessionShots.count { it.hecha } * 100 / allSessionShots.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // Active session header card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NavyCard)
                .border(1.dp, OrangeBase.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    jugadora?.nombre ?: "Equipo",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Text(
                    SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault())
                        .format(Date(sesion.fecha)),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${allSessionShots.size}",
                    color = OrangeBase,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text("tiros", color = TextTertiary, fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val pctColor = when {
                    sessionPct >= 55 -> NeonGreen
                    sessionPct >= 40 -> GoldAccent
                    else -> RedError
                }
                Text(
                    "$sessionPct%",
                    color = pctColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text("acierto", color = TextTertiary, fontSize = 10.sp)
            }
            OutlinedButton(
                onClick = { viewModel.endSession() },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, OrangeBase),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeBase),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("FINALIZAR", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            }
        }

        // Player selector chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PlayerChip(
                    nombre = "EQUIPO",
                    selected = state.selectedJugadoraId == -1,
                    onClick = { viewModel.selectJugadora(-1); onAddingHechaChange(null) }
                )
            }
            items(state.jugadoras, key = { it.id }) { j ->
                PlayerChip(
                    nombre = j.nombre.split(" ").first(),
                    selected = state.selectedJugadoraId == j.id,
                    onClick = { viewModel.selectJugadora(j.id); onAddingHechaChange(null) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Filter toggles
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterToggle("ENCESTADOS", state.showMade, GreenSuccess) { viewModel.toggleShowMade() }
            FilterToggle("FALLADOS", state.showMissed, RedError) { viewModel.toggleShowMissed() }
        }

        Spacer(Modifier.height(8.dp))

        // Basketball court
        CourtCanvas(
            filteredShots = filteredShots,
            addingHecha = addingHecha,
            addingHechaState = addingHechaState,
            readOnly = false,
            onTap = { nx, ny, zona, mode -> viewModel.addShot(nx, ny, zona, mode) }
        )

        Spacer(Modifier.height(10.dp))

        // Action buttons + zone stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(horizontal = if (isTablet) 80.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Main shot buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onAddingHechaChange(if (addingHecha == true) null else true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (addingHecha == true) GreenSuccess else GreenSuccess.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Filled.CheckCircle, null,
                        modifier = Modifier.size(18.dp),
                        tint = if (addingHecha == true) Color.White else GreenSuccess
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (addingHecha == true) "MODO ENCESTE" else "ENCESTAR",
                        color = if (addingHecha == true) Color.White else GreenSuccess,
                        fontWeight = FontWeight.ExtraBold, fontSize = 13.sp
                    )
                }
                Button(
                    onClick = { onAddingHechaChange(if (addingHecha == false) null else false) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (addingHecha == false) RedError else RedError.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Filled.Cancel, null,
                        modifier = Modifier.size(18.dp),
                        tint = if (addingHecha == false) Color.White else RedError
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (addingHecha == false) "MODO FALLO" else "FALLAR",
                        color = if (addingHecha == false) Color.White else RedError,
                        fontWeight = FontWeight.ExtraBold, fontSize = 13.sp
                    )
                }
            }

            // Free throw buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.addShot(0.5f, 0.58f, "LIBRE", true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GreenSuccess.copy(0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenSuccess)
                ) {
                    Text("TL ✓ Anotado", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.addShot(0.5f, 0.58f, "LIBRE", false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, RedError.copy(0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError)
                ) {
                    Text("TL ✗ Fallado", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            // Undo / clear buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val lastShot = filteredShots.lastOrNull()
                        if (lastShot != null) viewModel.deleteShotById(lastShot.id)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, YellowWarning.copy(0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YellowWarning)
                ) {
                    Icon(Icons.Filled.Undo, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Borrar último", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.clearAll() },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, RedError.copy(0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError.copy(0.7f))
                ) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Borrar todos", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            if (addingHecha == null && filteredShots.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TealAccent.copy(alpha = 0.07f))
                        .border(1.dp, TealAccent.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Filled.Info, null, tint = TealAccent, modifier = Modifier.size(18.dp))
                        Text(
                            "Pulsa ENCESTAR o FALLAR, luego toca en la cancha donde fue el tiro. La zona se detecta automáticamente.",
                            color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp
                        )
                    }
                }
            }

            // Zone statistics
            ZoneStatsSection(zoneStats = zoneStats)

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Case C — Viewing historial session (read-only)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HistorialViewMode(
    state: ShotChartUiState,
    filteredShots: List<com.example.tfg_aaron.data.local.entities.ShotChartDataEntity>,
    zoneStats: Map<String, Pair<Int, Int>>,
    viewModel: ShotChartViewModel,
    isTablet: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Read-only banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TealAccent.copy(alpha = 0.15f))
                .border(
                    width = 0.dp,
                    color = Color.Transparent
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = TealAccent,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "MODO LECTURA — Esta sesión ya está guardada",
                    color = TealAccent,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = { viewModel.exitHistorialView() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Volver", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        // Court (read-only)
        val noOp = rememberUpdatedState<Boolean?>(null)
        CourtCanvas(
            filteredShots = filteredShots,
            addingHecha = null,
            addingHechaState = noOp,
            readOnly = true,
            onTap = { _, _, _, _ -> }
        )

        Spacer(Modifier.height(10.dp))

        // Zone statistics
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(horizontal = if (isTablet) 80.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ZoneStatsSection(zoneStats = zoneStats)
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Historial tab
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HistorialTab(
    state: ShotChartUiState,
    viewModel: ShotChartViewModel,
    onDeleteRequest: (Int) -> Unit
) {
    if (state.sesiones.isEmpty()) {
        // Empty state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    "Sin sesiones aún",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text(
                    "Inicia tu primera sesión de tiro para ver el historial de progresión aquí.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
        return
    }

    val sortedSesiones = remember(state.sesiones) {
        state.sesiones.sortedByDescending { it.fecha }
    }

    // Build progression map: jugadoraId -> list of pct sorted by fecha ascending
    val progressionMap = remember(state.sesiones, state.sesionShots) {
        state.sesiones
            .groupBy { it.idJugadora }
            .mapValues { (_, sesiones) ->
                sesiones.sortedBy { it.fecha }.map { s ->
                    val shots = state.sesionShots[s.id] ?: emptyList()
                    if (shots.isEmpty()) 0 else shots.count { it.hecha } * 100 / shots.size
                }
            }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(sortedSesiones, key = { it.id }) { sesion ->
            val jugadora = state.jugadoras.find { it.id == sesion.idJugadora }
            val shots = state.sesionShots[sesion.id] ?: emptyList()
            val pct = if (shots.isEmpty()) 0 else shots.count { it.hecha } * 100 / shots.size
            val zStats = shots.groupBy { it.zona }
                .mapValues { (_, l) -> l.count { it.hecha } to l.size }

            // Progression trend
            val jugadoraPcts = progressionMap[sesion.idJugadora] ?: emptyList()
            val sesionIndex = state.sesiones.sortedBy { it.fecha }.indexOfFirst { it.id == sesion.id }
            val trendText: String?
            val trendColor: Color?
            if (jugadoraPcts.size >= 2 && sesionIndex > 0) {
                val prevPct = jugadoraPcts[sesionIndex - 1]
                val diff = pct - prevPct
                trendText = if (diff >= 0) "↑ +$diff%" else "↓ $diff%"
                trendColor = if (diff >= 0) NeonGreen else RedError
            } else {
                trendText = null
                trendColor = null
            }

            HistorialSessionCard(
                sesion = sesion,
                jugadoraNombre = jugadora?.nombre ?: "Equipo",
                totalTiros = shots.size,
                pct = pct,
                zoneStats = zStats,
                trendText = trendText,
                trendColor = trendColor,
                onClick = {
                    viewModel.viewHistorialSesion(sesion.id)
                    viewModel.setTab(ShotChartTab.SESION)
                },
                onDeleteRequest = { onDeleteRequest(sesion.id) }
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun HistorialSessionCard(
    sesion: SesionTiroEntity,
    jugadoraNombre: String,
    totalTiros: Int,
    pct: Int,
    zoneStats: Map<String, Pair<Int, Int>>,
    trendText: String?,
    trendColor: Color?,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val pctColor = when {
        pct >= 55 -> NeonGreen
        pct >= 40 -> GoldAccent
        else -> RedError
    }
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(sesion.fecha))
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(sesion.fecha))

    // Zone abbreviations for compact display
    val zoneAbbrev = mapOf(
        "TRIPLE_IZQ" to "3P Izq",
        "TRIPLE_CENT" to "3P Cnt",
        "TRIPLE_DER" to "3P Der",
        "MEDIA_DIST" to "MD",
        "PINTURA" to "ZP",
        "LIBRE" to "TL"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Date/time
            Column(
                modifier = Modifier.width(72.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(dateStr, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(timeStr, color = TextTertiary, fontSize = 11.sp)
            }

            // Jugadora + tiros
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        jugadoraNombre,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    if (trendText != null && trendColor != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(trendColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                trendText,
                                color = trendColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                Text("$totalTiros tiros", color = TextTertiary, fontSize = 12.sp)
            }

            // Percentage
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$pct%",
                    color = pctColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Text("general", color = TextTertiary, fontSize = 10.sp)
            }

            // Delete button
            IconButton(
                onClick = onDeleteRequest,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "Eliminar sesión",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Zone breakdown row
        if (zoneStats.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyElevated.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                zoneStats.entries.take(4).forEach { (zona, pair) ->
                    val (made, total) = pair
                    val zonePct = if (total > 0) made * 100 / total else 0
                    val zColor = when {
                        zonePct >= 55 -> NeonGreen
                        zonePct >= 40 -> GoldAccent
                        else -> RedError
                    }
                    val abbrev = zoneAbbrev[zona] ?: zona.take(4)
                    Text(
                        "$abbrev $zonePct%",
                        color = zColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (zoneStats.entries.take(4).last().key != zona) {
                        Text("·", color = TextTertiary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared: Basketball Court Canvas
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CourtCanvas(
    filteredShots: List<com.example.tfg_aaron.data.local.entities.ShotChartDataEntity>,
    addingHecha: Boolean?,
    addingHechaState: State<Boolean?>,
    readOnly: Boolean,
    onTap: (nx: Float, ny: Float, zona: String, mode: Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(15f / 14f)
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0A1628))
            .border(
                2.dp,
                when (addingHecha) {
                    true -> GreenSuccess.copy(0.8f)
                    false -> RedError.copy(0.8f)
                    null -> NavyBorder
                },
                RoundedCornerShape(14.dp)
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!readOnly) Modifier.pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val mode = addingHechaState.value
                            if (mode != null) {
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val margin = 12.dp.toPx()
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
                                onTap(nx, ny, zona, mode)
                            }
                        }
                    } else Modifier
                )
        ) {
            val w = size.width
            val h = size.height
            val lineColor = Color(0xFF4A7AB5)
            val lineColorBright = Color(0xFF6A9ADA)
            val strokeW = 2.dp.toPx()
            val margin = 12.dp.toPx()

            // Letterbox: fit court (15:14 aspect) into available canvas space
            val courtAspect = 15f / 14f
            val canvasAspect = w / h
            val scale: Float
            val courtOffX: Float
            val courtOffY: Float
            if (canvasAspect >= courtAspect) {
                // wider than court: use full height, pillarbox sides
                scale = (h - 2 * margin) / 14f
                courtOffX = (w - 15f * scale) / 2f
                courtOffY = margin
            } else {
                // taller than court: use full width, letterbox top/bottom
                scale = (w - 2 * margin) / 15f
                courtOffX = margin
                courtOffY = (h - 14f * scale) / 2f
            }
            val courtW = 15f * scale
            val courtH = 14f * scale
            val baselineY = courtOffY + courtH
            val midcourtY = courtOffY
            val basketX = courtOffX + courtW / 2f
            val basketY = baselineY - 1.575f * scale  // basket is 1.575m from baseline

            // ── Court background ──────────────────────────────────────
            drawRect(Color(0xFF091420))
            val path = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        courtOffX, courtOffY, courtOffX + courtW, courtOffY + courtH, 6.dp.toPx(), 6.dp.toPx()
                    )
                )
            }
            drawPath(path, Color(0xFF0D1E30))

            // ── Court boundary ────────────────────────────────────────
            drawRect(
                color = lineColorBright,
                topLeft = Offset(courtOffX, courtOffY),
                size = Size(courtW, courtH),
                style = Stroke(strokeW)
            )

            // ── Mid-court line ────────────────────────────────────────
            drawLine(
                color = lineColor.copy(alpha = 0.6f),
                start = Offset(courtOffX, midcourtY),
                end = Offset(courtOffX + courtW, midcourtY),
                strokeWidth = strokeW
            )

            // ── Center half-circle (midcourt, visible at top edge) ────
            val centerR = 1.8f * scale
            drawArc(
                color = lineColor,
                startAngle = 0f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(basketX - centerR, midcourtY - centerR),
                size = Size(centerR * 2f, centerR * 2f),
                style = Stroke(strokeW)
            )

            // ── Three-point line ──────────────────────────────────────
            val threeR = 6.75f * scale
            // Corner lines: 0.9m from sideline (FIBA)
            val cornerX = courtOffX + 0.9f * scale
            val cornerXR = courtOffX + courtW - 0.9f * scale
            val dxCorner = cornerX - basketX
            val cornerArcY = basketY - sqrt((threeR * threeR - dxCorner * dxCorner).coerceAtLeast(0f))

            drawLine(lineColor, Offset(cornerX, cornerArcY), Offset(cornerX, baselineY), strokeW)
            drawLine(lineColor, Offset(cornerXR, cornerArcY), Offset(cornerXR, baselineY), strokeW)

            // Arc from left corner to right corner (going away from basket)
            val arcStartDeg = Math.toDegrees(atan2((cornerArcY - basketY).toDouble(), (cornerX - basketX).toDouble())).toFloat()
            val arcEndDeg = Math.toDegrees(atan2((cornerArcY - basketY).toDouble(), (cornerXR - basketX).toDouble())).toFloat()
            val arcSweep = arcEndDeg - arcStartDeg
            drawArc(
                color = lineColor,
                startAngle = arcStartDeg,
                sweepAngle = arcSweep,
                useCenter = false,
                topLeft = Offset(basketX - threeR, basketY - threeR),
                size = Size(threeR * 2f, threeR * 2f),
                style = Stroke(strokeW)
            )

            // ── Paint / Key ───────────────────────────────────────────
            val paintW = 4.9f * scale   // FIBA lane: 4.9m wide
            val paintH = 5.8f * scale   // free throw line: 5.8m from baseline
            val paintL = basketX - paintW / 2f
            val paintTop = baselineY - paintH
            drawRect(
                color = Color(0xFF0B1E35),
                topLeft = Offset(paintL, paintTop),
                size = Size(paintW, paintH)
            )
            drawRect(
                color = lineColor,
                topLeft = Offset(paintL, paintTop),
                size = Size(paintW, paintH),
                style = Stroke(strokeW)
            )
            // Hash marks (4 per side)
            val markLen = 8.dp.toPx()
            val markSpacing = paintH / 5f
            (1..4).forEach { i ->
                val markY = paintTop + i * markSpacing
                drawLine(lineColor, Offset(paintL - markLen, markY), Offset(paintL, markY), strokeW * 0.7f)
                drawLine(lineColor, Offset(paintL + paintW, markY), Offset(paintL + paintW + markLen, markY), strokeW * 0.7f)
            }

            // ── Free throw circle (radius 1.8m, center at FT line) ────
            val ftR = 1.8f * scale
            val ftCy = paintTop
            // Bottom half — solid (inside paint)
            drawArc(
                color = lineColor,
                startAngle = 0f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(basketX - ftR, ftCy - ftR),
                size = Size(ftR * 2f, ftR * 2f),
                style = Stroke(strokeW)
            )
            // Top half — lighter (above paint)
            drawArc(
                color = lineColor.copy(alpha = 0.5f),
                startAngle = 180f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(basketX - ftR, ftCy - ftR),
                size = Size(ftR * 2f, ftR * 2f),
                style = Stroke(strokeW)
            )

            // ── Restricted area arc (radius 1.25m) ───────────────────
            val raR = 1.25f * scale
            drawArc(
                color = lineColor,
                startAngle = 180f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(basketX - raR, basketY - raR),
                size = Size(raR * 2f, raR * 2f),
                style = Stroke(strokeW * 0.8f)
            )

            // ── Backboard + basket ────────────────────────────────────
            val boardW = 0.915f * scale   // backboard = 1.83m total
            drawLine(
                color = lineColorBright,
                start = Offset(basketX - boardW, baselineY),
                end = Offset(basketX + boardW, baselineY),
                strokeWidth = strokeW * 2.5f
            )
            drawCircle(
                color = Color(0xFFFF7A00),
                radius = 7.dp.toPx(),
                center = Offset(basketX, basketY),
                style = Stroke(2.5f.dp.toPx())
            )

            // ── Plot shots ────────────────────────────────────────────
            filteredShots.forEachIndexed { idx, shot ->
                // Small deterministic jitter so overlapping shots remain distinguishable
                // Use index so pending shots (negative IDs) also get stable per-slot jitter
                val seed = idx + 1
                val jitterX = (seed * 3.7f % 10f) - 5f
                val jitterY = (seed * 2 % 5 * 3.3f % 10f) - 5f
                val sx = courtOffX + shot.zonaX * courtW + jitterX
                val sy = courtOffY + shot.zonaY * courtH + jitterY
                if (shot.hecha) {
                    drawCircle(
                        color = GreenSuccess,
                        radius = 7.dp.toPx(),
                        center = Offset(sx, sy)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = 7.dp.toPx(),
                        center = Offset(sx, sy),
                        style = Stroke(1.5f.dp.toPx())
                    )
                } else {
                    // X mark for missed
                    val r = 6.dp.toPx()
                    drawCircle(
                        color = RedError.copy(alpha = 0.6f),
                        radius = r,
                        center = Offset(sx, sy),
                        style = Stroke(2.dp.toPx())
                    )
                    drawLine(
                        color = RedError.copy(alpha = 0.9f),
                        start = Offset(sx - r * 0.7f, sy - r * 0.7f),
                        end = Offset(sx + r * 0.7f, sy + r * 0.7f),
                        strokeWidth = 1.8f.dp.toPx()
                    )
                    drawLine(
                        color = RedError.copy(alpha = 0.9f),
                        start = Offset(sx + r * 0.7f, sy - r * 0.7f),
                        end = Offset(sx - r * 0.7f, sy + r * 0.7f),
                        strokeWidth = 1.8f.dp.toPx()
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared: Zone statistics section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ZoneStatsSection(zoneStats: Map<String, Pair<Int, Int>>) {
    if (zoneStats.isEmpty()) return
    Spacer(Modifier.height(16.dp))
    Text(
        "ESTADÍSTICAS POR ZONA",
        color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp
    )
    Spacer(Modifier.height(8.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val zoneLabels = mapOf(
            "PINTURA" to "Zona / Pintura",
            "MEDIA_DIST" to "Media distancia",
            "TRIPLE_IZQ" to "Triple Izq.",
            "TRIPLE_CENT" to "Triple Centro",
            "TRIPLE_DER" to "Triple Der.",
            "LIBRE" to "Tiro Libre"
        )
        zoneStats.forEach { (zona, pair) ->
            val (made, total) = pair
            val pct = if (total > 0) (made * 100f / total) else 0f
            val barColor = when {
                pct >= 55 -> NeonGreen
                pct >= 40 -> GoldAccent
                else -> RedError
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    zoneLabels[zona] ?: zona.replace("_", " "),
                    color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(110.dp)
                )
                LinearProgressIndicator(
                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = barColor, trackColor = NavyElevated
                )
                Text("$made/$total", color = TextTertiary, fontSize = 11.sp, modifier = Modifier.width(36.dp))
                Text(
                    "${pct.toInt()}%", color = barColor,
                    fontWeight = FontWeight.ExtraBold, fontSize = 12.sp,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// calcularZonaAuto — verbatim from original
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Auto-detect shot zone from normalized tap position (0-1) and canvas pixel dimensions.
 * No dialog needed — zone is determined by court geometry.
 */
fun calcularZonaAuto(x: Float, y: Float, canvasW: Float, canvasH: Float): String {
    // x, y are court-relative normalized (0-1) where 0,0 = top-left of court, 1,1 = bottom-right
    val cW = 1500f; val cH = 1400f  // virtual 15m × 14m in mm
    val basketX = cW / 2f
    val baselineY = cH
    val basketY = baselineY - 157.5f
    val threeR = 675f
    val cornerX = 90f; val cornerXR = cW - 90f
    val dxCorner = cornerX - basketX
    val cornerArcY = basketY - sqrt((threeR * threeR - dxCorner * dxCorner).coerceAtLeast(0f))
    val tapX = x * cW
    val tapY = y * cH
    if (tapX < cornerX && tapY > cornerArcY) return "TRIPLE_IZQ"
    if (tapX > cornerXR && tapY > cornerArcY) return "TRIPLE_DER"
    val dx = tapX - basketX; val dy = tapY - basketY
    if (sqrt(dx * dx + dy * dy) > threeR) return when {
        tapX < cW * 0.40f -> "TRIPLE_IZQ"
        tapX > cW * 0.60f -> "TRIPLE_DER"
        else -> "TRIPLE_CENT"
    }
    val paintW = 490f; val paintL = basketX - paintW / 2f; val paintTop = baselineY - 580f
    return if (tapX in paintL..(paintL + paintW) && tapY >= paintTop) "PINTURA" else "MEDIA_DIST"
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable chips / toggles — verbatim from original
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlayerChip(nombre: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) NeonGreen.copy(alpha = 0.2f) else NavyCard)
            .border(1.dp, if (selected) NeonGreen else NavyBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            nombre,
            color = if (selected) NeonGreen else TextSecondary,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun FilterToggle(label: String, active: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) color.copy(alpha = 0.2f) else NavyCard)
            .border(1.dp, if (active) color else NavyBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (active) color else TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
