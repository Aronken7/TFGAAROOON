package com.example.tfg_aaron.ui.screens.sesiones

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.SesionEntrenamientoEntity
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*

@Composable
fun SesionesScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: SesionesViewModel = viewModel(factory = viewModelFactory {
        initializer { SesionesViewModel(entrenadorId, app.sesionRepository, app.jugadoraRepository) }
    })
    val uiState by viewModel.uiState.collectAsState()
    val sesionesActuales = viewModel.getSesionesDeSemana()
    val isTablet = LocalIsTablet.current
    var showDeleteDialog by remember { mutableStateOf<SesionEntrenamientoEntity?>(null) }

    val completadas = sesionesActuales.count { it.completada }

    Scaffold(
        bottomBar = { CoachBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditSesion.createRoute()) },
                containerColor = GoldAccent,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Filled.Add, "Nueva sesión", modifier = Modifier.size(22.dp))
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
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "ENTRENAMIENTO",
                            fontSize = 10.sp,
                            color = GoldAccent,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.5.sp
                        )
                        Text(
                            "Sesiones",
                            fontSize = 22.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.3).sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniPillSession("${uiState.sesiones.size}", "total", GoldAccent)
                        if (sesionesActuales.isNotEmpty()) {
                            MiniPillSession("$completadas/${sesionesActuales.size}", "esta sem.", GreenSuccess)
                        }
                    }
                }
            }
            HorizontalDivider(thickness = 1.dp, color = NavyBorder)

            // ── Week navigator ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (uiState.semanaActual > 1) viewModel.setSemana(uiState.semanaActual - 1) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (uiState.semanaActual > 1) GoldAccent.copy(0.1f) else Color.Transparent)
                    ) {
                        Icon(
                            Icons.Filled.ChevronLeft, null,
                            tint = if (uiState.semanaActual > 1) GoldAccent else TextTertiary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Semana ${uiState.semanaActual}",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Text(
                            "${sesionesActuales.size} sesiones planificadas",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setSemana(uiState.semanaActual + 1) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldAccent.copy(0.1f))
                    ) {
                        Icon(Icons.Filled.ChevronRight, null, tint = GoldAccent)
                    }
                }
            }

            // ── Day pills ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.diasSemana.forEach { dia ->
                    val hasSesion = sesionesActuales.any { it.diaSemana == dia }
                    val completada = sesionesActuales.filter { it.diaSemana == dia }.all { it.completada } && hasSesion
                    val dayColor = when {
                        completada -> GreenSuccess
                        hasSesion -> GoldAccent
                        else -> NavyBorder
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(dayColor.copy(alpha = if (hasSesion) 0.1f else 0.05f))
                            .border(1.dp, dayColor.copy(alpha = if (hasSesion) 0.4f else 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                dia.take(2).uppercase(),
                                fontSize = 10.sp,
                                color = when {
                                    completada -> GreenSuccess
                                    hasSesion -> GoldAccent
                                    else -> TextTertiary
                                },
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (hasSesion) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(dayColor)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (sesionesActuales.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.CalendarMonth,
                    title = "Sin sesiones esta semana",
                    subtitle = "Planifica sesiones de entrenamiento",
                    modifier = Modifier.padding(top = 60.dp)
                )
            } else if (isTablet) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gridItems(sesionesActuales, key = { it.id }) { sesion ->
                        SesionCard(
                            sesion = sesion,
                            onToggleComplete = { viewModel.toggleCompletada(sesion) },
                            onDelete = { showDeleteDialog = sesion }
                        )
                    }
                    item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sesionesActuales, key = { it.id }) { sesion ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) showDeleteDialog = sesion
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
                            SesionCard(
                                sesion = sesion,
                                onToggleComplete = { viewModel.toggleCompletada(sesion) },
                                onDelete = { showDeleteDialog = sesion }
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
                title = { Text("Eliminar sesión", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("¿Eliminar '${s.titulo}'?", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteSesion(s); showDeleteDialog = null },
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
private fun MiniPillSession(value: String, label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            Text(label, color = color.copy(0.7f), fontSize = 8.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SesionCard(
    sesion: SesionEntrenamientoEntity,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val diaColor = when (sesion.diaSemana) {
        "Lunes" -> TealAccent
        "Martes" -> GoldAccent
        "Miércoles" -> PurpleAccent
        "Jueves" -> GreenSuccess
        "Viernes" -> GoldAccent
        "Sábado" -> ElectricBlue
        else -> TextSecondary
    }
    val isComplete = sesion.completada

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NavyCard)
            .border(
                1.dp,
                if (isComplete) GreenSuccess.copy(0.3f) else diaColor.copy(0.2f),
                RoundedCornerShape(18.dp)
            )
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
                .background(if (isComplete) GreenSuccess else diaColor)
        )

        Row(
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Day box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(diaColor.copy(alpha = 0.1f))
                        .border(1.dp, diaColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Text(
                        sesion.diaSemana.take(2).uppercase(),
                        color = diaColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(sesion.horaInicio, color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        sesion.titulo,
                        color = if (isComplete) GreenSuccess else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleComplete, modifier = Modifier.size(30.dp)) {
                            Icon(
                                if (isComplete) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                null,
                                tint = if (isComplete) GreenSuccess else TextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.7f), modifier = Modifier.size(15.dp))
                        }
                    }
                }

                if (sesion.descripcion.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(sesion.descripcion, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
                }
                if (sesion.lugar.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, tint = TextTertiary, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(sesion.lugar, color = TextTertiary, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BadgeChip("${sesion.horaInicio}–${sesion.horaFin}", TealAccent)
                    if (isComplete) BadgeChip("Completada", GreenSuccess)
                }
            }
        }
    }
}
