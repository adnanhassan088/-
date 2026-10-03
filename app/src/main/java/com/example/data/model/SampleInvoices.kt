package com.example.data.model

data class SampleInvoicePreset(
    val title: String,
    val merchantName: String,
    val invoiceNumber: String,
    val category: String,
    val currency: String,
    val subtotal: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val paymentMethod: String,
    val notes: String,
    val items: List<InvoiceLineItem>
)

object SampleInvoicesProvider {
    val presets = listOf(
        SampleInvoicePreset(
            title = "فاتورة سوبرماركت هايبر",
            merchantName = "أسواق التميمي المركزية",
            invoiceNumber = "INV-78491",
            category = "بقالة ومواد غذائية",
            currency = "ر.س",
            subtotal = 145.0,
            taxAmount = 21.75,
            totalAmount = 166.75,
            paymentMethod = "بطاقة مدى",
            notes = "شراء مشتريات البقالة الأسبوعية للمنزل - خاضع لضريبة القيمة المضافة 15%",
            items = listOf(
                InvoiceLineItem("أرز بسمتي 5 كجم", 1.0, 48.0, 48.0),
                InvoiceLineItem("حليب المراعي طازج 2 لتر", 2.0, 11.5, 23.0),
                InvoiceLineItem("زيت دوار الشمس 1.5 لتر", 1.0, 19.0, 19.0),
                InvoiceLineItem("تفاح أحمر إيطالي (كجم)", 2.5, 12.0, 30.0),
                InvoiceLineItem("جبنة بيضاء فيتا 500 جم", 1.0, 25.0, 25.0)
            )
        ),
        SampleInvoicePreset(
            title = "فاتورة مطعم ومقهى",
            merchantName = "مطعم ومشاوي السرايا",
            invoiceNumber = "INV-43209",
            category = "مطاعم ومقاهي",
            currency = "ر.س",
            subtotal = 210.0,
            taxAmount = 31.5,
            totalAmount = 241.5,
            paymentMethod = "Apple Pay",
            notes = "عشاء عمل - طاولة رقم 14 - شامل خدمة وضريبة 15%",
            items = listOf(
                InvoiceLineItem("صحن مشكل مشاوي عائلي", 1.0, 120.0, 120.0),
                InvoiceLineItem("سلطة فتوش شامي", 2.0, 20.0, 40.0),
                InvoiceLineItem("حمص باللحمة والصنوبر", 1.0, 26.0, 26.0),
                InvoiceLineItem("عصير رمان طبيعي", 2.0, 12.0, 24.0)
            )
        ),
        SampleInvoicePreset(
            title = "فاتورة أجهزة وإلكترونيات",
            merchantName = "متجر تقنية المستقبل للإلكترونيات",
            invoiceNumber = "INV-99214",
            category = "إلكترونيات وأجهزة",
            currency = "ر.س",
            subtotal = 580.0,
            taxAmount = 87.0,
            totalAmount = 667.0,
            paymentMethod = "فيزا كارد",
            notes = "إكسسوارات حاسوب وهاتف مع ضمان لمدة عامين",
            items = listOf(
                InvoiceLineItem("سماعة لاسلكية بلوتوث ANC", 1.0, 320.0, 320.0),
                InvoiceLineItem("شاحن سريع 65 واط GaN", 1.0, 160.0, 160.0),
                InvoiceLineItem("كابل شحن سريع Type-C إلى Type-C", 2.0, 50.0, 100.0)
            )
        ),
        SampleInvoicePreset(
            title = "فاتورة صيدلية ورعاية",
            merchantName = "صيدليات المجتمع الحديثة",
            invoiceNumber = "INV-55102",
            category = "صحة وأدوية",
            currency = "ر.س",
            subtotal = 95.0,
            taxAmount = 14.25,
            totalAmount = 109.25,
            paymentMethod = "نقدي",
            notes = "أدوية فيتامينات ومستلزمات إسعاف أولية",
            items = listOf(
                InvoiceLineItem("فيتامين د 50000 وحدة", 1.0, 55.0, 55.0),
                InvoiceLineItem("بنادول إكسترا 24 قرص", 2.0, 12.5, 25.0),
                InvoiceLineItem("بخاخ معقم كحولي 100 مل", 3.0, 5.0, 15.0)
            )
        )
    )

    fun toEntity(preset: SampleInvoicePreset, dateString: String = "2026-09-23"): InvoiceEntity {
        return InvoiceEntity(
            merchantName = preset.merchantName,
            invoiceNumber = preset.invoiceNumber,
            date = dateString,
            category = preset.category,
            currency = preset.currency,
            subtotal = preset.subtotal,
            taxAmount = preset.taxAmount,
            discountAmount = 0.0,
            totalAmount = preset.totalAmount,
            paymentMethod = preset.paymentMethod,
            status = "PAID",
            notes = preset.notes,
            itemsJson = InvoiceEntity.itemsToJson(preset.items),
            createdAt = System.currentTimeMillis()
        )
    }
}
