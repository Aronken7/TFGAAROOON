package com.example.tfg_aaron.ui.screens.historialfisico

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.HistorialFisicoEntity
import com.example.tfg_aaron.ui.components.*
import com.example.tfg_aaron.ui.theme.*
import com.example.tfg_aaron.ui.utils.LocalIsTablet
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialFisicoScreen(navController: NavController, jugadoraId: Int, entrenadorId: Int) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: HistorialFisicoViewModel = viewModel(factory = viewModelFactory {
        initializer { HistorialFisicoViewModel(jugadoraId, app.historialFisicoRepository) }
    })
    val uiState by viewModel.uiState.collectAsState()
    val isTablet = LocalIsTablet.current
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = NavyDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = NeonGreen,
                contentColor = NavyDark,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Nuevo registro")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth().background(NavyCard)
                    .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("HISTORIAL FÍSICO", color = NeonGreen, fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                        Text("Evaluación completa", color = TextPrimary, fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(if (isTablet) 2 else 1),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = if (isTablet) Arrangement.spacedBy(10.dp) else Arrangement.Start
            ) {
                // Weight trend chart
                if (uiState.registros.size >= 2) {
                    item(span = { GridItemSpan(maxLineSpan) }) { PesoChart(registros = uiState.registros.reversed()) }
                }

                // Latest stats overview
                if (uiState.registros.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        val u = uiState.registros.first()
                        val condColor = conditionColor(u.condicionFisica)

                        Text("ESTADO ACTUAL", color = condColor, fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp))

                        // Condition + basic stats
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LatestStatCard("CONDICIÓN", u.condicionFisica, condColor, Icons.Filled.Favorite, Modifier.weight(1f))
                            LatestStatCard("PESO", if (u.peso > 0) "%.1f kg".format(u.peso) else "—", TealAccent, Icons.Filled.FitnessCenter, Modifier.weight(1f))
                            LatestStatCard("ALTURA", if (u.altura > 0) "%.0f cm".format(u.altura) else "—", GoldAccent, Icons.Filled.Height, Modifier.weight(1f))
                        }

                        if (u.envergadura > 0 || u.saltoVertical > 0 || u.masaGrasa > 0) {
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (u.envergadura > 0) LatestStatCard("ENVERGADURA", "%.0f cm".format(u.envergadura), PurpleAccent, Icons.Filled.OpenWith, Modifier.weight(1f))
                                if (u.saltoVertical > 0) LatestStatCard("SALTO VERT.", "%.0f cm".format(u.saltoVertical), OrangeBase, Icons.Filled.KeyboardArrowUp, Modifier.weight(1f))
                                if (u.masaGrasa > 0) LatestStatCard("GRASA", "%.1f%%".format(u.masaGrasa), YellowWarning, Icons.Filled.Analytics, Modifier.weight(1f))
                                if (u.envergadura <= 0) Spacer(Modifier.weight(1f))
                                if (u.saltoVertical <= 0) Spacer(Modifier.weight(1f))
                                if (u.masaGrasa <= 0) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("HISTORIAL (${uiState.registros.size})", color = TextTertiary, fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }

                if (uiState.registros.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Icons.Filled.FitnessCenter,
                                title = "Sin registros físicos",
                                subtitle = "Añade el primer perfil físico completo de la jugadora"
                            )
                        }
                    }
                } else {
                    gridItems(uiState.registros, key = { it.id }) { registro ->
                        HistorialFisicoCard(registro = registro, onDelete = { viewModel.deleteRegistro(registro) })
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = NavyElevated,
            tonalElevation = 0.dp,
            scrimColor = Color.Black.copy(alpha = 0.8f)
        ) {
            AddRegistroFisicoContent(
                onSave = { condicion, peso, altura, masaGrasa, masaMuscular, envergadura, alturaSentado,
                           perimetroBrazo, perimetroCintura, perimetroMuslo, perimetroCadera, perimetroPecho,
                           saltoVertical, saltoHorizontal, sprint10m, obs ->
                    viewModel.addRegistro(condicion, peso, altura, masaGrasa, masaMuscular, envergadura,
                        alturaSentado, perimetroBrazo, perimetroCintura, perimetroMuslo, perimetroCadera,
                        perimetroPecho, saltoVertical, saltoHorizontal, sprint10m, obs)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false }
            )
        }
    }
}

private fun conditionColor(condicion: String): Color = when (condicion) {
    "DISPONIBLE" -> GreenSuccess
    "LESIONADA" -> RedError
    "DESCANSANDO" -> GoldAccent
    else -> TextSecondary
}

@Composable
private fun LatestStatCard(label: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(NavyCard)
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(12.dp)).padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
                Text(label, color = color, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, maxLines = 1)
        }
    }
}

@Composable
private fun PesoChart(registros: List<HistorialFisicoEntity>) {
    val pesos = registros.map { it.peso }.filter { it > 0 }
    if (pesos.size < 2) return
    val minPeso = pesos.minOrNull() ?: 0f
    val maxPeso = pesos.maxOrNull() ?: 1f
    val range = (maxPeso - minPeso).coerceAtLeast(1f)

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(NavyCard).border(1.dp, NavyBorder, RoundedCornerShape(14.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.TrendingUp, null, tint = NeonGreen, modifier = Modifier.size(16.dp))
            Text("EVOLUCIÓN DE PESO", color = TextTertiary, fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        }
        Spacer(Modifier.height(12.dp))
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            drawPesoLine(pesos, minPeso, range, size.width, size.height)
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Min: %.1f kg".format(minPeso), color = TextTertiary, fontSize = 10.sp)
            Text("Max: %.1f kg".format(maxPeso), color = TextTertiary, fontSize = 10.sp)
        }
    }
}

private fun DrawScope.drawPesoLine(pesos: List<Float>, minPeso: Float, range: Float, width: Float, height: Float) {
    if (pesos.size < 2) return
    val stepX = width / (pesos.size - 1).coerceAtLeast(1)
    val points = pesos.mapIndexed { i, peso ->
        Offset(i * stepX, height - ((peso - minPeso) / range) * height * 0.85f - height * 0.075f)
    }
    val fillPath = Path().apply {
        moveTo(points.first().x, height)
        points.forEach { lineTo(it.x, it.y) }
        lineTo(points.last().x, height)
        close()
    }
    drawPath(fillPath, color = Color(0xFFADFF2F).copy(alpha = 0.08f))
    val linePath = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(linePath, color = Color(0xFFADFF2F), style = Stroke(width = 2.5f))
    points.forEach { point ->
        drawCircle(color = Color(0xFFADFF2F), radius = 5f, center = point)
        drawCircle(color = Color(0xFF131B2E), radius = 2.5f, center = point)
    }
}

@Composable
private fun HistorialFisicoCard(registro: HistorialFisicoEntity, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val condColor = conditionColor(registro.condicionFisica)
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(NavyCard).border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
    ) {
        Box(
            modifier = Modifier.width(3.dp).matchParentSize()
                .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                .background(condColor).align(Alignment.CenterStart)
        )
        Column(modifier = Modifier.padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.CalendarToday, null, tint = TextTertiary, modifier = Modifier.size(12.dp))
                    Text(sdf.format(Date(registro.fecha)), color = TextTertiary, fontSize = 11.sp)
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(condColor.copy(0.12f)).padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(registro.condicionFisica, color = condColor, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            null, tint = TextTertiary, modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Delete, null, tint = RedError.copy(0.6f), modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            // Quick stats always visible
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (registro.peso > 0) MiniPhysStat("PESO", "%.1f kg".format(registro.peso), TealAccent)
                if (registro.altura > 0) MiniPhysStat("ALTURA", "%.0f cm".format(registro.altura), GoldAccent)
                if (registro.saltoVertical > 0) MiniPhysStat("SALTO", "%.0f cm".format(registro.saltoVertical), OrangeBase)
                if (registro.masaGrasa > 0) MiniPhysStat("GRASA", "%.1f%%".format(registro.masaGrasa), YellowWarning)
            }

            // Expanded details
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = NavyBorder)
                Spacer(Modifier.height(10.dp))

                // Medidas corporales
                val circumData = listOf(
                    "Envergadura" to registro.envergadura,
                    "Alt. Sentado" to registro.alturaSentado,
                    "Pecho" to registro.perimetroPecho,
                    "Brazo" to registro.perimetroBrazo,
                    "Cintura" to registro.perimetroCintura,
                    "Muslo" to registro.perimetroMuslo,
                    "Cadera" to registro.perimetroCadera
                ).filter { it.second > 0 }
                if (circumData.isNotEmpty()) {
                    Text("MEDIDAS CORPORALES", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(6.dp))
                    circumData.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { (label, value) -> MiniPhysStat(label.uppercase(), "%.0f cm".format(value), PurpleAccent) }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Rendimiento
                val perfData = listOf(
                    "Salto H." to registro.saltoHorizontal,
                    "Sprint 10m" to registro.sprint10m,
                    "Masa musc." to registro.masaMuscular
                ).filter { it.second > 0 }
                if (perfData.isNotEmpty()) {
                    Text("RENDIMIENTO", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        perfData.forEach { (label, value) ->
                            val unit = if (label == "Sprint 10m") " s" else if (label == "Masa musc.") "%" else " cm"
                            MiniPhysStat(label.uppercase(), "%.1f$unit".format(value), GreenSuccess)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                if (registro.observaciones.isNotEmpty()) {
                    Text("NOTAS", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(registro.observaciones, color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MiniPhysStat(label: String, value: String, color: Color) {
    Column {
        Text(label, color = TextTertiary, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
        Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
private fun AddRegistroFisicoContent(
    onSave: (String, Float, Float, Float, Float, Float, Float, Float, Float, Float, Float, Float, Float, Float, Float, String) -> Unit,
    onCancel: () -> Unit
) {
    var condicion by remember { mutableStateOf("DISPONIBLE") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var masaGrasa by remember { mutableStateOf("") }
    var masaMuscular by remember { mutableStateOf("") }
    var envergadura by remember { mutableStateOf("") }
    var alturaSentado by remember { mutableStateOf("") }
    var perimetroBrazo by remember { mutableStateOf("") }
    var perimetroCintura by remember { mutableStateOf("") }
    var perimetroMuslo by remember { mutableStateOf("") }
    var perimetroCadera by remember { mutableStateOf("") }
    var perimetroPecho by remember { mutableStateOf("") }
    var saltoVertical by remember { mutableStateOf("") }
    var saltoHorizontal by remember { mutableStateOf("") }
    var sprint10m by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    fun String.toF() = replace(",", ".").toFloatOrNull() ?: 0f

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("NUEVO PERFIL FÍSICO", fontSize = 10.sp, color = NeonGreen, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
        Text("Evaluación completa de la jugadora", fontSize = 18.sp, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
        HorizontalDivider(color = NavyBorder)

        // Condición
        Text("CONDICIÓN FÍSICA", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("DISPONIBLE", "LESIONADA", "DESCANSANDO").forEach { c ->
                val color = conditionColor(c)
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                        .background(if (condicion == c) color.copy(0.15f) else NavyCard)
                        .border(1.dp, if (condicion == c) color else NavyBorder, RoundedCornerShape(8.dp))
                        .clickable { condicion = c }.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) { Text(c, color = if (condicion == c) color else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
        }

        // Datos básicos
        Text("DATOS BÁSICOS", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FisicoField("Peso (kg)", peso, { peso = it }, Modifier.weight(1f))
            FisicoField("Altura de pie (cm)", altura, { altura = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FisicoField("Envergadura (cm)", envergadura, { envergadura = it }, Modifier.weight(1f))
            FisicoField("Altura sentado (cm)", alturaSentado, { alturaSentado = it }, Modifier.weight(1f))
        }

        // Composición corporal
        Text("COMPOSICIÓN CORPORAL", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FisicoField("% Grasa", masaGrasa, { masaGrasa = it }, Modifier.weight(1f))
            FisicoField("% Masa musc.", masaMuscular, { masaMuscular = it }, Modifier.weight(1f))
        }

        // Perímetros
        Text("PERÍMETROS (cm)", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FisicoField("Pecho", perimetroPecho, { perimetroPecho = it }, Modifier.weight(1f))
            FisicoField("Brazo (bícep)", perimetroBrazo, { perimetroBrazo = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FisicoField("Cintura", perimetroCintura, { perimetroCintura = it }, Modifier.weight(1f))
            FisicoField("Cadera", perimetroCadera, { perimetroCadera = it }, Modifier.weight(1f))
        }
        FisicoField("Muslo", perimetroMuslo, { perimetroMuslo = it }, Modifier.fillMaxWidth())

        // Rendimiento atlético
        Text("RENDIMIENTO ATLÉTICO", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FisicoField("Salto vertical (cm)", saltoVertical, { saltoVertical = it }, Modifier.weight(1f))
            FisicoField("Salto horizontal (cm)", saltoHorizontal, { saltoHorizontal = it }, Modifier.weight(1f))
        }
        FisicoField("Sprint 10m (segundos)", sprint10m, { sprint10m = it }, Modifier.fillMaxWidth())

        // Observaciones
        OutlinedTextField(
            value = observaciones, onValueChange = { observaciones = it },
            label = { Text("Observaciones (opcional)", color = TextTertiary) },
            modifier = Modifier.fillMaxWidth(), minLines = 2,
            colors = fisicoFieldColors(), shape = RoundedCornerShape(10.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onCancel, modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, NavyBorder), shape = RoundedCornerShape(12.dp)
            ) { Text("Cancelar", color = TextSecondary) }
            Button(
                onClick = {
                    onSave(
                        condicion, peso.toF(), altura.toF(), masaGrasa.toF(), masaMuscular.toF(),
                        envergadura.toF(), alturaSentado.toF(), perimetroBrazo.toF(), perimetroCintura.toF(),
                        perimetroMuslo.toF(), perimetroCadera.toF(), perimetroPecho.toF(),
                        saltoVertical.toF(), saltoHorizontal.toF(), sprint10m.toF(), observaciones
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Guardar", color = NavyDark, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun FisicoField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, color = TextTertiary, fontSize = 11.sp) },
        modifier = modifier, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = fisicoFieldColors(), shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun fisicoFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonGreen, unfocusedBorderColor = NavyBorder,
    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
    cursorColor = NeonGreen, focusedLabelColor = NeonGreen, unfocusedLabelColor = TextTertiary
)
