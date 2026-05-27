package com.example.tfg_aaron.ui.screens.comparador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.tfg_aaron.ui.theme.*

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatFloat(value: Float): String = "%.1f".format(value)

// ── Entry point ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparadorScreen(
    navController: NavController,
    entrenadorId: Int
) {
    val app = LocalContext.current.applicationContext as TFGApplication
    val viewModel: ComparadorViewModel = viewModel(
        factory = ComparadorViewModel.factory(
            jugadoraRepo = app.jugadoraRepository,
            estadisticaRepo = app.estadisticaPartidoRepository,
            wellnessRepo = app.wellnessDiarioRepository,
            skillRepo = app.skillRatingRepository,
            testFisicoRepo = app.testFisicoRepository,
            entrenadorId = entrenadorId
        )
    )

    val state by viewModel.uiState.collectAsState()

    // Initialize with 2 slots on first composition
    LaunchedEffect(Unit) {
        if (state.selectedIds.isEmpty()) {
            viewModel.selectJugadora(0, -1)
            viewModel.selectJugadora(1, -1)
        }
    }

    Scaffold(
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Comparador de Jugadoras",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NavyDark)
        ) {
            // ── Player selector area ──────────────────────────────────────
            PlayerSelectorArea(
                allJugadoras = state.allJugadoras,
                selectedIds = state.selectedIds,
                onSelect = { slot, id -> viewModel.selectJugadora(slot, id) },
                onAddSlot = { viewModel.addSlot() },
                onRemoveSlot = { viewModel.removeSlot() }
            )

            // ── Tabs ─────────────────────────────────────────────────────
            ComparadorTabRow(
                activeTab = state.activeTab,
                onTabSelected = { viewModel.setTab(it) }
            )

            // ── Content ──────────────────────────────────────────────────
            val validIds = state.selectedIds.filter { it != -1 }
            if (validIds.size < 2) {
                EmptySelectionMessage()
            } else if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonGreen)
                }
            } else {
                when (state.activeTab) {
                    ComparadorTab.ESTADISTICAS -> EstadisticasTab(state.statsData)
                    ComparadorTab.FISICO -> FisicoTab(state.fisicoData)
                    ComparadorTab.WELLNESS -> WellnessTab(state.wellnessData)
                    ComparadorTab.SKILLS -> SkillsTab(state.skillsData)
                }
            }
        }
    }
}

// ── Player Selector ──────────────────────────────────────────────────────────

@Composable
private fun PlayerSelectorArea(
    allJugadoras: List<JugadoraEntity>,
    selectedIds: List<Int>,
    onSelect: (Int, Int?) -> Unit,
    onAddSlot: () -> Unit,
    onRemoveSlot: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavySurface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            "Selecciona jugadoras",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Slots (2 or 3)
            val slots = maxOf(selectedIds.size, 2)
            for (slot in 0 until slots) {
                val currentId = selectedIds.getOrElse(slot) { -1 }
                PlayerDropdown(
                    modifier = Modifier.weight(1f),
                    jugadoras = allJugadoras,
                    selectedId = currentId,
                    excludeIds = selectedIds.filterIndexed { i, _ -> i != slot }.filter { it != -1 },
                    label = "Jugadora ${slot + 1}",
                    onSelect = { id -> onSelect(slot, id) }
                )
            }
            // Add / Remove slot button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (slots < 3) {
                    IconButton(
                        onClick = onAddSlot,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir jugadora", tint = NeonGreen, modifier = Modifier.size(20.dp))
                    }
                }
                if (slots > 2) {
                    IconButton(
                        onClick = onRemoveSlot,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Quitar jugadora", tint = RedError, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerDropdown(
    modifier: Modifier = Modifier,
    jugadoras: List<JugadoraEntity>,
    selectedId: Int,
    excludeIds: List<Int>,
    label: String,
    onSelect: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = jugadoras.find { it.id == selectedId }
    val available = jugadoras.filter { it.id !in excludeIds }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected?.let { "${it.nombre} ${it.apellidos}" } ?: "— ninguna —",
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 10.sp, color = TextTertiary) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = NavyBorder,
                focusedContainerColor = NavyCard,
                unfocusedContainerColor = NavyCard,
                focusedLabelColor = NeonGreen,
                unfocusedLabelColor = TextTertiary
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(NavyElevated)
        ) {
            DropdownMenuItem(
                text = { Text("— ninguna —", color = TextSecondary, fontSize = 13.sp) },
                onClick = { onSelect(null); expanded = false }
            )
            available.forEach { jugadora ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "#${jugadora.numero} ${jugadora.nombre} ${jugadora.apellidos}",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = { onSelect(jugadora.id); expanded = false }
                )
            }
        }
    }
}

// ── Tab Row ──────────────────────────────────────────────────────────────────

@Composable
private fun ComparadorTabRow(
    activeTab: ComparadorTab,
    onTabSelected: (ComparadorTab) -> Unit
) {
    val tabs = listOf(
        ComparadorTab.ESTADISTICAS to "Estadísticas",
        ComparadorTab.FISICO to "Físico",
        ComparadorTab.WELLNESS to "Wellness",
        ComparadorTab.SKILLS to "Skills"
    )
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == activeTab }.coerceAtLeast(0),
        containerColor = NavySurface,
        contentColor = NeonGreen,
        edgePadding = 0.dp,
        indicator = { tabPositions ->
            val index = tabs.indexOfFirst { it.first == activeTab }.coerceAtLeast(0)
            if (index < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                    color = NeonGreen
                )
            }
        }
    ) {
        tabs.forEach { (tab, label) ->
            Tab(
                selected = activeTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        label,
                        fontSize = 13.sp,
                        fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal,
                        color = if (activeTab == tab) NeonGreen else TextSecondary
                    )
                }
            )
        }
    }
}

// ── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptySelectionMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Selecciona al menos 2 jugadoras",
                color = TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "para ver la comparativa",
                color = TextTertiary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Estadísticas Tab ─────────────────────────────────────────────────────────

@Composable
private fun EstadisticasTab(data: List<StatsComparacion>) {
    if (data.isEmpty()) {
        EmptyDataMessage()
        return
    }

    val rows: List<Pair<String, List<Float>>> = listOf(
        "Partidos" to data.map { it.nPartidos.toFloat() },
        "Puntos/P" to data.map { it.puntosPorPartido },
        "Rebotes/P" to data.map { it.rebotesPorPartido },
        "Asistencias/P" to data.map { it.asistenciasPorPartido },
        "Faltas/P" to data.map { it.faltasPorPartido },
        "Minutos/P" to data.map { it.minutosPorPartido }
    )
    // Faltas: lower is better
    val lowerIsBetter = setOf("Faltas/P")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            ComparisonTable(
                headers = data.map { "${it.jugadora.nombre}\n#${it.jugadora.numero}" },
                rows = rows,
                lowerIsBetterLabels = lowerIsBetter,
                formatValue = { label, v ->
                    if (label == "Partidos") v.toInt().toString()
                    else formatFloat(v)
                }
            )
        }
    }
}

// ── Físico Tab ───────────────────────────────────────────────────────────────

@Composable
private fun FisicoTab(data: List<FisicoComparacion>) {
    if (data.isEmpty()) {
        EmptyDataMessage()
        return
    }

    // Collect all test types present across all players
    val allTipos = data.flatMap { it.tests.keys }.distinct().sorted()

    if (allTipos.isEmpty()) {
        EmptyDataMessage("No hay tests físicos registrados")
        return
    }

    // Build rows: for each tipo, collect values (NaN if player has no value)
    val rows: List<Pair<String, List<Float>>> = allTipos.map { tipo ->
        val label = tipo.replace("_", " ")
        val values = data.map { fisico ->
            fisico.tests[tipo]?.valor?.toFloat() ?: Float.NaN
        }
        label to values
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            // Headers with unidad info from first player that has the test
            val headers = data.map { "${it.jugadora.nombre}\n#${it.jugadora.numero}" }
            val unitMap = allTipos.associateWith { tipo ->
                data.mapNotNull { it.tests[tipo]?.unidad }.firstOrNull() ?: ""
            }
            ComparisonTable(
                headers = headers,
                rows = rows,
                lowerIsBetterLabels = emptySet(),
                formatValue = { label, v ->
                    val tipo = label.replace(" ", "_")
                    val unit = unitMap[tipo] ?: ""
                    if (v.isNaN()) "—"
                    else if (unit.isNotEmpty()) "${formatFloat(v)} $unit"
                    else formatFloat(v)
                }
            )
        }
    }
}

// ── Wellness Tab ──────────────────────────────────────────────────────────────

@Composable
private fun WellnessTab(data: List<WellnessComparacion>) {
    if (data.isEmpty()) {
        EmptyDataMessage()
        return
    }

    val rows: List<Pair<String, List<Float>>> = listOf(
        "RPE (esfuerzo)" to data.map { it.mediaRpe },
        "Sueño" to data.map { it.mediaSueno },
        "Fatiga" to data.map { it.mediaFatiga },
        "Motivación" to data.map { it.mediaMotivacion },
        "Dolor" to data.map { it.mediaDolor }
    )
    // For wellness: RPE, fatiga, dolor → lower is better. Sueño, motivación → higher is better.
    val lowerIsBetter = setOf("RPE (esfuerzo)", "Fatiga", "Dolor")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    data.forEach { w ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = NavyCard,
                            tonalElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "${w.jugadora.nombre}\n#${w.jugadora.numero}",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "${w.nRegistros} reg. (7d)",
                                    color = TextTertiary,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                ComparisonTable(
                    headers = data.map { "${it.jugadora.nombre}\n#${it.jugadora.numero}" },
                    rows = rows,
                    lowerIsBetterLabels = lowerIsBetter,
                    formatValue = { _, v ->
                        if (v == 0f) "—"
                        else formatFloat(v)
                    },
                    showHeaders = false
                )
            }
        }
    }
}

// ── Skills Tab ────────────────────────────────────────────────────────────────

@Composable
private fun SkillsTab(data: List<SkillComparacion>) {
    if (data.isEmpty()) {
        EmptyDataMessage()
        return
    }

    val rows: List<Pair<String, List<Float>>> = listOf(
        "Tiro" to data.map { it.rating?.tiro?.toFloat() ?: 0f },
        "Defensa" to data.map { it.rating?.defensa?.toFloat() ?: 0f },
        "Balón-Mano" to data.map { it.rating?.balonMano?.toFloat() ?: 0f },
        "Visión" to data.map { it.rating?.vision?.toFloat() ?: 0f },
        "Atletismo" to data.map { it.rating?.atletismo?.toFloat() ?: 0f },
        "Mentalidad" to data.map { it.rating?.mentalidad?.toFloat() ?: 0f },
        "Liderazgo" to data.map { it.rating?.liderazgo?.toFloat() ?: 0f }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            // Show "sin datos" badge if a player has no rating
            val noDataPlayers = data.filter { it.rating == null }.map { it.jugadora.nombre }
            if (noDataPlayers.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = NavyCard
                ) {
                    Text(
                        "Sin datos: ${noDataPlayers.joinToString(", ")}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            ComparisonTable(
                headers = data.map { "${it.jugadora.nombre}\n#${it.jugadora.numero}" },
                rows = rows,
                lowerIsBetterLabels = emptySet(),
                formatValue = { _, v ->
                    if (v == 0f) "—" else formatFloat(v)
                }
            )
        }
    }
}

// ── Comparison Table Component ────────────────────────────────────────────────

/**
 * Renders a comparison table.
 *
 * @param headers        Column headers (one per player).
 * @param rows           List of (label, values) pairs — one value per player.
 * @param lowerIsBetterLabels  Set of row labels where a lower value is better.
 * @param formatValue    Formats a Float value for display given its row label.
 * @param showHeaders    Whether to show the column headers row.
 */
@Composable
private fun ComparisonTable(
    headers: List<String>,
    rows: List<Pair<String, List<Float>>>,
    lowerIsBetterLabels: Set<String>,
    formatValue: (label: String, value: Float) -> String,
    showHeaders: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
    ) {
        // Header row
        if (showHeaders) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Label column
                Spacer(Modifier.width(110.dp))
                headers.forEach { header ->
                    Text(
                        header,
                        modifier = Modifier.weight(1f),
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HorizontalDivider(color = NavyBorder, thickness = 1.dp)
        }

        // Data rows
        rows.forEachIndexed { rowIndex, (label, values) ->
            val lowerIsBetter = label in lowerIsBetterLabels
            // Filter out NaN values for best/worst calculations
            val validValues = values.mapIndexedNotNull { i, v -> if (!v.isNaN() && v != 0f) i to v else null }
            val validFloats = validValues.map { it.second }

            val bestGlobalIdx: Int
            val worstGlobalIdx: Int
            if (validFloats.size >= 2) {
                val bestVal = if (lowerIsBetter) validFloats.min() else validFloats.max()
                val worstVal = if (lowerIsBetter) validFloats.max() else validFloats.min()
                bestGlobalIdx = if (validFloats.all { it == bestVal }) -1
                                 else validValues[validFloats.indexOf(bestVal)].first
                worstGlobalIdx = if (validFloats.all { it == worstVal }) -1
                                  else validValues[validFloats.indexOf(worstVal)].first
            } else {
                bestGlobalIdx = -1
                worstGlobalIdx = -1
            }

            val bgColor = if (rowIndex % 2 == 0) NavyCard else NavySurface

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Row label
                Text(
                    label,
                    modifier = Modifier.width(110.dp),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // Value cells
                values.forEachIndexed { colIdx, v ->
                    val displayText = formatValue(label, v)
                    val isBest = colIdx == bestGlobalIdx
                    val isWorst = colIdx == worstGlobalIdx

                    val textColor = when {
                        isBest -> GreenSuccess
                        isWorst -> RedError
                        else -> TextPrimary
                    }
                    val cellBg = when {
                        isBest -> GreenSuccess.copy(alpha = 0.10f)
                        isWorst -> RedError.copy(alpha = 0.10f)
                        else -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(cellBg)
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            displayText,
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = if (isBest || isWorst) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (rowIndex < rows.lastIndex) {
                HorizontalDivider(color = NavyBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}

// ── Generic empty data ────────────────────────────────────────────────────────

@Composable
private fun EmptyDataMessage(message: String = "No hay datos disponibles") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}
