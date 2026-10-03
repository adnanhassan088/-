package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceLineItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class InvoiceAnalysisResult(
    val success: Boolean,
    val merchantName: String = "",
    val invoiceNumber: String = "",
    val date: String = "",
    val category: String = "عام",
    val currency: String = "ر.س",
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "نقدي",
    val notes: String = "",
    val items: List<InvoiceLineItem> = emptyList(),
    val errorMessage: String? = null,
    val rawJson: String = ""
)

object GeminiInvoiceAnalyzer {
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Resize if too huge to keep latency low
        val scaledBitmap = if (bitmap.width > 1200 || bitmap.height > 1200) {
            val ratio = 1200f / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else {
            bitmap
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun analyzeInvoiceImage(
        bitmap: Bitmap,
        customApiKey: String? = null
    ): InvoiceAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = when {
            !customApiKey.isNullOrBlank() -> customApiKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (apiKey.isBlank()) {
            return@withContext InvoiceAnalysisResult(
                success = false,
                errorMessage = "مفتاح Gemini API غير معين. يمكنك إدخال مفتاح API الخاص بك في صفحة الإعدادات أو استخدام التحليل التجريبي الذكي."
            )
        }

        val base64Data = bitmapToBase64(bitmap)
        val prompt = """
            You are a bilingual (Arabic and English) expert invoice and receipt parser. 
            Examine this invoice/receipt image carefully and extract all financial and operational details.
            
            Return STRICTLY a JSON object with this exact structure:
            {
              "merchantName": "اسم المتجر أو الشركة",
              "invoiceNumber": "رقم الفاتورة",
              "date": "YYYY-MM-DD أو التاريخ كما هو بالفاتورة",
              "category": "بقالة ومواد غذائية | مطاعم ومقاهي | إلكترونيات وأجهزة | خدمات وفواتير | صحة وأدوية | مواصلات ووقود | مستلزمات مكتبية | أخرى وعامة",
              "currency": "ر.س أو ج.م أو د.إ أو $ أو العملة الموضحة",
              "items": [
                {
                  "name": "اسم الصنف أو الخدمة",
                  "quantity": 1.0,
                  "unitPrice": 10.0,
                  "total": 10.0
                }
              ],
              "subtotal": 0.0,
              "taxAmount": 0.0,
              "discountAmount": 0.0,
              "totalAmount": 0.0,
              "paymentMethod": "نقدي أو بطاقة مدى أو تحويل أو فيزا",
              "notes": "ملخص أو وصف موجز للفاتورة بالعربية"
            }
            Ensure numbers are valid doubles/floats. If any field is missing or unreadable, give a sensible estimate or leave empty. Return pure JSON without markdown backticks.
        """.trimIndent()

        try {
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            // Text part
                            put(JSONObject().apply { put("text", prompt) })
                            // Image part
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Data)
                                }
                                put("inlineData", inlineData)
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext InvoiceAnalysisResult(
                    success = false,
                    errorMessage = "فشل الاتصال بـ Gemini API: ${response.code} ${response.message}"
                )
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textPart = parts?.optJSONObject(0)?.optString("text", "") ?: ""

            if (textPart.isBlank()) {
                return@withContext InvoiceAnalysisResult(
                    success = false,
                    errorMessage = "لم يتمكن النموذج من استخراج بيانات من الصورة."
                )
            }

            parseExtractedJson(textPart)
        } catch (e: Exception) {
            InvoiceAnalysisResult(
                success = false,
                errorMessage = "خطأ أثناء تحليل الفاتورة: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    fun parseExtractedJson(rawJson: String): InvoiceAnalysisResult {
        return try {
            // Clean markdown blocks if present
            var clean = rawJson.trim()
            if (clean.startsWith("```json")) {
                clean = clean.removePrefix("```json")
            }
            if (clean.startsWith("```")) {
                clean = clean.removePrefix("```")
            }
            if (clean.endsWith("```")) {
                clean = clean.removeSuffix("```")
            }
            clean = clean.trim()

            val json = JSONObject(clean)
            val merchantName = json.optString("merchantName", "فاتورة بدون اسم")
            val invoiceNumber = json.optString("invoiceNumber", "INV-${System.currentTimeMillis() % 100000}")
            val date = json.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            val category = json.optString("category", "أخرى وعامة")
            val currency = json.optString("currency", "ر.س")
            val subtotal = json.optDouble("subtotal", 0.0)
            val taxAmount = json.optDouble("taxAmount", 0.0)
            val discountAmount = json.optDouble("discountAmount", 0.0)
            var totalAmount = json.optDouble("totalAmount", 0.0)
            val paymentMethod = json.optString("paymentMethod", "نقدي")
            val notes = json.optString("notes", "")

            val itemsList = mutableListOf<InvoiceLineItem>()
            val itemsArray = json.optJSONArray("items")
            if (itemsArray != null) {
                for (i in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(i)
                    val name = itemObj.optString("name", "صنف ${i + 1}")
                    val quantity = itemObj.optDouble("quantity", 1.0)
                    val unitPrice = itemObj.optDouble("unitPrice", 0.0)
                    val total = itemObj.optDouble("total", quantity * unitPrice)
                    itemsList.add(InvoiceLineItem(name, quantity, unitPrice, total))
                }
            }

            if (totalAmount == 0.0 && itemsList.isNotEmpty()) {
                totalAmount = itemsList.sumOf { it.total } + taxAmount - discountAmount
            }

            InvoiceAnalysisResult(
                success = true,
                merchantName = merchantName,
                invoiceNumber = invoiceNumber,
                date = date,
                category = category,
                currency = currency,
                subtotal = subtotal,
                taxAmount = taxAmount,
                discountAmount = discountAmount,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                notes = notes,
                items = itemsList,
                rawJson = clean
            )
        } catch (e: Exception) {
            InvoiceAnalysisResult(
                success = false,
                errorMessage = "خطأ في قراءة بيانات الفاتورة المحللة: ${e.message}"
            )
        }
    }
}
