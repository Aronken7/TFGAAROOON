@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.tfg_aaron.ui.screens.testfisico

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.TestFisicoEntity
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

val TIPOS_TEST = listOf("YO_YO", "SPRINT_20M", "VERTICAL_JUMP", "T_TEST", "CMJ", "VO2MAX", "CUSTOM")
val TIPOS_TEST_LABELS = mapOf(
    "YO_YO" to "Yo-Yo Test", "SPRINT_20M" to "Sprint 20m",
    "VERTICAL_JUMP" to "Salto Vertical", "T_TEST" to "T-Test Agilidad",
    "CMJ" to "CMJ (Counter Movement)", "VO2MAX" to "VO2 Max", "CUSTOM" to "Personalizado"
)
val TIPOS_TEST_UNIDADES = mapOf(
    "YO_YO" to "nivel", "SPRINT_20M" to "s", "VERTICAL_JUMP" to "cm",
    "T_TEST" to "s", "CMJ" to "cm", "VO2MAX" to "ml/kg/min", "CUSTOM" to ""
)

// ── Progression charts section ────────────────────────────────────────────────

@Composable
private fun ProgresionSection(tests: List<TestFisicoEntity>) {
    // Group tests by tipo and keep only tipos with 2+ records
    val gruposPorTipo = tests
        .groupBy { it.tipo }
        .filter { it.value.size >= 2 }
        .map { (tipo, registros) ->
            tipo to registros.sortedBy { it.fecha }
        }

    if (gruposPorTipo.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "PROGRESIÓN",
            color = NeonGreen,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(gruposPorTipo) { (tipo, registros) ->
                ProgresionCard(tipo = tipo, registros = registros)
            }
        }
    }
}

@Composable
private fun ProgresionCard(tipo: String, registros: List<TestFisicoEntity>) {
    val label = TIPOS_TEST_LABELS[tipo] ?: registros.firstOrNull()?.nombreCustom?.ifEmpty { tipo } ?: tipo
    val unidad = registros.firstOrNull()?.unidad ?: TIPOS_TEST_UNIDADES[tipo] ?: ""

    val xValues = registros.indices.toList()
    val yValues = registros.map { it.valor.toFloat() }

    val modelProducer = remember(tipo, registros.size) { CartesianChartModelProducer() }

    LaunchedEffect(tipo, registros.size) {
        modelProducer.runTransaction {
            lineSeries {
                series(x = xValues, y = yValues)
            }
        }
    }

    Column(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = NeonGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "${registros.size} registros",
                color = TextTertiary,
                fontSize = 10.sp
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("MIN", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (unidad == "s") "%.2f".format(yValues.min()) else "%.1f".format(yValues.min()),
                    color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("MÁX", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (unidad == "s") "%.2f".format(yValues.max()) else "%.1f".format(yValues.max()),
                    color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold
                )
            }
        }
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = remember { LineCartesianLayer.LineFill.single(fill(NeonGreen)) },
                            areaFill = remember {
                                LineCartesianLayer.AreaFill.single(fill(NeonGreen.copy(alpha = 0.12f)))
                            }
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom()
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        )
    }
}

@Composable
fun TestFisicoScreen(
    navController: NavController,
    entrenadorId: Int,
    jugadoraId: Int = -1
) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: TestFisicoViewModel = viewModel(factory = viewModelFactory {
        initializer { TestFisicoViewModel(entrenadorId, jugadoraId, app.testFisicoRepository, app.jugadoraRepository) }
    })
    val isTablet = LocalIsTablet.current
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTest by remember { mutableStateOf<TestFisicoEntity?>(null) }

    val filteredTests = if (state.tipoFiltro == "TODOS") state.tests
    else state.tests.filter { it.tipo == state.tipoFiltro }

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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(NavyCard)
                    .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("TESTS FÍSICOS", color = NeonGreen, fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                        Text("Evaluación de rendimiento", color = TextPrimary,
                            fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // Filter chips
            ScrollableTabRow(
                selectedTabIndex = (listOf("TODOS") + TIPOS_TEST).indexOf(state.tipoFiltro),
                containerColor = NavyCard,
                contentColor = NeonGreen,
                edgePadding = 12.dp
            ) {
                (listOf("TODOS") + TIPOS_TEST).forEachIndexed { idx, tipo ->
                    Tab(
                        selected = state.tipoFiltro == tipo,
                        onClick = { viewModel.setTipoFiltro(tipo) },
                        text = {
                            Text(
                                if (tipo == "TODOS") "TODOS" else (TIPOS_TEST_LABELS[tipo] ?: tipo),
                                fontSize = 11.sp, fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            // Compute tests that belong to the currently displayed jugadora for progression
            val jugadoraTests = if (jugadoraId > 0) {
                state.tests.filter { it.idJugadora == jugadoraId }
            } else {
                state.tests
            }

            if (filteredTests.isEmpty()) {
                // Still show progression if there are tests (even if filtered away)
                if (jugadoraTests.isNotEmpty()) {
                    ProgresionSection(tests = jugadoraTests)
                }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Filled.FitnessCenter,
                        title = "Sin tests registrados",
                        subtitle = "Pulsa + para añadir el primer test físico"
                    )
                }
            } else if (isTablet) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Progression section — full width span
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ProgresionSection(tests = jugadoraTests)
                    }
                    gridItems(filteredTests, key = { it.id }) { test ->
                        TestCard(
                            test = test,
                            jugadoraNombre = state.jugadoras.find { it.id == test.idJugadora }?.let { "${it.nombre} · #${it.numero}" } ?: "",
                            onDelete = { viewModel.deleteTest(test.id) },
                            onEdit = { editingTest = test }
                        )
                    }
                    item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Progression section
                    item {
                        ProgresionSection(tests = jugadoraTests)
                    }
                    items(filteredTests, key = { it.id }) { test ->
                        TestCard(
                            test = test,
                            jugadoraNombre = state.jugadoras.find { it.id == test.idJugadora }?.let { "${it.nombre} · #${it.numero}" } ?: "",
                            onDelete = { viewModel.deleteTest(test.id) },
                            onEdit = { editingTest = test }
                        )
                    }
                    item { Spacer(Modifier.height(20.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTestSheet(
            jugadoraId = jugadoraId,
            jugadoras = state.jugadoras,
            onDismiss = { showAddDialog = false },
            onSave = { jId, tipo, custom, valor, unidad, notas ->
                viewModel.saveTest(jId, tipo, custom, valor, unidad, notas)
                showAddDialog = false
            }
        )
    }

    if (editingTest != null) {
        AddTestSheet(
            jugadoraId = editingTest!!.idJugadora,
            jugadoras = state.jugadoras,
            onDismiss = { editingTest = null },
            onSave = { jId, tipo, custom, valor, unidad, notas ->
                viewModel.updateTest(editingTest!!.copy(
                    idJugadora = jId, tipo = tipo, nombreCustom = custom,
                    valor = valor, unidad = unidad, notas = notas
                ))
                editingTest = null
            }
        )
    }
}

@Composable
private fun TestCard(test: TestFisicoEntity, jugadoraNombre: String, onDelete: () -> Unit, onEdit: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es"))
    val label = TIPOS_TEST_LABELS[test.tipo] ?: test.nombreCustom.ifEmpty { test.tipo }
    val accentColor = when (test.tipo) {
        "YO_YO" -> NeonGreen
        "SPRINT_20M" -> OrangeBase
        "VERTICAL_JUMP" -> TealAccent
        "T_TEST" -> GoldAccent
        "CMJ" -> Color(0xFF8B5CF6)
        "VO2MAX" -> GreenSuccess
        else -> TextSecondary
    }
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.FitnessCenter, null, tint = accentColor, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(label, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(sdf.format(Date(test.fecha)), color = TextTertiary, fontSize = 11.sp)
                if (jugadoraNombre.isNotEmpty()) {
                    Text(jugadoraNombre, color = accentColor.copy(0.85f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                if (test.notas.isNotEmpty()) {
                    Text(test.notas, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (test.unidad == "s") "%.2f".format(test.valor) else "%.1f".format(test.valor),
                    color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp
                )
                Text(test.unidad, color = TextTertiary, fontSize = 11.sp)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, null, tint = TealAccent.copy(0.7f), modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.7f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTestSheet(
    jugadoraId: Int,
    jugadoras: List<JugadoraEntity>,
    onDismiss: () -> Unit,
    onSave: (Int, String, String, Double, String, String) -> Unit
) {
    var tipoSeleccionado by remember { mutableStateOf("SPRINT_20M") }
    var valorText by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf(TIPOS_TEST_UNIDADES["SPRINT_20M"] ?: "") }
    var nombreCustom by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var selectedJugadoraId by remember { mutableIntStateOf(if (jugadoraId > 0) jugadoraId else jugadoras.firstOrNull()?.id ?: -1) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val testIcons = mapOf(
        "YO_YO" to Icons.Filled.DirectionsRun,
        "SPRINT_20M" to Icons.Filled.Speed,
        "VERTICAL_JUMP" to Icons.Filled.KeyboardArrowUp,
        "T_TEST" to Icons.Filled.Autorenew,
        "CMJ" to Icons.Filled.FitnessCenter,
        "VO2MAX" to Icons.Filled.Favorite,
        "CUSTOM" to Icons.Filled.Edit
    )
    val testColors = mapOf(
        "YO_YO" to NeonGreen, "SPRINT_20M" to OrangeBase, "VERTICAL_JUMP" to TealAccent,
        "T_TEST" to GoldAccent, "CMJ" to PurpleAccent, "VO2MAX" to GreenSuccess, "CUSTOM" to TextSecondary
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NavyDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(modifier = Modifier.padding(vertical = 10.dp).size(40.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(NavyBorder))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("REGISTRAR TEST", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                    Text("Test Físico", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
                TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) }
            }
            HorizontalDivider(color = NavyBorder)

            // ── Jugadora selector (only when not in single-player context) ──
            if (jugadoraId <= 0 && jugadoras.isNotEmpty()) {
                Text("JUGADORA", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(jugadoras) { j ->
                        val isSelected = selectedJugadoraId == j.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NeonGreen.copy(0.18f) else NavyCard)
                                .border(1.dp, if (isSelected) NeonGreen else NavyBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedJugadoraId = j.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("#${j.numero}", color = if (isSelected) NeonGreen else TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                Text(j.nombre.split(" ").first(), color = if (isSelected) NeonGreen else TextSecondary, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal)
                            }
                        }
                    }
                }
                HorizontalDivider(color = NavyBorder)
            }

            // ── Test type selector grid ──
            Text("TIPO DE TEST", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            val rows = TIPOS_TEST.chunked(3)
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { tipo ->
                        val isSelected = tipoSeleccionado == tipo
                        val accent = testColors[tipo] ?: TextSecondary
                        val icon = testIcons[tipo] ?: Icons.Filled.FitnessCenter
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) accent.copy(0.15f) else NavyCard)
                                .border(1.5.dp, if (isSelected) accent else NavyBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    tipoSeleccionado = tipo
                                    unidad = TIPOS_TEST_UNIDADES[tipo] ?: ""
                                }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(icon, null, tint = if (isSelected) accent else TextTertiary, modifier = Modifier.size(22.dp))
                            Text(
                                TIPOS_TEST_LABELS[tipo] ?: tipo,
                                color = if (isSelected) accent else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 2,
                                lineHeight = 13.sp
                            )
                        }
                    }
                    // Fill empty slots in last row
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            // ── Custom test name ──
            if (tipoSeleccionado == "CUSTOM") {
                OutlinedTextField(
                    value = nombreCustom, onValueChange = { nombreCustom = it },
                    label = { Text("Nombre del test personalizado", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = testFieldColors(NeonGreen),
                    singleLine = true
                )
            }

            // ── Value + unit ──
            val selectedColor = testColors[tipoSeleccionado] ?: NeonGreen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = valorText, onValueChange = { valorText = it },
                    label = { Text("Resultado / Valor", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp),
                    colors = testFieldColors(selectedColor),
                    singleLine = true,
                    placeholder = { Text("0.00", color = TextTertiary, fontSize = 12.sp) }
                )
                OutlinedTextField(
                    value = unidad, onValueChange = { unidad = it },
                    label = { Text("Unidad", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = testFieldColors(selectedColor),
                    singleLine = true
                )
            }

            // ── Notes ──
            OutlinedTextField(
                value = notas, onValueChange = { notas = it },
                label = { Text("Notas (opcional)", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = testFieldColors(NeonGreen),
                placeholder = { Text("Condiciones, observaciones...", color = TextTertiary, fontSize = 12.sp) }
            )

            // ── Save button ──
            Button(
                onClick = {
                    val v = valorText.toDoubleOrNull() ?: return@Button
                    onSave(selectedJugadoraId, tipoSeleccionado, nombreCustom, v, unidad, notas)
                },
                enabled = valorText.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = NavyDark,
                    disabledContainerColor = NavyElevated
                )
            ) {
                Icon(Icons.Filled.FitnessCenter, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("GUARDAR TEST", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun testFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = accent,
    unfocusedBorderColor = NavyBorder,
    focusedLabelColor = accent,
    unfocusedLabelColor = TextTertiary,
    cursorColor = accent,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    unfocusedContainerColor = NavySurface,
    focusedContainerColor = NavyElevated
)
