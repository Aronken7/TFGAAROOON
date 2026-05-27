package com.example.tfg_aaron.ui.screens.asistencia

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet

@Composable
fun AsistenciaScreen(
    navController: NavController,
    entrenadorId: Int,
    tipo: String,
    referenciaId: Int,
    titulo: String,
    fecha: Long
) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: AsistenciaViewModel = viewModel(factory = viewModelFactory {
        initializer {
            AsistenciaViewModel(entrenadorId, tipo, referenciaId, fecha, app.asistenciaRepository, app.jugadoraRepository)
        }
    })
    val isTablet = LocalIsTablet.current
    val state by viewModel.state.collectAsState()

    val presentes = state.asistencias.count { it.estado == "PRESENTE" }
    val ausentes = state.asistencias.count { it.estado == "AUSENTE" }
    val tarde = state.asistencias.count { it.estado == "TARDE" }
    val justificado = state.asistencias.count { it.estado == "JUSTIFICADO" }

    Scaffold(containerColor = NavyDark) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth().background(NavyCard)
                    .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("CONTROL DE ASISTENCIA", color = NeonGreen, fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                            Text(titulo, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    // Summary
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsistenciaSummaryChip("PRESENTES", presentes, GreenSuccess)
                        AsistenciaSummaryChip("TARDE", tarde, GoldAccent)
                        AsistenciaSummaryChip("AUSENTES", ausentes, RedError)
                        AsistenciaSummaryChip("JUSTIF.", justificado, TealAccent)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.initAsistencias() },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyElevated),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Marcar todos PRESENTE", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isTablet) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gridItems(state.jugadoras, key = { it.id }) { jugadora ->
                        val asistencia = state.asistencias.find { it.idJugadora == jugadora.id }
                        val estadoActual = asistencia?.estado ?: "PRESENTE"
                        AsistenciaRow(
                            jugadora = jugadora,
                            estado = estadoActual,
                            onEstadoChange = { viewModel.updateEstado(jugadora.id, it) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.jugadoras, key = { it.id }) { jugadora ->
                        val asistencia = state.asistencias.find { it.idJugadora == jugadora.id }
                        val estadoActual = asistencia?.estado ?: "PRESENTE"
                        AsistenciaRow(
                            jugadora = jugadora,
                            estado = estadoActual,
                            onEstadoChange = { viewModel.updateEstado(jugadora.id, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AsistenciaSummaryChip(label: String, count: Int, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
            .background(color.copy(0.15f))
            .border(1.dp, color.copy(0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$count", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Text(label, color = color.copy(0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AsistenciaRow(
    jugadora: JugadoraEntity,
    estado: String,
    onEstadoChange: (String) -> Unit
) {
    val estadoColor = when (estado) {
        "PRESENTE" -> GreenSuccess
        "TARDE" -> GoldAccent
        "AUSENTE" -> RedError
        "JUSTIFICADO" -> TealAccent
        else -> TextSecondary
    }
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCard)
            .border(1.dp, estadoColor.copy(0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(estadoColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    jugadora.nombre.first().uppercase(),
                    color = estadoColor, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(jugadora.nombre, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(jugadora.posicion, color = TextTertiary, fontSize = 11.sp)
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    .background(estadoColor.copy(0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(estado, color = estadoColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        // Estado buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("PRESENTE" to GreenSuccess, "TARDE" to GoldAccent, "AUSENTE" to RedError, "JUSTIF." to TealAccent)
                .forEachIndexed { idx, (label, color) ->
                    val realEstado = if (label == "JUSTIF.") "JUSTIFICADO" else label
                    val selected = estado == realEstado
                    Box(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) color.copy(0.25f) else NavyElevated)
                            .border(1.dp, if (selected) color else NavyBorder, RoundedCornerShape(6.dp))
                            .clickable { onEstadoChange(realEstado) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label, color = if (selected) color else TextTertiary,
                            fontSize = 9.sp, fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
        }
    }
}
