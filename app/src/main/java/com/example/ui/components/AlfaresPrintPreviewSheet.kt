package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.screens.BillItem
import java.util.Locale

/**
 * Replicates the exact paper invoice layout from the user's uploaded image.
 */
@Composable
fun AlfaresPrintPreviewSheet(
    companyName: String = "شركة الفارس للمطابخ التركية",
    branch: String = "الطريق الساحلي الخروبة",
    companyPhone: String = "0912127526",
    invoiceType: String = "فاتورة خدمات",
    invoiceNumber: String = "12",
    hijriDate: String = "1448/03/21",
    gregorianDate: String = "03/09/2026",
    timeStr: String = "01:08:54 م",
    storeName: String = "",
    customerNumber: String = "12",
    customerName: String = "الأخ غنيمة",
    customerAddress: String = "",
    customerPhone: String = "",
    notes: String = "",
    items: List<BillItem>,
    totalAmount: Double,
    tafqeetText: String,
    remainingAmount: Double,
    preparedBy: String = "المستخدم الرئيسي",
    paidAmount: Double = 0.0,
    bottomNotes: String = "",
    modifier: Modifier = Modifier
) {
    // A4 Paper representation: clean white surface with black crisp borders and exact text formatting
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // ==================== 1. HEADER SECTION ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Right Column (Company Info & Store)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = companyName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = branch,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = companyPhone,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = gregorianDate,
                        fontSize = 10.sp,
                        color = Color.Black,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = Color.Black,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "المخزن / $storeName",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.End
                    )
                }

                // Center Column (Title & Invoice No)
                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = invoiceType,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = invoiceNumber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                // Left Column (Logo & Dates)
                Column(
                    modifier = Modifier.weight(0.9f),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Company Logo
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFC5A059), RoundedCornerShape(6.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.alfares_logo),
                            contentDescription = "شعار الفارس",
                            modifier = Modifier.size(56.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("التاريخ", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Text(hijriDate, fontSize = 10.sp, color = Color.Black)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الموافق", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Text(gregorianDate, fontSize = 10.sp, color = Color.Black)
                    }
                    Text(timeStr, fontSize = 10.sp, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==================== 2. CUSTOMER INFO BOX ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color.Black, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side: Address & Phone
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("العنوان:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(customerAddress.ifBlank { "---" }, fontSize = 11.sp, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("الهاتف:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(customerPhone.ifBlank { "---" }, fontSize = 11.sp, color = Color.Black)
                        }
                    }

                    // Right Side: Customer Number & Name & Notes
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = customerName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = customerNumber.ifBlank { invoiceNumber },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = notes.ifBlank { "لا توجد" },
                                fontSize = 10.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ملاحظات", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ==================== 3. TABLE OF ITEMS ====================
            // Container with border for the table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color.Black)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEBEBEB))
                            .border(0.5.dp, Color.Black)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الاجمالي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1.3f)
                        )
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.Black))
                        Text(
                            text = "السعر",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1.1f)
                        )
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.Black))
                        Text(
                            text = "الكميه",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.Black))
                        Text(
                            text = "اسم الصـــنــف",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(3.5f)
                        )
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.Black))
                        Text(
                            text = "رقم الصنف",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1.2f)
                        )
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.Black))
                        Text(
                            text = "م",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.6f)
                        )
                    }

                    // Table Rows
                    items.forEachIndexed { index, item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color.Black)
                                .background(if (index % 2 == 1) Color(0xFFFBFBFB) else Color.White)
                                .padding(vertical = 5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // الاجمالي
                                Text(
                                    text = if (item.total % 1.0 == 0.0) item.total.toInt().toString() else String.format(Locale.US, "%.1f", item.total),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1.3f)
                                )
                                Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.Black))

                                // السعر
                                Text(
                                    text = if (item.unitPrice % 1.0 == 0.0) item.unitPrice.toInt().toString() else String.format(Locale.US, "%.1f", item.unitPrice),
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1.1f)
                                )
                                Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.Black))

                                // الكميه
                                Text(
                                    text = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else String.format(Locale.US, "%.1f", item.quantity),
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.Black))

                                // اسم الصنف
                                Text(
                                    text = item.name,
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier
                                        .weight(3.5f)
                                        .padding(horizontal = 4.dp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.Black))

                                // رقم الصنف
                                Text(
                                    text = item.itemCode.ifBlank { (index + 1).toString() },
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1.2f)
                                )
                                Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.Black))

                                // م
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // ==================== 3.5. BOTTOM NOTES SECTION ====================
            if (bottomNotes.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color.Black)
                        .background(Color(0xFFFAFAFA))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "ملاحظات / شروط:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = bottomNotes,
                            fontSize = 11.sp,
                            color = Color(0xFF222222),
                            lineHeight = 15.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            // ==================== 4. FOOTER / TOTALS SECTION ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color.Black, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Totals Breakdown (Total, Paid, Remaining)
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "الإجمالي:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (totalAmount % 1.0 == 0.0) totalAmount.toInt().toString() else String.format(Locale.US, "%.2f", totalAmount)} د.ل",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFD32F2F)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "المدفوع:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (paidAmount % 1.0 == 0.0) paidAmount.toInt().toString() else String.format(Locale.US, "%.2f", paidAmount)} د.ل",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "المتبقي:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (remainingAmount % 1.0 == 0.0) remainingAmount.toInt().toString() else String.format(Locale.US, "%.2f", remainingAmount)} د.ل",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }

                    // Center: Remaining amount in red
                    Text(
                        text = if (remainingAmount > 0.0) {
                            "باقي عليكم ${String.format(Locale.US, "%.3f", remainingAmount)}-"
                        } else {
                            "خالصة ومسددة بالكامل ✓"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingAmount > 0.0) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    )

                    // Right: Tafqeet & Prepared By & Signature
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tafqeetText,
                                fontSize = 10.sp,
                                color = Color.Black,
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الاجمالي", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = preparedBy,
                                fontSize = 10.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الاعداد", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "-------------------------",
                                fontSize = 10.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("التوقيع", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
