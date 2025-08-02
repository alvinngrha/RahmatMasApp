package com.example.rahmatmas.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import com.example.rahmatmas.data.local.dao.TransactionEntity
import kotlinx.io.IOException
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfGenerator(private val context: Context) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    fun generateTransactionReport(
        transactions: List<TransactionEntity>,
        fileName: String = "laporan_transaksi_${System.currentTimeMillis()}.pdf"
    ): Result<String> {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply {
                textSize = 12f
                color = android.graphics.Color.BLACK
            }

            val titlePaint = Paint().apply {
                textSize = 18f
                color = android.graphics.Color.BLACK
                isFakeBoldText = true
            }

            val headerPaint = Paint().apply {
                textSize = 14f
                color = android.graphics.Color.BLACK
                isFakeBoldText = true
            }

            var yPosition = 50f
            val leftMargin = 50f
            val lineHeight = 20f

            // Title
            canvas.drawText("LAPORAN TRANSAKSI", leftMargin, yPosition, titlePaint)
            yPosition += 30f

            // Date generated
            canvas.drawText("Tanggal: ${dateFormat.format(Date())}", leftMargin, yPosition, paint)
            yPosition += 20f

            canvas.drawText("Total Transaksi: ${transactions.size}", leftMargin, yPosition, paint)
            yPosition += 30f

            // Headers
            canvas.drawText("ID", leftMargin, yPosition, headerPaint)
            canvas.drawText("Nama Barang", leftMargin + 80, yPosition, headerPaint)
            canvas.drawText("Jenis", leftMargin + 200, yPosition, headerPaint)
            canvas.drawText("Berat", leftMargin + 250, yPosition, headerPaint)
            canvas.drawText("Total", leftMargin + 320, yPosition, headerPaint)
            canvas.drawText("Tanggal", leftMargin + 420, yPosition, headerPaint)
            yPosition += 25f

            // Draw line
            canvas.drawLine(leftMargin, yPosition, 545f, yPosition, paint)
            yPosition += 15f

            var totalAmount = 0.0

            // Transaction data
            for (transaction in transactions) {
                if (yPosition > 750) { // Start new page if needed
                    pdfDocument.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                    val newPage = pdfDocument.startPage(newPageInfo)
                    canvas.drawText("", 0f, 0f, paint) // Reset canvas reference
                    yPosition = 50f
                }

                canvas.drawText(
                    transaction.id.take(8) + "...",
                    leftMargin,
                    yPosition,
                    paint
                )

                canvas.drawText(
                    transaction.namaBarang.take(15) + if (transaction.namaBarang.length > 15) "..." else "",
                    leftMargin + 80,
                    yPosition,
                    paint
                )

                canvas.drawText(
                    transaction.jenisTransaksi,
                    leftMargin + 200,
                    yPosition,
                    paint
                )

                canvas.drawText(
                    "${transaction.beratEmas}g",
                    leftMargin + 250,
                    yPosition,
                    paint
                )

                canvas.drawText(
                    formatCurrency(transaction.totalHarga),
                    leftMargin + 320,
                    yPosition,
                    paint
                )

                canvas.drawText(
                    dateFormat.format(transaction.createdAt),
                    leftMargin + 420,
                    yPosition,
                    paint
                )

                // Tambahkan gambar jika ada
                val bitmap = getTransactionBitmap(transaction)
                if (bitmap != null) {
                    val scaled = Bitmap.createScaledBitmap(bitmap, 60, 60, true)
                    canvas.drawBitmap(scaled, leftMargin + 500, yPosition - 15f, paint)
                }

                totalAmount += transaction.totalHarga
                yPosition += lineHeight
            }

            // Summary
            yPosition += 20f
            canvas.drawLine(leftMargin, yPosition, 545f, yPosition, paint)
            yPosition += 25f

            canvas.drawText("TOTAL KESELURUHAN: ${formatCurrency(totalAmount)}", leftMargin, yPosition, headerPaint)

            // Status sinkronisasi
            yPosition += 30f
            val unsyncedCount = transactions.count { !it.isSynced }
            if (unsyncedCount > 0) {
                canvas.drawText("* $unsyncedCount transaksi belum disinkronkan", leftMargin, yPosition, paint)
            } else {
                canvas.drawText("* Semua transaksi telah disinkronkan", leftMargin, yPosition, paint)
            }

            pdfDocument.finishPage(page)

            // Save file
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()

            Result.success(file.absolutePath)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    fun generateSingleTransactionReceipt(
        transaction: TransactionEntity,
        fileName: String = "struk_${transaction.id}_${System.currentTimeMillis()}.pdf"
    ): Result<String> {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(300, 500, 1).create() // Receipt size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply {
                textSize = 10f
                color = android.graphics.Color.BLACK
            }

            val titlePaint = Paint().apply {
                textSize = 14f
                color = android.graphics.Color.BLACK
                isFakeBoldText = true
            }

            var yPosition = 30f
            val leftMargin = 20f
            val lineHeight = 15f

            // Header
            canvas.drawText("TOKO EMAS RAHMAT BARU", leftMargin, yPosition, titlePaint)
            yPosition += 20f
            canvas.drawText("STRUK TRANSAKSI", leftMargin, yPosition, titlePaint)
            yPosition += 25f

            // Transaction details
            canvas.drawText("ID: ${transaction.id}", leftMargin, yPosition, paint)
            yPosition += lineHeight

            canvas.drawText("Tanggal: ${dateFormat.format(transaction.createdAt)}", leftMargin, yPosition, paint)
            yPosition += lineHeight

            canvas.drawText("Nama Barang: ${transaction.namaBarang}", leftMargin, yPosition, paint)
            yPosition += lineHeight

            canvas.drawText("Jumlah: ${transaction.jumlahBarang} pcs", leftMargin, yPosition, paint)
            yPosition += lineHeight

            canvas.drawText("Kadar: ${transaction.kadarEmas}", leftMargin, yPosition, paint)
            yPosition += lineHeight

            canvas.drawText("Jenis: ${transaction.jenisTransaksi}", leftMargin, yPosition, paint)
            yPosition += lineHeight

            canvas.drawText("Berat: ${transaction.beratEmas} gram", leftMargin, yPosition, paint)
            yPosition += lineHeight

            canvas.drawText("Harga Dasar: ${formatCurrency(transaction.hargaDasarPerGram)}/gram", leftMargin, yPosition, paint)
            yPosition += lineHeight

            canvas.drawText("Ongkos: ${formatCurrency(transaction.ongkos)}/gram", leftMargin, yPosition, paint)
            yPosition += lineHeight

            // Separator
            yPosition += 10f
            canvas.drawLine(leftMargin, yPosition, 280f, yPosition, paint)
            yPosition += 15f

            // Total
            canvas.drawText("TOTAL: ${formatCurrency(transaction.totalHarga)}", leftMargin, yPosition, titlePaint)
            yPosition += 25f

            // Sync status
            val syncStatus = if (transaction.isSynced) "Tersinkronkan" else "Belum Tersinkronkan"
            canvas.drawText("Status: $syncStatus", leftMargin, yPosition, paint)

            // Tambahkan gambar bukti transaksi jika ada
            val bitmap = getTransactionBitmap(transaction)
            if (bitmap != null) {
                val scaled = Bitmap.createScaledBitmap(bitmap, 80, 80, true)
                canvas.drawBitmap(scaled, leftMargin, yPosition, paint)
                yPosition += 90f
            }

            pdfDocument.finishPage(page)

            // Save file
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()

            Result.success(file.absolutePath)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    // Helper untuk ambil bitmap dari photoPath lokal saja
    private fun getTransactionBitmap(transaction: TransactionEntity): Bitmap? {
        transaction.photoPath?.let {
            val file = File(Uri.parse(it).path ?: return null)
            if (file.exists()) {
                return BitmapFactory.decodeFile(file.absolutePath)
            }
        }
        return null
    }

    private fun formatCurrency(amount: Double): String {
        return currencyFormat.format(amount).replace("Rp", "Rp ")
    }
}