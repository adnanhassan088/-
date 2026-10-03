package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.R
import com.example.ui.screens.BillItem
import java.io.ByteArrayOutputStream
import java.util.Locale

object InvoiceHtmlPrinter {

    /**
     * Converts the company logo drawable into a Base64 data string to embed seamlessly in printed HTML
     */
    fun getLogoBase64(context: Context): String {
        return try {
            val inputStream = context.resources.openRawResource(R.drawable.alfares_logo)
            val bytes = inputStream.readBytes()
            inputStream.close()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            try {
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.alfares_logo)
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                } else ""
            } catch (e2: Exception) {
                ""
            }
        }
    }

    fun generateHtml(
        companyName: String,
        branch: String,
        phone: String,
        invoiceType: String,
        invoiceNumber: String,
        hijriDate: String,
        gregorianDate: String,
        timeStr: String,
        storeName: String,
        customerNumber: String,
        customerName: String,
        customerAddress: String,
        customerPhone: String,
        notes: String,
        items: List<BillItem>,
        totalAmount: Double,
        tafqeetText: String,
        remainingAmount: Double,
        preparedBy: String,
        paidAmount: Double = 0.0,
        bottomNotes: String = "",
        logoBase64: String = ""
    ): String {
        val rowsHtml = StringBuilder()
        items.forEachIndexed { index, item ->
            rowsHtml.append(
                """
                <tr>
                    <td style="text-align: center; width: 40px;">${index + 1}</td>
                    <td style="text-align: center; width: 70px;">${item.itemCode.ifBlank { (index + 1).toString() }}</td>
                    <td style="text-align: right; padding-right: 8px;">${item.name}</td>
                    <td style="text-align: center; width: 65px;">${if (item.quantity % 1.0 == 0.0) item.quantity.toInt() else item.quantity}</td>
                    <td style="text-align: center; width: 80px;">${if (item.unitPrice % 1.0 == 0.0) item.unitPrice.toInt() else String.format(Locale.US, "%.2f", item.unitPrice)}</td>
                    <td style="text-align: center; width: 90px; font-weight: bold;">${if (item.total % 1.0 == 0.0) item.total.toInt() else String.format(Locale.US, "%.2f", item.total)}</td>
                </tr>
                """.trimIndent()
            )
        }

        val formattedTotal = if (totalAmount % 1.0 == 0.0) totalAmount.toInt().toString() else String.format(Locale.US, "%.2f", totalAmount)
        val formattedPaid = if (paidAmount % 1.0 == 0.0) paidAmount.toInt().toString() else String.format(Locale.US, "%.2f", paidAmount)
        val formattedRemaining = if (remainingAmount % 1.0 == 0.0) remainingAmount.toInt().toString() else String.format(Locale.US, "%.2f", remainingAmount)

        return """
        <!DOCTYPE html>
        <html dir="rtl" lang="ar">
        <head>
            <meta charset="utf-8">
            <style>
                body {
                    font-family: 'Tahoma', 'Arial', sans-serif;
                    margin: 0;
                    padding: 20px;
                    color: #000;
                    background-color: #fff;
                    font-size: 13px;
                }
                .header {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;
                    margin-bottom: 12px;
                }
                .header-right {
                    text-align: right;
                    width: 35%;
                }
                .header-center {
                    text-align: center;
                    width: 30%;
                }
                .header-left {
                    text-align: left;
                    width: 35%;
                    display: flex;
                    flex-direction: column;
                    align-items: flex-end;
                }
                .company-title {
                    font-size: 17px;
                    font-weight: bold;
                    margin-bottom: 4px;
                }
                .company-sub {
                    font-size: 13px;
                    font-weight: bold;
                    margin-bottom: 3px;
                }
                .invoice-type {
                    font-size: 18px;
                    font-weight: bold;
                    margin-top: 15px;
                    margin-bottom: 8px;
                }
                .invoice-num {
                    font-size: 17px;
                    font-weight: bold;
                }
                .customer-box {
                    border: 1.5px solid #000;
                    border-radius: 8px;
                    padding: 8px 12px;
                    margin-bottom: 12px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    font-size: 13px;
                }
                .customer-right {
                    text-align: right;
                    font-weight: bold;
                }
                .customer-left {
                    text-align: left;
                    font-weight: bold;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 0px;
                }
                th, td {
                    border: 1px solid #000;
                    padding: 6px 4px;
                    font-size: 12px;
                }
                th {
                    background-color: #f2f2f2;
                    font-weight: bold;
                    text-align: center;
                }
                .bottom-notes-box {
                    border: 1.5px solid #000;
                    border-top: none;
                    padding: 7px 12px;
                    background-color: #fafafa;
                    font-size: 12px;
                    display: flex;
                    align-items: flex-start;
                }
                .footer-box {
                    border: 1.5px solid #000;
                    border-top: none;
                    border-radius: 0 0 8px 8px;
                    padding: 10px 12px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .footer-right {
                    text-align: right;
                    font-size: 12px;
                    line-height: 1.7;
                    width: 40%;
                }
                .footer-center {
                    text-align: center;
                    font-weight: bold;
                    font-size: 14px;
                    width: 25%;
                }
                .footer-left {
                    text-align: left;
                    width: 35%;
                    display: flex;
                    flex-direction: column;
                    align-items: flex-end;
                }
                .total-number-red {
                    color: #d32f2f;
                    font-weight: 900;
                    font-size: 17px;
                }
                .paid-number-green {
                    color: #2e7d32;
                    font-weight: bold;
                    font-size: 15px;
                }
                .remaining-number {
                    color: #d32f2f;
                    font-weight: bold;
                    font-size: 15px;
                }
            </style>
        </head>
        <body>
            <div class="header">
                <div class="header-right">
                    <div class="company-title">$companyName</div>
                    <div class="company-sub">$branch</div>
                    <div style="font-weight: bold; font-size: 14px; margin-top: 4px;">$phone</div>
                    <div style="margin-top: 6px; font-size: 11px;">$gregorianDate</div>
                    <div style="font-size: 11px;">$timeStr</div>
                    <div style="margin-top: 6px; font-weight: bold;">المخزن/ $storeName</div>
                </div>

                <div class="header-center">
                    <div class="invoice-type">$invoiceType</div>
                    <div class="invoice-num">$invoiceNumber</div>
                </div>

                <div class="header-left">
                    ${if (logoBase64.isNotBlank()) """
                    <div style="margin-bottom: 6px; text-align: left;">
                        <img src="data:image/jpeg;base64,$logoBase64" alt="شعار الفارس" style="height: 64px; max-width: 100px; object-fit: contain; border-radius: 6px; border: 1px solid #c5a059; padding: 2px; background: #000;" />
                    </div>
                    """ else """
                    <div style="margin-bottom: 6px; font-weight: bold; border: 1.5px solid #000; padding: 4px 8px; border-radius: 6px;">
                        الفارس للمطابخ
                    </div>
                    """}
                    <div style="font-size: 11px; margin-bottom: 2px; text-align: left;"><span style="font-weight: bold;">التاريخ:</span> $hijriDate</div>
                    <div style="font-size: 11px; margin-bottom: 2px; text-align: left;"><span style="font-weight: bold;">الموافق:</span> $gregorianDate</div>
                    <div style="font-size: 11px; text-align: left;"><span style="font-weight: bold;">الوقت:</span> $timeStr</div>
                </div>
            </div>

            <div class="customer-box">
                <div class="customer-right">
                    <div><span>${customerNumber.ifBlank { invoiceNumber }}</span> &nbsp; <span style="font-size: 14px;">$customerName</span></div>
                    <div style="margin-top: 4px; font-size: 11px; font-weight: normal;">ملاحظات العميل: ${notes.ifBlank { "لا توجد" }}</div>
                </div>
                <div class="customer-left">
                    <div>العنوان: ${customerAddress.ifBlank { "---" }}</div>
                    <div style="margin-top: 4px;">الهاتف: ${customerPhone.ifBlank { "---" }}</div>
                </div>
            </div>

            <table>
                <thead>
                    <tr>
                        <th style="width: 35px;">م</th>
                        <th style="width: 70px;">رقم الصنف</th>
                        <th>اسم الصـــنــف</th>
                        <th style="width: 65px;">الكميه</th>
                        <th style="width: 80px;">السعر</th>
                        <th style="width: 90px;">الاجمالي</th>
                    </tr>
                </thead>
                <tbody>
                    $rowsHtml
                </tbody>
            </table>

            ${if (bottomNotes.isNotBlank()) """
            <div class="bottom-notes-box">
                <span style="font-weight: bold; color: #000; white-space: nowrap; margin-left: 8px;">ملاحظات / شروط:</span>
                <span style="color: #222; line-height: 1.5;">$bottomNotes</span>
            </div>
            """ else ""}

            <div class="footer-box">
                <div class="footer-right">
                    <div><b>الاجمالي كتابة:</b> $tafqeetText</div>
                    <div><b>الاعداد:</b> $preparedBy</div>
                    <div><b>التوقيع:</b> ------------------------------</div>
                </div>

                <div class="footer-center">
                    ${if (remainingAmount > 0.0) """
                    <div style="color: #d32f2f;">باقي عليكم ${String.format(Locale.US, "%.3f", remainingAmount)}-</div>
                    """ else """
                    <div style="color: #2e7d32;">خالصة ومسددة بالكامل ✓</div>
                    """}
                </div>

                <div class="footer-left">
                    <div style="display: flex; justify-content: space-between; width: 100%; max-width: 170px; margin-bottom: 2px;">
                        <span style="font-size: 12px; font-weight: bold;">الإجمالي:</span>
                        <span class="total-number-red">$formattedTotal د.ل</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; width: 100%; max-width: 170px; margin-bottom: 2px;">
                        <span style="font-size: 12px; font-weight: bold; color: #2e7d32;">المدفوع:</span>
                        <span class="paid-number-green">$formattedPaid د.ل</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; width: 100%; max-width: 170px; border-top: 1px dashed #555; padding-top: 2px;">
                        <span style="font-size: 12px; font-weight: bold; color: #d32f2f;">المتبقي:</span>
                        <span class="remaining-number">$formattedRemaining د.ل</span>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun printInvoice(context: Context, html: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("فاتورة_الفارس")
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .build()
                printManager?.print("فاتورة_الفارس", printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
}
