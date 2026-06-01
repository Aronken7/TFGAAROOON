package com.example.tfg_aaron.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: DashboardViewModel = viewModel(factory = viewModelFactory {
        initializer {
            DashboardViewModel(
                entrenadorId, app.jugadoraRepository,
                app.sesionRepository, app.scoutingRepository, app.userPreferences,
                app.partidoRepository, app.estadisticaPartidoRepository
            )
        }
    })
    val stats by viewModel.stats.collectAsState()
    val proximoPartido by viewModel.proximoPartido.collectAsState()
    val ultimosResultados by viewModel.ultimosResultados.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }
    val isTablet = LocalIsTablet.current

    Scaffold(
        bottomBar = { CoachBottomNavBar(navController) },
        containerColor = NavyDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            PlayVisionTopBar(
                onSearchClick = { navController.navigate(Screen.Busqueda.route) },
                onSettingsClick = { navController.navigate(Screen.Perfil.route) }
            )

            HeroBanner(stats, navController)

            Spacer(Modifier.height(16.dp))

            MetricsRow(stats, isTablet)

            proximoPartido?.let {
                Spacer(Modifier.height(16.dp))
                ProximoPartidoCard(it, diasHastaPartido = stats.diasHastaPartido, onClick = { navController.navigate(Screen.Partidos.route) })
            }

            if (stats.totalPartidos > 0) {
                Spacer(Modifier.height(16.dp))
                SeasonRecordCard(stats)
                if (ultimosResultados.size >= 3) {
                    Spacer(Modifier.height(12.dp))
                    RachaResultadosCard(ultimosResultados)
                }
            }

            stats.jugadoraDestacada?.let { jugadora ->
                Spacer(Modifier.height(16.dp))
                JugadoraDestacadaCard(jugadora, stats.puntosDestacada, stats.partidosDestacada)
            }

            Spacer(Modifier.height(16.dp))

            LiveActionBanner(onClick = { navController.navigate(Screen.Partidos.route) })

            Spacer(Modifier.height(16.dp))

            QuickActionsSection(navController, isTablet)

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── HERO BANNER ──────────────────────────────────────────────────────────────

@Composable
private fun HeroBanner(stats: DashboardStats, navController: NavController) {
    val today = SimpleDateFormat("EEEE, d MMMM", Locale("es")).format(Date())
    val firstName = stats.nombreEntrenador.split(" ").firstOrNull()?.takeIf { it.isNotEmpty() } ?: "Entrenador"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(NeonGreen.copy(alpha = 0.07f), NavyDark)
                    )
                )
            }
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column {
            // Date badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeonGreen.copy(0.12f))
                    .border(1.dp, NeonGreen.copy(0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    today.uppercase(),
                    color = NeonGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Hola, Coach",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                firstName,
                color = TextPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                lineHeight = 36.sp
            )

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (stats.nombreEquipo.isNotEmpty()) {
                    HeroBadge(Icons.Filled.Shield, stats.nombreEquipo.uppercase(), NeonGreen)
                }
                HeroBadge(Icons.Filled.EmojiEvents, "TEMPORADA 24/25", GoldAccent)
            }
        }
    }
}

@Composable
private fun HeroBadge(icon: ImageVector, text: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(0.1f))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(11.dp))
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp)
    }
}

// ── METRICS ROW ──────────────────────────────────────────────────────────────

@Composable
private fun MetricsRow(stats: DashboardStats, isTablet: Boolean) {
    val items = listOf(
        Triple(Icons.Filled.People, "JUGADORAS", "${stats.totalJugadoras}") to NeonGreen,
        Triple(Icons.Filled.CalendarMonth, "SESIONES", "${stats.totalSesiones}") to TealAccent,
        Triple(Icons.Filled.Visibility, "SCOUTING", "${stats.totalScouting}") to PurpleAccent,
        Triple(Icons.Filled.SportsBasketball, "PARTIDOS", "${stats.totalPartidos}") to GoldAccent
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { (triple, color) ->
            val (icon, label, value) = triple
            MetricCard(icon, label, value, color, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCard(icon: ImageVector, label: String, value: String, accent: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, accent.copy(0.15f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accent.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
        }
        Text(
            value,
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp
        )
        Text(
            label,
            color = accent.copy(0.8f),
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
    }
}

// ── SEASON RECORD CARD ────────────────────────────────────────────────────────

@Composable
private fun SeasonRecordCard(stats: DashboardStats) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(stats.winRate) {
        animProgress.animateTo(
            stats.winRate / 100f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.EmojiEvents, null, tint = GoldAccent, modifier = Modifier.size(14.dp))
            Text(
                "RENDIMIENTO DE TEMPORADA",
                color = GoldAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Animated win rate arc
            WinRateArc(progress = animProgress.value, winRate = stats.winRate, size = 110.dp)

            // W / L / stats column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // W-L big display
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${stats.victorias}",
                            color = NeonGreen,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-2).sp,
                            lineHeight = 44.sp
                        )
                        Text("V", color = NeonGreen.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text(
                        "—",
                        color = TextTertiary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${stats.derrotas}",
                            color = RedError,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-2).sp,
                            lineHeight = 44.sp
                        )
                        Text("D", color = RedError.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                HorizontalDivider(color = NavyBorder)

                // PTS row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("PTS A FAVOR", color = TextTertiary, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
                        Spacer(Modifier.height(2.dp))
                        Text("%.1f".format(stats.promedioAnotado), color = NeonGreen, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("PTS EN CONTRA", color = TextTertiary, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
                        Spacer(Modifier.height(2.dp))
                        Text("%.1f".format(stats.promedioEncajado), color = RedError, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Win rate bar
                WinRateBar(stats.victorias, stats.derrotas)
            }
        }
    }
}

@Composable
private fun WinRateArc(progress: Float, winRate: Float, size: Dp) {
    val arcColor = when {
        winRate >= 60f -> NeonGreen
        winRate >= 40f -> GoldAccent
        else -> RedError
    }
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(inset, inset)

            // Background track
            drawArc(
                color = NavyElevated,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            // Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(arcColor.copy(0.6f), arcColor)
                ),
                startAngle = 135f,
                sweepAngle = 270f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${winRate.toInt()}%",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )
            Text(
                "WIN",
                color = TextTertiary,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun WinRateBar(victorias: Int, derrotas: Int) {
    val total = (victorias + derrotas).coerceAtLeast(1)
    val winFrac = victorias.toFloat() / total

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(RedError.copy(0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(winFrac)
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(
                    Brush.horizontalGradient(listOf(NeonGreen.copy(0.7f), NeonGreen))
                )
        )
    }
}

// ── LIVE ACTION BANNER ────────────────────────────────────────────────────────

@Composable
private fun LiveActionBanner(onClick: () -> Unit) {
    val infiniteAnim = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteAnim.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(GreenSurface, NavyCard)
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(listOf(NeonGreen.copy(0.6f), NeonGreen.copy(0.2f))),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NeonGreen.copy(pulse * 0.2f))
                        .border(2.dp, NeonGreen.copy(pulse), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.SportsBasketball,
                        null,
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        "MODO EN VIVO",
                        color = NeonGreen.copy(0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "Gestionar Partidos",
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.3).sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ArrowForward, null, tint = NeonGreen, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── QUICK ACTIONS ─────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsSection(navController: NavController, isTablet: Boolean) {
    data class Action(val icon: ImageVector, val label: String, val accent: Color, val onClick: () -> Unit)

    val actions = listOf(
        Action(Icons.Filled.People,              "MI EQUIPO",    NeonGreen)   { navController.navigate(Screen.Jugadoras.route) },
        Action(Icons.Filled.CalendarMonth,       "SESIONES",     TealAccent)  { navController.navigate(Screen.Sesiones.route) },
        Action(Icons.Filled.Draw,                "PIZARRA",      GoldAccent)  { navController.navigate(Screen.Pizarra.route) },
        Action(Icons.Filled.Visibility,          "SCOUTING",     PurpleAccent){ navController.navigate(Screen.Scouting.route) },
        Action(Icons.Filled.BarChart,            "AVANZADAS",    TealAccent)  { navController.navigate(Screen.EstadisticasAvanzadas.route) },
        Action(Icons.Filled.RadioButtonUnchecked,"SHOT CHART",   GoldAccent)  { navController.navigate(Screen.ShotChart.route) },
        Action(Icons.Filled.FitnessCenter,       "TEST FÍSICO",  NeonGreen)   { navController.navigate(Screen.TestFisico.createRoute()) },
        Action(Icons.Filled.Favorite,            "WELLNESS",     RedError)    { navController.navigate(Screen.Wellness.route) },
        Action(Icons.Filled.Assignment,          "GAME PLAN",    GoldAccent)  { navController.navigate(Screen.GamePlan.route) },
        Action(Icons.Filled.Groups,              "RIVALES",      PurpleAccent){ navController.navigate(Screen.RivalPerfil.route) },
        Action(Icons.Filled.VideoLibrary,        "VÍDEOS",       TealAccent)  { navController.navigate(Screen.Videos.route) },
        Action(Icons.Filled.CalendarToday,       "CALENDARIO",   NeonGreen)   { navController.navigate(Screen.Calendario.route) },
        Action(Icons.Filled.SportsBasketball,    "EJERCICIOS",   GoldAccent)  { navController.navigate(Screen.Ejercicios.route) },
        Action(Icons.Filled.Description,         "REPORTES",     TextSecondary){ navController.navigate(Screen.Reportes.route) },
        Action(Icons.Filled.MedicalServices,     "LESIONES",     RedError)    { navController.navigate(Screen.Lesiones.route) },
        Action(Icons.Filled.TrackChanges,        "OBJETIVOS",    TealAccent)  { navController.navigate(Screen.Objetivos.route) },
        Action(Icons.Filled.EmojiEvents,         "LIGA",         GoldAccent)  { navController.navigate(Screen.Clasificacion.route) },
        Action(Icons.Filled.CompareArrows,       "COMPARADOR",   ElectricBlue){ navController.navigate(Screen.Comparador.route) },
    )

    val cols = if (isTablet) 4 else 3
    val pad = if (isTablet) 20.dp else 16.dp

    Column(modifier = Modifier.padding(horizontal = pad)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(2.dp)).background(NeonGreen))
            Text(
                "ACCIONES RÁPIDAS",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            actions.chunked(cols).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { action ->
                        QuickActionCell(action.icon, action.label, action.accent, Modifier.weight(1f), action.onClick)
                    }
                    repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCell(
    icon: ImageVector,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, accent.copy(0.12f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(accent.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Text(
                label,
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

// ── PRÓXIMO PARTIDO ───────────────────────────────────────────────────────────

@Composable
private fun ProximoPartidoCard(partido: PartidoEntity, diasHastaPartido: Int? = null, onClick: () -> Unit) {
    val sdf = java.text.SimpleDateFormat("EEE d MMM", java.util.Locale("es"))
    val fechaStr = sdf.format(java.util.Date(partido.fecha)).uppercase()

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(listOf(NavySurface, NavyCard))
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(GoldAccent.copy(0.5f), GoldAccent.copy(0.1f))),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GoldAccent.copy(0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("PRÓXIMO PARTIDO", color = GoldAccent, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (partido.esLocal) NeonGreen.copy(0.1f) else PurpleAccent.copy(0.1f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (partido.esLocal) "LOCAL" else "VISITANTE",
                            color = if (partido.esLocal) NeonGreen else PurpleAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "VS ${partido.rival.uppercase()}",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.CalendarToday, null, tint = TextTertiary, modifier = Modifier.size(11.dp))
                    Text(fechaStr, color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    if (partido.lugar.isNotBlank()) {
                        Icon(Icons.Filled.LocationOn, null, tint = TextTertiary, modifier = Modifier.size(11.dp))
                        Text(partido.lugar, color = TextTertiary, fontSize = 11.sp, maxLines = 1)
                    }
                }
                if (diasHastaPartido != null) {
                    Spacer(Modifier.height(8.dp))
                    val chipColor = when {
                        diasHastaPartido <= 3 -> NeonGreen
                        diasHastaPartido <= 7 -> GoldAccent
                        else -> TextTertiary
                    }
                    val chipLabel = if (diasHastaPartido == 0) "HOY" else "EN $diasHastaPartido DÍAS"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipColor.copy(0.12f))
                            .border(1.dp, chipColor.copy(0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            chipLabel,
                            color = chipColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldAccent.copy(0.12f))
                    .border(1.dp, GoldAccent.copy(0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.SportsBasketball, null, tint = GoldAccent, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// ── JUGADORA DESTACADA DEL MES ────────────────────────────────────────────────

@Composable
private fun JugadoraDestacadaCard(
    jugadora: JugadoraEntity,
    puntos: Int,
    partidos: Int
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(GoldAccent.copy(0.08f), NavyCard)
                )
            )
            .border(1.dp, GoldAccent.copy(0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.Star, null, tint = GoldAccent, modifier = Modifier.size(14.dp))
            Text(
                "JUGADORA DEL MES",
                color = GoldAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        // Content row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Number circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(OrangeBase.copy(0.15f))
                    .border(2.dp, OrangeBase.copy(0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#${jugadora.numero}",
                    color = OrangeBase,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
            }

            // Name and position
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${jugadora.nombre} ${jugadora.apellidos}",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${jugadora.posicion.uppercase()} · ${jugadora.rol.uppercase()}",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Stats column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "$puntos",
                    color = NeonGreen,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    lineHeight = 28.sp
                )
                Text(
                    "PTS ESTE MES",
                    color = NeonGreen.copy(0.6f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "$partidos",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "PJ",
                        color = TextTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// ── RACHA DE RESULTADOS ───────────────────────────────────────────────────────

@Composable
private fun RachaResultadosCard(partidos: List<PartidoEntity>) {
    // Partidos ya vienen en orden DESC (más reciente primero), los mostramos de izq a der = más antiguo primero
    val reversed = partidos.reversed()

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.TrendingUp, null, tint = TealAccent, modifier = Modifier.size(13.dp))
            Text("ÚLTIMOS RESULTADOS", color = TealAccent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        }
        Spacer(Modifier.height(14.dp))

        // W/L dots row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            reversed.forEach { p ->
                val isWin = p.puntosPropio > p.puntosRival
                val color = if (isWin) NeonGreen else RedError
                val barH = if (isWin) 40.dp else 28.dp
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        if (isWin) "${p.puntosPropio}" else "${p.puntosPropio}",
                        color = color,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barH)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(color.copy(if (isWin) 0.25f else 0.15f))
                            .border(
                                width = 1.dp,
                                color = color.copy(0.5f),
                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isWin) "V" else "D",
                            color = color,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = NavyBorder)
        Spacer(Modifier.height(6.dp))

        // Rival names row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            reversed.forEach { p ->
                Text(
                    p.rival.take(4).uppercase(),
                    modifier = Modifier.weight(1f),
                    color = TextTertiary,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
