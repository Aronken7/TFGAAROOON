package com.example.tfg_aaron.ui.screens.pizarra

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.tfg_aaron.ui.theme.*
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PizarraEditorScreen(navController: NavController, entrenadorId: Int, jugadaId: Int = -1) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as com.example.tfg_aaron.TFGApplication
    val viewModel: PizarraViewModel = viewModel(factory = viewModelFactory {
        initializer { PizarraViewModel(entrenadorId, app.pizarraRepository) }
    })

    // Lock to portrait — prevent layout breakage when phone rotates
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    val editorState by viewModel.editorState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(viewModel.categorias[0]) }
    var categoriaExpanded by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Toolbar visibility
    var showToolbar by remember { mutableStateOf(true) }

    // Share image
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    var canvasBoundsInWindow by remember { mutableStateOf<android.graphics.Rect?>(null) }

    fun shareCourtImage() {
        val bounds = canvasBoundsInWindow ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        coroutineScope.launch {
            val w = bounds.width().coerceAtLeast(1)
            val h = bounds.height().coerceAtLeast(1)
            val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
            val latch = CountDownLatch(1)
            withContext(Dispatchers.Main) {
                PixelCopy.request(
                    (context as Activity).window, bounds, bitmap,
                    { latch.countDown() },
                    Handler(Looper.getMainLooper())
                )
            }
            withContext(Dispatchers.IO) {
                latch.await(3, TimeUnit.SECONDS)
                val file = File(context.cacheDir, "jugada_${System.currentTimeMillis()}.png")
                file.outputStream().use { out -> bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out) }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                withContext(Dispatchers.Main) {
                    context.startActivity(Intent.createChooser(intent, "Compartir jugada táctica"))
                }
            }
        }
    }

    // Snackbar host state for animate stub
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(jugadaId) {
        if (jugadaId > 0) viewModel.loadJugadaForEdit(jugadaId)
    }
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            navController.popBackStack()
        }
    }
    // Show animate snackbar when triggered
    LaunchedEffect(editorState.showAnimateSnackbar) {
        if (editorState.showAnimateSnackbar) {
            snackbarHostState.showSnackbar(
                message = "Modo animacion no disponible aun",
                duration = SnackbarDuration.Short
            )
            viewModel.dismissAnimateSnackbar()
        }
    }

    Scaffold(
        containerColor = NavyDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (jugadaId > 0) "Editar Jugada" else "Nueva Jugada",
                        color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.undo() }) {
                        Icon(
                            Icons.Filled.Undo, null,
                            tint = if (editorState.paths.isNotEmpty()) TextPrimary else TextTertiary
                        )
                    }
                    IconButton(onClick = { shareCourtImage() }) {
                        Icon(Icons.Filled.Share, null, tint = TealAccent)
                    }
                    IconButton(onClick = { viewModel.clearCanvas() }) {
                        Icon(Icons.Filled.Delete, null, tint = RedError)
                    }
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Filled.Save, null, tint = OrangeBase)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Basketball Court Canvas (tamaño fijo, siempre ocupa todo) ──────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
              Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .onSizeChanged { canvasSize = it.toSize() }
                    .onGloballyPositioned { coords ->
                        val b = coords.boundsInWindow()
                        canvasBoundsInWindow = android.graphics.Rect(
                            b.left.toInt(), b.top.toInt(), b.right.toInt(), b.bottom.toInt()
                        )
                    }
              ) {
                // Court background + lines + drawn paths
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(editorState.drawMode) {
                            when (editorState.drawMode) {
                                DrawMode.MOVE, DrawMode.ANIMATE -> { /* handled elsewhere */ }
                                DrawMode.TEXT -> {
                                    detectTapGestures { offset ->
                                        viewModel.requestTextAt(offset)
                                    }
                                }
                                else -> {
                                    detectDragGestures(
                                        onDragStart = { viewModel.startPath(it) },
                                        onDrag = { change, _ -> viewModel.addPoint(change.position) },
                                        onDragEnd = { viewModel.endPath() }
                                    )
                                }
                            }
                        }
                ) {
                    if (editorState.isHalfCourt) drawHalfCourt() else drawBasketballCourt()
                    drawPaths(editorState.paths, editorState.currentPath, editorState.selectedColor, editorState.strokeWidth, editorState.drawMode)
                }

                // Draggable board elements overlay
                editorState.elements.forEach { element ->
                    key(element.id) {
                        BoardElementToken(
                            element = element,
                            isMovable = editorState.drawMode == DrawMode.MOVE,
                            onDrag = { dx, dy -> viewModel.moveElement(element.id, dx, dy) },
                            onRemove = { viewModel.removeElement(element.id) }
                        )
                    }
                }

                // Text annotation overlays
                editorState.textAnnotations.forEach { annotation ->
                    key(annotation.id) {
                        TextAnnotationToken(
                            annotation = annotation,
                            isMovable = editorState.drawMode == DrawMode.MOVE,
                            onDrag = { dx, dy -> viewModel.moveTextAnnotation(annotation.id, dx, dy) },
                            onRemove = { viewModel.removeTextAnnotation(annotation.id) }
                        )
                    }
                }
              }
            }

            // ── Bottom Toolbar (overlay sobre la pizarra) ─────────────────────
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard.copy(alpha = 0.97f)),
                border = BorderStroke(1.dp, NavyBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                // Toggle row — always visible
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "HERRAMIENTAS",
                        fontSize = 9.sp, color = TextTertiary,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp
                    )
                    IconButton(
                        onClick = { showToolbar = !showToolbar },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            if (showToolbar) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (showToolbar) {
                Column(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 10.dp)) {
                    // Tool row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ToolButton(Icons.Filled.Edit, "Lapiz", editorState.drawMode == DrawMode.PEN, OrangeBase) {
                            viewModel.setDrawMode(DrawMode.PEN)
                        }
                        ToolButton(Icons.Filled.TrendingFlat, "Flecha", editorState.drawMode == DrawMode.ARROW, TealAccent) {
                            viewModel.setDrawMode(DrawMode.ARROW)
                        }
                        ToolButton(Icons.Filled.SwapHoriz, "Doble", editorState.drawMode == DrawMode.ARROW_DOUBLE, ElectricBlue) {
                            viewModel.setDrawMode(DrawMode.ARROW_DOUBLE)
                        }
                        ToolButton(Icons.Filled.Timeline, "Curva", editorState.drawMode == DrawMode.CURVED, Color(0xFF00E676)) {
                            viewModel.setDrawMode(DrawMode.CURVED)
                        }
                        ToolButton(Icons.Filled.LinearScale, "Guion", editorState.drawMode == DrawMode.DASHED, GoldAccent) {
                            viewModel.setDrawMode(DrawMode.DASHED)
                        }
                        ToolButton(Icons.Filled.Gesture, "Dribling", editorState.drawMode == DrawMode.ZIGZAG, Color(0xFFFF9100)) {
                            viewModel.setDrawMode(DrawMode.ZIGZAG)
                        }
                        // NEW: Zone tool
                        ToolButton(Icons.Filled.CropFree, "Zona", editorState.drawMode == DrawMode.ZONE, Color(0xFF00D4FF)) {
                            viewModel.setDrawMode(DrawMode.ZONE)
                        }
                        // NEW: Text tool
                        ToolButton(Icons.Filled.TextFields, "Texto", editorState.drawMode == DrawMode.TEXT, Color(0xFFFFB800)) {
                            viewModel.setDrawMode(DrawMode.TEXT)
                        }
                        ToolButton(Icons.Filled.AutoFixHigh, "Borrar", editorState.drawMode == DrawMode.ERASER, RedError) {
                            viewModel.setDrawMode(DrawMode.ERASER)
                        }
                        ToolButton(Icons.Filled.PanTool, "Mover", editorState.drawMode == DrawMode.MOVE, PurpleAccent) {
                            viewModel.setDrawMode(DrawMode.MOVE)
                        }
                        // NEW: Animate stub
                        ToolButton(Icons.Filled.PlayArrow, "Animar", false, Color(0xFF00E676)) {
                            viewModel.setDrawMode(DrawMode.ANIMATE)
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Secondary action row: half-court toggle + templates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Half-court toggle chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (editorState.isHalfCourt)
                                        NeonGreen.copy(alpha = 0.15f)
                                    else
                                        NavyElevated
                                )
                                .border(
                                    1.5.dp,
                                    if (editorState.isHalfCourt) NeonGreen else NavyBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.toggleHalfCourt() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    if (editorState.isHalfCourt) Icons.Filled.Fullscreen else Icons.Filled.CropLandscape,
                                    contentDescription = null,
                                    tint = if (editorState.isHalfCourt) NeonGreen else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    if (editorState.isHalfCourt) "PISTA COMPLETA" else "MEDIO CAMPO",
                                    color = if (editorState.isHalfCourt) NeonGreen else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                    }

                    HorizontalDivider(color = NavyBorder)
                    Spacer(Modifier.height(8.dp))

                    if (editorState.drawMode == DrawMode.MOVE) {
                        // Element palette
                        Text(
                            "ELEMENTOS DE PISTA",
                            fontSize = 9.sp, color = TealAccent,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ElementAddButton("Equipo", OrangeBase) {
                                viewModel.addElement(ElementType.PLAYER_HOME, canvasSize.width, canvasSize.height)
                            }
                            ElementAddButton("Rival", TealAccent) {
                                viewModel.addElement(ElementType.PLAYER_AWAY, canvasSize.width, canvasSize.height)
                            }
                            ElementAddButton("Balon", GoldAccent, Icons.Filled.SportsBasketball) {
                                viewModel.addElement(ElementType.BALL, canvasSize.width, canvasSize.height)
                            }
                            ElementAddButton("Cono", YellowWarning, Icons.Filled.ChangeHistory) {
                                viewModel.addElement(ElementType.CONE, canvasSize.width, canvasSize.height)
                            }
                            ElementAddButton("Coach", PurpleAccent, Icons.Filled.RecordVoiceOver) {
                                viewModel.addElement(ElementType.COACH, canvasSize.width, canvasSize.height)
                            }
                            ElementAddButton("Bloqueo", Color(0xFFFF5252), Icons.Filled.Block) {
                                viewModel.addElement(ElementType.SCREEN, canvasSize.width, canvasSize.height)
                            }
                            ElementAddButton("Marca", Color(0xFFE040FB), Icons.Filled.Close) {
                                viewModel.addElement(ElementType.X_MARKER, canvasSize.width, canvasSize.height)
                            }
                        }
                    } else {
                        // Color palette + stroke width
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Color row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                viewModel.coloresDisponibles.forEach { colorHex ->
                                    val color = try { Color(colorHex) } catch (_: Exception) { OrangeBase }
                                    val isSelected = editorState.selectedColor == colorHex
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                if (isSelected) 3.dp else 1.dp,
                                                if (isSelected) Color.White else Color.White.copy(0.3f),
                                                CircleShape
                                            )
                                            .clickable { viewModel.setColor(colorHex) }
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Filled.Check, null, tint = Color.Black.copy(0.8f),
                                                modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                            // Stroke width row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("GROSOR", color = TextTertiary, fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                listOf(3f to "Fino", 6f to "Medio", 11f to "Grueso").forEach { (w, label) ->
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(30.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (editorState.strokeWidth == w) OrangeBase.copy(0.18f) else NavyElevated)
                                            .border(if (editorState.strokeWidth == w) 1.5.dp else 0.dp,
                                                OrangeBase, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setStrokeWidth(w) }
                                    ) {
                                        Text(label, fontSize = 9.sp,
                                            color = if (editorState.strokeWidth == w) OrangeBase else TextSecondary,
                                            fontWeight = if (editorState.strokeWidth == w) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }
                }
                } // end if (showToolbar)
            }
        }

        // ── Save dialog ────────────────────────────────────────────────────────
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                containerColor = NavyCard,
                shape = RoundedCornerShape(20.dp),
                title = { Text("Guardar jugada", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeBase, unfocusedBorderColor = NavyBorder,
                            focusedLabelColor = OrangeBase, unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            cursorColor = OrangeBase, focusedContainerColor = NavyElevated,
                            unfocusedContainerColor = NavyElevated
                        )
                        OutlinedTextField(
                            nombre, { nombre = it }, label = { Text("Nombre *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp), colors = fieldColors, singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            descripcion, { descripcion = it }, label = { Text("Descripcion") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp), colors = fieldColors, singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        ExposedDropdownMenuBox(categoriaExpanded, { categoriaExpanded = it }) {
                            OutlinedTextField(
                                categoria, {}, readOnly = true, label = { Text("Categoria") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoriaExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp), colors = fieldColors
                            )
                            ExposedDropdownMenu(
                                categoriaExpanded, { categoriaExpanded = false },
                                modifier = Modifier.background(NavyCard)
                            ) {
                                viewModel.categorias.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c, color = if (c == categoria) OrangeBase else TextPrimary) },
                                        onClick = { categoria = c; categoriaExpanded = false }
                                    )
                                }
                            }
                        }
                        uiState.error?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = RedError, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.saveJugada(nombre, descripcion, categoria, jugadaId) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Guardar", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showSaveDialog = false },
                        border = BorderStroke(1.dp, NavyBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancelar", color = TextSecondary) }
                }
            )
        }

        // ── Text input dialog (DrawMode.TEXT) ──────────────────────────────────
        if (editorState.showTextDialog) {
            var inputText by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { viewModel.dismissTextDialog() },
                containerColor = NavyCard,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text("Añadir texto", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                },
                text = {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Texto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangeBase, unfocusedBorderColor = NavyBorder,
                            focusedLabelColor = OrangeBase, unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            cursorColor = OrangeBase, focusedContainerColor = NavyElevated,
                            unfocusedContainerColor = NavyElevated
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmTextAnnotation(inputText) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeBase),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Añadir", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.dismissTextDialog() },
                        border = BorderStroke(1.dp, NavyBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancelar", color = TextSecondary) }
                }
            )
        }

    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── PROFESSIONAL BASKETBALL COURT ────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawProfessionalParquet() {
    val w = size.width
    val h = size.height

    // ── Base: neutral tan/beige like FIBA pro courts ────────────────────────
    drawRect(Color(0xFFCFAE72))

    // ── Horizontal planks (run across the court width — like real maple courts) ──
    val plankCount = 50
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

        // Horizontal separation line
        drawLine(Color(0xFF7A5A14).copy(0.16f), Offset(0f, y), Offset(w, y), 0.9f)

        // Staggered vertical joints
        val xOffset = if (i % 2 == 0) 0f else staggerW * 0.5f
        var x = xOffset
        while (x < w) {
            drawLine(Color(0xFF6B4E10).copy(0.12f), Offset(x, y), Offset(x, y + plankH), 0.7f)
            x += staggerW
        }
    }

    // ── Lighting — subtle center glow ───────────────────────────────────────
    drawRect(Brush.radialGradient(
        listOf(Color.White.copy(0.10f), Color.Transparent),
        center = Offset(w * 0.5f, h * 0.5f), radius = maxOf(w, h) * 0.75f
    ))
    // Edge darkening
    drawRect(Brush.horizontalGradient(
        listOf(Color.Black.copy(0.06f), Color.Transparent), startX = 0f, endX = w * 0.04f
    ))
    drawRect(Brush.horizontalGradient(
        listOf(Color.Transparent, Color.Black.copy(0.06f)), startX = w * 0.96f, endX = w
    ))
    drawRect(Brush.verticalGradient(
        listOf(Color.Black.copy(0.05f), Color.Transparent), startY = 0f, endY = h * 0.05f
    ))
    drawRect(Brush.verticalGradient(
        listOf(Color.Transparent, Color.Black.copy(0.05f)), startY = h * 0.95f, endY = h
    ))
}

private fun DrawScope.drawCourtLines(cL: Float, cT: Float, cR: Float, cB: Float, isHalf: Boolean = false) {
    val cW = cR - cL
    val cH = cB - cT
    val mX = (cL + cR) / 2f
    val sX = cW / 15.0f
    val sY = if (isHalf) cH / 14.0f else cH / 28.0f
    val lineColor = Color(0xFF1A1208)  // Very dark brown/black — pro FIBA look
    val strokeW = 4.5f

    // ── Paint colors — NeonGreen (#ADFF2F) key areas ────────────────────────
    val paintColor = Color(0xFFADFF2F).copy(0.48f)
    val paintBorder = Color(0xFF7FAA20).copy(0.30f)
    val centerFill = Color(0xFFADFF2F).copy(0.30f)

    // ── Outer boundary — thick line ─────────────────────────────────────────
    drawRect(lineColor, Offset(cL, cT), Size(cW, cH), style = Stroke(strokeW * 1.4f))

    // ── Measurement constants ───────────────────────────────────────────────
    val keyHalfW = 2.45f * sX
    val keyDepth = 5.8f * sY
    val ftCircleR = 1.8f * sX
    val ftSize = Size(ftCircleR * 2f, ftCircleR * 2f)

    if (!isHalf) {
        // ── Half-court line ─────────────────────────────────────────────────
        val mY = cT + cH / 2f
        drawLine(lineColor, Offset(cL, mY), Offset(cR, mY), strokeW)

        // ── Center circle — filled + outline ────────────────────────────────
        val centerR = 1.8f * sX
        drawCircle(centerFill, centerR, Offset(mX, mY))
        drawCircle(lineColor, centerR, Offset(mX, mY), style = Stroke(strokeW))
        drawCircle(Color(0xFFADFF2F).copy(0.40f), 7f, Offset(mX, mY))
        drawCircle(lineColor, 4f, Offset(mX, mY))

        // ── TOP HALF ────────────────────────────────────────────────────────
        drawHalfCourtMarkings(cL, cT, cR, mX, sX, sY, keyHalfW, keyDepth, ftCircleR, ftSize,
            lineColor, strokeW, paintColor, paintBorder, isTop = true)
        // ── BOTTOM HALF ─────────────────────────────────────────────────────
        drawHalfCourtMarkings(cL, cB, cR, mX, sX, sY, keyHalfW, keyDepth, ftCircleR, ftSize,
            lineColor, strokeW, paintColor, paintBorder, isTop = false)
    } else {
        // Half court mode — only bottom half
        drawHalfCourtMarkings(cL, cB, cR, mX, sX, sY, keyHalfW, keyDepth, ftCircleR, ftSize,
            lineColor, strokeW, paintColor, paintBorder, isTop = false)

        // Center arc half at top
        val centerR = 1.8f * sX
        drawCircle(centerFill, centerR, Offset(mX, cT))
        drawArc(lineColor, 0f, 180f, false,
            Offset(mX - centerR, cT - centerR), Size(centerR * 2f, centerR * 2f),
            style = Stroke(strokeW))
    }
}

private fun DrawScope.drawHalfCourtMarkings(
    cL: Float, baseline: Float, cR: Float, mX: Float,
    sX: Float, sY: Float,
    keyHalfW: Float, keyDepth: Float, ftCircleR: Float, ftSize: Size,
    lineColor: Color, strokeW: Float,
    paintColor: Color, paintBorder: Color,
    isTop: Boolean
) {
    val dir = if (isTop) 1f else -1f // Top goes down, bottom goes up
    val basketY = baseline + dir * 1.575f * sY
    val ftLineY = baseline + dir * keyDepth
    val bbHalfW = 0.915f * sX

    // ── Painted key area (filled) ───────────────────────────────────────────
    val keyTop = if (isTop) baseline else baseline - keyDepth
    drawRect(paintColor, Offset(mX - keyHalfW, keyTop), Size(keyHalfW * 2f, keyDepth))
    drawRect(paintBorder, Offset(mX - keyHalfW, keyTop), Size(keyHalfW * 2f, keyDepth), style = Stroke(1f))

    // ── Key outline ─────────────────────────────────────────────────────────
    drawRect(lineColor, Offset(mX - keyHalfW, keyTop), Size(keyHalfW * 2f, keyDepth), style = Stroke(strokeW))
    drawLine(lineColor, Offset(mX - keyHalfW, ftLineY), Offset(mX + keyHalfW, ftLineY), strokeW)

    // ── Free throw circle ───────────────────────────────────────────────────
    val ftBBox = Offset(mX - ftCircleR, ftLineY - ftCircleR)
    if (isTop) {
        drawArc(lineColor, 180f, 180f, false, ftBBox, ftSize, style = Stroke(strokeW)) // solid inside
        drawArc(lineColor, 0f, 180f, false, ftBBox, ftSize,
            style = Stroke(strokeW, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))) // dashed
    } else {
        drawArc(lineColor, 0f, 180f, false, ftBBox, ftSize, style = Stroke(strokeW)) // solid inside
        drawArc(lineColor, 180f, 180f, false, ftBBox, ftSize,
            style = Stroke(strokeW, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))) // dashed
    }

    // ── Free throw spot ─────────────────────────────────────────────────────
    drawCircle(Color(0xFFADFF2F).copy(0.45f), 9f, Offset(mX, ftLineY))
    drawCircle(lineColor, 5f, Offset(mX, ftLineY))

    // ── Backboard ───────────────────────────────────────────────────────────
    val bbY = if (isTop) baseline + 2f else baseline - 2f
    drawLine(lineColor, Offset(mX - bbHalfW, bbY), Offset(mX + bbHalfW, bbY), strokeW * 2.2f)

    // ── Basket rim ──────────────────────────────────────────────────────────
    val rimR = 11f
    drawCircle(Color(0xFFADFF2F).copy(0.20f), rimR + 5f, Offset(mX, basketY))
    drawCircle(lineColor, rimR, Offset(mX, basketY), style = Stroke(strokeW * 1.8f))
    // Net center fill
    drawCircle(Color(0xFFADFF2F).copy(0.35f), rimR * 0.45f, Offset(mX, basketY))

    // ── 3-point line ────────────────────────────────────────────────────────
    val arc3R = 6.75f
    val arcRX = arc3R * sX
    val arcRY = arc3R * sY
    val cornerInsetPx = 0.9f * sX
    val cornerLineL = cL + cornerInsetPx
    val cornerLineR = cR - cornerInsetPx
    val cornerDyM = sqrt(arc3R * arc3R - 6.6f * 6.6f)
    val cornerDyPx = cornerDyM * sY
    val cornerAngleDeg = Math.toDegrees(atan2(cornerDyM.toDouble(), 6.6.toDouble())).toFloat()
    val sweepAngle3pt = 180f - 2f * cornerAngleDeg

    if (isTop) {
        val cornerEndY = basketY + cornerDyPx
        drawLine(lineColor, Offset(cornerLineL, baseline), Offset(cornerLineL, cornerEndY), strokeW)
        drawLine(lineColor, Offset(cornerLineR, baseline), Offset(cornerLineR, cornerEndY), strokeW)
        drawArc(lineColor, cornerAngleDeg, sweepAngle3pt, false,
            Offset(mX - arcRX, basketY - arcRY), Size(arcRX * 2f, arcRY * 2f), style = Stroke(strokeW))
    } else {
        val cornerEndY = basketY - cornerDyPx
        drawLine(lineColor, Offset(cornerLineL, baseline), Offset(cornerLineL, cornerEndY), strokeW)
        drawLine(lineColor, Offset(cornerLineR, baseline), Offset(cornerLineR, cornerEndY), strokeW)
        drawArc(lineColor, 180f + cornerAngleDeg, sweepAngle3pt, false,
            Offset(mX - arcRX, basketY - arcRY), Size(arcRX * 2f, arcRY * 2f), style = Stroke(strokeW))
    }

    // ── Lane space hash marks ───────────────────────────────────────────────
    val markLen = minOf(size.width, size.height) * 0.028f
    val laneMarkMeters = listOf(0.85f, 1.75f, 2.65f, 3.55f)
    laneMarkMeters.forEach { dist ->
        val y = baseline + dir * dist * sY
        drawLine(lineColor, Offset(mX - keyHalfW - markLen, y), Offset(mX - keyHalfW, y), strokeW * 0.9f)
        drawLine(lineColor, Offset(mX + keyHalfW, y), Offset(mX + keyHalfW + markLen, y), strokeW * 0.9f)
    }

    // ── Restricted area (no-charge zone) r=1.25m ────────────────────────────
    val restrictedR = 1.25f * sX
    if (isTop) {
        drawArc(lineColor, 0f, 180f, false,
            Offset(mX - restrictedR, basketY - restrictedR),
            Size(restrictedR * 2f, restrictedR * 2f), style = Stroke(strokeW * 0.9f))
    } else {
        drawArc(lineColor, 180f, 180f, false,
            Offset(mX - restrictedR, basketY - restrictedR),
            Size(restrictedR * 2f, restrictedR * 2f), style = Stroke(strokeW * 0.9f))
    }
}

private fun DrawScope.drawBasketballCourt() {
    drawProfessionalParquet()
    val pad = 16f
    drawCourtLines(pad, pad, size.width - pad, size.height - pad, isHalf = false)
}

private fun DrawScope.drawHalfCourt() {
    drawProfessionalParquet()
    val pad = 16f
    val availW = size.width - 2 * pad
    val availH = size.height - 2 * pad
    // Maintain real half-court aspect ratio: 15m wide × 14m tall
    val courtAspect = 15f / 14f
    val canvasAspect = availW / availH
    val cW: Float
    val cH: Float
    if (canvasAspect > courtAspect) {
        cH = availH
        cW = cH * courtAspect
    } else {
        cW = availW
        cH = cW / courtAspect
    }
    val cL = (size.width - cW) / 2f
    val cT = (size.height - cH) / 2f
    drawCourtLines(cL, cT, cL + cW, cT + cH, isHalf = true)
}

// ══════════════════════════════════════════════════════════════════════════════
// ── PATH DRAWING (includes curved + zigzag + zone) ───────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawPaths(
    paths: List<DrawPath>,
    currentPath: List<DrawPoint>,
    selectedColor: Long,
    strokeWidth: Float,
    drawMode: DrawMode = DrawMode.PEN
) {
    paths.forEach { path ->
        val color = try { Color(path.colorHex) } catch (_: Exception) { OrangeBase }
        when {
            path.isZone -> {
                if (path.points.size >= 2) drawZoneRect(path.points[0], path.points[1], color)
            }
            path.isZigzag -> drawZigzagPath(path.points, color, path.strokeWidth)
            path.isCurved -> drawCurvedPath(path, color)
            else -> {
                if (path.points.size >= 2) drawNormalPath(path, color)
            }
        }
    }

    // Live preview while drawing
    if (drawMode == DrawMode.ZONE && currentPath.size == 2) {
        // Show zone preview
        val color = try { Color(selectedColor) } catch (_: Exception) { OrangeBase }
        drawZoneRect(currentPath[0], currentPath[1], color)
    } else if (currentPath.size >= 2 && drawMode != DrawMode.ZONE) {
        val drawPath = Path().apply {
            currentPath.forEachIndexed { i, pt ->
                if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
            }
        }
        val color = try { Color(selectedColor) } catch (_: Exception) { OrangeBase }
        drawPath(drawPath, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private fun DrawScope.drawZoneRect(start: DrawPoint, end: DrawPoint, color: Color) {
    val left = minOf(start.x, end.x)
    val top = minOf(start.y, end.y)
    val right = maxOf(start.x, end.x)
    val bottom = maxOf(start.y, end.y)
    val size = Size(right - left, bottom - top)
    if (size.width < 4f || size.height < 4f) return

    // Filled semi-transparent rectangle
    drawRect(
        color = color.copy(alpha = 0.18f),
        topLeft = Offset(left, top),
        size = size
    )
    // Colored border
    drawRect(
        color = color.copy(alpha = 0.85f),
        topLeft = Offset(left, top),
        size = size,
        style = Stroke(width = 3f)
    )
}

private fun DrawScope.drawNormalPath(path: DrawPath, color: Color) {
    val drawPath = Path().apply {
        path.points.forEachIndexed { i, pt ->
            if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
        }
    }
    val pathEffect = if (path.isDashed) PathEffect.dashPathEffect(floatArrayOf(18f, 10f)) else null
    drawPath(drawPath, color,
        style = Stroke(width = path.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = pathEffect))

    // Arrow heads — use look-back of up to 8 points for a stable direction
    if ((path.arrowType == ArrowType.END || path.arrowType == ArrowType.BOTH || path.isArrow) && path.points.size >= 2) {
        val lookBack = minOf(4, path.points.size - 1)
        drawArrowHead(path.points.last(), path.points[path.points.size - 1 - lookBack], color, path.strokeWidth)
    }
    if (path.arrowType == ArrowType.BOTH && path.points.size >= 2) {
        val lookBack = minOf(4, path.points.size - 1)
        drawArrowHead(path.points.first(), path.points[lookBack], color, path.strokeWidth)
    }
}

private fun DrawScope.drawCurvedPath(path: DrawPath, color: Color) {
    if (path.points.size < 2) return
    // Smooth the points into a cubic bezier curve
    val pts = path.points
    val smoothPath = Path().apply {
        moveTo(pts[0].x, pts[0].y)
        if (pts.size == 2) {
            lineTo(pts[1].x, pts[1].y)
        } else {
            // Use cubic bezier with smoothed control points
            for (i in 1 until pts.size) {
                val prev = pts[i - 1]
                val curr = pts[i]
                val midX = (prev.x + curr.x) / 2f
                val midY = (prev.y + curr.y) / 2f
                quadraticBezierTo(prev.x, prev.y, midX, midY)
            }
            val last = pts.last()
            lineTo(last.x, last.y)
        }
    }
    drawPath(smoothPath, color,
        style = Stroke(width = path.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Arrow head at end — look back further for stable direction
    if (path.points.size >= 2) {
        val lookBack = minOf(4, path.points.size - 1)
        drawArrowHead(path.points.last(), path.points[path.points.size - 1 - lookBack], color, path.strokeWidth)
    }
}

private fun DrawScope.drawZigzagPath(points: List<DrawPoint>, color: Color, strokeW: Float) {
    if (points.size < 2) return
    // Sample points along the path at regular intervals, then zigzag between them
    val first = points.first()
    val last = points.last()
    val dx = last.x - first.x
    val dy = last.y - first.y
    val length = sqrt(dx * dx + dy * dy)
    if (length < 1f) return

    val zigCount = maxOf(4, (length / 22f).toInt())
    val dirX = dx / length
    val dirY = dy / length
    // Perpendicular direction
    val perpX = -dirY
    val perpY = dirX
    val amplitude = strokeW * 2.5f

    val zigPath = Path().apply {
        moveTo(first.x, first.y)
        for (i in 1..zigCount) {
            val t = i.toFloat() / zigCount
            val baseX = first.x + dx * t
            val baseY = first.y + dy * t
            if (i < zigCount) {
                val side = if (i % 2 == 0) amplitude else -amplitude
                lineTo(baseX + perpX * side, baseY + perpY * side)
            } else {
                lineTo(baseX, baseY)
            }
        }
    }
    drawPath(zigPath, color,
        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawArrowHead(
    last: DrawPoint, prev: DrawPoint, color: Color, strokeWidth: Float
) {
    val angle = atan2((last.y - prev.y).toDouble(), (last.x - prev.x).toDouble())
    // Keep arrowhead proportional but capped: looks clean on all stroke widths
    val arrowLen = maxOf(14f, minOf(strokeWidth * 4f, 24f))
    val arrowAngle = Math.PI / 6  // 30° — clean, classic arrow shape
    val ax1 = last.x - arrowLen * cos(angle - arrowAngle).toFloat()
    val ay1 = last.y - arrowLen * sin(angle - arrowAngle).toFloat()
    val ax2 = last.x - arrowLen * cos(angle + arrowAngle).toFloat()
    val ay2 = last.y - arrowLen * sin(angle + arrowAngle).toFloat()
    // Filled solid arrowhead
    val arrowPath = Path().apply {
        moveTo(last.x, last.y)
        lineTo(ax1, ay1)
        lineTo(ax2, ay2)
        close()
    }
    drawPath(arrowPath, color)
}

// ══════════════════════════════════════════════════════════════════════════════
// ── BOARD ELEMENT TOKENS ─────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun BoardElementToken(
    element: BoardElement,
    isMovable: Boolean,
    onDrag: (Float, Float) -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier.offset { IntOffset(element.x.toInt(), element.y.toInt()) }
    ) {
        val dragModifier = if (isMovable) {
            Modifier.pointerInput(element.id) {
                detectDragGestures { _, dragAmount -> onDrag(dragAmount.x, dragAmount.y) }
            }
        } else Modifier

        Box(modifier = dragModifier) {
            when (element.type) {
                ElementType.PLAYER_HOME -> PlayerToken(element.number, OrangeBase)
                ElementType.PLAYER_AWAY -> PlayerToken(element.number, TealAccent)
                ElementType.BALL -> BallToken()
                ElementType.CONE -> ConeToken()
                ElementType.COACH -> CoachToken()
                ElementType.SCREEN -> ScreenToken()
                ElementType.X_MARKER -> XMarkerToken()
            }
        }

        // Delete X button — only visible in MOVE mode
        if (isMovable) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(RedError)
                    .clickable { onRemove() }
            ) {
                Text("x", color = Color.White, fontSize = 11.sp, lineHeight = 11.sp,
                    fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── TEXT ANNOTATION TOKEN ─────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun TextAnnotationToken(
    annotation: TextAnnotation,
    isMovable: Boolean,
    onDrag: (Float, Float) -> Unit,
    onRemove: () -> Unit
) {
    val color = try { Color(annotation.colorHex) } catch (_: Exception) { Color.White }
    Box(
        modifier = Modifier.offset { IntOffset(annotation.x.toInt(), annotation.y.toInt()) }
    ) {
        val dragModifier = if (isMovable) {
            Modifier.pointerInput(annotation.id) {
                detectDragGestures { _, dragAmount -> onDrag(dragAmount.x, dragAmount.y) }
            }
        } else Modifier

        Box(
            modifier = dragModifier
                .background(Color.Black.copy(0.45f), RoundedCornerShape(6.dp))
                .border(1.dp, color.copy(0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = annotation.text,
                color = color,
                fontSize = annotation.fontSize.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Delete X button — only visible in MOVE mode
        if (isMovable) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(RedError)
                    .clickable { onRemove() }
            ) {
                Text("x", color = Color.White, fontSize = 11.sp, lineHeight = 11.sp,
                    fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun PlayerToken(number: Int, color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(color)
            .border(2.5.dp, Color.White.copy(0.95f), CircleShape)
    ) {
        Text("$number", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
private fun BallToken() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFFE87020))
            .border(2.dp, Color(0xFFCC4400), CircleShape)
    ) {
        Icon(Icons.Filled.SportsBasketball, null, tint = Color.White.copy(0.9f), modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ConeToken() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFFCC00))
            .border(2.dp, Color(0xFFCC9900), RoundedCornerShape(6.dp))
    ) {
        Icon(Icons.Filled.ChangeHistory, null, tint = Color(0xFF7A5500), modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun CoachToken() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(PurpleAccent)
            .border(2.dp, Color.White.copy(0.6f), RoundedCornerShape(10.dp))
    ) {
        Icon(Icons.Filled.RecordVoiceOver, null, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ScreenToken() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(48.dp)
            .height(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFFF5252))
            .border(1.5.dp, Color.White.copy(0.7f), RoundedCornerShape(4.dp))
    ) {
        Text("SCR", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp)
    }
}

@Composable
private fun XMarkerToken() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFFE040FB).copy(0.85f))
            .border(2.dp, Color.White.copy(0.7f), CircleShape)
    ) {
        Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

// ── Toolbar Components ────────────────────────────────────────────────────────

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    activeColor: Color = OrangeBase,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) activeColor.copy(0.2f) else NavyElevated)
                .border(if (selected) 1.5.dp else 0.dp, activeColor, RoundedCornerShape(10.dp))
        ) {
            Icon(icon, null, tint = if (selected) activeColor else TextSecondary, modifier = Modifier.size(18.dp))
        }
        Text(label, color = if (selected) activeColor else TextTertiary, fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun ElementAddButton(label: String, color: Color, icon: ImageVector? = null, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(0.20f))
                .border(2.dp, color.copy(0.65f), RoundedCornerShape(16.dp))
        ) {
            if (icon != null) {
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            } else {
                // Player token: solid circle with +
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(2.dp, Color.White.copy(0.85f), CircleShape)
                ) {
                    Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
    }
}
