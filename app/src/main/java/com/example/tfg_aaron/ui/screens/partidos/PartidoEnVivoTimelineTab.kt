package com.example.tfg_aaron.ui.screens.partidos

import androidx.compose.foundation.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_aaron.data.local.entities.EventoTipo
import com.example.tfg_aaron.ui.theme.*

@Composable
fun TimelineTab(state: PartidoEnVivoUiState) {
    if (state.timelineItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                        .background(TealAccent.copy(0.08f)).border(1.dp, TealAccent.copy(0.2f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Timeline, null, tint = TealAccent.copy(0.5f), modifier = Modifier.size(36.dp))
                }
                Text("Timeline vacío", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("El historial de eventos aparecerá aquí durante el partido",
                    color = TextTertiary, fontSize = 12.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp))
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Score header card
        item {
            val partido = state.partido
            if (partido != null) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NavyCard).border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("EN VIVO", fontSize = 8.sp, color = OrangeBase, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                            Text("vs ${partido.rival}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                .background(OrangeBase.copy(0.08f)).border(1.dp, OrangeBase.copy(0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "${state.puntosPropio} — ${state.puntosRival}",
                                color = OrangeBase, fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp, fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Group by quarter
        val byQuarter = state.timelineItems.groupBy { it.cuarto }
        val quarters = byQuarter.keys.sorted()

        quarters.forEach { cuarto ->
            val items = byQuarter[cuarto] ?: emptyList()

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            .background(NavyElevated).border(1.dp, OrangeBase.copy(0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            cuartoLabel(cuarto).uppercase(),
                            color = OrangeBase, fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                        )
                    }
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(NavyBorder))
                }
            }

            items(items) { item ->
                TimelineEventCard(item = item)
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun TimelineEventCard(item: TimelineItem) {
    if (item.isQuarterBoundary && item.tipo != EventoTipo.QUINTETO_INICIAL) return

    val (dotColor, bgColor, icon) = getEventStyle(item)
    val isImportant = item.isScore

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Timeline connector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp).padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier.size(if (isImportant) 14.dp else 10.dp)
                    .clip(CircleShape).background(dotColor)
            )
            Box(modifier = Modifier.width(1.5.dp).height(if (isImportant) 36.dp else 26.dp).background(NavyBorder.copy(0.6f)))
        }

        // Event card
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 4.dp, end = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isImportant) bgColor else Color.Transparent)
                .then(
                    if (isImportant) Modifier.border(1.dp, dotColor.copy(0.3f), RoundedCornerShape(10.dp))
                    else Modifier
                )
                .padding(if (isImportant) 10.dp else 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(if (isImportant) 32.dp else 24.dp)
                        .clip(RoundedCornerShape(if (isImportant) 10.dp else 6.dp))
                        .background(dotColor.copy(if (isImportant) 0.2f else 0.1f))
                ) {
                    Icon(icon, null, tint = dotColor, modifier = Modifier.size(if (isImportant) 18.dp else 12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.descripcion,
                        color = if (isImportant) dotColor else TextSecondary,
                        fontSize = if (isImportant) 13.sp else 11.sp,
                        fontWeight = if (isImportant) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 2
                    )
                    // Time stamp
                    val mins = (item.timestamp / 60).toInt()
                    val secs = (item.timestamp % 60).toInt()
                    if (item.timestamp > 0) {
                        Text(
                            "%d:%02d".format(mins, secs),
                            color = TextTertiary, fontSize = 9.sp
                        )
                    }
                }

                if (item.isRival) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(RedError.copy(0.12f)).padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("RIVAL", color = RedError, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

private data class EventStyle(val color: Color, val bgColor: Color, val icon: ImageVector)

private fun getEventStyle(item: TimelineItem): EventStyle {
    return when {
        item.tipo == EventoTipo.PUNTO_2 -> EventStyle(OrangeBase, OrangeBase.copy(0.06f), Icons.Filled.SportsBasketball)
        item.tipo == EventoTipo.PUNTO_3 -> EventStyle(GoldAccent, GoldAccent.copy(0.06f), Icons.Filled.SportsBasketball)
        item.tipo == EventoTipo.LIBRE_ANOTADO -> EventStyle(GreenSuccess, GreenSuccess.copy(0.06f), Icons.Filled.CheckCircle)
        item.tipo == EventoTipo.LIBRE_FALLADO -> EventStyle(RedError, RedError.copy(0.06f), Icons.Filled.Cancel)
        item.tipo == EventoTipo.PUNTO_RIVAL -> EventStyle(RedError, RedError.copy(0.06f), Icons.Filled.SportsBasketball)
        item.isFault -> EventStyle(YellowWarning, YellowWarning.copy(0.06f), Icons.Filled.Warning)
        item.tipo == EventoTipo.SUSTITUCION_ENTRA || item.tipo == EventoTipo.SUSTITUCION_SALE ->
            EventStyle(TealAccent, TealAccent.copy(0.06f), Icons.Filled.SwapHoriz)
        item.tipo == EventoTipo.REBOTE_OFENSIVO || item.tipo == EventoTipo.REBOTE_DEFENSIVO ->
            EventStyle(TealAccent, Color.Transparent, Icons.Filled.Refresh)
        item.tipo == EventoTipo.ASISTENCIA -> EventStyle(GoldAccent.copy(0.8f), Color.Transparent, Icons.Filled.TouchApp)
        item.tipo == EventoTipo.ROBO -> EventStyle(GreenSuccess, Color.Transparent, Icons.Filled.PanTool)
        item.tipo == EventoTipo.PERDIDA -> EventStyle(RedError.copy(0.7f), Color.Transparent, Icons.Filled.RemoveCircle)
        item.tipo == EventoTipo.TAPON -> EventStyle(PurpleAccent, Color.Transparent, Icons.Filled.Block)
        item.isRival -> EventStyle(RedError.copy(0.6f), Color.Transparent, Icons.Filled.FiberManualRecord)
        item.isQuarterBoundary -> EventStyle(OrangeBase, OrangeBase.copy(0.06f), Icons.Filled.Flag)
        else -> EventStyle(TealAccent.copy(0.6f), Color.Transparent, Icons.Filled.FiberManualRecord)
    }
}
