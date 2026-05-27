package com.example.tfg_aaron.ui.screens.busqueda

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusquedaScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: BusquedaViewModel = viewModel(factory = viewModelFactory {
        initializer {
            BusquedaViewModel(
                entrenadorId,
                app.jugadoraRepository,
                app.partidoRepository,
                app.sesionRepository,
                app.scoutingRepository
            )
        }
    })
    val uiState by viewModel.uiState.collectAsState()
    var localQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = NavyDark,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyCard)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "BÚSQUEDA",
                            color = NeonGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Búsqueda global",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search Bar ──────────────────────────────────────────────────
            BusquedaSearchBar(
                query = localQuery,
                onQueryChange = { newQuery ->
                    localQuery = newQuery
                    viewModel.search(newQuery)
                },
                onClear = {
                    localQuery = ""
                    viewModel.clearSearch()
                }
            )

            // ── Content ────────────────────────────────────────────────────
            if (!uiState.hasSearched && localQuery.isBlank()) {
                // Initial state — suggestion chips
                BusquedaInitialState { suggestion ->
                    localQuery = suggestion
                    viewModel.search(suggestion)
                }
            } else if (uiState.hasSearched && uiState.results.isEmpty()) {
                // Empty results state
                BusquedaEmptyState(query = uiState.query)
            } else if (uiState.results.isNotEmpty()) {
                // Results grouped by category
                BusquedaResultsList(
                    results = uiState.results,
                    navController = navController
                )
            }
        }
    }
}

// ── Search Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun BusquedaSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavySurface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Buscar jugadoras, partidos, sesiones...",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = TealAccent
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Borrar búsqueda",
                            tint = TextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = TealAccent,
                unfocusedBorderColor = NavyBorder,
                cursorColor = TealAccent,
                focusedContainerColor = NavyCard,
                unfocusedContainerColor = NavyCard
            )
        )
    }
}

// ── Initial State (suggestion chips) ───────────────────────────────────────────

@Composable
private fun BusquedaInitialState(onChipClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(24.dp))
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Busca en toda la app",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Escribe para buscar o selecciona una categoría",
            color = TextSecondary,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(32.dp))

        // Suggestion chips
        val suggestions = listOf(
            "Jugadoras" to OrangeBase,
            "Partidos" to TealAccent,
            "Sesiones" to NeonGreen,
            "Scouting" to GoldAccent
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            suggestions.take(2).forEach { (label, color) ->
                SuggestionChipItem(
                    label = label,
                    color = color,
                    modifier = Modifier.weight(1f),
                    onClick = { onChipClick(label) }
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            suggestions.drop(2).forEach { (label, color) ->
                SuggestionChipItem(
                    label = label,
                    color = color,
                    modifier = Modifier.weight(1f),
                    onClick = { onChipClick(label) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionChipItem(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NavyCard)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Empty State ─────────────────────────────────────────────────────────────────

@Composable
private fun BusquedaEmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(40.dp))
        Icon(
            Icons.Filled.SearchOff,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Sin resultados para '$query'",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Prueba con otro término de búsqueda",
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
}

// ── Results List ────────────────────────────────────────────────────────────────

@Composable
private fun BusquedaResultsList(
    results: List<SearchResult>,
    navController: NavController
) {
    val jugadoras = results.filterIsInstance<SearchResult.Jugadora>()
    val partidos = results.filterIsInstance<SearchResult.Partido>()
    val sesiones = results.filterIsInstance<SearchResult.Sesion>()
    val scoutings = results.filterIsInstance<SearchResult.Scouting>()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Jugadoras section ──────────────────────────────────────────────
        if (jugadoras.isNotEmpty()) {
            item {
                ResultCategoryHeader(
                    title = "JUGADORAS",
                    count = jugadoras.size,
                    icon = Icons.Filled.Person,
                    color = OrangeBase
                )
            }
            items(jugadoras) { result ->
                JugadoraResultRow(result = result) {
                    navController.navigate(Screen.JugadoraDetail.createRoute(result.entity.id))
                }
            }
        }

        // ── Partidos section ───────────────────────────────────────────────
        if (partidos.isNotEmpty()) {
            item {
                ResultCategoryHeader(
                    title = "PARTIDOS",
                    count = partidos.size,
                    icon = Icons.Filled.SportsSoccer,
                    color = TealAccent
                )
            }
            items(partidos) { result ->
                PartidoResultRow(result = result) {
                    navController.navigate(Screen.Partidos.route)
                }
            }
        }

        // ── Sesiones section ───────────────────────────────────────────────
        if (sesiones.isNotEmpty()) {
            item {
                ResultCategoryHeader(
                    title = "SESIONES",
                    count = sesiones.size,
                    icon = Icons.Filled.FitnessCenter,
                    color = NeonGreen
                )
            }
            items(sesiones) { result ->
                SesionResultRow(result = result) {
                    navController.navigate(Screen.Sesiones.route)
                }
            }
        }

        // ── Scouting section ───────────────────────────────────────────────
        if (scoutings.isNotEmpty()) {
            item {
                ResultCategoryHeader(
                    title = "SCOUTING",
                    count = scoutings.size,
                    icon = Icons.Filled.Visibility,
                    color = GoldAccent
                )
            }
            items(scoutings) { result ->
                ScoutingResultRow(result = result) {
                    navController.navigate(Screen.Scouting.route)
                }
            }
        }
    }
}

// ── Category header ─────────────────────────────────────────────────────────────

@Composable
private fun ResultCategoryHeader(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$title ($count)",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp
        )
        Spacer(Modifier.weight(1f))
        HorizontalDivider(
            modifier = Modifier.width(60.dp),
            color = color.copy(alpha = 0.3f),
            thickness = 1.dp
        )
    }
}

// ── Individual result rows ──────────────────────────────────────────────────────

@Composable
private fun JugadoraResultRow(result: SearchResult.Jugadora, onClick: () -> Unit) {
    val jugadora = result.entity
    ResultRow(
        icon = Icons.Filled.Person,
        iconColor = OrangeBase,
        title = "${jugadora.nombre} ${jugadora.apellidos}",
        subtitle = "${jugadora.posicion} · ${jugadora.rol} · #${jugadora.numero}",
        onClick = onClick
    )
}

@Composable
private fun PartidoResultRow(result: SearchResult.Partido, onClick: () -> Unit) {
    val partido = result.entity
    val dateStr = remember(partido.fecha) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(partido.fecha))
    }
    val localVisitante = if (partido.esLocal) "Local" else "Visitante"
    ResultRow(
        icon = Icons.Filled.SportsSoccer,
        iconColor = TealAccent,
        title = "vs ${partido.rival}",
        subtitle = "$dateStr · $localVisitante · ${partido.puntosPropio}-${partido.puntosRival}",
        onClick = onClick
    )
}

@Composable
private fun SesionResultRow(result: SearchResult.Sesion, onClick: () -> Unit) {
    val sesion = result.entity
    ResultRow(
        icon = Icons.Filled.FitnessCenter,
        iconColor = NeonGreen,
        title = sesion.titulo,
        subtitle = "${sesion.diaSemana} · ${sesion.horaInicio} – ${sesion.horaFin}",
        onClick = onClick
    )
}

@Composable
private fun ScoutingResultRow(result: SearchResult.Scouting, onClick: () -> Unit) {
    val scouting = result.entity
    val nameDisplay = if (scouting.rivalNombre.isNotBlank()) scouting.rivalNombre else "Sin nombre"
    ResultRow(
        icon = Icons.Filled.Visibility,
        iconColor = GoldAccent,
        title = nameDisplay,
        subtitle = "${scouting.tipo} · ${scouting.categoria} · Valoración: ${scouting.valoracion}/5",
        onClick = onClick
    )
}

// ── Generic result row ──────────────────────────────────────────────────────────

@Composable
private fun ResultRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
        color = NavyBorder,
        thickness = 0.5.dp
    )
}
