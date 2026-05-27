@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.tfg_aaron.ui.screens.acwr

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.ui.components.GlassCard
import com.example.tfg_aaron.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun AcwrScreen(
    navController: NavController,
    entrenadorId: Int
) {
    val app = LocalContext.current.applicationContext as TFGApplication
    val viewModel: AcwrViewModel = viewModel(
        factory = AcwrViewModel.factory(
            app.wellnessDiarioRepository,
            app.jugadoraRepository,
            entrenadorId
        )
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ACWR — Carga de Trabajo",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonGreen)
            }
            return@Scaffold
        }

        if (state.jugadoras.isEmpty()) {
            AcwrEmptyState(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Player / Team selector
            item {
                PlayerSelectorRow(
                    jugadoras = state.jugadoras,
                    selectedId = state.selectedJugadoraId,
                    onSelect = { viewModel.selectJugadora(it) }
                )
            }

            // Detail view for a single selected player
            val selectedId = state.selectedJugadoraId
            if (selectedId != null) {
                val data = state.playerData.firstOrNull { it.jugadora.id == selectedId }
                if (data != null) {
                    item {
                        PlayerAcwrDetailCard(data = data)
                    }
                    item {
                        DailyLoadBarChart(
                            dailyLoads = data.dailyLoads,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Team overview
                item {
                    TeamSummaryRow(playerData = state.playerData)
                }
                items(state.playerData, key = { it.jugadora.id }) { data ->
                    PlayerAcwrRowCard(
                        data = data,
                        onClick = { viewModel.selectJugadora(data.jugadora.id) }
                    )
                }
            }
        }
    }
}

// ── Zone helpers ──────────────────────────────────────────────────────────────

@Composable
private fun zoneColor(zone: AcwrZone): Color = when (zone) {
    AcwrZone.SAFE    -> GreenSuccess
    AcwrZone.WARNING -> YellowWarning
    AcwrZone.DANGER  -> RedError
    AcwrZone.NO_DATA -> TextTertiary
}

@Composable
private fun zoneLabel(zone: AcwrZone): String = when (zone) {
    AcwrZone.SAFE    -> "Zona Segura"
    AcwrZone.WARNING -> "Precaución"
    AcwrZone.DANGER  -> "Peligro"
    AcwrZone.NO_DATA -> "Sin datos"
}

// ── Player selector row ───────────────────────────────────────────────────────

@Composable
private fun PlayerSelectorRow(
    jugadoras: List<JugadoraEntity>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val labelText = if (selectedId == null) {
        "Equipo completo"
    } else {
        jugadoras.firstOrNull { it.id == selectedId }
            ?.let { "${it.nombre} ${it.apellidos}" } ?: "Equipo completo"
    }

    Box {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        if (selectedId == null) Icons.Filled.Group else Icons.Filled.Person,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(labelText, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextSecondary)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(NavyElevated)
        ) {
            DropdownMenuItem(
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Group, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        Text("Equipo completo", color = TextPrimary)
                    }
                },
                onClick = { onSelect(null); expanded = false }
            )
            HorizontalDivider(color = NavyBorder)
            jugadoras.forEach { j ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "${j.nombre} ${j.apellidos}  #${j.numero}",
                            color = if (j.id == selectedId) NeonGreen else TextPrimary
                        )
                    },
                    onClick = { onSelect(j.id); expanded = false }
                )
            }
        }
    }
}

// ── Team summary chips ────────────────────────────────────────────────────────

@Composable
private fun TeamSummaryRow(playerData: List<PlayerAcwrData>) {
    val safe    = playerData.count { it.zone == AcwrZone.SAFE }
    val warning = playerData.count { it.zone == AcwrZone.WARNING }
    val danger  = playerData.count { it.zone == AcwrZone.DANGER }
    val noData  = playerData.count { it.zone == AcwrZone.NO_DATA }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (safe > 0)    ZoneChip(count = safe,    zone = AcwrZone.SAFE,    modifier = Modifier.weight(1f))
        if (warning > 0) ZoneChip(count = warning, zone = AcwrZone.WARNING, modifier = Modifier.weight(1f))
        if (danger > 0)  ZoneChip(count = danger,  zone = AcwrZone.DANGER,  modifier = Modifier.weight(1f))
        if (noData > 0)  ZoneChip(count = noData,  zone = AcwrZone.NO_DATA, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ZoneChip(count: Int, zone: AcwrZone, modifier: Modifier = Modifier) {
    val color = zoneColor(zone)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = zoneLabel(zone),
                color = color.copy(alpha = 0.85f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Player row card (team list) ───────────────────────────────────────────────

@Composable
private fun PlayerAcwrRowCard(
    data: PlayerAcwrData,
    onClick: () -> Unit
) {
    val color = zoneColor(data.zone)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Number badge + name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(NavyElevated)
                        .border(1.dp, NavyBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "#${data.jugadora.numero}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        "${data.jugadora.nombre} ${data.jugadora.apellidos}",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        data.jugadora.posicion,
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }

            // ACWR value + zone badge
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = data.acwr?.let { String.format("%.2f", it) } ?: "—",
                    color = color,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                ZonePill(zone = data.zone)
            }
        }
    }
}

@Composable
private fun ZonePill(zone: AcwrZone) {
    val color = zoneColor(zone)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = zoneLabel(zone),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Single player detail card ─────────────────────────────────────────────────

@Composable
private fun PlayerAcwrDetailCard(data: PlayerAcwrData) {
    val color = zoneColor(data.zone)

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Header: name + zone pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${data.jugadora.nombre} ${data.jugadora.apellidos}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        "${data.jugadora.posicion}  ·  #${data.jugadora.numero}",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
                ZonePill(zone = data.zone)
            }

            // Big ACWR number
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = data.acwr?.let { String.format("%.2f", it) } ?: "—",
                        color = color,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("Ratio Agudo:Crónico", color = TextSecondary, fontSize = 12.sp)
                }
            }

            // ACWR gauge bar
            AcwrGaugeBar(acwr = data.acwr, color = color)

            HorizontalDivider(color = NavyBorder)

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AcwrStatItem(
                    label = "Carga Aguda",
                    sublabel = "Últimos 7 días",
                    value = "${data.acuteLoad}",
                    unit = "UA",
                    color = TealAccent
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(56.dp)
                        .background(NavyBorder)
                )
                AcwrStatItem(
                    label = "Carga Crónica",
                    sublabel = "Media semanal 28d",
                    value = String.format("%.0f", data.chronicLoad),
                    unit = "UA",
                    color = PurpleAccent
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(56.dp)
                        .background(NavyBorder)
                )
                AcwrStatItem(
                    label = "ACWR",
                    sublabel = zoneLabel(data.zone),
                    value = data.acwr?.let { String.format("%.2f", it) } ?: "—",
                    unit = "",
                    color = color
                )
            }
        }
    }
}

@Composable
private fun AcwrStatItem(
    label: String,
    sublabel: String,
    value: String,
    unit: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            if (unit.isNotEmpty()) {
                Text(unit, color = color.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
        Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(sublabel, color = TextTertiary, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

// ── ACWR Gauge Bar ────────────────────────────────────────────────────────────

@Composable
private fun AcwrGaugeBar(acwr: Double?, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Zone labels
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0", color = TextTertiary, fontSize = 10.sp)
            Text("0.5", color = RedError, fontSize = 10.sp)
            Text("0.8", color = GreenSuccess, fontSize = 10.sp)
            Text("1.3", color = YellowWarning, fontSize = 10.sp)
            Text("1.5", color = RedError, fontSize = 10.sp)
            Text("2.0", color = TextTertiary, fontSize = 10.sp)
        }

        val dangerStart1 = RedError
        val safeColor    = GreenSuccess
        val warnColor    = YellowWarning
        val dangerColor2 = RedError
        val indicatorColor = color

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            val w = size.width
            val h = size.height
            val radius = CornerRadius(h / 2, h / 2)

            // Max ACWR displayed = 2.0
            val scale = 2.0f

            // Background track
            drawRoundRect(
                color = Color.White.copy(alpha = 0.06f),
                size = Size(w, h),
                cornerRadius = radius
            )

            // Danger zone 0–0.5
            val x05 = w * (0.5f / scale)
            drawRoundRect(
                color = dangerStart1.copy(alpha = 0.35f),
                size = Size(x05, h),
                cornerRadius = radius
            )

            // Safe zone 0.8–1.3
            val x08 = w * (0.8f / scale)
            val x13 = w * (1.3f / scale)
            drawRect(
                color = safeColor.copy(alpha = 0.3f),
                topLeft = Offset(x08, 0f),
                size = Size(x13 - x08, h)
            )

            // Warning zone 1.3–1.5
            val x15 = w * (1.5f / scale)
            drawRect(
                color = warnColor.copy(alpha = 0.3f),
                topLeft = Offset(x13, 0f),
                size = Size(x15 - x13, h)
            )

            // Danger zone >1.5
            drawRoundRect(
                color = dangerColor2.copy(alpha = 0.25f),
                topLeft = Offset(x15, 0f),
                size = Size(w - x15, h),
                cornerRadius = radius
            )

            // Indicator needle
            if (acwr != null) {
                val clampedAcwr = acwr.coerceIn(0.0, scale.toDouble()).toFloat()
                val indicatorX = (w * (clampedAcwr / scale)).coerceIn(h / 2, w - h / 2)
                drawCircle(
                    color = indicatorColor,
                    radius = h / 2,
                    center = Offset(indicatorX, h / 2)
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.5f),
                    radius = h / 4,
                    center = Offset(indicatorX, h / 2)
                )
            }
        }
    }
}

// ── Daily load bar chart (Canvas, last 28 days) ───────────────────────────────

@Composable
private fun DailyLoadBarChart(
    dailyLoads: List<Pair<Long, Int>>,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.BarChart, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                Text(
                    "Carga Diaria — Últimos 28 días",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            if (dailyLoads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin registros en los últimos 28 días", color = TextTertiary, fontSize = 13.sp)
                }
            } else {
                val maxLoad = dailyLoads.maxOf { it.second }.coerceAtLeast(1)
                val barColor = NeonGreen
                val axisColor = NavyBorder

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val canvasW = size.width
                    val canvasH = size.height
                    val bottomPad = 20.dp.toPx()
                    val topPad = 4.dp.toPx()
                    val chartH = canvasH - bottomPad - topPad
                    val barCount = dailyLoads.size
                    val gap = 2.dp.toPx()
                    val totalGap = gap * (barCount - 1)
                    val barW = if (barCount > 0) ((canvasW - totalGap) / barCount).coerceAtLeast(2f) else canvasW

                    // Horizontal axis line
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, canvasH - bottomPad),
                        end = Offset(canvasW, canvasH - bottomPad),
                        strokeWidth = 1.dp.toPx()
                    )

                    dailyLoads.forEachIndexed { index, (_, load) ->
                        val normalizedH = (load.toFloat() / maxLoad.toFloat()) * chartH
                        val left = index * (barW + gap)
                        val top = canvasH - bottomPad - normalizedH

                        drawRoundRect(
                            color = barColor.copy(alpha = 0.85f),
                            topLeft = Offset(left, top),
                            size = Size(barW, normalizedH.coerceAtLeast(2f)),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }

                // X-axis date labels (first and last)
                if (dailyLoads.isNotEmpty()) {
                    val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(sdf.format(Date(dailyLoads.first().first)), color = TextTertiary, fontSize = 10.sp)
                        Text(sdf.format(Date(dailyLoads.last().first)), color = TextTertiary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun AcwrEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.BarChart,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                "Sin jugadoras activas",
                color = TextSecondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                "Añade jugadoras al equipo para visualizar el ACWR y monitorizar la carga de entrenamiento.",
                color = TextTertiary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
