package com.example.tfg_aaron.ui.screens.partidos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun ComparativaTab(state: PartidoEnVivoUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val rivalName = state.partido?.rival ?: "Rival"
        val shooting = state.shootingPropio
        val shootingRival = state.shootingRival

        // ── Parciales por Cuarto ─────────────────────────────────────────────
        SectionTitle("PARCIALES POR CUARTO", Icons.Filled.Timer)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("", modifier = Modifier.width(60.dp))
                    Text("NOS.", color = TealAccent, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text(rivalName.take(8), color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NavyBorder)

                if (state.parciales.isEmpty()) {
                    Text("Sin cuartos registrados", color = TextTertiary, fontSize = 12.sp,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(8.dp))
                } else {
                    state.parciales.forEach { parcial ->
                        ParcialRow(
                            label = cuartoLabel(parcial.cuarto).replace("Cuarto", "Q").replace("er", "").replace("do", "").replace("er", "").replace("to", ""),
                            propio = parcial.propio,
                            rival = parcial.rival
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NavyBorder)

                    // Total
                    ParcialRow(
                        label = "TOTAL",
                        propio = state.puntosPropio,
                        rival = state.puntosRival,
                        isTotal = true
                    )
                }
            }
        }

        // ── Tiros de Campo ────────────────────────────────────────────────────
        SectionTitle("TIROS DE CAMPO", Icons.Filled.SportsBasketball)

        ShootingCompareCard(
            title = "Campo Total",
            ourMade = shooting.tirosCampoAnotados,
            ourAtt = shooting.tirosCampo,
            ourPct = shooting.pctCampo,
            rivalMade = shootingRival.tirosCampoAnotados,
            rivalAtt = shootingRival.tirosCampo,
            rivalPct = shootingRival.pctCampo,
            color = GoldAccent
        )
        ShootingCompareCard(
            title = "Tiros de 2",
            ourMade = shooting.tirosDosAnotados,
            ourAtt = shooting.tirosDos,
            ourPct = shooting.pct2,
            rivalMade = shootingRival.tirosDosAnotados,
            rivalAtt = shootingRival.tirosDos,
            rivalPct = shootingRival.pct2,
            color = TealAccent
        )
        ShootingCompareCard(
            title = "Triples",
            ourMade = shooting.tirosTresAnotados,
            ourAtt = shooting.tirosTres,
            ourPct = shooting.pct3,
            rivalMade = shootingRival.tirosTresAnotados,
            rivalAtt = shootingRival.tirosTres,
            rivalPct = shootingRival.pct3,
            color = PurpleAccent
        )
        ShootingCompareCard(
            title = "Tiros Libres",
            ourMade = shooting.tirosLibresAnotados,
            ourAtt = shooting.tirosLibres,
            ourPct = shooting.pctTL,
            rivalMade = shootingRival.tirosLibresAnotados,
            rivalAtt = shootingRival.tirosLibres,
            rivalPct = shootingRival.pctTL,
            color = GoldAccent
        )

        // ── Faltas ────────────────────────────────────────────────────────────
        SectionTitle("FALTAS COMETIDAS", Icons.Filled.Warning)

        val rivalFaltasTotales = state.rivalStats.values.sumOf { it.faltasTotales }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CompareStatRow(
                    label = "Faltas totales",
                    ourVal = shooting.faltasCometidas,
                    rivalVal = rivalFaltasTotales,
                    ourColor = YellowWarning,
                    rivalColor = RedError
                )

                val myFaltasPersonales = state.stats.values.sumOf { it.faltasPersonales }
                val myFaltasTecnicas = state.stats.values.sumOf { it.faltasTecnicas }
                val myFaltasAnti = state.stats.values.sumOf { it.faltasAntideportivas }
                val rivalFP = state.rivalStats.values.sumOf { it.faltasPersonales }
                val rivalFT = state.rivalStats.values.sumOf { it.faltasTecnicas }
                val rivalFA = state.rivalStats.values.sumOf { it.faltasAntideportivas }

                CompareStatRow("Personales", myFaltasPersonales, rivalFP, YellowWarning, RedError)
                CompareStatRow("Técnicas", myFaltasTecnicas, rivalFT, PinkAccent, PinkAccent)
                CompareStatRow("Antideportivas", myFaltasAnti, rivalFA, RedError, RedError)
            }
        }

        // ── Estadísticas de Equipo ────────────────────────────────────────────
        SectionTitle("ESTADÍSTICAS DE EQUIPO", Icons.Filled.BarChart)

        val myRebOf = state.stats.values.sumOf { it.rebotesOfensivos }
        val myRebDef = state.stats.values.sumOf { it.rebotesDefensivos }
        val myRebTot = myRebOf + myRebDef
        val myAst = state.stats.values.sumOf { it.asistencias }
        val myRob = state.stats.values.sumOf { it.robos }
        val myPer = state.stats.values.sumOf { it.perdidas }
        val myTap = state.stats.values.sumOf { it.tapones }

        val rivRebOf = state.rivalStats.values.sumOf { it.rebotesOfensivos }
        val rivRebDef = state.rivalStats.values.sumOf { it.rebotesDefensivos }
        val rivRebTot = rivRebOf + rivRebDef
        val rivAst = state.rivalStats.values.sumOf { it.asistencias }
        val rivRob = state.rivalStats.values.sumOf { it.robos }
        val rivPer = state.rivalStats.values.sumOf { it.perdidas }
        val rivTap = state.rivalStats.values.sumOf { it.tapones }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("NOS.", color = TealAccent, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp,
                        modifier = Modifier.width(44.dp))
                    Text("STAT", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("RIVAL", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp,
                        modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
                }
                HorizontalDivider(color = NavyBorder)
                CompareStatRow("Rebotes Totales", myRebTot, rivRebTot, TealAccent, RedError)
                CompareStatRow("Reb. Ofensivos", myRebOf, rivRebOf, TealAccent.copy(0.8f), RedError.copy(0.8f))
                CompareStatRow("Reb. Defensivos", myRebDef, rivRebDef, TealAccent.copy(0.6f), RedError.copy(0.6f))
                HorizontalDivider(color = NavyBorder)
                CompareStatRow("Asistencias", myAst, rivAst, GoldAccent, RedError)
                CompareStatRow("Robos", myRob, rivRob, GreenSuccess, RedError)
                CompareStatRow("Pérdidas", myPer, rivPer, RedError.copy(0.7f), YellowWarning)
                CompareStatRow("Tapones", myTap, rivTap, PurpleAccent, RedError)
            }
        }

        // ── Jugadoras con peligro de falta ───────────────────────────────────
        val enPeligro = state.jugadoras.filter {
            val st = state.stats[it.id]
            st != null && (st.faltasPersonales >= 3 || st.faltasTecnicas >= 1 || st.faltasAntideportivas >= 1) && !st.expulsada
        }
        val expulsadas = state.jugadoras.filter { state.stats[it.id]?.expulsada == true }

        if (enPeligro.isNotEmpty() || expulsadas.isNotEmpty()) {
            SectionTitle("ALERTAS DE FALTAS", Icons.Filled.NotificationsActive)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    expulsadas.forEach { j ->
                        val st = state.stats[j.id] ?: EstadisticasJugadora()
                        AlertaFaltaRow(
                            nombre = "${j.nombre} #${j.numero}",
                            texto = "EXPULSADA — ${st.faltasTotales} faltas",
                            color = RedError,
                            icon = Icons.Filled.Block
                        )
                    }
                    enPeligro.forEach { j ->
                        val st = state.stats[j.id] ?: EstadisticasJugadora()
                        val texto = buildString {
                            append("${st.faltasTotales} faltas")
                            if (st.faltasPersonales >= 3) append(" · ${st.faltasPersonales} personales")
                            if (st.faltasTecnicas >= 1) append(" · ${st.faltasTecnicas} técnicas")
                        }
                        AlertaFaltaRow(
                            nombre = "${j.nombre} #${j.numero}",
                            texto = texto,
                            color = YellowWarning,
                            icon = Icons.Filled.Warning
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionTitle(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = GoldAccent, modifier = Modifier.size(16.dp))
        Text(text, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        HorizontalDivider(modifier = Modifier.weight(1f), color = NavyBorder)
    }
}

@Composable
private fun ParcialRow(label: String, propio: Int, rival: Int, isTotal: Boolean = false) {
    val isWin = propio > rival
    val isLoss = propio < rival

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = if (isTotal) GoldAccent else TextSecondary,
            fontWeight = if (isTotal) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = if (isTotal) 12.sp else 11.sp,
            modifier = Modifier.width(60.dp)
        )
        Text(
            "$propio",
            color = if (isWin) GreenSuccess else if (isLoss) TextSecondary else TextPrimary,
            fontWeight = if (isTotal) FontWeight.ExtraBold else FontWeight.Bold,
            fontSize = if (isTotal) 18.sp else 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            "$rival",
            color = if (isLoss) RedError else if (isWin) TextSecondary else TextPrimary,
            fontWeight = if (isTotal) FontWeight.ExtraBold else FontWeight.Bold,
            fontSize = if (isTotal) 18.sp else 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ShootingCompareCard(
    title: String,
    ourMade: Int, ourAtt: Int, ourPct: Float,
    rivalMade: Int, rivalAtt: Int, rivalPct: Float,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .background(color.copy(alpha = 0.04f))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Our column
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                    Text("NOS.", color = color, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Text("$ourMade/$ourAtt", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    if (ourAtt > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(NavyElevated)) {
                            Box(modifier = Modifier.fillMaxWidth(fraction = (ourPct / 100f).coerceIn(0f, 1f))
                                .fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(color))
                        }
                        Text("${ourPct.toInt()}%", color = TextSecondary, fontSize = 10.sp)
                    } else {
                        Text("—", color = TextTertiary, fontSize = 12.sp)
                    }
                }

                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text("VS", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                }

                // Rival column
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("RIVAL", color = RedError, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    Text("$rivalMade/$rivalAtt", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    if (rivalAtt > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(NavyElevated)) {
                            Box(modifier = Modifier.fillMaxWidth(fraction = (rivalPct / 100f).coerceIn(0f, 1f))
                                .fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(RedError))
                        }
                        Text("${rivalPct.toInt()}%", color = TextSecondary, fontSize = 10.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    } else {
                        Text("—", color = TextTertiary, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareStatRow(
    label: String,
    ourVal: Int,
    rivalVal: Int,
    ourColor: Color,
    rivalColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$ourVal", color = ourColor, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp,
            textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
        Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center)
        Text("$rivalVal", color = rivalColor, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp,
            textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
    }
}

@Composable
private fun AlertaFaltaRow(nombre: String, texto: String, color: Color, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(nombre, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(texto, color = TextSecondary, fontSize = 11.sp)
        }
    }
}
