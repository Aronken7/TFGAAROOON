package com.example.tfg_aaron.ui.screens.ejercicios

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.tfg_aaron.data.local.entities.EjercicioEntity
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EjerciciosScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: EjerciciosViewModel = viewModel(factory = viewModelFactory {
        initializer { EjerciciosViewModel(entrenadorId, app.ejercicioRepository) }
    })
    val isTablet = LocalIsTablet.current
    val uiState by viewModel.uiState.collectAsState()
    val ejerciciosFiltrados = viewModel.getFilteredEjercicios()
    var deleteTarget by remember { mutableStateOf<EjercicioEntity?>(null) }

    Scaffold(
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Biblioteca de Ejercicios", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${uiState.ejercicios.size} ejercicios", color = TextSecondary, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AddEditEjercicio.createRoute()) }) {
                        Icon(Icons.Filled.Add, "Nuevo ejercicio", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditEjercicio.createRoute()) },
                containerColor = OrangeBase,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Filled.Add, "Nuevo ejercicio", modifier = Modifier.size(22.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search bar ────────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Buscar ejercicios...", color = TextTertiary) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextSecondary) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, null, tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeBase,
                    unfocusedBorderColor = NavyBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = OrangeBase,
                    focusedContainerColor = NavyCard,
                    unfocusedContainerColor = NavyCard
                ),
                singleLine = true
            )

            // ── Category chips ────────────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                items(CATEGORIAS) { cat ->
                    val selected = uiState.selectedCategoria == cat
                    val color = categoriaColor(cat)
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setCategoria(cat) },
                        label = {
                            Text(
                                CATEGORIAS_LABEL[cat] ?: cat,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(0.2f),
                            selectedLabelColor = color,
                            containerColor = NavyCard,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            selectedBorderColor = color.copy(0.5f),
                            borderColor = NavyBorder
                        )
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = NavyBorder)

            // ── Exercise list ─────────────────────────────────────────────────
            if (ejerciciosFiltrados.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.FitnessCenter,
                    title = "Sin ejercicios",
                    subtitle = if (uiState.searchQuery.isNotEmpty() || uiState.selectedCategoria != "TODOS")
                        "No hay resultados con estos filtros"
                    else
                        "Añade ejercicios a tu biblioteca",
                    modifier = Modifier.padding(top = 60.dp)
                )
            } else if (isTablet) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gridItems(ejerciciosFiltrados, key = { it.id }) { ejercicio ->
                        EjercicioCard(
                            ejercicio = ejercicio,
                            onEdit = { navController.navigate(Screen.AddEditEjercicio.createRoute(ejercicio.id)) },
                            onDelete = { deleteTarget = ejercicio },
                            onToggleFavorito = { viewModel.toggleFavorito(ejercicio) }
                        )
                    }
                    item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ejerciciosFiltrados, key = { it.id }) { ejercicio ->
                        EjercicioCard(
                            ejercicio = ejercicio,
                            onEdit = { navController.navigate(Screen.AddEditEjercicio.createRoute(ejercicio.id)) },
                            onDelete = { deleteTarget = ejercicio },
                            onToggleFavorito = { viewModel.toggleFavorito(ejercicio) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        // ── Delete dialog ─────────────────────────────────────────────────────
        deleteTarget?.let { ej ->
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                containerColor = NavyCard,
                shape = RoundedCornerShape(20.dp),
                title = { Text("Eliminar ejercicio", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("¿Eliminar '${ej.nombre}'?", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteEjercicio(ej.id); deleteTarget = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RedError),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Eliminar", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { deleteTarget = null },
                        border = BorderStroke(1.dp, NavyBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancelar", color = TextSecondary) }
                }
            )
        }
    }
}

@Composable
fun EjercicioCard(
    ejercicio: EjercicioEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorito: () -> Unit
) {
    val catColor = categoriaColor(ejercicio.categoria)
    val intensidadColor = when (ejercicio.intensidad) {
        "ALTA" -> RedError
        "MEDIA" -> GoldAccent
        else -> GreenSuccess
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEdit,
                onLongClick = onDelete
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = BorderStroke(1.dp, catColor.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(ejercicio.nombre, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (ejercicio.descripcion.isNotEmpty()) {
                        Spacer(Modifier.height(3.dp))
                        Text(ejercicio.descripcion, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
                    }
                }
                IconButton(
                    onClick = onToggleFavorito,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (ejercicio.esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        null,
                        tint = if (ejercicio.esFavorito) RedError else TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Category chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(catColor.copy(0.15f))
                        .border(1.dp, catColor.copy(0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(CATEGORIAS_LABEL[ejercicio.categoria] ?: ejercicio.categoria, color = catColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Duration
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Timer, null, tint = TextTertiary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("${ejercicio.duracionMinutos} min", color = TextSecondary, fontSize = 11.sp)
                }

                // Intensity
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(intensidadColor.copy(0.12f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        when (ejercicio.intensidad) {
                            "ALTA" -> "Alta"
                            "MEDIA" -> "Media"
                            else -> "Baja"
                        },
                        color = intensidadColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold
                    )
                }

                // Players
                if (ejercicio.jugadoresMinimos > 1) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Group, null, tint = TextTertiary, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("min ${ejercicio.jugadoresMinimos}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

fun categoriaColor(categoria: String): Color = when (categoria) {
    "TIRO" -> TealAccent
    "FINALIZACION" -> OrangeBase
    "TECNICA_INDIVIDUAL" -> PurpleAccent
    "TRANSICION" -> NeonGreen
    "DEFENSA" -> RedError
    "ATAQUE" -> GoldAccent
    "FISICO" -> GreenSuccess
    "CALENTAMIENTO" -> YellowWarning
    "OTRO" -> TextSecondary
    else -> TextTertiary
}
