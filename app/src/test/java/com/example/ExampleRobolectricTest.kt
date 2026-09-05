package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TransactionType
import com.example.util.CsvColumn
import com.example.util.CsvManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Budgeter", appName)
  }

  @Test
  fun `parse user specific CSV transfer row correctly`() {
    val headerLine = "Type\tDate\tSet Time\tName\tAmount\tCurrency\tExchange Rate\tCategory Group\tCategory\tAccount Class\tAccount Groups\tAccount\tNotes\tLabels\tStatus"
    val dataLine = "Transfer\t2026-09-04 21:54:00\t21:54\tRM Joha Kaka Credit\t-1000\tBDT\t1\t(Transfer)\t(Transfer)\tLiabilities\tResting Money\tRM Others\t\t\t"

    val headerTokens = CsvManager.splitCsvLine(headerLine)
    val dataTokens = CsvManager.splitCsvLine(dataLine)

    val headerMap = CsvManager.detectHeaderMapping(headerTokens)
    assertTrue("Header map should contain TYPE", headerMap.containsKey(CsvColumn.TYPE))
    assertTrue("Header map should contain AMOUNT", headerMap.containsKey(CsvColumn.AMOUNT))
    assertTrue("Header map should contain ACCOUNT_CLASS", headerMap.containsKey(CsvColumn.ACCOUNT_CLASS))
    assertTrue("Header map should contain ACCOUNT_GROUPS", headerMap.containsKey(CsvColumn.ACCOUNT_GROUPS))
    assertTrue("Header map should contain ACCOUNT", headerMap.containsKey(CsvColumn.ACCOUNT))

    val parsedRow = CsvManager.parseRowFromTokens(dataTokens, headerMap, 2)
    assertTrue("Parsed row must be valid", parsedRow.isValid)
    assertEquals(TransactionType.TRANSFER, parsedRow.type)
    assertEquals(1000.0, parsedRow.amount, 0.001)
    assertEquals("RM Joha Kaka Credit", parsedRow.name)
    assertEquals("Liabilities", parsedRow.accountClass)
    assertEquals("Resting Money", parsedRow.accountGroup)
    assertEquals("RM Others", parsedRow.account)
  }
}
