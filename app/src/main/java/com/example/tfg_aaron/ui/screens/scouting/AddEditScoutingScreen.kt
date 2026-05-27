package com.example.tfg_aaron.ui.screens.scouting

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.ui.components.GradientHeader
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScoutingScreen(navController: NavController, entrenadorId: Int) {
    val isTablet = LocalIsTablet.current
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: ScoutingViewModel = viewModel(factory = viewModelFactory {
        initializer { ScoutingViewModel(entrenadorId, app.scoutingRepository, app.jugadoraRepository) }
    })
    val uiState by viewModel.uiState.collectAsState()

    var tipo by remember { mutableStateOf("RIVAL") }
    var rivalNombre by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("GENERAL") }
    var valoracion by remember { mutableStateOf(3) }
    var selectedJugadora by remember { mutableStateOf(-1) }
    var categoriaExpanded by remember { mutableStateOf(false) }
    var jugadoraExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            navController.popBackStack()
        }
    }

    Scaffold(containerColor = NavyDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            GradientHeader(
                title = "Nuevo Scouting",
                subtitle = "Registra observaciones",
                navController = navController,
                showBack = true
            )

            Column(modifier = Modifier.padding(16.dp).padding(horizontal = if (isTablet) 80.dp else 0.dp)) {

                // Tipo: RIVAL vs PROPIA
                Text("Tipo de scouting", color = TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp, top = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("RIVAL", "PROPIA").forEach { t ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { tipo = t },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (tipo == t) GoldAccent.copy(alpha = 0.15f) else NavyCard
                            ),
                            border = if (tipo == t) CardDefaults.outlinedCardBorder() else null
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    if (t == "RIVAL") Icons.Filled.Groups else Icons.Filled.Person,
                                    null,
                                    tint = if (tipo == t) GoldAccent else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    t,
                                    color = if (tipo == t) GoldAccent else TextSecondary,
                                    fontWeight = if (tipo == t) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Rival name or player selector
                if (tipo == "RIVAL") {
                    OutlinedTextField(
                        value = rivalNombre,
                        onValueChange = { rivalNombre = it },
                        label = { Text("Nombre del rival / equipo") },
                        leadingIcon = { Icon(Icons.Filled.Groups, null, tint = GoldAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent, unfocusedBorderColor = NavyBorder,
                            focusedLabelColor = GoldAccent, unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            cursorColor = GoldAccent, focusedContainerColor = NavyCard, unfocusedContainerColor = NavyCard
                        ),
                        singleLine = true
                    )
                } else {
                    ExposedDropdownMenuBox(expanded = jugadoraExpanded, onExpandedChange = { jugadoraExpanded = it }) {
                        val selectedName = uiState.jugadoras.find { it.id == selectedJugadora }
                            ?.let { "${it.nombre} ${it.apellidos}" } ?: "Seleccionar jugadora"
                        OutlinedTextField(
                            value = selectedName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Jugadora") },
                            leadingIcon = { Icon(Icons.Filled.Person, null, tint = GoldAccent) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jugadoraExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldAccent, unfocusedBorderColor = NavyBorder,
                                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                                focusedContainerColor = NavyCard, unfocusedContainerColor = NavyCard
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = jugadoraExpanded,
                            onDismissRequest = { jugadoraExpanded = false },
                            modifier = Modifier.background(NavyCard)
                        ) {
                            uiState.jugadoras.forEach { j ->
                                DropdownMenuItem(
                                    text = { Text("${j.nombre} ${j.apellidos}", color = TextPrimary) },
                                    onClick = { selectedJugadora = j.id; jugadoraExpanded = false }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Categoría
                Text("Categoría", color = TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.categorias.forEach { cat ->
                        FilterChip(
                            selected = categoria == cat,
                            onClick = { categoria = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PurpleAccent,
                                selectedLabelColor = Color.White,
                                containerColor = NavyCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = categoria == cat,
                                selectedBorderColor = PurpleAccent, borderColor = NavyBorder
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Valoración
                Text("Valoración", color = TextSecondary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(5) { i ->
                        IconButton(onClick = { valoracion = i + 1 }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                if (i < valoracion) Icons.Filled.Star else Icons.Filled.StarBorder,
                                null,
                                tint = GoldAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Observaciones
                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones *") },
                    placeholder = { Text("Describe lo observado...", color = TextTertiary) },
                    leadingIcon = { Icon(Icons.Filled.Notes, null, tint = GoldAccent) },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent, unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = GoldAccent, unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = GoldAccent, focusedContainerColor = NavyCard, unfocusedContainerColor = NavyCard
                    )
                )

                uiState.error?.let { err ->
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RedSurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Error, null, tint = RedError, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(err, color = RedError, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.addScouting(
                            rivalNombre = rivalNombre,
                            observaciones = observaciones,
                            tipo = tipo,
                            categoria = categoria,
                            valoracion = valoracion,
                            idJugadora = if (tipo == "PROPIA" && selectedJugadora > 0) selectedJugadora else null
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Scouting", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
