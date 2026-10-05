package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.CategoryType
import com.example.data.model.LanguageMode
import com.example.ui.components.filter.FilterState
import com.example.ui.components.filter.FilterValue
import com.example.ui.components.filter.activeChips
import com.example.ui.components.filter.activeFilterCount
import com.example.ui.components.filter.isActive
import com.example.ui.components.filter.specs.CategoriesFilterSpec
import com.example.ui.components.filter.specs.LabelsFilterSpec
import com.example.ui.components.filter.toggleCondition
import com.example.ui.components.filter.withSearchQuery
import com.example.ui.screens.CategorySortFilter
import com.example.ui.screens.CategoryViewHierarchyFilter
import com.example.util.FilterStore
import com.example.util.TabFilterPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SimpleScreensFilterSpecTest {

    @Test
    fun testCategoriesFilterSpecFiltersAndToggles() {
        val spec = CategoriesFilterSpec.createSpec()
        assertEquals(CategoriesFilterSpec.SPEC_KEY, spec.key)

        var state: FilterState = emptyMap()
        assertFalse(spec.isActive(state))
        assertEquals(0, spec.activeFilterCount(state))

        // 1. Search Query
        state = state.withSearchQuery("Food", CategoriesFilterSpec.FIELD_SEARCH)
        assertTrue(spec.isActive(state))
        assertEquals("Food", CategoriesFilterSpec.getSearchQuery(state))
        assertEquals(1, spec.activeFilterCount(state))

        // 2. Type mode toggles
        state = spec.toggleCondition(state, CategoriesFilterSpec.FIELD_TYPE_MODE, CategoriesFilterSpec.TYPE_EXPENSE)
        assertEquals(CategoryType.EXPENSE, CategoriesFilterSpec.getTypeFilter(state))

        state = spec.toggleCondition(state, CategoriesFilterSpec.FIELD_TYPE_MODE, CategoriesFilterSpec.TYPE_INCOME)
        assertEquals(CategoryType.INCOME, CategoriesFilterSpec.getTypeFilter(state))

        state = spec.toggleCondition(state, CategoriesFilterSpec.FIELD_TYPE_MODE, CategoriesFilterSpec.TYPE_ALL)
        assertEquals(null, CategoriesFilterSpec.getTypeFilter(state))

        // 3. Hierarchy toggles
        state = spec.toggleCondition(state, CategoriesFilterSpec.FIELD_HIERARCHY, CategoriesFilterSpec.HIERARCHY_ONLY_GROUPS)
        assertEquals(CategoryViewHierarchyFilter.ONLY_GROUPS, CategoriesFilterSpec.getHierarchyFilter(state))

        state = spec.toggleCondition(state, CategoriesFilterSpec.FIELD_HIERARCHY, CategoriesFilterSpec.HIERARCHY_ONLY_CATEGORIES)
        assertEquals(CategoryViewHierarchyFilter.ONLY_CATEGORIES, CategoriesFilterSpec.getHierarchyFilter(state))

        // 4. Sort selection
        state = state + (CategoriesFilterSpec.FIELD_SORT to FilterValue.Sort("budget_high_to_low"))
        assertEquals(CategorySortFilter.BUDGET_HIGH_TO_LOW, CategoriesFilterSpec.getSortFilter(state))

        // 5. Active chips
        val chips = spec.activeChips(state, LanguageMode.ENGLISH)
        assertTrue(chips.any { it.label.contains("Food") })
        assertTrue(chips.any { it.label.contains("Categories") || it.label.contains("Groups") })
    }

    @Test
    fun testLabelsFilterSpecFiltersAndToggles() {
        val categories = listOf(Category(id = 1L, nameEn = "Dining", nameBn = "খাবার", type = CategoryType.EXPENSE))
        val accounts = listOf(Account(id = 10L, nameEn = "Cash", nameBn = "নগদ", type = com.example.data.model.AccountType.ASSET, initialBalance = 1000.0))
        val spec = LabelsFilterSpec.createSpec(categories, accounts)
        assertEquals(LabelsFilterSpec.SPEC_KEY, spec.key)

        var state: FilterState = emptyMap()
        assertFalse(spec.isActive(state))

        // 1. Search Query
        state = state.withSearchQuery("#groceries", LabelsFilterSpec.FIELD_SEARCH)
        assertEquals("#groceries", LabelsFilterSpec.getSearchQuery(state))

        // 2. Segment selection
        state = spec.toggleCondition(state, LabelsFilterSpec.FIELD_SEGMENT, LabelsFilterSpec.SEGMENT_NOTES)
        assertEquals(LabelsFilterSpec.SEGMENT_NOTES, LabelsFilterSpec.getSelectedSegmentId(state))

        state = spec.toggleCondition(state, LabelsFilterSpec.FIELD_SEGMENT, LabelsFilterSpec.SEGMENT_PAYEES)
        assertEquals(LabelsFilterSpec.SEGMENT_PAYEES, LabelsFilterSpec.getSelectedSegmentId(state))

        // 3. Type mode
        state = spec.toggleCondition(state, LabelsFilterSpec.FIELD_TYPE_MODE, LabelsFilterSpec.MODE_EXPENSE)
        assertEquals(LabelsFilterSpec.MODE_EXPENSE, LabelsFilterSpec.getTypeModeId(state))

        // 4. Date bounds
        val bounds = LabelsFilterSpec.resolveDateBounds(spec, state)
        assertTrue(bounds.second > bounds.first)

        // 5. Range & Sort
        state = state + (LabelsFilterSpec.FIELD_AMOUNT_RANGE to FilterValue.Range(100.0, 500.0))
        state = state + (LabelsFilterSpec.FIELD_SORT to FilterValue.Sort("name_asc"))
        assertEquals("name_asc", LabelsFilterSpec.getSortOrderId(state))

        val chips = spec.activeChips(state, LanguageMode.ENGLISH)
        assertTrue(chips.isNotEmpty())
    }

    @Test
    fun testFilterStoreSaveAndLoadForSimpleScreens() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = FilterStore.getInstance(context)

        // Test Categories state save/load round-trip
        val catState: FilterState = mapOf(
            CategoriesFilterSpec.FIELD_SEARCH to FilterValue.Search("groceries"),
            CategoriesFilterSpec.FIELD_TYPE_MODE to FilterValue.ToggleGroup(setOf(CategoriesFilterSpec.TYPE_EXPENSE)),
            CategoriesFilterSpec.FIELD_HIERARCHY to FilterValue.ToggleGroup(setOf(CategoriesFilterSpec.HIERARCHY_ONLY_CATEGORIES)),
            CategoriesFilterSpec.FIELD_SORT to FilterValue.Sort("budget_high_to_low")
        )
        store.saveFilterState(CategoriesFilterSpec.SPEC_KEY, catState)
        val loadedCat = store.loadFilterState(CategoriesFilterSpec.SPEC_KEY)

        assertEquals("groceries", (loadedCat[CategoriesFilterSpec.FIELD_SEARCH] as FilterValue.Search).query)
        assertEquals(setOf(CategoriesFilterSpec.TYPE_EXPENSE), (loadedCat[CategoriesFilterSpec.FIELD_TYPE_MODE] as FilterValue.ToggleGroup).activeIds)
        assertEquals(setOf(CategoriesFilterSpec.HIERARCHY_ONLY_CATEGORIES), (loadedCat[CategoriesFilterSpec.FIELD_HIERARCHY] as FilterValue.ToggleGroup).activeIds)
        assertEquals("budget_high_to_low", (loadedCat[CategoriesFilterSpec.FIELD_SORT] as FilterValue.Sort).sortId)

        // Test Labels state save/load round-trip
        val labelsState: FilterState = mapOf(
            LabelsFilterSpec.FIELD_SEARCH to FilterValue.Search("#dinner"),
            LabelsFilterSpec.FIELD_SEGMENT to FilterValue.ToggleGroup(setOf(LabelsFilterSpec.SEGMENT_HASHTAGS)),
            LabelsFilterSpec.FIELD_TYPE_MODE to FilterValue.ToggleGroup(setOf(LabelsFilterSpec.MODE_INCOME)),
            LabelsFilterSpec.FIELD_AMOUNT_RANGE to FilterValue.Range(50.0, 500.0),
            LabelsFilterSpec.FIELD_SORT to FilterValue.Sort("amount_asc")
        )
        store.saveFilterState(LabelsFilterSpec.SPEC_KEY, labelsState)
        val loadedLabels = store.loadFilterState(LabelsFilterSpec.SPEC_KEY)

        assertEquals("#dinner", (loadedLabels[LabelsFilterSpec.FIELD_SEARCH] as FilterValue.Search).query)
        assertEquals(setOf(LabelsFilterSpec.SEGMENT_HASHTAGS), (loadedLabels[LabelsFilterSpec.FIELD_SEGMENT] as FilterValue.ToggleGroup).activeIds)
        assertEquals(setOf(LabelsFilterSpec.MODE_INCOME), (loadedLabels[LabelsFilterSpec.FIELD_TYPE_MODE] as FilterValue.ToggleGroup).activeIds)
        assertEquals(50.0, (loadedLabels[LabelsFilterSpec.FIELD_AMOUNT_RANGE] as FilterValue.Range).min)
        assertEquals(500.0, (loadedLabels[LabelsFilterSpec.FIELD_AMOUNT_RANGE] as FilterValue.Range).max)
        assertEquals("amount_asc", (loadedLabels[LabelsFilterSpec.FIELD_SORT] as FilterValue.Sort).sortId)
    }

    @Test
    fun testMigrationFromTabFilterPreferencesForLabelsAndCategories() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tabFilterPrefs = TabFilterPreferences.getInstance(context)
        val store = FilterStore.getInstance(context)

        // Clean up store keys
        store.clearFilterState(LabelsFilterSpec.SPEC_KEY)
        store.clearFilterState(CategoriesFilterSpec.SPEC_KEY)
        context.getSharedPreferences("filter_store_prefs", Context.MODE_PRIVATE).edit().clear().apply()

        tabFilterPrefs.labelsSearchQuery = "Lunch"
        tabFilterPrefs.labelsTabMode = "EXPENSE"
        tabFilterPrefs.labelsCategorySegment = "NOTES"
        tabFilterPrefs.categoriesSearchQuery = "Shopping"
        tabFilterPrefs.categoriesTypeFilter = CategoryType.EXPENSE

        store.migrateFromTabFilterPreferences(tabFilterPrefs)

        val migratedLabels = store.loadFilterState(LabelsFilterSpec.SPEC_KEY)
        assertEquals("Lunch", (migratedLabels[LabelsFilterSpec.FIELD_SEARCH] as FilterValue.Search).query)
        assertEquals(setOf("expense"), (migratedLabels[LabelsFilterSpec.FIELD_TYPE_MODE] as FilterValue.ToggleGroup).activeIds)
        assertEquals(setOf("notes"), (migratedLabels[LabelsFilterSpec.FIELD_SEGMENT] as FilterValue.ToggleGroup).activeIds)

        val migratedCategories = store.loadFilterState(CategoriesFilterSpec.SPEC_KEY)
        assertEquals("Shopping", (migratedCategories[CategoriesFilterSpec.FIELD_SEARCH] as FilterValue.Search).query)
        assertEquals(setOf(CategoriesFilterSpec.TYPE_EXPENSE), (migratedCategories[CategoriesFilterSpec.FIELD_TYPE_MODE] as FilterValue.ToggleGroup).activeIds)
    }
}
