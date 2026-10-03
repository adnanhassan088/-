package com.example.ui.util

import java.util.Locale

object ArabicNumberToWords {
    private val units = arrayOf(
        "", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة",
        "عشرة", "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر",
        "سبعة عشر", "ثمانية عشر", "تسعة عشر"
    )

    private val tens = arrayOf(
        "", "", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون"
    )

    private val hundreds = arrayOf(
        "", "مائة", "مائتان", "ثلاثمائة", "أربعمائة", "خمسمائة", "ستمائة", "سبعمائة", "ثمانمائة", "تسعمائة"
    )

    fun convert(number: Long): String {
        if (number == 0L) return "صفر"
        if (number < 0) return "سالب " + convert(-number)

        var result = ""
        var n = number

        val billions = n / 1_000_000_000
        n %= 1_000_000_000
        val millions = n / 1_000_000
        n %= 1_000_000
        val thousands = n / 1000
        n %= 1000
        val remainder = n

        if (billions > 0) {
            result += convertUnderThousand(billions) + " مليار"
        }

        if (millions > 0) {
            if (result.isNotEmpty()) result += " و"
            result += when (millions) {
                1L -> "مليون"
                2L -> "مليونان"
                in 3..10 -> convertUnderThousand(millions) + " ملايين"
                else -> convertUnderThousand(millions) + " مليون"
            }
        }

        if (thousands > 0) {
            if (result.isNotEmpty()) result += " و"
            result += when (thousands) {
                1L -> "ألف"
                2L -> "ألفان"
                in 3..10 -> convertUnderThousand(thousands) + " آلاف"
                else -> convertUnderThousand(thousands) + " ألف"
            }
        }

        if (remainder > 0) {
            if (result.isNotEmpty()) result += " و"
            result += convertUnderThousand(remainder)
        }

        return result
    }

    private fun convertUnderThousand(number: Long): String {
        var res = ""
        val c = (number / 100).toInt()
        val r = (number % 100).toInt()

        if (c > 0) {
            res += hundreds[c]
        }

        if (r > 0) {
            if (res.isNotEmpty()) res += " و"
            if (r < 20) {
                res += units[r]
            } else {
                val u = r % 10
                val t = r / 10
                if (u > 0) {
                    res += units[u] + " و" + tens[t]
                } else {
                    res += tens[t]
                }
            }
        }

        return res
    }

    fun toTafqeet(amount: Double, currencyUnit: String = ""): String {
        val longVal = amount.toLong()
        val fraction = ((amount - longVal) * 100).toLong()
        var text = convert(longVal)
        if (fraction > 0) {
            text += " و" + convert(fraction) + " قرش/فلس"
        }
        return if (currencyUnit.isNotBlank()) {
            "$text $currencyUnit فقط لاغير"
        } else {
            "$text فقط لاغير"
        }
    }
}
