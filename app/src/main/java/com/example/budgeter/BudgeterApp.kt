package com.example.budgeter

import android.app.Application
import com.example.budgeter.data.db.AppDatabase
import com.example.budgeter.data.repository.BudgeterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BudgeterApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy {
        BudgeterRepository(
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao(),
            accountDao = database.accountDao(),
            budgetDao = database.budgetDao()
        )
    }
}
