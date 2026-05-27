package com.example.tfg_aaron.ui.screens.partidos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tfg_aaron.data.local.entities.ShotChartDataEntity
import com.example.tfg_aaron.ui.theme.*
import kotlin.math.*

@Composable
fun StatsTab(state: PartidoEnVivoUiState, onAddRival: () -> Unit = {}) {
    var showTeam by remember { mutableStateOf(true) }
    var shotChartJugadoraId by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Team selector - clickable tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NavyCard)
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TeamTab(
                label = "Mi Equipo",
                selected = showTeam,
                color = OrangeBase,
                modifier = Modifier.weight(1f)
            ) { showTeam = true }
            TeamTab(
                label = "Rival (${state.rivalPlayers.size})",
                selected = !showTeam,
                color = RedError,
                modifier = Modifier.weight(1f)
            ) { showTeam = false }
        }

        if (showTeam) {
            MyTeamStatsContent(state = state, onShowShotChart = { jugadoraId ->
                shotChartJugadoraId = jugadoraId
            })
        } else {
            RivalStatsContent(state = state, onAddRival = onAddRival)
        }
    }

    shotChartJugadoraId?.let { jugadoraId ->
        val jugadora = state.jugadoras.find { it.id == jugadoraId }
        val shots = state.shotsPartido.filter { it.idJugadora == jugadoraId }
        PlayerShotChartDialog(
            jugadoraNombre = jugadora?.let { "${it.nombre} ${it.apellidos}" } ?: "Jugadora",
            shots = shots,
            onDismiss = { shotChartJugadoraId = null }
        )
    }
}

@Composable
private fun PlayerShotChartDialog(
    jugadoraNombre: String,
    shots: List<ShotChartDataEntity>,
    onDismiss: () -> Unit
) {
    val zoneLabels = mapOf(
        "PINTURA" to "Pintura",
        "MEDIA_DIST" to "Media dist.",
        "TRIPLE_IZQ" to "Triple Izq.",
        "TRIPLE_CENT" to "Triple Centro",
        "TRIPLE_DER" to "Triple Der."
    )
    val zoneOrder = listOf("PINTURA", "MEDIA_DIST", "TRIPLE_IZQ", "TRIPLE_CENT", "TRIPLE_DER")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Text(
                    text = jugadoraNombre,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Mapa de tiros en este partido",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Canvas - court with shots
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF091420))
                        .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val lineColor = Color(0xFF4A7AB5)
                        val lineColorBright = Color(0xFF6A9ADA)
                        val strokeW = 2.dp.toPx()
                        val margin = 12.dp.toPx()
                        val courtAspect = 15f / 14f
                        val canvasAspect = w / h
                        val scale: Float
                        val courtOffX: Float
                        val courtOffY: Float
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
                        val basketX = courtOffX + courtW / 2f
                        val basketY = baselineY - 1.575f * scale

                        drawRect(Color(0xFF091420))
                        val courtPath = Path().apply {
                            addRoundRect(
                                androidx.compose.ui.geometry.RoundRect(
                                    courtOffX, courtOffY,
                                    courtOffX + courtW, courtOffY + courtH,
                                    6.dp.toPx(), 6.dp.toPx()
                                )
                            )
                        }
                        drawPath(courtPath, Color(0xFF0D1E30))
                        drawRect(
                            color = lineColorBright,
                            topLeft = Offset(courtOffX, courtOffY),
                            size = Size(courtW, courtH),
                            style = Stroke(strokeW)
                        )
                        drawLine(
                            color = lineColor.copy(0.6f),
                            start = Offset(courtOffX, courtOffY),
                            end = Offset(courtOffX + courtW, courtOffY),
                            strokeWidth = strokeW
                        )
                        val threeR = 6.75f * scale
                        val cornerX = courtOffX + 0.9f * scale
                        val cornerXR = courtOffX + courtW - 0.9f * scale
                        val dxCorner = cornerX - basketX
                        val cornerArcY = basketY - sqrt((threeR * threeR - dxCorner * dxCorner).coerceAtLeast(0f))
                        drawLine(lineColor, Offset(cornerX, cornerArcY), Offset(cornerX, baselineY), strokeW)
                        drawLine(lineColor, Offset(cornerXR, cornerArcY), Offset(cornerXR, baselineY), strokeW)
                        val arcStartDeg = Math.toDegrees(
                            atan2((cornerArcY - basketY).toDouble(), (cornerX - basketX).toDouble())
                        ).toFloat()
                        val arcEndDeg = Math.toDegrees(
                            atan2((cornerArcY - basketY).toDouble(), (cornerXR - basketX).toDouble())
                        ).toFloat()
                        drawArc(
                            color = lineColor,
                            startAngle = arcStartDeg,
                            sweepAngle = arcEndDeg - arcStartDeg,
                            useCenter = false,
                            topLeft = Offset(basketX - threeR, basketY - threeR),
                            size = Size(threeR * 2f, threeR * 2f),
                            style = Stroke(strokeW)
                        )
                        val paintW = 4.9f * scale
                        val paintH = 5.8f * scale
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
                        val ftR = 1.8f * scale
                        val ftCy = paintTop
                        drawArc(
                            color = lineColor,
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(basketX - ftR, ftCy - ftR),
                            size = Size(ftR * 2f, ftR * 2f),
                            style = Stroke(strokeW)
                        )
                        drawArc(
                            color = lineColor.copy(0.5f),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(basketX - ftR, ftCy - ftR),
                            size = Size(ftR * 2f, ftR * 2f),
                            style = Stroke(strokeW)
                        )
                        val boardW = 0.915f * scale
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

                        // Plot shots
                        shots.forEachIndexed { idx, shot ->
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
                                    color = Color.White.copy(0.5f),
                                    radius = 7.dp.toPx(),
                                    center = Offset(sx, sy),
                                    style = Stroke(1.5f.dp.toPx())
                                )
                            } else {
                                val r = 6.dp.toPx()
                                drawCircle(
                                    color = RedError.copy(0.6f),
                                    radius = r,
                                    center = Offset(sx, sy),
                                    style = Stroke(2.dp.toPx())
                                )
                                drawLine(
                                    color = RedError.copy(0.9f),
                                    start = Offset(sx - r * 0.7f, sy - r * 0.7f),
                                    end = Offset(sx + r * 0.7f, sy + r * 0.7f),
                                    strokeWidth = 1.8f.dp.toPx()
                                )
                                drawLine(
                                    color = RedError.copy(0.9f),
                                    start = Offset(sx + r * 0.7f, sy - r * 0.7f),
                                    end = Offset(sx - r * 0.7f, sy + r * 0.7f),
                                    strokeWidth = 1.8f.dp.toPx()
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Zone stats
                val zoneStats = shots.groupBy { it.zona }
                val zonesWithData = zoneOrder.filter { zoneStats.containsKey(it) }

                if (zonesWithData.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        zonesWithData.forEach { zona ->
                            val zonShots = zoneStats[zona] ?: emptyList()
                            val anotados = zonShots.count { it.hecha }
                            val intentados = zonShots.size
                            val pct = if (intentados > 0) (anotados * 100f / intentados) else 0f
                            val pctColor = when {
                                pct >= 50f -> GreenSuccess
                                pct >= 35f -> YellowWarning
                                else -> RedError
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = zoneLabels[zona] ?: zona,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$anotados/$intentados",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${pct.toInt()}%",
                                    color = pctColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(40.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        "Sin tiros registrados",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Close button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar", color = OrangeBase, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TeamTab(
    label: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) color.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
            }
            Text(
                text = label,
                color = if (selected) color else TextTertiary,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun MyTeamStatsContent(state: PartidoEnVivoUiState, onShowShotChart: (Int) -> Unit = {}) {
    val jugadoras = state.jugadoras.filter {
        it.id in state.stats || state.metricasAvanzadas.containsKey(it.id)
    }.sortedByDescending {
        state.metricasAvanzadas[it.id]?.valoracion ?: 0
    }

    if (jugadoras.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.BarChart, null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Sin datos todavía", color = TextSecondary, fontWeight = FontWeight.SemiBold)
            }
        }
        return
    }

    val headers = listOf("PTS", "VAL", "MIN", "+/-", "REB", "AST", "ROB", "PER", "TAP", "FAL", "%2", "%3", "%TL")

    // SHARED scroll state — all rows scroll together
    val sharedScrollState = rememberScrollState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Header row
        item {
            Row(
                modifier = Modifier.horizontalScroll(sharedScrollState).padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        .background(NavyElevated)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(6.dp))
                    Text("#", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(28.dp))
                    Text("JUGADORA", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(82.dp))
                    headers.forEach { h ->
                        val color = when (h) {
                            "PTS" -> GoldAccent
                            "VAL" -> PurpleAccent
                            "MIN" -> TealAccent
                            "+/-" -> GreenSuccess
                            else -> TextTertiary
                        }
                        Text(
                            h, color = color.copy(alpha = 0.8f),
                            fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }
            }
        }

        items(jugadoras) { j ->
            val st = state.stats[j.id] ?: EstadisticasJugadora()
            val m = state.metricasAvanzadas[j.id] ?: MetricasAvanzadas()
            val isExpulsada = st.expulsada
            val isOnCourt = j.id in state.enCancha

            Row(
                modifier = Modifier.horizontalScroll(sharedScrollState).padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isExpulsada -> RedError.copy(alpha = 0.06f)
                                isOnCourt -> TealAccent.copy(alpha = 0.04f)
                                else -> NavyCard
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                isExpulsada -> RedError.copy(alpha = 0.2f)
                                isOnCourt -> TealAccent.copy(alpha = 0.1f)
                                else -> NavyBorder
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onShowShotChart(j.id) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // On-court dot
                    Box(modifier = Modifier.width(6.dp)) {
                        if (isOnCourt) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(GreenSuccess))
                        }
                    }

                    // Number badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(26.dp).clip(CircleShape)
                            .background(
                                when {
                                    isExpulsada -> RedError.copy(0.15f)
                                    isOnCourt -> TealAccent.copy(0.1f)
                                    else -> GoldAccent.copy(0.08f)
                                }
                            )
                    ) {
                        Text(
                            "#${j.numero}",
                            color = when {
                                isExpulsada -> RedError
                                isOnCourt -> TealAccent
                                else -> GoldAccent
                            },
                            fontSize = 8.sp, fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.width(4.dp))

                    // Name
                    Column(modifier = Modifier.width(78.dp)) {
                        Text(
                            j.nombre,
                            color = if (isExpulsada) RedError else TextPrimary,
                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            if (isExpulsada) "EXPULSADA" else j.posicion,
                            color = if (isExpulsada) RedError else TextTertiary,
                            fontSize = 9.sp
                        )
                    }

                    // Stats cells
                    StatCell("${st.puntos}", GoldAccent)
                    StatCell(
                        "${m.valoracion}",
                        when {
                            m.valoracion > 5 -> PurpleAccent
                            m.valoracion > 0 -> GreenSuccess
                            m.valoracion < -3 -> RedError
                            m.valoracion < 0 -> YellowWarning
                            else -> TextSecondary
                        }
                    )
                    StatCell(
                        "${m.minutosEnCancha.toInt()}",
                        if (m.minutosEnCancha >= 25) YellowWarning else TextSecondary
                    )
                    val pmText = if (m.plusMinus >= 0) "+${m.plusMinus}" else "${m.plusMinus}"
                    StatCell(
                        pmText,
                        if (m.plusMinus > 0) GreenSuccess else if (m.plusMinus < 0) RedError else TextSecondary
                    )
                    StatCell("${st.rebotesTotales}", TealAccent)
                    StatCell("${st.asistencias}", TealAccent.copy(alpha = 0.8f))
                    StatCell("${st.robos}", TextSecondary)
                    StatCell("${st.perdidas}", if (st.perdidas >= 4) YellowWarning else TextSecondary)
                    StatCell("${st.tapones}", TextSecondary)
                    StatCell(
                        "${st.faltasTotales}",
                        when {
                            st.faltasTotales >= 5 -> RedError
                            st.faltasTotales >= 4 -> YellowWarning
                            st.faltasTotales >= 3 -> OrangeBase
                            else -> TextSecondary
                        }
                    )
                    StatCell(if (st.tirosDos > 0) "${st.pct2.toInt()}%" else "-", TextSecondary)
                    StatCell(if (st.tirosTres > 0) "${st.pct3.toInt()}%" else "-", TextSecondary)
                    StatCell(if (st.tirosLibres > 0) "${st.pctTL.toInt()}%" else "-", TextSecondary)
                }
            }
        }

        // Totals row
        item {
            val totalPts = state.stats.values.sumOf { it.puntos }
            val totalReb = state.stats.values.sumOf { it.rebotesTotales }
            val totalAst = state.stats.values.sumOf { it.asistencias }
            val totalRob = state.stats.values.sumOf { it.robos }
            val totalPer = state.stats.values.sumOf { it.perdidas }
            val totalTap = state.stats.values.sumOf { it.tapones }
            val totalFal = state.stats.values.sumOf { it.faltasTotales }
            val totalVal = state.metricasAvanzadas.values.sumOf { it.valoracion }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.horizontalScroll(sharedScrollState).padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldAccent.copy(alpha = 0.06f))
                        .border(1.dp, GoldAccent.copy(0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(6.dp))
                    Spacer(Modifier.width(28.dp))
                    Text(
                        "TOTAL", color = GoldAccent,
                        fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp, modifier = Modifier.width(82.dp)
                    )
                    StatCell("$totalPts", GoldAccent)
                    StatCell("$totalVal", PurpleAccent)
                    StatCell("-", TextTertiary)
                    StatCell("-", TextTertiary)
                    StatCell("$totalReb", TealAccent)
                    StatCell("$totalAst", TealAccent.copy(alpha = 0.8f))
                    StatCell("$totalRob", TextSecondary)
                    StatCell("$totalPer", TextSecondary)
                    StatCell("$totalTap", TextSecondary)
                    StatCell("$totalFal", YellowWarning)
                    StatCell("-", TextTertiary)
                    StatCell("-", TextTertiary)
                    StatCell("-", TextTertiary)
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun RivalStatsContent(state: PartidoEnVivoUiState, onAddRival: () -> Unit = {}) {
    if (state.rivalPlayers.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                    .background(RedError.copy(alpha = 0.06f))
                    .border(1.dp, RedError.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.GroupAdd, null, tint = RedError.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Sin rivales añadidos", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "Añade jugadoras del equipo rival para\nregistrar sus estadísticas",
                color = TextTertiary, fontSize = 12.sp,
                textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp)
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onAddRival,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedError)
            ) {
                Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Añadir jugadora rival", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // SHARED scroll state for rival stats
    val sharedScrollState = rememberScrollState()
    val rivalHeaders = listOf("FP", "FT", "FA", "TOT", "REB", "AST", "ROB", "PER", "TAP")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.horizontalScroll(sharedScrollState).padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        .background(NavyElevated)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(30.dp))
                    Text("JUGADORA RIVAL", color = RedError.copy(alpha = 0.7f), fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(100.dp))
                    rivalHeaders.forEach { h ->
                        val color = when (h) {
                            "TOT" -> RedError
                            "REB" -> TealAccent
                            "AST" -> GoldAccent
                            else -> TextTertiary
                        }
                        Text(
                            h, color = color.copy(alpha = 0.8f),
                            fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }
            }
        }

        items(state.rivalPlayers) { rival ->
            val st = state.rivalStats[rival.id] ?: EstadisticasRival()
            val isExpulsada = st.expulsada

            Row(
                modifier = Modifier.horizontalScroll(sharedScrollState).padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isExpulsada) RedError.copy(alpha = 0.06f) else NavyCard)
                        .border(
                            1.dp,
                            if (isExpulsada) RedError.copy(alpha = 0.2f) else NavyBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(26.dp).clip(CircleShape).background(RedError.copy(0.1f))
                    ) {
                        Text(
                            if (rival.numero.isNotEmpty()) "#${rival.numero}" else "${rival.id}",
                            color = RedError, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(modifier = Modifier.width(96.dp)) {
                        Text(
                            rival.nombre,
                            color = if (isExpulsada) RedError else TextPrimary,
                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        if (isExpulsada) {
                            Text("EXPULSADA", color = RedError, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    StatCell(
                        "${st.faltasPersonales}",
                        if (st.faltasPersonales >= 4) RedError else if (st.faltasPersonales >= 3) YellowWarning else TextSecondary
                    )
                    StatCell("${st.faltasTecnicas}", if (st.faltasTecnicas >= 1) PinkAccent else TextSecondary)
                    StatCell("${st.faltasAntideportivas}", if (st.faltasAntideportivas >= 1) PinkAccent else TextSecondary)
                    StatCell(
                        "${st.faltasTotales}",
                        if (st.expulsada) RedError else if (st.faltasTotales >= 4) YellowWarning else TextSecondary
                    )
                    StatCell("${st.rebotesTotales}", TealAccent)
                    StatCell("${st.asistencias}", GoldAccent)
                    StatCell("${st.robos}", TextSecondary)
                    StatCell("${st.perdidas}", TextSecondary)
                    StatCell("${st.tapones}", TextSecondary)
                }
            }
        }

        // Add rival button at bottom
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(
                    onClick = onAddRival,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RedError.copy(alpha = 0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError)
                ) {
                    Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Añadir rival", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun StatCell(value: String, color: Color) {
    Text(
        text = value,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(36.dp)
    )
}
