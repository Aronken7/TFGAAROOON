package com.example.tfg_aaron.ui.screens.partidos

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.example.tfg_aaron.util.NotificationHelper
import com.example.tfg_aaron.util.NotificationScheduler
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPartidoScreen(
    navController: NavController,
    entrenadorId: Int,
    partidoId: Int = -1
) {
    val isTablet = LocalIsTablet.current
    val context = LocalContext.current
    val app = context.applicationContext as TFGApplication
    val viewModel: PartidosViewModel = viewModel(factory = viewModelFactory {
        initializer { PartidosViewModel(entrenadorId, app.partidoRepository) }
    })
    val uiState by viewModel.uiState.collectAsState()
    val notifPartidoEnabled by app.userPreferences.notifPartido.collectAsState(initial = true)

    // Rivals from repository for autocomplete
    val rivalesGuardados by app.rivalPerfilRepository
        .getByEntrenador(entrenadorId)
        .collectAsState(initial = emptyList())

    // Form state
    var rival by remember { mutableStateOf("") }
    var rivalDropdownExpanded by remember { mutableStateOf(false) }
    var lugar by remember { mutableStateOf("") }
    var puntosPropio by remember { mutableStateOf("") }
    var puntosRival by remember { mutableStateOf("") }
    var jornada by remember { mutableStateOf("") }
    var esLocal by remember { mutableStateOf(true) }
    var notas by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Load existing partido if editing
    LaunchedEffect(partidoId, uiState.partidos) {
        if (!initialized && partidoId > 0) {
            uiState.partidos.find { it.id == partidoId }?.let { p ->
                rival = p.rival
                lugar = p.lugar
                puntosPropio = p.puntosPropio.toString()
                puntosRival = p.puntosRival.toString()
                jornada = if (p.jornada > 0) p.jornada.toString() else ""
                esLocal = p.esLocal
                notas = p.notas
                fecha = p.fecha
                initialized = true
            }
        } else if (partidoId <= 0) {
            initialized = true
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) isSaving = false
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            // Schedule a notification reminder if the preference is enabled
            if (notifPartidoEnabled && rival.isNotBlank()) {
                val savedId = uiState.partidos.find { it.rival == rival.trim() && it.fecha == fecha }?.id
                    ?: (if (partidoId > 0) partidoId else -1)
                if (savedId > 0) {
                    NotificationScheduler.schedulePartidoReminder(
                        context = context,
                        partidoId = savedId,
                        rival = rival.trim(),
                        fechaMillis = fecha
                    )
                }
            }
            viewModel.clearMessages()
            navController.popBackStack()
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldAccent,
        unfocusedBorderColor = NavyBorder,
        focusedLabelColor = GoldAccent,
        unfocusedLabelColor = TextSecondary,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = GoldAccent,
        focusedContainerColor = NavyElevated,
        unfocusedContainerColor = NavyElevated
    )

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fecha)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        fecha = datePickerState.selectedDateMillis ?: fecha
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = NavyCard)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = NavyCard,
                    titleContentColor = TextPrimary,
                    headlineContentColor = GoldAccent,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextSecondary,
                    dayContentColor = TextPrimary,
                    selectedDayContainerColor = GoldAccent,
                    todayDateBorderColor = GoldAccent,
                    todayContentColor = GoldAccent
                )
            )
        }
    }

    Scaffold(
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (partidoId > 0) "Editar Partido" else "Nuevo Partido",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(horizontal = if (isTablet) 80.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rival — con autocompletado de rivales guardados
            val rivalesFiltrados = remember(rival, rivalesGuardados) {
                if (rival.isBlank()) rivalesGuardados
                else rivalesGuardados.filter { it.nombre.contains(rival.trim(), ignoreCase = true) }
            }
            Column {
                OutlinedTextField(
                    value = rival,
                    onValueChange = {
                        rival = it
                        rivalDropdownExpanded = true
                    },
                    label = { Text("Equipo rival *") },
                    leadingIcon = { Icon(Icons.Filled.Group, null, tint = GoldAccent) },
                    trailingIcon = {
                        if (rivalesGuardados.isNotEmpty()) {
                            IconButton(onClick = { rivalDropdownExpanded = !rivalDropdownExpanded }) {
                                Icon(
                                    if (rivalDropdownExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    null, tint = TextSecondary
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    singleLine = true
                )
                if (rivalDropdownExpanded && rivalesFiltrados.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyElevated),
                        border = BorderStroke(1.dp, GoldAccent.copy(0.4f))
                    ) {
                        Column {
                            rivalesFiltrados.take(5).forEach { r ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            rival = r.nombre
                                            rivalDropdownExpanded = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GoldAccent.copy(0.15f))
                                    ) {
                                        Icon(Icons.Filled.Groups, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(r.nombre, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (r.sistemaOfensivo.isNotBlank()) {
                                            Text("Ataque: ${r.sistemaOfensivo}", color = TextTertiary, fontSize = 11.sp)
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (r.victorias > 0 || r.derrotas > 0) {
                                            Text("${r.victorias}V-${r.derrotas}D", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                                HorizontalDivider(color = NavyBorder)
                            }
                        }
                    }
                }
            }

            // Lugar
            OutlinedTextField(
                value = lugar,
                onValueChange = { lugar = it },
                label = { Text("Pabellón / Lugar") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, null, tint = TealAccent) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                singleLine = true
            )

            // Date picker button
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, NavyBorder),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = NavyElevated)
            ) {
                Icon(Icons.Filled.DateRange, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Fecha: ${sdf.format(Date(fecha))}", color = TextPrimary)
            }

            // Home/Away toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(true to "LOCAL", false to "VISITANTE").forEach { (local, label) ->
                    val selected = esLocal == local
                    val color = if (local) GreenSuccess else PurpleAccent
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) color.copy(0.2f) else NavyCard)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) color else NavyBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { esLocal = local }
                            .padding(vertical = 14.dp)
                    ) {
                        Text(
                            label,
                            color = if (selected) color else TextSecondary,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Score inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Nuestros puntos", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = puntosPropio,
                        onValueChange = { if (it.length <= 3) puntosPropio = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("0", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorVictoria,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
                Text("–", color = TextTertiary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Puntos rival", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = puntosRival,
                        onValueChange = { if (it.length <= 3) puntosRival = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("0", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorDerrota,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            }

            // Jornada
            OutlinedTextField(
                value = jornada,
                onValueChange = { jornada = it.filter { c -> c.isDigit() } },
                label = { Text("Jornada (opcional)") },
                leadingIcon = { Icon(Icons.Filled.Numbers, null, tint = PurpleAccent) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Notas
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas del partido") },
                leadingIcon = { Icon(Icons.Filled.Notes, null, tint = GoldAccent) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                minLines = 3,
                maxLines = 5
            )

            // Error
            uiState.error?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        it,
                        color = RedError,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
            }

            // Save button
            Button(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        viewModel.savePartido(
                            id = partidoId,
                            rival = rival.trim(),
                            lugar = lugar.trim(),
                            jornada = jornada.toIntOrNull() ?: 0,
                            esLocal = esLocal,
                            notas = notas.trim(),
                            puntosPropio = puntosPropio.toIntOrNull() ?: 0,
                            puntosRival = puntosRival.toIntOrNull() ?: 0,
                            fecha = fecha
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Guardar Partido",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
