package com.example.rahmatmas.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
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
        private const val MARGIN = 40

        // Modern color palette
        private val PRIMARY_GOLD = Color.rgb(218, 165, 32) // Elegant gold
        private val SECONDARY_GOLD = Color.rgb(255, 215, 0) // Brighter gold
        private val DARK_TEXT = Color.rgb(33, 37, 41) // Almost black
        private val MEDIUM_TEXT = Color.rgb(73, 80, 87) // Medium gray
        private val LIGHT_TEXT = Color.rgb(108, 117, 125) // Light gray
        private val ACCENT_BLUE = Color.rgb(52, 144, 220) // Professional blue
        private val SUCCESS_GREEN = Color.rgb(40, 167, 69) // Success green
        private val BACKGROUND_LIGHT = Color.rgb(248, 249, 250) // Very light gray
    }

    // [Previous methods remain the same until drawTransactionReceipt...]

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
        title: String = "Nota Transaksi"
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
                val fileName = "Nota_Transaksi_${System.currentTimeMillis()}.pdf"
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

    //Enhanced single transaction receipt with modern design
    private fun drawTransactionReceipt(canvas: Canvas, transaction: TransactionEntity) {
        // Draw premium background
        drawPremiumBackground(canvas)

        // Enhanced paint configurations
        val titlePaint = createPaint(DARK_TEXT, 24f, Typeface.DEFAULT_BOLD)
        val headerPaint = createPaint(DARK_TEXT, 16f, Typeface.DEFAULT_BOLD)
        val subHeaderPaint = createPaint(PRIMARY_GOLD, 14f, Typeface.DEFAULT_BOLD)
        val bodyPaint = createPaint(DARK_TEXT, 12f)
        val mutedPaint = createPaint(LIGHT_TEXT, 11f)
        val accentPaint = createPaint(PRIMARY_GOLD, 12f)

        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        var y = MARGIN + 40f

        // Premium header with logo area
        drawPremiumHeader(canvas, y)
        y += 120f

        // Transaction ID badge
        drawTransactionBadge(canvas, transaction.id, y)
        y += 50f

        // Date and type info with icons
        drawInfoSection(canvas, transaction, dateFormat, y)
        y += 80f

        // Main product card with enhanced styling
        y = drawEnhancedProductCard(canvas, transaction, currencyFormat, y)

        // Payment summary with elegant styling
        drawPaymentSummary(canvas, transaction, currencyFormat, y + 30f)

        // Premium footer
        drawPremiumFooter(canvas)
    }

    private fun drawPremiumBackground(canvas: Canvas) {
        // Gradient background effect
        val backgroundPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, 200f,
                intArrayOf(Color.rgb(255, 255, 255), Color.rgb(250, 250, 252)),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 200f, backgroundPaint)

        // Decorative corner elements
        val decorPaint = Paint().apply {
            color = PRIMARY_GOLD
            alpha = 30
        }
        canvas.drawCircle(MARGIN.toFloat() - 20f, MARGIN.toFloat() - 20f, 40f, decorPaint)
        canvas.drawCircle((PAGE_WIDTH - MARGIN + 20).toFloat(), MARGIN.toFloat() - 20f, 40f, decorPaint)
    }

    private fun drawPremiumHeader(canvas: Canvas, startY: Float) {
        var y = startY

        // Main logo/brand area with background
        val headerBg = Paint().apply {
            color = PRIMARY_GOLD
            alpha = 15
        }
        val headerRect = RectF(MARGIN.toFloat(), y - 10f, (PAGE_WIDTH - MARGIN).toFloat(), y + 80f)
        canvas.drawRoundRect(headerRect, 12f, 12f, headerBg)

        // Store name with elegant styling
        val storePaint = createPaint(DARK_TEXT, 28f, Typeface.DEFAULT_BOLD)
        canvas.drawText("✦ TOKO EMAS RAHMAT BARU ✦", MARGIN + 40f, y + 35f, storePaint)

        // Store details
        val detailsPaint = createPaint(MEDIUM_TEXT, 12f)
        val mutedPaint = createPaint(LIGHT_TEXT, 11f)
        canvas.drawText("Jl. Ulin (Pasar Kedondong Baru), Samarinda | ☎ 0813-5036-6540", MARGIN + 40f, y + 55f, detailsPaint)
        canvas.drawText("Jual Beli Perhiasan Emas", MARGIN + 40f, y + 70f, mutedPaint)

        // Decorative line
        val linePaint = Paint().apply {
            shader = LinearGradient(
                MARGIN.toFloat(), 0f, (PAGE_WIDTH - MARGIN).toFloat(), 0f,
                intArrayOf(PRIMARY_GOLD, SECONDARY_GOLD, PRIMARY_GOLD),
                null,
                Shader.TileMode.CLAMP
            )
            strokeWidth = 3f
        }
        canvas.drawLine(MARGIN.toFloat(), y + 90f, (PAGE_WIDTH - MARGIN).toFloat(), y + 90f, linePaint)
    }

    private fun drawTransactionBadge(canvas: Canvas, transactionId: String, y: Float) {
        // Badge background
        val badgePaint = Paint().apply {
            color = ACCENT_BLUE
        }
        val badgeRect = RectF(MARGIN.toFloat(), y - 5f, MARGIN + 280f, y + 25f)
        canvas.drawRoundRect(badgeRect, 15f, 15f, badgePaint)

        // Badge text
        val badgeTextPaint = createPaint(Color.WHITE, 12f, Typeface.DEFAULT_BOLD)
        canvas.drawText("NOTA TRANSAKSI #${transactionId.take(8).uppercase()}", MARGIN + 15f, y + 12f, badgeTextPaint)

        // Status indicator
        val statusPaint = Paint().apply { color = SUCCESS_GREEN }
        canvas.drawCircle(MARGIN + 250f, y + 10f, 8f, statusPaint)
        val statusTextPaint = createPaint(Color.WHITE, 8f, Typeface.DEFAULT_BOLD)
        canvas.drawText("✓", MARGIN + 246f, y + 13f, statusTextPaint)
    }

    private fun drawInfoSection(canvas: Canvas, transaction: TransactionEntity, dateFormat: SimpleDateFormat, y: Float) {
        // Info cards
        drawInfoCard(canvas, "📅 Tanggal Transaksi", dateFormat.format(transaction.createdAt),
            MARGIN.toFloat(), y, 240f)
        drawInfoCard(canvas, "🔄 Jenis Transaksi", transaction.jenisTransaksi,
            MARGIN + 280f, y, 240f)
    }

    private fun drawInfoCard(canvas: Canvas, label: String, value: String, x: Float, y: Float, width: Float) {
        // Card background
        val cardPaint = Paint().apply {
            color = Color.WHITE
            setShadowLayer(4f, 2f, 2f, Color.LTGRAY)
        }
        val cardRect = RectF(x, y, x + width, y + 50f)
        canvas.drawRoundRect(cardRect, 8f, 8f, cardPaint)

        // Card border
        val borderPaint = Paint().apply {
            color = PRIMARY_GOLD
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(cardRect, 8f, 8f, borderPaint)

        // Content
        val labelPaint = createPaint(LIGHT_TEXT, 10f)
        val valuePaint = createPaint(DARK_TEXT, 12f, Typeface.DEFAULT_BOLD)
        canvas.drawText(label, x + 12f, y + 18f, labelPaint)
        canvas.drawText(value, x + 12f, y + 35f, valuePaint)
    }

    private fun drawEnhancedProductCard(canvas: Canvas, transaction: TransactionEntity,
                                        currencyFormat: NumberFormat, startY: Float): Float {
        val cardTop = startY
        val cardLeft = MARGIN.toFloat()
        val cardRight = (PAGE_WIDTH - MARGIN).toFloat()
        val photoSize = 140f
        val cardHeight = 180f

        // Main product card with shadow
        val cardPaint = Paint().apply {
            color = Color.WHITE
            setShadowLayer(8f, 0f, 4f, Color.argb(0, 0, 0, 40))
        }
        val cardRect = RectF(cardLeft, cardTop, cardRight, cardTop + cardHeight)
        canvas.drawRoundRect(cardRect, 12f, 12f, cardPaint)

        // Gold accent border
        val accentBorderPaint = Paint().apply {
            color = PRIMARY_GOLD
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(cardRect, 12f, 12f, accentBorderPaint)

        // Product photo with frame
        val photoRect = RectF(cardLeft + 20f, cardTop + 20f, cardLeft + 20f + photoSize, cardTop + 20f + photoSize)
        drawEnhancedProductPhoto(canvas, transaction.photoPath, photoRect)

        // Product details section
        val detailsX = photoRect.right + 25f
        var detailsY = cardTop + 35f

        // Product title
        val titlePaint = createPaint(DARK_TEXT, 16f, Typeface.DEFAULT_BOLD)
        canvas.drawText(transaction.namaBarang, detailsX, detailsY, titlePaint)
        detailsY += 25f

        // Product specs in elegant layout
        drawSpecItem(canvas, "Kadar", transaction.kadarEmas, "🥇", detailsX, detailsY)
        detailsY += 20f
        drawSpecItem(canvas, "Berat", "${transaction.beratEmas} gram", "⚖️", detailsX, detailsY)
        detailsY += 20f
        drawSpecItem(canvas, "Jumlah", "${transaction.jumlahBarang} pcs", "📦", detailsX, detailsY)
        detailsY += 20f
        drawSpecItem(canvas, "Ongkos", "${currencyFormat.format(transaction.ongkos)}/gram", "🔨", detailsX, detailsY)

        return cardTop + cardHeight
    }

    private fun drawSpecItem(canvas: Canvas, label: String, value: String, icon: String, x: Float, y: Float) {
        val iconPaint = createPaint(PRIMARY_GOLD, 12f)
        val labelPaint = createPaint(MEDIUM_TEXT, 11f)
        val valuePaint = createPaint(DARK_TEXT, 11f, Typeface.DEFAULT_BOLD)

        canvas.drawText(icon, x, y, iconPaint)
        canvas.drawText(label, x + 20f, y, labelPaint)
        canvas.drawText(value, x + 120f, y, valuePaint)
    }

    private fun drawEnhancedProductPhoto(canvas: Canvas, path: String?, destRect: RectF) {
        // Photo frame with gradient border
        val framePaint = Paint().apply {
            shader = LinearGradient(
                destRect.left, destRect.top, destRect.right, destRect.bottom,
                intArrayOf(PRIMARY_GOLD, SECONDARY_GOLD),
                null,
                Shader.TileMode.CLAMP
            )
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(destRect, 8f, 8f, framePaint)

        // Load and draw photo
        val bmp = loadBitmap(path)
        if (bmp != null) {
            val photoPaint = Paint().apply { isAntiAlias = true }
            val photoRect = RectF(destRect.left + 3f, destRect.top + 3f,
                destRect.right - 3f, destRect.bottom - 3f)
            canvas.drawRoundRect(photoRect, 5f, 5f, photoPaint)
            canvas.drawBitmap(bmp, null, photoRect, photoPaint)
            bmp.recycle()
        } else {
            // Enhanced placeholder
            val placeholderPaint = Paint().apply {
                color = BACKGROUND_LIGHT
            }
            canvas.drawRoundRect(destRect, 8f, 8f, placeholderPaint)

            val iconPaint = createPaint(PRIMARY_GOLD, 32f)
            canvas.drawText("📸", destRect.centerX() - 16f, destRect.centerY() - 5f, iconPaint)

            val textPaint = createPaint(LIGHT_TEXT, 10f)
            val text = "Foto Produk"
            val textWidth = textPaint.measureText(text)
            canvas.drawText(text, destRect.centerX() - textWidth/2, destRect.centerY() + 20f, textPaint)
        }
    }

    private fun drawPaymentSummary(canvas: Canvas, transaction: TransactionEntity,
                                   currencyFormat: NumberFormat, startY: Float) {
        val summaryLeft = MARGIN.toFloat()
        val summaryRight = (PAGE_WIDTH - MARGIN).toFloat()
        val summaryTop = startY
        val summaryHeight = 120f

        // Summary card background
        val summaryBg = Paint().apply {
            shader = LinearGradient(
                summaryLeft, summaryTop, summaryLeft, summaryTop + summaryHeight,
                intArrayOf(Color.rgb(248, 249, 250), Color.WHITE),
                null,
                Shader.TileMode.CLAMP
            )
            setShadowLayer(6f, 0f, 3f, Color.LTGRAY)
        }
        val summaryRect = RectF(summaryLeft, summaryTop, summaryRight, summaryTop + summaryHeight)
        canvas.drawRoundRect(summaryRect, 10f, 10f, summaryBg)

        // Summary border
        val summaryBorder = Paint().apply {
            color = PRIMARY_GOLD
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(summaryRect, 10f, 10f, summaryBorder)

        // Calculate totals (hanya total akhir tanpa breakdown)
        val grandTotal = transaction.totalHarga

        var summaryY = summaryTop + 25f

        // Summary header
        val summaryHeaderPaint = createPaint(DARK_TEXT, 14f, Typeface.DEFAULT_BOLD)
        canvas.drawText("💳 TOTAL PEMBAYARAN", summaryLeft + 20f, summaryY, summaryHeaderPaint)
        summaryY += 40f

        // Total langsung tanpa breakdown
        val totalPaint = createPaint(PRIMARY_GOLD, 18f, Typeface.DEFAULT_BOLD)
        val totalLabelPaint = createPaint(DARK_TEXT, 16f, Typeface.DEFAULT_BOLD)
        canvas.drawText("TOTAL HARGA:", summaryLeft + 20f, summaryY, totalLabelPaint)
        val totalValue = currencyFormat.format(grandTotal)
        val totalValueWidth = totalPaint.measureText(totalValue)
        canvas.drawText(totalValue, summaryRight - 20f - totalValueWidth, summaryY, totalPaint)
    }

    private fun drawSummaryLine(canvas: Canvas, label: String, value: String,
                                leftX: Float, rightX: Float, y: Float, isTotal: Boolean) {
        val labelPaint = if (isTotal) {
            createPaint(DARK_TEXT, 14f, Typeface.DEFAULT_BOLD)
        } else {
            createPaint(MEDIUM_TEXT, 12f)
        }

        val valuePaint = if (isTotal) {
            createPaint(PRIMARY_GOLD, 16f, Typeface.DEFAULT_BOLD)
        } else {
            createPaint(DARK_TEXT, 12f, Typeface.DEFAULT_BOLD)
        }

        canvas.drawText(label, leftX, y, labelPaint)
        val valueWidth = valuePaint.measureText(value)
        canvas.drawText(value, rightX - valueWidth, y, valuePaint)
    }

    private fun drawPremiumFooter(canvas: Canvas) {
        val footerY = PAGE_HEIGHT - 80f

        // Footer background
        val footerBg = Paint().apply {
            color = Color.rgb(248, 249, 250)
        }
        canvas.drawRect(0f, footerY - 10f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), footerBg)

        // Thank you message
        val thanksPaint = createPaint(DARK_TEXT, 13f, Typeface.DEFAULT_BOLD)
        canvas.drawText("🙏 Terima kasih atas kepercayaan Anda!", MARGIN.toFloat(), footerY + 15f, thanksPaint)

        // Terms
        val termsPaint = createPaint(LIGHT_TEXT, 10f)
        canvas.drawText("📋 JIka menjual kembali, harap sertakan nota ini sebagai bukti pembelian.",
            MARGIN.toFloat(), footerY + 32f, termsPaint)
        canvas.drawText("📞 Hubungi kami untuk pertanyaan: 0813-5036-6540",
            MARGIN.toFloat(), footerY + 46f, termsPaint)
    }

    private fun drawQRPlaceholder(canvas: Canvas, x: Float, y: Float) {
        val qrSize = 50f
        val qrBg = Paint().apply { color = Color.WHITE }
        val qrBorder = Paint().apply {
            color = PRIMARY_GOLD
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val qrRect = RectF(x, y, x + qrSize, y + qrSize)
        canvas.drawRect(qrRect, qrBg)
        canvas.drawRect(qrRect, qrBorder)

        val qrText = createPaint(LIGHT_TEXT, 8f)
        canvas.drawText("QR", x + 18f, y + 28f, qrText)
        canvas.drawText("Verify", x + 12f, y + 38f, qrText)
    }

    //Enhanced multiple transactions report with modern design
    private fun drawMultipleTransactionsReport(
        canvas: Canvas,
        transactions: List<TransactionEntity>,
        title: String,
        pageNumber: Int,
        totalPages: Int
    ) {
        // Draw premium background
        drawPremiumBackground(canvas)

        // Enhanced report header
        drawEnhancedReportHeader(canvas, title, pageNumber, totalPages)

        // Modern table with enhanced styling
        drawModernReportTable(canvas, transactions, 160f)

        // Premium footer for reports
        drawEnhancedReportFooter(canvas, pageNumber, totalPages)
    }

    private fun drawEnhancedReportHeader(canvas: Canvas, title: String, pageNumber: Int, totalPages: Int) {
        var y = MARGIN + 40f

        // Premium header with logo area (same as single transaction)
        val headerBg = Paint().apply {
            color = PRIMARY_GOLD
            alpha = 15
        }
        val headerRect = RectF(MARGIN.toFloat(), y - 10f, (PAGE_WIDTH - MARGIN).toFloat(), y + 80f)
        canvas.drawRoundRect(headerRect, 12f, 12f, headerBg)

        // Store name
        val storePaint = createPaint(DARK_TEXT, 28f, Typeface.DEFAULT_BOLD)
        canvas.drawText("✦ TOKO EMAS RAHMAT BARU ✦", MARGIN + 40f, y + 35f, storePaint)

        // Store details
        val detailsPaint = createPaint(MEDIUM_TEXT, 12f)
        val mutedPaint = createPaint(LIGHT_TEXT, 11f)
        canvas.drawText("Jl. Ulin (Pasar Kedondong Baru), Samarinda | ☎ 0813-5036-6540", MARGIN + 40f, y + 55f, detailsPaint)
        canvas.drawText("Jual Beli Perhiasan Emas", MARGIN + 40f, y + 70f, mutedPaint)

        // Decorative line
        val linePaint = Paint().apply {
            shader = LinearGradient(
                MARGIN.toFloat(), 0f, (PAGE_WIDTH - MARGIN).toFloat(), 0f,
                intArrayOf(PRIMARY_GOLD, SECONDARY_GOLD, PRIMARY_GOLD),
                null,
                Shader.TileMode.CLAMP
            )
            strokeWidth = 3f
        }
        canvas.drawLine(MARGIN.toFloat(), y + 90f, (PAGE_WIDTH - MARGIN).toFloat(), y + 90f, linePaint)

        y += 110f

        // Report title badge (similar to transaction badge)
        val badgePaint = Paint().apply {
            color = ACCENT_BLUE
        }
        val badgeRect = RectF(MARGIN.toFloat(), y, MARGIN + 350f, y + 30f)
        canvas.drawRoundRect(badgeRect, 15f, 15f, badgePaint)

        val badgeTextPaint = createPaint(Color.WHITE, 14f, Typeface.DEFAULT_BOLD)
        canvas.drawText("📊 $title", MARGIN + 20f, y + 20f, badgeTextPaint)

        // Page indicator
        val pageBadgePaint = Paint().apply {
            color = SUCCESS_GREEN
        }
        val pageBadgeRect = RectF(MARGIN + 370f, y, MARGIN + 480f, y + 30f)
        canvas.drawRoundRect(pageBadgeRect, 15f, 15f, pageBadgePaint)
        canvas.drawText("Hal $pageNumber/$totalPages", MARGIN + 385f, y + 20f, badgeTextPaint)

        y += 50f

        // Date info card
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
        drawInfoCard(canvas, "📅 Tanggal Cetak", dateFormat.format(Date()),
            MARGIN.toFloat(), y, 300f)
    }

    private fun drawModernReportTable(canvas: Canvas, transactions: List<TransactionEntity>, startY: Float) {
        var y = startY + 70f

        // Table container dengan layout yang lebih compact dan rapi
        val rowHeight = 45f
        val tableTop = y - 10f
        val tableBottom = y + (transactions.size * rowHeight) + 80f // Extra space untuk summary
        val tableBg = Paint().apply {
            color = Color.WHITE
            setShadowLayer(4f, 0f, 2f, Color.LTGRAY)
        }
        val tableRect = RectF(MARGIN.toFloat(), tableTop, (PAGE_WIDTH - MARGIN).toFloat(), tableBottom)
        canvas.drawRoundRect(tableRect, 10f, 10f, tableBg)

        // Table border
        val tableBorder = Paint().apply {
            color = PRIMARY_GOLD
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(tableRect, 10f, 10f, tableBorder)

        // Table header - lebih compact
        val headerBg = Paint().apply {
            shader = LinearGradient(
                MARGIN.toFloat(), y, (PAGE_WIDTH - MARGIN).toFloat(), y + 35f,
                intArrayOf(PRIMARY_GOLD, SECONDARY_GOLD),
                null,
                Shader.TileMode.CLAMP
            )
        }
        val headerRect = RectF(MARGIN + 12f, y, (PAGE_WIDTH - MARGIN - 12).toFloat(), y + 35f)
        canvas.drawRoundRect(headerRect, 6f, 6f, headerBg)

        // Header text yang lebih compact dan rapi
        val headerPaint = createPaint(Color.WHITE, 10f, Typeface.DEFAULT_BOLD)
        y += 22f

        // Layout header dengan kadar emas: Foto | ID | Nama | Kadar | Berat | Jumlah | Total | Tanggal
        canvas.drawText("📸", MARGIN + 25f, y, headerPaint)
        canvas.drawText("ID", MARGIN + 65f, y, headerPaint)
        canvas.drawText("Nama Barang", MARGIN + 105f, y, headerPaint)
        canvas.drawText("Kadar", MARGIN + 195f, y, headerPaint)
        canvas.drawText("Berat", MARGIN + 235f, y, headerPaint)
        canvas.drawText("Jumlah", MARGIN + 275f, y, headerPaint)
        canvas.drawText("Total", MARGIN + 315f, y, headerPaint)
        canvas.drawText("Tanggal", MARGIN + 400f, y, headerPaint)
        y += 20f

        // Table rows dengan styling yang clean
        val rowPaint = createPaint(DARK_TEXT, 9f)
        val altRowBg = Paint().apply {
            color = Color.rgb(252, 252, 252)
        }
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale("id", "ID"))

        transactions.forEachIndexed { index, transaction ->
            // Alternating row background
            if (index % 2 == 1) {
                val rowRect = RectF(MARGIN + 18f, y - 15f, (PAGE_WIDTH - MARGIN - 18).toFloat(), y + 20f)
                canvas.drawRoundRect(rowRect, 4f, 4f, altRowBg)
            }

            // Photo thumbnail - lebih kecil dan rapi
            val photoSize = 30f
            val photoRect = RectF(MARGIN + 25f, y - 12f, MARGIN + 25f + photoSize, y - 12f + photoSize)
            drawCompactProductThumbnail(canvas, transaction.photoPath, photoRect)

            // Data dengan spacing yang konsisten dengan kolom kadar emas
            canvas.drawText(transaction.id.take(6).uppercase(), MARGIN + 65f, y, rowPaint)

            // Nama barang dengan truncate yang lebih ketat karena ada kolom kadar
            val itemName = if (transaction.namaBarang.length > 12) {
                transaction.namaBarang.take(9) + "..."
            } else {
                transaction.namaBarang
            }
            canvas.drawText(itemName, MARGIN + 105f, y, rowPaint)

            // Kadar emas - kolom baru
            canvas.drawText(transaction.kadarEmas, MARGIN + 195f, y, rowPaint)

            canvas.drawText("${transaction.beratEmas}g", MARGIN + 235f, y, rowPaint)
            canvas.drawText("${transaction.jumlahBarang}x", MARGIN + 275f, y, rowPaint)

            // Format currency yang clean
            val totalFormatted = currencyFormat.format(transaction.totalHarga)
                .replace("Rp", "")
                .replace(",00", "")
                .replace(".", ",") // Indonesian number format
            canvas.drawText(totalFormatted, MARGIN + 315f, y, rowPaint)

            canvas.drawText(dateFormat.format(transaction.createdAt), MARGIN + 400f, y, rowPaint)
            y += rowHeight
        }

        // Summary section yang lebih rapi
        drawCompactTableSummary(canvas, transactions, y + 15f, currencyFormat)
    }

    // Method baru untuk thumbnail yang lebih compact
    private fun drawCompactProductThumbnail(canvas: Canvas, path: String?, destRect: RectF) {
        // Frame dengan border tipis
        val framePaint = Paint().apply {
            color = PRIMARY_GOLD
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(destRect, 3f, 3f, framePaint)

        // Load and draw thumbnail
        val bmp = loadBitmap(path)
        if (bmp != null) {
            val thumbPaint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
            }
            val thumbRect = RectF(destRect.left + 1f, destRect.top + 1f,
                destRect.right - 1f, destRect.bottom - 1f)
            canvas.drawBitmap(bmp, null, thumbRect, thumbPaint)
            bmp.recycle()
        } else {
            // Compact placeholder
            val placeholderBg = Paint().apply {
                color = Color.rgb(245, 245, 245)
            }
            val innerRect = RectF(destRect.left + 1f, destRect.top + 1f,
                destRect.right - 1f, destRect.bottom - 1f)
            canvas.drawRoundRect(innerRect, 2f, 2f, placeholderBg)

            // Small icon
            val iconPaint = createPaint(LIGHT_TEXT, 12f)
            canvas.drawText("📷", destRect.centerX() - 6f, destRect.centerY() + 4f, iconPaint)
        }
    }

    // Summary yang lebih compact tanpa rata-rata
    private fun drawCompactTableSummary(canvas: Canvas, transactions: List<TransactionEntity>, y: Float, currencyFormat: NumberFormat) {
        // Summary background yang lebih subtle
        val summaryBg = Paint().apply {
            color = Color.rgb(250, 250, 250)
        }
        val summaryRect = RectF(MARGIN + 18f, y, (PAGE_WIDTH - MARGIN - 18).toFloat(), y + 30f)
        canvas.drawRoundRect(summaryRect, 6f, 6f, summaryBg)

        // Summary border
        val summaryBorder = Paint().apply {
            color = LIGHT_TEXT
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
        }
        canvas.drawRoundRect(summaryRect, 6f, 6f, summaryBorder)

        // Summary text tanpa rata-rata - hanya total transaksi dan nilai
        val summaryPaint = createPaint(DARK_TEXT, 10f, Typeface.DEFAULT_BOLD)
        val totalTransactions = transactions.size
        val totalValue = transactions.sumOf { it.totalHarga }

        canvas.drawText("📊 Total: $totalTransactions transaksi", MARGIN + 30f, y + 20f, summaryPaint)

        val totalFormatted = currencyFormat.format(totalValue).replace("Rp", "Rp")
        canvas.drawText("💎 Total Nilai: $totalFormatted", MARGIN + 250f, y + 20f, summaryPaint)
    }

    private fun drawTableSummary(canvas: Canvas, transactions: List<TransactionEntity>, y: Float, currencyFormat: NumberFormat) {
        // Gunakan method yang sudah diupdate
        drawCompactTableSummary(canvas, transactions, y, currencyFormat)
    }

    private fun drawEnhancedReportFooter(canvas: Canvas, pageNumber: Int, totalPages: Int) {
        val footerY = PAGE_HEIGHT - 80f

        // Footer background (same as single transaction)
        val footerBg = Paint().apply {
            color = Color.rgb(248, 249, 250)
        }
        canvas.drawRect(0f, footerY - 10f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), footerBg)

        // Footer content
        val footerPaint = createPaint(DARK_TEXT, 11f)
        val mutedFooterPaint = createPaint(LIGHT_TEXT, 10f)

        canvas.drawText("🏪 Toko Emas Rahmat Baru - Nota Transaksi", MARGIN.toFloat(), footerY + 15f, footerPaint)
        canvas.drawText("📱 Hubungi kami: 0813-5036-6540 | 📧 Email: akhmadsujana2@gmail.com",
            MARGIN.toFloat(), footerY + 30f, mutedFooterPaint)
        canvas.drawText("⚠️ Jika menjual kembali, harap sertakan Nota ini sebagai bukti pembelian.",
            MARGIN.toFloat(), footerY + 44f, mutedFooterPaint)

        // Page indicator with modern styling
        val pageIndicatorBg = Paint().apply {
            color = PRIMARY_GOLD
        }
        val pageRect = RectF(PAGE_WIDTH - 120f, footerY, PAGE_WIDTH - 20f, footerY + 25f)
        canvas.drawRoundRect(pageRect, 12f, 12f, pageIndicatorBg)

        val pageTextPaint = createPaint(Color.WHITE, 10f, Typeface.DEFAULT_BOLD)
        canvas.drawText("$pageNumber / $totalPages", PAGE_WIDTH - 95f, footerY + 16f, pageTextPaint)
    }

    private fun createPaint(color: Int, textSize: Float, typeface: Typeface = Typeface.DEFAULT): Paint {
        return Paint().apply {
            this.color = color
            this.textSize = textSize
            this.typeface = typeface
            isAntiAlias = true
        }
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
            color = Color.rgb(240, 240, 240)
            textSize = 56f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            alpha = 25
            isAntiAlias = true
        }
        val centerX = PAGE_WIDTH / 2f
        val centerY = PAGE_HEIGHT / 2f
        val textWidth = paint.measureText(text)
        val fm = paint.fontMetrics
        val textHeight = fm.bottom - fm.top

        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.rotate(-25f)
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
            // Enhanced placeholder
            val border = Paint().apply {
                color = PRIMARY_GOLD
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            canvas.drawRoundRect(destRect, 8f, 8f, border)
            val tp = createPaint(LIGHT_TEXT, 11f)
            val placeholder = "📸 Foto Produk"
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