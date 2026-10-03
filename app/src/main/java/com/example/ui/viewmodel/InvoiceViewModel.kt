package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.InvoiceCategory
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceLineItem
import com.example.data.model.SampleInvoicePreset
import com.example.data.model.SampleInvoicesProvider
import com.example.data.remote.GeminiInvoiceAnalyzer
import com.example.data.remote.InvoiceAnalysisResult
import com.example.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface ScanUiState {
    object Idle : ScanUiState
    data class Analyzing(val stepMessage: String) : ScanUiState
    data class Extracted(val result: InvoiceAnalysisResult, val imageBitmap: Bitmap? = null) : ScanUiState
    data class Error(val message: String, val canFallback: Boolean = true) : ScanUiState
}

data class EditableInvoiceForm(
    val merchantName: String = "",
    val invoiceNumber: String = "",
    val date: String = "",
    val category: String = "عام",
    val currency: String = "ر.س",
    val items: List<InvoiceLineItem> = emptyList(),
    val subtotal: Double = 0.0,
    val taxRatePercent: Double = 15.0,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "نقدي",
    val status: String = "PAID",
    val notes: String = "",
    val imageUri: String? = null
) {
    fun recalculate(): EditableInvoiceForm {
        val calculatedSubtotal = if (items.isNotEmpty()) items.sumOf { it.total } else subtotal
        val calculatedTax = if (taxRatePercent > 0 && subtotal > 0 && taxAmount == 0.0) {
            calculatedSubtotal * (taxRatePercent / 100.0)
        } else {
            taxAmount
        }
        val calculatedTotal = (calculatedSubtotal + calculatedTax - discountAmount).coerceAtLeast(0.0)
        return copy(
            subtotal = (calculatedSubtotal * 100).toLong() / 100.0,
            taxAmount = (calculatedTax * 100).toLong() / 100.0,
            totalAmount = (calculatedTotal * 100).toLong() / 100.0
        )
    }

    fun toEntity(id: Long = 0): InvoiceEntity {
        return InvoiceEntity(
            id = id,
            merchantName = merchantName.ifBlank { "فاتورة بدون اسم" },
            invoiceNumber = invoiceNumber.ifBlank { "INV-${System.currentTimeMillis() % 100000}" },
            date = date.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) },
            category = category,
            currency = currency,
            subtotal = subtotal,
            taxAmount = taxAmount,
            discountAmount = discountAmount,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            status = status,
            notes = notes,
            imageUri = imageUri,
            itemsJson = InvoiceEntity.itemsToJson(items),
            createdAt = System.currentTimeMillis()
        )
    }
}

class InvoiceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: InvoiceRepository
    private val prefs = application.getSharedPreferences("smart_invoice_prefs", Context.MODE_PRIVATE)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _currentEditForm = MutableStateFlow(EditableInvoiceForm())
    val currentEditForm: StateFlow<EditableInvoiceForm> = _currentEditForm.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_gemini_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _defaultCurrency = MutableStateFlow(prefs.getString("default_currency", "ر.س") ?: "ر.س")
    val defaultCurrency: StateFlow<String> = _defaultCurrency.asStateFlow()

    private val _defaultVatPercent = MutableStateFlow(prefs.getFloat("default_vat", 15.0f).toDouble())
    val defaultVatPercent: StateFlow<Double> = _defaultVatPercent.asStateFlow()

    private val _selectedInvoice = MutableStateFlow<InvoiceEntity?>(null)
    val selectedInvoice: StateFlow<InvoiceEntity?> = _selectedInvoice.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = InvoiceRepository(database.invoiceDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val invoices: StateFlow<List<InvoiceEntity>> = combine(
        repository.allInvoices,
        _searchQuery,
        _selectedCategory
    ) { all, query, category ->
        var list = all
        if (!query.isBlank()) {
            list = list.filter {
                it.merchantName.contains(query, ignoreCase = true) ||
                it.invoiceNumber.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
        if (category != null) {
            list = list.filter { it.category == category }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun selectInvoice(invoice: InvoiceEntity?) {
        _selectedInvoice.value = invoice
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key.trim()
        prefs.edit().putString("custom_gemini_api_key", key.trim()).apply()
    }

    fun setDefaultCurrency(currency: String) {
        _defaultCurrency.value = currency
        prefs.edit().putString("default_currency", currency).apply()
    }

    fun setDefaultVat(vat: Double) {
        _defaultVatPercent.value = vat
        prefs.edit().putFloat("default_vat", vat.toFloat()).apply()
    }

    fun resetScanState() {
        _scanState.value = ScanUiState.Idle
    }

    fun analyzeImageUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _scanState.value = ScanUiState.Analyzing("جاري قراءة وتجهيز الصورة...")
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap == null) {
                    _scanState.value = ScanUiState.Error("تعذر قراءة ملف الصورة المحدد")
                    return@launch
                }

                analyzeBitmap(bitmap, imageUriString = uri.toString())
            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error("خطأ في قراءة الصورة: ${e.message}")
            }
        }
    }

    fun analyzeBitmap(bitmap: Bitmap, imageUriString: String? = null) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Analyzing("جاري فحص الفاتورة بواسطة ذكاء Gemini الاصطناعي...")
            val result = GeminiInvoiceAnalyzer.analyzeInvoiceImage(
                bitmap = bitmap,
                customApiKey = _customApiKey.value
            )

            if (result.success) {
                _scanState.value = ScanUiState.Extracted(result, bitmap)
                setupFormFromAnalysis(result, imageUriString)
            } else {
                _scanState.value = ScanUiState.Error(
                    message = result.errorMessage ?: "حدث خطأ غير متوقع أثناء تحليل الفاتورة"
                )
            }
        }
    }

    fun applyPreset(preset: SampleInvoicePreset) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val form = EditableInvoiceForm(
            merchantName = preset.merchantName,
            invoiceNumber = preset.invoiceNumber,
            date = today,
            category = preset.category,
            currency = preset.currency,
            items = preset.items,
            subtotal = preset.subtotal,
            taxAmount = preset.taxAmount,
            discountAmount = 0.0,
            totalAmount = preset.totalAmount,
            paymentMethod = preset.paymentMethod,
            status = "PAID",
            notes = preset.notes
        )
        _currentEditForm.value = form.recalculate()
        _scanState.value = ScanUiState.Extracted(
            result = InvoiceAnalysisResult(
                success = true,
                merchantName = preset.merchantName,
                invoiceNumber = preset.invoiceNumber,
                date = today,
                category = preset.category,
                currency = preset.currency,
                subtotal = preset.subtotal,
                taxAmount = preset.taxAmount,
                discountAmount = 0.0,
                totalAmount = preset.totalAmount,
                paymentMethod = preset.paymentMethod,
                notes = preset.notes,
                items = preset.items
            )
        )
    }

    private fun setupFormFromAnalysis(result: InvoiceAnalysisResult, imageUri: String?) {
        val form = EditableInvoiceForm(
            merchantName = result.merchantName,
            invoiceNumber = result.invoiceNumber,
            date = result.date.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) },
            category = result.category,
            currency = result.currency.ifBlank { _defaultCurrency.value },
            items = result.items,
            subtotal = result.subtotal,
            taxRatePercent = _defaultVatPercent.value,
            taxAmount = result.taxAmount,
            discountAmount = result.discountAmount,
            totalAmount = result.totalAmount,
            paymentMethod = result.paymentMethod,
            status = "PAID",
            notes = result.notes,
            imageUri = imageUri
        )
        _currentEditForm.value = form.recalculate()
    }

    fun updateEditForm(transform: (EditableInvoiceForm) -> EditableInvoiceForm) {
        _currentEditForm.value = transform(_currentEditForm.value).recalculate()
    }

    fun addLineItem(name: String, quantity: Double, unitPrice: Double) {
        val item = InvoiceLineItem(name, quantity, unitPrice, quantity * unitPrice)
        updateEditForm { it.copy(items = it.items + item) }
    }

    fun removeLineItem(index: Int) {
        updateEditForm {
            val list = it.items.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            it.copy(items = list)
        }
    }

    fun initNewManualInvoice() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        _currentEditForm.value = EditableInvoiceForm(
            merchantName = "",
            invoiceNumber = "INV-${System.currentTimeMillis() % 100000}",
            date = today,
            category = InvoiceCategory.OTHER.titleArabic,
            currency = _defaultCurrency.value,
            taxRatePercent = _defaultVatPercent.value,
            paymentMethod = "نقدي",
            status = "PAID",
            items = emptyList()
        ).recalculate()
    }

    fun initEditFromInvoice(invoice: InvoiceEntity) {
        _currentEditForm.value = EditableInvoiceForm(
            merchantName = invoice.merchantName,
            invoiceNumber = invoice.invoiceNumber,
            date = invoice.date,
            category = invoice.category,
            currency = invoice.currency,
            items = invoice.parseItems(),
            subtotal = invoice.subtotal,
            taxRatePercent = if (invoice.subtotal > 0 && invoice.taxAmount > 0) (invoice.taxAmount / invoice.subtotal) * 100 else _defaultVatPercent.value,
            taxAmount = invoice.taxAmount,
            discountAmount = invoice.discountAmount,
            totalAmount = invoice.totalAmount,
            paymentMethod = invoice.paymentMethod,
            status = invoice.status,
            notes = invoice.notes,
            imageUri = invoice.imageUri
        ).recalculate()
    }

    fun saveCurrentForm(editingId: Long? = null, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val form = _currentEditForm.value.recalculate()
            val entity = form.toEntity(editingId ?: 0)
            val id = if (editingId != null && editingId > 0) {
                repository.updateInvoice(entity)
                editingId
            } else {
                repository.insertInvoice(entity)
            }
            _selectedInvoice.value = entity.copy(id = id)
            _scanState.value = ScanUiState.Idle
            onSaved(id)
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
            if (_selectedInvoice.value?.id == invoice.id) {
                _selectedInvoice.value = null
            }
            onDeleted()
        }
    }

    fun toggleInvoiceStatus(invoice: InvoiceEntity) {
        viewModelScope.launch {
            val nextStatus = when (invoice.status) {
                "PAID" -> "PENDING"
                "PENDING" -> "OVERDUE"
                else -> "PAID"
            }
            val updated = invoice.copy(status = nextStatus)
            repository.updateInvoice(updated)
            if (_selectedInvoice.value?.id == invoice.id) {
                _selectedInvoice.value = updated
            }
        }
    }

    fun populateMoreSampleInvoices() {
        viewModelScope.launch {
            val presets = SampleInvoicesProvider.presets
            val entities = presets.mapIndexed { index, preset ->
                SampleInvoicesProvider.toEntity(
                    preset,
                    dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                        Date(System.currentTimeMillis() - (index * 86400000L))
                    )
                )
            }
            for (e in entities) {
                repository.insertInvoice(e)
            }
        }
    }
}
