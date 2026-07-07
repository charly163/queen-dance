package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.Alumna
import com.example.data.model.Attendance
import com.example.ui.viewmodel.GeneralMonthlyStats
import com.example.ui.viewmodel.StudentStats
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PdfGenerator {

    fun generateIndividualReport(
        context: Context,
        student: Alumna,
        stats: StudentStats,
        history: List<Attendance>
    ): String {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
            color = Color.rgb(233, 30, 99) // Rose/Pink accent
        }

        val subtitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
            color = Color.DKGRAY
        }

        val textPaint = Paint().apply {
            textSize = 11f
            color = Color.BLACK
        }

        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 11f
            color = Color.WHITE
        }

        val rowBgPaint = Paint().apply {
            color = Color.rgb(245, 245, 245)
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(63, 81, 181) // Theme Blue
        }

        // Header Title
        canvas.drawText("CUERPOS EN MOVIMIENTO", 40f, 50f, Paint().apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.GRAY
            letterSpacing = 0.15f
        })
        canvas.drawText("Queen Dance - Reporte Individual", 40f, 75f, titlePaint)

        // Divider
        canvas.drawLine(40f, 90f, 555f, 90f, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })

        // Student Info Block
        var y = 120f
        canvas.drawText("INFORMACIÓN DE LA ALUMNA", 40f, y, subtitlePaint)
        y += 20f
        canvas.drawText("Nombre Completo: ${student.name} ${student.lastName}".trim(), 40f, y, textPaint)
        canvas.drawText("Profesor/a o Tutor: ${student.tutor}", 320f, y, textPaint)
        y += 18f
        canvas.drawText("Plan Contratado: ${student.plan} días por semana", 40f, y, textPaint)
        canvas.drawText("Días Programados: ${student.normalDays}", 320f, y, textPaint)

        // Stats Block
        y += 40f
        canvas.drawText("MÉTRICAS DE ASISTENCIA (MENSUAL)", 40f, y, subtitlePaint)
        y += 15f
        
        // Draw stats grid card background
        canvas.drawRoundRect(40f, y, 555f, y + 60f, 8f, 8f, Paint().apply { color = Color.rgb(240, 244, 248) })
        
        // Draw stats texts
        val statY = y + 35f
        canvas.drawText("Clases Planificadas: ${stats.scheduledClasses}", 60f, statY, textPaint)
        canvas.drawText("Presentes: ${stats.presentCount}", 220f, statY, textPaint)
        canvas.drawText("Ausentes: ${stats.absentCount}", 340f, statY, textPaint)
        canvas.drawText("Porcentaje: ${stats.attendancePercentage}%", 450f, statY, Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = Color.rgb(233, 30, 99)
        })

        // Table Header
        y += 100f
        canvas.drawText("HISTORIAL DE ASISTENCIAS", 40f, y, subtitlePaint)
        y += 15f

        canvas.drawRect(40f, y, 555f, y + 22f, headerBgPaint)
        canvas.drawText("Fecha", 50f, y + 15f, headerPaint)
        canvas.drawText("Concepto", 200f, y + 15f, headerPaint)
        canvas.drawText("Estado", 450f, y + 15f, headerPaint)
        y += 22f

        // Table Rows
        for ((index, att) in history.take(15).withIndex()) {
            if (index % 2 == 1) {
                canvas.drawRect(40f, y, 555f, y + 20f, rowBgPaint)
            }
            canvas.drawText(att.date, 50f, y + 14f, textPaint)
            canvas.drawText("Clase regular de danza", 200f, y + 14f, textPaint)
            
            val statusColor = if (att.status == "PRESENT") Color.rgb(76, 175, 80) else Color.rgb(244, 67, 54)
            canvas.drawText(
                if (att.status == "PRESENT") "Presente" else "Ausente",
                450f,
                y + 14f,
                Paint().apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textSize = 11f
                    color = statusColor
                }
            )
            y += 20f
        }

        // Footer
        canvas.drawText("Reporte oficial generado por la aplicación Queen Dance", 40f, 810f, Paint().apply {
            textSize = 9f
            color = Color.GRAY
        })

        pdfDocument.finishPage(page)

        val fileName = "Reporte_${student.name.replace(" ", "_")}_${student.lastName.replace(" ", "_")}_Asistencia.pdf"
        val savedPath = savePdfToDownloads(context, pdfDocument, fileName)
        pdfDocument.close()
        return savedPath
    }

    fun generateGeneralReport(
        context: Context,
        stats: GeneralMonthlyStats,
        allAlumnas: List<Alumna>,
        allAttendance: List<Attendance>
    ): String {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
            color = Color.rgb(233, 30, 99) // Rose/Pink accent
        }

        val subtitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
            color = Color.DKGRAY
        }

        val textPaint = Paint().apply {
            textSize = 11f
            color = Color.BLACK
        }

        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 11f
            color = Color.WHITE
        }

        val rowBgPaint = Paint().apply {
            color = Color.rgb(245, 245, 245)
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(63, 81, 181) // Theme Blue
        }

        // Header Title
        canvas.drawText("CUERPOS EN MOVIMIENTO", 40f, 50f, Paint().apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.GRAY
            letterSpacing = 0.15f
        })
        canvas.drawText("Queen Dance - Reporte Mensual General", 40f, 75f, titlePaint)

        // Divider
        canvas.drawLine(40f, 90f, 555f, 90f, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })

        // General Stats Block
        var y = 120f
        canvas.drawText("RESUMEN GLOBAL DE LA ACADEMIA", 40f, y, subtitlePaint)
        y += 15f

        canvas.drawRoundRect(40f, y, 555f, y + 60f, 8f, 8f, Paint().apply { color = Color.rgb(240, 244, 248) })

        val statY = y + 35f
        canvas.drawText("Total Alumnas Activas: ${stats.totalActiveStudents}", 60f, statY, textPaint)
        canvas.drawText("Total de Asistencias: ${stats.totalAsistencias}", 240f, statY, textPaint)
        canvas.drawText("Promedio de Asistencia: ${stats.averageAttendancePercentage}%", 400f, statY, Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = Color.rgb(233, 30, 99)
        })

        // Table Header
        y += 100f
        canvas.drawText("DESGLOSE POR ALUMNA", 40f, y, subtitlePaint)
        y += 15f

        canvas.drawRect(40f, y, 555f, y + 22f, headerBgPaint)
        canvas.drawText("Alumna", 50f, y + 15f, headerPaint)
        canvas.drawText("Profesor/a", 240f, y + 15f, headerPaint)
        canvas.drawText("Plan", 400f, y + 15f, headerPaint)
        canvas.drawText("Días", 470f, y + 15f, headerPaint)
        y += 22f

        // List of Students and their plans
        for ((index, student) in allAlumnas.withIndex()) {
            if (index % 2 == 1) {
                canvas.drawRect(40f, y, 555f, y + 20f, rowBgPaint)
            }
            canvas.drawText("${student.name} ${student.lastName}".trim(), 50f, y + 14f, textPaint)
            canvas.drawText(student.tutor, 240f, y + 14f, textPaint)
            canvas.drawText("Plan ${student.plan} días", 400f, y + 14f, textPaint)
            canvas.drawText(student.normalDays, 470f, y + 14f, Paint().apply {
                textSize = 9f
                color = Color.rgb(100, 100, 100)
            })
            y += 20f
        }

        // Footer
        canvas.drawText("Reporte oficial generado por la aplicación Queen Dance", 40f, 810f, Paint().apply {
            textSize = 9f
            color = Color.GRAY
        })

        pdfDocument.finishPage(page)

        val fileName = "Reporte_Mensual_General_Queen_Dance.pdf"
        val savedPath = savePdfToDownloads(context, pdfDocument, fileName)
        pdfDocument.close()
        return savedPath
    }

    private fun savePdfToDownloads(context: Context, pdfDocument: PdfDocument, fileName: String): String {
        var outputStream: OutputStream? = null
        var savedPath = ""

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    outputStream = resolver.openOutputStream(uri)
                    savedPath = "Descargas/$fileName"
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                outputStream = FileOutputStream(file)
                savedPath = file.absolutePath
            }

            if (outputStream != null) {
                pdfDocument.writeTo(outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
        }

        return if (savedPath.isNotEmpty()) savedPath else fileName
    }
}