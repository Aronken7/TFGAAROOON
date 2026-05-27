package com.example.tfg_aaron.ui.screens.calendario

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import com.example.tfg_aaron.data.local.entities.SesionEntrenamientoEntity
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.navigation.Screen
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarioScreen(navController: NavController, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: CalendarioViewModel = viewModel(factory = viewModelFactory {
        initializer {
            CalendarioViewModel(entrenadorId, app.partidoRepository, app.sesionRepository)
        }
    })
    val uiState by viewModel.uiState.collectAsState()
    val isTablet = LocalIsTablet.current

    Scaffold(
        bottomBar = { CoachBottomNavBar(navController) },
        containerColor = NavyDark
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isTablet) 2 else 1),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PlayVisionTopBar { navController.navigate(Screen.Perfil.route) }
            }

            // Header: mes/año + navegación
            item(span = { GridItemSpan(maxLineSpan) }) {
                CalendarioHeader(
                    mes = uiState.mesActual,
                    anio = uiState.anioActual,
                    partidosGanados = uiState.partidosGanados,
                    partidosPerdidos = uiState.partidosPerdidos,
                    sesiones = uiState.sesionesDelMes,
                    onPrev = { viewModel.navegarMes(-1) },
                    onNext = { viewModel.navegarMes(1) }
                )
            }

            // Leyenda
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LeyendaItem(NeonGreen, "Partido ganado")
                    LeyendaItem(RedError, "Partido perdido")
                    LeyendaItem(TealAccent, "Entrenamiento")
                }
            }

            // Calendario grid
            item(span = { GridItemSpan(maxLineSpan) }) {
                CalendarioGrid(
                    mes = uiState.mesActual,
                    anio = uiState.anioActual,
                    eventosPorDia = uiState.eventosPorDia,
                    diaSeleccionado = uiState.diaSeleccionado,
                    onDiaClick = { dia -> viewModel.seleccionarDia(if (uiState.diaSeleccionado == dia) null else dia) }
                )
            }

            // Eventos del día seleccionado
            val diaSelected = uiState.diaSeleccionado
            if (diaSelected != null) {
                val eventos = uiState.eventosPorDia[diaSelected] ?: emptyList()
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            "EVENTOS DEL DÍA $diaSelected",
                            color = NeonGreen, fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        if (eventos.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NavyCard)
                                    .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Sin eventos este día", color = TextTertiary, fontSize = 13.sp)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                eventos.forEach { evento ->
                                    EventoDelDiaCard(evento)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarioHeader(
    mes: Int,
    anio: Int,
    partidosGanados: Int,
    partidosPerdidos: Int,
    sesiones: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val nombresMes = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Mes/Año + nav
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(NavyCard)
                .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 8.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPrev) {
                    Icon(Icons.Filled.ChevronLeft, null, tint = TextSecondary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        nombresMes[mes].uppercase(),
                        color = NeonGreen, fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp, letterSpacing = 1.sp
                    )
                    Text("$anio", color = TextTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Filled.ChevronRight, null, tint = TextSecondary)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Resumen del mes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MesStatCard(
                label = "VICTORIAS",
                value = "$partidosGanados",
                color = NeonGreen,
                modifier = Modifier.weight(1f)
            )
            MesStatCard(
                label = "DERROTAS",
                value = "$partidosPerdidos",
                color = RedError,
                modifier = Modifier.weight(1f)
            )
            MesStatCard(
                label = "ENTRENOS",
                value = "$sesiones",
                color = TealAccent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MesStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(0.08f))
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
        Text(label, color = TextTertiary, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun LeyendaItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, color = TextTertiary, fontSize = 10.sp)
    }
}

@Composable
private fun CalendarioGrid(
    mes: Int,
    anio: Int,
    eventosPorDia: Map<Int, List<CalendarioEvento>>,
    diaSeleccionado: Int?,
    onDiaClick: (Int) -> Unit
) {
    val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")

    // Calcular primer día del mes y total de días
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, anio)
        set(Calendar.MONTH, mes)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    // Ajustar: domingo = 7, lunes = 1
    val primerDiaSemana = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7) // 0=Lunes
    val totalDias = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        // Cabecera días semana
        Row(modifier = Modifier.fillMaxWidth()) {
            diasSemana.forEach { dia ->
                Text(
                    dia,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // Construir semanas
        val totalCeldas = primerDiaSemana + totalDias
        val semanas = (totalCeldas + 6) / 7

        repeat(semanas) { semana ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                repeat(7) { columna ->
                    val celda = semana * 7 + columna
                    val dia = celda - primerDiaSemana + 1
                    val esValido = dia in 1..totalDias
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (esValido) {
                            DiaCelda(
                                dia = dia,
                                eventos = eventosPorDia[dia] ?: emptyList(),
                                isSelected = diaSeleccionado == dia,
                                onClick = { onDiaClick(dia) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaCelda(
    dia: Int,
    eventos: List<CalendarioEvento>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val hoy = Calendar.getInstance()
    val esHoy = dia == hoy.get(Calendar.DAY_OF_MONTH)

    val tienePartidoGanado = eventos.any { it is CalendarioEvento.Partido && (it.entity.puntosPropio > it.entity.puntosRival) }
    val tienePartidoPerdido = eventos.any { it is CalendarioEvento.Partido && (it.entity.puntosPropio <= it.entity.puntosRival) }
    val tieneSesion = eventos.any { it is CalendarioEvento.Sesion }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> NeonGreen.copy(0.2f)
                    esHoy -> NeonGreen.copy(0.08f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isSelected) 1.5.dp else if (esHoy) 1.dp else 0.dp,
                color = if (isSelected) NeonGreen else if (esHoy) NeonGreen.copy(0.4f) else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                "$dia",
                color = when {
                    isSelected -> NeonGreen
                    esHoy -> NeonGreen
                    eventos.isNotEmpty() -> TextPrimary
                    else -> TextTertiary
                },
                fontWeight = if (isSelected || esHoy) FontWeight.ExtraBold else FontWeight.Normal,
                fontSize = 12.sp
            )
            // Badges
            if (tienePartidoGanado || tienePartidoPerdido || tieneSesion) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (tienePartidoGanado) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(NeonGreen)
                        )
                    }
                    if (tienePartidoPerdido) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(RedError)
                        )
                    }
                    if (tieneSesion) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(TealAccent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventoDelDiaCard(evento: CalendarioEvento) {
    when (evento) {
        is CalendarioEvento.Partido -> {
            val partido = evento.entity
            val isWin = partido.puntosPropio > partido.puntosRival
            val color = if (isWin) NeonGreen else RedError
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(0.08f))
                    .border(1.dp, color.copy(0.25f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color.copy(0.15f))
                    ) {
                        Icon(Icons.Filled.SportsBasketball, null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Partido vs ${partido.rival}", color = TextPrimary,
                            fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "${partido.puntosPropio} - ${partido.puntosRival}  •  ${if (isWin) "VICTORIA" else "DERROTA"}",
                            color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    BadgeChip(if (isWin) "W" else "L", color)
                }
            }
        }
        is CalendarioEvento.Sesion -> {
            val sesion = evento.entity
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TealAccent.copy(0.08f))
                    .border(1.dp, TealAccent.copy(0.25f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TealAccent.copy(0.15f))
                    ) {
                        Icon(Icons.Filled.FitnessCenter, null, tint = TealAccent, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sesion.titulo, color = TextPrimary,
                            fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "${sesion.horaInicio} - ${sesion.horaFin}  •  ${sesion.lugar.ifEmpty { "Sin lugar" }}",
                            color = TextSecondary, fontSize = 11.sp
                        )
                    }
                    BadgeChip("ENTRENO", TealAccent)
                }
            }
        }
    }
}
