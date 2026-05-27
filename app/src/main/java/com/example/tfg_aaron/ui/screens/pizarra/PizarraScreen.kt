package com.example.tfg_aaron.ui.screens.pizarra

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.tfg_aaron.data.local.entities.PizarraJugadaEntity
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PizarraScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: PizarraViewModel = viewModel(factory = viewModelFactory {
        initializer { PizarraViewModel(entrenadorId, app.pizarraRepository) }
    })
    val isTablet = LocalIsTablet.current
    val uiState by viewModel.uiState.collectAsState()
    var filterCategoria by remember { mutableStateOf("TODAS") }
    var showDeleteDialog by remember { mutableStateOf<PizarraJugadaEntity?>(null) }

    val filtered = uiState.jugadas.filter {
        filterCategoria == "TODAS" || it.categoria == filterCategoria
    }

    Scaffold(
        bottomBar = { CoachBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.resetEditor()
                    navController.navigate(Screen.PizarraEditor.createRoute())
                },
                containerColor = NeonGreen,
                contentColor = NavyDark,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Filled.Add, "Nueva jugada", modifier = Modifier.size(24.dp))
            }
        },
        containerColor = NavyDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Top Bar ──────────────────────────────────────────────────────
            PlayVisionTopBar { navController.navigate(Screen.Perfil.route) }

            // ── Header ───────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
            ) {
                Text(
                    "TÁCTICAS",
                    fontSize = 44.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-2).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Jugadas y Estrategias Guardadas",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // ── Filter Chips ─────────────────────────────────────────────────
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categoriaFiltros = listOf("TODAS") + viewModel.categorias
                val categoriaColors = mapOf(
                    "ATAQUE" to ColorAtaque,
                    "DEFENSA" to ColorDefensa,
                    "BLOQUEO" to ColorBloqueo,
                    "TRANSICION" to ColorTransicion
                )
                categoriaFiltros.forEach { cat ->
                    val catColor = categoriaColors[cat] ?: NeonGreen
                    val isSelected = filterCategoria == cat
                    val chipBorderColor = if (cat == "TODAS") NeonGreen else catColor
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected && cat == "TODAS") NeonGreen else Color.Transparent)
                            .border(1.5.dp, chipBorderColor, RoundedCornerShape(8.dp))
                            .clickable { filterCategoria = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            if (cat == "TODAS") "TODOS" else cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected && cat == "TODAS") NavyDark else chipBorderColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Filled.Draw,
                        title = "Sin jugadas guardadas",
                        subtitle = "Crea tu primera jugada táctica"
                    )
                }
            } else if (isTablet) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gridItems(filtered, key = { it.id }) { jugada ->
                        JugadaCard(
                            jugada = jugada,
                            onEdit = { navController.navigate(Screen.PizarraEditor.createRoute(jugada.id)) },
                            onDelete = { showDeleteDialog = jugada }
                        )
                    }
                    item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(100.dp)) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { jugada ->
                        JugadaCard(
                            jugada = jugada,
                            onEdit = { navController.navigate(Screen.PizarraEditor.createRoute(jugada.id)) },
                            onDelete = { showDeleteDialog = jugada }
                        )
                    }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }

        showDeleteDialog?.let { j ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                containerColor = NavyElevated,
                shape = RoundedCornerShape(16.dp),
                title = { Text("Eliminar jugada", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("¿Eliminar '${j.nombreJugada}'?", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteJugada(j); showDeleteDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RedError)
                    ) { Text("Eliminar") }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteDialog = null },
                        border = BorderStroke(1.dp, NavyBorder)
                    ) { Text("Cancelar", color = TextSecondary) }
                }
            )
        }
    }
}

@Composable
fun JugadaCard(
    jugada: PizarraJugadaEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val catColor = when (jugada.categoria) {
        "ATAQUE" -> ColorAtaque
        "DEFENSA" -> ColorDefensa
        "BLOQUEO" -> ColorBloqueo
        "TRANSICION" -> ColorTransicion
        else -> NeonGreen
    }
    val catIcon = when (jugada.categoria) {
        "ATAQUE" -> Icons.Filled.SportsMartialArts
        "DEFENSA" -> Icons.Filled.Shield
        "BLOQUEO" -> Icons.Filled.Block
        else -> Icons.Filled.Bolt
    }
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(10.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onEdit)
    ) {
        // Left accent border
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(catColor)
        )

        Column(modifier = Modifier.weight(1f).padding(14.dp)) {
            // Category badge + ⋮ menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(catIcon, null, tint = catColor, modifier = Modifier.size(14.dp))
                    Text(
                        jugada.categoria,
                        color = catColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.MoreVert, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = NavyElevated
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar", color = TextPrimary, fontSize = 14.sp) },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Filled.Edit, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = RedError, fontSize = 14.sp) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Filled.Delete, null, tint = RedError, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Play name
            Text(
                jugada.nombreJugada,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                letterSpacing = (-0.3).sp
            )

            if (jugada.descripcion.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    jugada.descripcion,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // Date chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NavyElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "Modificado el: ${sdf.format(Date(jugada.ultimaModificacion))}",
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}
