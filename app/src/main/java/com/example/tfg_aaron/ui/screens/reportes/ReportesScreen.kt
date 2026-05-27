package com.example.tfg_aaron.ui.screens.reportes

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.ReporteEntity
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportesScreen(navController: NavController, entrenadorId: Int) {
    val context = LocalContext.current
    val app = context.applicationContext as TFGApplication
    val viewModel: ReportesViewModel = viewModel(factory = viewModelFactory {
        initializer {
            ReportesViewModel(
                entrenadorId, app.reporteRepository, app.jugadoraRepository,
                app.sesionRepository, app.scoutingRepository,
                app.estadisticaRepository, app.userPreferences
            )
        }
    })
    val isTablet = LocalIsTablet.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.pdfPath) {
        uiState.pdfPath?.let { path ->
            try {
                val file = File(path)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
            } catch (_: Exception) {}
            viewModel.clearMessages()
        }
    }

    Scaffold(
        bottomBar = { CoachBottomNavBar(navController) },
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
                        Text("EXPORTAR", fontSize = 10.sp, color = ElectricBlue, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.5.sp)
                        Text("Reportes PDF", fontSize = 22.sp, color = TextPrimary, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElectricBlue.copy(alpha = 0.15f))
                            .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    ) {
                        Icon(Icons.Filled.Description, null, tint = ElectricBlue, modifier = Modifier.size(22.dp))
                    }
                }
            }
            HorizontalDivider(thickness = 1.dp, color = NavyBorder)

            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isTablet) 2 else 1),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = if (isTablet) Arrangement.spacedBy(10.dp) else Arrangement.Start
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionLabel("GENERAR NUEVO REPORTE", ElectricBlue)
                }

                item {
                    GenerarReporteCard(
                        icon = Icons.Filled.People, title = "Reporte de Jugadoras",
                        subtitle = "Lista completa del equipo con posiciones y roles",
                        color = TealAccent, isLoading = uiState.isGenerating,
                        onClick = { viewModel.generarReporteJugadoras(context) }
                    )
                }
                item {
                    GenerarReporteCard(
                        icon = Icons.Filled.BarChart, title = "Reporte de Estadísticas",
                        subtitle = "Porcentajes de tiros libres por jugadora",
                        color = GoldAccent, isLoading = uiState.isGenerating,
                        onClick = { viewModel.generarReporteEstadisticas(context) }
                    )
                }
                item {
                    GenerarReporteCard(
                        icon = Icons.Filled.CalendarMonth, title = "Reporte de Sesiones",
                        subtitle = "Historial de entrenamientos y completados",
                        color = GoldAccent, isLoading = uiState.isGenerating,
                        onClick = { viewModel.generarReporteSesiones(context) }
                    )
                }
                item {
                    GenerarReporteCard(
                        icon = Icons.Filled.Visibility, title = "Reporte de Scouting",
                        subtitle = "Observaciones de rivales y jugadoras propias",
                        color = PurpleAccent, isLoading = uiState.isGenerating,
                        onClick = { viewModel.generarReporteScouting(context) }
                    )
                }

                if (uiState.reportes.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(Modifier.height(4.dp))
                        SectionLabel("REPORTES GENERADOS (${uiState.reportes.size})", TextSecondary)
                    }
                    gridItems(uiState.reportes, key = { it.id }) { reporte ->
                        ReporteHistorialCard(reporte = reporte, onDelete = { viewModel.deleteReporte(reporte) })
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(80.dp)) }
            }
        }

        LoadingOverlay(uiState.isGenerating)
    }
}

@Composable
fun GenerarReporteCard(
    icon: ImageVector, title: String, subtitle: String,
    color: Color, isLoading: Boolean, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NavyCard)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .clickable(enabled = !isLoading, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(0.18f))
                    .border(1.dp, color.copy(0.3f), RoundedCornerShape(16.dp))
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(3.dp))
                Text(subtitle, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(0.15f))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = color, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.PictureAsPdf, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun ReporteHistorialCard(reporte: ReporteEntity, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val tipoColor = when (reporte.tipo) {
        "JUGADORAS" -> TealAccent
        "ESTADISTICAS" -> GoldAccent
        "SESIONES" -> GoldAccent
        "SCOUTING" -> PurpleAccent
        else -> TextSecondary
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, tipoColor.copy(0.15f), RoundedCornerShape(14.dp))
    ) {
        // Left bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                .background(tipoColor)
        )
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeChip(reporte.tipo, tipoColor)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reporte.titulo, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Filled.CalendarToday, null, tint = TextTertiary, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(sdf.format(Date(reporte.fechaGeneracion)), color = TextTertiary, fontSize = 10.sp)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}
