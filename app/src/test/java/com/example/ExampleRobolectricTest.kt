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
  fun `parse user specific CSV transfer and expense rows correctly`() {
    val headerLine = "Type\tDate\tSet Time\tName\tAmount\tCurrency\tExchange Rate\tCategory Group\tCategory\tAccount Class\tAccount Groups\tAccount\tNotes\tLabels\tStatus"
    val transferOutLine = "Transfer\t2026-09-04 21:54:00\t21:54\tRM Joha Kaka Credit\t-1000\tBDT\t1\t(Transfer)\t(Transfer)\tLiabilities\tResting Money\tRM Others\t\t\t"
    val transferInLine = "Transfer\t2026-09-04 21:54:00\t21:54\tRM Joha Kaka Credit\t1000\tBDT\t1\t(Transfer)\t(Transfer)\tAssets\tMobile Banking Accounts\tbKash\t\t\t"
    val expenseLine = "Expense\t2026-09-04 13:58:09\t13:58\tMy Mobile Recharge\t-50\tBDT\t1\t➤Utilities\tMobile Recharge\tAssets\tMobile Banking Accounts\tbKash\t\tself\t"

    val (headerMap, _) = CsvManager.detectHeaderMapping(listOf(headerLine))
    assertTrue("Header map should contain type", headerMap.containsKey("type"))
    assertTrue("Header map should contain amount", headerMap.containsKey("amount"))
    assertTrue("Header map should contain account_class", headerMap.containsKey("account_class"))
    assertTrue("Header map should contain account_groups", headerMap.containsKey("account_groups"))
    assertTrue("Header map should contain account", headerMap.containsKey("account"))
    assertTrue("Header map should contain category_group", headerMap.containsKey("category_group"))
    assertTrue("Header map should contain category", headerMap.containsKey("category"))
    assertTrue("Header map should contain labels", headerMap.containsKey("labels"))

    // Test Outflow Transfer Row
    val outTokens = CsvManager.parseCsvLine(transferOutLine)
    val parsedOut = CsvManager.parseRowFromTokens(outTokens, headerMap, 2)
    assertTrue("Parsed row must be valid", parsedOut.isValid)
    assertEquals(TransactionType.TRANSFER, parsedOut.type)
    assertEquals(1000.0, parsedOut.amount, 0.001)
    assertEquals("RM Joha Kaka Credit", parsedOut.name)
    assertEquals("Liabilities", parsedOut.accountClass)
    assertEquals("Resting Money", parsedOut.accountGroup)
    assertEquals("RM Others", parsedOut.account)

    // Test Inflow Transfer Row
    val inTokens = CsvManager.parseCsvLine(transferInLine)
    val parsedIn = CsvManager.parseRowFromTokens(inTokens, headerMap, 3)
    assertTrue("Parsed row must be valid", parsedIn.isValid)
    assertEquals(TransactionType.TRANSFER, parsedIn.type)
    assertEquals(1000.0, parsedIn.amount, 0.001)
    assertEquals("bKash", parsedIn.account)
    assertEquals("Mobile Banking Accounts", parsedIn.accountGroup)
    assertEquals("Assets", parsedIn.accountClass)

    // Test Expense Row with symbol category group
    val expTokens = CsvManager.parseCsvLine(expenseLine)
    val parsedExp = CsvManager.parseRowFromTokens(expTokens, headerMap, 4)
    assertTrue("Parsed row must be valid", parsedExp.isValid)
    assertEquals(TransactionType.EXPENSE, parsedExp.type)
    assertEquals(50.0, parsedExp.amount, 0.001)
    assertEquals("My Mobile Recharge", parsedExp.name)
    assertEquals("Utilities", CsvManager.cleanCategoryGroupName(parsedExp.categoryGroup))
    assertEquals("Mobile Recharge", parsedExp.category)
    assertEquals("bKash", parsedExp.account)
    assertEquals("self", parsedExp.labels)
  }
}
