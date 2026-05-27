package com.example.tfg_aaron.ui.screens.partidos

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_aaron.ui.theme.*

@Composable
fun AsistenteTab(state: PartidoEnVivoUiState) {
    val alertas = state.alertasAsistente
    val metricas = state.metricasAvanzadas
    val parcialVivo = state.parcialVivo

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Live Partial Card ──
        item {
            ParcialVivoCard(parcialVivo, state.puntosPropio, state.puntosRival)
        }

        // ── Efficiency Ranking ──
        item {
            EfficiencyRankingCard(state)
        }

        // ── Alerts Header ──
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Filled.Psychology, null, tint = PurpleAccent, modifier = Modifier.size(18.dp))
                Text(
                    "RECOMENDACIONES DEL ASISTENTE",
                    fontSize = 10.sp, color = PurpleAccent,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = NavyBorder)
            }
        }

        if (alertas.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = GreenSuccess, modifier = Modifier.size(40.dp))
                        Text("Todo bajo control", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "No hay alertas ni recomendaciones en este momento.",
                            color = TextTertiary, fontSize = 12.sp, textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(alertas, key = { it.id }) { alerta ->
                AlertaCard(alerta)
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ParcialVivoCard(parcialVivo: Pair<Int, Int>, puntosPropio: Int, puntosRival: Int) {
    val diff = puntosPropio - puntosRival
    val diffText = when {
        diff > 0 -> "+$diff"
        diff < 0 -> "$diff"
        else -> "0"
    }
    val diffColor = when {
        diff > 0 -> GreenSuccess
        diff < 0 -> RedError
        else -> TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(
            modifier = Modifier
                .background(NavyCard)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GoldAccent))
                Text("PARCIAL EN VIVO", color = GoldAccent, fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp, letterSpacing = 1.5.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Our partial
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NOS.", color = TealAccent, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Text("${parcialVivo.first}", color = TealAccent, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
                }

                Text("-", color = TextTertiary, fontSize = 24.sp)

                // Rival partial
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("RIVAL", color = RedError, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Text("${parcialVivo.second}", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
                }

                // Divider
                Box(
                    modifier = Modifier.width(1.dp).height(40.dp).background(NavyBorder)
                )

                // Differential
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DIF.", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Text(diffText, color = diffColor, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
private fun EfficiencyRankingCard(state: PartidoEnVivoUiState) {
    val sortedByVal = state.jugadoras
        .filter { state.metricasAvanzadas.containsKey(it.id) }
        .sortedByDescending { state.metricasAvanzadas[it.id]?.valoracion ?: 0 }
        .take(5)

    if (sortedByVal.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TealAccent))
                Text("RANKING EFICIENCIA (VAL)", color = TealAccent, fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp, letterSpacing = 1.5.sp)
            }

            // Header
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyElevated)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("", modifier = Modifier.width(22.dp))
                Text("#", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
                Text("JUGADORA", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("VAL", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp))
                Text("MIN", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp))
                Text("+/-", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp))
                Text("PTS", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp))
            }

            sortedByVal.forEachIndexed { index, j ->
                val m = state.metricasAvanzadas[j.id] ?: MetricasAvanzadas()
                val st = state.stats[j.id] ?: EstadisticasJugadora()
                val isOnCourt = j.id in state.enCancha
                val medalColor = when (index) {
                    0 -> GoldAccent
                    1 -> Color(0xFFC0C0C0) // silver
                    2 -> Color(0xFFCD7F32) // bronze
                    else -> TextTertiary
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isOnCourt) TealAccent.copy(0.05f) else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Medal/rank
                    Text(
                        "${index + 1}",
                        color = medalColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.width(22.dp)
                    )
                    // Number
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(24.dp).clip(CircleShape)
                            .background(if (isOnCourt) TealAccent.copy(0.15f) else NavyElevated)
                    ) {
                        Text("#${j.numero}", color = if (isOnCourt) TealAccent else TextTertiary,
                            fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.width(6.dp))
                    // Name
                    Column(modifier = Modifier.weight(1f)) {
                        Text(j.nombre, color = TextPrimary, fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                    // VAL
                    Text(
                        "${m.valoracion}",
                        color = if (m.valoracion > 0) GreenSuccess else if (m.valoracion < 0) RedError else TextSecondary,
                        fontWeight = FontWeight.ExtraBold, fontSize = 12.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.width(36.dp)
                    )
                    // MIN
                    Text(
                        "${m.minutosEnCancha.toInt()}",
                        color = if (m.minutosEnCancha >= 25) YellowWarning else TextSecondary,
                        fontWeight = FontWeight.Bold, fontSize = 12.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.width(36.dp)
                    )
                    // +/-
                    val pmText = if (m.plusMinus >= 0) "+${m.plusMinus}" else "${m.plusMinus}"
                    Text(
                        pmText,
                        color = if (m.plusMinus > 0) GreenSuccess else if (m.plusMinus < 0) RedError else TextSecondary,
                        fontWeight = FontWeight.Bold, fontSize = 12.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.width(36.dp)
                    )
                    // PTS
                    Text(
                        "${st.puntos}",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold, fontSize = 12.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertaCard(alerta: AlertaAsistente) {
    val (bgColor, borderColor, iconColor, icon) = when (alerta.prioridad) {
        AlertaPrioridad.CRITICA -> listOf(RedError.copy(0.08f), RedError.copy(0.4f), RedError, Icons.Filled.Error as Any)
        AlertaPrioridad.ALTA -> listOf(YellowWarning.copy(0.08f), YellowWarning.copy(0.3f), YellowWarning, Icons.Filled.Warning as Any)
        AlertaPrioridad.MEDIA -> listOf(OrangeBase.copy(0.06f), OrangeBase.copy(0.25f), OrangeBase, Icons.Filled.Info as Any)
        AlertaPrioridad.BAJA -> listOf(TealAccent.copy(0.05f), TealAccent.copy(0.2f), TealAccent, Icons.Filled.LightbulbCircle as Any)
    }

    val catIcon = when (alerta.categoria) {
        AlertaCategoria.FALTAS -> Icons.Filled.Warning
        AlertaCategoria.RENDIMIENTO -> Icons.Filled.TrendingDown
        AlertaCategoria.TACTICA -> Icons.Filled.Psychology
        AlertaCategoria.TIEMPO -> Icons.Filled.Timer
        AlertaCategoria.RACHA -> Icons.Filled.Whatshot
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor as Color),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor as Color)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Priority icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background((iconColor as Color).copy(0.15f))
            ) {
                Icon(catIcon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        alerta.titulo,
                        color = iconColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    // Priority badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background((iconColor).copy(0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            when (alerta.prioridad) {
                                AlertaPrioridad.CRITICA -> "URGENTE"
                                AlertaPrioridad.ALTA -> "ALTO"
                                AlertaPrioridad.MEDIA -> "MEDIO"
                                AlertaPrioridad.BAJA -> "INFO"
                            },
                            color = iconColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Text(
                    alerta.mensaje,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
