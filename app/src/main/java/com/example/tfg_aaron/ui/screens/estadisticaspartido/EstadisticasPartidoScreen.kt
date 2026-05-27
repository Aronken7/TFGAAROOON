package com.example.tfg_aaron.ui.screens.estadisticaspartido

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.EstadisticaPartidoEntity
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasPartidoScreen(
    navController: NavController,
    entrenadorId: Int,
    idPartido: Int
) {
    val context = LocalContext.current
    val app = context.applicationContext as TFGApplication
    val viewModel: EstadisticasPartidoViewModel = viewModel(factory = viewModelFactory {
        initializer {
            EstadisticasPartidoViewModel(
                entrenadorId = entrenadorId,
                idPartido = idPartido,
                estadisticaPartidoRepository = app.estadisticaPartidoRepository,
                jugadoraRepository = app.jugadoraRepository,
                partidoRepository = app.partidoRepository
            )
        }
    })

    val uiState by viewModel.uiState.collectAsState()
    val jugadorasConStats by viewModel.jugadorasConStats.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var editTarget by remember { mutableStateOf<JugadoraConStats?>(null) }

    // Snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "STATS DEL PARTIDO",
                            fontSize = 9.sp, color = NeonGreen,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                        )
                        Text(
                            "vs ${uiState.partido?.rival ?: "..."}",
                            fontSize = 18.sp, color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Save is auto */ }) {
                        Icon(Icons.Filled.Save, null, tint = OrangeBase)
                    }
                    IconButton(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val rival = uiState.partido?.rival ?: ""
                            val sdf = SimpleDateFormat("d MMM yyyy", Locale("es"))
                            val fecha = uiState.partido?.fecha?.let { sdf.format(java.util.Date(it)) } ?: ""
                            val file = PdfGenerator.generarEstadisticasPartido(
                                context = context,
                                rival = rival,
                                fecha = fecha,
                                jugadoras = jugadorasConStats
                            )
                            file?.let {
                                withContext(Dispatchers.Main) {
                                    PdfGenerator.compartirPdf(context, it)
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Exportar PDF", tint = TealAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        containerColor = NavyDark
    ) { padding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(44.dp), strokeWidth = 3.dp)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // ── Header card: partido info ─────────────────────────────────────
            item {
                uiState.partido?.let { partido ->
                    PartidoHeaderCard(partido = partido, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                }
            }

            // ── Section label ─────────────────────────────────────────────────
            item {
                Text(
                    "ESTADÍSTICAS POR JUGADORA",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = TextTertiary, fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = NavyBorder, thickness = 1.dp
                )
                Spacer(Modifier.height(4.dp))
            }

            // ── Empty state ───────────────────────────────────────────────────
            if (jugadorasConStats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(OrangeBase.copy(0.08f))
                                    .border(1.dp, OrangeBase.copy(0.2f), RoundedCornerShape(16.dp))
                            ) {
                                Icon(Icons.Filled.BarChart, null, tint = OrangeBase, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.height(14.dp))
                            Text("Sin jugadoras activas", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Añade jugadoras al equipo primero", color = TextTertiary, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                // ── Jugadora rows ─────────────────────────────────────────────
                items(jugadorasConStats, key = { it.jugadora.id }) { jcs ->
                    JugadoraStatCard(
                        jcs = jcs,
                        onEditClick = { editTarget = jcs },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // ── Team totals ───────────────────────────────────────────────
                item {
                    val statsConDatos = jugadorasConStats.mapNotNull { it.stat }
                    if (statsConDatos.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        TeamTotalsRow(
                            totalPts = statsConDatos.sumOf { it.puntos },
                            totalReb = statsConDatos.sumOf { it.rebotes },
                            totalAst = statsConDatos.sumOf { it.asistencias },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // ── Edit dialog ───────────────────────────────────────────────────────────
    editTarget?.let { jcs ->
        EditStatDialog(
            jcs = jcs,
            onDismiss = { editTarget = null },
            onConfirm = { puntos, rebotes, asistencias, faltas, minutos ->
                viewModel.saveOrUpdateStat(
                    idJugadora = jcs.jugadora.id,
                    puntos = puntos,
                    rebotes = rebotes,
                    asistencias = asistencias,
                    faltas = faltas,
                    minutos = minutos
                )
                editTarget = null
            },
            onDelete = { stat ->
                viewModel.deleteStat(stat)
                editTarget = null
            }
        )
    }
}

// ── PARTIDO HEADER CARD ───────────────────────────────────────────────────────

@Composable
private fun PartidoHeaderCard(partido: PartidoEntity, modifier: Modifier = Modifier) {
    val sdf = SimpleDateFormat("d MMM yyyy", Locale("es"))
    val isWin = partido.puntosPropio > partido.puntosRival
    val isLoss = partido.puntosPropio < partido.puntosRival
    val hasScore = partido.puntosPropio > 0 || partido.puntosRival > 0

    val resultColor = when {
        !hasScore -> TextTertiary
        isWin -> NeonGreen
        isLoss -> RedError
        else -> TextSecondary
    }
    val resultLabel = when {
        !hasScore -> "PENDIENTE"
        isWin -> "VICTORIA"
        isLoss -> "DERROTA"
        else -> "EMPATE"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        resultColor.copy(alpha = 0.10f),
                        NavyCard,
                        NavyCard
                    )
                )
            )
            .border(1.dp, resultColor.copy(0.25f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Result badge + date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(resultColor.copy(0.15f))
                            .border(1.dp, resultColor.copy(0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(resultLabel, color = resultColor, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NavyElevated)
                            .border(1.dp, NavyBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (partido.esLocal) "LOCAL" else "VISITANTE",
                            color = if (partido.esLocal) TealAccent else PurpleAccent,
                            fontSize = 9.sp, fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.CalendarToday, null, tint = TextTertiary, modifier = Modifier.size(11.dp))
                    Text(
                        sdf.format(Date(partido.fecha)).uppercase(),
                        color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Score row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("NOSOTROS", color = TextTertiary, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Text(
                        "${partido.puntosPropio}",
                        color = if (isWin) NeonGreen else if (isLoss) TextSecondary else TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 48.sp,
                        letterSpacing = (-2).sp,
                        lineHeight = 48.sp
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text("VS", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        partido.rival.uppercase().take(12),
                        color = TextSecondary, fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp
                    )
                    Text(
                        "${partido.puntosRival}",
                        color = if (isLoss) RedError else if (isWin) TextSecondary else TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 48.sp,
                        letterSpacing = (-2).sp,
                        lineHeight = 48.sp
                    )
                }
            }

            if (partido.lugar.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.LocationOn, null, tint = TextTertiary, modifier = Modifier.size(11.dp))
                    Text(partido.lugar, color = TextTertiary, fontSize = 10.sp)
                }
            }
        }
    }
}

// ── JUGADORA STAT CARD ────────────────────────────────────────────────────────

@Composable
private fun JugadoraStatCard(
    jcs: JugadoraConStats,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val jugadora = jcs.jugadora
    val stat = jcs.stat
    val hasStat = stat != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, if (hasStat) OrangeBase.copy(0.15f) else NavyBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Number circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(OrangeBase.copy(0.15f))
                .border(1.dp, OrangeBase.copy(0.4f), CircleShape)
        ) {
            Text(
                "#${jugadora.numero}",
                color = OrangeBase,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.width(10.dp))

        // Name + posicion
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${jugadora.nombre} ${jugadora.apellidos}",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                jugadora.posicion,
                color = TextTertiary,
                fontSize = 10.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        // Stats display or "Sin stats"
        if (hasStat && stat != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatChip(value = "${stat.puntos}", label = "PTS", accent = NeonGreen)
                StatChip(value = "${stat.rebotes}", label = "REB", accent = TealAccent)
                StatChip(value = "${stat.asistencias}", label = "AST", accent = ElectricBlue)
                StatChip(value = "${stat.minutos}", label = "MIN", accent = GoldAccent)
                StatChip(value = "${stat.faltas}", label = "FAL", accent = RedError)
            }
        } else {
            Text(
                "Sin stats",
                color = TextTertiary,
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        // Edit / Add button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (hasStat) OrangeBase.copy(0.12f) else NeonGreen.copy(0.10f))
                .border(
                    1.dp,
                    if (hasStat) OrangeBase.copy(0.35f) else NeonGreen.copy(0.3f),
                    RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onEditClick)
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Text(
                if (hasStat) "Editar" else "+",
                color = if (hasStat) OrangeBase else NeonGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ── STAT CHIP ─────────────────────────────────────────────────────────────────

@Composable
private fun StatChip(value: String, label: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 18.sp)
        Text(label, color = TextTertiary, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
    }
}

// ── TEAM TOTALS ROW ───────────────────────────────────────────────────────────

@Composable
private fun TeamTotalsRow(
    totalPts: Int,
    totalReb: Int,
    totalAst: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        NeonGreen.copy(0.06f),
                        NavyCard,
                        TealAccent.copy(0.06f)
                    )
                )
            )
            .border(1.dp, NeonGreen.copy(0.2f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "TOTALES",
                color = TextTertiary, fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                StatChip(value = "$totalPts", label = "PTS", accent = NeonGreen)
                StatChip(value = "$totalReb", label = "REB", accent = TealAccent)
                StatChip(value = "$totalAst", label = "AST", accent = ElectricBlue)
            }
        }
    }
}

// ── EDIT STAT DIALOG ──────────────────────────────────────────────────────────

@Composable
private fun EditStatDialog(
    jcs: JugadoraConStats,
    onDismiss: () -> Unit,
    onConfirm: (puntos: Int, rebotes: Int, asistencias: Int, faltas: Int, minutos: Int) -> Unit,
    onDelete: (EstadisticaPartidoEntity) -> Unit
) {
    val existing = jcs.stat
    val jugadora = jcs.jugadora

    var puntosStr by remember { mutableStateOf(existing?.puntos?.toString() ?: "0") }
    var rebotesStr by remember { mutableStateOf(existing?.rebotes?.toString() ?: "0") }
    var asistenciasStr by remember { mutableStateOf(existing?.asistencias?.toString() ?: "0") }
    var faltasStr by remember { mutableStateOf(existing?.faltas?.toString() ?: "0") }
    var minutosStr by remember { mutableStateOf(existing?.minutos?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyElevated,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OrangeBase.copy(0.15f))
                        .border(1.dp, OrangeBase.copy(0.4f), CircleShape)
                ) {
                    Text("#${jugadora.numero}", color = OrangeBase, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                }
                Column {
                    Text(
                        "${jugadora.nombre} ${jugadora.apellidos}",
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(jugadora.posicion, color = TextTertiary, fontSize = 11.sp)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatInputField(
                    label = "PUNTOS",
                    value = puntosStr,
                    onValueChange = { puntosStr = it.filter { c -> c.isDigit() } },
                    accent = NeonGreen
                )
                StatInputField(
                    label = "REBOTES",
                    value = rebotesStr,
                    onValueChange = { rebotesStr = it.filter { c -> c.isDigit() } },
                    accent = TealAccent
                )
                StatInputField(
                    label = "ASISTENCIAS",
                    value = asistenciasStr,
                    onValueChange = { asistenciasStr = it.filter { c -> c.isDigit() } },
                    accent = ElectricBlue
                )
                StatInputField(
                    label = "MINUTOS",
                    value = minutosStr,
                    onValueChange = { minutosStr = it.filter { c -> c.isDigit() } },
                    accent = GoldAccent
                )
                StatInputField(
                    label = "FALTAS",
                    value = faltasStr,
                    onValueChange = { faltasStr = it.filter { c -> c.isDigit() } },
                    accent = RedError
                )
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        onConfirm(
                            puntosStr.toIntOrNull() ?: 0,
                            rebotesStr.toIntOrNull() ?: 0,
                            asistenciasStr.toIntOrNull() ?: 0,
                            faltasStr.toIntOrNull() ?: 0,
                            minutosStr.toIntOrNull() ?: 0
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeBase),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar", fontWeight = FontWeight.ExtraBold)
                }
                if (existing != null) {
                    OutlinedButton(
                        onClick = { onDelete(existing) },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, RedError.copy(0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedError)
                    ) {
                        Icon(Icons.Filled.Delete, null, modifier = Modifier.size(15.dp), tint = RedError)
                        Spacer(Modifier.width(6.dp))
                        Text("Eliminar stats", color = RedError, fontSize = 13.sp)
                    }
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, NavyBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        }
    )
}

// ── STAT INPUT FIELD ──────────────────────────────────────────────────────────

@Composable
private fun StatInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(80.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(accent.copy(0.08f))
                .border(1.dp, accent.copy(0.2f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                label,
                color = accent,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).height(52.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                unfocusedBorderColor = NavyBorder,
                cursorColor = accent,
                focusedContainerColor = NavyCard,
                unfocusedContainerColor = NavyCard
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}
