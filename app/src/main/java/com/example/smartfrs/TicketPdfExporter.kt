package com.example.smartfrs

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object TicketPdfExporter {

    fun exportTicketToPdf(context: Context, ticket: BookedTicket): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard (595x842 pt)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Paint definitions
        val bgPaint = Paint().apply { color = Color.parseColor("#F4F6F9") }
        val headerPaint = Paint().apply { color = Color.parseColor("#0066FF") }
        val whiteTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subWhitePaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            textSize = 12f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = Color.parseColor("#777777")
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#111111")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val pnrValuePaint = Paint().apply {
            color = Color.parseColor("#0066FF")
            textSize = 16f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        // Draw Background
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        // Draw Ticket Card Frame
        val cardPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRoundRect(30f, 40f, 565f, 420f, 16f, 16f, cardPaint)

        // Header Blue Banner
        canvas.drawRoundRect(30f, 40f, 565f, 110f, 16f, 16f, headerPaint)
        canvas.drawRect(30f, 80f, 565f, 110f, headerPaint) // fill bottom corners

        canvas.drawText("AIR TICKET / BOARDING PASS", 50f, 75f, whiteTextPaint)
        canvas.drawText("${ticket.airlineName.uppercase()}  |  ${ticket.flightClass.uppercase()}", 50f, 98f, subWhitePaint)

        // Main Boarding Pass Content
        var top = 140f
        val col1 = 50f
        val col2 = 220f
        val col3 = 380f

        // Row 1: Passenger Name & Flight No
        canvas.drawText("NAME OF PASSENGER", col1, top, labelPaint)
        canvas.drawText(ticket.passengerName.uppercase(), col1, top + 18f, valuePaint)

        canvas.drawText("FLIGHT", col2, top, labelPaint)
        canvas.drawText(ticket.flightNumber, col2, top + 18f, valuePaint)

        canvas.drawText("DATE", col3, top, labelPaint)
        canvas.drawText(ticket.travelDate, col3, top + 18f, valuePaint)

        // Row 2: Route & Gate/Seat
        top += 55f
        canvas.drawText("FROM", col1, top, labelPaint)
        canvas.drawText("${ticket.source.uppercase()} (${ticket.fromCode})", col1, top + 18f, valuePaint)

        canvas.drawText("TO", col2, top, labelPaint)
        canvas.drawText("${ticket.destination.uppercase()} (${ticket.toCode})", col2, top + 18f, valuePaint)

        canvas.drawText("BOARDING TIME", col3, top, labelPaint)
        canvas.drawText(ticket.boardingTime, col3, top + 18f, valuePaint)

        // Row 3: Gate, Seat & ETKT PNR
        top += 55f
        canvas.drawText("GATE", col1, top, labelPaint)
        canvas.drawText(ticket.gate, col1, top + 18f, valuePaint)

        canvas.drawText("SEAT", col2, top, labelPaint)
        canvas.drawText(ticket.seatNumber, col2, top + 18f, valuePaint)

        canvas.drawText("TICKET ID / PNR", col3, top, labelPaint)
        canvas.drawText("#${ticket.ticketId}", col3, top + 18f, pnrValuePaint)

        // Row 4: Status & Price
        top += 55f
        canvas.drawText("STATUS", col1, top, labelPaint)
        val statusPaint = Paint(valuePaint).apply { color = Color.parseColor("#2E7D32") }
        canvas.drawText(ticket.status, col1, top + 18f, statusPaint)

        canvas.drawText("TOTAL AMOUNT", col2, top, labelPaint)
        canvas.drawText("৳ ${String.format("%.2f", ticket.price)}", col2, top + 18f, valuePaint)

        // Perforated Dashed Line
        top += 40f
        val linePaint = Paint().apply {
            color = Color.parseColor("#CCCCCC")
            strokeWidth = 2f
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(8f, 6f), 0f)
        }
        canvas.drawLine(50f, top, 545f, top, linePaint)

        // QR Code & Barcode at bottom of card
        top += 15f
        val qrBitmap = QRCodeGenerator.generateQRCode(ticket.ticketId, 100, 100)
        canvas.drawBitmap(qrBitmap, 50f, top, null)

        val barcodeBitmap = QRCodeGenerator.generateBarcode(ticket.ticketId, 360, 60)
        canvas.drawBitmap(barcodeBitmap, 170f, top + 20f, null)

        val footerTextPaint = Paint().apply {
            color = Color.parseColor("#888888")
            textSize = 9f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText("GATE CLOSES 30 MINUTES BEFORE DEPARTURE  *  PLEASE PRESENT BOARDING PASS AT GATE", 170f, top + 95f, footerTextPaint)

        pdfDocument.finishPage(page)

        // Save PDF to Storage
        val fileName = "BoardingPass_${ticket.ticketId}.pdf"
        var outputFile: File? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SmartFRS")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        pdfDocument.writeTo(stream)
                    }
                }
            }

            // Also write to app's external files directory so File/Share Provider can easily access it
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "SmartFRS")
            if (!dir.exists()) dir.mkdirs()
            outputFile = File(dir, fileName)
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        return outputFile
    }
}
