package com.example

import com.example.data.model.LanguageMode
import com.example.util.CalculatorEvaluator
import com.example.util.LanguageHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun calculatorEvaluator_expressions() {
    val r1 = CalculatorEvaluator.evaluate("500 + 250 * 2")
    assertTrue(r1.isSuccess)
    assertEquals(1000.0, r1.getOrNull()!!, 0.001)

    val r2 = CalculatorEvaluator.evaluate("1000 - 200 ÷ 4")
    assertTrue(r2.isSuccess)
    assertEquals(950.0, r2.getOrNull()!!, 0.001)

    val r3 = CalculatorEvaluator.evaluate("1000 * 1.85%")
    assertTrue(r3.isSuccess)
    assertEquals(18.5, r3.getOrNull()!!, 0.001)

    val r4 = CalculatorEvaluator.evaluate("1200 * 1.85%")
    assertTrue(r4.isSuccess)
    assertEquals(22.2, r4.getOrNull()!!, 0.001)

    val r5 = CalculatorEvaluator.evaluate("100 + 10%")
    assertTrue(r5.isSuccess)
    assertEquals(110.0, r5.getOrNull()!!, 0.001)

    val r6 = CalculatorEvaluator.evaluate("100 - 10%")
    assertTrue(r6.isSuccess)
    assertEquals(90.0, r6.getOrNull()!!, 0.001)
  }

  @Test
  fun languageHelper_banglaDigits() {
    val bn = LanguageHelper.toBanglaDigits("12345")
    assertEquals("১২৩৪৫", bn)
  }

  @Test
  fun languageHelper_currencyFormatting() {
    val formattedEn = LanguageHelper.formatCurrency(12500.5, LanguageMode.ENGLISH)
    assertTrue(formattedEn.contains("12,500.50") || formattedEn.contains("12500.50") || formattedEn.contains("৳"))

    val formattedBn = LanguageHelper.formatCurrency(12500.5, LanguageMode.BANGLA)
    assertTrue(formattedBn.contains("৳"))
  }

  @Test
  fun languageHelper_amountSeparatorFormats() {
    // 1. Standard Western 3-digit comma
    val standard = LanguageHelper.formatAmountNumber(
      value = 1234567.89,
      mode = LanguageMode.ENGLISH,
      groupingSeparator = ",",
      decimalSeparator = ".",
      groupingStyle = com.example.util.NumberGroupingStyle.STANDARD_3,
      decimalPlaces = 2
    )
    assertEquals("1,234,567.89", standard)

    // 2. South Asian Lakh/Crore
    val southAsian = LanguageHelper.formatAmountNumber(
      value = 1234567.89,
      mode = LanguageMode.ENGLISH,
      groupingSeparator = ",",
      decimalSeparator = ".",
      groupingStyle = com.example.util.NumberGroupingStyle.SOUTH_ASIAN,
      decimalPlaces = 2
    )
    assertEquals("12,34,567.89", southAsian)

    // 3. European Dot and Comma
    val european = LanguageHelper.formatAmountNumber(
      value = 1234567.89,
      mode = LanguageMode.ENGLISH,
      groupingSeparator = ".",
      decimalSeparator = ",",
      groupingStyle = com.example.util.NumberGroupingStyle.STANDARD_3,
      decimalPlaces = 2
    )
    assertEquals("1.234.567,89", european)

    // 4. Space / SI Separator
    val spaceSep = LanguageHelper.formatAmountNumber(
      value = 1234567.89,
      mode = LanguageMode.ENGLISH,
      groupingSeparator = " ",
      decimalSeparator = ".",
      groupingStyle = com.example.util.NumberGroupingStyle.STANDARD_3,
      decimalPlaces = 2
    )
    assertEquals("1 234 567.89", spaceSep)

    // 5. Swiss Apostrophe
    val swiss = LanguageHelper.formatAmountNumber(
      value = 1234567.89,
      mode = LanguageMode.ENGLISH,
      groupingSeparator = "'",
      decimalSeparator = ".",
      groupingStyle = com.example.util.NumberGroupingStyle.STANDARD_3,
      decimalPlaces = 2
    )
    assertEquals("1'234'567.89", swiss)

    // 6. Plain / No separator
    val plain = LanguageHelper.formatAmountNumber(
      value = 1234567.89,
      mode = LanguageMode.ENGLISH,
      groupingSeparator = "",
      decimalSeparator = ".",
      groupingStyle = com.example.util.NumberGroupingStyle.NONE,
      decimalPlaces = 2
    )
    assertEquals("1234567.89", plain)

    // 7. Bangla digits conversion with South Asian grouping
    val bnLakhCrore = LanguageHelper.formatAmountNumber(
      value = 1234567.89,
      mode = LanguageMode.BANGLA,
      groupingSeparator = ",",
      decimalSeparator = ".",
      groupingStyle = com.example.util.NumberGroupingStyle.SOUTH_ASIAN,
      decimalPlaces = 2
    )
    assertEquals("১২,৩৪,৫৬৭.৮৯", bnLakhCrore)
  }
}

