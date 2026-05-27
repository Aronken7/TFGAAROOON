@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.tfg_aaron.ui.screens.lesion

import androidx.compose.animation.AnimatedVisibility
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
import com.example.tfg_aaron.data.local.entities.LesionEntity
import com.example.tfg_aaron.ui.components.EmptyState
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import java.text.SimpleDateFormat
import java.util.*

private val GRAVEDAD_COLORS = listOf(Color(0xFF22C55E), Color(0xFFFACC15), Color(0xFFF97316), Color(0xFFEF4444), Color(0xFF991B1B))
private val GRAVEDAD_LABELS = listOf("LEVE", "MODERADA", "GRAVE", "MUY GRAVE", "CRÍTICA")
private val ZONAS_COMUNES = listOf("Tobillo derecho","Tobillo izquierdo","Rodilla derecha","Rodilla izquierda","Isquiotibial derecho","Isquiotibial izquierdo","Cuádriceps","Gemelo","Espalda baja","Hombro","Muñeca","Cadera","Otro")
private val TIPOS_LESION = listOf("Esguince","Rotura fibrilar","Contusión","Tendinitis","Fractura","Luxación","Sobrecarga muscular","Distensión","Otro")

@Composable
fun LesionScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val vm: LesionViewModel = viewModel(factory = viewModelFactory {
        initializer { LesionViewModel(entrenadorId, app.lesionRepository, app.jugadoraRepository) }
    })
    val state by vm.state.collectAsState()
    val lesiones = remember(state) { vm.lesionesFiltered() }
    val activas = remember(state) { state.lesiones.count { it.estado == "ACTIVA" } }

    val isTablet = LocalIsTablet.current
    var showAddSheet by remember { mutableStateOf(false) }
    var editingLesion by remember { mutableStateOf<LesionEntity?>(null) }

    Scaffold(
        containerColor = NavyDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingLesion = null; showAddSheet = true },
                containerColor = RedError, contentColor = Color.White,
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
            // Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().background(NavyCard)
                    .padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                        }
                        Column {
                            Text("SEGUIMIENTO DE LESIONES", color = RedError, fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                            Text("Estado físico del equipo", color = TextPrimary,
                                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // Summary bar
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(NavyCard)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryChip("$activas ACTIVAS", RedError, Modifier.weight(1f))
                    SummaryChip("${state.lesiones.count { it.estado == "RECUPERADA" }} RECUPERADAS", GreenSuccess, Modifier.weight(1f))
                }
                HorizontalDivider(color = NavyBorder)
            }

            // Filter chips
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("TODAS", "ACTIVA", "RECUPERADA").forEach { f ->
                        FilterChip(
                            selected = state.filtroEstado == f,
                            onClick = { vm.setFiltro(f) },
                            label = { Text(f, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RedError.copy(0.2f),
                                selectedLabelColor = RedError,
                                containerColor = NavyCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = state.filtroEstado == f,
                                selectedBorderColor = RedError.copy(0.5f), borderColor = NavyBorder
                            )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (lesiones.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        EmptyState(Icons.Filled.HealthAndSafety, "Sin lesiones registradas",
                            "Pulsa + para registrar una lesión")
                    }
                }
            } else {
                gridItems(lesiones, key = { it.id }) { lesion ->
                    val jugadora = state.jugadoras.find { it.id == lesion.idJugadora }
                    LesionCard(
                        lesion = lesion,
                        jugadora = jugadora,
                        onEdit = { editingLesion = lesion; showAddSheet = true },
                        onDelete = { vm.deleteLesion(lesion.id) },
                        onMarcarRecuperada = { vm.marcarRecuperada(lesion) }
                    )
                }
            }
        }
    }

    if (showAddSheet && state.jugadoras.isNotEmpty()) {
        LesionAddSheet(
            jugadoras = state.jugadoras,
            initial = editingLesion,
            onDismiss = { showAddSheet = false; editingLesion = null },
            onSave = { id, jId, tipo, zona, grav, fIni, fEst, proto, notas ->
                vm.saveLesion(id, jId, tipo, zona, grav, fIni, fEst, proto, notas)
                showAddSheet = false; editingLesion = null
            }
        )
    }
}

@Composable
private fun SummaryChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp))
            .background(color.copy(0.12f))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold) }
}

@Composable
private fun LesionCard(
    lesion: LesionEntity,
    jugadora: JugadoraEntity?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarcarRecuperada: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es"))
    val gravColor = GRAVEDAD_COLORS.getOrElse(lesion.gravedad - 1) { RedError }
    val gravLabel = GRAVEDAD_LABELS.getOrElse(lesion.gravedad - 1) { "—" }
    val estadoColor = if (lesion.estado == "ACTIVA") RedError else GreenSuccess

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, if (lesion.estado == "ACTIVA") RedError.copy(0.3f) else NavyBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top row: player + actions
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    .background(estadoColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(jugadora?.let { "#${it.numero}" } ?: "?", color = estadoColor, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold) }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(jugadora?.nombre ?: "Jugadora ${lesion.idJugadora}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(jugadora?.posicion ?: "", color = TextTertiary, fontSize = 10.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Filled.Edit, null, tint = TealAccent, modifier = Modifier.size(15.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.6f), modifier = Modifier.size(15.dp))
                }
            }
        }

        // Injury info row
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PillLabel(gravLabel, gravColor)
            PillLabel(lesion.estado, estadoColor)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("TIPO", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                Text(lesion.tipo.ifEmpty { "—" }, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("ZONA", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                Text(lesion.zonaCorpo.ifEmpty { "—" }, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("INICIO", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                Text(sdf.format(Date(lesion.fechaInicio)), color = TextSecondary, fontSize = 12.sp)
            }
            if (lesion.fechaEstimadaVuelta > 0) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("VUELTA EST.", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    Text(sdf.format(Date(lesion.fechaEstimadaVuelta)), color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (lesion.protocolo.isNotBlank()) {
            Column {
                Text("PROTOCOLO", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                Text(lesion.protocolo, color = TextSecondary, fontSize = 12.sp, maxLines = 3)
            }
        }

        if (lesion.estado == "ACTIVA") {
            Button(
                onClick = onMarcarRecuperada,
                modifier = Modifier.fillMaxWidth().height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess.copy(0.15f), contentColor = GreenSuccess),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("MARCAR COMO RECUPERADA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun PillLabel(text: String, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
            .background(color.copy(0.15f))
            .border(1.dp, color.copy(0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) { Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
}

@Composable
private fun LesionAddSheet(
    jugadoras: List<JugadoraEntity>,
    initial: LesionEntity?,
    onDismiss: () -> Unit,
    onSave: (Int, Int, String, String, Int, Long, Long, String, String) -> Unit
) {
    var selectedJugadora by remember { mutableStateOf(jugadoras.find { it.id == initial?.idJugadora } ?: jugadoras.first()) }
    var tipo by remember { mutableStateOf(initial?.tipo ?: "") }
    var zonaCorpo by remember { mutableStateOf(initial?.zonaCorpo ?: "") }
    var gravedad by remember { mutableFloatStateOf((initial?.gravedad ?: 1).toFloat()) }
    var fechaInicio by remember { mutableLongStateOf(initial?.fechaInicio ?: System.currentTimeMillis()) }
    var fechaEstimada by remember { mutableLongStateOf(initial?.fechaEstimadaVuelta ?: 0L) }
    var protocolo by remember { mutableStateOf(initial?.protocolo ?: "") }
    var notas by remember { mutableStateOf(initial?.notas ?: "") }
    var showDatePickerInicio by remember { mutableStateOf(false) }
    var showDatePickerEstimada by remember { mutableStateOf(false) }
    var tipoExpanded by remember { mutableStateOf(false) }
    var zonaExpanded by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es"))
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tf = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = RedError, unfocusedBorderColor = NavyBorder,
        focusedLabelColor = RedError, cursorColor = RedError,
        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = NavyDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { Box(modifier = Modifier.padding(vertical = 10.dp).size(40.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(NavyBorder)) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp).padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(RedError.copy(0.15f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.HealthAndSafety, null, tint = RedError, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(if (initial == null) "Registrar lesión" else "Editar lesión", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
                TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) }
            }
            HorizontalDivider(color = NavyBorder)

            // Player selector
            Text("JUGADORA", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(jugadoras) { j ->
                    val isSel = selectedJugadora.id == j.id
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) RedError.copy(0.2f) else NavyCard)
                            .border(1.dp, if (isSel) RedError else NavyBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedJugadora = j }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("#${j.numero}", color = if (isSel) RedError else TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            Text(j.nombre.split(" ").first(), color = if (isSel) RedError else TextSecondary, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Normal)
                        }
                    }
                }
            }

            // Tipo de lesión
            Text("TIPO DE LESIÓN", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            ExposedDropdownMenuBox(expanded = tipoExpanded, onExpandedChange = { tipoExpanded = it }) {
                OutlinedTextField(
                    value = tipo, onValueChange = { tipo = it }, readOnly = false,
                    label = { Text("Tipo o descripción", color = TextTertiary, fontSize = 12.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = tf
                )
                ExposedDropdownMenu(expanded = tipoExpanded, onDismissRequest = { tipoExpanded = false }, modifier = Modifier.background(NavyElevated)) {
                    TIPOS_LESION.forEach { t ->
                        DropdownMenuItem(text = { Text(t, color = TextPrimary, fontSize = 13.sp) }, onClick = { tipo = t; tipoExpanded = false })
                    }
                }
            }

            // Zona corporal
            Text("ZONA CORPORAL", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            ExposedDropdownMenuBox(expanded = zonaExpanded, onExpandedChange = { zonaExpanded = it }) {
                OutlinedTextField(
                    value = zonaCorpo, onValueChange = { zonaCorpo = it }, readOnly = false,
                    label = { Text("Zona afectada", color = TextTertiary, fontSize = 12.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(zonaExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = tf
                )
                ExposedDropdownMenu(expanded = zonaExpanded, onDismissRequest = { zonaExpanded = false }, modifier = Modifier.background(NavyElevated)) {
                    ZONAS_COMUNES.forEach { z ->
                        DropdownMenuItem(text = { Text(z, color = TextPrimary, fontSize = 13.sp) }, onClick = { zonaCorpo = z; zonaExpanded = false })
                    }
                }
            }

            // Gravedad slider
            Text("GRAVEDAD", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            val gravIdx = (gravedad.toInt() - 1).coerceIn(0, 4)
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(GRAVEDAD_LABELS.getOrElse(gravIdx) { "" }, color = GRAVEDAD_COLORS.getOrElse(gravIdx) { RedError }, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Text("${gravedad.toInt()}/5", color = TextTertiary, fontSize = 12.sp)
                }
                Slider(
                    value = gravedad, onValueChange = { gravedad = it },
                    valueRange = 1f..5f, steps = 3,
                    colors = SliderDefaults.colors(thumbColor = GRAVEDAD_COLORS.getOrElse(gravIdx) { RedError }, activeTrackColor = GRAVEDAD_COLORS.getOrElse(gravIdx) { RedError }, inactiveTrackColor = NavyElevated),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Dates
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("FECHA INICIO", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    OutlinedButton(
                        onClick = { showDatePickerInicio = true }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, NavyBorder), shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.CalendarToday, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(sdf.format(Date(fechaInicio)), fontSize = 12.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("VUELTA EST.", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    OutlinedButton(
                        onClick = { showDatePickerEstimada = true }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (fechaEstimada > 0) GoldAccent else TextTertiary),
                        border = BorderStroke(1.dp, if (fechaEstimada > 0) GoldAccent.copy(0.5f) else NavyBorder), shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.CalendarToday, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (fechaEstimada > 0) sdf.format(Date(fechaEstimada)) else "Sin fecha", fontSize = 12.sp)
                    }
                }
            }

            OutlinedTextField(value = protocolo, onValueChange = { protocolo = it },
                label = { Text("Protocolo de recuperación", color = TextTertiary, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(), minLines = 2, colors = tf)

            OutlinedTextField(value = notas, onValueChange = { notas = it },
                label = { Text("Notas adicionales", color = TextTertiary, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(), colors = tf)

            Button(
                onClick = { onSave(initial?.id ?: 0, selectedJugadora.id, tipo, zonaCorpo, gravedad.toInt(), fechaInicio, fechaEstimada, protocolo, notas) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedError, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (initial == null) "REGISTRAR LESIÓN" else "GUARDAR CAMBIOS", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }

    if (showDatePickerInicio) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = fechaInicio)
        DatePickerDialog(
            onDismissRequest = { showDatePickerInicio = false },
            confirmButton = { TextButton(onClick = { dpState.selectedDateMillis?.let { fechaInicio = it }; showDatePickerInicio = false }) { Text("OK", color = RedError, fontWeight = FontWeight.ExtraBold) } },
            dismissButton = { TextButton(onClick = { showDatePickerInicio = false }) { Text("Cancelar", color = TextSecondary) } },
            colors = DatePickerDefaults.colors(containerColor = NavyCard)
        ) { DatePicker(state = dpState) }
    }

    if (showDatePickerEstimada) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = if (fechaEstimada > 0) fechaEstimada else System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePickerEstimada = false },
            confirmButton = { TextButton(onClick = { dpState.selectedDateMillis?.let { fechaEstimada = it }; showDatePickerEstimada = false }) { Text("OK", color = GoldAccent, fontWeight = FontWeight.ExtraBold) } },
            dismissButton = { TextButton(onClick = { showDatePickerEstimada = false }) { Text("Cancelar", color = TextSecondary) } },
            colors = DatePickerDefaults.colors(containerColor = NavyCard)
        ) { DatePicker(state = dpState) }
    }
}
