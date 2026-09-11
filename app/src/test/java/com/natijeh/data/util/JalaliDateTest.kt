package com.natijeh.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JalaliDateTest {
    @Test
    fun `nowruz 2024 is first of farvardin 1403`() {
        val (jy, jm, jd) = JalaliDate.toJalali(2024, 3, 20)
        assertEquals(1403, jy)
        assertEquals(1, jm)
        assertEquals(1, jd)
    }

    @Test
    fun `rss date is parsed to epoch`() {
        val millis = JalaliDate.parseRss("Thu, 11 Sep 2026 12:00:00 GMT")
        assertTrue(millis != null && millis > 0)
        val formatted = JalaliDate.format(millis!!)
        assertTrue(formatted.contains("شهریور") || formatted.contains("۱۴۰۵"))
    }

    @Test
    fun `relative text for fresh update`() {
        val now = 1_000_000L
        assertEquals("به‌روز شد همین الان", JalaliDate.relative(now - 5_000, now))
    }
}
