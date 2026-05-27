@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.tfg_aaron.ui.screens.objetivo

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
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.ObjetivoEntity
import com.example.tfg_aaron.ui.components.EmptyState
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import java.text.SimpleDateFormat
import java.util.*

private val TIPO_OBJETIVO_COLORS = mapOf(
    "RENDIMIENTO" to Color(0xFF3B82F6),
    "FÍSICO" to Color(0xFF10B981),
    "TÉCNICO" to Color(0xFFF59E0B),
    "PERSONAL" to Color(0xFF8B5CF6),
    "EQUIPO" to Color(0xFFEF4444)
)

@Composable
fun ObjetivoScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val vm: ObjetivoViewModel = viewModel(factory = viewModelFactory {
        initializer { ObjetivoViewModel(entrenadorId, app.objetivoRepository, app.jugadoraRepository) }
    })
    val state by vm.state.collectAsState()
    val objetivos = remember(state) { vm.filteredObjetivos() }
    val pendientes = remember(state) { state.objetivos.count { !it.completado } }
    val completados = remember(state) { state.objetivos.count { it.completado } }

    val isTablet = LocalIsTablet.current
    var showAddSheet by remember { mutableStateOf(false) }
    var editingObjetivo by remember { mutableStateOf<ObjetivoEntity?>(null) }
    var updatingProgreso by remember { mutableStateOf<ObjetivoEntity?>(null) }

    Scaffold(
        containerColor = NavyDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingObjetivo = null; showAddSheet = true },
                containerColor = GoldAccent, contentColor = NavyDark,
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
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().background(NavyCard)
                    .padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                        }
                        Column {
                            Text("OBJETIVOS DE TEMPORADA", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                            Text("Metas del equipo y jugadoras", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(modifier = Modifier.fillMaxWidth().background(NavyCard).padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryPill("$pendientes PENDIENTES", GoldAccent, Modifier.weight(1f))
                    SummaryPill("$completados COMPLETADOS", GreenSuccess, Modifier.weight(1f))
                }
                HorizontalDivider(color = NavyBorder)
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(10.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("TODOS", "EQUIPO", "JUGADORA", "COMPLETADOS")) { f ->
                        FilterChip(
                            selected = state.filtroTipo == f,
                            onClick = { vm.setFiltro(f) },
                            label = { Text(f, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent.copy(0.2f), selectedLabelColor = GoldAccent,
                                containerColor = NavyCard, labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = state.filtroTipo == f, selectedBorderColor = GoldAccent.copy(0.5f), borderColor = NavyBorder)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (objetivos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        EmptyState(Icons.Filled.EmojiEvents, "Sin objetivos", "Pulsa + para crear el primer objetivo")
                    }
                }
            } else {
                gridItems(objetivos, key = { it.id }) { obj ->
                    val jugadora = state.jugadoras.find { it.id == obj.idJugadora }
                    ObjetivoCard(
                        objetivo = obj,
                        jugadora = jugadora,
                        onEdit = { editingObjetivo = obj; showAddSheet = true },
                        onDelete = { vm.deleteObjetivo(obj.id) },
                        onToggleCompletado = { vm.toggleCompletado(obj) },
                        onUpdateProgreso = { updatingProgreso = obj }
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        ObjetivoAddSheet(
            jugadoras = state.jugadoras,
            initial = editingObjetivo,
            onDismiss = { showAddSheet = false; editingObjetivo = null },
            onSave = { id, jId, titulo, desc, tipo, metrica, valObj, unidad, fechaFin, notas ->
                vm.saveObjetivo(id, jId, titulo, desc, tipo, metrica, valObj, unidad, fechaFin, notas)
                showAddSheet = false; editingObjetivo = null
            }
        )
    }

    updatingProgreso?.let { obj ->
        ProgresoDialog(
            objetivo = obj,
            onDismiss = { updatingProgreso = null },
            onSave = { nuevoValor -> vm.updateProgreso(obj, nuevoValor); updatingProgreso = null }
        )
    }
}

@Composable
private fun SummaryPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(10.dp)).background(color.copy(0.12f)).border(1.dp, color.copy(0.3f), RoundedCornerShape(10.dp)).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun ObjetivoCard(
    objetivo: ObjetivoEntity,
    jugadora: JugadoraEntity?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleCompletado: () -> Unit,
    onUpdateProgreso: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es"))
    val tipoColor = TIPO_OBJETIVO_COLORS[objetivo.tipo] ?: GoldAccent
    val progress = if (objetivo.valorObjetivo > 0) (objetivo.valorActual / objetivo.valorObjetivo).coerceIn(0f, 1f) else 0f
    val progressPct = (progress * 100).toInt()

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (objetivo.completado) GreenSuccess.copy(0.05f) else NavyCard)
            .border(1.dp, if (objetivo.completado) GreenSuccess.copy(0.3f) else NavyBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top row
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(objetivo.titulo, color = if (objetivo.completado) GreenSuccess else TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Spacer(Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PillObj(objetivo.tipo, tipoColor)
                    if (objetivo.idJugadora == -1) {
                        PillObj("EQUIPO", OrangeBase)
                    } else {
                        jugadora?.let { PillObj("#${it.numero} ${it.nombre.split(" ").first()}", TealAccent) }
                    }
                    if (objetivo.completado) PillObj("COMPLETADO", GreenSuccess)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) { Icon(Icons.Filled.Edit, null, tint = TealAccent, modifier = Modifier.size(14.dp)) }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.6f), modifier = Modifier.size(14.dp)) }
            }
        }

        if (objetivo.descripcion.isNotBlank()) {
            Text(objetivo.descripcion, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
        }

        // Progress section
        if (objetivo.metrica.isNotBlank() || objetivo.valorObjetivo > 0) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (objetivo.metrica.isNotBlank()) {
                    Text(objetivo.metrica.uppercase(), color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (objetivo.completado) GreenSuccess else tipoColor,
                        trackColor = NavyElevated
                    )
                    Text("$progressPct%", color = if (objetivo.completado) GreenSuccess else tipoColor, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Actual: %.1f ${objetivo.unidad}".format(objetivo.valorActual), color = TextSecondary, fontSize = 11.sp)
                    Text("Meta: %.1f ${objetivo.unidad}".format(objetivo.valorObjetivo), color = tipoColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (objetivo.fechaFin > 0) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.Schedule, null, tint = TextTertiary, modifier = Modifier.size(12.dp))
                Text("Límite: ${sdf.format(Date(objetivo.fechaFin))}", color = TextTertiary, fontSize = 10.sp)
            }
        }

        // Action buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!objetivo.completado) {
                OutlinedButton(
                    onClick = onUpdateProgreso,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, tipoColor.copy(0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = tipoColor)
                ) { Text("Actualizar progreso", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
            OutlinedButton(
                onClick = onToggleCompletado,
                modifier = Modifier.weight(1f).height(36.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (objetivo.completado) GreenSuccess.copy(0.5f) else NavyBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (objetivo.completado) GreenSuccess else TextSecondary)
            ) {
                Icon(if (objetivo.completado) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (objetivo.completado) "Completado" else "Completar", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun PillObj(text: String, color: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(color.copy(0.15f)).border(1.dp, color.copy(0.4f), RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 2.dp)) {
        Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun ProgresoDialog(
    objetivo: ObjetivoEntity,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    var texto by remember { mutableStateOf(objetivo.valorActual.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        title = { Text("Actualizar progreso", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${objetivo.metrica} (${objetivo.unidad})", color = TextSecondary, fontSize = 13.sp)
                Text("Meta: %.1f ${objetivo.unidad} · Actual: %.1f ${objetivo.unidad}".format(objetivo.valorObjetivo, objetivo.valorActual), color = TextTertiary, fontSize = 11.sp)
                OutlinedTextField(
                    value = texto, onValueChange = { texto = it },
                    label = { Text("Nuevo valor", color = TextTertiary, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = NavyBorder, cursorColor = GoldAccent, focusedLabelColor = GoldAccent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { texto.toFloatOrNull()?.let { onSave(it) } }) {
                Text("GUARDAR", color = GoldAccent, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", color = TextSecondary) } }
    )
}

@Composable
private fun ObjetivoAddSheet(
    jugadoras: List<JugadoraEntity>,
    initial: ObjetivoEntity?,
    onDismiss: () -> Unit,
    onSave: (Int, Int, String, String, String, String, Float, String, Long, String) -> Unit
) {
    var esEquipo by remember { mutableStateOf(initial?.let { it.idJugadora == -1 } ?: true) }
    var selectedJugadora by remember { mutableStateOf(jugadoras.find { it.id == (initial?.idJugadora ?: -1) } ?: jugadoras.firstOrNull()) }
    var titulo by remember { mutableStateOf(initial?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(initial?.descripcion ?: "") }
    var tipoObj by remember { mutableStateOf(initial?.tipo ?: "RENDIMIENTO") }
    var metrica by remember { mutableStateOf(initial?.metrica ?: "") }
    var valorObjText by remember { mutableStateOf(if ((initial?.valorObjetivo ?: 0f) > 0) "%.1f".format(initial!!.valorObjetivo) else "") }
    var unidad by remember { mutableStateOf(initial?.unidad ?: "") }
    var fechaFin by remember { mutableLongStateOf(initial?.fechaFin ?: 0L) }
    var notas by remember { mutableStateOf(initial?.notas ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es"))
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tf = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent, unfocusedBorderColor = NavyBorder, focusedLabelColor = GoldAccent, cursorColor = GoldAccent, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)

    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = NavyDark, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { Box(modifier = Modifier.padding(vertical = 10.dp).size(40.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(NavyBorder)) }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(GoldAccent.copy(0.15f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.EmojiEvents, null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(if (initial == null) "Nuevo objetivo" else "Editar objetivo", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
                TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) }
            }
            HorizontalDivider(color = NavyBorder)

            // EQUIPO vs JUGADORA toggle
            Text("ASIGNAR A", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("EQUIPO" to true, "JUGADORA" to false).forEach { (label, isTeam) ->
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            .background(if (esEquipo == isTeam) OrangeBase.copy(0.2f) else NavyCard)
                            .border(1.dp, if (esEquipo == isTeam) OrangeBase else NavyBorder, RoundedCornerShape(10.dp))
                            .clickable { esEquipo = isTeam }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) { Text(label, color = if (esEquipo == isTeam) OrangeBase else TextSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp) }
                }
            }

            AnimatedVisibility(visible = !esEquipo && jugadoras.isNotEmpty()) {
                Column {
                    Text("JUGADORA", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(jugadoras) { j ->
                            val isSel = selectedJugadora?.id == j.id
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) TealAccent.copy(0.2f) else NavyCard)
                                    .border(1.dp, if (isSel) TealAccent else NavyBorder, RoundedCornerShape(10.dp))
                                    .clickable { selectedJugadora = j }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("#${j.numero}", color = if (isSel) TealAccent else TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(j.nombre.split(" ").first(), color = if (isSel) TealAccent else TextSecondary, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título del objetivo *", color = TextTertiary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), colors = tf)
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción", color = TextTertiary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), minLines = 2, colors = tf)

            Text("TIPO DE OBJETIVO", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("RENDIMIENTO","FÍSICO","TÉCNICO","PERSONAL")) { t ->
                    val c = TIPO_OBJETIVO_COLORS[t] ?: GoldAccent
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(if (tipoObj == t) c.copy(0.2f) else NavyCard)
                            .border(1.dp, if (tipoObj == t) c else NavyBorder, RoundedCornerShape(8.dp))
                            .clickable { tipoObj = t }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) { Text(t, color = if (tipoObj == t) c else TextSecondary, fontSize = 11.sp, fontWeight = if (tipoObj == t) FontWeight.ExtraBold else FontWeight.Normal) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = metrica, onValueChange = { metrica = it }, label = { Text("Métrica (qué medir)", color = TextTertiary, fontSize = 12.sp) }, modifier = Modifier.weight(2f), colors = tf)
                OutlinedTextField(value = unidad, onValueChange = { unidad = it }, label = { Text("Unidad", color = TextTertiary, fontSize = 12.sp) }, modifier = Modifier.weight(1f), colors = tf)
            }

            OutlinedTextField(value = valorObjText, onValueChange = { valorObjText = it },
                label = { Text("Valor objetivo", color = TextTertiary, fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(), colors = tf)

            Text("FECHA LÍMITE (opcional)", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            OutlinedButton(
                onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (fechaFin > 0) GoldAccent else TextSecondary),
                border = BorderStroke(1.dp, if (fechaFin > 0) GoldAccent.copy(0.5f) else NavyBorder), shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.CalendarToday, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (fechaFin > 0) sdf.format(Date(fechaFin)) else "Sin fecha límite", fontSize = 13.sp)
            }

            OutlinedTextField(value = notas, onValueChange = { notas = it }, label = { Text("Notas", color = TextTertiary, fontSize = 12.sp) }, modifier = Modifier.fillMaxWidth(), colors = tf)

            Button(
                onClick = {
                    if (titulo.isNotBlank()) {
                        val jId = if (esEquipo) -1 else (selectedJugadora?.id ?: -1)
                        onSave(initial?.id ?: 0, jId, titulo, descripcion, tipoObj, metrica, valorObjText.toFloatOrNull() ?: 0f, unidad, fechaFin, notas)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark),
                shape = RoundedCornerShape(12.dp), enabled = titulo.isNotBlank()
            ) {
                Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (initial == null) "CREAR OBJETIVO" else "GUARDAR CAMBIOS", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }

    if (showDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = if (fechaFin > 0) fechaFin else System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { dpState.selectedDateMillis?.let { fechaFin = it }; showDatePicker = false }) { Text("OK", color = GoldAccent, fontWeight = FontWeight.ExtraBold) } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = TextSecondary) } },
            colors = DatePickerDefaults.colors(containerColor = NavyCard)
        ) { DatePicker(state = dpState) }
    }
}
