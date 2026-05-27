package com.example.tfg_aaron.ui.screens.estadisticas

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun EstadisticasScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: EstadisticasViewModel = viewModel(factory = viewModelFactory {
        initializer { EstadisticasViewModel(entrenadorId, app.jugadoraRepository, app.estadisticaRepository) }
    })
    val isTablet = LocalIsTablet.current
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var dialogJugadora by remember { mutableStateOf<JugadoraEntity?>(null) }

    Scaffold(
        bottomBar = { CoachBottomNavBar(navController) },
        containerColor = NavyDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Top Bar ───────────────────────────────────────────────────────
            PlayVisionTopBar { navController.navigate(Screen.Perfil.route) }

            // ── Header ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "RENDIMIENTO",
                            fontSize = 10.sp,
                            color = GreenSuccess,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.5.sp
                        )
                        Text(
                            "Estadísticas",
                            fontSize = 22.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.3).sp
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(GreenSuccess.copy(alpha = 0.1f))
                            .border(1.dp, GreenSuccess.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    ) {
                        Icon(Icons.Filled.BarChart, null, tint = GreenSuccess, modifier = Modifier.size(22.dp))
                    }
                }
            }
            HorizontalDivider(thickness = 1.dp, color = NavyBorder)

            // Tipo tiro filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.tiposTiro.forEach { tipo ->
                    FilterChip(
                        selected = uiState.tipoFiltro == tipo,
                        onClick = { viewModel.setTipoFiltro(tipo) },
                        label = {
                            Text(
                                tipo, fontSize = 12.sp,
                                fontWeight = if (uiState.tipoFiltro == tipo) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenSuccess.copy(alpha = 0.18f),
                            selectedLabelColor = GreenSuccess,
                            containerColor = NavyCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = uiState.tipoFiltro == tipo,
                            selectedBorderColor = GreenSuccess, borderColor = NavyBorder
                        )
                    )
                }
            }

            if (uiState.jugadorasStats.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.BarChart,
                    title = "Sin estadísticas",
                    subtitle = "Selecciona una jugadora y añade datos de tiros",
                    modifier = Modifier.padding(top = 80.dp)
                )
            } else if (isTablet) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gridItems(uiState.jugadorasStats, key = { it.jugadora.id }) { stats ->
                        JugadoraStatsCard(
                            stats = stats,
                            isSelected = uiState.jugadoraSeleccionada?.id == stats.jugadora.id,
                            onClick = { viewModel.selectJugadora(stats.jugadora) },
                            onAddStats = {
                                dialogJugadora = stats.jugadora
                                showAddDialog = true
                            }
                        )
                    }
                    item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.jugadorasStats, key = { it.jugadora.id }) { stats ->
                        JugadoraStatsCard(
                            stats = stats,
                            isSelected = uiState.jugadoraSeleccionada?.id == stats.jugadora.id,
                            onClick = { viewModel.selectJugadora(stats.jugadora) },
                            onAddStats = {
                                dialogJugadora = stats.jugadora
                                showAddDialog = true
                            }
                        )

                        // Detalle expandido
                        AnimatedVisibility(
                            visible = uiState.jugadoraSeleccionada?.id == stats.jugadora.id,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
                                colors = CardDefaults.cardColors(containerColor = NavyElevated)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    if (uiState.registrosJugadora.isEmpty()) {
                                        Text("Sin registros para este tipo de tiro",
                                            color = TextTertiary, style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(vertical = 8.dp))
                                    } else {
                                        Text("Evolución", style = MaterialTheme.typography.labelMedium,
                                            color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
                                        TirosLineChart(
                                            data = uiState.registrosJugadora.map { it.porcentaje },
                                            modifier = Modifier.fillMaxWidth().height(80.dp)
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        Text("Últimos registros", style = MaterialTheme.typography.labelMedium,
                                            color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
                                        uiState.registrosJugadora.take(5).forEach { reg ->
                                            val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(sdf.format(Date(reg.fecha)),
                                                    color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                                                Text("${reg.tirosAcertados}/${reg.tirosIntentados}",
                                                    color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                                Text("${reg.porcentaje.roundToInt()}%",
                                                    color = if (reg.porcentaje >= 70) GreenSuccess else if (reg.porcentaje >= 50) GoldAccent else RedError,
                                                    fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                                IconButton(onClick = { viewModel.deleteEstadistica(reg) },
                                                    modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Filled.Delete, null, tint = RedError, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog && dialogJugadora != null) {
        AddStatsDialog(
            jugadora = dialogJugadora!!,
            tipoTiro = uiState.tipoFiltro,
            error = uiState.error,
            onDismiss = { showAddDialog = false; viewModel.clearMessages() },
            onConfirm = { intentados, acertados, notas ->
                viewModel.addEstadistica(dialogJugadora!!.id, intentados, acertados, notas)
                if (uiState.error == null) showAddDialog = false
            }
        )
    }
}

@Composable
fun JugadoraStatsCard(
    stats: JugadoraStats,
    isSelected: Boolean,
    onClick: () -> Unit,
    onAddStats: () -> Unit
) {
    val color = when {
        stats.promedio >= 75 -> GreenSuccess
        stats.promedio >= 50 -> GoldAccent
        stats.promedio > 0 -> GoldAccent
        else -> TextTertiary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = if (isSelected) RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp) else RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) NavyElevated else NavyCard)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
            ) {
                if (stats.promedio > 0) {
                    CircularProgressBar(
                        progress = stats.promedio / 100f,
                        color = color,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "${stats.promedio.roundToInt()}%",
                        color = color,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                } else {
                    Text("-", color = TextTertiary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${stats.jugadora.nombre} ${stats.jugadora.apellidos}",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${stats.acertados}/${stats.intentados} tiros",
                        color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    BadgeChip(stats.jugadora.posicion, posicionColor(stats.jugadora.posicion))
                }
            }

            IconButton(
                onClick = onAddStats,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GoldAccent.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Filled.Add, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun CircularProgressBar(progress: Float, color: Color, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val strokeWidth = 4.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        drawArc(color = color.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = 360f, useCenter = false,
            style = Stroke(strokeWidth, cap = StrokeCap.Round), topLeft = topLeft, size = arcSize)
        drawArc(color = color, startAngle = -90f, sweepAngle = 360f * animatedProgress, useCenter = false,
            style = Stroke(strokeWidth, cap = StrokeCap.Round), topLeft = topLeft, size = arcSize)
    }
}

@Composable
fun TirosLineChart(data: List<Float>, modifier: Modifier = Modifier) {
    if (data.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Necesitas al menos 2 registros", color = TextTertiary, fontSize = 11.sp)
        }
        return
    }
    val maxVal = 100f
    val animated = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animated.snapTo(0f)
        animated.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
    }
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stepX = w / (data.size - 1)
        val progress = animated.value

        // Reference lines at 33%, 50%, 70%
        listOf(33f, 50f, 70f).forEach { pct ->
            val y = h - (pct / maxVal) * h
            drawLine(
                color = NavyBorder,
                start = Offset(0f, y), end = Offset(w, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
        }

        // Fill path
        val fillPath = Path()
        fillPath.moveTo(0f, h)
        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = h - ((value / maxVal) * h * progress).coerceAtMost(h)
            fillPath.lineTo(x, y)
        }
        fillPath.lineTo((data.size - 1) * stepX, h)
        fillPath.close()
        drawPath(fillPath, color = GoldAccent.copy(alpha = 0.15f))

        // Line path
        val linePath = Path()
        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = h - ((value / maxVal) * h * progress).coerceAtMost(h)
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        drawPath(linePath, color = GoldAccent, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

        // Dots
        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = h - ((value / maxVal) * h * progress).coerceAtMost(h)
            drawCircle(GoldAccent, radius = 4.dp.toPx(), center = Offset(x, y))
            drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
fun AddStatsDialog(
    jugadora: JugadoraEntity,
    tipoTiro: String,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, String) -> Unit
) {
    var intentados by remember { mutableStateOf("") }
    var acertados by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        title = {
            Text("Añadir registro - $tipoTiro", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "${jugadora.nombre} ${jugadora.apellidos}",
                    color = GoldAccent, style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent, unfocusedBorderColor = NavyBorder,
                    focusedLabelColor = GoldAccent, unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    cursorColor = GoldAccent, focusedContainerColor = NavyElevated, unfocusedContainerColor = NavyElevated
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(intentados, { intentados = it }, label = { Text("Intentados") },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = fieldColors, singleLine = true)
                    OutlinedTextField(acertados, { acertados = it }, label = { Text("Acertados") },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = fieldColors, singleLine = true)
                }
                Spacer(Modifier.height(8.dp))
                val intentadosInt = intentados.toIntOrNull() ?: 0
                val acertadosInt = acertados.toIntOrNull() ?: 0
                if (intentadosInt > 0) {
                    val pct = (acertadosInt.toFloat() / intentadosInt * 100).roundToInt()
                    Text("Porcentaje: $pct%", color = GoldAccent, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
                }
                OutlinedTextField(notas, { notas = it }, label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(10.dp), colors = fieldColors, singleLine = true)
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = RedError, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(intentados.toIntOrNull() ?: 0, acertados.toIntOrNull() ?: 0, notas) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) { Text("Guardar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, border = BorderStroke(1.dp, NavyBorder)) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}
