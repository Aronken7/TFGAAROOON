package com.example.tfg_aaron.ui.screens.jugadoras

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
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
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet

@Composable
fun JugadorasScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: JugadorasViewModel = viewModel(factory = viewModelFactory {
        initializer { JugadorasViewModel(entrenadorId, app.jugadoraRepository, app.estadisticaRepository) }
    })
    val uiState by viewModel.uiState.collectAsState()
    val isTablet = LocalIsTablet.current

    var filterTab by remember { mutableStateOf(0) } // 0=Todas, 1=Disponibles, 2=Activas, 3=Lesionadas
    var showDeleteDialog by remember { mutableStateOf<JugadoraEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredJugadoras = remember(filterTab, searchQuery, uiState.jugadoras) {
        val byFilter = when (filterTab) {
            1 -> uiState.jugadoras.filter { it.condicionFisica == "DISPONIBLE" }
            2 -> uiState.jugadoras.filter { it.activa }
            3 -> uiState.jugadoras.filter { it.condicionFisica == "LESIONADA" }
            else -> uiState.jugadoras
        }
        if (searchQuery.isBlank()) byFilter
        else byFilter.filter {
            "${it.nombre} ${it.apellidos}".contains(searchQuery, ignoreCase = true) ||
            it.numero.toString() == searchQuery.trim()
        }
    }

    val disponiblesCount = uiState.jugadoras.count { it.condicionFisica == "DISPONIBLE" }
    val activasCount = uiState.jugadoras.count { it.activa }
    val lesionadasCount = uiState.jugadoras.count { it.condicionFisica == "LESIONADA" }

    Scaffold(
        bottomBar = { CoachBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditJugadora.createRoute()) },
                containerColor = NeonGreen,
                contentColor = NavyDark,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir jugadora", modifier = Modifier.size(24.dp))
            }
        },
        containerColor = NavyDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Top Bar ─────────────────────────────────────────────────────
            PlayVisionTopBar { navController.navigate(Screen.Perfil.route) }

            // ── Header ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
            ) {
                Text(
                    "PLANTILLA DEL\nEQUIPO",
                    fontSize = 38.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.5).sp,
                    lineHeight = 42.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "TEMPORADA 25/26",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            // ── Search Bar ───────────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar jugadora…", color = TextTertiary) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextTertiary, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = NavyBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonGreen,
                    focusedContainerColor = NavyCard,
                    unfocusedContainerColor = NavyCard
                )
            )

            // ── Filter Tabs ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Todas (${uiState.jugadoras.size})",
                    "Disponibles ($disponiblesCount)",
                    "Activas ($activasCount)",
                    "Lesionadas ($lesionadasCount)"
                ).forEachIndexed { index, label ->
                    val selected = filterTab == index
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) Color.Transparent else Color.Transparent)
                            .border(
                                1.dp,
                                if (selected) NeonGreen else NavyBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { filterTab = index }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) NeonGreen else TextSecondary
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = NavyBorder)

            // ── Content ──────────────────────────────────────────────────────
            if (filteredJugadoras.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(NeonGreen.copy(alpha = 0.08f))
                                .border(1.dp, NeonGreen.copy(0.2f), RoundedCornerShape(16.dp))
                        ) {
                            Icon(Icons.Filled.Groups, null, tint = NeonGreen, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Sin jugadoras", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Añade jugadoras a tu plantilla",
                            color = TextTertiary, fontSize = 13.sp, textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (isTablet) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(filteredJugadoras, key = { it.id }) { jugadora ->
                        Column {
                            PlayerRow(
                                jugadora = jugadora,
                                onClick = { navController.navigate(Screen.JugadoraDetail.createRoute(jugadora.id)) },
                                onDeleteClick = { showDeleteDialog = jugadora }
                            )
                            HorizontalDivider(thickness = 1.dp, color = NavyBorder)
                        }
                    }
                    item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(32.dp)) }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredJugadoras, key = { it.id }) { jugadora ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) showDeleteDialog = jugadora
                                false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                        RedError.copy(0.12f) else Color.Transparent,
                                    label = "swipe_bg"
                                )
                                Box(
                                    modifier = Modifier.fillMaxSize().background(color).padding(end = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Filled.Delete, null, tint = RedError, modifier = Modifier.size(22.dp))
                                }
                            }
                        ) {
                            PlayerRow(
                                jugadora = jugadora,
                                onClick = { navController.navigate(Screen.JugadoraDetail.createRoute(jugadora.id)) }
                            )
                        }
                        HorizontalDivider(thickness = 1.dp, color = NavyBorder)
                    }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }

        // ── Delete Dialog ────────────────────────────────────────────────────
        showDeleteDialog?.let { j ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                containerColor = NavyElevated,
                shape = RoundedCornerShape(16.dp),
                title = { Text("Eliminar jugadora", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("¿Eliminar a ${j.nombre} ${j.apellidos}?", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteJugadora(j); showDeleteDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RedError),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Eliminar", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteDialog = null },
                        border = BorderStroke(1.dp, NavyBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Cancelar", color = TextSecondary) }
                }
            )
        }
    }
}

@Composable
private fun PlayerRow(
    jugadora: JugadoraEntity,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    val posColor = posicionColor(jugadora.posicion)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(NavyCard)
                .border(1.dp, posColor.copy(0.4f), CircleShape)
        ) {
            if (jugadora.fotoUri.isNotBlank()) {
                AsyncImage(
                    model = jugadora.fotoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                val initials = buildString {
                    jugadora.nombre.firstOrNull()?.let { append(it) }
                    jugadora.apellidos.firstOrNull()?.let { append(it) }
                }
                Text(
                    initials.uppercase(),
                    color = posColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${jugadora.nombre} ${jugadora.apellidos}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    "#${jugadora.numero}",
                    color = TextTertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Position badge — solid colored background
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(posColor)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        jugadora.posicion.uppercase(),
                        color = NavyDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                // Role badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NavyBorder)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        jugadora.rol.uppercase(),
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (onDeleteClick != null) {
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.5f), modifier = Modifier.size(18.dp))
            }
        } else {
            Icon(Icons.Filled.MoreVert, null, tint = TextTertiary, modifier = Modifier.size(20.dp))
        }
    }
}
