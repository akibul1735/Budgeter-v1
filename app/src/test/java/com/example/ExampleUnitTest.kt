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
  }

  @Test
  fun languageHelper_banglaDigits() {
    val bn = LanguageHelper.toBanglaDigits("12345")
    assertEquals("১২৩৪৫", bn)
  }
}

