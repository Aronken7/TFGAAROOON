@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.tfg_aaron.ui.screens.gameplan

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.GamePlanEntity
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.ui.components.EmptyState
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import java.text.SimpleDateFormat
import java.util.*

// ── Role types ──────────────────────────────────────────────────────────────

enum class RolTipo(val label: String, val color: Color) {
    MARCAJE("Marcaje 1v1", Color(0xFFEF4444)),
    ZONA("Posición zona", Color(0xFF3B82F6)),
    TAREA("Tarea libre", Color(0xFF10B981))
}

data class AsignacionJugadora(
    val jugadora: JugadoraEntity,
    var activa: Boolean = false,
    var rolTipo: RolTipo = RolTipo.TAREA,
    var detalle: String = ""  // rival name, zone name, or free task
)

private fun compilarAjustes(asignaciones: List<AsignacionJugadora>): String {
    return asignaciones
        .filter { it.activa && it.detalle.isNotBlank() }
        .joinToString("\n") { a ->
            val rol = when (a.rolTipo) {
                RolTipo.MARCAJE -> "Marcar a: ${a.detalle}"
                RolTipo.ZONA -> "Zona/Posición: ${a.detalle}"
                RolTipo.TAREA -> a.detalle
            }
            "#${a.jugadora.numero} ${a.jugadora.nombre} → $rol"
        }
}

private fun parsearAjustes(texto: String, jugadoras: List<JugadoraEntity>): List<AsignacionJugadora> {
    val lines = texto.split("\n").filter { it.isNotBlank() }
    val base = jugadoras.map { AsignacionJugadora(it) }
    lines.forEach { line ->
        val arrowIdx = line.indexOf(" → ")
        if (arrowIdx < 0) return@forEach
        val detail = line.substring(arrowIdx + 3)
        val rolTipo = when {
            detail.startsWith("Marcar a:") -> RolTipo.MARCAJE
            detail.startsWith("Zona/Posición:") -> RolTipo.ZONA
            else -> RolTipo.TAREA
        }
        val detalleVal = when (rolTipo) {
            RolTipo.MARCAJE -> detail.removePrefix("Marcar a: ").trim()
            RolTipo.ZONA -> detail.removePrefix("Zona/Posición: ").trim()
            RolTipo.TAREA -> detail.trim()
        }
        // Try to find matching jugadora by #number
        val numMatch = Regex("#(\\d+)").find(line)?.groupValues?.getOrNull(1)?.toIntOrNull()
        base.find { it.jugadora.numero == numMatch }?.let { asig ->
            asig.activa = true; asig.rolTipo = rolTipo; asig.detalle = detalleVal
        }
    }
    return base
}

val SISTEMAS_DEFENSIVOS = listOf("MAN-TO-MAN","ZONA 2-3","ZONA 3-2","PRESS COMPLETO","PRESS MEDIO","MIXTA","TRAMPA","OTRO")

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun GamePlanScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: GamePlanViewModel = viewModel(factory = viewModelFactory {
        initializer { GamePlanViewModel(entrenadorId, app.gamePlanRepository, app.jugadoraRepository) }
    })
    val isTablet = LocalIsTablet.current
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPlan by remember { mutableStateOf<GamePlanEntity?>(null) }
    var viewingPlan by remember { mutableStateOf<GamePlanEntity?>(null) }

    Scaffold(
        containerColor = NavyDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingPlan = null; showAddDialog = true },
                containerColor = NeonGreen, contentColor = NavyDark,
                shape = RoundedCornerShape(14.dp)
            ) { Icon(Icons.Filled.Add, null) }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ── Header ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth().background(NavyCard)
                    .padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                    Column {
                        Text("PREPARACIÓN TÁCTICA", color = NeonGreen, fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                        Text("Planes de partido", color = TextPrimary,
                            fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            if (state.planes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(Icons.Filled.Assignment, "Sin planes de partido",
                        "Crea tu primer plan táctico pulsando +")
                }
            } else if (isTablet) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gridItems(state.planes, key = { it.id }) { plan ->
                        GamePlanCard(
                            plan = plan,
                            jugadoras = state.jugadoras,
                            onClick = { viewingPlan = plan },
                            onEdit = { editingPlan = plan; showAddDialog = true },
                            onDelete = { viewModel.deletePlan(plan.id) }
                        )
                    }
                    item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.planes, key = { it.id }) { plan ->
                        GamePlanCard(
                            plan = plan,
                            jugadoras = state.jugadoras,
                            onClick = { viewingPlan = plan },
                            onEdit = { editingPlan = plan; showAddDialog = true },
                            onDelete = { viewModel.deletePlan(plan.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        GamePlanBottomSheet(
            initial = editingPlan,
            jugadoras = state.jugadoras,
            onDismiss = { showAddDialog = false; editingPlan = null },
            onSave = { id, rival, desc, focoOf, focoDef, jugadas, sistema, ajustes, notas ->
                viewModel.savePlan(id, rival, desc, focoOf, focoDef, jugadas, sistema, ajustes, notas)
                showAddDialog = false; editingPlan = null
            }
        )
    }

    viewingPlan?.let { plan ->
        GamePlanDetailSheet(
            plan = plan,
            jugadoras = state.jugadoras,
            onDismiss = { viewingPlan = null }
        )
    }
}

// ── Plan card ─────────────────────────────────────────────────────────────────

@Composable
private fun GamePlanCard(
    plan: GamePlanEntity,
    jugadoras: List<JugadoraEntity>,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es"))
    val asignCount = plan.ajustesIndividuales.split("\n").count { it.contains(" → ") }
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("VS ${plan.rival.uppercase()}", color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                Text(sdf.format(Date(plan.fecha)), color = TextTertiary, fontSize = 11.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, null, tint = TealAccent, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.7f), modifier = Modifier.size(16.dp))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (plan.sistemaDefensivo.isNotEmpty()) {
                PillTag(plan.sistemaDefensivo, TealAccent)
            }
            if (asignCount > 0) {
                PillTag("$asignCount jugadoras asignadas", NeonGreen)
            }
        }
        if (plan.descripcion.isNotEmpty()) {
            Text(plan.descripcion, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
        }
    }
}

@Composable
private fun PillTag(text: String, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
            .background(color.copy(0.15f))
            .border(1.dp, color.copy(0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) { Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
}

// ── Detail sheet ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GamePlanDetailSheet(
    plan: GamePlanEntity,
    jugadoras: List<JugadoraEntity>,
    onDismiss: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("es"))
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = NavyCard) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                .padding(bottom = 32.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("PLAN VS ${plan.rival.uppercase()}", color = NeonGreen,
                fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text(sdf.format(Date(plan.fecha)), color = TextTertiary, fontSize = 12.sp)
            HorizontalDivider(color = NavyBorder)

            if (plan.descripcion.isNotEmpty()) PlanSection("DESCRIPCIÓN GENERAL", plan.descripcion, OrangeBase)
            if (plan.sistemaDefensivo.isNotEmpty()) PlanSection("SISTEMA DEFENSIVO", plan.sistemaDefensivo, TealAccent)
            if (plan.focoOfensivo.isNotEmpty()) PlanSection("FOCO OFENSIVO", plan.focoOfensivo, NeonGreen)
            if (plan.focoDefensivo.isNotEmpty()) PlanSection("FOCO DEFENSIVO", plan.focoDefensivo, Color(0xFF3B82F6))
            if (plan.jugadasClave.isNotEmpty()) PlanSection("JUGADAS CLAVE", plan.jugadasClave, GoldAccent)

            // ── Player assignments ─────────────────────────────────────────
            if (plan.ajustesIndividuales.isNotEmpty()) {
                val lineas = plan.ajustesIndividuales.split("\n").filter { it.contains(" → ") }
                if (lineas.isNotEmpty()) {
                    Text("ASIGNACIONES INDIVIDUALES", color = OrangeBase, fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        lineas.forEach { linea ->
                            val parts = linea.split(" → ", limit = 2)
                            val playerPart = parts.getOrElse(0) { linea }
                            val rolPart = parts.getOrElse(1) { "" }
                            val rolColor = when {
                                rolPart.startsWith("Marcar a:") -> Color(0xFFEF4444)
                                rolPart.startsWith("Zona/Posición:") -> Color(0xFF3B82F6)
                                else -> Color(0xFF10B981)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NavyElevated)
                                    .border(1.dp, rolColor.copy(0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(playerPart.trim(), color = TextPrimary, fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                                Text(rolPart.trim(), color = rolColor, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            if (plan.notasFinales.isNotEmpty()) PlanSection("NOTAS FINALES", plan.notasFinales, TextSecondary)
        }
    }
}

@Composable
private fun PlanSection(titulo: String, contenido: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(titulo, color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Text(contenido, color = TextPrimary, fontSize = 14.sp)
    }
}

// ── Add / Edit BottomSheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GamePlanBottomSheet(
    initial: GamePlanEntity?,
    jugadoras: List<JugadoraEntity>,
    onDismiss: () -> Unit,
    onSave: (Int, String, String, String, String, String, String, String, String) -> Unit
) {
    var rival by remember { mutableStateOf(initial?.rival ?: "") }
    var descripcion by remember { mutableStateOf(initial?.descripcion ?: "") }
    var focoOfensivo by remember { mutableStateOf(initial?.focoOfensivo ?: "") }
    var focoDefensivo by remember { mutableStateOf(initial?.focoDefensivo ?: "") }
    var jugadasClave by remember { mutableStateOf(initial?.jugadasClave ?: "") }
    var sistemaExpanded by remember { mutableStateOf(false) }
    var sistemaDefensivo by remember { mutableStateOf(initial?.sistemaDefensivo ?: "MAN-TO-MAN") }
    var notasFinales by remember { mutableStateOf(initial?.notasFinales ?: "") }

    // Player role assignments
    val asignaciones = remember {
        if (initial != null && initial.ajustesIndividuales.isNotBlank()) {
            mutableStateListOf(*parsearAjustes(initial.ajustesIndividuales, jugadoras).toTypedArray())
        } else {
            mutableStateListOf(*jugadoras.map { AsignacionJugadora(it) }.toTypedArray())
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val tf = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NeonGreen, unfocusedBorderColor = NavyBorder,
        focusedLabelColor = NeonGreen, cursorColor = NeonGreen,
        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
    )

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
                .padding(horizontal = 16.dp).padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(OrangeBase.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.Assignment, null, tint = OrangeBase, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(10.dp))
                    Text(if (initial == null) "Nuevo Plan" else "Editar Plan",
                        color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
                TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) }
            }
            HorizontalDivider(color = NavyBorder)

            // ── Basic info ────────────────────────────────────────────────────
            Text("RIVAL", color = TextTertiary, fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            OutlinedTextField(
                value = rival, onValueChange = { rival = it },
                label = { Text("Nombre del equipo rival", color = TextTertiary, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(), colors = tf
            )

            // ── Defensive system ──────────────────────────────────────────────
            Text("SISTEMA DEFENSIVO", color = TextTertiary, fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            ExposedDropdownMenuBox(expanded = sistemaExpanded, onExpandedChange = { sistemaExpanded = it }) {
                OutlinedTextField(
                    value = sistemaDefensivo, onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sistemaExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = tf
                )
                ExposedDropdownMenu(expanded = sistemaExpanded, onDismissRequest = { sistemaExpanded = false },
                    modifier = Modifier.background(NavyElevated)) {
                    SISTEMAS_DEFENSIVOS.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s, color = TextPrimary, fontSize = 13.sp) },
                            onClick = { sistemaDefensivo = s; sistemaExpanded = false }
                        )
                    }
                }
            }

            HorizontalDivider(color = NavyBorder)

            // ── Player role assignments ────────────────────────────────────────
            Text("ASIGNACIONES INDIVIDUALES", color = OrangeBase, fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)

            if (jugadoras.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyCard)
                        .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin jugadoras en el equipo", color = TextTertiary, fontSize = 13.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    asignaciones.forEachIndexed { idx, asig ->
                        JugadoraRolCard(
                            asignacion = asig,
                            onToggle = { asignaciones[idx] = asig.copy(activa = !asig.activa) },
                            onRolChange = { nuevoRol -> asignaciones[idx] = asig.copy(rolTipo = nuevoRol) },
                            onDetalleChange = { nuevo -> asignaciones[idx] = asig.copy(detalle = nuevo) },
                            tf = tf
                        )
                    }
                }
            }

            HorizontalDivider(color = NavyBorder)

            // ── Tactical focus ────────────────────────────────────────────────
            Text("FOCO OFENSIVO", color = NeonGreen, fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            OutlinedTextField(
                value = focoOfensivo, onValueChange = { focoOfensivo = it },
                label = { Text("Qué queremos hacer en ataque...", color = TextTertiary, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(), minLines = 2, colors = tf
            )
            Text("FOCO DEFENSIVO", color = Color(0xFF3B82F6), fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            OutlinedTextField(
                value = focoDefensivo, onValueChange = { focoDefensivo = it },
                label = { Text("Cómo vamos a defender...", color = TextTertiary, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(), minLines = 2, colors = tf
            )

            Text("JUGADAS CLAVE", color = GoldAccent, fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            OutlinedTextField(
                value = jugadasClave, onValueChange = { jugadasClave = it },
                label = { Text("Jugadas diseñadas para este partido...", color = TextTertiary, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(), minLines = 2, colors = tf
            )

            Text("DESCRIPCIÓN Y NOTAS", color = TextTertiary, fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            OutlinedTextField(
                value = descripcion, onValueChange = { descripcion = it },
                label = { Text("Contexto general...", color = TextTertiary, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(), minLines = 2, colors = tf
            )
            OutlinedTextField(
                value = notasFinales, onValueChange = { notasFinales = it },
                label = { Text("Notas finales del cuerpo técnico...", color = TextTertiary, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(), minLines = 2, colors = tf
            )

            // ── Save button ───────────────────────────────────────────────────
            Button(
                onClick = {
                    if (rival.isNotBlank()) {
                        onSave(
                            initial?.id ?: 0, rival, descripcion,
                            focoOfensivo, focoDefensivo, jugadasClave,
                            sistemaDefensivo, compilarAjustes(asignaciones), notasFinales
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = NavyDark),
                shape = RoundedCornerShape(12.dp),
                enabled = rival.isNotBlank()
            ) {
                Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (initial == null) "CREAR PLAN" else "GUARDAR CAMBIOS",
                    fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }
}

// ── Player role card ──────────────────────────────────────────────────────────

@Composable
private fun JugadoraRolCard(
    asignacion: AsignacionJugadora,
    onToggle: () -> Unit,
    onRolChange: (RolTipo) -> Unit,
    onDetalleChange: (String) -> Unit,
    tf: TextFieldColors
) {
    val j = asignacion.jugadora
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NavyCard)
            .border(
                1.dp,
                if (asignacion.activa) asignacion.rolTipo.color.copy(0.5f) else NavyBorder,
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Player toggle header ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp))
                        .background(if (asignacion.activa) asignacion.rolTipo.color.copy(0.2f) else NavyElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text("#${j.numero}", color = if (asignacion.activa) asignacion.rolTipo.color else TextTertiary,
                        fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
                Column {
                    Text(j.nombre, color = if (asignacion.activa) TextPrimary else TextSecondary,
                        fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (j.posicion.isNotBlank()) {
                        Text(j.posicion, color = TextTertiary, fontSize = 10.sp)
                    }
                }
            }
            Switch(
                checked = asignacion.activa,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NavyDark,
                    checkedTrackColor = asignacion.rolTipo.color,
                    uncheckedTrackColor = NavyBorder
                )
            )
        }

        // ── Role options (only when active) ────────────────────────────────
        AnimatedVisibility(visible = asignacion.activa) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Role type selector
                Text("TIPO DE ROL", color = TextTertiary, fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RolTipo.entries.forEach { tipo ->
                        val isSelected = asignacion.rolTipo == tipo
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) tipo.color.copy(0.2f) else NavyElevated)
                                .border(1.dp, if (isSelected) tipo.color else NavyBorder, RoundedCornerShape(8.dp))
                                .clickable { onRolChange(tipo) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(tipo.label, color = if (isSelected) tipo.color else TextTertiary,
                                fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal)
                        }
                    }
                }

                // Detail field
                val placeholder = when (asignacion.rolTipo) {
                    RolTipo.MARCAJE -> "Nombre/dorsal jugadora rival a marcar"
                    RolTipo.ZONA -> "Ej: Poste alto, Esquina izq., Zona 5..."
                    RolTipo.TAREA -> "Describe la tarea o responsabilidad..."
                }
                OutlinedTextField(
                    value = asignacion.detalle,
                    onValueChange = onDetalleChange,
                    label = { Text(placeholder, color = TextTertiary, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = tf,
                    maxLines = 3
                )
            }
        }
    }
}
