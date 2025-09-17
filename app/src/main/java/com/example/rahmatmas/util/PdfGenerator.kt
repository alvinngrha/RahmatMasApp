package com.example.rahmatmas.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
import java.security.MessageDigest
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
        // Common paints
        val basePaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val mutedPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            isAntiAlias = true
        }

        val accentPaint = Paint().apply {
            color = Color.rgb(212, 175, 55) // gold-like accent
            strokeWidth = 2.5f
            isAntiAlias = true
        }

        // Draw watermark first (behind content)
        drawWatermark(canvas, "Toko Emas Rahmat Baru")

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID"))
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        var y = MARGIN + 28f

        // Header
        canvas.drawText("NOTA TRANSAKSI", MARGIN.toFloat(), y, titlePaint)
        y += 26f
        canvas.drawText("Toko Emas Rahmat Mas", MARGIN.toFloat(), y, headerPaint)
        y += 18f
        canvas.drawText("Jl. Contoh Alamat No. 123 | Telp: 0812-3456-7890", MARGIN.toFloat(), y, mutedPaint)
        y += 10f
        canvas.drawLine(MARGIN.toFloat(), y, (PAGE_WIDTH - MARGIN).toFloat(), y, accentPaint)
        y += 18f

        // Transaction meta
        canvas.drawText("ID Transaksi:", MARGIN.toFloat(), y, headerPaint)
        canvas.drawText(transaction.id, (MARGIN + 110).toFloat(), y, basePaint)
        y += 18f
        canvas.drawText("Tanggal:", MARGIN.toFloat(), y, headerPaint)
        canvas.drawText(dateFormat.format(transaction.createdAt), (MARGIN + 110).toFloat(), y, basePaint)
        y += 18f
        canvas.drawText("Jenis:", MARGIN.toFloat(), y, headerPaint)
        canvas.drawText(transaction.jenisTransaksi, (MARGIN + 110).toFloat(), y, basePaint)
        y += 24f

        // Item section title
        canvas.drawText("Detail Barang", MARGIN.toFloat(), y, headerPaint)
        y += 12f

        // Card background for item section
        val cardTop = y
        val cardLeft = MARGIN.toFloat()
        val cardRight = (PAGE_WIDTH - MARGIN).toFloat()
        val photoSize = 120f
        val cardBottom = cardTop + photoSize + 60f

        // Optional light border
        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        canvas.drawRect(cardLeft, cardTop, cardRight, cardBottom, borderPaint)

        // Photo
        val photoRect = RectF(cardLeft + 12f, cardTop + 12f, cardLeft + 12f + photoSize, cardTop + 12f + photoSize)
        drawProductPhoto(canvas, transaction.photoPath, photoRect)

        // Item details (right of photo)
        val textX = photoRect.right + 14f
        var textY = cardTop + 22f

        drawLabelValue(canvas, "Nama Barang", transaction.namaBarang, textX, textY, headerPaint, basePaint)
        textY += 20f
        drawLabelValue(canvas, "Kadar Emas", transaction.kadarEmas, textX, textY, headerPaint, basePaint)
        textY += 20f
        drawLabelValue(canvas, "Berat Emas", "${transaction.beratEmas} gram", textX, textY, headerPaint, basePaint)
        textY += 20f
        drawLabelValue(canvas, "Jumlah", "${transaction.jumlahBarang} pcs", textX, textY, headerPaint, basePaint)
        textY += 20f
        drawLabelValue(canvas, "Harga Dasar", "${currencyFormat.format(transaction.hargaDasarPerGram)}/gram", textX, textY, headerPaint, basePaint)
        textY += 20f
        drawLabelValue(canvas, "Ongkos", "${currencyFormat.format(transaction.ongkos)}/gram", textX, textY, headerPaint, basePaint)

        // Totals box
        val totalsTop = photoRect.bottom + 18f
        val totalsLeft = cardLeft + 12f
        val totalsRight = cardRight - 12f
        val lineY = totalsTop + 44f

        // Subtotal, Ongkos, Grand total calculations
        val subtotal = transaction.hargaDasarPerGram * transaction.beratEmas * transaction.jumlahBarang
        val ongkosTotal = transaction.ongkos * transaction.beratEmas * transaction.jumlahBarang
        val grandTotal = subtotal + ongkosTotal

        canvas.drawLine(totalsLeft, totalsTop, totalsRight, totalsTop, borderPaint)
        canvas.drawText("Subtotal", totalsLeft, totalsTop + 16f, headerPaint)
        canvas.drawText(currencyFormat.format(subtotal).replace("Rp", "Rp "), totalsRight - 180f, totalsTop + 16f, headerPaint)
        canvas.drawText("Ongkos", totalsLeft, totalsTop + 34f, basePaint)
        canvas.drawText(currencyFormat.format(ongkosTotal).replace("Rp", "Rp "), totalsRight - 180f, totalsTop + 34f, basePaint)
        canvas.drawLine(totalsLeft, lineY, totalsRight, lineY, borderPaint)

        val totalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText("Total Harga", totalsLeft, lineY + 22f, totalPaint)
        canvas.drawText(currencyFormat.format(grandTotal).replace("Rp", "Rp "), totalsRight - 200f, lineY + 22f, totalPaint)

        // Footer note
        val footerY = cardBottom + 80f
        canvas.drawText("Terima kasih atas kepercayaan Anda", MARGIN.toFloat(), footerY, basePaint)
        canvas.drawText("Barang yang sudah dibeli tidak dapat dikembalikan", MARGIN.toFloat(), footerY + 18f, mutedPaint)
    }


    //Draw multiple transactions report content
    private fun drawMultipleTransactionsReport(
        canvas: Canvas,
        transactions: List<TransactionEntity>,
        title: String,
        pageNumber: Int,
        totalPages: Int
    ) {
        // Watermark on report pages as well
        drawWatermark(canvas, "Toko Emas Rahmat Baru")

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

    private fun drawLabelValue(
        canvas: Canvas,
        label: String,
        value: String,
        x: Float,
        y: Float,
        labelPaint: Paint,
        valuePaint: Paint
    ) {
        canvas.drawText(label + ":", x, y, labelPaint)
        canvas.drawText(value, x + 120f, y, valuePaint)
    }

    private fun drawWatermark(canvas: Canvas, text: String) {
        val paint = Paint().apply {
            color = Color.LTGRAY
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            alpha = 40 // subtle watermark
            isAntiAlias = true
        }
        val centerX = PAGE_WIDTH / 2f
        val centerY = PAGE_HEIGHT / 2f
        val textWidth = paint.measureText(text)
        val fm = paint.fontMetrics
        val textHeight = fm.bottom - fm.top

        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.rotate(-30f)
        canvas.drawText(text, -textWidth / 2, textHeight / 4, paint)
        canvas.restore()
    }

    private fun drawProductPhoto(canvas: Canvas, path: String?, destRect: RectF) {
        val paint = Paint().apply { isAntiAlias = true }
        val bmp = loadBitmap(path)
        if (bmp != null) {
            canvas.drawBitmap(bmp, null, destRect, paint)
            bmp.recycle()
        } else {
            // Placeholder border and text
            val border = Paint().apply {
                color = Color.LTGRAY
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                isAntiAlias = true
            }
            canvas.drawRect(destRect, border)
            val tp = Paint().apply {
                color = Color.GRAY
                textSize = 11f
                isAntiAlias = true
            }
            val placeholder = "Foto Barang"
            val w = tp.measureText(placeholder)
            canvas.drawText(placeholder, destRect.centerX() - w / 2, destRect.centerY(), tp)
        }
    }

    private fun loadBitmap(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(path)
            when (uri.scheme) {
                "content" -> context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
                "file" -> BitmapFactory.decodeFile(uri.path)
                else -> {
                    if (path.startsWith("http://") || path.startsWith("https://")) {
                        // Try cache first
                        val cacheDir = File(context.filesDir, "pdf_image_cache").apply { if (!exists()) mkdirs() }
                        val cacheFile = File(cacheDir, md5(path) + ".bin")
                        if (cacheFile.exists() && cacheFile.length() > 0) {
                            BitmapFactory.decodeFile(cacheFile.absolutePath)
                        } else {
                            try {
                                val url = java.net.URL(path)
                                val conn = url.openConnection().apply {
                                    connectTimeout = 5000
                                    readTimeout = 5000
                                }
                                conn.getInputStream().use { input ->
                                    val bytes = input.readBytes()
                                    // Save to cache for future offline use
                                    FileOutputStream(cacheFile).use { it.write(bytes) }
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                }
                            } catch (_: Exception) {
                                null
                            }
                        }
                    } else {
                        // Treat as raw file path if no scheme
                        BitmapFactory.decodeFile(path)
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
