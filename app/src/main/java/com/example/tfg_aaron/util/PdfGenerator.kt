package com.example.tfg_aaron.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.tfg_aaron.ui.screens.estadisticaspartido.JugadoraConStats
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    // ── Page dimensions: A4 landscape (842 x 595 points) ─────────────────────
    private const val PAGE_WIDTH = 842
    private const val PAGE_HEIGHT = 595
    private const val MARGIN = 36f

    // ── Brand colours (as ARGB ints) ─────────────────────────────────────────
    private val COLOR_NAVY_DARK   = Color.parseColor("#0A0E1A")
    private val COLOR_NAVY_CARD   = Color.parseColor("#131929")
    private val COLOR_NAVY_BORDER = Color.parseColor("#1E2D45")
    private val COLOR_ORANGE      = Color.parseColor("#FF6B2C")
    private val COLOR_NEON_GREEN  = Color.parseColor("#00E676")
    private val COLOR_TEAL        = Color.parseColor("#00BCD4")
    private val COLOR_ELECTRIC    = Color.parseColor("#448AFF")
    private val COLOR_GOLD        = Color.parseColor("#FFD600")
    private val COLOR_RED         = Color.parseColor("#F44336")
    private val COLOR_TEXT_PRI    = Color.parseColor("#F0F4FF")
    private val COLOR_TEXT_SEC    = Color.parseColor("#8899BB")
    private val COLOR_TEXT_TER    = Color.parseColor("#4A5A7A")
    private val COLOR_WHITE       = Color.WHITE

    // ── Column layout ─────────────────────────────────────────────────────────
    // Headers: JUGADORA | #  | PTS | REB | AST | MIN | FAL
    private val COL_X = floatArrayOf(
        MARGIN,          // JUGADORA  (wide)
        MARGIN + 260f,   // #
        MARGIN + 310f,   // PTS
        MARGIN + 380f,   // REB
        MARGIN + 450f,   // AST
        MARGIN + 520f,   // MIN
        MARGIN + 590f    // FAL
    )
    private val COL_HEADERS = arrayOf("JUGADORA", "#", "PTS", "REB", "AST", "MIN", "FAL")
    private val COL_ACCENTS = intArrayOf(
        COLOR_TEXT_SEC,
        COLOR_TEXT_SEC,
        COLOR_NEON_GREEN,
        COLOR_TEAL,
        COLOR_ELECTRIC,
        COLOR_GOLD,
        COLOR_RED
    )

    // ─────────────────────────────────────────────────────────────────────────

    fun generarEstadisticasPartido(
        context: Context,
        rival: String,
        fecha: String,
        jugadoras: List<JugadoraConStats>
    ): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var y = drawHeader(canvas, rival, fecha)
            y = drawTableHeader(canvas, y)
            y = drawRows(canvas, jugadoras, y)
            if (jugadoras.any { it.stat != null }) {
                drawTotalsRow(canvas, jugadoras, y)
            }
            drawFooter(canvas)

            document.finishPage(page)

            val timestamp = System.currentTimeMillis()
            val file = File(context.cacheDir, "stats_partido_$timestamp.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun compartirPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exportar PDF de estadísticas"))
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────

    /** Returns the Y coordinate after the header. */
    private fun drawHeader(canvas: Canvas, rival: String, fecha: String): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Dark background stripe across the top
        paint.color = COLOR_NAVY_CARD
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 80f, paint)

        // Accent bar on the left
        paint.color = COLOR_ORANGE
        canvas.drawRect(0f, 0f, 6f, 80f, paint)

        // Title
        paint.color = COLOR_NEON_GREEN
        paint.textSize = 8f
        paint.isFakeBoldText = true
        paint.letterSpacing = 0.15f
        canvas.drawText("PLAYVISION AV — ESTADÍSTICAS DEL PARTIDO", MARGIN, 24f, paint)

        // Rival + date subtitle
        paint.color = COLOR_TEXT_PRI
        paint.textSize = 20f
        paint.letterSpacing = 0f
        canvas.drawText("vs ${rival.uppercase()}", MARGIN, 52f, paint)

        paint.color = COLOR_TEXT_TER
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText(fecha.uppercase(), MARGIN, 68f, paint)

        // Score badge (right side)
        paint.color = COLOR_ORANGE
        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText("INFORME ESTADÍSTICO", PAGE_WIDTH - MARGIN - 130f, 30f, paint)

        // Thin separator line below header
        paint.color = COLOR_NAVY_BORDER
        paint.strokeWidth = 1f
        canvas.drawLine(0f, 80f, PAGE_WIDTH.toFloat(), 80f, paint)

        return 96f
    }

    /** Draws the column header row; returns Y after headers. */
    private fun drawTableHeader(canvas: Canvas, startY: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header background band
        paint.color = Color.parseColor("#0D1625")
        canvas.drawRect(MARGIN - 8f, startY - 14f, PAGE_WIDTH - MARGIN + 8f, startY + 10f, paint)

        for (i in COL_HEADERS.indices) {
            paint.color = COL_ACCENTS[i]
            paint.textSize = 8f
            paint.isFakeBoldText = true
            paint.letterSpacing = 0.12f
            canvas.drawText(COL_HEADERS[i], COL_X[i], startY, paint)
        }

        // Separator
        paint.color = COLOR_NAVY_BORDER
        paint.strokeWidth = 1f
        paint.isFakeBoldText = false
        paint.letterSpacing = 0f
        canvas.drawLine(MARGIN - 8f, startY + 12f, PAGE_WIDTH - MARGIN + 8f, startY + 12f, paint)

        return startY + 24f
    }

    /** Draws all player rows; returns Y after last row. */
    private fun drawRows(canvas: Canvas, jugadoras: List<JugadoraConStats>, startY: Float): Float {
        var y = startY
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rowHeight = 30f

        jugadoras.forEachIndexed { index, jcs ->
            val stat = jcs.stat

            // Alternate row background
            if (index % 2 == 0) {
                paint.color = Color.parseColor("#0C1220")
                canvas.drawRect(MARGIN - 8f, y - 16f, PAGE_WIDTH - MARGIN + 8f, y + rowHeight - 16f, paint)
            }

            // Jugadora name + posicion
            paint.color = COLOR_TEXT_PRI
            paint.textSize = 11f
            paint.isFakeBoldText = true
            paint.letterSpacing = 0f
            val fullName = "${jcs.jugadora.nombre} ${jcs.jugadora.apellidos}"
            canvas.drawText(fullName.take(28), COL_X[0], y, paint)

            paint.color = COLOR_TEXT_TER
            paint.textSize = 8f
            paint.isFakeBoldText = false
            canvas.drawText(jcs.jugadora.posicion, COL_X[0], y + 10f, paint)

            // Number
            paint.color = COLOR_ORANGE
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText("#${jcs.jugadora.numero}", COL_X[1], y, paint)

            if (stat != null) {
                // PTS
                paint.color = COLOR_NEON_GREEN
                paint.textSize = 12f
                canvas.drawText("${stat.puntos}", COL_X[2], y, paint)
                // REB
                paint.color = COLOR_TEAL
                canvas.drawText("${stat.rebotes}", COL_X[3], y, paint)
                // AST
                paint.color = COLOR_ELECTRIC
                canvas.drawText("${stat.asistencias}", COL_X[4], y, paint)
                // MIN
                paint.color = COLOR_GOLD
                canvas.drawText("${stat.minutos}", COL_X[5], y, paint)
                // FAL
                paint.color = COLOR_RED
                canvas.drawText("${stat.faltas}", COL_X[6], y, paint)
            } else {
                paint.color = COLOR_TEXT_TER
                paint.textSize = 9f
                paint.isFakeBoldText = false
                canvas.drawText("—", COL_X[2], y, paint)
                canvas.drawText("—", COL_X[3], y, paint)
                canvas.drawText("—", COL_X[4], y, paint)
                canvas.drawText("—", COL_X[5], y, paint)
                canvas.drawText("—", COL_X[6], y, paint)
            }

            // Row separator (light)
            paint.color = COLOR_NAVY_BORDER
            paint.strokeWidth = 0.5f
            paint.isFakeBoldText = false
            canvas.drawLine(
                MARGIN - 8f, y + rowHeight - 16f,
                PAGE_WIDTH - MARGIN + 8f, y + rowHeight - 16f,
                paint
            )

            y += rowHeight
        }

        return y
    }

    /** Draws the team totals row. */
    private fun drawTotalsRow(canvas: Canvas, jugadoras: List<JugadoraConStats>, startY: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val statsConDatos = jugadoras.mapNotNull { it.stat }

        val totalPts = statsConDatos.sumOf { it.puntos }
        val totalReb = statsConDatos.sumOf { it.rebotes }
        val totalAst = statsConDatos.sumOf { it.asistencias }
        val totalMin = statsConDatos.sumOf { it.minutos }
        val totalFal = statsConDatos.sumOf { it.faltas }

        val y = startY + 8f

        // Totals background
        paint.color = Color.parseColor("#112210")
        val rect = RectF(MARGIN - 8f, y - 16f, PAGE_WIDTH - MARGIN + 8f, y + 16f)
        canvas.drawRoundRect(rect, 4f, 4f, paint)

        // Border
        paint.color = COLOR_NEON_GREEN
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 4f, 4f, paint)
        paint.style = Paint.Style.FILL

        // "TOTALES" label
        paint.color = COLOR_TEXT_TER
        paint.textSize = 8f
        paint.isFakeBoldText = true
        paint.letterSpacing = 0.1f
        canvas.drawText("TOTALES", COL_X[0], y, paint)

        // Values
        paint.letterSpacing = 0f
        paint.textSize = 12f

        paint.color = COLOR_NEON_GREEN
        canvas.drawText("$totalPts", COL_X[2], y, paint)
        paint.color = COLOR_TEAL
        canvas.drawText("$totalReb", COL_X[3], y, paint)
        paint.color = COLOR_ELECTRIC
        canvas.drawText("$totalAst", COL_X[4], y, paint)
        paint.color = COLOR_GOLD
        canvas.drawText("$totalMin", COL_X[5], y, paint)
        paint.color = COLOR_RED
        canvas.drawText("$totalFal", COL_X[6], y, paint)
    }

    /** Draws the footer at the bottom of the page. */
    private fun drawFooter(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = COLOR_NAVY_CARD
        canvas.drawRect(0f, PAGE_HEIGHT - 28f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        paint.color = COLOR_ORANGE
        canvas.drawRect(0f, PAGE_HEIGHT - 28f, 4f, PAGE_HEIGHT.toFloat(), paint)

        paint.color = COLOR_TEXT_TER
        paint.textSize = 8f
        paint.isFakeBoldText = false
        paint.letterSpacing = 0.05f
        canvas.drawText(
            "Generado por PlayVision AV",
            MARGIN,
            PAGE_HEIGHT - 10f,
            paint
        )

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(
            "PlayVision AV © 2025",
            PAGE_WIDTH - MARGIN.toFloat(),
            PAGE_HEIGHT - 10f,
            paint
        )
    }
}
