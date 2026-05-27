package com.example.tfg_aaron.ui.screens.jugadoras

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.ShotChartDataEntity
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import kotlin.math.*

@Composable
fun JugadoraDetailScreen(navController: NavController, jugadoraId: Int, entrenadorId: Int) {
    val isTablet = LocalIsTablet.current
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: JugadorasViewModel = viewModel(factory = viewModelFactory {
        initializer { JugadorasViewModel(entrenadorId, app.jugadoraRepository, app.estadisticaRepository, partidoRepository = app.partidoRepository) }
    })
    val detail by viewModel.detailState.collectAsState()

    LaunchedEffect(jugadoraId) { viewModel.loadDetail(jugadoraId) }

    val jugadora = detail.jugadora

    val shots by produceState(initialValue = emptyList<ShotChartDataEntity>()) {
        app.shotChartRepository.getByJugadoraAndEntrenador(jugadoraId, entrenadorId).collect { value = it }
    }

    Scaffold(
        containerColor = NavyDark,
        floatingActionButton = {
            if (jugadora != null) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddEditJugadora.createRoute(jugadoraId)) },
                    containerColor = GoldAccent,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Edit, "Editar")
                }
            }
        }
    ) { padding ->
        if (detail.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldAccent)
            }
            return@Scaffold
        }

        if (jugadora == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Jugadora no encontrada", color = TextSecondary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isTablet) 80.dp else 0.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(posicionColor(jugadora.posicion).copy(alpha = 0.2f))
                            .border(3.dp, posicionColor(jugadora.posicion), CircleShape)
                    ) {
                        Text(
                            text = jugadora.numero.toString(),
                            color = posicionColor(jugadora.posicion),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "${jugadora.nombre} ${jugadora.apellidos}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BadgeChip(jugadora.posicion, posicionColor(jugadora.posicion))
                        BadgeChip(jugadora.rol, rolColor(jugadora.rol))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats rápidas
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = if (jugadora.edad > 0) "${jugadora.edad}" else "-",
                    label = "Edad",
                    icon = Icons.Filled.Cake,
                    color = TealAccent,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = if (jugadora.altura > 0) "${jugadora.altura.roundToInt()}cm" else "-",
                    label = "Altura",
                    icon = Icons.Filled.Height,
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "${detail.promedioTiros.roundToInt()}%",
                    label = "T. Libres",
                    icon = Icons.Filled.SportsBasketball,
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Info section
            InfoSection(title = "Información General") {
                InfoRow("Nombre completo", "${jugadora.nombre} ${jugadora.apellidos}")
                InfoRow("Dorsal", "#${jugadora.numero}")
                InfoRow("Posición", jugadora.posicion)
                InfoRow("Rol", jugadora.rol)
                if (jugadora.edad > 0) InfoRow("Edad", "${jugadora.edad} años")
                if (jugadora.altura > 0) InfoRow("Altura", "${jugadora.altura.roundToInt()} cm")
            }

            if (jugadora.notas.isNotEmpty()) {
                InfoSection(title = "Notas del entrenador") {
                    Text(
                        text = jugadora.notas,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // Áreas de mejora
            InfoSection(title = "Áreas de Mejora") {
                if (jugadora.areasMejora.isEmpty()) {
                    Text(
                        "Sin áreas de mejora registradas",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    jugadora.areasMejora.split(",").filter { it.isNotBlank() }.forEach { area ->
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.FitnessCenter, null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(area.trim(), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Tiros libres stats
            if (detail.totalIntentados > 0) {
                InfoSection(title = "Estadísticas de Tiros") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatMini("${detail.totalIntentados}", "Intentados", GoldAccent)
                        StatMini("${detail.totalAcertados}", "Acertados", GreenSuccess)
                        StatMini("${detail.promedioTiros.roundToInt()}%", "Promedio", GoldAccent)
                    }
                    // Progress bar
                    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                        LinearProgressIndicator(
                            progress = { (detail.promedioTiros / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = GoldAccent,
                            trackColor = NavyElevated
                        )
                    }
                }
            }

            // Historical match stats
            if (detail.promedios.partidosJugados > 0) {
                InfoSection(title = "Promedios en Partidos (${detail.promedios.partidosJugados} partidos)") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatMini("%.1f".format(detail.promedios.ptsMedia), "PTS", GoldAccent)
                        StatMini("%.1f".format(detail.promedios.rebMedia), "REB", TealAccent)
                        StatMini("%.1f".format(detail.promedios.astMedia), "AST", GreenSuccess)
                    }
                    HorizontalDivider(color = NavyBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatMini("%.1f".format(detail.promedios.robMedia), "ROB", TealAccent)
                        StatMini("%.1f".format(detail.promedios.perMedia), "PER", RedError)
                        StatMini("%.1f".format(detail.promedios.tapMedia), "TAP", GoldAccent)
                        StatMini("%.1f".format(detail.promedios.falMedia), "FAL", YellowWarning)
                    }
                    HorizontalDivider(color = NavyBorder, modifier = Modifier.padding(horizontal = 16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatMini(
                            if (detail.promedios.pct2 > 0) "${detail.promedios.pct2.toInt()}%" else "-",
                            "%2PT", TealAccent
                        )
                        StatMini(
                            if (detail.promedios.pct3 > 0) "${detail.promedios.pct3.toInt()}%" else "-",
                            "%3PT", PurpleAccent
                        )
                        StatMini(
                            if (detail.promedios.pctTL > 0) "${detail.promedios.pctTL.toInt()}%" else "-",
                            "%TL", GoldAccent
                        )
                    }
                }

                // Last 5 matches
                val lastMatches = detail.historial.take(5)
                if (lastMatches.isNotEmpty()) {
                    InfoSection(title = "Últimos Partidos") {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("RIVAL", color = TextTertiary, fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                listOf("PTS", "REB", "AST").forEach { h ->
                                    Text(h, color = TextTertiary, fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.width(36.dp))
                                }
                            }
                            lastMatches.forEach { match ->
                                HorizontalDivider(color = NavyBorder, modifier = Modifier.padding(horizontal = 8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        match.rival,
                                        color = TextPrimary, fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text("${match.puntos}", color = GoldAccent, fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.width(36.dp))
                                    Text("${match.rebotes}", color = TealAccent, fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.width(36.dp))
                                    Text("${match.asistencias}", color = GreenSuccess, fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.width(36.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Shot Chart section
            if (shots.isNotEmpty()) {
                val zoneLabels = mapOf(
                    "PINTURA" to "Pintura",
                    "MEDIA_DIST" to "Media dist.",
                    "TRIPLE_IZQ" to "Triple Izq.",
                    "TRIPLE_CENT" to "Triple Centro",
                    "TRIPLE_DER" to "Triple Der."
                )
                val zoneOrder = listOf("PINTURA", "MEDIA_DIST", "TRIPLE_IZQ", "TRIPLE_CENT", "TRIPLE_DER")

                InfoSection(title = "Mapa de Tiros en Partidos (${shots.size} tiros)") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF091420))
                            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
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

                    // Zone stats
                    val zoneStats = shots.groupBy { it.zona }
                    val zonesWithData = zoneOrder.filter { zoneStats.containsKey(it) }
                    if (zonesWithData.isNotEmpty()) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
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
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                                    )
                                }
                            }
                        }
                    }

                    // Legend
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(GreenSuccess)
                            )
                            Text("Anotado", color = TextSecondary, fontSize = 11.sp)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, RedError, CircleShape)
                            )
                            Text("Fallado", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Historial Físico button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = { navController.navigate(Screen.HistorialFisico.createRoute(jugadoraId)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(Icons.Filled.FitnessCenter, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Historial Físico", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = GoldAccent,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider(color = NavyBorder, modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun StatMini(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}
