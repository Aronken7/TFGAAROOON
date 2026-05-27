package com.example.tfg_aaron.ui.screens.estadisticasavanzadas

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet

@Composable
fun EstadisticasAvanzadasScreen(navController: NavController, entrenadorId: Int) {
    val app = LocalContext.current.applicationContext as TFGApplication
    val viewModel: EstadisticasAvanzadasViewModel = viewModel(factory = viewModelFactory {
        initializer {
            EstadisticasAvanzadasViewModel(
                entrenadorId,
                app.jugadoraRepository,
                app.partidoRepository
            )
        }
    })
    val isTablet = LocalIsTablet.current
    val state by viewModel.state.collectAsState()
    val sortedStats = viewModel.getSortedStats()
    val netRtg = state.teamOrtg - state.teamDrtg

    Scaffold(containerColor = NavyDark) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isTablet) 2 else 1),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = if (isTablet) Arrangement.spacedBy(10.dp) else Arrangement.Start
        ) {
            // ── Header ────────────────────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyCard)
                        .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "ESTADÍSTICAS AVANZADAS",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                "Métricas profesionales",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // ── Team metrics ──────────────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "MÉTRICAS DE EQUIPO",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeamMetricCard(
                        titulo = "ORTG",
                        valor = "%.1f".format(state.teamOrtg),
                        color = NeonGreen,
                        subtitulo = "pts/partido",
                        modifier = Modifier.weight(1f)
                    )
                    TeamMetricCard(
                        titulo = "DRTG",
                        valor = "%.1f".format(state.teamDrtg),
                        color = RedError,
                        subtitulo = "pts/partido",
                        modifier = Modifier.weight(1f)
                    )
                    TeamMetricCard(
                        titulo = "NET RTG",
                        valor = "%+.1f".format(netRtg),
                        color = if (netRtg >= 0) NeonGreen else RedError,
                        subtitulo = "diferencial",
                        modifier = Modifier.weight(1f)
                    )
                    TeamMetricCard(
                        titulo = "RITMO",
                        valor = "%.0f".format(state.teamPace),
                        color = TealAccent,
                        subtitulo = "pts/pos.",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Sort chips ────────────────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "ORDENAR POR",
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("PTS", "REB", "AST", "TS%", "eFG%", "AST/TO")) { campo ->
                        SortChip(
                            label = campo,
                            selected = state.sortBy == campo,
                            onClick = { viewModel.setSortBy(campo) }
                        )
                    }
                }
            }

            // ── Column header ─────────────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "JUGADORA",
                        color = TextTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(2f)
                    )
                    listOf("PTS", "REB", "AST", "MIN", "TS%", "AST/TO").forEach { h ->
                        Text(
                            h,
                            color = TextTertiary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Empty state ───────────────────────────────────────────────────
            if (sortedStats.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    if (state.isLoading) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NeonGreen)
                        }
                    } else {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Sin datos disponibles",
                                    color = TextSecondary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Registra partidos con estadísticas para ver las métricas avanzadas",
                                    color = TextTertiary,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // ── Player rows ───────────────────────────────────────────────
                gridItems(sortedStats, key = { it.jugadora.id }) { s ->
                    PlayerAdvancedRow(s)
                }
            }

            // ── Legend ────────────────────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("LEYENDA:", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    LegendDot(NeonGreen, "Alto")
                    LegendDot(GoldAccent, "Medio")
                    LegendDot(RedError, "Bajo")
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Text(label, color = TextTertiary, fontSize = 9.sp)
    }
}

@Composable
private fun TeamMetricCard(
    titulo: String,
    valor: String,
    color: Color,
    subtitulo: String,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            titulo,
            color = TextTertiary,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            valor,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            subtitulo,
            color = TextTertiary,
            fontSize = 8.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) NeonGreen.copy(alpha = 0.2f) else NavyCard)
            .border(1.dp, if (selected) NeonGreen else NavyBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) NeonGreen else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal
        )
    }
}

@Composable
private fun PlayerAdvancedRow(s: JugadoraAvanzadaStats) {
    val tsColor = when {
        s.tsFg >= 0.60 -> NeonGreen
        s.tsFg >= 0.45 -> GoldAccent
        else           -> RedError
    }
    val astToColor = when {
        s.astToRatio >= 2.5 -> NeonGreen
        s.astToRatio >= 1.5 -> GoldAccent
        else                -> RedError
    }
    val ptsColor = when {
        s.puntosPromedio >= 15.0 -> NeonGreen
        s.puntosPromedio >= 8.0  -> GoldAccent
        else                     -> TextSecondary
    }
    val rebColor = when {
        s.rebotesPromedio >= 8.0 -> NeonGreen
        s.rebotesPromedio >= 4.0 -> GoldAccent
        else                     -> TextSecondary
    }
    val astColor = when {
        s.asistenciasPromedio >= 6.0 -> NeonGreen
        s.asistenciasPromedio >= 3.0 -> GoldAccent
        else                         -> TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                s.jugadora.nombre.split(" ").first(),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                "${s.jugadora.posicion} · ${s.partidos}PJ",
                color = TextTertiary,
                fontSize = 9.sp
            )
        }
        StatVal("%.1f".format(s.puntosPromedio), ptsColor)
        StatVal("%.1f".format(s.rebotesPromedio), rebColor)
        StatVal("%.1f".format(s.asistenciasPromedio), astColor)
        StatVal("%.0f".format(s.minutosPromedio), TextSecondary)
        StatVal("${(s.tsFg * 100).toInt()}%", tsColor)
        StatVal("%.1f".format(s.astToRatio), astToColor)
    }
}

@Composable
private fun RowScope.StatVal(valor: String, color: Color) {
    Text(
        valor,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.weight(1f),
        textAlign = TextAlign.Center
    )
}
