package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.ui.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: InvoiceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val defaultCurrency by viewModel.defaultCurrency.collectAsStateWithLifecycle()
    val defaultVat by viewModel.defaultVatPercent.collectAsStateWithLifecycle()

    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    var keySavedNotice by remember { mutableStateOf(false) }
    var samplePopulatedNotice by remember { mutableStateOf(false) }

    val currencies = listOf("ر.س", "ج.م", "د.إ", "$", "د.ك", "د.ب", "ر.ع", "€")
    val vatPresets = listOf(15.0 to "السعودية (15%)", 14.0 to "مصر (14%)", 5.0 to "الإمارات (5%)", 0.0 to "معفى (0%)")

    val hasSystemKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("إعدادات التطبيق") },
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
            // Gemini API Key Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مفتاح Gemini API للذكاء الاصطناعي",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = if (hasSystemKey) {
                                "✓ مفتاح Gemini مدمج ومفعل في بيئة العمل تلقائياً."
                            } else {
                                "يمكنك إدخال مفتاح Google Gemini API لاستخدام تحليل الصور أو تعديله:"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = {
                                apiKeyInput = it
                                keySavedNotice = false
                            },
                            label = { Text("مفتاح API الخاص بك (اختياري)") },
                            placeholder = { Text("AIzaSy...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("api_key_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (keySavedNotice) {
                                Text(
                                    text = "تم حفظ المفتاح بنجاح ✓",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Button(
                                onClick = {
                                    viewModel.setCustomApiKey(apiKeyInput)
                                    keySavedNotice = true
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حفظ المفتاح")
                            }
                        }
                    }
                }
            }

            // Currency & VAT Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "العملة الافتراضية",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            currencies.take(4).forEach { cur ->
                                val isSelected = defaultCurrency == cur
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setDefaultCurrency(cur) },
                                    label = { Text(cur) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            currencies.drop(4).forEach { cur ->
                                val isSelected = defaultCurrency == cur
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setDefaultCurrency(cur) },
                                    label = { Text(cur) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Percent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "نسبة الضريبة الافتراضية (VAT)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            vatPresets.forEach { (rate, label) ->
                                val isSelected = defaultVat == rate
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setDefaultVat(rate) },
                                    label = { Text(label) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Demo Data Generator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "فواتير تجريبية",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "يمكنك إضافة المزيد من نماذج الفواتير لتجربة الرسوم البيانية وفلاتر البحث.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = {
                                viewModel.populateMoreSampleInvoices()
                                samplePopulatedNotice = true
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إضافة فواتير تجريبية جديدة")
                        }

                        if (samplePopulatedNotice) {
                            Text(
                                text = "تمت إضافة النماذج التجريبية بنجاح ✓",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // About Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حول تطبيق فاتورة الذكية",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تطبيق متطور لإدارة وحفظ وتحليل الفواتير الذكية، يتيح استخراج البنود والمبالغ والضرائب تلقائياً باستخدام رؤية الذكاء الاصطناعي متعدد الوسائط.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
