package com.natijeh.data.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object JalaliDate {
    private val persianMonths = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    private val persianWeekdays = listOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    )

    fun format(millis: Long): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        cal.timeInMillis = millis
        val (jy, jm, jd) = toJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        val weekday = persianWeekdays.getOrElse(cal.get(Calendar.DAY_OF_WEEK) - 1) { "" }
        val month = persianMonths.getOrElse(jm - 1) { "" }
        return "$weekday ${toFaDigits(jd)} $month ${toFaDigits(jy)}"
    }

    fun parseRss(pubDate: String): Long? {
        if (pubDate.isBlank()) return null
        val patterns = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "dd MMM yyyy HH:mm:ss Z"
        )
        for (pattern in patterns) {
            try {
                val fmt = SimpleDateFormat(pattern, Locale.US)
                fmt.timeZone = TimeZone.getTimeZone("GMT")
                val parsed = fmt.parse(pubDate)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
            }
        }
        return null
    }

    fun formatRss(pubDate: String): String {
        val millis = parseRss(pubDate) ?: return pubDate
        return format(millis)
    }

    fun relative(fromMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        if (fromMillis <= 0L) return ""
        val delta = (nowMillis - fromMillis).coerceAtLeast(0)
        return when {
            delta < 15_000 -> "به‌روز شد همین الان"
            delta < 60_000 -> "به‌روز شد ${toFaDigits((delta / 1000).toInt())} ثانیه پیش"
            delta < 3_600_000 -> "به‌روز شد ${toFaDigits((delta / 60_000).toInt())} دقیقه پیش"
            delta < 86_400_000 -> "به‌روز شد ${toFaDigits((delta / 3_600_000).toInt())} ساعت پیش"
            else -> format(fromMillis)
        }
    }

    fun toFaDigits(value: Int): String = value.toString().map { ch ->
        if (ch in '0'..'9') ('۰' + (ch - '0')) else ch
    }.joinToString("")

    fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
            ((gy2 + 399) / 400) + gd + gDm[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + (days % 31)
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + ((days - 186) % 30)
        }
        return Triple(jy, jm, jd)
    }
}
