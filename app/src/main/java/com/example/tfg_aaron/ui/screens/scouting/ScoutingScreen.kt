package com.example.tfg_aaron.ui.screens.scouting

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.ScoutingEntity
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScoutingScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: ScoutingViewModel = viewModel(factory = viewModelFactory {
        initializer { ScoutingViewModel(entrenadorId, app.scoutingRepository, app.jugadoraRepository) }
    })
    val uiState by viewModel.uiState.collectAsState()

    val isTablet = LocalIsTablet.current
    var filterTipo by remember { mutableStateOf("TODOS") }
    var showDeleteDialog by remember { mutableStateOf<ScoutingEntity?>(null) }

    val filtered = uiState.scoutingList.filter {
        filterTipo == "TODOS" || it.tipo == filterTipo
    }

    Scaffold(
        bottomBar = { CoachBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditScouting.createRoute()) },
                containerColor = NeonGreen,
                contentColor = NavyDark,
                shape = androidx.compose.foundation.shape.CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Filled.Add, "Nuevo scouting", modifier = Modifier.size(24.dp))
            }
        },
        containerColor = NavyDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Top Bar ───────────────────────────────────────────────────────
            PlayVisionTopBar { navController.navigate(Screen.Perfil.route) }

            // ── Header ────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    "MÓDULO DE INTELIGENCIA",
                    fontSize = 10.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Informes de Scouting",
                    fontSize = 28.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Análisis detallado de perfiles, tendencias tácticas y evaluaciones de rendimiento.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(14.dp))
                // Stats row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val activos = uiState.scoutingList.size
                    val pendientes = uiState.scoutingList.count { it.tipo == "RIVAL" }
                    StatPill("$activos", "ACTIVOS")
                    StatPill("%02d".format(pendientes), "PENDIENTES")
                }
            }
            HorizontalDivider(thickness = 1.dp, color = NavyBorder)

            // ── Filter chips ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("TODOS" to "Todos", "RIVAL" to "Rival", "PROPIA" to "Propia").forEach { (tipo, label) ->
                    val isSelected = filterTipo == tipo
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isSelected) NeonGreen else NavyBorder, RoundedCornerShape(8.dp))
                            .clickable { filterTipo = tipo }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        if (tipo == "TODOS") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    Icons.Filled.FilterList,
                                    contentDescription = null,
                                    tint = if (isSelected) NeonGreen else TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NeonGreen else TextSecondary
                                )
                            }
                        } else {
                            Text(
                                label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonGreen else TextSecondary
                            )
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Visibility,
                    title = "Sin reportes de scouting",
                    subtitle = "Añade observaciones de rivales o jugadoras",
                    modifier = Modifier.padding(top = 80.dp)
                )
            } else if (isTablet) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gridItems(filtered, key = { it.id }) { scouting ->
                        ScoutingCard(
                            scouting = scouting,
                            jugadoraNombre = viewModel.getJugadoraNombre(scouting.idJugadora),
                            onDelete = { showDeleteDialog = scouting }
                        )
                    }
                    item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { scouting ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) showDeleteDialog = scouting
                                false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                        RedError.copy(0.15f) else Color.Transparent,
                                    label = "swipe_bg"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(color)
                                        .padding(end = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Filled.Delete, null, tint = RedError, modifier = Modifier.size(24.dp))
                                }
                            }
                        ) {
                            ScoutingCard(
                                scouting = scouting,
                                jugadoraNombre = viewModel.getJugadoraNombre(scouting.idJugadora),
                                onDelete = { showDeleteDialog = scouting }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        showDeleteDialog?.let { s ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                containerColor = NavyCard,
                shape = RoundedCornerShape(20.dp),
                title = { Text("Eliminar scouting", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("¿Eliminar este reporte de scouting?", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteScouting(s); showDeleteDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RedError),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Eliminar", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteDialog = null },
                        border = BorderStroke(1.dp, NavyBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancelar", color = TextSecondary) }
                }
            )
        }
    }
}

@Composable
private fun StatPill(value: String, label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text(label, color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun ScoutingCard(
    scouting: ScoutingEntity,
    jugadoraNombre: String,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
    val categoriaColor = when (scouting.categoria) {
        "ATAQUE" -> ColorAtaque
        "DEFENSA" -> ColorDefensa
        "FISICO" -> GreenSuccess
        "MENTAL" -> PurpleAccent
        "TECNICO" -> TealAccent
        else -> TextSecondary
    }
    val tipoColor = if (scouting.tipo == "RIVAL") RedError else GoldAccent
    val displayName = if (scouting.tipo == "RIVAL" && scouting.rivalNombre.isNotEmpty())
        scouting.rivalNombre else jugadoraNombre.ifEmpty { "—" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(18.dp))
    ) {
        Column {
            // ── Banner oscuro ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF060B18), Color(0xFF0C1527))
                        )
                    )
            ) {
                // Badge tipo — esquina superior derecha
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(tipoColor.copy(alpha = 0.15f))
                        .border(1.dp, tipoColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        scouting.tipo,
                        color = tipoColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // ── Contenido ────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                // Nombre + estrellas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            displayName,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            scouting.categoria.lowercase().replaceFirstChar { it.uppercase() },
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                    Row {
                        repeat(5) { i ->
                            Icon(
                                if (i < scouting.valoracion) Icons.Filled.Star else Icons.Filled.StarBorder,
                                null,
                                tint = if (i < scouting.valoracion) NeonGreen else NavyBorder,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    scouting.observaciones,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 3,
                    lineHeight = 18.sp,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(14.dp))

                // Tags + fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BadgeChip(scouting.categoria, categoriaColor)
                    Text(
                        "Actualizado: ${sdf.format(Date(scouting.fecha))}",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
