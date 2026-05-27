package com.example.tfg_aaron.ui.screens.pizarra

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.PizarraJugadaEntity
import com.example.tfg_aaron.data.repository.PizarraRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

// ── Drawing ──────────────────────────────────────────────────────────────────

enum class ArrowType { NONE, END, BOTH }

data class DrawPath(
    val points: List<DrawPoint>,
    val colorHex: Long = 0xFFFF4500,
    val strokeWidth: Float = 6f,
    val isArrow: Boolean = false,
    val isDashed: Boolean = false,
    val arrowType: ArrowType = ArrowType.NONE,
    val isCurved: Boolean = false,
    val isZigzag: Boolean = false,
    val isZone: Boolean = false   // NEW: filled semi-transparent zone rectangle
)

data class DrawPoint(val x: Float, val y: Float)

enum class DrawMode { PEN, ARROW, ARROW_DOUBLE, ERASER, MOVE, DASHED, CURVED, ZIGZAG, ZONE, TEXT, ANIMATE }

// ── Text Annotation ───────────────────────────────────────────────────────────

data class TextAnnotation(
    val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val text: String,
    val colorHex: Long = 0xFFFFFFFF,
    val fontSize: Float = 14f
)

// ── Board Elements ────────────────────────────────────────────────────────────

enum class ElementType { PLAYER_HOME, PLAYER_AWAY, BALL, CONE, COACH, SCREEN, X_MARKER }

data class BoardElement(
    val id: String = UUID.randomUUID().toString(),
    val type: ElementType,
    val x: Float,
    val y: Float,
    val number: Int = 1
)

// ── State ─────────────────────────────────────────────────────────────────────

data class PizarraUiState(
    val jugadas: List<PizarraJugadaEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

data class EditorState(
    val paths: List<DrawPath> = emptyList(),
    val currentPath: List<DrawPoint> = emptyList(),
    val selectedColor: Long = 0xFFFF4500,
    val strokeWidth: Float = 6f,
    val drawMode: DrawMode = DrawMode.PEN,
    val elements: List<BoardElement> = emptyList(),
    val nextHomeNumber: Int = 1,
    val nextAwayNumber: Int = 1,
    val isHalfCourt: Boolean = false,
    // Zone drawing: track start point separately
    val zoneStart: DrawPoint? = null,
    // Text annotation
    val textAnnotations: List<TextAnnotation> = emptyList(),
    val showTextDialog: Boolean = false,
    val pendingTextPosition: Offset? = null,
    // Animate stub
    val showAnimateSnackbar: Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class PizarraViewModel(
    private val entrenadorId: Int,
    private val pizarraRepository: PizarraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PizarraUiState())
    val uiState: StateFlow<PizarraUiState> = _uiState.asStateFlow()

    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    val categorias = listOf("ATAQUE", "DEFENSA", "BLOQUEO", "TRANSICION")
    val coloresDisponibles = listOf(
        0xFFFF4500L, // orange
        0xFF00E676L, // green
        0xFFFF3355L, // red
        0xFF0A84FFL, // blue
        0xFFFFFFFFL, // white
        0xFFFFB800L, // gold
        0xFFAA5CFFL, // purple
        0xFF00D4FFL  // teal
    )

    init { loadJugadas() }

    private fun loadJugadas() {
        viewModelScope.launch {
            pizarraRepository.getAll(entrenadorId).collect { list ->
                _uiState.update { it.copy(jugadas = list) }
            }
        }
    }

    fun loadJugadaForEdit(id: Int) {
        viewModelScope.launch {
            val jugada = pizarraRepository.getById(id) ?: return@launch
            if (jugada.dibujoData.isNotEmpty()) {
                try {
                    val (paths, elements) = jsonToState(jugada.dibujoData)
                    _editorState.update { it.copy(paths = paths, elements = elements) }
                } catch (_: Exception) {}
            }
        }
    }

    // ── Drawing ──────────────────────────────────────────────────────────────

    fun startPath(offset: Offset) {
        val mode = _editorState.value.drawMode
        if (mode == DrawMode.MOVE || mode == DrawMode.TEXT || mode == DrawMode.ANIMATE) return
        if (mode == DrawMode.ZONE) {
            val pt = DrawPoint(offset.x, offset.y)
            _editorState.update { it.copy(zoneStart = pt, currentPath = listOf(pt)) }
        } else {
            _editorState.update { it.copy(currentPath = listOf(DrawPoint(offset.x, offset.y))) }
        }
    }

    fun addPoint(offset: Offset) {
        val mode = _editorState.value.drawMode
        if (mode == DrawMode.MOVE || mode == DrawMode.TEXT || mode == DrawMode.ANIMATE) return
        if (mode == DrawMode.ZONE) {
            // currentPath stores [start, end] for live preview
            val start = _editorState.value.zoneStart ?: return
            _editorState.update {
                it.copy(currentPath = listOf(start, DrawPoint(offset.x, offset.y)))
            }
        } else {
            _editorState.update { it.copy(currentPath = it.currentPath + DrawPoint(offset.x, offset.y)) }
        }
    }

    fun endPath() {
        val state = _editorState.value
        val mode = state.drawMode
        if (mode == DrawMode.MOVE || mode == DrawMode.TEXT || mode == DrawMode.ANIMATE) return

        if (mode == DrawMode.ZONE) {
            val pts = state.currentPath
            if (pts.size == 2) {
                val newZone = DrawPath(
                    points = pts,
                    colorHex = state.selectedColor,
                    strokeWidth = state.strokeWidth,
                    isZone = true
                )
                _editorState.update { it.copy(paths = it.paths + newZone, currentPath = emptyList(), zoneStart = null) }
            } else {
                _editorState.update { it.copy(currentPath = emptyList(), zoneStart = null) }
            }
            return
        }

        if (state.currentPath.size < 2) {
            _editorState.update { it.copy(currentPath = emptyList()) }
            return
        }

        if (mode == DrawMode.ERASER) {
            val eraserRadius = state.strokeWidth * 6f
            val remaining = state.paths.filter { path ->
                path.points.none { pt ->
                    state.currentPath.any { ep ->
                        val dx = pt.x - ep.x
                        val dy = pt.y - ep.y
                        (dx * dx + dy * dy) < (eraserRadius * eraserRadius)
                    }
                }
            }
            _editorState.update { it.copy(paths = remaining, currentPath = emptyList()) }
            return
        }

        val arrowType = when (mode) {
            DrawMode.ARROW -> ArrowType.END
            DrawMode.ARROW_DOUBLE -> ArrowType.BOTH
            DrawMode.CURVED -> ArrowType.END
            else -> ArrowType.NONE
        }
        val isDashed = mode == DrawMode.DASHED
        val isCurved = mode == DrawMode.CURVED
        val isZigzag = mode == DrawMode.ZIGZAG
        val newPath = DrawPath(
            points = state.currentPath,
            colorHex = state.selectedColor,
            strokeWidth = state.strokeWidth,
            isArrow = arrowType != ArrowType.NONE,
            isDashed = isDashed,
            arrowType = arrowType,
            isCurved = isCurved,
            isZigzag = isZigzag
        )
        _editorState.update { it.copy(paths = it.paths + newPath, currentPath = emptyList()) }
    }

    fun undo() {
        _editorState.update { it.copy(paths = it.paths.dropLast(1)) }
    }

    fun clearCanvas() {
        _editorState.update {
            it.copy(
                paths = emptyList(),
                currentPath = emptyList(),
                elements = emptyList(),
                textAnnotations = emptyList(),
                zoneStart = null
            )
        }
    }

    fun setColor(color: Long) { _editorState.update { it.copy(selectedColor = color) } }

    fun setDrawMode(mode: DrawMode) {
        if (mode == DrawMode.ANIMATE) {
            _editorState.update { it.copy(showAnimateSnackbar = true) }
            return
        }
        _editorState.update { it.copy(drawMode = mode) }
    }

    fun setStrokeWidth(width: Float) { _editorState.update { it.copy(strokeWidth = width) } }

    fun toggleHalfCourt() { _editorState.update { it.copy(isHalfCourt = !it.isHalfCourt) } }

    fun dismissAnimateSnackbar() { _editorState.update { it.copy(showAnimateSnackbar = false) } }

    // ── Text Annotations ─────────────────────────────────────────────────────

    fun requestTextAt(offset: Offset) {
        _editorState.update { it.copy(pendingTextPosition = offset, showTextDialog = true) }
    }

    fun confirmTextAnnotation(text: String) {
        val state = _editorState.value
        val pos = state.pendingTextPosition ?: return
        if (text.isNotBlank()) {
            val annotation = TextAnnotation(
                x = pos.x,
                y = pos.y,
                text = text.trim(),
                colorHex = state.selectedColor,
                fontSize = 14f
            )
            _editorState.update {
                it.copy(
                    textAnnotations = it.textAnnotations + annotation,
                    showTextDialog = false,
                    pendingTextPosition = null
                )
            }
        } else {
            dismissTextDialog()
        }
    }

    fun dismissTextDialog() {
        _editorState.update { it.copy(showTextDialog = false, pendingTextPosition = null) }
    }

    fun moveTextAnnotation(id: String, dx: Float, dy: Float) {
        _editorState.update { state ->
            state.copy(textAnnotations = state.textAnnotations.map { ann ->
                if (ann.id == id) ann.copy(x = ann.x + dx, y = ann.y + dy) else ann
            })
        }
    }

    fun removeTextAnnotation(id: String) {
        _editorState.update { it.copy(textAnnotations = it.textAnnotations.filter { ann -> ann.id != id }) }
    }

    // ── Elements ─────────────────────────────────────────────────────────────

    fun addElement(type: ElementType, canvasW: Float, canvasH: Float) {
        val state = _editorState.value
        // Place en la zona superior visible (no detrás del toolbar overlay)
        val offset = state.elements.size * 20f
        val cx = canvasW / 2f - 22f + offset
        val cy = canvasH * 0.30f - 22f + offset
        val number = when (type) {
            ElementType.PLAYER_HOME -> state.nextHomeNumber
            ElementType.PLAYER_AWAY -> state.nextAwayNumber
            else -> 0
        }
        val newElement = BoardElement(type = type, x = cx, y = cy, number = number)
        _editorState.update {
            it.copy(
                elements = it.elements + newElement,
                nextHomeNumber = if (type == ElementType.PLAYER_HOME) it.nextHomeNumber + 1 else it.nextHomeNumber,
                nextAwayNumber = if (type == ElementType.PLAYER_AWAY) it.nextAwayNumber + 1 else it.nextAwayNumber
            )
        }
    }

    fun moveElement(id: String, dx: Float, dy: Float) {
        _editorState.update { state ->
            state.copy(elements = state.elements.map { el ->
                if (el.id == id) el.copy(x = el.x + dx, y = el.y + dy) else el
            })
        }
    }

    fun removeElement(id: String) {
        _editorState.update { it.copy(elements = it.elements.filter { el -> el.id != id }) }
    }

    // ── Play Templates ────────────────────────────────────────────────────────

    /**
     * Adds a predefined basketball play template.
     * Positions are stored as fractions of canvas width/height.
     */
    fun applyTemplate(template: PlayTemplate, canvasW: Float, canvasH: Float) {
        when (template) {
            PlayTemplate.PICK_AND_ROLL -> applyPickAndRoll(canvasW, canvasH)
            PlayTemplate.FASTBREAK -> applyFastbreak(canvasW, canvasH)
            PlayTemplate.ZONE_DEFENSE_2_3 -> applyZoneDefense23(canvasW, canvasH)
            PlayTemplate.MOTION_OFFENSE -> applyMotionOffense(canvasW, canvasH)
        }
    }

    private fun applyPickAndRoll(w: Float, h: Float) {
        val state = _editorState.value
        // Ball handler at top of key — attacking BOTTOM basket (~h*0.94)
        // FT line ≈ h*0.79, 3pt top ≈ h*0.70
        val ballHandler = BoardElement(type = ElementType.PLAYER_HOME, x = w * 0.50f - 21f, y = h * 0.72f, number = state.nextHomeNumber)
        // Screener at elbow
        val screener = BoardElement(type = ElementType.PLAYER_HOME, x = w * 0.37f - 21f, y = h * 0.78f, number = state.nextHomeNumber + 1)
        // Screen arrow: from screener position toward ball handler
        val screenArrow = DrawPath(
            points = listOf(
                DrawPoint(screener.x + 21f, screener.y + 21f),
                DrawPoint(ballHandler.x + 21f, ballHandler.y + 21f)
            ),
            colorHex = 0xFFFFB800L,
            strokeWidth = 5f,
            isArrow = true,
            arrowType = ArrowType.END,
            isDashed = true
        )
        // Roll arrow: screener rolls to basket
        val rollArrow = DrawPath(
            points = listOf(
                DrawPoint(screener.x + 21f, screener.y + 21f),
                DrawPoint(w * 0.50f, h * 0.90f)
            ),
            colorHex = 0xFFFF4500L,
            strokeWidth = 5f,
            isArrow = true,
            arrowType = ArrowType.END,
            isCurved = true
        )
        _editorState.update {
            it.copy(
                elements = it.elements + listOf(ballHandler, screener),
                paths = it.paths + listOf(screenArrow, rollArrow),
                nextHomeNumber = it.nextHomeNumber + 2
            )
        }
    }

    private fun applyFastbreak(w: Float, h: Float) {
        val state = _editorState.value
        val baseNum = state.nextHomeNumber
        // 5 players in transition attacking BOTTOM basket — start from mid-court
        val positions = listOf(
            DrawPoint(w * 0.50f - 21f, h * 0.55f), // P1 - ball handler (center)
            DrawPoint(w * 0.18f - 21f, h * 0.60f), // P2 - left wing
            DrawPoint(w * 0.82f - 21f, h * 0.60f), // P3 - right wing
            DrawPoint(w * 0.30f - 21f, h * 0.52f), // P4 - left trailer
            DrawPoint(w * 0.70f - 21f, h * 0.52f)  // P5 - right trailer
        )
        val players = positions.mapIndexed { i, pt ->
            BoardElement(type = ElementType.PLAYER_HOME, x = pt.x, y = pt.y, number = baseNum + i)
        }
        // Movement arrows toward bottom basket
        val basketX = w * 0.50f
        val basketY = h * 0.90f
        val arrows = positions.map { pt ->
            DrawPath(
                points = listOf(DrawPoint(pt.x + 21f, pt.y + 21f), DrawPoint(basketX, basketY)),
                colorHex = 0xFFFF4500L,
                strokeWidth = 4f,
                isArrow = true,
                arrowType = ArrowType.END
            )
        }
        _editorState.update {
            it.copy(
                elements = it.elements + players,
                paths = it.paths + arrows,
                nextHomeNumber = it.nextHomeNumber + 5
            )
        }
    }

    private fun applyZoneDefense23(w: Float, h: Float) {
        val state = _editorState.value
        val baseNum = state.nextAwayNumber
        // 2-3 zone defending BOTTOM basket (basket ≈ h*0.94, FT line ≈ h*0.79)
        // 2 guards: top of the zone just above/inside the 3pt line (~h*0.74)
        // 3 bigs: spread across the paint near the baseline (~h*0.86)
        val positions = listOf(
            DrawPoint(w * 0.36f - 21f, h * 0.74f), // D1 guard left
            DrawPoint(w * 0.64f - 21f, h * 0.74f), // D2 guard right
            DrawPoint(w * 0.20f - 21f, h * 0.86f), // D3 big left post
            DrawPoint(w * 0.50f - 21f, h * 0.84f), // D4 big center (paint)
            DrawPoint(w * 0.80f - 21f, h * 0.86f)  // D5 big right post
        )
        val players = positions.mapIndexed { i, pt ->
            BoardElement(type = ElementType.PLAYER_AWAY, x = pt.x, y = pt.y, number = baseNum + i)
        }
        // Zone area highlight covering the defensive area
        val zoneRect = DrawPath(
            points = listOf(
                DrawPoint(w * 0.14f, h * 0.70f),
                DrawPoint(w * 0.86f, h * 0.93f)
            ),
            colorHex = 0xFF00D4FFL,
            strokeWidth = 3f,
            isZone = true
        )
        _editorState.update {
            it.copy(
                elements = it.elements + players,
                paths = it.paths + listOf(zoneRect),
                nextAwayNumber = it.nextAwayNumber + 5
            )
        }
    }

    private fun applyMotionOffense(w: Float, h: Float) {
        val state = _editorState.value
        val baseNum = state.nextHomeNumber
        // 5-out motion offense attacking BOTTOM basket
        val positions = listOf(
            DrawPoint(w * 0.50f - 21f, h * 0.73f), // P1 - point guard (top key)
            DrawPoint(w * 0.22f - 21f, h * 0.77f), // P2 - left wing
            DrawPoint(w * 0.78f - 21f, h * 0.77f), // P3 - right wing
            DrawPoint(w * 0.15f - 21f, h * 0.88f), // P4 - left corner
            DrawPoint(w * 0.85f - 21f, h * 0.88f)  // P5 - right corner
        )
        val players = positions.mapIndexed { i, pt ->
            BoardElement(type = ElementType.PLAYER_HOME, x = pt.x, y = pt.y, number = baseNum + i)
        }
        // Pass arrows (P1->P2, P2 cuts to basket, P1->P3)
        val passArrows = listOf(
            DrawPath(
                points = listOf(
                    DrawPoint(positions[0].x + 21f, positions[0].y + 21f),
                    DrawPoint(positions[1].x + 21f, positions[1].y + 21f)
                ),
                colorHex = 0xFFFFB800L, strokeWidth = 4f, isArrow = true, arrowType = ArrowType.END, isDashed = true
            ),
            DrawPath(
                points = listOf(
                    DrawPoint(positions[1].x + 21f, positions[1].y + 21f),
                    DrawPoint(w * 0.50f, h * 0.91f)
                ),
                colorHex = 0xFFFF4500L, strokeWidth = 4f, isArrow = true, arrowType = ArrowType.END, isCurved = true
            ),
            DrawPath(
                points = listOf(
                    DrawPoint(positions[0].x + 21f, positions[0].y + 21f),
                    DrawPoint(positions[2].x + 21f, positions[2].y + 21f)
                ),
                colorHex = 0xFFFFB800L, strokeWidth = 4f, isArrow = true, arrowType = ArrowType.END, isDashed = true
            )
        )
        _editorState.update {
            it.copy(
                elements = it.elements + players,
                paths = it.paths + passArrows,
                nextHomeNumber = it.nextHomeNumber + 5
            )
        }
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    fun saveJugada(nombre: String, descripcion: String, categoria: String, jugadaId: Int = -1) {
        if (nombre.isBlank()) { _uiState.update { it.copy(error = "El nombre es obligatorio") }; return }
        viewModelScope.launch {
            val state = _editorState.value
            val dibujoData = try { stateToJson(state.paths, state.elements, state.textAnnotations) } catch (_: Exception) { "" }
            if (jugadaId > 0) {
                pizarraRepository.getById(jugadaId)?.let { existing ->
                    pizarraRepository.update(
                        existing.copy(
                            nombreJugada = nombre.trim(), descripcion = descripcion.trim(),
                            categoria = categoria, dibujoData = dibujoData,
                            ultimaModificacion = System.currentTimeMillis()
                        )
                    )
                }
            } else {
                pizarraRepository.save(
                    PizarraJugadaEntity(
                        idEntrenador = entrenadorId, nombreJugada = nombre.trim(),
                        descripcion = descripcion.trim(), categoria = categoria, dibujoData = dibujoData
                    )
                )
            }
            _uiState.update { it.copy(successMessage = "Jugada guardada", error = null) }
        }
    }

    fun deleteJugada(jugada: PizarraJugadaEntity) {
        viewModelScope.launch { pizarraRepository.delete(jugada) }
    }

    fun clearMessages() { _uiState.update { it.copy(error = null, successMessage = null) } }

    fun resetEditor() { _editorState.update { EditorState() } }

    // ── JSON ─────────────────────────────────────────────────────────────────

    private fun stateToJson(
        paths: List<DrawPath>,
        elements: List<BoardElement>,
        textAnnotations: List<TextAnnotation> = emptyList()
    ): String {
        val root = JSONObject()
        root.put("v", 3)
        val pathsArr = JSONArray()
        paths.forEach { path ->
            val obj = JSONObject()
            val pts = JSONArray()
            path.points.forEach { pt ->
                val p = JSONObject(); p.put("x", pt.x); p.put("y", pt.y); pts.put(p)
            }
            obj.put("points", pts)
            obj.put("colorHex", path.colorHex)
            obj.put("strokeWidth", path.strokeWidth)
            obj.put("isArrow", path.isArrow)
            obj.put("isDashed", path.isDashed)
            obj.put("arrowType", path.arrowType.name)
            obj.put("isCurved", path.isCurved)
            obj.put("isZigzag", path.isZigzag)
            obj.put("isZone", path.isZone)
            pathsArr.put(obj)
        }
        root.put("paths", pathsArr)
        val elemsArr = JSONArray()
        elements.forEach { el ->
            val obj = JSONObject()
            obj.put("id", el.id)
            obj.put("type", el.type.name)
            obj.put("x", el.x)
            obj.put("y", el.y)
            obj.put("number", el.number)
            elemsArr.put(obj)
        }
        root.put("elements", elemsArr)
        val textsArr = JSONArray()
        textAnnotations.forEach { ann ->
            val obj = JSONObject()
            obj.put("id", ann.id)
            obj.put("x", ann.x)
            obj.put("y", ann.y)
            obj.put("text", ann.text)
            obj.put("colorHex", ann.colorHex)
            obj.put("fontSize", ann.fontSize)
            textsArr.put(obj)
        }
        root.put("textAnnotations", textsArr)
        return root.toString()
    }

    private fun jsonToState(json: String): Pair<List<DrawPath>, List<BoardElement>> {
        return try {
            // Try new format (v2/v3 object)
            val root = JSONObject(json)
            val paths = parsePaths(root.getJSONArray("paths"))
            val elementsArr = if (root.has("elements")) root.getJSONArray("elements") else JSONArray()
            val elements = (0 until elementsArr.length()).mapNotNull { i ->
                try {
                    val obj = elementsArr.getJSONObject(i)
                    BoardElement(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        type = ElementType.valueOf(obj.getString("type")),
                        x = obj.getDouble("x").toFloat(),
                        y = obj.getDouble("y").toFloat(),
                        number = obj.optInt("number", 1)
                    )
                } catch (_: Exception) { null }
            }
            // Parse text annotations if present
            val textsArr = if (root.has("textAnnotations")) root.getJSONArray("textAnnotations") else JSONArray()
            val textAnnotations = (0 until textsArr.length()).mapNotNull { i ->
                try {
                    val obj = textsArr.getJSONObject(i)
                    TextAnnotation(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        x = obj.getDouble("x").toFloat(),
                        y = obj.getDouble("y").toFloat(),
                        text = obj.getString("text"),
                        colorHex = obj.getLong("colorHex"),
                        fontSize = obj.optDouble("fontSize", 14.0).toFloat()
                    )
                } catch (_: Exception) { null }
            }
            if (textAnnotations.isNotEmpty()) {
                _editorState.update { it.copy(textAnnotations = textAnnotations) }
            }
            Pair(paths, elements)
        } catch (_: Exception) {
            // Try old format (raw array)
            try {
                Pair(parsePaths(JSONArray(json)), emptyList())
            } catch (_: Exception) {
                Pair(emptyList(), emptyList())
            }
        }
    }

    private fun parsePaths(arr: JSONArray): List<DrawPath> {
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            val ptsArr = obj.getJSONArray("points")
            val pts = (0 until ptsArr.length()).map { j ->
                val p = ptsArr.getJSONObject(j)
                DrawPoint(p.getDouble("x").toFloat(), p.getDouble("y").toFloat())
            }
            val isArrowLegacy = obj.optBoolean("isArrow", false)
            DrawPath(
                points = pts,
                colorHex = obj.getLong("colorHex"),
                strokeWidth = obj.getDouble("strokeWidth").toFloat(),
                isArrow = isArrowLegacy,
                isDashed = obj.optBoolean("isDashed", false),
                arrowType = try {
                    ArrowType.valueOf(obj.optString("arrowType", "NONE"))
                } catch (_: Exception) {
                    if (isArrowLegacy) ArrowType.END else ArrowType.NONE
                },
                isCurved = obj.optBoolean("isCurved", false),
                isZigzag = obj.optBoolean("isZigzag", false),
                isZone = obj.optBoolean("isZone", false)
            )
        }
    }
}

// ── Play Template Enum ────────────────────────────────────────────────────────

enum class PlayTemplate(val displayName: String, val description: String) {
    PICK_AND_ROLL("Pick & Roll", "2 jugadores: bloqueo y rol al aro"),
    FASTBREAK("Contraataque", "5 jugadores en transicion ofensiva"),
    ZONE_DEFENSE_2_3("Defensa Zona 2-3", "5 defensores en zona 2-3"),
    MOTION_OFFENSE("Motion Offense", "5 jugadores con pases y cortes")
}
