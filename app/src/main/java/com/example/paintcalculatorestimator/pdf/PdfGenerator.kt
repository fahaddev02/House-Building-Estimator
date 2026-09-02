package com.example.paintcalculatorestimator.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.paintcalculatorestimator.domain.model.ProjectDetails
import com.example.paintcalculatorestimator.domain.model.UnitType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generatePdf(context: Context, projectDetails: ProjectDetails, currency: String): File {
        val pdfDocument = PdfDocument()
        
        // A4 standard dimensions at 72 DPI (595 x 842 points)
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Paint definitions
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.rgb(10, 80, 120) // Premium Deep Teal/Blue
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val sectionHeaderPaint = Paint().apply {
            color = Color.rgb(10, 80, 120)
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val valuePaint = Paint().apply {
            color = Color.rgb(50, 50, 50)
            textSize = 12f
            isAntiAlias = true
        }

        val totalPaint = Paint().apply {
            color = Color.rgb(220, 80, 20) // Highlight coral/orange for total
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val fillPaint = Paint().apply {
            color = Color.rgb(240, 245, 250) // Soft background tint
            style = Paint.Style.FILL
        }

        var yPos = 50f
        val xLeft = 40f
        val xRight = 555f
        val colWidth = 240f

        // 1. Header: App Name
        canvas.drawText("PAINT CALCULATOR & ESTIMATOR", xLeft, yPos, titlePaint)
        yPos += 20f

        // Sub-bar
        canvas.drawLine(xLeft, yPos, xRight, yPos, borderPaint)
        yPos += 20f

        // 2. Project Meta Info
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateString = sdf.format(Date(projectDetails.project.createdAt))

        canvas.drawText("Project: ${projectDetails.project.name}", xLeft, yPos, textPaint.apply { isFakeBoldText = true })
        canvas.drawText("Date: $dateString", xLeft + colWidth, yPos, textPaint.apply { isFakeBoldText = false })
        yPos += 18f
        
        if (projectDetails.project.location.isNotEmpty()) {
            canvas.drawText("Location: ${projectDetails.project.location}", xLeft, yPos, textPaint)
            yPos += 18f
        }
        
        if (projectDetails.project.notes.isNotEmpty()) {
            canvas.drawText("Notes: ${projectDetails.project.notes}", xLeft, yPos, textPaint)
            yPos += 25f
        } else {
            yPos += 10f
        }

        canvas.drawLine(xLeft, yPos, xRight, yPos, borderPaint)
        yPos += 25f

        // 3. Room details & Measurements
        canvas.drawText("ROOM DETAILS & MEASUREMENTS", xLeft, yPos, sectionHeaderPaint)
        yPos += 18f

        val firstRoomDetails = projectDetails.rooms.firstOrNull()
        if (firstRoomDetails != null) {
            val room = firstRoomDetails.room
            val unitStr = if (room.unitType == UnitType.METRIC) "m" else "ft"
            val areaUnitStr = if (room.unitType == UnitType.METRIC) "m²" else "ft²"

            canvas.drawText("Room Name: ${room.name}", xLeft, yPos, valuePaint)
            canvas.drawText("Walls Count: ${room.wallCount}", xLeft + colWidth, yPos, valuePaint)
            yPos += 16f

            canvas.drawText("Width: ${room.width} $unitStr", xLeft, yPos, valuePaint)
            canvas.drawText("Length: ${room.length} $unitStr", xLeft + colWidth, yPos, valuePaint)
            yPos += 16f

            canvas.drawText("Height: ${room.height} $unitStr", xLeft, yPos, valuePaint)
            canvas.drawText("Ceiling Included: ${if (room.includeCeiling) "Yes" else "No"}", xLeft + colWidth, yPos, valuePaint)
            yPos += 20f

            // Deductions
            if (firstRoomDetails.openings.isNotEmpty()) {
                canvas.drawText("Deductions (Doors & Windows):", xLeft, yPos, textPaint.apply { isFakeBoldText = true })
                yPos += 16f
                for (opening in firstRoomDetails.openings) {
                    canvas.drawText(
                        "- ${opening.type.name.lowercase().replaceFirstChar { it.uppercase() }}: " +
                                "${opening.width} x ${opening.height} $unitStr (Qty: ${opening.quantity})",
                        xLeft + 15, yPos, valuePaint
                    )
                    yPos += 16f
                }
                yPos += 8f
            }
        }
        
        canvas.drawLine(xLeft, yPos, xRight, yPos, borderPaint)
        yPos += 25f

        // 4. Paint settings
        canvas.drawText("PAINT SPECIFICATIONS", xLeft, yPos, sectionHeaderPaint)
        yPos += 18f

        val settings = projectDetails.paintSettings
        val volUnitStr = if (projectDetails.rooms.firstOrNull()?.room?.unitType == UnitType.METRIC) "Liters" else "Gallons"
        val covUnitStr = if (projectDetails.rooms.firstOrNull()?.room?.unitType == UnitType.METRIC) "m²/L" else "ft²/gal"

        canvas.drawText("Coverage: ${settings.coverage} $covUnitStr", xLeft, yPos, valuePaint)
        canvas.drawText("Coats: ${settings.coats}", xLeft + colWidth, yPos, valuePaint)
        yPos += 16f

        canvas.drawText("Wastage: ${(settings.wastage * 100).toInt()}%", xLeft, yPos, valuePaint)
        canvas.drawText("Primer Included: ${if (settings.includePrimer) "Yes (${settings.primerCoats} coat(s) @ ${settings.primerCoverage} $covUnitStr)" else "No"}", xLeft + colWidth, yPos, valuePaint)
        yPos += 25f

        canvas.drawLine(xLeft, yPos, xRight, yPos, borderPaint)
        yPos += 25f

        // 5. Cost Breakdown
        canvas.drawText("ESTIMATE BREAKDOWN", xLeft, yPos, sectionHeaderPaint)
        yPos += 18f

        val est = projectDetails.estimate
        val formattedPaintReq = String.format(Locale.US, "%.1f", est.paintRequired)
        val formattedPrimerReq = String.format(Locale.US, "%.1f", est.primerRequired)
        
        canvas.drawText("Total Surface Area: ${est.totalSurfaceArea} ${if (projectDetails.rooms.firstOrNull()?.room?.unitType == UnitType.METRIC) "m²" else "ft²"}", xLeft, yPos, valuePaint)
        canvas.drawText("Paintable Wall Area: ${est.paintableArea} ${if (projectDetails.rooms.firstOrNull()?.room?.unitType == UnitType.METRIC) "m²" else "ft²"}", xLeft + colWidth, yPos, valuePaint)
        yPos += 16f

        canvas.drawText("Paint Required: $formattedPaintReq $volUnitStr", xLeft, yPos, valuePaint)
        if (settings.includePrimer) {
            canvas.drawText("Primer Required: $formattedPrimerReq $volUnitStr", xLeft + colWidth, yPos, valuePaint)
            yPos += 16f
        } else {
            yPos += 16f
        }
        
        yPos += 10f

        // Drawing a table of costs
        val tableY = yPos
        canvas.drawRect(RectF(xLeft, tableY, xRight, tableY + 100f), fillPaint)
        canvas.drawRect(RectF(xLeft, tableY, xRight, tableY + 100f), borderPaint)

        var itemY = tableY + 20f
        canvas.drawText("Paint Cost:", xLeft + 15f, itemY, valuePaint)
        canvas.drawText("$currency ${String.format(Locale.US, "%,.2f", est.paintCost)}", xLeft + colWidth + 50f, itemY, valuePaint)
        itemY += 18f

        if (settings.includePrimer) {
            canvas.drawText("Primer Cost:", xLeft + 15f, itemY, valuePaint)
            canvas.drawText("$currency ${String.format(Locale.US, "%,.2f", est.primerCost)}", xLeft + colWidth + 50f, itemY, valuePaint)
            itemY += 18f
        }

        canvas.drawText("Labor Cost:", xLeft + 15f, itemY, valuePaint)
        canvas.drawText("$currency ${String.format(Locale.US, "%,.2f", est.laborCost)}", xLeft + colWidth + 50f, itemY, valuePaint)
        itemY += 18f

        if (est.materialsCost > 0) {
            canvas.drawText("Additional Materials Cost:", xLeft + 15f, itemY, valuePaint)
            canvas.drawText("$currency ${String.format(Locale.US, "%,.2f", est.materialsCost)}", xLeft + colWidth + 50f, itemY, valuePaint)
            itemY += 18f
        }

        yPos = itemY + 20f

        // Total highlighted cost box
        canvas.drawRect(RectF(xLeft, yPos, xRight, yPos + 40f), Paint().apply {
            color = Color.rgb(230, 240, 230) // Soft green highlight
            style = Paint.Style.FILL
        })
        canvas.drawRect(RectF(xLeft, yPos, xRight, yPos + 40f), borderPaint)
        
        canvas.drawText("TOTAL ESTIMATED COST:", xLeft + 15f, yPos + 25f, totalPaint.apply { color = Color.rgb(10, 100, 10) })
        canvas.drawText(
            "$currency ${String.format(Locale.US, "%,.2f", est.totalCost)}",
            xLeft + colWidth + 50f, yPos + 25f,
            totalPaint.apply { color = Color.rgb(10, 100, 10) }
        )
        yPos += 75f

        // Disclaimer at the bottom
        val disclaimerText = context.getString(com.example.paintcalculatorestimator.R.string.est_disclaimer)
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
            isAntiAlias = true
        }

        // Wrap disclaimer text into lines
        val words = disclaimerText.split(" ")
        var line = ""
        val maxLineWidth = xRight - xLeft - 20f
        var disclaimerY = yPos
        
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            val width = footerPaint.measureText(testLine)
            if (width > maxLineWidth) {
                canvas.drawText(line, xLeft, disclaimerY, footerPaint)
                disclaimerY += 12f
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, xLeft, disclaimerY, footerPaint)
        }

        pdfDocument.finishPage(page)

        // Save PDF to cache directory
        val cacheDir = context.cacheDir
        val pdfFile = File(cacheDir, "Paint_Estimate_${System.currentTimeMillis()}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        
        pdfDocument.close()
        return pdfFile
    }
}
