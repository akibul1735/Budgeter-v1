package com.example.util

import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class CategoryTimelineInterval(val titleEn: String, val titleBn: String) {
    PAST_12_MONTHS("Past 12 Months", "গত ১২ মাস"),
    PAST_6_MONTHS("Past 6 Months", "গত ৬ মাস"),
    PAST_3_MONTHS("Past 3 Months", "গত ৩ মাস"),
    QUARTERLY("Quarterly (4 Quarters)", "ত্রৈমাসিক (৪ কোয়ার্টার)"),
    YEARLY("Yearly (Past 3 Years)", "বাৎসরিক (৩ বছর)"),
    ALL_TIME("All Time", "সর্বমোট সময়কাল"),
    CUSTOM("Custom Range", "নির্দিষ্ট সময়সীমা")
}

enum class CategoryTimelineSortOrder(val titleEn: String, val titleBn: String) {
    DEFAULT("Default Order", "ডিফল্ট ক্রম"),
    AMOUNT_DESC("Total: High to Low", "মোট: বেশি থেকে কম"),
    AMOUNT_ASC("Total: Low to High", "মোট: কম থেকে বেশি"),
    NAME_ASC("Name: A to Z", "নাম: ক থেকে ঁ")
}

data class CategoryTimelinePeriod(
    val startEpochMs: Long,
    val endEpochMs: Long,
    val label: String,
    val shortLabel: String
)

data class CategoryTimelineRow(
    val category: Category,
    val amountsByPeriod: List<Double>,
    val totalAmount: Double = amountsByPeriod.sum(),
    val averageAmount: Double = if (amountsByPeriod.isNotEmpty()) amountsByPeriod.sum() / amountsByPeriod.size else 0.0,
    val trendDelta: Double = if (amountsByPeriod.size >= 2) amountsByPeriod.last() - amountsByPeriod[amountsByPeriod.size - 2] else 0.0
)

data class CategoryTimelineGroup(
    val parentCategory: Category?,
    val groupNameEn: String,
    val groupNameBn: String,
    val type: CategoryType,
    val groupAmountsByPeriod: List<Double>,
    val subCategories: List<CategoryTimelineRow>,
    val totalAmount: Double = groupAmountsByPeriod.sum(),
    val averageAmount: Double = if (groupAmountsByPeriod.isNotEmpty()) groupAmountsByPeriod.sum() / groupAmountsByPeriod.size else 0.0,
    val trendDelta: Double = if (groupAmountsByPeriod.size >= 2) groupAmountsByPeriod.last() - groupAmountsByPeriod[groupAmountsByPeriod.size - 2] else 0.0
)

data class CategoryTimelineData(
    val interval: CategoryTimelineInterval,
    val periods: List<CategoryTimelinePeriod>,
    val expenseGroups: List<CategoryTimelineGroup>,
    val incomeGroups: List<CategoryTimelineGroup>,
    val totalExpenseByPeriod: List<Double>,
    val totalIncomeByPeriod: List<Double>,
    val netSavingsByPeriod: List<Double>,
    val grandTotalExpense: Double,
    val grandTotalIncome: Double,
    val grandNetSavings: Double,
    val avgExpensePerPeriod: Double,
    val avgIncomePerPeriod: Double,
    val avgNetSavingsPerPeriod: Double
)

object CategoryTimelineHelper {

    fun generatePeriods(
        interval: CategoryTimelineInterval,
        customPeriodsCount: Int = 12,
        customStartDateMs: Long? = null,
        customEndDateMs: Long? = null,
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ): List<CategoryTimelinePeriod> {
        val periods = mutableListOf<CategoryTimelinePeriod>()
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()

        when (interval) {
            CategoryTimelineInterval.PAST_12_MONTHS -> {
                generateMonthlyPeriods(periods, count = 12, cal, languageMode)
            }
            CategoryTimelineInterval.PAST_6_MONTHS -> {
                generateMonthlyPeriods(periods, count = 6, cal, languageMode)
            }
            CategoryTimelineInterval.PAST_3_MONTHS -> {
                generateMonthlyPeriods(periods, count = 3, cal, languageMode)
            }
            CategoryTimelineInterval.QUARTERLY -> {
                // 4 Quarters
                for (i in 3 downTo 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.MONTH, -i * 3)
                    val qEndMonth = (c.get(Calendar.MONTH) / 3) * 3 + 2
                    val qYear = c.get(Calendar.YEAR)

                    val startCal = Calendar.getInstance()
                    startCal.set(Calendar.YEAR, qYear)
                    startCal.set(Calendar.MONTH, qEndMonth - 2)
                    startCal.set(Calendar.DAY_OF_MONTH, 1)
                    startCal.set(Calendar.HOUR_OF_DAY, 0)
                    startCal.set(Calendar.MINUTE, 0)
                    startCal.set(Calendar.SECOND, 0)
                    startCal.set(Calendar.MILLISECOND, 0)

                    val endCal = Calendar.getInstance()
                    endCal.set(Calendar.YEAR, qYear)
                    endCal.set(Calendar.MONTH, qEndMonth)
                    endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                    endCal.set(Calendar.HOUR_OF_DAY, 23)
                    endCal.set(Calendar.MINUTE, 59)
                    endCal.set(Calendar.SECOND, 59)
                    endCal.set(Calendar.MILLISECOND, 999)

                    val qNum = (qEndMonth / 3) + 1
                    val shortLbl = if (languageMode == LanguageMode.BANGLA) "কোয়ার্টার ${LanguageHelper.toBanglaDigits(qNum.toString())} '$qYear" else "Q$qNum '$qYear"
                    val fullLbl = if (languageMode == LanguageMode.BANGLA) "$qYear এর ${LanguageHelper.toBanglaDigits(qNum.toString())}ম কোয়ার্টার" else "Quarter $qNum, $qYear"

                    periods.add(CategoryTimelinePeriod(startCal.timeInMillis, endCal.timeInMillis, fullLbl, shortLbl))
                }
            }
            CategoryTimelineInterval.YEARLY -> {
                // Past 3 years
                for (i in 2 downTo 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.YEAR, -i)
                    val yr = c.get(Calendar.YEAR)

                    val startCal = Calendar.getInstance()
                    startCal.set(Calendar.YEAR, yr)
                    startCal.set(Calendar.MONTH, Calendar.JANUARY)
                    startCal.set(Calendar.DAY_OF_MONTH, 1)
                    startCal.set(Calendar.HOUR_OF_DAY, 0)
                    startCal.set(Calendar.MINUTE, 0)
                    startCal.set(Calendar.SECOND, 0)
                    startCal.set(Calendar.MILLISECOND, 0)

                    val endCal = Calendar.getInstance()
                    endCal.set(Calendar.YEAR, yr)
                    endCal.set(Calendar.MONTH, Calendar.DECEMBER)
                    endCal.set(Calendar.DAY_OF_MONTH, 31)
                    endCal.set(Calendar.HOUR_OF_DAY, 23)
                    endCal.set(Calendar.MINUTE, 59)
                    endCal.set(Calendar.SECOND, 59)
                    endCal.set(Calendar.MILLISECOND, 999)

                    val yrLabel = if (languageMode == LanguageMode.BANGLA) LanguageHelper.toBanglaDigits(yr.toString()) else yr.toString()
                    periods.add(CategoryTimelinePeriod(startCal.timeInMillis, endCal.timeInMillis, yrLabel, yrLabel))
                }
            }
            CategoryTimelineInterval.ALL_TIME -> {
                generateMonthlyPeriods(periods, count = 24, cal, languageMode)
            }
            CategoryTimelineInterval.CUSTOM -> {
                if (customStartDateMs != null && customEndDateMs != null && customStartDateMs < customEndDateMs) {
                    generateCustomPeriods(periods, customStartDateMs, customEndDateMs, languageMode)
                } else {
                    generateMonthlyPeriods(periods, count = 12, cal, languageMode)
                }
            }
        }
        return periods
    }

    private fun generateMonthlyPeriods(
        periods: MutableList<CategoryTimelinePeriod>,
        count: Int,
        cal: Calendar,
        languageMode: LanguageMode
    ) {
        val now = System.currentTimeMillis()
        val shortEnSdf = SimpleDateFormat("MMM ''yy", Locale.US)
        val fullEnSdf = SimpleDateFormat("MMMM yyyy", Locale.US)

        for (i in (count - 1) downTo 0) {
            val c = Calendar.getInstance()
            c.timeInMillis = now
            c.add(Calendar.MONTH, -i)

            val yr = c.get(Calendar.YEAR)
            val mo = c.get(Calendar.MONTH) + 1

            val startMs = DateUtils.getStartOfMonth(yr, mo)
            val endMs = DateUtils.getEndOfMonth(yr, mo)

            val fullLbl = DateUtils.formatMonthYear(yr, mo, languageMode)
            val shortLbl = if (languageMode == LanguageMode.BANGLA) {
                val bnShort = getBanglaShortMonth(mo)
                val yrStr = LanguageHelper.toBanglaDigits((yr % 100).toString())
                "$bnShort '$yrStr"
            } else {
                shortEnSdf.format(Date(startMs))
            }

            periods.add(CategoryTimelinePeriod(startMs, endMs, fullLbl, shortLbl))
        }
    }

    private fun generateCustomPeriods(
        periods: MutableList<CategoryTimelinePeriod>,
        startMs: Long,
        endMs: Long,
        languageMode: LanguageMode
    ) {
        val diffDays = (endMs - startMs) / (1000L * 60 * 60 * 24)
        if (diffDays <= 45) {
            // Split into weekly periods
            var currentStart = startMs
            var weekIndex = 1
            while (currentStart < endMs) {
                val currentEnd = (currentStart + 7L * 24 * 60 * 60 * 1000 - 1).coerceAtMost(endMs)
                val shortLbl = "W$weekIndex"
                val fullLbl = "${DateUtils.formatDate(currentStart, languageMode)} - ${DateUtils.formatDate(currentEnd, languageMode)}"
                periods.add(CategoryTimelinePeriod(currentStart, currentEnd, fullLbl, shortLbl))
                currentStart = currentEnd + 1
                weekIndex++
            }
        } else {
            // Split month-by-month
            val c = Calendar.getInstance()
            c.timeInMillis = startMs
            while (c.timeInMillis <= endMs) {
                val yr = c.get(Calendar.YEAR)
                val mo = c.get(Calendar.MONTH) + 1
                val pStart = DateUtils.getStartOfMonth(yr, mo).coerceAtLeast(startMs)
                val pEnd = DateUtils.getEndOfMonth(yr, mo).coerceAtMost(endMs)
                val fullLbl = DateUtils.formatMonthYear(yr, mo, languageMode)
                val shortLbl = if (languageMode == LanguageMode.BANGLA) {
                    val bnShort = getBanglaShortMonth(mo)
                    val yrStr = LanguageHelper.toBanglaDigits((yr % 100).toString())
                    "$bnShort '$yrStr"
                } else {
                    SimpleDateFormat("MMM ''yy", Locale.US).format(Date(pStart))
                }
                periods.add(CategoryTimelinePeriod(pStart, pEnd, fullLbl, shortLbl))
                c.add(Calendar.MONTH, 1)
            }
        }
    }

    private fun getBanglaShortMonth(month: Int): String {
        return when (month) {
            1 -> "জানু"
            2 -> "ফেব্রু"
            3 -> "মার্চ"
            4 -> "এপ্রিল"
            5 -> "মে"
            6 -> "জুন"
            7 -> "জুলাই"
            8 -> "আগস্ট"
            9 -> "সেপ্টে"
            10 -> "অক্টো"
            11 -> "নভে"
            12 -> "ডিসে"
            else -> ""
        }
    }

    fun calculateTimeline(
        categories: List<Category>,
        transactions: List<Transaction>,
        interval: CategoryTimelineInterval = CategoryTimelineInterval.PAST_12_MONTHS,
        customPeriodsCount: Int = 12,
        customStartDateMs: Long? = null,
        customEndDateMs: Long? = null,
        selectedCategoryIds: Set<Long> = emptySet(),
        selectedStatusSet: Set<TransactionStatus> = emptySet(),
        excludeZeroAmounts: Boolean = false,
        searchQuery: String = "",
        sortOrder: CategoryTimelineSortOrder = CategoryTimelineSortOrder.DEFAULT,
        languageMode: LanguageMode = LanguageMode.ENGLISH
    ): CategoryTimelineData {
        val periods = generatePeriods(
            interval = interval,
            customPeriodsCount = customPeriodsCount,
            customStartDateMs = customStartDateMs,
            customEndDateMs = customEndDateMs,
            languageMode = languageMode
        )

        val numPeriods = periods.size

        // Filter transactions by status and valid types
        val validTx = transactions.filter { tx ->
            (tx.type == TransactionType.EXPENSE || tx.type == TransactionType.INCOME) &&
                    (selectedStatusSet.isEmpty() || tx.status in selectedStatusSet)
        }

        // Map Category ID to Category object
        val categoryMap = categories.associateBy { it.id }

        // Find parent and subcategories
        val expenseParents = categories.filter { it.type == CategoryType.EXPENSE && it.parentId == null }
        val incomeParents = categories.filter { it.type == CategoryType.INCOME && it.parentId == null }

        // Build Category Groups
        fun buildGroups(
            parentCategories: List<Category>,
            catType: CategoryType
        ): List<CategoryTimelineGroup> {
            val groups = mutableListOf<CategoryTimelineGroup>()

            // 1. Process defined parent categories
            for (parent in parentCategories) {
                val subs = categories.filter { it.parentId == parent.id }

                val subRows = mutableListOf<CategoryTimelineRow>()
                val groupAmounts = DoubleArray(numPeriods) { 0.0 }

                for (sub in subs) {
                    val subAmounts = DoubleArray(numPeriods) { 0.0 }
                    for ((pIdx, period) in periods.withIndex()) {
                        val amount = validTx.filter { tx ->
                            tx.type == (if (catType == CategoryType.EXPENSE) TransactionType.EXPENSE else TransactionType.INCOME) &&
                                    tx.dateEpochMs in period.startEpochMs..period.endEpochMs &&
                                    (tx.subCategoryId == sub.id || (tx.subCategoryId == null && tx.categoryId == sub.id))
                        }.sumOf { it.amount }
                        subAmounts[pIdx] = amount
                        groupAmounts[pIdx] += amount
                    }

                    val row = CategoryTimelineRow(
                        category = sub,
                        amountsByPeriod = subAmounts.toList()
                    )
                    subRows.add(row)
                }

                // Check transactions directly under parent category without subcategory
                val directAmounts = DoubleArray(numPeriods) { 0.0 }
                for ((pIdx, period) in periods.withIndex()) {
                    val direct = validTx.filter { tx ->
                        tx.type == (if (catType == CategoryType.EXPENSE) TransactionType.EXPENSE else TransactionType.INCOME) &&
                                tx.dateEpochMs in period.startEpochMs..period.endEpochMs &&
                                tx.categoryId == parent.id && tx.subCategoryId == null
                    }.sumOf { it.amount }
                    directAmounts[pIdx] = direct
                    groupAmounts[pIdx] += direct
                }

                // If parent has direct transactions or no subcategories, add parent as a row if needed
                if (subs.isEmpty() || directAmounts.any { it > 0.0 }) {
                    if (subs.isEmpty()) {
                        // Parent category acts as its own row
                        subRows.add(
                            CategoryTimelineRow(
                                category = parent,
                                amountsByPeriod = directAmounts.toList()
                            )
                        )
                    } else if (directAmounts.any { it > 0.0 }) {
                        val generalSub = parent.copy(
                            id = -parent.id,
                            nameEn = "${parent.nameEn} (General)",
                            nameBn = "${parent.nameBn} (সাধারণ)"
                        )
                        subRows.add(
                            CategoryTimelineRow(
                                category = generalSub,
                                amountsByPeriod = directAmounts.toList()
                            )
                        )
                    }
                }

                val group = CategoryTimelineGroup(
                    parentCategory = parent,
                    groupNameEn = parent.nameEn,
                    groupNameBn = parent.nameBn,
                    type = catType,
                    groupAmountsByPeriod = groupAmounts.toList(),
                    subCategories = subRows
                )
                groups.add(group)
            }

            // 2. Process orphaned subcategories or uncategorized transactions
            val assignedCatIds = (parentCategories.map { it.id } + categories.filter { it.parentId != null }.map { it.id }).toSet()
            val unassignedTx = validTx.filter { tx ->
                tx.type == (if (catType == CategoryType.EXPENSE) TransactionType.EXPENSE else TransactionType.INCOME) &&
                        ((tx.categoryId == null && tx.subCategoryId == null) ||
                                (tx.categoryId != null && tx.categoryId !in assignedCatIds))
            }

            if (unassignedTx.isNotEmpty()) {
                val otherAmounts = DoubleArray(numPeriods) { 0.0 }
                for ((pIdx, period) in periods.withIndex()) {
                    otherAmounts[pIdx] = unassignedTx.filter {
                        it.dateEpochMs in period.startEpochMs..period.endEpochMs
                    }.sumOf { it.amount }
                }

                if (otherAmounts.any { it > 0.0 }) {
                    val dummyCat = Category(
                        id = -9999L,
                        nameEn = "Uncategorized",
                        nameBn = "অশ্রেণীভুক্ত",
                        type = catType
                    )
                    val otherRow = CategoryTimelineRow(
                        category = dummyCat,
                        amountsByPeriod = otherAmounts.toList()
                    )
                    groups.add(
                        CategoryTimelineGroup(
                            parentCategory = null,
                            groupNameEn = if (catType == CategoryType.EXPENSE) "Other Expenses" else "Other Income",
                            groupNameBn = if (catType == CategoryType.EXPENSE) "অন্যান্য ব্যয়" else "অন্যান্য আয়",
                            type = catType,
                            groupAmountsByPeriod = otherAmounts.toList(),
                            subCategories = listOf(otherRow)
                        )
                    )
                }
            }

            // Apply category selection filter if set
            var filteredGroups = if (selectedCategoryIds.isNotEmpty()) {
                groups.mapNotNull { grp ->
                    val isParentSelected = grp.parentCategory?.id in selectedCategoryIds
                    val filteredSubs = grp.subCategories.filter { it.category.id in selectedCategoryIds || isParentSelected }
                    if (isParentSelected || filteredSubs.isNotEmpty()) {
                        val newAmounts = DoubleArray(numPeriods) { pIdx ->
                            filteredSubs.sumOf { it.amountsByPeriod.getOrElse(pIdx) { 0.0 } }
                        }
                        grp.copy(
                            subCategories = filteredSubs,
                            groupAmountsByPeriod = newAmounts.toList(),
                            totalAmount = newAmounts.sum(),
                            averageAmount = if (numPeriods > 0) newAmounts.sum() / numPeriods else 0.0
                        )
                    } else null
                }
            } else groups

            // Apply search query filter
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                filteredGroups = filteredGroups.mapNotNull { grp ->
                    val parentMatch = grp.groupNameEn.lowercase().contains(q) || grp.groupNameBn.lowercase().contains(q)
                    val matchingSubs = grp.subCategories.filter {
                        it.category.nameEn.lowercase().contains(q) || it.category.nameBn.lowercase().contains(q)
                    }
                    if (parentMatch || matchingSubs.isNotEmpty()) {
                        val subs = if (parentMatch) grp.subCategories else matchingSubs
                        val newAmounts = DoubleArray(numPeriods) { pIdx ->
                            subs.sumOf { it.amountsByPeriod.getOrElse(pIdx) { 0.0 } }
                        }
                        grp.copy(
                            subCategories = subs,
                            groupAmountsByPeriod = newAmounts.toList(),
                            totalAmount = newAmounts.sum(),
                            averageAmount = if (numPeriods > 0) newAmounts.sum() / numPeriods else 0.0
                        )
                    } else null
                }
            }

            // Exclude Zero amounts filter
            if (excludeZeroAmounts) {
                filteredGroups = filteredGroups.mapNotNull { grp ->
                    val nonZeroSubs = grp.subCategories.filter { it.totalAmount > 0.0 }
                    if (grp.totalAmount > 0.0 || nonZeroSubs.isNotEmpty()) {
                        grp.copy(subCategories = nonZeroSubs)
                    } else null
                }
            }

            // Sort Groups
            return when (sortOrder) {
                CategoryTimelineSortOrder.DEFAULT -> filteredGroups
                CategoryTimelineSortOrder.AMOUNT_DESC -> filteredGroups.sortedByDescending { it.totalAmount }
                CategoryTimelineSortOrder.AMOUNT_ASC -> filteredGroups.sortedBy { it.totalAmount }
                CategoryTimelineSortOrder.NAME_ASC -> filteredGroups.sortedBy {
                    if (languageMode == LanguageMode.BANGLA) it.groupNameBn else it.groupNameEn
                }
            }
        }

        val expenseGroups = buildGroups(expenseParents, CategoryType.EXPENSE)
        val incomeGroups = buildGroups(incomeParents, CategoryType.INCOME)

        // Compute total expenses and incomes per period
        val totalExpenseByPeriod = List(numPeriods) { pIdx ->
            expenseGroups.sumOf { it.groupAmountsByPeriod.getOrElse(pIdx) { 0.0 } }
        }
        val totalIncomeByPeriod = List(numPeriods) { pIdx ->
            incomeGroups.sumOf { it.groupAmountsByPeriod.getOrElse(pIdx) { 0.0 } }
        }
        val netSavingsByPeriod = List(numPeriods) { pIdx ->
            totalIncomeByPeriod[pIdx] - totalExpenseByPeriod[pIdx]
        }

        val grandTotalExpense = totalExpenseByPeriod.sum()
        val grandTotalIncome = totalIncomeByPeriod.sum()
        val grandNetSavings = grandTotalIncome - grandTotalExpense

        val avgExpensePerPeriod = if (numPeriods > 0) grandTotalExpense / numPeriods else 0.0
        val avgIncomePerPeriod = if (numPeriods > 0) grandTotalIncome / numPeriods else 0.0
        val avgNetSavingsPerPeriod = if (numPeriods > 0) grandNetSavings / numPeriods else 0.0

        return CategoryTimelineData(
            interval = interval,
            periods = periods,
            expenseGroups = expenseGroups,
            incomeGroups = incomeGroups,
            totalExpenseByPeriod = totalExpenseByPeriod,
            totalIncomeByPeriod = totalIncomeByPeriod,
            netSavingsByPeriod = netSavingsByPeriod,
            grandTotalExpense = grandTotalExpense,
            grandTotalIncome = grandTotalIncome,
            grandNetSavings = grandNetSavings,
            avgExpensePerPeriod = avgExpensePerPeriod,
            avgIncomePerPeriod = avgIncomePerPeriod,
            avgNetSavingsPerPeriod = avgNetSavingsPerPeriod
        )
    }
}
