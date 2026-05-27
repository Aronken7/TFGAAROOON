package com.example.tfg_aaron.ui.screens.partidos

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun PartidosScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: PartidosViewModel = viewModel(factory = viewModelFactory {
        initializer { PartidosViewModel(entrenadorId, app.partidoRepository) }
    })
    val uiState by viewModel.uiState.collectAsState()

    val isTablet = LocalIsTablet.current
    var showDeleteDialog by remember { mutableStateOf<PartidoEntity?>(null) }
    var showKonfetti by remember { mutableStateOf(false) }
    // 0=Todos, 1=Victoria, 2=Derrota, 3=Local, 4=Visitante
    var filtroActivo by remember { mutableStateOf(0) }

    val partidosFiltrados = remember(filtroActivo, uiState.partidos) {
        when (filtroActivo) {
            1 -> uiState.partidos.filter { it.puntosPropio > it.puntosRival }
            2 -> uiState.partidos.filter { it.puntosPropio < it.puntosRival }
            3 -> uiState.partidos.filter { it.esLocal }
            4 -> uiState.partidos.filter { !it.esLocal }
            else -> uiState.partidos
        }
    }

    Scaffold(
        bottomBar = { CoachBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditPartido.createRoute()) },
                containerColor = NeonGreen,
                contentColor = NavyDark,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Filled.Add, "Nuevo partido", modifier = Modifier.size(26.dp))
            }
        },
        containerColor = NavyDark
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isTablet) 2 else 1),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalArrangement = if (isTablet) Arrangement.spacedBy(10.dp) else Arrangement.Start
        ) {
            // ── App Bar ──────────────────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                PlayVisionTopBar { navController.navigate(Screen.Perfil.route) }
            }

            // ── Record Card ──────────────────────────────────────────────────
            if (uiState.partidos.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val total = uiState.victorias + uiState.derrotas
                    val winPct = if (total > 0) uiState.victorias.toFloat() / total else 0f
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        NeonGreen.copy(alpha = 0.12f),
                                        NavyCard,
                                        RedError.copy(alpha = 0.07f)
                                    )
                                )
                            )
                            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "RÉCORD DE TEMPORADA",
                                    color = TextTertiary, fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                                )
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonGreen.copy(0.12f))
                                        .border(1.dp, NeonGreen.copy(0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${(winPct * 100).toInt()}% WIN",
                                        color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Victorias
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${uiState.victorias}",
                                        color = NeonGreen,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 64.sp,
                                        letterSpacing = (-3).sp,
                                        lineHeight = 64.sp
                                    )
                                    Text("VICTORIAS", color = NeonGreen.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                }
                                // Separador
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Text("–", color = TextTertiary, fontSize = 32.sp, fontWeight = FontWeight.Thin)
                                    Text("$total PJ", color = TextTertiary, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                // Derrotas
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${uiState.derrotas}",
                                        color = RedError,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 64.sp,
                                        letterSpacing = (-3).sp,
                                        lineHeight = 64.sp
                                    )
                                    Text("DERROTAS", color = RedError.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                }
                            }
                            // Win % bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(RedError.copy(0.25f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(winPct)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(listOf(NeonGreen, NeonGreen.copy(0.7f)))
                                        )
                                )
                            }
                        }
                    }
                }

                // ── Media Cards ──────────────────────────────────────────────
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MediaCard(
                            label = "MEDIA ANOTADA",
                            value = "%.1f".format(uiState.promedioAnotado),
                            iconTint = NeonGreen,
                            icon = Icons.Filled.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                        MediaCard(
                            label = "MEDIA RECIBIDA",
                            value = "%.1f".format(uiState.promedioEncajado),
                            iconTint = RedError,
                            icon = Icons.Filled.Shield,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Section Label + Filters ─────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Text(
                        "LISTA DE PARTIDOS",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = TextTertiary, fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Todos", "Victoria", "Derrota", "Local", "Visitante")
                            .forEachIndexed { i, label ->
                                val selected = filtroActivo == i
                                val chipColor = when (i) {
                                    1 -> com.example.tfg_aaron.ui.theme.NeonGreen
                                    2 -> com.example.tfg_aaron.ui.theme.RedError
                                    3 -> com.example.tfg_aaron.ui.theme.TealAccent
                                    4 -> com.example.tfg_aaron.ui.theme.PurpleAccent
                                    else -> com.example.tfg_aaron.ui.theme.TextSecondary
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (selected) chipColor.copy(0.15f) else Color.Transparent)
                                        .border(1.dp, if (selected) chipColor else NavyBorder, RoundedCornerShape(20.dp))
                                        .clickable { filtroActivo = i }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        label,
                                        color = if (selected) chipColor else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal
                                    )
                                }
                            }
                    }
                }
            }

            // ── Empty State ──────────────────────────────────────────────────
            if (uiState.partidos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NeonGreen.copy(0.08f))
                                    .border(1.dp, NeonGreen.copy(0.2f), RoundedCornerShape(16.dp))
                            ) {
                                Icon(Icons.Filled.SportsBasketball, null, tint = NeonGreen, modifier = Modifier.size(32.dp))
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Sin partidos registrados", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("Registra resultados para ver estadísticas", color = TextTertiary, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else if (partidosFiltrados.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sin resultados para este filtro", color = TextTertiary, fontSize = 14.sp)
                    }
                }
            } else {
                gridItems(partidosFiltrados, key = { it.id }) { partido ->
                    PartidoCard(
                        partido = partido,
                        onEdit = {
                            if (partido.puntosPropio > partido.puntosRival) showKonfetti = true
                            navController.navigate(Screen.AddEditPartido.createRoute(partido.id))
                        },
                        onDelete = { showDeleteDialog = partido },
                        onEnVivo = { navController.navigate(Screen.PartidoEnVivo.createRoute(partido.id)) },
                        onStats = { navController.navigate(Screen.EstadisticasPartido.createRoute(partido.id)) },
                        onAlineacion = { navController.navigate(Screen.Alineacion.createRoute(partido.id)) }
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(80.dp)) }
        }

        // ── Delete Dialog ─────────────────────────────────────────────────────
        showDeleteDialog?.let { p ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                containerColor = NavyElevated,
                shape = RoundedCornerShape(16.dp),
                title = { Text("Eliminar partido", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("¿Eliminar el partido vs ${p.rival}?", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deletePartido(p); showDeleteDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RedError),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Eliminar", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteDialog = null },
                        border = BorderStroke(1.dp, NavyBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Cancelar", color = TextSecondary) }
                }
            )
        }

        if (showKonfetti) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(
                    Party(
                        speed = 0f, maxSpeed = 30f, damping = 0.9f, spread = 360,
                        colors = listOf(0xADFF2F, 0xFF5A1F, 0x0A84FF, 0x8B5CF6).map { it.toInt() },
                        position = Position.Relative(0.5, 0.0),
                        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
                    )
                ),
                updateListener = object : OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(system: nl.dionsegijn.konfetti.core.PartySystem, activeSystems: Int) {
                        if (activeSystems == 0) showKonfetti = false
                    }
                }
            )
        }
    }
}

// ── MEDIA CARD ────────────────────────────────────────────────────────────────

@Composable
private fun MediaCard(
    label: String, value: String,
    iconTint: Color, icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(listOf(iconTint.copy(0.10f), NavyCard))
            )
            .border(1.dp, iconTint.copy(0.2f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(0.15f))
                ) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
                }
                Text(label, color = TextTertiary, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, letterSpacing = (-1).sp, lineHeight = 32.sp)
            Spacer(Modifier.height(2.dp))
            Text("PTS/G", color = iconTint, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        }
    }
}

// ── PARTIDO CARD ─────────────────────────────────────────────────────────────

@Composable
fun PartidoCard(
    partido: PartidoEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEnVivo: () -> Unit,
    onStats: () -> Unit = {},
    onAlineacion: () -> Unit = {}
) {
    val sdf = SimpleDateFormat("d MMM", Locale("es"))
    val isWin = partido.puntosPropio > partido.puntosRival
    val isLoss = partido.puntosPropio < partido.puntosRival
    val hasScore = partido.puntosPropio > 0 || partido.puntosRival > 0
    val jornada = if (partido.jornada > 0) "J${partido.jornada}" else "—"

    val resultColor = when {
        !hasScore -> TextTertiary
        isWin -> NeonGreen
        isLoss -> RedError
        else -> TextSecondary
    }
    val resultLabel = when {
        !hasScore -> "PENDIENTE"
        isWin -> "VICTORIA"
        isLoss -> "DERROTA"
        else -> "EMPATE"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NavyCard)
            .border(1.dp, resultColor.copy(0.2f), RoundedCornerShape(16.dp))
    ) {
        // ── Accent stripe izquierda ──
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(resultColor.copy(0.9f), resultColor.copy(0.3f))
                    )
                )
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Header: badges + fecha ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(resultColor.copy(0.15f))
                            .border(1.dp, resultColor.copy(0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(resultLabel, color = resultColor, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavyElevated)
                            .border(1.dp, NavyBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(jornada, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        if (partido.esLocal) Icons.Filled.Home else Icons.Filled.Flight,
                        null, tint = TextTertiary, modifier = Modifier.size(11.dp)
                    )
                    Text(
                        "${sdf.format(Date(partido.fecha)).uppercase()} · ${if (partido.esLocal) "CASA" else "FUERA"}",
                        color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Score section con fondo sutil ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                if (isWin) NeonGreen.copy(0.06f) else NavyElevated.copy(0.5f),
                                NavyElevated.copy(0.3f),
                                if (isLoss) RedError.copy(0.06f) else NavyElevated.copy(0.5f)
                            )
                        )
                    )
                    .clickable(onClick = onEdit)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nuestros puntos
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "NOSOTROS",
                            color = TextTertiary, fontSize = 7.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp
                        )
                        Text(
                            "${partido.puntosPropio}",
                            color = if (isWin) NeonGreen else if (isLoss) TextSecondary else TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 52.sp,
                            letterSpacing = (-2).sp,
                            lineHeight = 52.sp
                        )
                    }
                    // Separador central
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("VS", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    }
                    // Puntos rival
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(
                            partido.rival.uppercase().take(12),
                            color = TextSecondary, fontSize = 7.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp
                        )
                        Text(
                            "${partido.puntosRival}",
                            color = if (isLoss) RedError else if (isWin) TextSecondary else TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 52.sp,
                            letterSpacing = (-2).sp,
                            lineHeight = 52.sp
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = NavyBorder)

            // ── Quick action chips ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // EN VIVO
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonGreen.copy(0.12f))
                        .border(1.dp, NeonGreen.copy(0.35f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onEnVivo)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.PlayArrow, null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                        Text("EN VIVO", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    }
                }
                // STATS
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealAccent.copy(0.12f))
                        .border(1.dp, TealAccent.copy(0.35f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onStats)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.QueryStats, null, tint = TealAccent, modifier = Modifier.size(12.dp))
                        Text("STATS", color = TealAccent, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    }
                }
                // ALINEACIÓN
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OrangeBase.copy(0.12f))
                        .border(1.dp, OrangeBase.copy(0.35f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onAlineacion)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Groups, null, tint = OrangeBase, modifier = Modifier.size(12.dp))
                        Text("ALINEACIÓN", color = OrangeBase, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    }
                }
                // EDITAR
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyElevated)
                        .border(1.dp, NavyBorder, RoundedCornerShape(8.dp))
                        .clickable(onClick = onEdit)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Edit, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                        Text("EDITAR", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    }
                }
                // BORRAR
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RedError.copy(0.08f))
                        .border(1.dp, RedError.copy(0.3f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onDelete)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.7f), modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}
