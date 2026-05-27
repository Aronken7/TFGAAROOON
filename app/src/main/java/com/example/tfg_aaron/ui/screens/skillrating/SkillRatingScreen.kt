package com.example.tfg_aaron.ui.screens.skillrating

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.SkillRatingEntity
import com.example.tfg_aaron.ui.components.EmptyState
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val SKILLS = listOf("TIRO", "DEFENSA", "BALÓN", "VISIÓN", "ATLETISMO", "MENTAL.", "LIDER.")

@Composable
fun SkillRatingScreen(navController: NavController, entrenadorId: Int) {
    val isTablet = LocalIsTablet.current
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: SkillRatingViewModel = viewModel(factory = viewModelFactory {
        initializer { SkillRatingViewModel(entrenadorId, app.jugadoraRepository, app.skillRatingRepository) }
    })
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val selectedJugadora = state.jugadoras.find { it.id == state.selectedJugadoraId }
    val latestRating = state.ratings.firstOrNull()

    Scaffold(
        containerColor = NavyDark,
        floatingActionButton = {
            if (selectedJugadora != null) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = NeonGreen, contentColor = NavyDark,
                    shape = RoundedCornerShape(14.dp)
                ) { Icon(Icons.Filled.Add, null) }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isTablet) 2 else 1),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = if (isTablet) Arrangement.spacedBy(10.dp) else Arrangement.Start
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(NavyCard)
                        .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("VALORACIÓN", color = NeonGreen, fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                            Text("Análisis de habilidades", color = TextPrimary,
                                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // Player selector
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(12.dp))
                Text("SELECCIONAR JUGADORA", color = TextTertiary, fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.jugadoras, key = { it.id }) { j ->
                        val selected = j.id == state.selectedJugadoraId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) NeonGreen.copy(0.2f) else NavyCard)
                                .border(1.dp, if (selected) NeonGreen else NavyBorder, RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectJugadora(j.id) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(j.nombre.split(" ").first(), color = if (selected) NeonGreen else TextSecondary,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
                                fontSize = 13.sp)
                        }
                    }
                }
            }

            // Radar chart
            if (latestRating != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(20.dp))
                    Text("RADAR DE HABILIDADES", color = TextTertiary, fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyCard)
                            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SkillRadarChart(
                            values = listOf(
                                latestRating.tiro, latestRating.defensa, latestRating.balonMano,
                                latestRating.vision, latestRating.atletismo, latestRating.mentalidad,
                                latestRating.liderazgo
                            ),
                            labels = SKILLS,
                            modifier = Modifier.size(260.dp)
                        )
                    }
                }
            } else if (state.jugadoras.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        EmptyState(Icons.Filled.Star, "Sin valoraciones",
                            "Pulsa + para evaluar a ${selectedJugadora?.nombre ?: "la jugadora"}")
                    }
                }
            }

            // History
            if (state.ratings.size > 1) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(20.dp))
                    Text("HISTORIAL DE VALORACIONES", color = TextTertiary, fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                }
                gridItems(state.ratings, key = { it.id }) { rating ->
                    SkillRatingHistoryCard(rating, onDelete = { viewModel.deleteRating(rating.id) })
                }
            }
        }
    }

    if (showAddDialog && selectedJugadora != null) {
        AddSkillRatingDialog(
            jugadoraNombre = selectedJugadora.nombre,
            onDismiss = { showAddDialog = false },
            onSave = { tiro, def, bal, vis, atl, men, lid, notas ->
                viewModel.saveRating(selectedJugadora.id, tiro, def, bal, vis, atl, men, lid, notas)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SkillRadarChart(values: List<Int>, labels: List<String>, modifier: Modifier = Modifier) {
    val n = values.size
    val neonGreen = NeonGreen
    val navyElevated = NavyElevated
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = minOf(cx, cy) * 0.65f
        val labelRadius = minOf(cx, cy) * 0.90f
        val angleStep = (2 * PI / n).toFloat()
        val startAngle = (-PI / 2).toFloat()

        // Grid rings
        for (ring in 1..4) {
            val r = radius * ring / 4f
            val path = Path()
            for (i in 0 until n) {
                val angle = startAngle + i * angleStep
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, navyElevated, style = Stroke(width = 1.dp.toPx()))
        }

        // Axis lines
        for (i in 0 until n) {
            val angle = startAngle + i * angleStep
            drawLine(
                color = navyElevated,
                start = Offset(cx, cy),
                end = Offset(cx + radius * cos(angle), cy + radius * sin(angle)),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Data polygon
        val dataPath = Path()
        for (i in 0 until n) {
            val v = (values.getOrElse(i) { 5 } / 10f).coerceIn(0f, 1f)
            val angle = startAngle + i * angleStep
            val x = cx + radius * v * cos(angle)
            val y = cy + radius * v * sin(angle)
            if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()
        drawPath(dataPath, neonGreen.copy(alpha = 0.25f))
        drawPath(dataPath, neonGreen, style = Stroke(width = 2.dp.toPx()))

        // Dots
        for (i in 0 until n) {
            val v = (values.getOrElse(i) { 5 } / 10f).coerceIn(0f, 1f)
            val angle = startAngle + i * angleStep
            drawCircle(neonGreen, radius = 4.dp.toPx(),
                center = Offset(cx + radius * v * cos(angle), cy + radius * v * sin(angle)))
        }

        // Labels
        for (i in 0 until n) {
            val angle = startAngle + i * angleStep
            val x = cx + labelRadius * cos(angle)
            val y = cy + labelRadius * sin(angle)
            drawContext.canvas.nativeCanvas.drawText(
                labels.getOrElse(i) { "" },
                x, y + 5f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(200, 148, 163, 184)
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
            )
        }
    }
}

@Composable
private fun SkillRatingHistoryCard(rating: SkillRatingEntity, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es"))
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(sdf.format(Date(rating.fecha)), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkillMini("TIR", rating.tiro)
                SkillMini("DEF", rating.defensa)
                SkillMini("BAL", rating.balonMano)
                SkillMini("VIS", rating.vision)
                SkillMini("ATL", rating.atletismo)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.6f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SkillMini(label: String, valor: Int) {
    val color = when {
        valor >= 8 -> NeonGreen
        valor >= 6 -> GoldAccent
        valor >= 4 -> OrangeBase
        else -> RedError
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextTertiary, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
        Text("$valor", color = color, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun AddSkillRatingDialog(
    jugadoraNombre: String,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int, Int, Int, Int, String) -> Unit
) {
    var tiro by remember { mutableFloatStateOf(5f) }
    var defensa by remember { mutableFloatStateOf(5f) }
    var balonMano by remember { mutableFloatStateOf(5f) }
    var vision by remember { mutableFloatStateOf(5f) }
    var atletismo by remember { mutableFloatStateOf(5f) }
    var mentalidad by remember { mutableFloatStateOf(5f) }
    var liderazgo by remember { mutableFloatStateOf(5f) }
    var notas by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        title = {
            Column {
                Text("NUEVA VALORACIÓN", color = NeonGreen, fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Text(jugadoraNombre, color = TextSecondary, fontSize = 13.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkillSlider("Tiro", tiro, { tiro = it })
                SkillSlider("Defensa", defensa, { defensa = it })
                SkillSlider("Manejo de balón", balonMano, { balonMano = it })
                SkillSlider("Visión de juego", vision, { vision = it })
                SkillSlider("Atletismo", atletismo, { atletismo = it })
                SkillSlider("Mentalidad", mentalidad, { mentalidad = it })
                SkillSlider("Liderazgo", liderazgo, { liderazgo = it })
                OutlinedTextField(
                    value = notas, onValueChange = { notas = it },
                    label = { Text("Notas", color = TextTertiary, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen, unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = NeonGreen, cursorColor = NeonGreen,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(tiro.toInt(), defensa.toInt(), balonMano.toInt(), vision.toInt(),
                    atletismo.toInt(), mentalidad.toInt(), liderazgo.toInt(), notas)
            }) { Text("GUARDAR", color = NeonGreen, fontWeight = FontWeight.ExtraBold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = TextSecondary) } }
    )
}

@Composable
private fun SkillSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    val color = when {
        value >= 8f -> NeonGreen
        value >= 6f -> GoldAccent
        value >= 4f -> OrangeBase
        else -> RedError
    }
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(value.toInt().toString(), color = color, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }
        Slider(
            value = value, onValueChange = onValueChange,
            valueRange = 1f..10f, steps = 8,
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color, inactiveTrackColor = NavyElevated),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
