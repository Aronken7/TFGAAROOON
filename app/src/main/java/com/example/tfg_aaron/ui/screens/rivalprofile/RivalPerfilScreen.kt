package com.example.tfg_aaron.ui.screens.rivalprofile

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.RivalPerfilEntity
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RivalPerfilScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: RivalPerfilViewModel = viewModel(factory = viewModelFactory {
        initializer { RivalPerfilViewModel(entrenadorId, app.rivalPerfilRepository) }
    })
    val isTablet = LocalIsTablet.current
    val state by viewModel.state.collectAsState()
    var editingRival by remember { mutableStateOf<RivalPerfilEntity?>(null) }
    var showEdit by remember { mutableStateOf(false) }
    var viewingRival by remember { mutableStateOf<RivalPerfilEntity?>(null) }

    Scaffold(
        containerColor = NavyDark
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ── Header ──
            Box(
                modifier = Modifier.fillMaxWidth().background(NavySurface)
                    .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "SCOUTING",
                            color = RedError, fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                        )
                        Text(
                            "Perfil de Rivales",
                            color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (state.rivales.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(RedError.copy(0.12f))
                                    .border(1.dp, RedError.copy(0.3f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("${state.rivales.size}", color = RedError, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RedError)
                                    .clickable { editingRival = null; showEdit = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Text("AÑADIR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }

            if (state.rivales.isEmpty()) {
                // ── Empty state ──
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(88.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(RedError.copy(0.08f))
                                .border(1.dp, RedError.copy(0.2f), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Groups, null, tint = RedError.copy(0.5f), modifier = Modifier.size(44.dp))
                        }
                        Text("Sin rivales registrados", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            "Crea el perfil de equipos rivales\npara analizar sus jugadoras,\ntáctica y tendencias.",
                            color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(RedError)
                                .clickable { showEdit = true }
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Text("Crear primer rival", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            } else if (isTablet) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gridItems(state.rivales, key = { it.id }) { rival ->
                        ProRivalCard(
                            rival = rival,
                            onClick = { viewingRival = rival },
                            onEdit = { editingRival = rival; showEdit = true },
                            onDelete = { viewModel.deleteRival(rival.id) }
                        )
                    }
                    item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.rivales, key = { it.id }) { rival ->
                        ProRivalCard(
                            rival = rival,
                            onClick = { viewingRival = rival },
                            onEdit = { editingRival = rival; showEdit = true },
                            onDelete = { viewModel.deleteRival(rival.id) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // ── Edit / Create sheet ──
    if (showEdit) {
        RivalEditSheet(
            initial = editingRival,
            onDismiss = { showEdit = false; editingRival = null },
            onSave = { id, nombre, temp, sof, sdef, jug, fort, deb, notas, vic, der ->
                viewModel.saveRival(id, nombre, temp, sof, sdef, jug, fort, deb, notas, vic, der)
                showEdit = false; editingRival = null
            }
        )
    }

    // ── Detail sheet ──
    viewingRival?.let { rival ->
        RivalDetailFullSheet(
            rival = rival,
            onDismiss = { viewingRival = null },
            onEdit = { editingRival = rival; viewingRival = null; showEdit = true }
        )
    }
}

// ── Main rival card ──────────────────────────────────────────────────────────

@Composable
private fun ProRivalCard(
    rival: RivalPerfilEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val hasRecord = rival.victorias > 0 || rival.derrotas > 0
    val totalGames = rival.victorias + rival.derrotas
    val winPct = if (totalGames > 0) rival.victorias.toFloat() / totalGames else -1f
    val accentColor = when {
        winPct > 0.6f -> RedError       // Dangerous — they win a lot
        winPct in 0.4f..0.6f -> YellowWarning
        winPct >= 0f -> GreenSuccess    // Beatable record
        else -> TextSecondary           // No record yet
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor)
        )

        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Name row + record + actions ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        rival.nombre.uppercase(),
                        color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    if (rival.temporada.isNotEmpty()) {
                        Text("Temp. ${rival.temporada}", color = TextTertiary, fontSize = 11.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (hasRecord) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(0.12f))
                                .border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${rival.victorias}V-${rival.derrotas}D",
                                color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Edit, null, tint = TealAccent, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.6f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // ── System badges ──
            if (rival.sistemaOfensivo.isNotEmpty() || rival.sistemaDefensivo.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (rival.sistemaOfensivo.isNotEmpty()) {
                        SystemBadge("OF: ${rival.sistemaOfensivo}", OrangeBase)
                    }
                    if (rival.sistemaDefensivo.isNotEmpty()) {
                        SystemBadge("DEF: ${rival.sistemaDefensivo}", TealAccent)
                    }
                }
            }

            // ── Strengths / Weaknesses ──
            if (rival.fortalezas.isNotEmpty() || rival.debilidades.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    if (rival.fortalezas.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(Icons.Filled.TrendingUp, null, tint = RedError, modifier = Modifier.size(13.dp).padding(top = 1.dp))
                            Text(rival.fortalezas, color = TextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (rival.debilidades.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(Icons.Filled.TrendingDown, null, tint = GreenSuccess, modifier = Modifier.size(13.dp).padding(top = 1.dp))
                            Text(rival.debilidades, color = TextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // ── Key players preview ──
            if (rival.jugadoresClave.isNotEmpty()) {
                val parsedPlayers = parseJugadoraLines(rival.jugadoresClave)
                val first = parsedPlayers.firstOrNull()
                val firstDisplay = if (first != null) {
                    buildString {
                        val num = first.getOrNull(0) ?: ""; val name = first.getOrNull(1) ?: ""; val pos = first.getOrNull(2) ?: ""
                        if (num.isNotEmpty()) append("#$num "); append(name); if (pos.isNotEmpty()) append(" · $pos")
                    }
                } else rival.jugadoresClave.lines().firstOrNull() ?: ""
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent.copy(0.06f))
                        .border(1.dp, GoldAccent.copy(0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Person, null, tint = GoldAccent, modifier = Modifier.size(12.dp))
                    Text(firstDisplay.take(55), color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    if (parsedPlayers.size > 1) {
                        Text("+${parsedPlayers.size - 1}", color = GoldAccent.copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Footer "Ver análisis" ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("VER ANÁLISIS COMPLETO", color = RedError.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                Icon(Icons.Filled.ChevronRight, null, tint = RedError.copy(0.7f), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun SystemBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(0.1f))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp)
    }
}

// ── Edit / Create full-screen sheet ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RivalEditSheet(
    initial: RivalPerfilEntity?,
    onDismiss: () -> Unit,
    onSave: (Int, String, String, String, String, String, String, String, String, Int, Int) -> Unit
) {
    var nombre by remember { mutableStateOf(initial?.nombre ?: "") }
    var temporada by remember { mutableStateOf(initial?.temporada ?: "") }
    var victorias by remember { mutableStateOf(if ((initial?.victorias ?: 0) > 0) initial!!.victorias.toString() else "") }
    var derrotas by remember { mutableStateOf(if ((initial?.derrotas ?: 0) > 0) initial!!.derrotas.toString() else "") }
    var sistemaOfensivo by remember { mutableStateOf(initial?.sistemaOfensivo ?: "") }
    var sistemaDefensivo by remember { mutableStateOf(initial?.sistemaDefensivo ?: "") }
    var jugadoresClave by remember { mutableStateOf(initial?.jugadoresClave ?: "") }
    var fortalezas by remember { mutableStateOf(initial?.fortalezas ?: "") }
    var debilidades by remember { mutableStateOf(initial?.debilidades ?: "") }
    var notas by remember { mutableStateOf(initial?.notas ?: "") }
    var selectedTab by remember { mutableIntStateOf(0) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NavyDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.96f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ──
            Column(
                modifier = Modifier.fillMaxWidth().background(NavySurface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (initial == null) "NUEVO RIVAL" else "EDITAR RIVAL",
                            color = RedError, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                        )
                        Text(
                            nombre.ifEmpty { "Sin nombre" },
                            color = if (nombre.isEmpty()) TextTertiary else TextPrimary,
                            fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar", color = TextSecondary)
                        }
                        Button(
                            onClick = {
                                if (nombre.isNotBlank()) onSave(
                                    initial?.id ?: 0, nombre, temporada,
                                    sistemaOfensivo, sistemaDefensivo, jugadoresClave,
                                    fortalezas, debilidades, notas,
                                    victorias.toIntOrNull() ?: 0, derrotas.toIntOrNull() ?: 0
                                )
                            },
                            enabled = nombre.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = RedError, disabledContainerColor = NavyElevated),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("GUARDAR", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                        }
                    }
                }

                // Tab progress dots
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("EQUIPO", "JUGADORAS", "TÁCTICA", "ANÁLISIS").forEachIndexed { i, label ->
                        val isSelected = i == selectedTab
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) RedError else NavyElevated)
                                .clickable { selectedTab = i }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape)
                                .background(if (isSelected) Color.White else NavyBorder))
                            Text(label, color = if (isSelected) Color.White else TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }
            HorizontalDivider(color = NavyBorder)

            // ── Tab content ──
            when (selectedTab) {
                0 -> EditEquipoTab(nombre, { nombre = it }, temporada, { temporada = it }, victorias, { victorias = it }, derrotas, { derrotas = it })
                1 -> EditJugadorasTab(jugadoresClave, { jugadoresClave = it })
                2 -> EditTacticaTab(sistemaOfensivo, { sistemaOfensivo = it }, sistemaDefensivo, { sistemaDefensivo = it })
                3 -> EditAnalisisTab(fortalezas, { fortalezas = it }, debilidades, { debilidades = it }, notas, { notas = it })
            }
        }
    }
}

// ── Edit tab: EQUIPO ─────────────────────────────────────────────────────────

@Composable
private fun EditEquipoTab(
    nombre: String, onNombre: (String) -> Unit,
    temporada: String, onTemporada: (String) -> Unit,
    victorias: String, onVictorias: (String) -> Unit,
    derrotas: String, onDerrotas: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EditSectionHeader("INFORMACIÓN DEL EQUIPO", RedError)

        OutlinedTextField(
            value = nombre, onValueChange = onNombre,
            label = { Text("Nombre del equipo *", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = editFieldColors(RedError),
            singleLine = true
        )

        OutlinedTextField(
            value = temporada, onValueChange = onTemporada,
            label = { Text("Temporada (ej: 2024/25)", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = editFieldColors(TextSecondary),
            singleLine = true
        )

        HorizontalDivider(color = NavyBorder, modifier = Modifier.padding(vertical = 4.dp))
        EditSectionHeader("HISTORIAL DE ENFRENTAMIENTOS", GoldAccent)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = victorias, onValueChange = onVictorias,
                label = { Text("Victorias", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = editFieldColors(GreenSuccess),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = derrotas, onValueChange = onDerrotas,
                label = { Text("Derrotas", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = editFieldColors(RedError),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        // Balance preview
        val vic = victorias.toIntOrNull() ?: 0
        val der = derrotas.toIntOrNull() ?: 0
        if (vic + der > 0) {
            val total = vic + der
            val pct = vic.toFloat() / total
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("BALANCE VS ESTE EQUIPO", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$vic", color = GreenSuccess, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 36.sp)
                            Text("VICTORIAS", color = GreenSuccess.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                        }
                        Text("–", color = TextTertiary, fontSize = 20.sp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$der", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 36.sp)
                            Text("DERROTAS", color = RedError.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    // Win % bar
                    Box(
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                            .background(RedError.copy(0.2f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxHeight().fillMaxWidth(pct).clip(RoundedCornerShape(3.dp))
                                .background(GreenSuccess)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${(pct * 100).toInt()}% victorias", color = TextTertiary, fontSize = 10.sp)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

// ── Edit tab: JUGADORAS ──────────────────────────────────────────────────────

// Player data model for the interactive list
// Stored as pipe-delimited lines: "numero|nombre|posicion|notas"
private fun parseJugadoraLines(raw: String): List<List<String>> {
    return raw.lines()
        .filter { it.trim().isNotEmpty() }
        .map { line ->
            val parts = line.split("|")
            listOf(
                parts.getOrNull(0) ?: "",
                parts.getOrNull(1) ?: "",
                parts.getOrNull(2) ?: "",
                parts.getOrNull(3) ?: ""
            )
        }
}

private fun serializeJugadoraLines(list: List<List<String>>): String {
    return list.joinToString("\n") { it.joinToString("|") }
}

@Composable
private fun EditJugadorasTab(jugadoras: String, onJugadoras: (String) -> Unit) {
    val players = remember(jugadoras) { parseJugadoraLines(jugadoras).toMutableList() }
    var showAddPlayer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditSectionHeader("PLANTILLA RIVAL (${players.size})", OrangeBase)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrangeBase)
                    .clickable { showAddPlayer = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.PersonAdd, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text("AÑADIR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        if (players.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(NavyCard).border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.PersonOutline, null, tint = TextTertiary, modifier = Modifier.size(36.dp))
                    Text("Sin jugadoras registradas", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Pulsa AÑADIR para registrar\nlas jugadoras del equipo rival", color = TextTertiary, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        } else {
            players.forEachIndexed { index, player ->
                val accentColors = listOf(OrangeBase, TealAccent, PurpleAccent, GoldAccent, GreenSuccess, RedError)
                val accent = accentColors[index % accentColors.size]
                PlayerEditCard(
                    player = player,
                    accent = accent,
                    onDelete = {
                        val updated = players.toMutableList()
                        updated.removeAt(index)
                        onJugadoras(serializeJugadoraLines(updated))
                    }
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }

    // Add player dialog
    if (showAddPlayer) {
        AddPlayerDialog(
            onDismiss = { showAddPlayer = false },
            onAdd = { numero, nombre, posicion, notas ->
                val updated = players.toMutableList()
                updated.add(listOf(numero, nombre, posicion, notas))
                onJugadoras(serializeJugadoraLines(updated))
                showAddPlayer = false
            }
        )
    }
}

@Composable
private fun PlayerEditCard(player: List<String>, accent: Color, onDelete: () -> Unit) {
    val numero = player.getOrNull(0) ?: ""
    val nombre = player.getOrNull(1) ?: ""
    val posicion = player.getOrNull(2) ?: ""
    val notas = player.getOrNull(3) ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
    ) {
        Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(accent))
        Row(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Jersey number
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(0.12f)).border(1.dp, accent.copy(0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (numero.isNotEmpty()) "#$numero" else "?",
                    color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp
                )
            }
            // Info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(nombre.ifEmpty { "Sin nombre" }, color = if (nombre.isEmpty()) TextTertiary else TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (posicion.isNotEmpty()) {
                    Text(posicion, color = accent.copy(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                if (notas.isNotEmpty()) {
                    Text(notas, color = TextSecondary, fontSize = 11.sp)
                }
            }
            // Delete
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.6f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AddPlayerDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String) -> Unit) {
    var numero by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var posicion by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(OrangeBase))
                Text("AÑADIR JUGADORA", color = OrangeBase, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = numero, onValueChange = { numero = it },
                        label = { Text("Dorsal", fontSize = 12.sp) },
                        modifier = Modifier.width(80.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = editFieldColors(OrangeBase),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = nombre, onValueChange = { nombre = it },
                        label = { Text("Nombre *", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = editFieldColors(OrangeBase),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = posicion, onValueChange = { posicion = it },
                    label = { Text("Posición (Base, Escolta...)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = editFieldColors(OrangeBase),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notas, onValueChange = { notas = it },
                    label = { Text("Características / Notas", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    colors = editFieldColors(OrangeBase),
                    placeholder = { Text("Tiradora de 3, rápida en transición...", color = TextTertiary, fontSize = 12.sp) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nombre.isNotBlank()) onAdd(numero, nombre, posicion, notas) },
                enabled = nombre.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeBase),
                shape = RoundedCornerShape(10.dp)
            ) { Text("AÑADIR", fontWeight = FontWeight.ExtraBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) }
        }
    )
}

// ── Edit tab: TÁCTICA ────────────────────────────────────────────────────────

@Composable
private fun EditTacticaTab(
    sistemaOfensivo: String, onOfensivo: (String) -> Unit,
    sistemaDefensivo: String, onDefensivo: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        EditSectionHeader("SISTEMA OFENSIVO", OrangeBase)
        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(OrangeBase.copy(0.05f))
                .border(1.dp, OrangeBase.copy(0.15f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.FlashOn, null, tint = OrangeBase, modifier = Modifier.size(14.dp))
                Text("¿Cómo atacan? ¿Qué movimientos y jugadas usan?", color = TextTertiary, fontSize = 11.sp)
            }
        }
        OutlinedTextField(
            value = sistemaOfensivo, onValueChange = onOfensivo,
            label = { Text("Sistema ofensivo y notas", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp),
            minLines = 4,
            shape = RoundedCornerShape(12.dp),
            colors = editFieldColors(OrangeBase),
            placeholder = { Text("Motion offense, mucho pick & roll, atacan el aro en transición...", color = TextTertiary, fontSize = 12.sp) }
        )

        HorizontalDivider(color = NavyBorder, modifier = Modifier.padding(vertical = 4.dp))

        EditSectionHeader("SISTEMA DEFENSIVO", TealAccent)
        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(TealAccent.copy(0.05f))
                .border(1.dp, TealAccent.copy(0.15f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Shield, null, tint = TealAccent, modifier = Modifier.size(14.dp))
                Text("¿Cómo defienden? ¿Zona, hombre a hombre, presión?", color = TextTertiary, fontSize = 11.sp)
            }
        }
        OutlinedTextField(
            value = sistemaDefensivo, onValueChange = onDefensivo,
            label = { Text("Sistema defensivo y notas", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp),
            minLines = 4,
            shape = RoundedCornerShape(12.dp),
            colors = editFieldColors(TealAccent),
            placeholder = { Text("Zona 2-3 principalmente, presión full court en el 4º cuarto...", color = TextTertiary, fontSize = 12.sp) }
        )
        Spacer(Modifier.height(32.dp))
    }
}

// ── Edit tab: ANÁLISIS ───────────────────────────────────────────────────────

@Composable
private fun EditAnalisisTab(
    fortalezas: String, onFortalezas: (String) -> Unit,
    debilidades: String, onDebilidades: (String) -> Unit,
    notas: String, onNotas: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Fortalezas
        EditSectionHeader("FORTALEZAS DEL RIVAL", RedError)
        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(RedError.copy(0.05f))
                .border(1.dp, RedError.copy(0.15f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Warning, null, tint = RedError, modifier = Modifier.size(14.dp))
                Text("¿En qué son buenos? ¿Qué nos puede hacer daño?", color = TextTertiary, fontSize = 11.sp)
            }
        }
        OutlinedTextField(
            value = fortalezas, onValueChange = onFortalezas,
            label = { Text("Fortalezas", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = editFieldColors(RedError),
            placeholder = { Text("Muy buenas en transición, tiran bien de 3...", color = TextTertiary, fontSize = 12.sp) }
        )

        HorizontalDivider(color = NavyBorder, modifier = Modifier.padding(vertical = 4.dp))

        // Debilidades
        EditSectionHeader("DEBILIDADES DEL RIVAL", GreenSuccess)
        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(GreenSuccess.copy(0.05f))
                .border(1.dp, GreenSuccess.copy(0.15f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.GpsFixed, null, tint = GreenSuccess, modifier = Modifier.size(14.dp))
                Text("¿Dónde podemos atacarles? ¿Qué explotar?", color = TextTertiary, fontSize = 11.sp)
            }
        }
        OutlinedTextField(
            value = debilidades, onValueChange = onDebilidades,
            label = { Text("Debilidades", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = editFieldColors(GreenSuccess),
            placeholder = { Text("Flojean en rebote defensivo, lentas en el cierre...", color = TextTertiary, fontSize = 12.sp) }
        )

        HorizontalDivider(color = NavyBorder, modifier = Modifier.padding(vertical = 4.dp))

        // Notas
        EditSectionHeader("NOTAS ADICIONALES", GoldAccent)
        OutlinedTextField(
            value = notas, onValueChange = onNotas,
            label = { Text("Notas libres", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            shape = RoundedCornerShape(12.dp),
            colors = editFieldColors(GoldAccent),
            placeholder = { Text("Observaciones generales, plan de partido...", color = TextTertiary, fontSize = 12.sp) }
        )
        Spacer(Modifier.height(32.dp))
    }
}

// ── Detail full-screen sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RivalDetailFullSheet(
    rival: RivalPerfilEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NavyDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.96f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ──
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(RedError.copy(0.1f), Color.Transparent))
                    )
                    .background(NavySurface)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ANÁLISIS DE RIVAL", color = RedError, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                            Text(rival.nombre.uppercase(), color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            if (rival.temporada.isNotEmpty()) {
                                Text("Temporada ${rival.temporada}", color = TextTertiary, fontSize = 12.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (rival.victorias + rival.derrotas > 0) {
                                val isWinning = rival.victorias > rival.derrotas
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isWinning) RedError.copy(0.12f) else GreenSuccess.copy(0.08f))
                                        .border(1.dp, if (isWinning) RedError.copy(0.4f) else GreenSuccess.copy(0.3f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${rival.victorias}V · ${rival.derrotas}D",
                                            color = if (isWinning) RedError else GreenSuccess,
                                            fontWeight = FontWeight.ExtraBold, fontSize = 13.sp
                                        )
                                        Text("RECORD", color = TextTertiary, fontSize = 8.sp, letterSpacing = 1.sp)
                                    }
                                }
                            }
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Filled.Edit, null, tint = TealAccent)
                            }
                        }
                    }

                    // System badges inline in header
                    if (rival.sistemaOfensivo.isNotEmpty() || rival.sistemaDefensivo.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (rival.sistemaOfensivo.isNotEmpty()) SystemBadge("⚡ OF: ${rival.sistemaOfensivo}", OrangeBase)
                            if (rival.sistemaDefensivo.isNotEmpty()) SystemBadge("🛡 DEF: ${rival.sistemaDefensivo}", TealAccent)
                        }
                    }
                }
            }

            // ── Tab bar ──
            val tabs = listOf("RESUMEN", "JUGADORAS", "TÁCTICA", "ANÁLISIS")
            RivalTabBar(tabs = tabs, selectedTab = selectedTab, onTabSelected = { selectedTab = it })

            // ── Tab content ──
            when (selectedTab) {
                0 -> ViewResumenTab(rival)
                1 -> ViewJugadorasTab(rival.jugadoresClave)
                2 -> ViewTacticaTab(rival.sistemaOfensivo, rival.sistemaDefensivo)
                3 -> ViewAnalisisTab(rival.fortalezas, rival.debilidades, rival.notas)
            }
        }
    }
}

@Composable
private fun ViewResumenTab(rival: RivalPerfilEntity) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Record card
        if (rival.victorias + rival.derrotas > 0) {
            val total = rival.victorias + rival.derrotas
            val pct = rival.victorias.toFloat() / total
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("BALANCE ENFRENTAMIENTOS", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${rival.victorias}", color = GreenSuccess, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 36.sp)
                            Text("VICTORIAS", color = GreenSuccess.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                        }
                        Text("–", color = TextTertiary, fontSize = 20.sp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${rival.derrotas}", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 36.sp)
                            Text("DERROTAS", color = RedError.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(RedError.copy(0.2f))) {
                        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(pct).clip(RoundedCornerShape(3.dp)).background(GreenSuccess))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${(pct * 100).toInt()}% de victorias contra este rival", color = TextTertiary, fontSize = 10.sp)
                }
            }
        }

        // Quick overview of all data
        if (rival.sistemaOfensivo.isNotEmpty() || rival.sistemaDefensivo.isNotEmpty()) {
            ViewSection("SISTEMAS DE JUEGO", NeonGreen) {
                if (rival.sistemaOfensivo.isNotEmpty()) {
                    InfoRow(Icons.Filled.FlashOn, "OFENSIVO", rival.sistemaOfensivo, OrangeBase)
                }
                if (rival.sistemaDefensivo.isNotEmpty()) {
                    InfoRow(Icons.Filled.Shield, "DEFENSIVO", rival.sistemaDefensivo, TealAccent)
                }
            }
        }

        if (rival.fortalezas.isNotEmpty()) {
            ViewSection("FORTALEZAS", RedError) {
                Text(rival.fortalezas, color = TextPrimary, fontSize = 14.sp, lineHeight = 22.sp)
            }
        }

        if (rival.debilidades.isNotEmpty()) {
            ViewSection("DEBILIDADES", GreenSuccess) {
                Text(rival.debilidades, color = TextPrimary, fontSize = 14.sp, lineHeight = 22.sp)
            }
        }

        if (rival.jugadoresClave.isNotEmpty()) {
            val lineCount = rival.jugadoresClave.lines().count { it.trim().isNotEmpty() }
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldAccent.copy(0.06f))
                    .border(1.dp, GoldAccent.copy(0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Person, null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Text("$lineCount jugadora${if (lineCount != 1) "s" else ""} registrada${if (lineCount != 1) "s" else ""}", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.weight(1f))
                    Text("→ Ver tab JUGADORAS", color = GoldAccent.copy(0.6f), fontSize = 10.sp)
                }
            }
        }

        if (rival.notas.isNotEmpty()) {
            ViewSection("NOTAS", GoldAccent) {
                Text(rival.notas, color = TextPrimary, fontSize = 14.sp, lineHeight = 22.sp)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ViewJugadorasTab(jugadoresClave: String) {
    if (jugadoresClave.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.PersonOutline, null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                Text("Sin jugadoras registradas", color = TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Edita el perfil para añadir\nlas jugadoras del rival", color = TextTertiary, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
        return
    }

    val players = parseJugadoraLines(jugadoresClave)
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(3.dp, 16.dp).clip(RoundedCornerShape(1.5.dp)).background(OrangeBase))
            Text("PLANTILLA (${players.size})", color = OrangeBase, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        }
        players.forEachIndexed { index, player ->
            val accentColors = listOf(OrangeBase, TealAccent, PurpleAccent, GoldAccent, GreenSuccess, RedError)
            val accent = accentColors[index % accentColors.size]
            val numero = player.getOrNull(0) ?: ""
            val nombre = player.getOrNull(1) ?: ""
            val posicion = player.getOrNull(2) ?: ""
            val notas = player.getOrNull(3) ?: ""
            val displayLine = buildString {
                if (numero.isNotEmpty()) append("#$numero  ")
                append(nombre)
                if (posicion.isNotEmpty()) append("  ·  $posicion")
                if (notas.isNotEmpty()) append("\n$notas")
            }
            PlayerRow(index + 1, displayLine.trim(), accent)
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PlayerRow(index: Int, line: String, accent: Color = OrangeBase) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
    ) {
        Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(accent))
        Row(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(accent.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text("$index", color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
            Text(line, color = TextPrimary, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ViewTacticaTab(sistemaOfensivo: String, sistemaDefensivo: String) {
    if (sistemaOfensivo.isEmpty() && sistemaDefensivo.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.SportsSoccer, null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                Text("Sin sistema registrado", color = TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (sistemaOfensivo.isNotEmpty()) {
            ViewSection("SISTEMA OFENSIVO", OrangeBase) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(OrangeBase.copy(0.06f))
                        .border(1.dp, OrangeBase.copy(0.2f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.FlashOn, null, tint = OrangeBase, modifier = Modifier.size(22.dp))
                        Text(sistemaOfensivo, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (sistemaDefensivo.isNotEmpty()) {
            ViewSection("SISTEMA DEFENSIVO", TealAccent) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TealAccent.copy(0.06f))
                        .border(1.dp, TealAccent.copy(0.2f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.Shield, null, tint = TealAccent, modifier = Modifier.size(22.dp))
                        Text(sistemaDefensivo, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ViewAnalisisTab(fortalezas: String, debilidades: String, notas: String) {
    if (fortalezas.isEmpty() && debilidades.isEmpty() && notas.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.Analytics, null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                Text("Sin análisis registrado", color = TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (fortalezas.isNotEmpty()) {
            ViewSection("FORTALEZAS — PUNTOS DE PELIGRO", RedError) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    fortalezas.lines().filter { it.trim().isNotEmpty() }.forEachIndexed { i, line ->
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(RedError).padding(top = 7.dp))
                            Text(line.trim(), color = TextPrimary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        if (debilidades.isNotEmpty()) {
            ViewSection("DEBILIDADES — PUNTOS DE ATAQUE", GreenSuccess) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    debilidades.lines().filter { it.trim().isNotEmpty() }.forEach { line ->
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GreenSuccess).padding(top = 7.dp))
                            Text(line.trim(), color = TextPrimary, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        if (notas.isNotEmpty()) {
            ViewSection("NOTAS DE SCOUTING", GoldAccent) {
                Text(notas, color = TextPrimary, fontSize = 14.sp, lineHeight = 22.sp)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

// ── Tab bar helper ───────────────────────────────────────────────────────────

@Composable
private fun RivalTabBar(tabs: List<String>, selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { i, label ->
                val isSelected = selectedTab == i
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSelected(i) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        label, fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected) RedError else TextTertiary,
                        letterSpacing = if (isSelected) 0.5.sp else 0.sp
                    )
                    if (isSelected) {
                        Spacer(Modifier.height(3.dp))
                        Box(modifier = Modifier.width(16.dp).height(2.dp).clip(RoundedCornerShape(1.dp)).background(RedError))
                    }
                }
            }
        }
        HorizontalDivider(color = NavyBorder)
    }
}

// ── Shared view helpers ──────────────────────────────────────────────────────

@Composable
private fun ViewSection(title: String, color: Color, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(3.dp, 16.dp).clip(RoundedCornerShape(1.5.dp)).background(color))
            Text(title, color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        }
        content()
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(0.06f))
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Column {
            Text(label, color = color.copy(0.7f), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
            Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Edit form helpers ────────────────────────────────────────────────────────

@Composable
private fun EditSectionHeader(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(3.dp, 16.dp).clip(RoundedCornerShape(1.5.dp)).background(color))
        Text(title, color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
    }
}

@Composable
private fun editFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
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
