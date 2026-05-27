package com.example.tfg_aaron.ui.screens.alineacion

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.tfg_aaron.TFGApplication
import com.example.tfg_aaron.data.local.entities.JugadoraEntity
import com.example.tfg_aaron.data.local.entities.PartidoEntity
import com.example.tfg_aaron.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Position metadata
private data class PositionInfo(
    val index: Int,
    val shortName: String,
    val longName: String,
    val color: Color,
    // Fractional position on court canvas (0f..1f)
    val xFrac: Float,
    val yFrac: Float
)

@Composable
private fun positionInfoList(): List<PositionInfo> = listOf(
    PositionInfo(0, "PG", "BASE",      ColorBase,     0.50f, 0.82f),
    PositionInfo(1, "SG", "ESCOLTA",   ColorEscolta,  0.20f, 0.70f),
    PositionInfo(2, "SF", "ALERO",     ColorAlero,    0.80f, 0.70f),
    PositionInfo(3, "PF", "ALA-PÍVOT", ColorAlaPivot, 0.25f, 0.38f),
    PositionInfo(4, "C",  "PÍVOT",     ColorPivot,    0.50f, 0.22f),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlineacionScreen(
    navController: NavController,
    entrenadorId: Int,
    idPartido: Int
) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TFGApplication
    val viewModel: AlineacionViewModel = viewModel(factory = viewModelFactory {
        initializer {
            AlineacionViewModel(
                entrenadorId,
                idPartido,
                app.convocatoriaRepository,
                app.jugadoraRepository,
                app.partidoRepository
            )
        }
    })

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog state: which posicion slot is being filled
    var pickerPosicion by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(uiState.savedMessage) {
        uiState.savedMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSavedMessage()
        }
    }

    Scaffold(
        containerColor = NavyDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CONVOCATORIA",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                        uiState.partido?.let {
                            Text(
                                text = "vs ${it.rival}",
                                color = OrangeBase,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveAlineacion() }) {
                        Icon(Icons.Filled.Save, contentDescription = "Guardar", tint = OrangeBase)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)
            )
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangeBase)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Match info header ─────────────────────────────────────────────
            uiState.partido?.let { partido ->
                AlineacionMatchHeader(
                    partido = partido,
                    titularCount = viewModel.titulares.size,
                    suplentesCount = viewModel.suplentes.size
                )
            }

            // ── Half-court diagram ────────────────────────────────────────────
            HalfCourtDiagram(
                viewModel = viewModel,
                onPositionTap = { posIdx -> pickerPosicion = posIdx },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.7f)
            )

            // ── Instruction hint ──────────────────────────────────────────────
            Text(
                text = "Toca un círculo en el campo para asignar jugadora titular",
                color = TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(Modifier.height(4.dp))

            // ── Section header: convocadas / suplentes ────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .background(OrangeBase, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "PLANTILLA",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.weight(1f))
                val titularCount = viewModel.titulares.size
                Text(
                    "$titularCount/5 TITULARES",
                    color = if (titularCount == 5) NeonGreen else OrangeBase,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Player grid ───────────────────────────────────────────────────
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                gridItems(uiState.jugadoras) { jugadora ->
                    PlayerCard(
                        jugadora = jugadora,
                        viewModel = viewModel,
                        onAssignTitularClick = { posIdx ->
                            viewModel.assignTitular(jugadora, posIdx)
                        },
                        onToggleSuplente = {
                            viewModel.toggleSuplente(jugadora)
                        },
                        onPickPosition = { pickerPosicion = -jugadora.id } // negative = from card
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Player Picker Dialog ──────────────────────────────────────────────
        pickerPosicion?.let { picIdx ->
            if (picIdx >= 0) {
                // Tapped from court: pick player for this slot
                val posInfo = positionInfoList().getOrNull(picIdx)
                PlayerPickerDialog(
                    positionLabel = posInfo?.let { "${it.shortName} · ${it.longName}" } ?: "Posición $picIdx",
                    jugadoras = uiState.jugadoras,
                    excludedJugadoraId = viewModel.getJugadoraForPosicion(picIdx)?.id,
                    onAssign = { jugadora ->
                        viewModel.assignTitular(jugadora, picIdx)
                        pickerPosicion = null
                    },
                    onDismiss = { pickerPosicion = null }
                )
            } else {
                // Tapped from card: pick position for this player
                val jugadoraId = -picIdx
                val jugadora = uiState.jugadoras.find { it.id == jugadoraId }
                if (jugadora != null) {
                    PositionPickerDialog(
                        jugadora = jugadora,
                        onAssign = { posIdx ->
                            viewModel.assignTitular(jugadora, posIdx)
                            pickerPosicion = null
                        },
                        onDismiss = { pickerPosicion = null }
                    )
                } else {
                    pickerPosicion = null
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Match header card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AlineacionMatchHeader(
    partido: PartidoEntity,
    titularCount: Int,
    suplentesCount: Int
) {
    val fechaStr = remember(partido.fecha) {
        SimpleDateFormat("d MMM yyyy", Locale("es")).format(Date(partido.fecha))
    }
    val localLabel = if (partido.esLocal) "LOCAL" else "VISITANTE"
    val localColor = if (partido.esLocal) TealAccent else OrangeBase

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = NavyCard,
        border = BorderStroke(1.dp, NavyBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Court icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(OrangeBase.copy(0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.SportsBasketball,
                    contentDescription = null,
                    tint = OrangeBase,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = partido.rival,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(fechaStr, color = TextSecondary, fontSize = 11.sp)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = localColor.copy(0.15f)
                    ) {
                        Text(
                            localLabel,
                            color = localColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Status column
            Column(horizontalAlignment = Alignment.End) {
                val titColor = if (titularCount == 5) NeonGreen else OrangeBase
                Text(
                    text = "$titularCount/5",
                    color = titColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Text("TITULARES", color = titColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                if (suplentesCount > 0) {
                    Text(
                        text = "$suplentesCount SUP",
                        color = TealAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Half-court Canvas with overlaid position circles
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HalfCourtDiagram(
    viewModel: AlineacionViewModel,
    onPositionTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val positions = positionInfoList()
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current

    Box(modifier = modifier.clip(RoundedCornerShape(8.dp))) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
        ) {
            val w = size.width
            val h = size.height
            val mX = w / 2f

            // ── Professional parquet floor (same as Pizarra) ─────────────────
            drawRect(Color(0xFFCFAE72))
            val plankCount = 40
            val plankH = h / plankCount
            val woodTones = listOf(
                Color(0xFFD9BE84), Color(0xFFD3B57C), Color(0xFFD6BA80),
                Color(0xFFDDC48C), Color(0xFFD1B278), Color(0xFFD8BC82),
                Color(0xFFD4B77E), Color(0xFFCFAF74), Color(0xFFDBC088),
                Color(0xFFD2B47A), Color(0xFFE0C890), Color(0xFFCDAC70)
            )
            val staggerW = w / 6f
            for (i in 0 until plankCount) {
                val y = i * plankH
                val baseTone = woodTones[i % woodTones.size]
                drawRect(baseTone, Offset(0f, y), Size(w, plankH + 1f))
                drawLine(Color(0xFF7A5A14).copy(0.16f), Offset(0f, y), Offset(w, y), 0.9f)
                val xOff = if (i % 2 == 0) 0f else staggerW * 0.5f
                var x = xOff
                while (x < w) {
                    drawLine(Color(0xFF6B4E10).copy(0.12f), Offset(x, y), Offset(x, y + plankH), 0.7f)
                    x += staggerW
                }
            }
            // Center glow
            drawRect(
                Brush.radialGradient(
                    listOf(Color.White.copy(0.09f), Color.Transparent),
                    center = Offset(mX, h * 0.45f), radius = maxOf(w, h) * 0.8f
                )
            )

            // ── FIBA half-court lines ─────────────────────────────────────────
            val lineColor = Color(0xFF1A1208)
            val strokeW = (w * 0.012f).coerceAtLeast(3f)

            // Outer boundary
            drawRect(lineColor, Offset(0f, 0f), Size(w, h), style = Stroke(strokeW * 1.5f))

            // Paint / key area (basket at top)
            val keyHalfW = w * 0.163f   // 4.9m / 2 / 15m
            val keyDepth = h * 0.414f   // 5.8m / 14m
            val paintFill = Color(0xFFADFF2F).copy(0.38f)
            drawRect(paintFill, Offset(mX - keyHalfW, 0f), Size(keyHalfW * 2f, keyDepth))
            drawRect(lineColor, Offset(mX - keyHalfW, 0f), Size(keyHalfW * 2f, keyDepth), style = Stroke(strokeW))

            // Lane restriction marks
            val markLen = strokeW * 2.5f
            listOf(0.32f, 0.53f, 0.72f).forEach { frac ->
                val y = frac * keyDepth
                drawLine(lineColor, Offset(mX - keyHalfW - markLen, y), Offset(mX - keyHalfW, y), strokeW * 0.8f)
                drawLine(lineColor, Offset(mX + keyHalfW, y), Offset(mX + keyHalfW + markLen, y), strokeW * 0.8f)
            }

            // Free throw line + circle
            val ftRadius = w * 0.12f
            val ftCenterY = keyDepth
            drawLine(lineColor, Offset(mX - keyHalfW, ftCenterY), Offset(mX + keyHalfW, ftCenterY), strokeW)
            drawCircle(lineColor, radius = ftRadius, center = Offset(mX, ftCenterY), style = Stroke(strokeW))

            // Backboard
            val bbY = h * 0.05f
            val bbHalfW = w * 0.061f
            drawLine(lineColor, Offset(mX - bbHalfW, bbY), Offset(mX + bbHalfW, bbY), strokeW * 2.2f)

            // Rim (orange)
            val rimRadius = w * 0.018f
            val rimCenterY = bbY + rimRadius * 2.2f + strokeW
            drawCircle(OrangeBase, radius = rimRadius * 2f, center = Offset(mX, rimCenterY), style = Stroke(strokeW * 1.3f))

            // 3-point arc
            val threeRadius = w * 0.44f
            drawArc(
                color = lineColor,
                startAngle = 22f, sweepAngle = 136f,
                useCenter = false,
                topLeft = Offset(mX - threeRadius, rimCenterY - threeRadius),
                size = Size(threeRadius * 2f, threeRadius * 2f),
                style = Stroke(strokeW)
            )
            // 3-pt corner lines
            val angleRad = Math.toRadians(22.0)
            val cornerLineX = mX - threeRadius * kotlin.math.cos(angleRad).toFloat()
            drawLine(lineColor, Offset(cornerLineX, rimCenterY), Offset(cornerLineX, h), strokeW)
            drawLine(lineColor, Offset(w - cornerLineX, rimCenterY), Offset(w - cornerLineX, h), strokeW)

            // ── Position circles ──────────────────────────────────────────────
            val circleR = w * 0.063f
            positions.forEach { pos ->
                val cx = w * pos.xFrac
                val cy = h * pos.yFrac
                val jugadora = viewModel.getJugadoraForPosicion(pos.index)
                if (jugadora != null) {
                    drawCircle(pos.color, radius = circleR, center = Offset(cx, cy))
                    drawCircle(Color.White.copy(0.9f), radius = circleR, center = Offset(cx, cy), style = Stroke(2.5f))
                } else {
                    drawCircle(Color(0xAA1E293B), radius = circleR, center = Offset(cx, cy))
                    drawCircle(
                        pos.color.copy(0.85f), radius = circleR, center = Offset(cx, cy),
                        style = Stroke(2.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f)))
                    )
                }
            }
        }

        // ── Interactive overlay (tap targets + labels) ────────────────────────
        if (canvasSize != Size.Zero) {
            val circleR = canvasSize.width * 0.063f
            val circleDp = with(density) { (circleR * 2f).toDp() }
            val nameLabelW = with(density) { (circleR * 3.8f).toDp() }

            positions.forEach { pos ->
                val jugadora = viewModel.getJugadoraForPosicion(pos.index)
                val cx = canvasSize.width * pos.xFrac
                val cy = canvasSize.height * pos.yFrac

                // Tap circle
                Box(
                    modifier = Modifier
                        .offset { IntOffset((cx - circleR).toInt(), (cy - circleR).toInt()) }
                        .size(circleDp)
                        .clip(CircleShape)
                        .clickable { onPositionTap(pos.index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (jugadora != null) {
                        Text("${jugadora.numero}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    } else {
                        Text("+", color = pos.color, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }

                // Name / position label below circle
                Box(
                    modifier = Modifier
                        .offset { IntOffset((cx - circleR * 1.9f).toInt(), (cy + circleR + 2f).toInt()) }
                        .width(nameLabelW),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (jugadora != null) jugadora.nombre else pos.shortName,
                        color = if (jugadora != null) Color.White.copy(0.92f) else pos.color.copy(0.85f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Player Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlayerCard(
    jugadora: JugadoraEntity,
    viewModel: AlineacionViewModel,
    onAssignTitularClick: (Int) -> Unit,
    onToggleSuplente: () -> Unit,
    onPickPosition: () -> Unit
) {
    val convocadas = viewModel.uiState.collectAsState().value.convocadas
    val conv = convocadas.find { it.idJugadora == jugadora.id }
    val isTitular = conv?.esTitular == true
    val isSuplente = conv != null && !isTitular
    val titularPosIdx = if (isTitular) conv?.posicionIndice else null
    val positions = positionInfoList()
    val posInfo = if (titularPosIdx != null) positions.getOrNull(titularPosIdx) else null

    val cardBg = when {
        isTitular -> OrangeBase.copy(alpha = 0.15f)
        isSuplente -> TealAccent.copy(alpha = 0.10f)
        else -> NavyCard
    }
    val borderColor = when {
        isTitular -> OrangeBase.copy(alpha = 0.5f)
        isSuplente -> TealAccent.copy(alpha = 0.4f)
        else -> NavyBorder
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Number circle
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = posInfo?.color ?: NavyElevated,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${jugadora.numero}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jugadora.nombre,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = jugadora.posicion,
                    color = TextSecondary,
                    fontSize = 10.sp
                )

                // Status badge
                if (isTitular && posInfo != null) {
                    Spacer(Modifier.height(3.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = OrangeBase.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "TITULAR ${posInfo.shortName}",
                            color = OrangeBase,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                } else if (isSuplente) {
                    Spacer(Modifier.height(3.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = TealAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "SUP",
                            color = TealAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Action icons column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Assign to position icon (opens position picker)
                if (!isTitular) {
                    IconButton(
                        onClick = onPickPosition,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Asignar como titular",
                            tint = OrangeBase,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    // Remove titular button
                    IconButton(
                        onClick = {
                            if (titularPosIdx != null) viewModel.removeTitular(titularPosIdx)
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Filled.StarBorder,
                            contentDescription = "Quitar titular",
                            tint = OrangeBase,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Suplente toggle (only if not titular)
                if (!isTitular) {
                    IconButton(
                        onClick = onToggleSuplente,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            if (isSuplente) Icons.Filled.RemoveCircleOutline
                            else Icons.Filled.AddCircleOutline,
                            contentDescription = if (isSuplente) "Quitar suplente" else "Añadir suplente",
                            tint = TealAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Player Picker Dialog (tap on empty court position)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlayerPickerDialog(
    positionLabel: String,
    jugadoras: List<JugadoraEntity>,
    excludedJugadoraId: Int?,
    onAssign: (JugadoraEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val available = jugadoras.filter { it.id != excludedJugadoraId }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                "Seleccionar para $positionLabel",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            if (available.isEmpty()) {
                Text(
                    "No hay jugadoras disponibles",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(available) { jugadora ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavyElevated)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(OrangeBase, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${jugadora.numero}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${jugadora.nombre} ${jugadora.apellidos}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    jugadora.posicion,
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            TextButton(
                                onClick = { onAssign(jugadora) },
                                colors = ButtonDefaults.textButtonColors(contentColor = OrangeBase)
                            ) {
                                Text("Asignar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
            ) {
                Text("Cancelar")
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Position Picker Dialog (long-press / star icon on player card)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PositionPickerDialog(
    jugadora: JugadoraEntity,
    onAssign: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val positions = positionInfoList()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavyCard,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                "Posición para ${jugadora.nombre}",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                positions.forEach { pos ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(pos.color.copy(alpha = 0.12f))
                            .border(1.dp, pos.color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onAssign(pos.index) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(pos.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                pos.shortName,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                pos.shortName,
                                color = pos.color,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                pos.longName,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
            ) {
                Text("Cancelar")
            }
        }
    )
}
