package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.filter.BudgetMakerDashboardFilter
import com.example.ui.components.filter.BudgetMakerFilterSpecs
import com.example.ui.components.filter.BudgetMakerTabFilter
import com.example.ui.components.filter.DatePresetOption
import com.example.ui.components.filter.FilterCondition
import com.example.ui.components.filter.FilterEngine
import com.example.ui.components.filter.FilterField
import com.example.ui.components.filter.FilterSpec
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.SortOptionItem
import com.example.ui.components.filter.activeChips
import com.example.ui.components.filter.activeFilterCount
import com.example.ui.components.filter.apply
import com.example.ui.components.filter.clear
import com.example.ui.components.filter.count
import com.example.ui.components.filter.isActive
import com.example.ui.components.filter.removeActiveChip
import com.example.ui.components.filter.toBudgetMakerDashboardFilter
import com.example.ui.components.filter.toBudgetMakerTabFilter
import com.example.ui.components.filter.toFilterState
import com.example.ui.components.filter.toggleCondition
import com.example.util.FilterStore
import com.example.data.model.LanguageMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UnifiedFilterTest {

    data class DummyItem(
        val id: String,
        val amount: Double,
        val category: String,
        val dateEpochMs: Long,
        val isFlagged: Boolean = false
    )

    @Test
    fun testAndLogicAcrossFields() {
        val items = listOf(
            DummyItem("1", 500.0, "Food", 1000L),
            DummyItem("2", 1500.0, "Food", 2000L),
            DummyItem("3", 2500.0, "Transport", 3000L),
            DummyItem("4", 50.0, "Utilities", 4000L)
        )

        val spec = FilterSpec<DummyItem>(
            key = "test_spec",
            titleEn = "Test Filter",
            titleBn = "টেস্ট ফিল্টার",
            fields = listOf(
                FilterField.SelectField(
                    id = "category",
                    titleEn = "Category",
                    titleBn = "ক্যাটাগরি",
                    predicate = { item, selectedIds -> item.category in selectedIds }
                ),
                FilterField.RangeField(
                    id = "amount",
                    titleEn = "Amount",
                    titleBn = "পরিমাণ",
                    predicate = { item, min, max ->
                        (min == null || item.amount >= min) && (max == null || item.amount <= max)
                    }
                )
            )
        )

        val state = mapOf(
            "category" to FilterValue.Select(setOf("Food")),
            "amount" to FilterValue.Range(min = 1000.0, max = 2000.0)
        )

        val filtered = spec.apply(items, state)
        assertEquals(1, filtered.size)
        assertEquals("2", filtered.first().id)
        assertEquals(1, spec.count(items, state))
    }

    @Test
    fun testToggleConditionMutualExclusivity() {
        val spec = FilterSpec<DummyItem>(
            key = "toggle_spec",
            titleEn = "Toggles",
            titleBn = "টগলস",
            fields = listOf(
                FilterField.ToggleGroupField(
                    id = "conds",
                    titleEn = "Conditions",
                    titleBn = "শর্ত",
                    conditions = listOf(
                        FilterCondition(
                            id = "over_budget",
                            titleEn = "Over Budget",
                            titleBn = "বাজেট অতিক্রম",
                            group = "budget_status"
                        ),
                        FilterCondition(
                            id = "under_budget",
                            titleEn = "Under Budget",
                            titleBn = "বাজেট বাকি",
                            group = "budget_status"
                        ),
                        FilterCondition(
                            id = "has_activity",
                            titleEn = "Has Activity",
                            titleBn = "লেনদেন আছে",
                            group = "activity"
                        )
                    )
                )
            )
        )

        var state = emptyMap<String, FilterValue>()

        // Toggle on over_budget
        state = spec.toggleCondition(state, "conds", "over_budget")
        val active1 = (state["conds"] as FilterValue.ToggleGroup).activeIds
        assertTrue(active1.contains("over_budget"))

        // Toggle on under_budget in same group -> over_budget must be removed!
        state = spec.toggleCondition(state, "conds", "under_budget")
        val active2 = (state["conds"] as FilterValue.ToggleGroup).activeIds
        assertFalse(active2.contains("over_budget"))
        assertTrue(active2.contains("under_budget"))

        // Toggle on has_activity in different group -> under_budget should remain!
        state = spec.toggleCondition(state, "conds", "has_activity")
        val active3 = (state["conds"] as FilterValue.ToggleGroup).activeIds
        assertTrue(active3.contains("under_budget"))
        assertTrue(active3.contains("has_activity"))

        // Toggle off under_budget
        state = spec.toggleCondition(state, "conds", "under_budget")
        val active4 = (state["conds"] as FilterValue.ToggleGroup).activeIds
        assertFalse(active4.contains("under_budget"))
        assertTrue(active4.contains("has_activity"))
    }

    @Test
    fun testDatePresetsAndCustomRanges() {
        val items = listOf(
            DummyItem("1", 100.0, "Food", 1000L),
            DummyItem("2", 200.0, "Food", 5000L),
            DummyItem("3", 300.0, "Transport", 9000L)
        )

        val spec = FilterSpec<DummyItem>(
            key = "date_spec",
            titleEn = "Date Filter",
            titleBn = "তারিখ ফিল্টার",
            fields = listOf(
                FilterField.DateField(
                    id = "date_field",
                    titleEn = "Date Range",
                    titleBn = "তারিখ সীমা",
                    presets = listOf(
                        DatePresetOption("preset_mid", "Mid Range", "মাঝামাঝি", { Pair(2000L, 6000L) })
                    ),
                    predicate = { item, startMs, endMs ->
                        item.dateEpochMs in startMs..endMs
                    }
                )
            )
        )

        // 1. Test preset
        val presetState = mapOf("date_field" to FilterValue.Date(presetId = "preset_mid"))
        val presetFiltered = spec.apply(items, presetState)
        assertEquals(1, presetFiltered.size)
        assertEquals("2", presetFiltered.first().id)

        // 2. Test custom range
        val customState = mapOf("date_field" to FilterValue.Date(startMs = 4000L, endMs = 10000L))
        val customFiltered = spec.apply(items, customState)
        assertEquals(2, customFiltered.size)
        assertEquals(setOf("2", "3"), customFiltered.map { it.id }.toSet())

        // 3. Test active chips for custom range
        val chips = spec.activeChips(customState, LanguageMode.ENGLISH)
        assertEquals(1, chips.size)
        assertTrue(chips.first().label.contains("-"))
    }

    @Test
    fun testSortAppliedAfterAllFilters() {
        val items = listOf(
            DummyItem("1", 100.0, "Food", 1000L),
            DummyItem("2", 300.0, "Food", 2000L),
            DummyItem("3", 200.0, "Food", 3000L),
            DummyItem("4", 400.0, "Transport", 4000L)
        )

        // Put SortField FIRST in spec fields to verify engine executes sort AFTER all predicates
        val spec = FilterSpec<DummyItem>(
            key = "sort_spec",
            titleEn = "Sort Test",
            titleBn = "সর্ট টেস্ট",
            fields = listOf(
                FilterField.SortField<DummyItem>(
                    id = "sort_field",
                    titleEn = "Sort",
                    titleBn = "সাজান",
                    options = listOf(
                        SortOptionItem("amount_desc", "Amount High to Low", "পরিমাণ বেশি থেকে কম", compareByDescending<DummyItem> { it.amount }),
                        SortOptionItem("amount_asc", "Amount Low to High", "পরিমাণ কম থেকে বেশি", compareBy<DummyItem> { it.amount })
                    )
                ),
                FilterField.SelectField(
                    id = "category",
                    titleEn = "Category",
                    titleBn = "ক্যাটাগরি",
                    predicate = { item, selectedIds -> item.category in selectedIds }
                )
            )
        )

        val state = mapOf(
            "category" to FilterValue.Select(setOf("Food")),
            "sort_field" to FilterValue.Sort("amount_desc")
        )

        val result = spec.apply(items, state)
        assertEquals(3, result.size)
        assertEquals("2", result[0].id) // 300.0
        assertEquals("3", result[1].id) // 200.0
        assertEquals("1", result[2].id) // 100.0
    }

    @Test
    fun testClearSingleFieldAndClearAll() {
        val spec = FilterSpec<DummyItem>(
            key = "clear_spec",
            titleEn = "Clear Test",
            titleBn = "ক্লিয়ার টেস্ট",
            fields = listOf(
                FilterField.SelectField(id = "cat", titleEn = "Cat", titleBn = "ক্যাট"),
                FilterField.RangeField(id = "amt", titleEn = "Amt", titleBn = "পরিমাণ")
            )
        )

        val state = mapOf(
            "cat" to FilterValue.Select(setOf("Food")),
            "amt" to FilterValue.Range(min = 100.0, max = 500.0)
        )

        assertEquals(2, spec.activeFilterCount(state))
        assertTrue(spec.isActive(state))

        // Clear single field
        val stateAfterSingleClear = spec.clear(state, "cat")
        assertEquals(1, spec.activeFilterCount(stateAfterSingleClear))
        assertNull(stateAfterSingleClear["cat"])
        assertNotNull(stateAfterSingleClear["amt"])

        // Clear all
        val stateAfterAllClear = spec.clear(stateAfterSingleClear)
        assertEquals(0, spec.activeFilterCount(stateAfterAllClear))
        assertFalse(spec.isActive(stateAfterAllClear))
    }

    @Test
    fun testBudgetMakerTabFilterConverters() {
        val tabFilter = BudgetMakerTabFilter(
            selectedItemIds = setOf(101L, 102L),
            minAmount = 500.0,
            maxAmount = 2500.0,
            onlyOverBudget = true,
            onlyWithActualActivity = true,
            excludeZeroAmounts = true
        )

        val state = tabFilter.toFilterState(1)
        val restored = state.toBudgetMakerTabFilter()

        assertEquals(tabFilter.selectedItemIds, restored.selectedItemIds)
        assertEquals(tabFilter.minAmount, restored.minAmount)
        assertEquals(tabFilter.maxAmount, restored.maxAmount)
        assertEquals(tabFilter.onlyOverBudget, restored.onlyOverBudget)
        assertEquals(tabFilter.onlyWithActualActivity, restored.onlyWithActualActivity)
        assertEquals(tabFilter.excludeZeroAmounts, restored.excludeZeroAmounts)
    }

    @Test
    fun testBudgetMakerDashboardFilterConverters() {
        val dashFilter = BudgetMakerDashboardFilter(
            includeExpenses = true,
            includeIncomes = false,
            includeAssets = true,
            includeLiabilities = false,
            onlyNonZero = true
        )

        val state = dashFilter.toFilterState()
        val restored = state.toBudgetMakerDashboardFilter()

        assertEquals(dashFilter.includeExpenses, restored.includeExpenses)
        assertEquals(dashFilter.includeIncomes, restored.includeIncomes)
        assertEquals(dashFilter.includeAssets, restored.includeAssets)
        assertEquals(dashFilter.includeLiabilities, restored.includeLiabilities)
        assertEquals(dashFilter.onlyNonZero, restored.onlyNonZero)
    }

    @Test
    fun testFilterStoreSaveLoadRoundTrip() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val filterStore = FilterStore.getInstance(context)

        val originalState = mapOf(
            "select" to FilterValue.Select(setOf("id1", "id2")),
            "single_select" to FilterValue.SingleSelect("single_id"),
            "range" to FilterValue.Range(min = 100.5, max = 500.75),
            "date" to FilterValue.Date(presetId = "last_30_days", startMs = 1000L, endMs = 2000L),
            "toggle" to FilterValue.ToggleGroup(setOf("cond1", "cond2")),
            "sort" to FilterValue.Sort("sort_asc"),
            "bool" to FilterValue.BooleanVal(true),
            "custom" to FilterValue.Custom("{\"key\":\"val\"}")
        )

        val testKey = "test_roundtrip_spec"
        filterStore.saveFilterState(testKey, originalState)

        val loadedState = filterStore.loadFilterState(testKey)
        assertEquals(originalState.size, loadedState.size)
        assertEquals((originalState["select"] as FilterValue.Select).selectedIds, (loadedState["select"] as FilterValue.Select).selectedIds)
        assertEquals((originalState["single_select"] as FilterValue.SingleSelect).selectedId, (loadedState["single_select"] as FilterValue.SingleSelect).selectedId)
        assertEquals((originalState["range"] as FilterValue.Range).min, (loadedState["range"] as FilterValue.Range).min)
        assertEquals((originalState["range"] as FilterValue.Range).max, (loadedState["range"] as FilterValue.Range).max)
        assertEquals((originalState["date"] as FilterValue.Date).presetId, (loadedState["date"] as FilterValue.Date).presetId)
        assertEquals((originalState["date"] as FilterValue.Date).startMs, (loadedState["date"] as FilterValue.Date).startMs)
        assertEquals((originalState["date"] as FilterValue.Date).endMs, (loadedState["date"] as FilterValue.Date).endMs)
        assertEquals((originalState["toggle"] as FilterValue.ToggleGroup).activeIds, (loadedState["toggle"] as FilterValue.ToggleGroup).activeIds)
        assertEquals((originalState["sort"] as FilterValue.Sort).sortId, (loadedState["sort"] as FilterValue.Sort).sortId)
        assertEquals((originalState["bool"] as FilterValue.BooleanVal).value, (loadedState["bool"] as FilterValue.BooleanVal).value)
        assertEquals((originalState["custom"] as FilterValue.Custom).rawJson, (loadedState["custom"] as FilterValue.Custom).rawJson)

        filterStore.clearFilterState(testKey)
        val afterClear = filterStore.loadFilterState(testKey)
        assertTrue(afterClear.isEmpty())
    }
}
