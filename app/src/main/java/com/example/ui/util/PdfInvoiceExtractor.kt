package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.data.remote.GeminiInvoiceAnalyzer
import com.example.ui.screens.BillItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

data class ExtractedPdfInvoice(
    val merchantName: String = "",
    val customerName: String = "",
    val invoiceNumber: String = "",
    val date: String = "",
    val items: List<BillItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val notes: String = "",
    val previewBitmap: Bitmap? = null,
    val extractionMethod: String = "",
    val rawText: String = "",
    val errorMessage: String? = null
)

object PdfInvoiceExtractor {

    /**
     * Safely reads and extracts invoice information (items, quantities, prices, metadata)
     * from a PDF Uri using native Android PdfRenderer and AI/heuristic analysis.
     */
    suspend fun extractInvoiceFromPdf(
        context: Context,
        uri: Uri
    ): ExtractedPdfInvoice = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var renderedBitmap: Bitmap? = null
        var rawExtractedText = ""

        try {
            // 1. Copy stream to cache file to ensure a seekable ParcelFileDescriptor
            tempFile = File(context.cacheDir, "pdf_import_${System.currentTimeMillis()}.pdf")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("تعذر فتح ملف الـ PDF")

            // 2. Extract any text strings from the raw PDF bytes
            try {
                rawExtractedText = extractTextFromPdfFile(tempFile)
            } catch (_: Exception) {}

            // 3. Render the first page of the PDF to a high-resolution Bitmap
            pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)

            if (renderer.pageCount > 0) {
                val page = renderer.openPage(0)
                // Determine scale factor for sharp rendering
                val targetWidth = 1400
                val scale = targetWidth.toFloat() / page.width.coerceAtLeast(1)
                val targetHeight = (page.height * scale).toInt().coerceAtLeast(1)

                val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderedBitmap = bitmap
            }
        } catch (e: Exception) {
            return@withContext ExtractedPdfInvoice(
                errorMessage = "خطأ أثناء قراءة ملف الـ PDF: ${e.localizedMessage ?: e.message}"
            )
        } finally {
            try { renderer?.close() } catch (_: Exception) {}
            try { pfd?.close() } catch (_: Exception) {}
            try { tempFile?.delete() } catch (_: Exception) {}
        }

        if (renderedBitmap == null) {
            return@withContext ExtractedPdfInvoice(
                errorMessage = "تعذر قراءة صفحات ملف الـ PDF"
            )
        }

        // 4. Try Gemini AI analysis first (multimodal image parser)
        try {
            val geminiResult = GeminiInvoiceAnalyzer.analyzeInvoiceImage(renderedBitmap)
            if (geminiResult.success && geminiResult.items.isNotEmpty()) {
                val billItems = geminiResult.items.mapIndexed { index, item ->
                    BillItem(
                        id = System.currentTimeMillis() + index,
                        itemCode = "${index + 1}",
                        name = item.name.trim(),
                        quantity = if (item.quantity > 0.0) item.quantity else 1.0,
                        unitPrice = item.unitPrice
                    )
                }

                val total = if (geminiResult.totalAmount > 0.0) {
                    geminiResult.totalAmount
                } else {
                    billItems.sumOf { it.total }
                }

                return@withContext ExtractedPdfInvoice(
                    merchantName = geminiResult.merchantName,
                    customerName = if (geminiResult.merchantName.isNotBlank() && !geminiResult.merchantName.contains("الفارس")) geminiResult.merchantName else "",
                    invoiceNumber = geminiResult.invoiceNumber.filter { it.isDigit() || it.isLetter() || it == '-' },
                    date = geminiResult.date.ifBlank { SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date()) },
                    items = billItems,
                    totalAmount = total,
                    paidAmount = 0.0,
                    notes = geminiResult.notes,
                    previewBitmap = renderedBitmap,
                    extractionMethod = "الذكاء الاصطناعي (Gemini AI)",
                    rawText = rawExtractedText
                )
            }
        } catch (_: Exception) {
            // Fallback to local parsing
        }

        // 5. Intelligent Fallback: Rule-based local parser on raw extracted PDF text
        val localExtracted = parseInvoiceTextLocally(rawExtractedText, renderedBitmap)
        return@withContext localExtracted
    }

    /**
     * Extracts text streams from a PDF file by scanning literal strings and text operators.
     */
    private fun extractTextFromPdfFile(file: File): String {
        val bytes = file.readBytes()
        val textBuilder = StringBuilder()
        val content = String(bytes, Charsets.ISO_8859_1)

        // Find strings enclosed in parentheses (literal PDF strings)
        val pattern = Pattern.compile("\\(([^\\(\\)\\\\]*)\\)")
        val matcher = pattern.matcher(content)
        while (matcher.find()) {
            val match = matcher.group(1)
            if (!match.isNullOrBlank() && match.length > 1) {
                // Try decoding UTF-8 or Arabic bytes if encoded
                val decoded = tryDecodeArabicOrUtf8(match)
                textBuilder.append(decoded).append("\n")
            }
        }

        return textBuilder.toString()
    }

    private fun tryDecodeArabicOrUtf8(raw: String): String {
        return try {
            val rawBytes = raw.toByteArray(Charsets.ISO_8859_1)
            val utf8 = String(rawBytes, Charsets.UTF_8)
            if (utf8.any { it.code in 0x0600..0x06FF }) utf8 else raw
        } catch (_: Exception) {
            raw
        }
    }

    /**
     * Heuristic parser that finds item lines, prices, quantities, and invoice numbers
     * from raw text extracted from PDF.
     */
    private fun parseInvoiceTextLocally(rawText: String, bitmap: Bitmap?): ExtractedPdfInvoice {
        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val candidateItems = mutableListOf<BillItem>()
        var foundInvoiceNumber = ""
        var foundCustomerName = ""
        var foundDate = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
        var foundTotal = 0.0

        val numberPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)")

        for (line in lines) {
            // Check for Invoice Number
            if (line.contains("فاتورة") || line.contains("رقم") || line.contains("Invoice") || line.contains("INV")) {
                val numMatch = numberPattern.matcher(line)
                if (numMatch.find() && foundInvoiceNumber.isEmpty()) {
                    foundInvoiceNumber = numMatch.group(1) ?: ""
                }
            }

            // Check for Customer
            if (line.contains("العميل") || line.contains("السيد") || line.contains("الأخ") || line.contains("زبون")) {
                val clean = line.replace("العميل", "").replace("السيد", "").replace("الأخ", "").replace(":", "").trim()
                if (clean.length > 2 && foundCustomerName.isEmpty()) {
                    foundCustomerName = clean
                }
            }

            // Check for Date
            val datePattern = Pattern.compile("(\\d{4}[/\\-]\\d{1,2}[/\\-]\\d{1,2}|\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{4})")
            val dateMatcher = datePattern.matcher(line)
            if (dateMatcher.find()) {
                foundDate = dateMatcher.group(1) ?: foundDate
            }

            // Check for Tabular Item Rows (contains words + numbers for qty and price)
            val matcher = numberPattern.matcher(line)
            val numbersInLine = mutableListOf<Double>()
            while (matcher.find()) {
                matcher.group(1)?.toDoubleOrNull()?.let { numbersInLine.add(it) }
            }

            // If line contains at least 2 numbers (like Qty and Price or Qty, Price, Total)
            // and has text representing item name
            val lettersOnly = line.replace(Regex("[0-9.,\\-:/#]"), "").trim()
            if (lettersOnly.length >= 3 && numbersInLine.isNotEmpty() && !line.contains("الإجمالي") && !line.contains("المجموع")) {
                val qty = if (numbersInLine.size >= 2) numbersInLine[0] else 1.0
                val price = if (numbersInLine.size >= 2) numbersInLine[1] else numbersInLine[0]

                if (price > 0.0 && qty in 0.1..10000.0) {
                    candidateItems.add(
                        BillItem(
                            id = System.currentTimeMillis() + candidateItems.size,
                            itemCode = "${candidateItems.size + 1}",
                            name = lettersOnly,
                            quantity = qty,
                            unitPrice = price
                        )
                    )
                }
            }

            // Check for Total
            if (line.contains("الإجمالي") || line.contains("المجموع") || line.contains("Total") || line.contains("الصافي")) {
                if (numbersInLine.isNotEmpty()) {
                    foundTotal = numbersInLine.last()
                }
            }
        }

        // If no items were parsed automatically from unstructured text,
        // provide sensible starter items extracted from detected text fragments
        val finalItems = if (candidateItems.isNotEmpty()) {
            candidateItems
        } else {
            // Create default single entry so the user can easily edit with the PDF preview
            listOf(
                BillItem(
                    id = System.currentTimeMillis(),
                    itemCode = "1",
                    name = "صنف مستورد من ملف PDF",
                    quantity = 1.0,
                    unitPrice = if (foundTotal > 0.0) foundTotal else 100.0
                )
            )
        }

        val calculatedTotal = if (foundTotal > 0.0) foundTotal else finalItems.sumOf { it.total }

        return ExtractedPdfInvoice(
            merchantName = "شركة الفارس للمطابخ والديكور",
            customerName = foundCustomerName.ifBlank { "عميل PDF" },
            invoiceNumber = foundInvoiceNumber.ifBlank { "${System.currentTimeMillis() % 10000}" },
            date = foundDate,
            items = finalItems,
            totalAmount = calculatedTotal,
            paidAmount = 0.0,
            notes = "تم الاستيراد من ملف PDF",
            previewBitmap = bitmap,
            extractionMethod = "القارئ الرقمي الذكي للمستندات (PDF Engine)",
            rawText = rawText
        )
    }
}
