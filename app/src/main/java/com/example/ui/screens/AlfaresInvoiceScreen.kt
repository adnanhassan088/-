package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.InvoiceEntity
import com.example.ui.components.AlfaresPrintPreviewSheet
import com.example.ui.theme.CharcoalBackground
import com.example.ui.theme.CharcoalCardBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalSurfaceVariant
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.RedRemove
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.util.ArabicNumberToWords
import com.example.ui.util.ExtractedPdfInvoice
import com.example.ui.util.InvoiceHtmlPrinter
import com.example.ui.util.PdfInvoiceExtractor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BillItem(
    val id: Long = System.currentTimeMillis(),
    val itemCode: String = "",
    val name: String,
    val quantity: Double,
    val unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}

fun billItemsToJson(items: List<BillItem>): String {
    val array = JSONArray()
    for (item in items) {
        val obj = JSONObject().apply {
            put("id", item.id)
            put("itemCode", item.itemCode)
            put("name", item.name)
            put("quantity", item.quantity)
            put("unitPrice", item.unitPrice)
        }
        array.put(obj)
    }
    return array.toString()
}

fun jsonToBillItems(json: String): List<BillItem> {
    return try {
        val list = mutableListOf<BillItem>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                BillItem(
                    id = obj.optLong("id", System.currentTimeMillis() + i),
                    itemCode = obj.optString("itemCode", "${i + 1}"),
                    name = obj.optString("name", "صنف"),
                    quantity = obj.optDouble("quantity", 1.0),
                    unitPrice = obj.optDouble("unitPrice", 0.0)
                )
            )
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}

data class QuickItemPreset(
    val code: String,
    val name: String,
    val defaultPrice: Double,
    val category: String
)

enum class InvoiceStatusFilter {
    ALL, PAID, PARTIAL, UNPAID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlfaresInvoiceScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("alfares_invoice_draft", Context.MODE_PRIVATE) }
    val database = remember { AppDatabase.getDatabase(context) }
    val savedInvoices by database.invoiceDao().getAllInvoices().collectAsState(initial = emptyList())

    // Two Primary Navigation Pages:
    // 0 = صفحة إضافة الفواتير (Add Invoice Page - Image 1)
    // 1 = صفحة عرض الفواتير (View Invoices Page - Image 2)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Handle Android system back press to return from Tab 1 to Tab 0
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    // Modal state for A4 paper preview dialog
    var showA4PreviewDialog by remember { mutableStateOf(false) }
    var previewInvoiceEntity by remember { mutableStateOf<InvoiceEntity?>(null) }

    // PDF Import and Extraction State
    var isProcessingPdf by remember { mutableStateOf(false) }
    var extractedPdfResult by remember { mutableStateOf<ExtractedPdfInvoice?>(null) }
    var showPdfPreviewDialog by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            isProcessingPdf = true
            coroutineScope.launch {
                try {
                    val result = PdfInvoiceExtractor.extractInvoiceFromPdf(context, uri)
                    if (result.errorMessage != null && result.items.isEmpty()) {
                        Toast.makeText(context, result.errorMessage, Toast.LENGTH_LONG).show()
                    } else {
                        extractedPdfResult = result
                        showPdfPreviewDialog = true
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "حدث خطأ أثناء قراءة ملف الـ PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isProcessingPdf = false
                }
            }
        }
    }

    // Search and filter for the second page (صفحة عرض الفواتير)
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(InvoiceStatusFilter.ALL) }
    var invoiceToDelete by remember { mutableStateOf<InvoiceEntity?>(null) }

    // Current invoice ID in DB (if editing an existing saved invoice)
    var currentInvoiceDbId by remember {
        val savedId = prefs.getLong("currentInvoiceDbId", -1L)
        mutableStateOf<Long?>(if (savedId > 0) savedId else null)
    }

    // Company & Invoice Meta State
    var companyName by remember {
        mutableStateOf(prefs.getString("companyName", "شركة الفارس للمطابخ التركية") ?: "شركة الفارس للمطابخ التركية")
    }
    var branch by remember {
        mutableStateOf(prefs.getString("branch", "الطريق الساحلي الخروبة") ?: "الطريق الساحلي الخروبة")
    }
    var companyPhone by remember {
        mutableStateOf(prefs.getString("companyPhone", "0912127526") ?: "0912127526")
    }
    var invoiceType by remember {
        mutableStateOf(prefs.getString("invoiceType", "فاتورة خدمات") ?: "فاتورة خدمات")
    }
    var invoiceNumber by remember {
        mutableStateOf(prefs.getString("invoiceNumber", "1") ?: "1")
    }
    var hijriDate by remember {
        mutableStateOf(prefs.getString("hijriDate", "1448/03/21") ?: "1448/03/21")
    }
    var gregorianDate by remember {
        mutableStateOf(prefs.getString("gregorianDate", SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())) ?: SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date()))
    }
    var timeStr by remember {
        mutableStateOf(prefs.getString("timeStr", SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date()).replace("AM", "ص").replace("PM", "م")) ?: "10:30:00 ص")
    }
    var storeName by remember {
        mutableStateOf(prefs.getString("storeName", "المخزن الرئيسي") ?: "المخزن الرئيسي")
    }

    // Customer info - Default completely empty for new customer!
    var customerNumber by remember {
        mutableStateOf(prefs.getString("customerNumber", "1") ?: "1")
    }
    var customerName by remember {
        mutableStateOf(prefs.getString("customerName", "") ?: "")
    }
    var customerAddress by remember {
        mutableStateOf(prefs.getString("customerAddress", "") ?: "")
    }
    var customerPhone by remember {
        mutableStateOf(prefs.getString("customerPhone", "") ?: "")
    }
    var notes by remember {
        mutableStateOf(prefs.getString("notes", "") ?: "")
    }
    var bottomNotes by remember {
        mutableStateOf(prefs.getString("bottomNotes", "ملاحظات: البضاعة المستلمة بحالة ممتازة - يسري الضمان حسب عقد التوريد والتركيب.") ?: "ملاحظات: البضاعة المستلمة بحالة ممتازة - يسري الضمان حسب عقد التوريد والتركيب.")
    }
    var paidAmountStr by remember {
        mutableStateOf(prefs.getString("paidAmountStr", "") ?: "")
    }
    var preparedBy by remember {
        mutableStateOf(prefs.getString("preparedBy", "المستخدم الرئيسي") ?: "المستخدم الرئيسي")
    }

    // Input fields for adding items: Name, Code, Quantity, Unit Price
    var inputItemName by remember { mutableStateOf("") }
    var inputItemCode by remember { mutableStateOf("") }
    var inputQuantityStr by remember { mutableStateOf("1") }
    var inputUnitPriceStr by remember { mutableStateOf("") }

    // List of added bill items
    val billItems = remember {
        val savedJson = prefs.getString("itemsJson", null)
        val initialList = if (!savedJson.isNullOrBlank()) {
            val parsed = jsonToBillItems(savedJson)
            if (parsed.isNotEmpty()) parsed else null
        } else null

        val finalList = initialList ?: listOf(
            BillItem(1, "1", "درابزين زجاج", 14.5, 680.0),
            BillItem(2, "2", "حماية خارجية زجاج", 17.8, 750.0),
            BillItem(3, "3", "ابواب داخليه ستانلستيل", 1.0, 7300.0),
            BillItem(4, "4", "ابواب داخليه 4.5 سم", 6.0, 1750.0),
            BillItem(5, "5", "باب خارجي 7 سم باب و ثلث تلبيسة قشرة موقنة", 1.0, 4000.0),
            BillItem(6, "6", "باب خارجي 7 سم موقنة", 1.0, 5500.0),
            BillItem(7, "7", "قفص ديكور", 1.0, 850.0)
        )
        mutableStateListOf<BillItem>().apply { addAll(finalList) }
    }

    var showResetDialog by remember { mutableStateOf(false) }

    // Computations
    val totalAmount by remember {
        derivedStateOf { billItems.sumOf { it.total } }
    }
    val tafqeetText by remember {
        derivedStateOf { ArabicNumberToWords.toTafqeet(totalAmount) }
    }
    val paidAmount by remember {
        derivedStateOf { paidAmountStr.toDoubleOrNull() ?: 0.0 }
    }
    val remainingAmount by remember {
        derivedStateOf { (totalAmount - paidAmount).coerceAtLeast(0.0) }
    }

    // Save current invoice draft to SharedPreferences
    fun persistDraft() {
        prefs.edit()
            .putString("companyName", companyName)
            .putString("branch", branch)
            .putString("companyPhone", companyPhone)
            .putString("invoiceType", invoiceType)
            .putString("invoiceNumber", invoiceNumber)
            .putString("hijriDate", hijriDate)
            .putString("gregorianDate", gregorianDate)
            .putString("timeStr", timeStr)
            .putString("storeName", storeName)
            .putString("customerNumber", customerNumber)
            .putString("customerName", customerName)
            .putString("customerAddress", customerAddress)
            .putString("customerPhone", customerPhone)
            .putString("notes", notes)
            .putString("bottomNotes", bottomNotes)
            .putString("paidAmountStr", paidAmountStr)
            .putString("preparedBy", preparedBy)
            .putString("itemsJson", billItemsToJson(billItems))
            .apply()
    }

    // Save invoice to Room DB
    fun saveInvoiceToDb(showToast: Boolean = true) {
        coroutineScope.launch {
            try {
                val entity = InvoiceEntity(
                    id = currentInvoiceDbId ?: 0L,
                    companyName = companyName,
                    branch = branch,
                    companyPhone = companyPhone,
                    invoiceType = invoiceType,
                    invoiceNumber = invoiceNumber.ifBlank { "1" },
                    hijriDate = hijriDate,
                    gregorianDate = gregorianDate,
                    timeStr = timeStr,
                    storeName = storeName,
                    customerNumber = customerNumber,
                    customerName = customerName.ifBlank { "عميل عام" },
                    customerAddress = customerAddress,
                    customerPhone = customerPhone,
                    notes = notes,
                    bottomNotes = bottomNotes,
                    preparedBy = preparedBy,
                    merchantName = companyName,
                    date = gregorianDate,
                    currency = "د.ل",
                    totalAmount = totalAmount,
                    subtotal = totalAmount,
                    paidAmount = paidAmount,
                    remainingAmount = remainingAmount,
                    status = if (remainingAmount <= 0.0 && totalAmount > 0.0) "PAID" else if (paidAmount > 0.0) "PARTIAL" else "PENDING",
                    itemsJson = billItemsToJson(billItems),
                    createdAt = System.currentTimeMillis()
                )
                val insertedId = database.invoiceDao().insertInvoice(entity)
                currentInvoiceDbId = insertedId
                prefs.edit().putLong("currentInvoiceDbId", insertedId).apply()
                persistDraft()
                if (showToast) {
                    Toast.makeText(context, "تم حفظ الفاتورة (#$invoiceNumber) بنجاح في السجل الدائم! 💾", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "حدث خطأ أثناء الحفظ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load invoice from archive into editor (Page 1)
    fun loadInvoice(invoice: InvoiceEntity) {
        currentInvoiceDbId = invoice.id
        companyName = invoice.companyName.ifBlank { "شركة الفارس للمطابخ التركية" }
        branch = invoice.branch.ifBlank { "الطريق الساحلي الخروبة" }
        companyPhone = invoice.companyPhone.ifBlank { "0912127526" }
        invoiceType = invoice.invoiceType.ifBlank { "فاتورة خدمات" }
        invoiceNumber = invoice.invoiceNumber
        customerNumber = invoice.customerNumber.ifBlank { invoice.invoiceNumber }
        hijriDate = invoice.hijriDate.ifBlank { "1448/03/21" }
        gregorianDate = invoice.gregorianDate.ifBlank { invoice.date }
        timeStr = invoice.timeStr.ifBlank { "10:00:00 ص" }
        storeName = invoice.storeName.ifBlank { "المخزن الرئيسي" }
        customerName = invoice.customerName
        customerAddress = invoice.customerAddress
        customerPhone = invoice.customerPhone
        notes = invoice.notes
        bottomNotes = invoice.bottomNotes.ifBlank { "ملاحظات: البضاعة المستلمة بحالة ممتازة - يسري الضمان حسب عقد التوريد والتركيب." }
        paidAmountStr = if (invoice.paidAmount > 0.0) {
            if (invoice.paidAmount % 1.0 == 0.0) invoice.paidAmount.toInt().toString() else String.format(Locale.US, "%.2f", invoice.paidAmount)
        } else ""
        preparedBy = invoice.preparedBy.ifBlank { "المستخدم الرئيسي" }

        val loadedItems = jsonToBillItems(invoice.itemsJson)
        billItems.clear()
        billItems.addAll(loadedItems)

        persistDraft()
        selectedTab = 0 // Navigate to Page 1: صفحة إضافة الفواتير
        Toast.makeText(context, "تم فتح الفاتورة رقم #${invoice.invoiceNumber} للعميل ${invoice.customerName} في صفحة الإضافة", Toast.LENGTH_SHORT).show()
    }

    // Dynamic sequential invoice number calculation (ترتيبي)
    fun getNextSequentialNumber(): String {
        val numbers = savedInvoices.mapNotNull { inv ->
            inv.invoiceNumber.trim().toIntOrNull()
                ?: Regex("""\d+""").find(inv.invoiceNumber)?.value?.toIntOrNull()
        }
        val maxNum = numbers.maxOrNull() ?: 0
        return (maxNum + 1).toString()
    }

    // Start a brand new invoice with sequential number and completely cleared customer details
    fun startNewInvoice() {
        currentInvoiceDbId = null
        val nextNumber = getNextSequentialNumber()
        invoiceNumber = nextNumber
        customerNumber = nextNumber
        customerName = "" // تفريغ اسم الزبون تماماً للزبون الجديد
        customerPhone = ""
        customerAddress = ""
        notes = ""
        bottomNotes = "ملاحظات: البضاعة المستلمة بحالة ممتازة - يسري الضمان حسب عقد التوريد والتركيب."
        paidAmountStr = ""
        inputItemName = ""
        inputItemCode = ""
        inputQuantityStr = "1"
        inputUnitPriceStr = ""
        gregorianDate = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
        timeStr = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date()).replace("AM", "ص").replace("PM", "م")
        billItems.clear()
        selectedTab = 0 // Navigate to Page 1: صفحة إضافة الفواتير

        prefs.edit()
            .remove("currentInvoiceDbId")
            .remove("customerName")
            .remove("customerPhone")
            .remove("customerAddress")
            .remove("notes")
            .remove("paidAmountStr")
            .remove("itemsJson")
            .putString("invoiceNumber", nextNumber)
            .putString("customerNumber", nextNumber)
            .apply()

        Toast.makeText(context, "تم بدء فاتورة جديدة فارغة برقم ترتيبي #$nextNumber", Toast.LENGTH_SHORT).show()
    }

    // Auto-sync sequential numbering on new invoices
    LaunchedEffect(savedInvoices) {
        if (currentInvoiceDbId == null) {
            val nextSeq = getNextSequentialNumber()
            if (customerName == "الأخ غنيمة") {
                customerName = "" // تفريغ اسم الزبون التجريبي القديم
            }
            if (invoiceNumber.isBlank() || invoiceNumber == "1" || invoiceNumber == "12") {
                invoiceNumber = nextSeq
                customerNumber = nextSeq
            }
        }
    }

    // Apply extracted PDF data to current invoice
    fun applyExtractedPdf(result: ExtractedPdfInvoice, replaceAll: Boolean) {
        if (replaceAll) {
            currentInvoiceDbId = null
            if (result.customerName.isNotBlank() && result.customerName != "عميل PDF") {
                customerName = result.customerName
            }
            if (result.invoiceNumber.isNotBlank()) {
                invoiceNumber = result.invoiceNumber
                customerNumber = result.invoiceNumber
            }
            if (result.date.isNotBlank()) {
                gregorianDate = result.date
            }
            if (result.notes.isNotBlank()) {
                notes = result.notes
            }
            billItems.clear()
            billItems.addAll(result.items)
        } else {
            val startIndex = billItems.size
            result.items.forEachIndexed { i, item ->
                billItems.add(
                    item.copy(
                        id = System.currentTimeMillis() + i,
                        itemCode = (startIndex + i + 1).toString()
                    )
                )
            }
            if (customerName.isBlank() && result.customerName.isNotBlank() && result.customerName != "عميل PDF") {
                customerName = result.customerName
            }
        }
        persistDraft()
        selectedTab = 0
        Toast.makeText(context, "تم إدراج ${result.items.size} أصناف من ملف الـ PDF بنجاح! 📄✨", Toast.LENGTH_LONG).show()
    }

    // Seed initial invoice into Room DB if empty (for history archive)
    LaunchedEffect(Unit) {
        try {
            val count = database.invoiceDao().getInvoiceCount().first()
            if (count == 0) {
                val seedItems = listOf(
                    BillItem(1, "1", "درابزين زجاج", 14.5, 680.0),
                    BillItem(2, "2", "حماية خارجية زجاج", 17.8, 750.0),
                    BillItem(3, "3", "ابواب داخليه ستانلستيل", 1.0, 7300.0),
                    BillItem(4, "4", "ابواب داخليه 4.5 سم", 6.0, 1750.0),
                    BillItem(5, "5", "باب خارجي 7 سم باب و ثلث تلبيسة قشرة موقنة", 1.0, 4000.0),
                    BillItem(6, "6", "باب خارجي 7 سم موقنة", 1.0, 5500.0),
                    BillItem(7, "7", "قفص ديكور", 1.0, 850.0)
                )
                val initialEntity = InvoiceEntity(
                    companyName = "شركة الفارس للمطابخ التركية",
                    branch = "الطريق الساحلي الخروبة",
                    companyPhone = "0912127526",
                    invoiceType = "فاتورة خدمات",
                    invoiceNumber = "12",
                    hijriDate = "1448/03/21",
                    gregorianDate = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date()),
                    timeStr = "10:30:00 ص",
                    storeName = "المخزن الرئيسي",
                    customerNumber = "12",
                    customerName = "الأخ غنيمة",
                    customerAddress = "الخروبة - الطريق الساحلي",
                    customerPhone = "0912127526",
                    notes = "تسليم وتركيب حسب القياسات",
                    bottomNotes = "ملاحظات: البضاعة المستلمة بحالة ممتازة - يسري الضمان حسب عقد التوريد والتركيب.",
                    preparedBy = "المستخدم الرئيسي",
                    merchantName = "شركة الفارس للمطابخ التركية",
                    date = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date()),
                    currency = "د.ل",
                    totalAmount = seedItems.sumOf { it.total },
                    subtotal = seedItems.sumOf { it.total },
                    paidAmount = 0.0,
                    remainingAmount = seedItems.sumOf { it.total },
                    itemsJson = billItemsToJson(seedItems),
                    createdAt = System.currentTimeMillis()
                )
                database.invoiceDao().insertInvoice(initialEntity)
            }
        } catch (_: Exception) {}
    }

    // Quick presets
    val presets = remember {
        listOf(
            QuickItemPreset("1", "درابزين زجاج", 680.0, "زجاج"),
            QuickItemPreset("2", "حماية خارجية زجاج", 750.0, "زجاج"),
            QuickItemPreset("3", "ابواب داخليه ستانلستيل", 7300.0, "أبواب"),
            QuickItemPreset("4", "ابواب داخليه 4.5 سم", 1750.0, "أبواب"),
            QuickItemPreset("5", "باب خارجي 7 سم موقنة", 5500.0, "أبواب"),
            QuickItemPreset("6", "قفص ديكور", 850.0, "ديكور"),
            QuickItemPreset("7", "مطبخ تركي مودرن", 4500.0, "مطابخ")
        )
    }

    // Helper to add item
    fun addNewItem() {
        val name = inputItemName.trim()
        val code = inputItemCode.trim().ifBlank { (billItems.size + 1).toString() }
        val qty = inputQuantityStr.toDoubleOrNull() ?: 1.0
        val price = inputUnitPriceStr.toDoubleOrNull() ?: 0.0

        if (name.isNotEmpty() && price > 0.0) {
            billItems.add(
                BillItem(
                    id = System.currentTimeMillis(),
                    itemCode = code,
                    name = name,
                    quantity = qty,
                    unitPrice = price
                )
            )
            inputItemName = ""
            inputItemCode = ""
            inputQuantityStr = "1"
            inputUnitPriceStr = ""
            focusManager.clearFocus()
            persistDraft()
            Toast.makeText(context, "تمت إضافة الصنف [$name] إلى جدول الفاتورة بنجاح", Toast.LENGTH_SHORT).show()
        } else if (name.isEmpty()) {
            Toast.makeText(context, "الرجاء كتابة اسم الصنف", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "الرجاء إدخال السعر", Toast.LENGTH_SHORT).show()
        }
    }

    // Printing action
    fun executePrint(targetInvoice: InvoiceEntity? = null) {
        val logoBase64 = InvoiceHtmlPrinter.getLogoBase64(context)
        val html = if (targetInvoice == null) {
            InvoiceHtmlPrinter.generateHtml(
                companyName = companyName,
                branch = branch,
                phone = companyPhone,
                invoiceType = invoiceType,
                invoiceNumber = invoiceNumber,
                hijriDate = hijriDate,
                gregorianDate = gregorianDate,
                timeStr = timeStr,
                storeName = storeName,
                customerNumber = customerNumber,
                customerName = customerName,
                customerAddress = customerAddress,
                customerPhone = customerPhone,
                notes = notes,
                items = billItems,
                totalAmount = totalAmount,
                tafqeetText = tafqeetText,
                remainingAmount = remainingAmount,
                preparedBy = preparedBy,
                paidAmount = paidAmount,
                bottomNotes = bottomNotes,
                logoBase64 = logoBase64
            )
        } else {
            val loadedItems = jsonToBillItems(targetInvoice.itemsJson)
            InvoiceHtmlPrinter.generateHtml(
                companyName = targetInvoice.companyName.ifBlank { "شركة الفارس للمطابخ التركية" },
                branch = targetInvoice.branch.ifBlank { "الطريق الساحلي الخروبة" },
                phone = targetInvoice.companyPhone.ifBlank { "0912127526" },
                invoiceType = targetInvoice.invoiceType.ifBlank { "فاتورة خدمات" },
                invoiceNumber = targetInvoice.invoiceNumber,
                hijriDate = targetInvoice.hijriDate.ifBlank { "1448/03/21" },
                gregorianDate = targetInvoice.gregorianDate.ifBlank { targetInvoice.date },
                timeStr = targetInvoice.timeStr.ifBlank { "10:00:00 ص" },
                storeName = targetInvoice.storeName.ifBlank { "المخزن الرئيسي" },
                customerNumber = targetInvoice.customerNumber,
                customerName = targetInvoice.customerName,
                customerAddress = targetInvoice.customerAddress,
                customerPhone = targetInvoice.customerPhone,
                notes = targetInvoice.notes,
                items = loadedItems,
                totalAmount = targetInvoice.totalAmount,
                tafqeetText = ArabicNumberToWords.toTafqeet(targetInvoice.totalAmount),
                remainingAmount = if (targetInvoice.paidAmount > 0.0 || targetInvoice.remainingAmount > 0.0) targetInvoice.remainingAmount else targetInvoice.totalAmount,
                preparedBy = targetInvoice.preparedBy.ifBlank { "المستخدم الرئيسي" },
                paidAmount = targetInvoice.paidAmount,
                bottomNotes = targetInvoice.bottomNotes,
                logoBase64 = logoBase64
            )
        }
        InvoiceHtmlPrinter.printInvoice(context, html)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CharcoalBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.alfares_logo),
                            contentDescription = "شعار شركة الفارس",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(
                                text = "شركة الفارس للمطابخ والديكور",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                            )
                            Text(
                                text = if (selectedTab == 0) "صفحة إضافة وتحرير الفواتير" else "صفحة استعراض وسجل الفواتير",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                },
                actions = {
                    // Quick Save Button
                    IconButton(
                        onClick = { saveInvoiceToDb(true) },
                        modifier = Modifier.testTag("top_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "حفظ الفاتورة في السجل الدائم",
                            tint = GoldPrimary
                        )
                    }

                    // Direct Print
                    IconButton(
                        onClick = { executePrint() },
                        modifier = Modifier.testTag("top_print_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "طباعة الفاتورة",
                            tint = GoldLight
                        )
                    }

                    // Import invoice from PDF
                    IconButton(
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        modifier = Modifier.testTag("top_pdf_import_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "استيراد فاتورة من ملف PDF",
                            tint = Color(0xFFEF5350)
                        )
                    }

                    // A4 Preview Dialog
                    IconButton(
                        onClick = {
                            previewInvoiceEntity = null
                            showA4PreviewDialog = true
                        },
                        modifier = Modifier.testTag("top_preview_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "معاينة الفاتورة A4",
                            tint = GoldLight
                        )
                    }

                    // New Invoice Button
                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("top_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "بدء فاتورة جديدة",
                            tint = TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ==================== PRIMARY 2-PAGE TABS NAVIGATION ====================
            // Page 1: صفحة إضافة الفواتير (نفس الصورة الأولى)
            // Page 2: صفحة عرض الفواتير (نفس الصورة الثانية)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CharcoalSurface,
                contentColor = GoldPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldPrimary,
                        height = 3.5.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "صفحة إضافة الفواتير",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    },
                    selectedContentColor = GoldPrimary,
                    unselectedContentColor = TextMuted
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "صفحة عرض الفواتير (${savedInvoices.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    },
                    selectedContentColor = GoldPrimary,
                    unselectedContentColor = TextMuted
                )
            }

            // ==================== TAB CONTENTS ====================
            if (selectedTab == 0) {
                // =========================================================================
                // PAGE 1: صفحة إضافة الفواتير (CREATE / EDIT INVOICE - IMAGE 1)
                // =========================================================================
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Card 1: بيانات العميل والفاتورة (Customer & Invoice Information)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("invoice_meta_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    listOf(GoldPrimary.copy(alpha = 0.5f), CharcoalCardBorder)
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "بيانات الفاتورة والعميل",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GoldLight
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GoldContainer)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "فاتورة رقم: $invoiceNumber",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GoldPrimary
                                        )
                                    }
                                }

                                HorizontalDivider(color = CharcoalCardBorder.copy(alpha = 0.6f))

                                // Row 1: اسم العميل ورقم الفاتورة
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customerName,
                                        onValueChange = {
                                            customerName = it
                                            persistDraft()
                                        },
                                        label = { Text("اسم العميل *") },
                                        placeholder = { Text("اكتب اسم الزبون الجديد...") },
                                        trailingIcon = {
                                            if (customerName.isNotEmpty()) {
                                                IconButton(onClick = {
                                                    customerName = ""
                                                    persistDraft()
                                                }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "مسح اسم العميل", tint = TextMuted)
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .testTag("input_customer_name"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = CharcoalSurfaceVariant,
                                            unfocusedContainerColor = CharcoalSurfaceVariant,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = CharcoalCardBorder,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        )
                                    )
                                    OutlinedTextField(
                                        value = invoiceNumber,
                                        onValueChange = {
                                            invoiceNumber = it
                                            customerNumber = it
                                            persistDraft()
                                        },
                                        label = { Text("رقم الفاتورة") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = CharcoalSurfaceVariant,
                                            unfocusedContainerColor = CharcoalSurfaceVariant,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = CharcoalCardBorder,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        )
                                    )
                                }

                                // Row 2: هاتف العميل ونوع الفاتورة
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customerPhone,
                                        onValueChange = {
                                            customerPhone = it
                                            persistDraft()
                                        },
                                        label = { Text("هاتف العميل") },
                                        placeholder = { Text("0912127526") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = CharcoalSurfaceVariant,
                                            unfocusedContainerColor = CharcoalSurfaceVariant,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = CharcoalCardBorder,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        )
                                    )
                                    OutlinedTextField(
                                        value = invoiceType,
                                        onValueChange = {
                                            invoiceType = it
                                            persistDraft()
                                        },
                                        label = { Text("نوع الفاتورة") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = CharcoalSurfaceVariant,
                                            unfocusedContainerColor = CharcoalSurfaceVariant,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = CharcoalCardBorder,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        )
                                    )
                                }

                                // Row 3: تاريخ الفاتورة والفرع
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = gregorianDate,
                                        onValueChange = {
                                            gregorianDate = it
                                            persistDraft()
                                        },
                                        label = { Text("التاريخ") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = CharcoalSurfaceVariant,
                                            unfocusedContainerColor = CharcoalSurfaceVariant,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = CharcoalCardBorder,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        )
                                    )
                                    OutlinedTextField(
                                        value = branch,
                                        onValueChange = {
                                            branch = it
                                            persistDraft()
                                        },
                                        label = { Text("الفرع / الموقع") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = CharcoalSurfaceVariant,
                                            unfocusedContainerColor = CharcoalSurfaceVariant,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = CharcoalCardBorder,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        )
                                    )
                                }

                                // ملاحظات الفاتورة العامة
                                OutlinedTextField(
                                    value = notes,
                                    onValueChange = {
                                        notes = it
                                        persistDraft()
                                    },
                                    label = { Text("ملاحظات الفاتورة") },
                                    placeholder = { Text("تسليم وتركيب حسب القياسات...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = CharcoalSurfaceVariant,
                                        unfocusedContainerColor = CharcoalSurfaceVariant,
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = CharcoalCardBorder,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    )
                                )
                            }
                        }
                    }

                    // Card: استيراد فاتورة من ملف PDF (Import Invoice from PDF)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("import_pdf_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFE53935).copy(alpha = 0.5f), GoldPrimary.copy(alpha = 0.5f))
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFE53935).copy(alpha = 0.18f))
                                            .border(1.dp, Color(0xFFE53935).copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = Color(0xFFEF5350),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "إضافة فاتورة من ملف PDF",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = TextWhite
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(GoldContainer)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "استخراج ذكي",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = GoldPrimary,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                        Text(
                                            text = "استخراج اسم الصنف، الكمية، والسعر تلقائياً من المستند",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE53935),
                                        contentColor = TextWhite
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("اختيار PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Card 2: أصناف جاهزة للاختيار السريع
                    item {
                        Column {
                            Text(
                                text = "أصناف جاهزة للاختيار السريع بنقرة واحدة:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = GoldLight
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                presets.forEach { preset ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CharcoalSurface)
                                            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(12.dp))
                                            .clickable {
                                                inputItemName = preset.name
                                                inputItemCode = preset.code
                                                inputUnitPriceStr = preset.defaultPrice.toInt().toString()
                                                inputQuantityStr = "1"
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = preset.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TextWhite
                                            )
                                            Text(
                                                text = "${preset.defaultPrice.toInt()} د.ل",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = GoldPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card 3: إضافة صنف جديد (Add New Item Form)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("item_input_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(GoldPrimary.copy(alpha = 0.6f), CharcoalCardBorder)
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(GoldPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = CharcoalBackground,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "إضافة صنف جديد",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                        )
                                    }

                                    // Serial auto code badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GoldContainer)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "رقم الصنف المسلسل: ${billItems.size + 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = GoldPrimary
                                        )
                                    }
                                }

                                // اسم الصنف
                                OutlinedTextField(
                                    value = inputItemName,
                                    onValueChange = { inputItemName = it },
                                    label = { Text("اسم الصنف *") },
                                    placeholder = { Text("مثال: مطبخ تركي، باب خشب، درابزين...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = CharcoalSurfaceVariant,
                                        unfocusedContainerColor = CharcoalSurfaceVariant,
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = CharcoalCardBorder,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    )
                                )

                                // Row: كود الصنف + الكمية + السعر
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // كود الصنف
                                    OutlinedTextField(
                                        value = if (inputItemCode.isNotBlank()) inputItemCode else (billItems.size + 1).toString(),
                                        onValueChange = { inputItemCode = it },
                                        label = { Text("كود الصنف") },
                                        modifier = Modifier.weight(0.9f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = CharcoalSurfaceVariant,
                                            unfocusedContainerColor = CharcoalSurfaceVariant,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = CharcoalCardBorder,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        )
                                    )

                                    // الكمية مع أزرار الزيادة والنقصان
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text(
                                            text = "الكمية:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(CharcoalSurfaceVariant)
                                                .border(1.dp, CharcoalCardBorder, RoundedCornerShape(12.dp)),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    val current = inputQuantityStr.toDoubleOrNull() ?: 1.0
                                                    if (current > 1.0) {
                                                        inputQuantityStr = if (current % 1.0 == 0.0) {
                                                            (current - 1.0).toInt().toString()
                                                        } else {
                                                            String.format(Locale.US, "%.1f", current - 1.0)
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "تقليل", tint = GoldLight)
                                            }

                                            OutlinedTextField(
                                                value = inputQuantityStr,
                                                onValueChange = { inputQuantityStr = it },
                                                modifier = Modifier.weight(1f),
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Decimal,
                                                    imeAction = ImeAction.Next
                                                ),
                                                singleLine = true,
                                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                    textAlign = TextAlign.Center,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextWhite
                                                ),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color.Transparent,
                                                    unfocusedContainerColor = Color.Transparent,
                                                    focusedBorderColor = Color.Transparent,
                                                    unfocusedBorderColor = Color.Transparent
                                                )
                                            )

                                            IconButton(
                                                onClick = {
                                                    val current = inputQuantityStr.toDoubleOrNull() ?: 0.0
                                                    inputQuantityStr = if (current % 1.0 == 0.0) {
                                                        (current + 1.0).toInt().toString()
                                                    } else {
                                                        String.format(Locale.US, "%.1f", current + 1.0)
                                                    }
                                                },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "زيادة", tint = GoldLight)
                                            }
                                        }
                                    }

                                    // سعر الوحدة
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "السعر:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        OutlinedTextField(
                                            value = inputUnitPriceStr,
                                            onValueChange = { inputUnitPriceStr = it },
                                            placeholder = { Text("0.00", color = TextMuted) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Decimal,
                                                imeAction = ImeAction.Done
                                            ),
                                            keyboardActions = KeyboardActions(onDone = { addNewItem() }),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = CharcoalSurfaceVariant,
                                                unfocusedContainerColor = CharcoalSurfaceVariant,
                                                focusedBorderColor = GoldPrimary,
                                                unfocusedBorderColor = CharcoalCardBorder,
                                                focusedTextColor = TextWhite,
                                                unfocusedTextColor = TextWhite
                                            )
                                        )
                                    }
                                }

                                // Live calculation
                                val previewQty = inputQuantityStr.toDoubleOrNull() ?: 0.0
                                val previewPrice = inputUnitPriceStr.toDoubleOrNull() ?: 0.0
                                val previewTotal = previewQty * previewPrice

                                if (previewTotal > 0.0) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GoldContainer.copy(alpha = 0.5f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "إجمالي الصنف الحالي:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldLight
                                        )
                                        Text(
                                            text = "${if (previewTotal % 1.0 == 0.0) previewTotal.toInt() else String.format(Locale.US, "%.2f", previewTotal)} د.ل",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = GoldPrimary
                                            )
                                        )
                                    }
                                }

                                Button(
                                    onClick = { addNewItem() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("add_item_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldPrimary,
                                        contentColor = CharcoalBackground
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "إضافة الصنف إلى جدول الفاتورة",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    // Card 4: جدول أصناف الفاتورة (Invoice Items Table)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("invoice_items_table_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    listOf(GoldPrimary.copy(alpha = 0.5f), CharcoalCardBorder)
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                // Table Header bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "جدول أصناف الفاتورة",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextWhite
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(GoldContainer)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${billItems.size} أصناف",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = GoldPrimary
                                            )
                                        }
                                    }

                                    if (billItems.isNotEmpty()) {
                                        TextButton(
                                            onClick = {
                                                billItems.clear()
                                                persistDraft()
                                            },
                                            colors = ButtonDefaults.textButtonColors(contentColor = RedRemove)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تفريغ الجدول", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (billItems.isEmpty()) {
                                    // Empty state: exactly as requested ("لا توجد أصناف بعد")
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CharcoalSurfaceVariant)
                                            .padding(28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Receipt,
                                                contentDescription = null,
                                                tint = TextMuted,
                                                modifier = Modifier.size(44.dp)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "لا توجد أصناف بعد",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = TextWhite,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "قم بكتابة اسم الصنف والكمية والسعر أعلاه واضغط 'إضافة'",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    // Structured Items Table
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(10.dp))
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // Table Header row: [م] [كود] [اسم الصنف] [الكمية] [السعر] [الإجمالي] [حذف]
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(CharcoalSurfaceVariant)
                                                    .padding(horizontal = 6.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "م",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldPrimary,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(0.5f)
                                                )
                                                Text(
                                                    text = "كود",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldLight,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(0.6f)
                                                )
                                                Text(
                                                    text = "اسم الصنف",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextWhite,
                                                    textAlign = TextAlign.Start,
                                                    modifier = Modifier.weight(2.0f)
                                                )
                                                Text(
                                                    text = "الكمية",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextWhite,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(0.8f)
                                                )
                                                Text(
                                                    text = "السعر",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextWhite,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(0.9f)
                                                )
                                                Text(
                                                    text = "الإجمالي",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldPrimary,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(1.0f)
                                                )
                                                Text(
                                                    text = "حذف",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = RedRemove,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(0.6f)
                                                )
                                            }

                                            HorizontalDivider(color = CharcoalCardBorder, thickness = 1.dp)

                                            // Table Body Rows
                                            billItems.forEachIndexed { index, item ->
                                                val rowBg = if (index % 2 == 1) CharcoalSurfaceVariant.copy(alpha = 0.4f) else Color.Transparent
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(rowBg)
                                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // م
                                                    Text(
                                                        text = "${index + 1}",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GoldLight,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.weight(0.5f)
                                                    )

                                                    // كود الصنف
                                                    Text(
                                                        text = item.itemCode.ifBlank { "${index + 1}" },
                                                        fontSize = 11.sp,
                                                        color = TextMuted,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.weight(0.6f)
                                                    )

                                                    // اسم الصنف
                                                    Text(
                                                        text = item.name,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextWhite,
                                                        textAlign = TextAlign.Start,
                                                        modifier = Modifier.weight(2.0f),
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )

                                                    // الكمية
                                                    Text(
                                                        text = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString(),
                                                        fontSize = 12.sp,
                                                        color = TextWhite,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.weight(0.8f)
                                                    )

                                                    // السعر
                                                    Text(
                                                        text = if (item.unitPrice % 1.0 == 0.0) item.unitPrice.toInt().toString() else item.unitPrice.toString(),
                                                        fontSize = 12.sp,
                                                        color = TextWhite,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.weight(0.9f)
                                                    )

                                                    // الإجمالي
                                                    Text(
                                                        text = if (item.total % 1.0 == 0.0) item.total.toInt().toString() else String.format(Locale.US, "%.1f", item.total),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GoldPrimary,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.weight(1.0f)
                                                    )

                                                    // حذف
                                                    Box(
                                                        modifier = Modifier.weight(0.6f),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                val deletedName = item.name
                                                                billItems.removeAt(index)
                                                                persistDraft()
                                                                Toast.makeText(context, "تم حذف صنف: $deletedName", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(30.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "حذف الصنف",
                                                                tint = RedRemove,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                if (index < billItems.size - 1) {
                                                    HorizontalDivider(color = CharcoalCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                                                }
                                            }

                                            // Table Summary Footer
                                            HorizontalDivider(color = CharcoalCardBorder, thickness = 1.5.dp)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(CharcoalSurfaceVariant)
                                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "إجمالي الفاتورة (${billItems.size} أصناف):",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextWhite
                                                )
                                                Text(
                                                    text = "${if (totalAmount % 1.0 == 0.0) totalAmount.toInt() else String.format(Locale.US, "%.2f", totalAmount)} د.ل",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFFE53935)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card 5: بيانات السداد، المبالغ وملاحظات أسفل الفاتورة (Payment & Bottom Notes)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payment_and_bottom_notes_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(GoldPrimary.copy(alpha = 0.5f), CharcoalCardBorder)
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 1. قسم المدفوع والمتبقي
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(GoldContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Calculate,
                                                contentDescription = null,
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "المبلغ المدفوع والمتبقي",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = GoldLight
                                        )
                                    }

                                    // Payment Status Badge
                                    val statusText = when {
                                        remainingAmount <= 0.0 && totalAmount > 0.0 -> "خالصة بالكامل ✓"
                                        paidAmount > 0.0 -> "عربون مدفوع"
                                        else -> "غير مدفوعة"
                                    }
                                    val statusColor = when {
                                        remainingAmount <= 0.0 && totalAmount > 0.0 -> Color(0xFF2E7D32)
                                        paidAmount > 0.0 -> GoldPrimary
                                        else -> Color(0xFFE53935)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(statusColor.copy(alpha = 0.15f))
                                            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = statusText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = statusColor
                                        )
                                    }
                                }

                                // حقل إدخال المبلغ المدفوع
                                OutlinedTextField(
                                    value = paidAmountStr,
                                    onValueChange = {
                                        paidAmountStr = it
                                        persistDraft()
                                    },
                                    label = { Text("المبلغ المدفوع (د.ل) *") },
                                    placeholder = { Text("0.00", color = TextMuted) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = CharcoalSurfaceVariant,
                                        unfocusedContainerColor = CharcoalSurfaceVariant,
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = CharcoalCardBorder,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    )
                                )

                                // أزرار سريعة للمدفوع
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            paidAmountStr = if (totalAmount % 1.0 == 0.0) totalAmount.toInt().toString() else String.format(Locale.US, "%.2f", totalAmount)
                                            persistDraft()
                                        },
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurfaceVariant, contentColor = GoldLight),
                                        shape = RoundedCornerShape(8.dp),
                                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.horizontalGradient(listOf(GoldPrimary.copy(alpha = 0.5f), CharcoalCardBorder)))
                                    ) {
                                        Text("دفع كامل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            val half = totalAmount / 2.0
                                            paidAmountStr = if (half % 1.0 == 0.0) half.toInt().toString() else String.format(Locale.US, "%.2f", half)
                                            persistDraft()
                                        },
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurfaceVariant, contentColor = GoldLight),
                                        shape = RoundedCornerShape(8.dp),
                                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.horizontalGradient(listOf(GoldPrimary.copy(alpha = 0.5f), CharcoalCardBorder)))
                                    ) {
                                        Text("عربون 50%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            paidAmountStr = "0"
                                            persistDraft()
                                        },
                                        modifier = Modifier.weight(0.8f).height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurfaceVariant, contentColor = TextMuted),
                                        shape = RoundedCornerShape(8.dp),
                                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.horizontalGradient(listOf(CharcoalCardBorder, CharcoalCardBorder)))
                                    ) {
                                        Text("تصفير (0)", fontSize = 11.sp)
                                    }
                                }

                                // Financial Summary Strip
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CharcoalSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("الإجمالي", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Text(
                                            "${if (totalAmount % 1.0 == 0.0) totalAmount.toInt() else String.format(Locale.US, "%.2f", totalAmount)} د.ل",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = TextWhite
                                        )
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(CharcoalCardBorder))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("المدفوع", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                                        Text(
                                            "${if (paidAmount % 1.0 == 0.0) paidAmount.toInt() else String.format(Locale.US, "%.2f", paidAmount)} د.ل",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(CharcoalCardBorder))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("المتبقي", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE53935))
                                        Text(
                                            "${if (remainingAmount % 1.0 == 0.0) remainingAmount.toInt() else String.format(Locale.US, "%.2f", remainingAmount)} د.ل",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFFE53935)
                                        )
                                    }
                                }

                                // Tafqeet
                                if (totalAmount > 0.0) {
                                    Text(
                                        text = "فقط: $tafqeetText لا غير",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GoldLight,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }

                                HorizontalDivider(color = CharcoalCardBorder.copy(alpha = 0.7f), thickness = 1.dp)

                                // 2. قسم ملاحظات أسفل الفاتورة
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "ملاحظات أسفل الفاتورة (تظهر في ذيل الفاتورة من الأسفل)",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = GoldLight
                                        )
                                        Text(
                                            text = "شروط الضمان أو الاستلام والتركيب في الورقة المطبوعة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = bottomNotes,
                                    onValueChange = {
                                        bottomNotes = it
                                        persistDraft()
                                    },
                                    label = { Text("ملاحظات أسفل الفاتورة المطبوعة") },
                                    placeholder = { Text("اكتب شروط الضمان، مدة التسليم، أو أي ملاحظات ختامية...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                    maxLines = 4,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = CharcoalSurfaceVariant,
                                        unfocusedContainerColor = CharcoalSurfaceVariant,
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = CharcoalCardBorder,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite
                                    )
                                )

                                // Preset suggestions for bottom notes
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val noteSuggestions = listOf(
                                        "ضمان لمدة عام كامل" to "ملاحظات: يسري ضمان الجودة والتصنيع والتركيب لمدة عام كامل من تاريخ الاستلام.",
                                        "البضاعة لا ترد بعد التفصيل" to "ملاحظات: البضاعة التي يتم تفصيلها وتصنيعها حسب المقاسات الخاصة لا ترد ولا تستبدل.",
                                        "تسليم بالموقع" to "ملاحظات: الاستلام والتركيب في موقع العميل بعد استكمال التجهيزات.",
                                        "مسح الملاحظة" to ""
                                    )
                                    noteSuggestions.forEach { (label, fullText) ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(CharcoalSurfaceVariant)
                                                .border(1.dp, CharcoalCardBorder, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    bottomNotes = fullText
                                                    persistDraft()
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(label, fontSize = 11.sp, color = if (fullText.isEmpty()) RedRemove else GoldLight)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card 6: شريط أزرار العمليات (Actions Toolbar)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Primary Save Button
                            Button(
                                onClick = { saveInvoiceToDb(true) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("action_save_invoice_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = CharcoalBackground
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentInvoiceDbId != null) "حفظ التعديلات في السجل الدائم 💾" else "حفظ الفاتورة في السجل الدائم 💾",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            // Start New Invoice Button
                            Button(
                                onClick = { showResetDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("action_new_invoice_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CharcoalSurfaceVariant,
                                    contentColor = GoldLight
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                    brush = Brush.horizontalGradient(listOf(GoldPrimary.copy(alpha = 0.5f), CharcoalCardBorder))
                                )
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp), tint = GoldPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("➕ بدء فاتورة جديدة برقم ترتيبي (تفريغ العميل والأصناف)", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }

                            // Secondary Action Buttons Row: Print & Preview
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { executePrint() },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurfaceVariant, contentColor = GoldPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.horizontalGradient(listOf(GoldPrimary, GoldDark)))
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("طباعة الفاتورة", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        previewInvoiceEntity = null
                                        showA4PreviewDialog = true
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurfaceVariant, contentColor = GoldLight),
                                    shape = RoundedCornerShape(10.dp),
                                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.horizontalGradient(listOf(GoldPrimary, GoldDark)))
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("معاينة A4", fontWeight = FontWeight.Bold)
                                }
                            }

                            // Switch to Page 2 Button
                            OutlinedButton(
                                onClick = { selectedTab = 1 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                    brush = Brush.horizontalGradient(listOf(CharcoalCardBorder, GoldPrimary.copy(alpha = 0.5f)))
                                )
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("الانتقال إلى صفحة عرض الفواتير (${savedInvoices.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            } else {
                // =========================================================================
                // PAGE 2: صفحة عرض الفواتير (INVOICES ARCHIVE & HISTORY - IMAGE 2)
                // =========================================================================
                val filteredInvoices = remember(savedInvoices, searchQuery, selectedFilter) {
                    savedInvoices.filter { inv ->
                        val matchesSearch = if (searchQuery.isBlank()) true else {
                            val q = searchQuery.trim().lowercase(Locale.ROOT)
                            inv.invoiceNumber.lowercase(Locale.ROOT).contains(q) ||
                            inv.customerName.lowercase(Locale.ROOT).contains(q) ||
                            inv.customerPhone.lowercase(Locale.ROOT).contains(q) ||
                            inv.notes.lowercase(Locale.ROOT).contains(q) ||
                            inv.itemsJson.lowercase(Locale.ROOT).contains(q)
                        }

                        val matchesFilter = when (selectedFilter) {
                            InvoiceStatusFilter.ALL -> true
                            InvoiceStatusFilter.PAID -> inv.remainingAmount <= 0.0 && inv.totalAmount > 0.0
                            InvoiceStatusFilter.PARTIAL -> inv.paidAmount > 0.0 && inv.remainingAmount > 0.0
                            InvoiceStatusFilter.UNPAID -> inv.paidAmount <= 0.0
                        }

                        matchesSearch && matchesFilter
                    }
                }

                // Single unified LazyColumn so the entire top section moves with scrolling (الجزء العلوي متحرك مش ثابت)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("invoices_archive_list"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Item 1: Search bar and action buttons (Compact height 38dp)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sleek, compact search box (smaller in height!)
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CharcoalSurface)
                                    .border(
                                        1.dp,
                                        if (searchQuery.isNotEmpty()) GoldPrimary else CharcoalCardBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "بحث",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "بحث بالرقم، العميل، الهاتف...",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = TextWhite,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        cursorBrush = SolidColor(GoldPrimary),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("search_invoices_input")
                                    )
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "مسح البحث",
                                            tint = TextMuted,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            // "فاتورة جديدة" button (compact height)
                            Button(
                                onClick = { startNewInvoice() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = CharcoalBackground
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("archive_new_invoice_btn"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("فاتورة جديدة", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            }

                            // "استيراد PDF" button (compact height)
                            Button(
                                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFC62828),
                                    contentColor = TextWhite
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("archive_import_pdf_btn"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("استيراد PDF", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            }
                        }
                    }

                    // Item 2: Status Filter Chips
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedFilter == InvoiceStatusFilter.ALL,
                                onClick = { selectedFilter = InvoiceStatusFilter.ALL },
                                label = { Text("الكل (${savedInvoices.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = CharcoalBackground,
                                    containerColor = CharcoalSurface,
                                    labelColor = TextWhite
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedFilter == InvoiceStatusFilter.ALL,
                                    borderColor = if (selectedFilter == InvoiceStatusFilter.ALL) GoldPrimary else CharcoalCardBorder
                                )
                            )

                            FilterChip(
                                selected = selectedFilter == InvoiceStatusFilter.PAID,
                                onClick = { selectedFilter = InvoiceStatusFilter.PAID },
                                label = { Text("خالصة بالكامل ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2E7D32),
                                    selectedLabelColor = TextWhite,
                                    containerColor = CharcoalSurface,
                                    labelColor = Color(0xFF2E7D32)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedFilter == InvoiceStatusFilter.PAID,
                                    borderColor = if (selectedFilter == InvoiceStatusFilter.PAID) Color(0xFF2E7D32) else CharcoalCardBorder
                                )
                            )

                            FilterChip(
                                selected = selectedFilter == InvoiceStatusFilter.PARTIAL,
                                onClick = { selectedFilter = InvoiceStatusFilter.PARTIAL },
                                label = { Text("عربون / جزئي", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldDark,
                                    selectedLabelColor = TextWhite,
                                    containerColor = CharcoalSurface,
                                    labelColor = GoldLight
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedFilter == InvoiceStatusFilter.PARTIAL,
                                    borderColor = if (selectedFilter == InvoiceStatusFilter.PARTIAL) GoldDark else CharcoalCardBorder
                                )
                            )

                            FilterChip(
                                selected = selectedFilter == InvoiceStatusFilter.UNPAID,
                                onClick = { selectedFilter = InvoiceStatusFilter.UNPAID },
                                label = { Text("غير مدفوعة", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE53935),
                                    selectedLabelColor = TextWhite,
                                    containerColor = CharcoalSurface,
                                    labelColor = Color(0xFFE53935)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedFilter == InvoiceStatusFilter.UNPAID,
                                    borderColor = if (selectedFilter == InvoiceStatusFilter.UNPAID) Color(0xFFE53935) else CharcoalCardBorder
                                )
                            )
                        }
                    }

                    // Item 3: 4-Card Summary Metrics Grid (Row 1)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Metric 1: Count
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldPrimary.copy(alpha = 0.5f), CharcoalCardBorder)))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("إجمالي الفواتير", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("${savedInvoices.size} فاتورة", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GoldLight)
                                }
                            }

                            // Metric 2: Sales
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CharcoalCardBorder, GoldPrimary.copy(alpha = 0.5f))))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("إجمالي المبيعات", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    val sumTotal = savedInvoices.sumOf { it.totalAmount }
                                    val formattedSum = if (sumTotal % 1.0 == 0.0) sumTotal.toInt().toString() else String.format(Locale.US, "%.1f", sumTotal)
                                    Text("$formattedSum د.ل", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextWhite)
                                }
                            }
                        }
                    }

                    // Item 4: 4-Card Summary Metrics Grid (Row 2)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Metric 3: Paid
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(PaidGreen.copy(alpha = 0.4f), CharcoalCardBorder)))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("المبالغ المحصّلة (مدفوع)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    val sumPaid = savedInvoices.sumOf { it.paidAmount }
                                    val formattedPaid = if (sumPaid % 1.0 == 0.0) sumPaid.toInt().toString() else String.format(Locale.US, "%.1f", sumPaid)
                                    Text("$formattedPaid د.ل", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PaidGreen)
                                }
                            }

                            // Metric 4: Remaining / Debts
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RedRemove.copy(alpha = 0.4f), CharcoalCardBorder)))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("المبالغ المتبقية (ديون)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    val sumRemaining = savedInvoices.sumOf { it.remainingAmount }
                                    val formattedRemaining = if (sumRemaining % 1.0 == 0.0) sumRemaining.toInt().toString() else String.format(Locale.US, "%.1f", sumRemaining)
                                    Text("$formattedRemaining د.ل", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = RedRemove)
                                }
                            }
                        }
                    }

                    // Item 5: Section Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سجل الفواتير (${filteredInvoices.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldLight
                            )
                            if (searchQuery.isNotBlank() || selectedFilter != InvoiceStatusFilter.ALL) {
                                Text(
                                    text = "نتائج التصفية والبحث",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    // Item 6+: Empty State or Invoices List Cards
                    if (filteredInvoices.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
                                    Text(
                                        text = if (searchQuery.isBlank()) "لا توجد فواتير محفوظة في السجل حالياً" else "لا توجد نتائج بحث مطابقة لـ '$searchQuery'",
                                        color = TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "يمكنك إنشاء فاتورة جديدة من زر 'فاتورة جديدة' أعلاه وحفظها في السجل.",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredInvoices, key = { it.id }) { inv ->
                                val invItems = jsonToBillItems(inv.itemsJson)
                                val formattedAmount = if (inv.totalAmount % 1.0 == 0.0) inv.totalAmount.toInt().toString() else String.format(Locale.US, "%.2f", inv.totalAmount)
                                val isCurrentActive = currentInvoiceDbId == inv.id

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = if (isCurrentActive) {
                                            Brush.horizontalGradient(listOf(GoldPrimary, GoldDark))
                                        } else {
                                            Brush.horizontalGradient(listOf(CharcoalCardBorder, CharcoalSurfaceVariant))
                                        }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        // Header Row: Invoice Number, Date, Status
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "فاتورة رقم #${inv.invoiceNumber}",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = GoldLight
                                                )
                                                if (isCurrentActive) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("قيد التحرير حالياً", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            // Status Badge
                                            val isFullyPaid = inv.remainingAmount <= 0.0 && inv.totalAmount > 0.0
                                            val isPartial = inv.paidAmount > 0.0 && inv.remainingAmount > 0.0
                                            val badgeText = when {
                                                isFullyPaid -> "خالصة بالكامل ✓"
                                                isPartial -> "عربون مدفوع"
                                                else -> "غير مدفوعة"
                                            }
                                            val badgeColor = when {
                                                isFullyPaid -> Color(0xFF2E7D32)
                                                isPartial -> GoldPrimary
                                                else -> Color(0xFFE53935)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(badgeColor.copy(alpha = 0.15f))
                                                    .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = badgeText,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = badgeColor
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Date & Time
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${inv.gregorianDate.ifBlank { inv.date }}  |  ${inv.timeStr}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                            Text(
                                                text = inv.invoiceType.ifBlank { "فاتورة خدمات" },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextGold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Customer Info
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = inv.customerName.ifBlank { "عميل عام" },
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = TextWhite
                                                )
                                            }

                                            if (inv.customerPhone.isNotBlank()) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(inv.customerPhone, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Items summary preview
                                        if (invItems.isNotEmpty()) {
                                            Text(
                                                text = "${invItems.size} أصناف: " + invItems.take(3).joinToString("، ") { it.name } + if (invItems.size > 3) "..." else "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = CharcoalCardBorder.copy(alpha = 0.6f))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Financial breakdown badges
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(CharcoalSurfaceVariant.copy(alpha = 0.6f))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("الإجمالي", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                Text(
                                                    text = "$formattedAmount د.ل",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = GoldPrimary
                                                )
                                            }

                                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(CharcoalCardBorder))

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("المدفوع", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                                                Text(
                                                    text = "${if (inv.paidAmount % 1.0 == 0.0) inv.paidAmount.toInt() else String.format(Locale.US, "%.1f", inv.paidAmount)} د.ل",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFF2E7D32)
                                                )
                                            }

                                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(CharcoalCardBorder))

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("المتبقي", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE53935))
                                                Text(
                                                    text = "${if (inv.remainingAmount % 1.0 == 0.0) inv.remainingAmount.toInt() else String.format(Locale.US, "%.1f", inv.remainingAmount)} د.ل",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFFE53935)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Bottom Action Buttons for each invoice
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Open & Edit
                                            Button(
                                                onClick = { loadInvoice(inv) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalBackground),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(34.dp).weight(1.1f)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("فتح وتعديل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // A4 Preview
                                            OutlinedButton(
                                                onClick = {
                                                    previewInvoiceEntity = inv
                                                    showA4PreviewDialog = true
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.horizontalGradient(listOf(GoldPrimary, GoldDark))),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(34.dp).weight(1f)
                                            ) {
                                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("معاينة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Direct Print
                                            OutlinedButton(
                                                onClick = { executePrint(inv) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.horizontalGradient(listOf(GoldPrimary, GoldDark))),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(34.dp).weight(1f)
                                            ) {
                                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("طباعة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Share
                                            IconButton(
                                                onClick = {
                                                    val shareText = buildString {
                                                        appendLine("📋 ${inv.companyName}")
                                                        appendLine("📍 ${inv.branch} | 📞 ${inv.companyPhone}")
                                                        appendLine("--------------------------------")
                                                        appendLine("فاتورة رقم: ${inv.invoiceNumber} (${inv.invoiceType})")
                                                        appendLine("العميل: ${inv.customerName}")
                                                        appendLine("التاريخ: ${inv.gregorianDate} - ${inv.timeStr}")
                                                        appendLine("--------------------------------")
                                                        appendLine("الأصناف:")
                                                        invItems.forEachIndexed { i, item ->
                                                            appendLine("${i + 1}. [كود ${item.itemCode}] ${item.name} × ${item.quantity} = ${item.total} د.ل")
                                                        }
                                                        appendLine("--------------------------------")
                                                        appendLine("💰 الإجمالي: ${inv.totalAmount} د.ل")
                                                        appendLine("💵 المدفوع: ${inv.paidAmount} د.ل")
                                                        appendLine("🔴 المتبقي: ${inv.remainingAmount} د.ل")
                                                        if (inv.bottomNotes.isNotBlank()) {
                                                            appendLine("--------------------------------")
                                                            appendLine("📌 ${inv.bottomNotes}")
                                                        }
                                                    }
                                                    val sendIntent = Intent().apply {
                                                        action = Intent.ACTION_SEND
                                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                                        type = "text/plain"
                                                    }
                                                    context.startActivity(Intent.createChooser(sendIntent, "مشاركة الفاتورة #${inv.invoiceNumber}"))
                                                },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = GoldLight, modifier = Modifier.size(18.dp))
                                            }

                                            // Delete
                                            IconButton(
                                                onClick = { invoiceToDelete = inv },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedRemove.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

    // ==================== FULLSCREEN A4 PREVIEW DIALOG ====================
    if (showA4PreviewDialog) {
        val target = previewInvoiceEntity
        val displayCompanyName = target?.companyName ?: companyName
        val displayBranch = target?.branch ?: branch
        val displayPhone = target?.companyPhone ?: companyPhone
        val displayType = target?.invoiceType ?: invoiceType
        val displayNumber = target?.invoiceNumber ?: invoiceNumber
        val displayHijri = target?.hijriDate ?: hijriDate
        val displayGregorian = target?.gregorianDate ?: gregorianDate
        val displayTime = target?.timeStr ?: timeStr
        val displayStore = target?.storeName ?: storeName
        val displayCustomerNum = target?.customerNumber ?: customerNumber
        val displayCustomerName = target?.customerName ?: customerName
        val displayCustomerAddr = target?.customerAddress ?: customerAddress
        val displayCustomerPhone = target?.customerPhone ?: customerPhone
        val displayNotes = target?.notes ?: notes
        val displayBottomNotes = target?.bottomNotes ?: bottomNotes
        val displayPaid = target?.paidAmount ?: paidAmount
        val displayItems = if (target != null) jsonToBillItems(target.itemsJson) else billItems
        val displayTotal = target?.totalAmount ?: totalAmount
        val displayRemaining = if (target != null) {
            if (target.paidAmount > 0.0 || target.remainingAmount > 0.0) target.remainingAmount else target.totalAmount
        } else remainingAmount
        val displayTafqeet = ArabicNumberToWords.toTafqeet(displayTotal)

        Dialog(
            onDismissRequest = { showA4PreviewDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF101014))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Header Bar for Preview Dialog
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showA4PreviewDialog = false },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق المعاينة", tint = TextWhite)
                        }

                        Text(
                            text = "معاينة الفاتورة A4 طبق الأصل (#$displayNumber)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GoldPrimary
                        )

                        Button(
                            onClick = {
                                executePrint(target)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalBackground),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Scrollable A4 Paper preview
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AlfaresPrintPreviewSheet(
                            companyName = displayCompanyName,
                            branch = displayBranch,
                            companyPhone = displayPhone,
                            invoiceType = displayType,
                            invoiceNumber = displayNumber,
                            hijriDate = displayHijri,
                            gregorianDate = displayGregorian,
                            timeStr = displayTime,
                            storeName = displayStore,
                            customerNumber = displayCustomerNum,
                            customerName = displayCustomerName,
                            customerAddress = displayCustomerAddr,
                            customerPhone = displayCustomerPhone,
                            notes = displayNotes,
                            items = displayItems,
                            totalAmount = displayTotal,
                            tafqeetText = displayTafqeet,
                            remainingAmount = displayRemaining,
                            preparedBy = preparedBy,
                            paidAmount = displayPaid,
                            bottomNotes = displayBottomNotes,
                            modifier = Modifier.shadow(16.dp, RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = CharcoalSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "بدء فاتورة جديدة برقم ترتيبي؟", color = GoldLight, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "سيتم تفريغ اسم الزبون والأصناف لبدء فاتورة جديدة برقم ترتيبي (#${getNextSequentialNumber()}). يمكنك دوماً استرجاع الفواتير السابقة وتعديلها من صفحة عرض الفواتير.",
                    color = TextWhite
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        startNewInvoice()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalBackground)
                ) {
                    Text("نعم، بدء فاتورة جديدة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            }
        )
    }

    // Delete Invoice Confirmation Dialog
    if (invoiceToDelete != null) {
        val target = invoiceToDelete!!
        AlertDialog(
            onDismissRequest = { invoiceToDelete = null },
            containerColor = CharcoalSurface,
            title = {
                Text(text = "حذف الفاتورة من السجل؟", color = RedRemove, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف الفاتورة رقم #${target.invoiceNumber} للعميل ${target.customerName.ifBlank { "عام" }} نهائياً من الذاكرة؟",
                    color = TextWhite
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            database.invoiceDao().deleteInvoiceById(target.id)
                            if (currentInvoiceDbId == target.id) {
                                currentInvoiceDbId = null
                            }
                            Toast.makeText(context, "تم حذف الفاتورة من السجل بنجاح", Toast.LENGTH_SHORT).show()
                        }
                        invoiceToDelete = null
                    }
                ) {
                    Text("نعم، حذف نهائي", color = RedRemove, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToDelete = null }) {
                    Text("إلغاء", color = TextMuted)
                }
            }
        )
    }

    // ==================== PDF PROCESSING PROGRESS DIALOG ====================
    if (isProcessingPdf) {
        AlertDialog(
            onDismissRequest = { /* prevent dismiss while reading PDF */ },
            containerColor = CharcoalSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = GoldPrimary,
                        modifier = Modifier.size(26.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("قراءة ملف الـ PDF...", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "جارٍ فحص المستند واستخراج أسماء الأصناف، الكميات، والأسعار تلقائياً...",
                        color = TextWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "يرجى الانتظار بضع ثوانٍ...",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {}
        )
    }

    // ==================== PDF EXTRACTED INVOICE PREVIEW DIALOG ====================
    if (showPdfPreviewDialog && extractedPdfResult != null) {
        val res = extractedPdfResult!!
        AlertDialog(
            onDismissRequest = { showPdfPreviewDialog = false },
            containerColor = CharcoalSurface,
            modifier = Modifier.padding(6.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بيانات الفاتورة المستخرجة",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    if (res.extractionMethod.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = res.extractionMethod,
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // PDF page thumbnail preview
                    if (res.previewBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .border(1.dp, CharcoalCardBorder, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = res.previewBitmap.asImageBitmap(),
                                contentDescription = "معاينة صفحة ملف الـ PDF",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    // Extracted general metadata
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CharcoalSurfaceVariant)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (res.customerName.isNotBlank() && res.customerName != "عميل PDF") {
                            Text(
                                text = "👤 العميل المستخرج: ${res.customerName}",
                                fontSize = 12.sp,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (res.invoiceNumber.isNotBlank()) {
                            Text(
                                text = "🔢 رقم الفاتورة: #${res.invoiceNumber}",
                                fontSize = 12.sp,
                                color = GoldLight
                            )
                        }
                        if (res.date.isNotBlank()) {
                            Text(
                                text = "📅 تاريخ الفاتورة: ${res.date}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Text(
                        text = "جدول الأصناف المستخرجة (${res.items.size} أصناف):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = GoldLight
                    )

                    // Extracted Items Table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(8.dp))
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CharcoalSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("م", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldPrimary, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                            Text("اسم الصنف", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.weight(2f), textAlign = TextAlign.Start)
                            Text("الكمية", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                            Text("السعر", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                            Text("الإجمالي", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldPrimary, modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                        }

                        HorizontalDivider(color = CharcoalCardBorder)

                        res.items.forEachIndexed { idx, itm ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (idx % 2 == 1) CharcoalSurfaceVariant.copy(alpha = 0.4f) else Color.Transparent)
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${idx + 1}", fontSize = 10.sp, color = GoldLight, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                                Text(itm.name, fontSize = 11.sp, color = TextWhite, modifier = Modifier.weight(2f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (itm.quantity % 1.0 == 0.0) itm.quantity.toInt().toString() else itm.quantity.toString(), fontSize = 10.sp, color = TextWhite, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                                Text(if (itm.unitPrice % 1.0 == 0.0) itm.unitPrice.toInt().toString() else itm.unitPrice.toString(), fontSize = 10.sp, color = TextWhite, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                Text("${if (itm.total % 1.0 == 0.0) itm.total.toInt() else String.format(Locale.US, "%.1f", itm.total)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldPrimary, modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                            }
                            if (idx < res.items.size - 1) {
                                HorizontalDivider(color = CharcoalCardBorder.copy(alpha = 0.4f))
                            }
                        }

                        // Total Row
                        HorizontalDivider(color = CharcoalCardBorder)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CharcoalSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("إجمالي الأصناف:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            val totalVal = if (res.totalAmount > 0.0) res.totalAmount else res.items.sumOf { it.total }
                            Text("${if (totalVal % 1.0 == 0.0) totalVal.toInt() else String.format(Locale.US, "%.2f", totalVal)} د.ل", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                        }
                    }
                }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            applyExtractedPdf(res, replaceAll = true)
                            showPdfPreviewDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalBackground),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("بدء فاتورة جديدة بهذه الأصناف بالكامل", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            applyExtractedPdf(res, replaceAll = false)
                            showPdfPreviewDialog = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.horizontalGradient(listOf(GoldPrimary, GoldDark)))
                    ) {
                        Text("إضافة الأصناف إلى الفاتورة الحالية", fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = { showPdfPreviewDialog = false },
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    ) {
                        Text("إلغاء", color = TextMuted, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {}
        )
    }
}
