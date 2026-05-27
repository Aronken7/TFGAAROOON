package com.example.tfg_aaron.ui.screens.ejercicios

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEjercicioScreen(
    navController: NavController,
    entrenadorId: Int,
    ejercicioId: Int = -1
) {
    val isTablet = LocalIsTablet.current
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: EjerciciosViewModel = viewModel(factory = viewModelFactory {
        initializer { EjerciciosViewModel(entrenadorId, app.ejercicioRepository) }
    })
    val uiState by viewModel.uiState.collectAsState()

    // Form state
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("TIRO") }
    var duracion by remember { mutableFloatStateOf(15f) }
    var intensidad by remember { mutableStateOf("MEDIA") }
    var materiales by remember { mutableStateOf("") }
    var jugadoresMinimos by remember { mutableStateOf("1") }
    var notas by remember { mutableStateOf("") }
    var esFavorito by remember { mutableStateOf(false) }
    var showCategoriaMenu by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    // Load existing exercise if editing
    LaunchedEffect(ejercicioId, uiState.ejercicios) {
        if (!initialized && ejercicioId > 0) {
            uiState.ejercicios.find { it.id == ejercicioId }?.let { ej ->
                nombre = ej.nombre
                descripcion = ej.descripcion
                categoria = ej.categoria
                duracion = ej.duracionMinutos.toFloat()
                intensidad = ej.intensidad
                materiales = ej.materialesNecesarios
                jugadoresMinimos = ej.jugadoresMinimos.toString()
                notas = ej.notas
                esFavorito = ej.esFavorito
                initialized = true
            }
        } else if (ejercicioId <= 0) {
            initialized = true
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            navController.popBackStack()
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = OrangeBase,
        unfocusedBorderColor = NavyBorder,
        focusedLabelColor = OrangeBase,
        unfocusedLabelColor = TextSecondary,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = OrangeBase,
        focusedContainerColor = NavyElevated,
        unfocusedContainerColor = NavyElevated
    )

    Scaffold(
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (ejercicioId > 0) "Editar Ejercicio" else "Nuevo Ejercicio",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { esFavorito = !esFavorito }) {
                        Icon(
                            if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            null,
                            tint = if (esFavorito) RedError else TextSecondary
                        )
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
            // ── Nombre ────────────────────────────────────────────────────────
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del ejercicio *") },
                leadingIcon = { Icon(Icons.Filled.FitnessCenter, null, tint = OrangeBase) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                singleLine = true
            )

            // ── Categoría ─────────────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = showCategoriaMenu,
                onExpandedChange = { showCategoriaMenu = !showCategoriaMenu }
            ) {
                OutlinedTextField(
                    value = CATEGORIAS_LABEL[categoria] ?: categoria,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    leadingIcon = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(categoriaColor(categoria).copy(0.2f))
                        ) {
                            Icon(Icons.Filled.Category, null, tint = categoriaColor(categoria), modifier = Modifier.size(14.dp))
                        }
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoriaMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors
                )
                ExposedDropdownMenu(
                    expanded = showCategoriaMenu,
                    onDismissRequest = { showCategoriaMenu = false },
                    modifier = Modifier.background(NavyElevated)
                ) {
                    CATEGORIAS.drop(1).forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(categoriaColor(cat))
                                    )
                                    Text(CATEGORIAS_LABEL[cat] ?: cat, color = TextPrimary)
                                }
                            },
                            onClick = { categoria = cat; showCategoriaMenu = false }
                        )
                    }
                }
            }

            // ── Duración slider ───────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Timer, null, tint = TealAccent, modifier = Modifier.size(18.dp))
                            Text("Duración", color = TextSecondary, fontSize = 13.sp)
                        }
                        Text(
                            "${duracion.roundToInt()} min",
                            color = TealAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Slider(
                        value = duracion,
                        onValueChange = { duracion = it },
                        valueRange = 5f..90f,
                        steps = 16,
                        colors = SliderDefaults.colors(
                            thumbColor = TealAccent,
                            activeTrackColor = TealAccent,
                            inactiveTrackColor = NavyBorder
                        )
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("5 min", color = TextTertiary, fontSize = 10.sp)
                        Text("90 min", color = TextTertiary, fontSize = 10.sp)
                    }
                }
            }

            // ── Intensidad ────────────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Bolt, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Text("Intensidad", color = TextSecondary, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("BAJA", "Baja", GreenSuccess),
                            Triple("MEDIA", "Media", GoldAccent),
                            Triple("ALTA", "Alta", RedError)
                        ).forEach { (value, label, color) ->
                            val selected = intensidad == value
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) color.copy(0.2f) else NavyElevated)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) color else NavyBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { intensidad = value }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    label,
                                    color = if (selected) color else TextSecondary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Descripción ───────────────────────────────────────────────────
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                leadingIcon = { Icon(Icons.Filled.Description, null, tint = OrangeBase) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                minLines = 3,
                maxLines = 5
            )

            // ── Materiales ────────────────────────────────────────────────────
            OutlinedTextField(
                value = materiales,
                onValueChange = { materiales = it },
                label = { Text("Materiales necesarios") },
                leadingIcon = { Icon(Icons.Filled.SportsSoccer, null, tint = TealAccent) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                singleLine = true
            )

            // ── Jugadores mínimos ─────────────────────────────────────────────
            OutlinedTextField(
                value = jugadoresMinimos,
                onValueChange = { jugadoresMinimos = it.filter { c -> c.isDigit() } },
                label = { Text("Jugadoras mínimas") },
                leadingIcon = { Icon(Icons.Filled.Group, null, tint = PurpleAccent) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )

            // ── Notas ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas adicionales") },
                leadingIcon = { Icon(Icons.Filled.Notes, null, tint = GoldAccent) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                minLines = 2,
                maxLines = 4
            )

            // ── Favorito toggle ───────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (esFavorito) RedError.copy(0.4f) else NavyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { esFavorito = !esFavorito }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            null,
                            tint = if (esFavorito) RedError else TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text("Marcar como favorito", color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text("Aparece destacado en la lista", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                    Switch(
                        checked = esFavorito,
                        onCheckedChange = { esFavorito = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = RedError,
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = NavyBorder
                        )
                    )
                }
            }

            // ── Error ─────────────────────────────────────────────────────────
            uiState.error?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(it, color = RedError, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                }
            }

            // ── Save button ───────────────────────────────────────────────────
            Button(
                onClick = {
                    viewModel.saveEjercicio(
                        id = ejercicioId,
                        nombre = nombre.trim(),
                        descripcion = descripcion.trim(),
                        categoria = categoria,
                        duracionMinutos = duracion.roundToInt(),
                        intensidad = intensidad,
                        materialesNecesarios = materiales.trim(),
                        jugadoresMinimos = jugadoresMinimos.toIntOrNull() ?: 1,
                        notas = notas.trim(),
                        esFavorito = esFavorito
                    )
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeBase)
            ) {
                Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (ejercicioId > 0) "Actualizar Ejercicio" else "Guardar Ejercicio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
