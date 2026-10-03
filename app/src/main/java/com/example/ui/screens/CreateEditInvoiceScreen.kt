package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InvoiceCategory
import com.example.data.model.InvoiceEntity
import com.example.ui.viewmodel.InvoiceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditInvoiceScreen(
    viewModel: InvoiceViewModel,
    editingInvoiceId: Long? = null,
    onNavigateBack: () -> Unit,
    onSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val form by viewModel.currentEditForm.collectAsStateWithLifecycle()
    var showAddItemDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        InvoiceCategory.GROCERY.titleArabic,
        InvoiceCategory.RESTAURANT.titleArabic,
        InvoiceCategory.ELECTRONICS.titleArabic,
        InvoiceCategory.UTILITIES.titleArabic,
        InvoiceCategory.HEALTHCARE.titleArabic,
        InvoiceCategory.TRANSPORT.titleArabic,
        InvoiceCategory.OTHER.titleArabic
    )

    val paymentMethods = listOf("نقدي", "بطاقة مدى", "فيزا / ماستركارد", "تحويل بنكي", "Apple Pay")
    val taxRates = listOf(15.0, 14.0, 5.0, 0.0)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (editingInvoiceId != null) "تعديل الفاتورة" else "إنشاء فاتورة جديدة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "المعلومات الأساسية",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        OutlinedTextField(
                            value = form.merchantName,
                            onValueChange = { viewModel.updateEditForm { f -> f.copy(merchantName = it) } },
                            label = { Text("اسم المتجر أو العميل *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_merchant_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = form.invoiceNumber,
                                onValueChange = { viewModel.updateEditForm { f -> f.copy(invoiceNumber = it) } },
                                label = { Text("رقم الفاتورة") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_invoice_number_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = form.date,
                                onValueChange = { viewModel.updateEditForm { f -> f.copy(date = it) } },
                                label = { Text("التاريخ") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_date_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // Category Chips
                        Column {
                            Text(
                                text = "تصنيف الفاتورة:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categories.forEach { cat ->
                                    val isSelected = form.category == cat
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateEditForm { f -> f.copy(category = cat) } },
                                        label = { Text(cat) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }

                        // Payment Method Chips
                        Column {
                            Text(
                                text = "طريقة الدفع:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                paymentMethods.forEach { method ->
                                    val isSelected = form.paymentMethod == method
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateEditForm { f -> f.copy(paymentMethod = method) } },
                                        label = { Text(method) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Line Items Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "بنود وأصناف الفاتورة (${form.items.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            TextButton(
                                onClick = { showAddItemDialog = true },
                                modifier = Modifier.testTag("form_add_item_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة صنف")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (form.items.isEmpty()) {
                            Text(
                                text = "لم تتم إضافة أصناف بعد. يمكنك إدخال المجموع مباشرة أو إضافة الأصناف لحسابها تلقائياً.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                form.items.forEachIndexed { index, item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "${item.quantity} × ${item.unitPrice} = ${item.total} ${form.currency}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { viewModel.removeLineItem(index) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "حذف",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Totals & Tax Calculation Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "الحسابات والضرائب",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        // Tax rate presets
                        Column {
                            Text(
                                text = "نسبة ضريبة القيمة المضافة (VAT):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                taxRates.forEach { rate ->
                                    val isSelected = form.taxRatePercent == rate
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.updateEditForm { f ->
                                                val newTax = if (rate > 0) (f.subtotal * (rate / 100.0)) else 0.0
                                                f.copy(taxRatePercent = rate, taxAmount = newTax)
                                            }
                                        },
                                        label = { Text("${rate.toInt()}%") },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = if (form.subtotal > 0) form.subtotal.toString() else "",
                                onValueChange = {
                                    val sub = it.toDoubleOrNull() ?: 0.0
                                    viewModel.updateEditForm { f -> f.copy(subtotal = sub) }
                                },
                                label = { Text("المجموع الفرعي") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = if (form.taxAmount > 0) form.taxAmount.toString() else "",
                                onValueChange = {
                                    val tax = it.toDoubleOrNull() ?: 0.0
                                    viewModel.updateEditForm { f -> f.copy(taxAmount = tax) }
                                },
                                label = { Text("مبلغ الضريبة") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = if (form.discountAmount > 0) form.discountAmount.toString() else "",
                            onValueChange = {
                                val disc = it.toDoubleOrNull() ?: 0.0
                                viewModel.updateEditForm { f -> f.copy(discountAmount = disc) }
                            },
                            label = { Text("الخصم إن وجد") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Final Total Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المبلغ الإجمالي النهائي:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = String.format(Locale.US, "%.2f %s", form.totalAmount, form.currency),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            // Notes Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ملاحظات إضافية",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = form.notes,
                            onValueChange = { viewModel.updateEditForm { f -> f.copy(notes = it) } },
                            placeholder = { Text("أي تفاصيل إضافية عن الفاتورة...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3
                        )
                    }
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = {
                        viewModel.saveCurrentForm(editingId = editingInvoiceId) { savedId ->
                            onSaved(savedId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_invoice_form_button"),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (editingInvoiceId != null) "تحديث الفاتورة" else "حفظ الفاتورة",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    // Add item modal dialog
    if (showAddItemDialog) {
        var name by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("1") }
        var price by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("إضافة صنف جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الصنف") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qty,
                            onValueChange = { qty = it },
                            label = { Text("الكمية") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("السعر") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val q = qty.toDoubleOrNull() ?: 1.0
                        val p = price.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank()) {
                            viewModel.addLineItem(name, q, p)
                            showAddItemDialog = false
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
