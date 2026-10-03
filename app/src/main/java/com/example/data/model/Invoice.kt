package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val merchantName: String = "شركة الفارس للمطابخ التركية",
    val invoiceNumber: String = "",
    val date: String = "",
    val category: String = "مطابخ وديكور",
    val currency: String = "د.ل",
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentMethod: String = "نقدي",
    val status: String = "PAID", // PAID, PENDING, OVERDUE
    val notes: String = "",
    val bottomNotes: String = "",
    val imageUri: String? = null,
    val itemsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),

    // Fields for Alfares Invoices
    val companyName: String = "شركة الفارس للمطابخ التركية",
    val branch: String = "الطريق الساحلي الخروبة",
    val companyPhone: String = "0912127526",
    val invoiceType: String = "فاتورة خدمات",
    val hijriDate: String = "",
    val gregorianDate: String = "",
    val timeStr: String = "",
    val storeName: String = "المخزن الرئيسي",
    val customerNumber: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val preparedBy: String = "المستخدم الرئيسي"
) {
    fun parseItems(): List<InvoiceLineItem> {
        return try {
            val list = mutableListOf<InvoiceLineItem>()
            val array = JSONArray(itemsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InvoiceLineItem(
                        name = obj.optString("name", "صنف بدون اسم"),
                        quantity = obj.optDouble("quantity", 1.0),
                        unitPrice = obj.optDouble("unitPrice", 0.0),
                        total = obj.optDouble("total", 0.0)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun itemsToJson(items: List<InvoiceLineItem>): String {
            val array = JSONArray()
            for (item in items) {
                val obj = JSONObject().apply {
                    put("name", item.name)
                    put("quantity", item.quantity)
                    put("unitPrice", item.unitPrice)
                    put("total", item.total)
                }
                array.put(obj)
            }
            return array.toString()
        }
    }
}

data class InvoiceLineItem(
    val name: String,
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val total: Double = quantity * unitPrice
)

enum class InvoiceCategory(val titleArabic: String, val englishName: String) {
    GROCERY("بقالة ومواد غذائية", "Grocery"),
    RESTAURANT("مطاعم ومقاهي", "Restaurant"),
    ELECTRONICS("إلكترونيات وأجهزة", "Electronics"),
    UTILITIES("خدمات وفواتير", "Utilities"),
    HEALTHCARE("صحة وأدوية", "Healthcare"),
    TRANSPORT("مواصلات ووقود", "Transport"),
    OFFICE("مستلزمات مكتبية", "Office Supplies"),
    OTHER("أخرى وعامة", "Other");

    companion object {
        fun fromString(value: String): InvoiceCategory {
            return entries.firstOrNull {
                it.titleArabic.contains(value, ignoreCase = true) ||
                it.englishName.equals(value, ignoreCase = true) ||
                value.contains(it.name, ignoreCase = true)
            } ?: OTHER
        }
    }
}

enum class InvoiceStatus(val labelArabic: String, val key: String) {
    PAID("مدفوعة", "PAID"),
    PENDING("معلقة / غير مسددة", "PENDING"),
    OVERDUE("متأخرة", "OVERDUE")
}
