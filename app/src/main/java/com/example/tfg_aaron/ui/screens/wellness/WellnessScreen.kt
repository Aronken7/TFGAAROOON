@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.tfg_aaron.ui.screens.wellness

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.WellnessDiarioEntity
import com.example.tfg_aaron.ui.components.EmptyState
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import java.text.SimpleDateFormat
import java.util.*

private val NOMBRES_MES = listOf(
    "Enero","Febrero","Marzo","Abril","Mayo","Junio",
    "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
)

@Composable
fun WellnessScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: WellnessViewModel = viewModel(factory = viewModelFactory {
        initializer { WellnessViewModel(entrenadorId, app.wellnessDiarioRepository, app.jugadoraRepository) }
    })
    val state by viewModel.state.collectAsState()

    // Reactive derivation: recomputes when state changes
    val registrosMes = remember(state) { viewModel.registrosDelMes() }
    val promediosMes = remember(state) { viewModel.promediosDelMes() }
    val meses = remember(state) { viewModel.mesesConDatos() }
    val isCurrentMonth = remember(state) {
        val now = Calendar.getInstance()
        state.selectedYear == now.get(Calendar.YEAR) && state.selectedMonth == now.get(Calendar.MONTH)
    }

    val isTablet = LocalIsTablet.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRegistro by remember { mutableStateOf<WellnessDiarioEntity?>(null) }

    Scaffold(
        containerColor = NavyDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NeonGreen, contentColor = NavyDark,
                shape = RoundedCornerShape(14.dp)
            ) { Icon(Icons.Filled.Add, null) }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isTablet) 2 else 1),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalArrangement = if (isTablet) Arrangement.spacedBy(10.dp) else Arrangement.Start
        ) {
            // ── Header ────────────────────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(NavyCard)
                        .padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("WELLNESS & RPE", color = NeonGreen, fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                            Text("Control de bienestar del equipo", color = TextPrimary,
                                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // ── Month navigator ───────────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(NavyCard)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.prevMonth() }) {
                        Icon(Icons.Filled.ChevronLeft, null, tint = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${NOMBRES_MES[state.selectedMonth].uppercase()} ${state.selectedYear}",
                            color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                        )
                        if (isCurrentMonth) {
                            Text("MES ACTUAL", color = NeonGreen, fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }
                    }
                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        enabled = !isCurrentMonth
                    ) {
                        Icon(Icons.Filled.ChevronRight, null,
                            tint = if (isCurrentMonth) NavyBorder else TextPrimary)
                    }
                }
                HorizontalDivider(color = NavyBorder)
            }

            // ── Month folder pills ────────────────────────────────────────────
            if (meses.size > 1) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(12.dp))
                    Text("MESES CON DATOS", color = TextTertiary, fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(6.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(meses) { (year, month, count) ->
                            val isSelected = year == state.selectedYear && month == state.selectedMonth
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) NeonGreen.copy(0.15f) else NavyCard)
                                    .border(1.dp, if (isSelected) NeonGreen else NavyBorder, RoundedCornerShape(10.dp))
                                    .clickable { viewModel.selectMonth(year, month) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(NOMBRES_MES[month].take(3).uppercase(),
                                        color = if (isSelected) NeonGreen else TextSecondary,
                                        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("$count reg", color = TextTertiary, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ── Promedios del mes ─────────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(if (meses.size <= 1) 16.dp else 0.dp))
                Text(
                    "PROMEDIOS — ${NOMBRES_MES[state.selectedMonth].uppercase()}",
                    color = TextTertiary, fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyCard)
                        .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (promediosMes.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text("Sin datos para ${NOMBRES_MES[state.selectedMonth]}",
                                color = TextTertiary, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        WellnessBar("FATIGA", promediosMes["fatiga"] ?: 5.0, RedError, invertido = true)
                        WellnessBar("SUEÑO", promediosMes["sueno"] ?: 5.0, TealAccent)
                        WellnessBar("MOTIVACIÓN", promediosMes["motivacion"] ?: 5.0, NeonGreen)
                        WellnessBar("DOLOR", promediosMes["dolor"] ?: 1.0, Color(0xFFFF9500), invertido = true)
                        WellnessBar("RPE SESIÓN", promediosMes["rpe"] ?: 5.0, OrangeBase)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Tendencia charts ──────────────────────────────────────────────
            if (registrosMes.size >= 3) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    TendenciaSection(registros = registrosMes)
                }
            }

            // ── Records list for selected month ───────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "REGISTROS — ${NOMBRES_MES[state.selectedMonth].uppercase()} (${registrosMes.size})",
                    color = TextTertiary, fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (registrosMes.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        EmptyState(Icons.Filled.MonitorHeart, "Sin registros",
                            "Pulsa + para añadir bienestar en ${NOMBRES_MES[state.selectedMonth]}")
                    }
                }
            } else {
                gridItems(registrosMes, key = { it.id }) { reg ->
                    val jugadora = state.jugadoras.find { it.id == reg.idJugadora }
                    WellnessCard(
                        reg = reg,
                        nombreJugadora = jugadora?.nombre ?: "Jugadora ${reg.idJugadora}",
                        onDelete = { viewModel.deleteRegistro(reg.id) },
                        onEdit = { editingRegistro = reg }
                    )
                }
            }
        }
    }

    // ── Add dialog ────────────────────────────────────────────────────────────
    if (showAddDialog && state.jugadoras.isNotEmpty()) {
        WellnessBottomSheet(
            jugadoras = state.jugadoras,
            initialFecha = System.currentTimeMillis(),
            onDismiss = { showAddDialog = false },
            onSave = { jId, fatiga, sueno, motiv, dolor, rpe, notas, fecha ->
                viewModel.saveWellness(jId, fatiga, sueno, motiv, dolor, rpe, notas, fecha)
                showAddDialog = false
            }
        )
    }

    // ── Edit dialog ───────────────────────────────────────────────────────────
    editingRegistro?.let { reg ->
        WellnessBottomSheet(
            jugadoras = state.jugadoras,
            initialJugadoraId = reg.idJugadora,
            initialFecha = reg.fecha,
            initialFatiga = reg.fatiga.toFloat(),
            initialSueno = reg.sueno.toFloat(),
            initialMotivacion = reg.motivacion.toFloat(),
            initialDolor = reg.dolor.toFloat(),
            initialRpe = reg.rpe.toFloat(),
            initialNotas = reg.notas,
            isEditing = true,
            onDismiss = { editingRegistro = null },
            onSave = { jId, fatiga, sueno, motiv, dolor, rpe, notas, fecha ->
                viewModel.updateWellness(reg.copy(
                    idJugadora = jId, fatiga = fatiga, sueno = sueno,
                    motivacion = motiv, dolor = dolor, rpe = rpe, notas = notas, fecha = fecha
                ))
                editingRegistro = null
            }
        )
    }
}

// ── Tendencia charts section ──────────────────────────────────────────────────

@Composable
private fun TendenciaSection(registros: List<WellnessDiarioEntity>) {
    if (registros.size < 3) return

    // Sort records by date
    val sorted = registros.sortedBy { it.fecha }
    val xValues = sorted.indices.toList()
    val rpeValues = sorted.map { it.rpe.toFloat() }
    val fatigaValues = sorted.map { it.fatiga.toFloat() }
    val suenoValues = sorted.map { it.sueno.toFloat() }

    val rpeProducer = remember(registros.size) { CartesianChartModelProducer() }
    val fatigaProducer = remember(registros.size) { CartesianChartModelProducer() }
    val suenoProducer = remember(registros.size) { CartesianChartModelProducer() }

    LaunchedEffect(registros.size) {
        rpeProducer.runTransaction {
            lineSeries { series(x = xValues, y = rpeValues) }
        }
        fatigaProducer.runTransaction {
            lineSeries { series(x = xValues, y = fatigaValues) }
        }
        suenoProducer.runTransaction {
            lineSeries { series(x = xValues, y = suenoValues) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "TENDENCIA",
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = BorderStroke(1.dp, NavyBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // RPE chart
                TendenciaChart(
                    title = "RPE",
                    producer = rpeProducer,
                    color = OrangeBase
                )
                HorizontalDivider(color = NavyBorder.copy(alpha = 0.5f))
                // Fatiga chart
                TendenciaChart(
                    title = "Fatiga",
                    producer = fatigaProducer,
                    color = RedError
                )
                HorizontalDivider(color = NavyBorder.copy(alpha = 0.5f))
                // Sueño chart
                TendenciaChart(
                    title = "Sueño",
                    producer = suenoProducer,
                    color = TealAccent
                )
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun TendenciaChart(
    title: String,
    producer: CartesianChartModelProducer,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            title,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = remember { LineCartesianLayer.LineFill.single(fill(color)) },
                            areaFill = remember {
                                LineCartesianLayer.AreaFill.single(fill(color.copy(alpha = 0.10f)))
                            }
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom()
            ),
            modelProducer = producer,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
    }
}

// ── WellnessBar ───────────────────────────────────────────────────────────────

@Composable
private fun WellnessBar(label: String, valor: Double, color: Color, invertido: Boolean = false) {
    val progress = (valor / 10.0).coerceIn(0.0, 1.0).toFloat()
    val displayColor = if (invertido) {
        val g = (1f - progress).coerceIn(0f, 1f)
        Color(1f - g * 0.5f, g, 0f, 1f)
    } else color
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.width(90.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = displayColor,
            trackColor = NavyElevated
        )
        Text("%.1f".format(valor), color = displayColor, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.width(32.dp))
    }
}

// ── WellnessCard ──────────────────────────────────────────────────────────────

@Composable
private fun WellnessCard(
    reg: WellnessDiarioEntity,
    nombreJugadora: String,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("es"))
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
            Text(nombreJugadora, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(sdf.format(Date(reg.fecha)), color = TextTertiary, fontSize = 10.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WellnessMini("FAT", reg.fatiga, RedError)
                WellnessMini("SUE", reg.sueno, TealAccent)
                WellnessMini("MOT", reg.motivacion, NeonGreen)
                WellnessMini("DOL", reg.dolor, Color(0xFFFF9500))
                WellnessMini("RPE", reg.rpe, OrangeBase)
            }
            if (reg.notas.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(reg.notas, color = TextTertiary, fontSize = 10.sp, maxLines = 2)
            }
        }
        Column {
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Edit, null, tint = TealAccent.copy(0.7f), modifier = Modifier.size(15.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.6f), modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun WellnessMini(label: String, valor: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextTertiary, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
        Text("$valor", color = color, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}

// ── Add / Edit BottomSheet ────────────────────────────────────────────────────

@Composable
private fun WellnessBottomSheet(
    jugadoras: List<JugadoraEntity>,
    initialJugadoraId: Int = jugadoras.firstOrNull()?.id ?: -1,
    initialFecha: Long = System.currentTimeMillis(),
    initialFatiga: Float = 5f,
    initialSueno: Float = 7f,
    initialMotivacion: Float = 7f,
    initialDolor: Float = 1f,
    initialRpe: Float = 6f,
    initialNotas: String = "",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int, Int, Int, String, Long) -> Unit
) {
    var selectedJugadora by remember {
        mutableStateOf(jugadoras.find { it.id == initialJugadoraId } ?: jugadoras.first())
    }
    var fatiga by remember { mutableFloatStateOf(initialFatiga) }
    var sueno by remember { mutableFloatStateOf(initialSueno) }
    var motivacion by remember { mutableFloatStateOf(initialMotivacion) }
    var dolor by remember { mutableFloatStateOf(initialDolor) }
    var rpe by remember { mutableFloatStateOf(initialRpe) }
    var notas by remember { mutableStateOf(initialNotas) }
    var selectedFecha by remember { mutableLongStateOf(initialFecha) }
    var showDatePicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es"))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NavyDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(modifier = Modifier.padding(vertical = 10.dp).size(40.dp, 4.dp)
                .clip(RoundedCornerShape(2.dp)).background(NavyBorder))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp).padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(NeonGreen.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.MonitorHeart, null, tint = NeonGreen, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(10.dp))
                    Text(if (isEditing) "Editar Wellness" else "Wellness & RPE",
                        color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
                TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) }
            }
            HorizontalDivider(color = NavyBorder)

            // ── Date picker row ───────────────────────────────────────────────
            Text("FECHA", color = TextTertiary, fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = BorderStroke(1.dp, NavyBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.CalendarToday, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(sdf.format(Date(selectedFecha)), fontSize = 14.sp)
            }

            // ── Jugadora picker ───────────────────────────────────────────────
            Text("JUGADORA", color = TextTertiary, fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(jugadoras) { j ->
                    val isSelected = selectedJugadora.id == j.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) NeonGreen.copy(0.15f) else NavyCard)
                            .border(1.dp, if (isSelected) NeonGreen else NavyBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedJugadora = j }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("#${j.numero}", color = if (isSelected) NeonGreen else TextTertiary,
                                fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            Text(j.nombre.split(" ").first(),
                                color = if (isSelected) NeonGreen else TextSecondary,
                                fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal)
                        }
                    }
                }
            }

            HorizontalDivider(color = NavyBorder)

            // ── Sliders ───────────────────────────────────────────────────────
            WellnessSlider("Fatiga (1=bajo, 10=alto)", fatiga, { fatiga = it }, RedError)
            WellnessSlider("Sueño (1=malo, 10=excelente)", sueno, { sueno = it }, TealAccent)
            WellnessSlider("Motivación (1=baja, 10=alta)", motivacion, { motivacion = it }, NeonGreen)
            WellnessSlider("Dolor muscular (1=ninguno, 10=intenso)", dolor, { dolor = it }, Color(0xFFFF9500))
            WellnessSlider("RPE sesión (1=muy fácil, 10=máximo)", rpe, { rpe = it }, OrangeBase)

            // ── Notes ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value = notas, onValueChange = { notas = it },
                label = { Text("Notas opcionales", color = TextTertiary, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = wellnessTextFieldColors(),
                maxLines = 3
            )

            // ── Save button ───────────────────────────────────────────────────
            Button(
                onClick = {
                    onSave(selectedJugadora.id, fatiga.toInt(), sueno.toInt(),
                        motivacion.toInt(), dolor.toInt(), rpe.toInt(), notas, selectedFecha)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = NavyDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) "ACTUALIZAR" else "GUARDAR",
                    fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }

    // ── Date picker dialog ────────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedFecha)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedFecha = it }
                    showDatePicker = false
                }) { Text("OK", color = NeonGreen, fontWeight = FontWeight.ExtraBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = TextSecondary) }
            },
            colors = DatePickerDefaults.colors(
                containerColor = NavyCard,
                titleContentColor = TextPrimary,
                headlineContentColor = TextPrimary,
                weekdayContentColor = TextTertiary,
                subheadContentColor = TextSecondary,
                dayContentColor = TextPrimary,
                selectedDayContentColor = NavyDark,
                selectedDayContainerColor = NeonGreen,
                todayContentColor = NeonGreen,
                todayDateBorderColor = NeonGreen
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = NavyCard,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextTertiary,
                    dayContentColor = TextPrimary,
                    selectedDayContentColor = NavyDark,
                    selectedDayContainerColor = NeonGreen,
                    todayContentColor = NeonGreen,
                    todayDateBorderColor = NeonGreen
                )
            )
        }
    }
}

// ── Slider ────────────────────────────────────────────────────────────────────

@Composable
private fun WellnessSlider(label: String, value: Float, onValueChange: (Float) -> Unit, color: Color) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text(value.toInt().toString(), color = color, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
        Slider(
            value = value, onValueChange = onValueChange,
            valueRange = 1f..10f, steps = 8,
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color, inactiveTrackColor = NavyElevated),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun wellnessTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonGreen, unfocusedBorderColor = NavyBorder,
    focusedLabelColor = NeonGreen, cursorColor = NeonGreen,
    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
)
