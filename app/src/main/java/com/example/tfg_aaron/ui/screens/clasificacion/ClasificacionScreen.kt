package com.example.tfg_aaron.ui.screens.clasificacion

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.tfg_aaron.data.local.entities.ClasificacionEntity
import com.example.tfg_aaron.ui.theme.*

private val GoldPodium = Color(0xFFFFD700)
private val SilverPodium = Color(0xFFC0C0C0)
private val BronzePodium = Color(0xFFCD7F32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClasificacionScreen(navController: NavController, entrenadorId: Int) {
    val app = LocalContext.current.applicationContext as TFGApplication
    val viewModel: ClasificacionViewModel = viewModel(factory = viewModelFactory {
        initializer {
            ClasificacionViewModel(entrenadorId, app.clasificacionRepository)
        }
    })

    val state by viewModel.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingEquipo by remember { mutableStateOf<ClasificacionEntity?>(null) }

    // Snackbar for saved/error messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.savedMessage, state.errorMessage) {
        val msg = state.savedMessage ?: state.errorMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = NavyDark,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavySurface)
                    .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "LIGA",
                            color = OrangeBase, fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                        )
                        Text(
                            "CLASIFICACIÓN",
                            color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold
                        )
                    }
                    // Add button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(OrangeBase)
                            .clickable { editingEquipo = null; showDialog = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("AÑADIR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // ── Temporada chips ──
            if (state.temporadas.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavySurface)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.temporadas.forEach { temporada ->
                        val isSelected = temporada == state.temporadaActual
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) OrangeBase else NavyCard)
                                .border(
                                    1.dp,
                                    if (isSelected) OrangeBase else NavyBorder,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.setTemporada(temporada) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                temporada,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                            )
                        }
                    }
                }
                HorizontalDivider(color = NavyBorder)
            }

            // ── Content ──
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrangeBase)
                    }
                }

                state.equipos.isEmpty() -> {
                    // Empty state
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(OrangeBase.copy(0.08f))
                                    .border(1.dp, OrangeBase.copy(0.2f), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = OrangeBase.copy(0.5f),
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            Text(
                                "Sin equipos en la tabla",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                "Añade equipos de tu liga para ver la tabla",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(OrangeBase)
                                    .clickable { editingEquipo = null; showDialog = true }
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Text("Añadir primer equipo", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Standings table
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            StandingsTableCard(
                                equipos = state.equipos,
                                entrenadorId = entrenadorId,
                                onEdit = { equipo ->
                                    editingEquipo = equipo
                                    showDialog = true
                                },
                                onDelete = { equipo ->
                                    viewModel.deleteEquipo(equipo)
                                }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    // ── Add/Edit dialog ──
    if (showDialog) {
        AddEditEquipoDialog(
            initial = editingEquipo,
            temporadaActual = state.temporadaActual,
            onDismiss = { showDialog = false; editingEquipo = null },
            onSave = { entity ->
                viewModel.saveEquipo(entity)
                showDialog = false
                editingEquipo = null
            },
            onDelete = { entity ->
                viewModel.deleteEquipo(entity)
                showDialog = false
                editingEquipo = null
            },
            recalcularPuntos = viewModel::recalcularPuntos
        )
    }
}

// ── Standings Table Card ──────────────────────────────────────────────────────

@Composable
private fun StandingsTableCard(
    equipos: List<ClasificacionEntity>,
    entrenadorId: Int,
    onEdit: (ClasificacionEntity) -> Unit,
    onDelete: (ClasificacionEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
    ) {
        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyElevated)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
            Text("EQUIPO", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            listOf("PJ", "PG", "PP", "SF", "SC", "DIF", "PTS").forEach { col ->
                Text(
                    col,
                    color = if (col == "PTS") OrangeBase else TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.width(40.dp)) // action buttons space
        }

        HorizontalDivider(color = NavyBorder)

        // Team rows
        equipos.forEachIndexed { index, equipo ->
            val position = index + 1
            val positionColor = when (position) {
                1 -> GoldPodium
                2 -> SilverPodium
                3 -> BronzePodium
                else -> NavyElevated
            }
            val isMyTeam = equipo.idEntrenador == entrenadorId
            val dif = equipo.sf - equipo.sc

            val rowBorder = if (isMyTeam) Modifier.border(1.dp, OrangeBase.copy(0.5f), RoundedCornerShape(0.dp)) else Modifier

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(rowBorder)
                    .background(
                        if (isMyTeam) OrangeBase.copy(0.06f)
                        else if (index % 2 == 0) Color.Transparent
                        else NavyElevated.copy(0.3f)
                    )
                    .clickable { onEdit(equipo) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Position badge
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(positionColor.copy(if (position <= 3) 0.85f else 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$position",
                        color = if (position <= 3) Color.Black else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.width(4.dp))

                // Team name
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        equipo.equipoNombre,
                        color = if (isMyTeam) OrangeBase else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isMyTeam) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isMyTeam) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(OrangeBase.copy(0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("YO", color = OrangeBase, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                // Stats
                StatCell(equipo.pj.toString(), TextSecondary)
                StatCell(equipo.pg.toString(), GreenSuccess)
                StatCell(equipo.pp.toString(), RedError.copy(0.8f))
                StatCell(equipo.sf.toString(), TextSecondary)
                StatCell(equipo.sc.toString(), TextSecondary)
                StatCell(
                    if (dif > 0) "+$dif" else "$dif",
                    when {
                        dif > 0 -> GreenSuccess
                        dif < 0 -> RedError
                        else -> TextTertiary
                    }
                )
                StatCell(equipo.puntos.toString(), OrangeBase, bold = true)

                // Actions
                IconButton(
                    onClick = { onDelete(equipo) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = TextTertiary.copy(0.5f), modifier = Modifier.size(14.dp))
                }
            }

            if (index < equipos.lastIndex) {
                HorizontalDivider(color = NavyBorder.copy(0.5f), modifier = Modifier.padding(horizontal = 12.dp))
            }
        }

        // Footer summary
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyElevated.copy(0.5f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "2 pts por victoria · 1 pt por derrota · Pulsa un equipo para editar",
                color = TextTertiary, fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun StatCell(value: String, color: Color, bold: Boolean = false) {
    Text(
        value,
        color = color,
        fontSize = 11.sp,
        fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Normal,
        modifier = Modifier.width(32.dp),
        textAlign = TextAlign.Center
    )
}

// ── Add/Edit Dialog ───────────────────────────────────────────────────────────

@Composable
private fun AddEditEquipoDialog(
    initial: ClasificacionEntity?,
    temporadaActual: String,
    onDismiss: () -> Unit,
    onSave: (ClasificacionEntity) -> Unit,
    onDelete: (ClasificacionEntity) -> Unit,
    recalcularPuntos: (ClasificacionEntity) -> ClasificacionEntity
) {
    var nombre by remember { mutableStateOf(initial?.equipoNombre ?: "") }
    var temporada by remember { mutableStateOf(initial?.temporada ?: temporadaActual) }
    var pjStr by remember { mutableStateOf(initial?.pj?.toString() ?: "") }
    var pgStr by remember { mutableStateOf(initial?.pg?.toString() ?: "") }
    var sfStr by remember { mutableStateOf(initial?.sf?.toString() ?: "") }
    var scStr by remember { mutableStateOf(initial?.sc?.toString() ?: "") }

    val pj = pjStr.toIntOrNull() ?: 0
    val pg = pgStr.toIntOrNull() ?: 0
    val pp = (pj - pg).coerceAtLeast(0)
    val sf = sfStr.toIntOrNull() ?: 0
    val sc = scStr.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(OrangeBase))
                Text(
                    if (initial == null) "AÑADIR EQUIPO" else "EDITAR EQUIPO",
                    color = OrangeBase, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del equipo *", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogFieldColors(OrangeBase),
                    singleLine = true
                )

                // Temporada
                OutlinedTextField(
                    value = temporada,
                    onValueChange = { temporada = it },
                    label = { Text("Temporada (ej: 2024-25)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = dialogFieldColors(TextSecondary),
                    singleLine = true
                )

                HorizontalDivider(color = NavyBorder, modifier = Modifier.padding(vertical = 2.dp))
                Text("ESTADÍSTICAS", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)

                // PJ + PG row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pjStr,
                        onValueChange = { pjStr = it.filter { c -> c.isDigit() } },
                        label = { Text("PJ", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = dialogFieldColors(TextSecondary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pgStr,
                        onValueChange = { pgStr = it.filter { c -> c.isDigit() } },
                        label = { Text("PG", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = dialogFieldColors(GreenSuccess),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    // PP auto-calculated
                    Box(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavyElevated)
                            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                    ) {
                        Column {
                            Text("PP", color = TextTertiary, fontSize = 10.sp)
                            Text("$pp", color = RedError, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // SF + SC row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sfStr,
                        onValueChange = { sfStr = it.filter { c -> c.isDigit() } },
                        label = { Text("SF", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = dialogFieldColors(TealAccent),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = scStr,
                        onValueChange = { scStr = it.filter { c -> c.isDigit() } },
                        label = { Text("SC", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = dialogFieldColors(RedError),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // Preview: Points auto-calculated
                val previewPts = pg * 2 + pp
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(OrangeBase.copy(0.08f))
                        .border(1.dp, OrangeBase.copy(0.2f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Puntos calculados:", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            "$previewPts pts  ($pg×2 + $pp×1)",
                            color = OrangeBase, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Delete button (only for existing)
                if (initial != null) {
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { onDelete(initial) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, RedError.copy(0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Eliminar equipo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        val draft = ClasificacionEntity(
                            id = initial?.id ?: 0,
                            idEntrenador = initial?.idEntrenador ?: 0,
                            temporada = temporada.ifBlank { temporadaActual },
                            equipoNombre = nombre.trim(),
                            pj = pj,
                            pg = pg,
                            pp = pp,
                            sf = sf,
                            sc = sc
                        )
                        onSave(recalcularPuntos(draft))
                    }
                },
                enabled = nombre.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeBase,
                    disabledContainerColor = NavyElevated
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("GUARDAR", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun dialogFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
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
