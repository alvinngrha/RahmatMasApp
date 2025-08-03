package com.example.rahmatmas.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.rahmatmas.data.local.dao.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfGenerator(private val context: Context) {

    companion object {
        private const val TAG = "PdfGenerator"
        private const val PAGE_WIDTH = 595 // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 50
    }


    // generate PDF untuk single transaction dengan storage yang dapat diakses
    suspend fun generateSingleTransactionReceipt(transaction: TransactionEntity): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                // Generate content
                drawTransactionReceipt(canvas, transaction)

                pdfDocument.finishPage(page)

                // Save to accessible location
                val fileName = "Transaksi_${transaction.namaBarang}.pdf"
                val result = savePdfToAccessibleLocation(pdfDocument, fileName)

                pdfDocument.close()

                if (result.isSuccess) {
                    // Show notification to user about successful save
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "PDF tersimpan di folder Downloads",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                result
            } catch (e: Exception) {
                Log.e(TAG, "Error generating PDF", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Generate PDF untuk multiple transactions
     */
    suspend fun generateMultipleTransactionReport(
        transactions: List<TransactionEntity>,
        title: String = "Laporan Transaksi"
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val pdfDocument = PdfDocument()

                // Calculate pages needed
                val transactionsPerPage = 10
                val totalPages = kotlin.math.ceil(transactions.size.toDouble() / transactionsPerPage).toInt()

                for (pageNum in 0 until totalPages) {
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas

                    val startIndex = pageNum * transactionsPerPage
                    val endIndex = minOf(startIndex + transactionsPerPage, transactions.size)
                    val pageTransactions = transactions.subList(startIndex, endIndex)

                    drawMultipleTransactionsReport(canvas, pageTransactions, title, pageNum + 1, totalPages)

                    pdfDocument.finishPage(page)
                }

                // Save to accessible location
                val fileName = "Laporan_Transaksi_${System.currentTimeMillis()}.pdf"
                val result = savePdfToAccessibleLocation(pdfDocument, fileName)

                pdfDocument.close()

                if (result.isSuccess) {
                    withContext(Dispatchers.Main) {
                    }
                }
                result
            } catch (e: Exception) {
                Log.e(TAG, "Error generating multiple transaction PDF", e)
                Result.failure(e)
            }
        }
    }

    // Save PDF to accessible location (Downloads folder or MediaStore)
    private suspend fun savePdfToAccessibleLocation(
        pdfDocument: PdfDocument,
        fileName: String
    ): Result<String> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Use MediaStore
                savePdfToMediaStore(pdfDocument, fileName)
            } else {
                // Android 9 and below - Use External Storage
                savePdfToExternalStorage(pdfDocument, fileName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving PDF", e)
            Result.failure(e)
        }
    }

    //Save PDF using MediaStore (Android 10+)
    private suspend fun savePdfToMediaStore(
        pdfDocument: PdfDocument,
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let { pdfUri ->
                resolver.openOutputStream(pdfUri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }

                // Get actual file path for return
                val filePath = getFilePathFromUri(pdfUri) ?: "Downloads/$fileName"
                Result.success(filePath)
            } ?: Result.failure(Exception("Gagal membuat file PDF"))

        } catch (e: Exception) {
            Log.e(TAG, "Error saving to MediaStore", e)
            Result.failure(e)
        }
    }


    //Save PDF to External Storage (Android 9 and below)
    private suspend fun savePdfToExternalStorage(
        pdfDocument: PdfDocument,
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            // Make file visible in Downloads app
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = Uri.fromFile(file)
            context.sendBroadcast(intent)

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to external storage", e)
            Result.failure(e)
        }
    }


    // Get file path from URI (for MediaStore)
    private fun getFilePathFromUri(uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (displayNameIndex >= 0) {
                        val displayName = it.getString(displayNameIndex)
                        "Downloads/$displayName"
                    } else null
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file path from URI", e)
            null
        }
    }


    //Open PDF file after creation
    suspend fun openPdfFile(filePath: String) {
        withContext(Dispatchers.Main) {
            try {
                val file = File(filePath)
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                } else {
                    Uri.fromFile(file)
                }

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                val chooser = Intent.createChooser(intent, "Buka PDF dengan")
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(chooser)
                } else {
                    Toast.makeText(context, "Tidak ada aplikasi untuk membuka PDF", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error opening PDF", e)
                Toast.makeText(context, "Gagal membuka PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //Draw single transaction receipt content
    private fun drawTransactionReceipt(canvas: Canvas, transaction: TransactionEntity) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        var yPosition = MARGIN + 30f

        // Title
        canvas.drawText("STRUK TRANSAKSI", MARGIN.toFloat(), yPosition, titlePaint)
        yPosition += 40f

        // Store info
        canvas.drawText("Toko Emas Rahmat Mas", MARGIN.toFloat(), yPosition, headerPaint)
        yPosition += 25f
        canvas.drawText("Jl. Contoh Alamat No. 123", MARGIN.toFloat(), yPosition, paint)
        yPosition += 20f
        canvas.drawText("Telp: 0812-3456-7890", MARGIN.toFloat(), yPosition, paint)
        yPosition += 40f

        // Transaction details
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID"))
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        canvas.drawText("ID Transaksi: ${transaction.id}", MARGIN.toFloat(), yPosition, paint)
        yPosition += 25f
        canvas.drawText("Tanggal: ${dateFormat.format(transaction.createdAt)}", MARGIN.toFloat(), yPosition, paint)
        yPosition += 25f
        canvas.drawText("Jenis: ${transaction.jenisTransaksi}", MARGIN.toFloat(), yPosition, paint)
        yPosition += 40f

        // Item details
        canvas.drawText("DETAIL BARANG", MARGIN.toFloat(), yPosition, headerPaint)
        yPosition += 30f

        canvas.drawText("Nama Barang: ${transaction.namaBarang}", MARGIN.toFloat(), yPosition, paint)
        yPosition += 25f
        canvas.drawText("Jumlah: ${transaction.jumlahBarang} pcs", MARGIN.toFloat(), yPosition, paint)
        yPosition += 25f
        canvas.drawText("Kadar Emas: ${transaction.kadarEmas}", MARGIN.toFloat(), yPosition, paint)
        yPosition += 25f
        canvas.drawText("Berat Emas: ${transaction.beratEmas} gram", MARGIN.toFloat(), yPosition, paint)
        yPosition += 25f
        canvas.drawText("Harga Dasar: ${currencyFormat.format(transaction.hargaDasarPerGram)}/gram", MARGIN.toFloat(), yPosition, paint)
        yPosition += 25f
        canvas.drawText("Ongkos: ${currencyFormat.format(transaction.ongkos)}/gram", MARGIN.toFloat(), yPosition, paint)
        yPosition += 40f

        // Total
        canvas.drawText("TOTAL HARGA", MARGIN.toFloat(), yPosition, headerPaint)
        yPosition += 30f
        val totalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText(currencyFormat.format(transaction.totalHarga).replace("Rp", "Rp "), MARGIN.toFloat(), yPosition, totalPaint)
        yPosition += 50f

        // Footer
        canvas.drawText("Terima kasih atas kepercayaan Anda", MARGIN.toFloat(), yPosition, paint)
        yPosition += 25f
        canvas.drawText("Barang yang sudah dibeli tidak dapat dikembalikan", MARGIN.toFloat(), yPosition, paint)
    }


    //Draw multiple transactions report content
    private fun drawMultipleTransactionsReport(
        canvas: Canvas,
        transactions: List<TransactionEntity>,
        title: String,
        pageNumber: Int,
        totalPages: Int
    ) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        var yPosition = MARGIN + 30f

        // Title
        canvas.drawText(title, MARGIN.toFloat(), yPosition, titlePaint)
        yPosition += 30f

        // Date
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
        canvas.drawText("Tanggal Cetak: ${dateFormat.format(Date())}", MARGIN.toFloat(), yPosition, paint)
        yPosition += 40f

        // Table header
        canvas.drawText("ID", MARGIN.toFloat(), yPosition, headerPaint)
        canvas.drawText("Barang", (MARGIN + 80).toFloat(), yPosition, headerPaint)
        canvas.drawText("Jenis", (MARGIN + 200).toFloat(), yPosition, headerPaint)
        canvas.drawText("Berat", (MARGIN + 270).toFloat(), yPosition, headerPaint)
        canvas.drawText("Total", (MARGIN + 350).toFloat(), yPosition, headerPaint)
        canvas.drawText("Tanggal", (MARGIN + 450).toFloat(), yPosition, headerPaint)
        yPosition += 25f

        // Draw line
        canvas.drawLine(MARGIN.toFloat(), yPosition, (PAGE_WIDTH - MARGIN).toFloat(), yPosition, paint)
        yPosition += 15f

        // Transaction rows
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val shortDateFormat = SimpleDateFormat("dd/MM/yy", Locale("id", "ID"))

        for (transaction in transactions) {
            canvas.drawText(transaction.id.take(8), MARGIN.toFloat(), yPosition, paint)
            canvas.drawText(transaction.namaBarang.take(15), (MARGIN + 80).toFloat(), yPosition, paint)
            canvas.drawText(transaction.jenisTransaksi, (MARGIN + 200).toFloat(), yPosition, paint)
            canvas.drawText("${transaction.beratEmas}g", (MARGIN + 270).toFloat(), yPosition, paint)
            canvas.drawText(currencyFormat.format(transaction.totalHarga).replace("Rp", ""), (MARGIN + 350).toFloat(), yPosition, paint)
            canvas.drawText(shortDateFormat.format(transaction.createdAt), (MARGIN + 450).toFloat(), yPosition, paint)
            yPosition += 20f
        }

        // Page number
        canvas.drawText("Halaman $pageNumber dari $totalPages", (PAGE_WIDTH - MARGIN - 100).toFloat(), (PAGE_HEIGHT - MARGIN).toFloat(), paint)
    }
}
