package com.example.tfg_aaron.ui.screens.reportes

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_aaron.data.local.entities.ReporteEntity
import com.example.tfg_aaron.data.preferences.UserPreferences
import com.example.tfg_aaron.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class ReportesUiState(
    val reportes: List<ReporteEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val error: String? = null,
    val pdfPath: String? = null,
    val successMessage: String? = null
)

class ReportesViewModel(
    private val entrenadorId: Int,
    private val reporteRepository: ReporteRepository,
    private val jugadoraRepository: JugadoraRepository,
    private val sesionRepository: SesionRepository,
    private val scoutingRepository: ScoutingRepository,
    private val estadisticaRepository: EstadisticaRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportesUiState())
    val uiState: StateFlow<ReportesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            reporteRepository.getAll(entrenadorId).collect { list ->
                _uiState.update { it.copy(reportes = list) }
            }
        }
    }

    fun generarReporteJugadoras(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            try {
                val jugadoras = jugadoraRepository.getActivas(entrenadorId).first()
                val equipoNombre = userPreferences.equipoNombre.first()
                val sb = StringBuilder()
                sb.appendLine("=== REPORTE DE JUGADORAS ===")
                sb.appendLine("Equipo: $equipoNombre")
                sb.appendLine("Total: ${jugadoras.size} jugadoras activas")
                sb.appendLine()
                jugadoras.forEach { j ->
                    sb.appendLine("- #${j.numero} ${j.nombre} ${j.apellidos} | ${j.posicion} | ${j.rol}")
                    if (j.areasMejora.isNotEmpty()) sb.appendLine("  Mejoras: ${j.areasMejora}")
                }
                val content = sb.toString()
                val pdfPath = generatePdf(context, "Jugadoras", content, equipoNombre)
                reporteRepository.add(ReporteEntity(idEntrenador = entrenadorId, tipo = "JUGADORAS",
                    titulo = "Reporte Jugadoras - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}",
                    contenido = content))
                _uiState.update { it.copy(isGenerating = false, pdfPath = pdfPath, successMessage = "Reporte generado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, error = "Error generando reporte: ${e.message}") }
            }
        }
    }

    fun generarReporteEstadisticas(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            try {
                val jugadoras = jugadoraRepository.getActivas(entrenadorId).first()
                val equipoNombre = userPreferences.equipoNombre.first()
                val sb = StringBuilder()
                sb.appendLine("=== REPORTE DE ESTADÍSTICAS ===")
                sb.appendLine("Equipo: $equipoNombre")
                sb.appendLine()
                jugadoras.forEach { j ->
                    val promedio = estadisticaRepository.getPromedio(j.id)
                    val intentados = estadisticaRepository.getTotalIntentados(j.id)
                    val acertados = estadisticaRepository.getTotalAcertados(j.id)
                    sb.appendLine("${j.nombre} ${j.apellidos}: $acertados/$intentados = ${String.format("%.1f", promedio)}%")
                }
                val content = sb.toString()
                val pdfPath = generatePdf(context, "Estadísticas", content, equipoNombre)
                reporteRepository.add(ReporteEntity(idEntrenador = entrenadorId, tipo = "ESTADISTICAS",
                    titulo = "Reporte Estadísticas - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}",
                    contenido = content))
                _uiState.update { it.copy(isGenerating = false, pdfPath = pdfPath, successMessage = "Reporte generado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, error = e.message) }
            }
        }
    }

    fun generarReporteSesiones(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            try {
                val sesiones = sesionRepository.getAll(entrenadorId).first()
                val equipoNombre = userPreferences.equipoNombre.first()
                val sb = StringBuilder()
                sb.appendLine("=== REPORTE DE SESIONES ===")
                sb.appendLine("Equipo: $equipoNombre")
                sb.appendLine("Total: ${sesiones.size} sesiones")
                sb.appendLine("Completadas: ${sesiones.count { it.completada }}")
                sb.appendLine()
                sesiones.forEach { s ->
                    val estado = if (s.completada) "✓" else "○"
                    sb.appendLine("$estado Semana ${s.semana} - ${s.diaSemana}: ${s.titulo}")
                    if (s.objetivos.isNotEmpty()) sb.appendLine("  Objetivos: ${s.objetivos}")
                }
                val content = sb.toString()
                val pdfPath = generatePdf(context, "Sesiones", content, equipoNombre)
                reporteRepository.add(ReporteEntity(idEntrenador = entrenadorId, tipo = "SESIONES",
                    titulo = "Reporte Sesiones - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}",
                    contenido = content))
                _uiState.update { it.copy(isGenerating = false, pdfPath = pdfPath, successMessage = "Reporte generado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, error = e.message) }
            }
        }
    }

    fun generarReporteScouting(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            try {
                val scoutingList = scoutingRepository.getAll(entrenadorId).first()
                val equipoNombre = userPreferences.equipoNombre.first()
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val sb = StringBuilder()
                sb.appendLine("=== REPORTE DE SCOUTING ===")
                sb.appendLine("Equipo: $equipoNombre")
                sb.appendLine("Total: ${scoutingList.size} reportes")
                sb.appendLine()
                scoutingList.forEach { s ->
                    sb.appendLine("Tipo: ${s.tipo} | Cat: ${s.categoria} | Val: ${"★".repeat(s.valoracion)}")
                    if (s.rivalNombre.isNotEmpty()) sb.appendLine("Rival: ${s.rivalNombre}")
                    sb.appendLine("Obs: ${s.observaciones}")
                    sb.appendLine("Fecha: ${sdf.format(Date(s.fecha))}")
                    sb.appendLine("---")
                }
                val content = sb.toString()
                val pdfPath = generatePdf(context, "Scouting", content, equipoNombre)
                reporteRepository.add(ReporteEntity(idEntrenador = entrenadorId, tipo = "SCOUTING",
                    titulo = "Reporte Scouting - ${sdf.format(Date())}",
                    contenido = content))
                _uiState.update { it.copy(isGenerating = false, pdfPath = pdfPath, successMessage = "Reporte generado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, error = e.message) }
            }
        }
    }

    private fun generatePdf(context: Context, tipo: String, content: String, equipo: String): String {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply { textSize = 11f; color = android.graphics.Color.BLACK }
        val titlePaint = Paint().apply { textSize = 16f; color = android.graphics.Color.BLACK; isFakeBoldText = true }

        var y = 50f
        canvas.drawText("Coach Pro - $equipo", 40f, y, titlePaint)
        y += 30f
        canvas.drawText("Reporte: $tipo", 40f, y, paint)
        y += 20f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 20f

        content.lines().forEach { line ->
            if (y > 800f) return@forEach
            canvas.drawText(line.take(90), 40f, y, paint)
            y += 16f
        }

        pdfDoc.finishPage(page)

        val dir = context.getExternalFilesDir("reports") ?: context.filesDir
        dir.mkdirs()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val file = File(dir, "coach_${tipo.lowercase()}_${sdf.format(Date())}.pdf")
        FileOutputStream(file).use { pdfDoc.writeTo(it) }
        pdfDoc.close()
        return file.absolutePath
    }

    fun deleteReporte(reporte: ReporteEntity) {
        viewModelScope.launch { reporteRepository.delete(reporte) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null, pdfPath = null) }
    }
}
