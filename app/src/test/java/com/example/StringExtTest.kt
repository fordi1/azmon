package com.example

import com.example.utils.toEnglishDigits
import com.example.utils.toPersianDigits
import org.junit.Assert.*
import org.junit.Test

class StringExtTest {

    @Test
    fun `toPersianDigits converts English digits correctly`() {
        assertEquals("۰۱۲۳۴۵۶۷۸۹", "0123456789".toPersianDigits())
    }

    @Test
    fun `toPersianDigits converts Arabic digits correctly`() {
        assertEquals("۰۱۲۳۴۵۶۷۸۹", "٠١٢٣٤٥٦٧٨٩".toPersianDigits())
    }

    @Test
    fun `toPersianDigits handles mixed content`() {
        assertEquals("سال ۱۴۰۳", "سال 1403".toPersianDigits())
    }

    @Test
    fun `toPersianDigits handles empty string`() {
        assertEquals("", "".toPersianDigits())
    }

    @Test
    fun `toPersianDigits handles string without digits`() {
        assertEquals("سلام دنیا", "سلام دنیا".toPersianDigits())
    }

    @Test
    fun `toEnglishDigits converts Persian digits correctly`() {
        assertEquals("0123456789", "۰۱۲۳۴۵۶۷۸۹".toEnglishDigits())
    }

    @Test
    fun `toEnglishDigits converts Arabic digits correctly`() {
        assertEquals("0123456789", "٠١٢٣٤٥٦٧٨٩".toEnglishDigits())
    }

    @Test
    fun `toEnglishDigits handles mixed content`() {
        assertEquals("سال 1403", "سال ۱۴۰۳".toEnglishDigits())
    }

    @Test
    fun `toEnglishDigits handles empty string`() {
        assertEquals("", "".toEnglishDigits())
    }

    @Test
    fun `roundtrip conversion preserves content`() {
        val original = "1403/03/15"
        val persian = original.toPersianDigits()
        val english = persian.toEnglishDigits()
        assertEquals(original, english)
    }

    @Test
    fun `toPersianDigits preserves non-digit text`() {
        val input = "سؤال ۵: اگر x=3 باشد"
        val result = input.toPersianDigits()
        assertTrue(result.contains("سؤال"))
        assertTrue(result.contains("۵"))
        assertTrue(result.contains("x=3") || result.contains("x=۳"))
    }
}
