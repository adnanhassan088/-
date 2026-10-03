package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.InvoiceEntity
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.OverdueRedContainer
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.PendingOrange
import com.example.ui.theme.PendingOrangeContainer
import com.example.ui.viewmodel.InvoiceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    viewModel: InvoiceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (InvoiceEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedInvoice by viewModel.selectedInvoice.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val invoice = selectedInvoice

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(invoice?.merchantName ?: "تفاصيل الفاتورة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    if (invoice != null) {
                        IconButton(
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    val itemsListStr = invoice.parseItems().joinToString("\n") {
                                        "- ${it.name}: ${it.quantity} × ${it.unitPrice} = ${it.total} ${invoice.currency}"
                                    }
                                    val shareText = """
                                        📄 *فاتورة ${invoice.merchantName}*
                                        رقم الفاتورة: ${invoice.invoiceNumber}
                                        التاريخ: ${invoice.date}
                                        التصنيف: ${invoice.category}
                                        طريقة الدفع: ${invoice.paymentMethod}
                                        
                                        📋 *الأصناف:*
                                        $itemsListStr
                                        
                                        المجموع الفرعي: ${invoice.subtotal} ${invoice.currency}
                                        ضريبة القيمة المضافة: ${invoice.taxAmount} ${invoice.currency}
                                        *الإجمالي النهائي:* ${invoice.totalAmount} ${invoice.currency}
                                        
                                        تمت المعالجة عبر تطبيق فاتورة الذكية
                                    """.trimIndent()
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "مشاركة تفاصيل الفاتورة")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.testTag("share_invoice_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(
                            onClick = { onNavigateToEdit(invoice) },
                            modifier = Modifier.testTag("edit_invoice_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("delete_invoice_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (invoice == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("لم يتم العثور على الفاتورة")
            }
        } else {
            val items = invoice.parseItems()

            val statusColor = when (invoice.status) {
                "PAID" -> PaidGreen
                "PENDING" -> PendingOrange
                else -> OverdueRed
            }
            val statusBg = when (invoice.status) {
                "PAID" -> PaidGreenContainer
                "PENDING" -> PendingOrangeContainer
                else -> OverdueRedContainer
            }
            val statusLabel = when (invoice.status) {
                "PAID" -> "مدفوعة بالكامل"
                "PENDING" -> "معلقة / غير مسددة"
                else -> "متأخرة"
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Receipt Header Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("receipt_detail_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalMall,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = invoice.merchantName,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "رقم الفاتورة: ${invoice.invoiceNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Status badge with toggle click
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(statusBg)
                                    .clickable { viewModel.toggleInvoiceStatus(invoice) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val icon = when (invoice.status) {
                                        "PAID" -> Icons.Default.CheckCircle
                                        "PENDING" -> Icons.Default.HourglassTop
                                        else -> Icons.Default.Warning
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$statusLabel (اضغط للتغيير)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = statusColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Grand Total Display
                            Text(
                                text = "المبلغ الإجمالي",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.2f %s", invoice.totalAmount, invoice.currency),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                // Info Meta Card (Date, Category, Payment Method)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("التاريخ:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(invoice.date, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("التصنيف:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(invoice.category, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("طريقة الدفع:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(invoice.paymentMethod, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Line Items Table
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "تفاصيل الأصناف والخدمات (${items.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (items.isEmpty()) {
                                Text(
                                    text = "تم تسجيل الفاتورة بالمبلغ الإجمالي دون تفصيل فردي للأصناف.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.name, fontWeight = FontWeight.Medium)
                                                Text(
                                                    text = "${item.quantity} × ${item.unitPrice} ${invoice.currency}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = String.format(Locale.US, "%.2f %s", item.total, invoice.currency),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Financial breakdown
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("المجموع الفرعي:", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        String.format(Locale.US, "%.2f %s", invoice.subtotal, invoice.currency),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("ضريبة القيمة المضافة (VAT):", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        String.format(Locale.US, "%.2f %s", invoice.taxAmount, invoice.currency),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (invoice.discountAmount > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("الخصم:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                                        Text(
                                            String.format(Locale.US, "-%.2f %s", invoice.discountAmount, invoice.currency),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("الإجمالي الكلي:", fontWeight = FontWeight.Bold)
                                    Text(
                                        String.format(Locale.US, "%.2f %s", invoice.totalAmount, invoice.currency),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes Card (if present)
                if (invoice.notes.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ملاحظات الفاتورة",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = invoice.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Scanned Image (if present)
                if (invoice.imageUri != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            AsyncImage(
                                model = invoice.imageUri,
                                contentDescription = "الصورة الأصلية للفاتورة",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && invoice != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف الفاتورة") },
            text = { Text("هل أنت متأكد من رغبتك في حذف فاتورة ${invoice.merchantName} نهائياً؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInvoice(invoice) {
                            showDeleteDialog = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
