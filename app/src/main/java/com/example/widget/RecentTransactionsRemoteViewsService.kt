package com.example.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.util.CurrencyPreferences
import com.example.util.WidgetPreferences
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentTransactionsRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return RecentTransactionsRemoteViewsFactory(applicationContext)
    }
}

class RecentTransactionsRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private val transactions = mutableListOf<Transaction>()
    private val accountMap = mutableMapOf<Long, Account>()
    private val categoryMap = mutableMapOf<Long, Category>()
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    private fun loadData() {
        try {
            runBlocking {
                val db = AppDatabase.getDatabase(context)
                val txs = db.transactionDao().getRecentTransactions(10)
                val accs = db.accountDao().getAllAccountsSnapshot()
                val cats = db.categoryDao().getAllCategoriesSnapshot()

                synchronized(this@RecentTransactionsRemoteViewsFactory) {
                    transactions.clear()
                    transactions.addAll(txs)

                    accountMap.clear()
                    accs.forEach { accountMap[it.id] = it }

                    categoryMap.clear()
                    cats.forEach { categoryMap[it.id] = it }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        transactions.clear()
    }

    override fun getCount(): Int = synchronized(this) { transactions.size }

    override fun getViewAt(position: Int): RemoteViews? {
        val tx: Transaction = synchronized(this) {
            if (position < 0 || position >= transactions.size) return null
            transactions[position]
        }

        val views = RemoteViews(context.packageName, R.layout.widget_recent_transaction_item)
        val isPrivacy = WidgetPreferences.getInstance(context).isPrivacyEnabled()
        val currencySymbol = CurrencyPreferences.getInstance(context).config.value.activeSymbol

        val cat = categoryMap[tx.categoryId]
        val title = tx.payeeOrPayer.ifBlank { cat?.nameEn ?: (if (tx.note.isNotBlank()) tx.note else "Transaction") }
        views.setTextViewText(R.id.tv_item_title, title)

        val acc = accountMap[tx.debitAccountId ?: tx.creditAccountId]
        val accName = acc?.nameEn ?: "Account"
        val dateStr = dateFormat.format(Date(tx.dateEpochMs))
        views.setTextViewText(R.id.tv_item_subtitle, "$accName • $dateStr")

        val (amountStr, colorRes, iconRes) = when (tx.type) {
            TransactionType.INCOME -> {
                val formatted = if (isPrivacy) "+$currencySymbol •••" else "+$currencySymbol ${decimalFormat.format(tx.amount)}"
                Triple(formatted, 0xFF10B981.toInt(), R.drawable.ic_widget_trending_up)
            }
            TransactionType.EXPENSE -> {
                val formatted = if (isPrivacy) "-$currencySymbol •••" else "-$currencySymbol ${decimalFormat.format(tx.amount)}"
                Triple(formatted, 0xFFEF4444.toInt(), R.drawable.ic_widget_trending_down)
            }
            TransactionType.TRANSFER -> {
                val formatted = if (isPrivacy) "⇄ $currencySymbol •••" else "⇄ $currencySymbol ${decimalFormat.format(tx.amount)}"
                Triple(formatted, 0xFF38BDF8.toInt(), R.drawable.ic_widget_refresh)
            }
        }

        views.setTextViewText(R.id.tv_item_amount, amountStr)
        views.setTextColor(R.id.tv_item_amount, colorRes)
        views.setImageViewResource(R.id.iv_item_type, iconRes)

        // Fill-in intent for click template
        val fillInIntent = Intent().apply {
            putExtra("EXTRA_TRANSACTION_ID", tx.id)
        }
        views.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return synchronized(this) {
            if (position in transactions.indices) transactions[position].id else position.toLong()
        }
    }

    override fun hasStableIds(): Boolean = true
}
