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
import com.example.ui.components.filter.specs.AccountsFilterSpec
import com.example.ui.components.filter.specs.CategoriesFilterSpec
import com.example.ui.components.filter.specs.ItemsFilterSpec
import com.example.ui.components.filter.specs.LabelsFilterSpec
import com.example.ui.components.filter.specs.PaymentSourceFilterSpec
import com.example.ui.components.filter.specs.RmManagerFilterSpec
import com.example.ui.components.filter.specs.SavingsGoalsFilterSpec
import com.example.ui.components.filter.specs.WishlistFilterSpec
import com.example.ui.components.filter.toggleCondition
import com.example.ui.components.filter.withSearchQuery
import com.example.ui.screens.AccountActiveStatusFilter
import com.example.ui.screens.AccountSortFilter
import com.example.ui.screens.AccountStatusFilter
import com.example.ui.screens.AccountViewHierarchyFilter
import com.example.ui.screens.AssignedItemSectionFilter
import com.example.ui.screens.AssignedItemSortOption
import com.example.ui.screens.AssignedItemStatusFilter
import com.example.ui.screens.CategorySortFilter
import com.example.ui.screens.CategoryViewHierarchyFilter
import com.example.ui.screens.GoalFilterType
import com.example.ui.screens.MainPaymentSourceTab
import com.example.ui.screens.PaymentSourceSortOption
import com.example.ui.screens.WishlistFilterTab
import com.example.ui.screens.WishlistSort
import com.example.data.model.RequirementCalculationBasis
import com.example.data.model.SavingsGoal
import com.example.ui.components.filter.specs.BalanceSheetFilterSpec
import com.example.ui.components.filter.specs.CashFlowFilterSpec
import com.example.ui.screens.BalanceSheetFilterState
import com.example.ui.screens.CashFlowTabSection
import com.example.util.BalanceSheetComparisonPreset
import com.example.util.BalanceSheetSortOrder
import com.example.util.CashFlowPeriodPreset
import com.example.data.model.TransactionType
import com.example.data.model.SavingsGoalWithDetails
import com.example.data.model.TransactionStatus
import com.example.data.model.WishlistItem
import com.example.data.model.WishlistItemWithCategory
import com.example.data.model.WishlistPriority
import com.example.data.model.WishlistTargetType
import com.example.util.FilterStore
import com.example.util.RmManagerHelper.RmFilterCategory
import com.example.util.RmManagerHelper.RmSortOption
import com.example.util.TabFilterPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.ui.components.filter.FilterField
import com.example.ui.dialogs.AggregatedDatePreset
import com.example.ui.dialogs.AggregatedSortOrder
import com.example.ui.components.filter.specs.NetEarningsFilterSpec
import com.example.ui.components.BudgetDateRangePreset
import com.example.ui.components.BudgetComparisonPreset
import com.example.ui.components.NetEarningsFlowScope
import com.example.ui.components.NetEarningsSortOrder
import com.example.ui.screens.NetEarningsHierarchyView
import com.example.ui.screens.NetEarningsSort
import com.example.data.model.TransactionWithDetails
import com.example.data.model.Transaction

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SimpleScreensFilterSpecTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("unified_filter_store", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("budgeter_tab_filter_sort_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        FilterStore.resetInstanceForTesting()
        TabFilterPreferences.resetInstanceForTesting()
    }

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

        // FilterValue.Search is stripped per Requirement 3
        assertNull(loadedCat[CategoriesFilterSpec.FIELD_SEARCH])
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

        // FilterValue.Search is stripped per Requirement 3
        assertNull(loadedLabels[LabelsFilterSpec.FIELD_SEARCH])
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

        // Clean up store keys and migration flags synchronously
        store.clearAllForTesting()

        tabFilterPrefs.labelsSearchQuery = "Lunch"
        tabFilterPrefs.labelsTabMode = "EXPENSE"
        tabFilterPrefs.labelsCategorySegment = "NOTES"
        tabFilterPrefs.categoriesSearchQuery = "Shopping"
        tabFilterPrefs.categoriesTypeFilter = CategoryType.EXPENSE

        store.migrateFromTabFilterPreferences(tabFilterPrefs)

        val migratedLabels = store.loadFilterState(LabelsFilterSpec.SPEC_KEY)
        // Search query must NOT be persisted or migrated per Requirement 3
        assertNull(migratedLabels[LabelsFilterSpec.FIELD_SEARCH])
        assertEquals(setOf("expense"), (migratedLabels[LabelsFilterSpec.FIELD_TYPE_MODE] as FilterValue.ToggleGroup).activeIds)
        assertEquals(setOf("notes"), (migratedLabels[LabelsFilterSpec.FIELD_SEGMENT] as FilterValue.ToggleGroup).activeIds)

        val migratedCategories = store.loadFilterState(CategoriesFilterSpec.SPEC_KEY)
        // Search query must NOT be persisted or migrated per Requirement 3
        assertNull(migratedCategories[CategoriesFilterSpec.FIELD_SEARCH])
        assertEquals(setOf(CategoriesFilterSpec.TYPE_EXPENSE), (migratedCategories[CategoriesFilterSpec.FIELD_TYPE_MODE] as FilterValue.ToggleGroup).activeIds)
    }

    @Test
    fun testEveryMigratedEnumValueExistsInMatchingSpecOrFallsBackToDefault() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tabFilterPrefs = TabFilterPreferences.getInstance(context)
        val store = FilterStore.getInstance(context)
        val prefs = context.getSharedPreferences("unified_filter_store", Context.MODE_PRIVATE)

        val labelsSpec = LabelsFilterSpec.createSpec()
        val validDatePresets = (labelsSpec.fields.first { it.id == LabelsFilterSpec.FIELD_DATE } as FilterField.DateField<*>).presets.map { it.id }.toSet()
        val validSorts = (labelsSpec.fields.first { it.id == LabelsFilterSpec.FIELD_SORT } as FilterField.SortField<*>).options.map { it.id }.toSet()
        val validSegments = (labelsSpec.fields.first { it.id == LabelsFilterSpec.FIELD_SEGMENT } as FilterField.ToggleGroupField<*>).conditions.map { it.id }.toSet()
        val validTabModes = (labelsSpec.fields.first { it.id == LabelsFilterSpec.FIELD_TYPE_MODE } as FilterField.ToggleGroupField<*>).conditions.map { it.id }.toSet()

        // 1. Verify every AggregatedDatePreset value maps to a valid preset or fallback
        for (preset in AggregatedDatePreset.values()) {
            store.clearFilterState(LabelsFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.labelsDatePreset = preset

            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(LabelsFilterSpec.SPEC_KEY)
            val dateVal = state[LabelsFilterSpec.FIELD_DATE] as FilterValue.Date
            assertNotNull(dateVal.presetId)
            assertTrue(
                "Date preset ID '${dateVal.presetId}' for enum $preset must exist in LabelsFilterSpec presets",
                validDatePresets.contains(dateVal.presetId)
            )
            if (preset == AggregatedDatePreset.CUSTOM) {
                assertEquals(FilterStore.DEFAULT_LABELS_DATE_PRESET, dateVal.presetId)
            } else {
                assertEquals(preset.name.lowercase(), dateVal.presetId)
            }
        }
        // Unknown preset fallback
        assertEquals(FilterStore.DEFAULT_LABELS_DATE_PRESET, FilterStore.mapLabelsDatePreset(null))

        // 2. Verify every AggregatedSortOrder value maps to a valid sort option or fallback
        for (sort in AggregatedSortOrder.values()) {
            store.clearFilterState(LabelsFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.labelsSortOrder = sort

            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(LabelsFilterSpec.SPEC_KEY)
            val sortVal = state[LabelsFilterSpec.FIELD_SORT] as FilterValue.Sort
            assertNotNull(sortVal.sortId)
            assertTrue(
                "Sort ID '${sortVal.sortId}' for enum $sort must exist in LabelsFilterSpec options",
                validSorts.contains(sortVal.sortId)
            )
            if (sort == AggregatedSortOrder.DEFAULT) {
                assertEquals(FilterStore.DEFAULT_LABELS_SORT_ORDER, sortVal.sortId)
            } else {
                assertEquals(sort.name.lowercase(), sortVal.sortId)
            }
        }
        // Unknown sort fallback
        assertEquals(FilterStore.DEFAULT_LABELS_SORT_ORDER, FilterStore.mapLabelsSortOrder(null))

        // 3. Verify Labels category segments and fallbacks
        val segmentsToTest = listOf("HASHTAGS", "NOTES", "PAYEES", "UNTAGGED", "ALL")
        for (seg in segmentsToTest) {
            val mapped = FilterStore.mapLabelsCategorySegment(seg)
            assertTrue("Segment '$mapped' must exist in LabelsFilterSpec conditions", validSegments.contains(mapped))
            assertEquals(seg.lowercase(), mapped)
        }
        assertEquals(FilterStore.DEFAULT_LABELS_CATEGORY_SEGMENT, FilterStore.mapLabelsCategorySegment("INVALID_SEGMENT"))

        // 4. Verify Labels tab modes and fallbacks
        val modesToTest = listOf("EXPENSE", "ALL", "INCOME")
        for (mode in modesToTest) {
            val mapped = FilterStore.mapLabelsTabMode(mode)
            assertTrue("Tab mode '$mapped' must exist in LabelsFilterSpec conditions", validTabModes.contains(mapped))
            assertEquals(mode.lowercase(), mapped)
        }
        assertEquals(FilterStore.DEFAULT_LABELS_TAB_MODE, FilterStore.mapLabelsTabMode("UNKNOWN_MODE"))

        // Categories Spec Verification
        val catSpec = CategoriesFilterSpec.createSpec()
        val validCatTypes = (catSpec.fields.first { it.id == CategoriesFilterSpec.FIELD_TYPE_MODE } as FilterField.ToggleGroupField<*>).conditions.map { it.id }.toSet()
        val validCatHierarchies = (catSpec.fields.first { it.id == CategoriesFilterSpec.FIELD_HIERARCHY } as FilterField.ToggleGroupField<*>).conditions.map { it.id }.toSet()
        val validCatSorts = (catSpec.fields.first { it.id == CategoriesFilterSpec.FIELD_SORT } as FilterField.SortField<*>).options.map { it.id }.toSet()

        // 5. Verify Categories types
        assertEquals(CategoriesFilterSpec.TYPE_EXPENSE, FilterStore.mapCategoriesType(CategoryType.EXPENSE))
        assertEquals(CategoriesFilterSpec.TYPE_INCOME, FilterStore.mapCategoriesType(CategoryType.INCOME))
        assertEquals(CategoriesFilterSpec.TYPE_ALL, FilterStore.mapCategoriesType(null))
        assertTrue(validCatTypes.contains(FilterStore.mapCategoriesType(CategoryType.EXPENSE)))
        assertTrue(validCatTypes.contains(FilterStore.mapCategoriesType(CategoryType.INCOME)))
        assertTrue(validCatTypes.contains(FilterStore.mapCategoriesType(null)))

        // 6. Verify every CategoryViewHierarchyFilter enum value
        for (h in CategoryViewHierarchyFilter.values()) {
            val mapped = FilterStore.mapCategoriesHierarchy(h)
            assertTrue("Hierarchy '$mapped' must exist in CategoriesFilterSpec conditions", validCatHierarchies.contains(mapped))
            assertEquals(h.name.lowercase(), mapped)
        }
        assertEquals(FilterStore.DEFAULT_CATEGORIES_HIERARCHY, FilterStore.mapCategoriesHierarchy(null))

        // 7. Verify every CategorySortFilter enum value
        for (s in CategorySortFilter.values()) {
            val mapped = FilterStore.mapCategoriesSort(s)
            assertTrue("Sort '$mapped' must exist in CategoriesFilterSpec options", validCatSorts.contains(mapped))
            assertEquals(s.name.lowercase(), mapped)
        }
        assertEquals(FilterStore.DEFAULT_CATEGORIES_SORT, FilterStore.mapCategoriesSort(null))
    }

    @Test
    fun testPerScreenMigrationFlagsAndNoOverwriteOfExistingFilterStoreState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tabFilterPrefs = TabFilterPreferences.getInstance(context)
        val store = FilterStore.getInstance(context)
        val prefs = context.getSharedPreferences("unified_filter_store", Context.MODE_PRIVATE)

        // Clear all state
        store.clearAllForTesting()

        // User already configured a custom FilterStore state for Categories prior to migration
        val existingCatState = mapOf(
            CategoriesFilterSpec.FIELD_HIERARCHY to FilterValue.ToggleGroup(setOf(CategoriesFilterSpec.HIERARCHY_ONLY_GROUPS)),
            CategoriesFilterSpec.FIELD_SORT to FilterValue.Sort("most_used")
        )
        store.saveFilterState(CategoriesFilterSpec.SPEC_KEY, existingCatState)

        // TabFilterPreferences has different values
        tabFilterPrefs.categoriesHierarchyFilter = CategoryViewHierarchyFilter.ALL
        tabFilterPrefs.categoriesSortFilter = CategorySortFilter.DEFAULT
        tabFilterPrefs.labelsCategorySegment = "PAYEES"

        // Simulate legacy migration flag being present (e.g. from an earlier release)
        prefs.edit().putBoolean("migrated_tab_filter_preferences_v1", true).commit()

        // Run migration
        store.migrateFromTabFilterPreferences(tabFilterPrefs)

        // 1. Separate per-screen flags should be set
        assertTrue("migrated_labels_v1 flag must be true", prefs.getBoolean(FilterStore.KEY_MIGRATED_LABELS, false))
        assertTrue("migrated_categories_v1 flag must be true", prefs.getBoolean(FilterStore.KEY_MIGRATED_CATEGORIES, false))

        // 2. Labels should have been migrated even though legacy flag was true
        val migratedLabels = store.loadFilterState(LabelsFilterSpec.SPEC_KEY)
        assertEquals(setOf("payees"), (migratedLabels[LabelsFilterSpec.FIELD_SEGMENT] as FilterValue.ToggleGroup).activeIds)

        // 3. Existing Categories state must NOT be overwritten!
        val categoriesState = store.loadFilterState(CategoriesFilterSpec.SPEC_KEY)
        assertEquals(
            setOf(CategoriesFilterSpec.HIERARCHY_ONLY_GROUPS),
            (categoriesState[CategoriesFilterSpec.FIELD_HIERARCHY] as FilterValue.ToggleGroup).activeIds
        )
        assertEquals(
            "most_used",
            (categoriesState[CategoriesFilterSpec.FIELD_SORT] as FilterValue.Sort).sortId
        )
    }

    @Test
    fun testWishlistFilterSpecFiltersTogglesAndSorting() {
        val categories = listOf(Category(id = 1L, nameEn = "Tech", nameBn = "প্রযুক্তি", type = CategoryType.EXPENSE))
        val spec = WishlistFilterSpec.createSpec(categories)
        assertEquals(WishlistFilterSpec.SPEC_KEY, spec.key)

        var state: FilterState = emptyMap()
        assertFalse(spec.isActive(state))
        assertEquals(0, spec.activeFilterCount(state))

        // 1. Search Query
        state = state.withSearchQuery("Laptop", WishlistFilterSpec.FIELD_SEARCH)
        assertTrue(spec.isActive(state))
        assertEquals("Laptop", WishlistFilterSpec.getSearchQuery(state))

        // 2. Tab toggles
        state = spec.toggleCondition(state, WishlistFilterSpec.FIELD_TAB, WishlistFilterSpec.TAB_NEXT_MONTH)
        assertEquals(WishlistFilterTab.NEXT_MONTH, WishlistFilterSpec.getTab(state))

        state = spec.toggleCondition(state, WishlistFilterSpec.FIELD_TAB, WishlistFilterSpec.TAB_PURCHASED)
        assertEquals(WishlistFilterTab.PURCHASED, WishlistFilterSpec.getTab(state))

        // 3. Priority and Category Selection
        state = state + (WishlistFilterSpec.FIELD_PRIORITY to FilterValue.Select(setOf(WishlistFilterSpec.PRIORITY_HIGH)))
        state = state + (WishlistFilterSpec.FIELD_CATEGORIES to FilterValue.Select(setOf("1")))

        // 4. Sort selection
        state = state + (WishlistFilterSpec.FIELD_SORT to FilterValue.Sort(WishlistFilterSpec.SORT_AMOUNT_DESC))
        assertEquals(WishlistSort.AMOUNT_DESC, WishlistFilterSpec.getSort(state))

        // 5. Active chips
        val chips = spec.activeChips(state, LanguageMode.ENGLISH)
        assertTrue(chips.any { it.label.contains("Laptop") })

        // 6. filterItems execution
        val item1 = WishlistItemWithCategory(
            item = WishlistItem(id = 1L, title = "Gaming Laptop", estimatedAmount = 1500.0, priority = WishlistPriority.HIGH, isPurchased = false, targetType = WishlistTargetType.NEXT_MONTH, categoryId = 1L),
            category = categories[0]
        )
        val item2 = WishlistItemWithCategory(
            item = WishlistItem(id = 2L, title = "Book", estimatedAmount = 20.0, priority = WishlistPriority.LOW, isPurchased = false),
            category = null
        )
        val filtered = WishlistFilterSpec.filterItems(listOf(item1, item2), state, 2026, 11)
        assertEquals(0, filtered.size) // state has TAB_PURCHASED

        val activeState = mapOf(
            WishlistFilterSpec.FIELD_TAB to FilterValue.ToggleGroup(setOf(WishlistFilterSpec.TAB_ACTIVE)),
            WishlistFilterSpec.FIELD_SEARCH to FilterValue.Search("laptop")
        )
        val filteredActive = WishlistFilterSpec.filterItems(listOf(item1, item2), activeState, 2026, 11)
        assertEquals(1, filteredActive.size)
        assertEquals("Gaming Laptop", filteredActive[0].item.title)
    }

    @Test
    fun testSavingsGoalsFilterSpecFiltersTogglesAndSorting() {
        val spec = SavingsGoalsFilterSpec.createSpec()
        assertEquals(SavingsGoalsFilterSpec.SPEC_KEY, spec.key)

        var state: FilterState = emptyMap()
        assertFalse(spec.isActive(state))
        assertEquals(0, spec.activeFilterCount(state))

        // 1. Search Query
        state = state.withSearchQuery("Emergency", SavingsGoalsFilterSpec.FIELD_SEARCH)
        assertTrue(spec.isActive(state))
        assertEquals("Emergency", SavingsGoalsFilterSpec.getSearchQuery(state))

        // 2. Status toggles
        state = spec.toggleCondition(state, SavingsGoalsFilterSpec.FIELD_STATUS, SavingsGoalsFilterSpec.STATUS_ACTIVE)
        assertEquals(GoalFilterType.ACTIVE, SavingsGoalsFilterSpec.getStatus(state))

        state = spec.toggleCondition(state, SavingsGoalsFilterSpec.FIELD_STATUS, SavingsGoalsFilterSpec.STATUS_COMPLETED)
        assertEquals(GoalFilterType.COMPLETED, SavingsGoalsFilterSpec.getStatus(state))

        state = spec.toggleCondition(state, SavingsGoalsFilterSpec.FIELD_STATUS, SavingsGoalsFilterSpec.STATUS_DEFICIT)
        assertEquals(GoalFilterType.DEFICIT, SavingsGoalsFilterSpec.getStatus(state))

        state = spec.toggleCondition(state, SavingsGoalsFilterSpec.FIELD_STATUS, SavingsGoalsFilterSpec.STATUS_ALL)
        assertEquals(GoalFilterType.ALL, SavingsGoalsFilterSpec.getStatus(state))

        // 3. Sort selection
        state = state + (SavingsGoalsFilterSpec.FIELD_SORT to FilterValue.Sort(SavingsGoalsFilterSpec.SORT_TARGET_DESC))
        assertEquals(SavingsGoalsFilterSpec.SORT_TARGET_DESC, SavingsGoalsFilterSpec.getSort(state))

        // 4. filterGoals execution
        val goal1 = SavingsGoalWithDetails(
            goal = SavingsGoal(id = 1L, name = "Emergency Fund", targetAmount = 5000.0, isCompleted = false)
        )
        val goal2 = SavingsGoalWithDetails(
            goal = SavingsGoal(id = 2L, name = "Vacation", targetAmount = 1000.0, isCompleted = true)
        )
        val activeOnlyState = mapOf(
            SavingsGoalsFilterSpec.FIELD_STATUS to FilterValue.ToggleGroup(setOf(SavingsGoalsFilterSpec.STATUS_ACTIVE))
        )
        val filteredActive = SavingsGoalsFilterSpec.filterGoals(listOf(goal1, goal2), activeOnlyState)
        assertEquals(1, filteredActive.size)
        assertEquals("Emergency Fund", filteredActive[0].goal.name)
    }

    @Test
    fun testFilterStoreSaveAndLoadForWishlistAndSavingsGoals() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = FilterStore.getInstance(context)

        // 1. Test Wishlist state save/load round-trip
        val wishlistState: FilterState = mapOf(
            WishlistFilterSpec.FIELD_SEARCH to FilterValue.Search("Headphones"),
            WishlistFilterSpec.FIELD_TAB to FilterValue.ToggleGroup(setOf(WishlistFilterSpec.TAB_SAVINGS_GOALS)),
            WishlistFilterSpec.FIELD_PRIORITY to FilterValue.Select(setOf(WishlistFilterSpec.PRIORITY_HIGH)),
            WishlistFilterSpec.FIELD_SORT to FilterValue.Sort(WishlistFilterSpec.SORT_AMOUNT_DESC)
        )
        store.saveFilterState(WishlistFilterSpec.SPEC_KEY, wishlistState)
        val loadedWishlist = store.loadFilterState(WishlistFilterSpec.SPEC_KEY)

        // FilterValue.Search is stripped
        assertNull(loadedWishlist[WishlistFilterSpec.FIELD_SEARCH])
        assertEquals(setOf(WishlistFilterSpec.TAB_SAVINGS_GOALS), (loadedWishlist[WishlistFilterSpec.FIELD_TAB] as FilterValue.ToggleGroup).activeIds)
        assertEquals(setOf(WishlistFilterSpec.PRIORITY_HIGH), (loadedWishlist[WishlistFilterSpec.FIELD_PRIORITY] as FilterValue.Select).selectedIds)
        assertEquals(WishlistFilterSpec.SORT_AMOUNT_DESC, (loadedWishlist[WishlistFilterSpec.FIELD_SORT] as FilterValue.Sort).sortId)

        // 2. Test SavingsGoals state save/load round-trip
        val savingsState: FilterState = mapOf(
            SavingsGoalsFilterSpec.FIELD_SEARCH to FilterValue.Search("Car"),
            SavingsGoalsFilterSpec.FIELD_STATUS to FilterValue.ToggleGroup(setOf(SavingsGoalsFilterSpec.STATUS_ACTIVE)),
            SavingsGoalsFilterSpec.FIELD_SORT to FilterValue.Sort(SavingsGoalsFilterSpec.SORT_PROGRESS_DESC)
        )
        store.saveFilterState(SavingsGoalsFilterSpec.SPEC_KEY, savingsState)
        val loadedSavings = store.loadFilterState(SavingsGoalsFilterSpec.SPEC_KEY)

        // FilterValue.Search is stripped
        assertNull(loadedSavings[SavingsGoalsFilterSpec.FIELD_SEARCH])
        assertEquals(setOf(SavingsGoalsFilterSpec.STATUS_ACTIVE), (loadedSavings[SavingsGoalsFilterSpec.FIELD_STATUS] as FilterValue.ToggleGroup).activeIds)
        assertEquals(SavingsGoalsFilterSpec.SORT_PROGRESS_DESC, (loadedSavings[SavingsGoalsFilterSpec.FIELD_SORT] as FilterValue.Sort).sortId)
    }

    @Test
    fun testMigrationFromTabFilterPreferencesForWishlistAndSavingsGoals() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tabFilterPrefs = TabFilterPreferences.getInstance(context)
        val store = FilterStore.getInstance(context)
        val prefs = context.getSharedPreferences("unified_filter_store", Context.MODE_PRIVATE)

        store.clearAllForTesting()

        tabFilterPrefs.wishlistSearchQuery = "Old Wishlist Search"
        tabFilterPrefs.wishlistTab = WishlistFilterTab.PLANNED_MONTHS
        tabFilterPrefs.wishlistSort = WishlistSort.AMOUNT_ASC

        tabFilterPrefs.savingsGoalsSearchQuery = "Old Goal Search"
        tabFilterPrefs.savingsGoalsFilter = GoalFilterType.DEFICIT

        store.migrateFromTabFilterPreferences(tabFilterPrefs)

        // Check Wishlist migration
        val migratedWishlist = store.loadFilterState(WishlistFilterSpec.SPEC_KEY)
        assertNull("Search must not be persisted in Wishlist", migratedWishlist[WishlistFilterSpec.FIELD_SEARCH])
        assertEquals(setOf(WishlistFilterSpec.TAB_PLANNED_MONTHS), (migratedWishlist[WishlistFilterSpec.FIELD_TAB] as FilterValue.ToggleGroup).activeIds)
        assertEquals(WishlistFilterSpec.SORT_AMOUNT_ASC, (migratedWishlist[WishlistFilterSpec.FIELD_SORT] as FilterValue.Sort).sortId)
        assertTrue("migrated_wishlist_v1 flag must be true", prefs.getBoolean(FilterStore.KEY_MIGRATED_WISHLIST, false))

        // Check SavingsGoals migration
        val migratedGoals = store.loadFilterState(SavingsGoalsFilterSpec.SPEC_KEY)
        assertNull("Search must not be persisted in SavingsGoals", migratedGoals[SavingsGoalsFilterSpec.FIELD_SEARCH])
        assertEquals(setOf(SavingsGoalsFilterSpec.STATUS_DEFICIT), (migratedGoals[SavingsGoalsFilterSpec.FIELD_STATUS] as FilterValue.ToggleGroup).activeIds)
        assertTrue("migrated_savings_goals_v1 flag must be true", prefs.getBoolean(FilterStore.KEY_MIGRATED_SAVINGS_GOALS, false))
    }

    @Test
    fun testEveryMigratedEnumValueForWishlistAndSavingsGoalsWithDefaults() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tabFilterPrefs = TabFilterPreferences.getInstance(context)
        val store = FilterStore.getInstance(context)

        // 1. Verify every WishlistFilterTab enum value
        for (tab in WishlistFilterTab.values()) {
            store.clearFilterState(WishlistFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.wishlistTab = tab

            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(WishlistFilterSpec.SPEC_KEY)
            val tabVal = state[WishlistFilterSpec.FIELD_TAB] as FilterValue.ToggleGroup
            assertEquals(1, tabVal.activeIds.size)
            val activeTabId = tabVal.activeIds.first()
            assertTrue(
                "Tab ID '$activeTabId' for enum $tab must exist in VALID_WISHLIST_TABS",
                FilterStore.VALID_WISHLIST_TABS.contains(activeTabId)
            )
            assertEquals(tab.name.lowercase(), activeTabId)
        }
        assertEquals(FilterStore.DEFAULT_WISHLIST_TAB, FilterStore.mapWishlistTab(null))

        // 2. Verify every WishlistSort enum value
        for (sort in WishlistSort.values()) {
            store.clearFilterState(WishlistFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.wishlistSort = sort

            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(WishlistFilterSpec.SPEC_KEY)
            val sortVal = state[WishlistFilterSpec.FIELD_SORT] as FilterValue.Sort
            assertNotNull(sortVal.sortId)
            assertTrue(
                "Sort ID '${sortVal.sortId}' for enum $sort must exist in VALID_WISHLIST_SORTS",
                FilterStore.VALID_WISHLIST_SORTS.contains(sortVal.sortId)
            )
            assertEquals(sort.name.lowercase(), sortVal.sortId)
        }
        assertEquals(FilterStore.DEFAULT_WISHLIST_SORT, FilterStore.mapWishlistSort(null))

        // 3. Verify every GoalFilterType enum value
        for (goalFilter in GoalFilterType.values()) {
            store.clearFilterState(SavingsGoalsFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.savingsGoalsFilter = goalFilter

            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(SavingsGoalsFilterSpec.SPEC_KEY)
            val statusVal = state[SavingsGoalsFilterSpec.FIELD_STATUS] as FilterValue.ToggleGroup
            assertEquals(1, statusVal.activeIds.size)
            val activeStatusId = statusVal.activeIds.first()
            assertTrue(
                "Status ID '$activeStatusId' for enum $goalFilter must exist in VALID_SAVINGS_GOALS_STATUSES",
                FilterStore.VALID_SAVINGS_GOALS_STATUSES.contains(activeStatusId)
            )
            assertEquals(goalFilter.name.lowercase(), activeStatusId)
        }
        assertEquals(FilterStore.DEFAULT_SAVINGS_GOALS_STATUS, FilterStore.mapSavingsGoalsStatus(null))
    }

    @Test
    fun testPaymentSourceFilterSpecFiltersAndToggles() {
        val sampleAccounts = listOf(
            Account(id = 1L, nameEn = "City Bank", nameBn = "সিটি ব্যাংক", initialBalance = 5000.0, type = com.example.data.model.AccountType.ASSET),
            Account(id = 2L, nameEn = "bKash", nameBn = "বিকাশ", initialBalance = 2000.0, type = com.example.data.model.AccountType.ASSET)
        )
        val sourcesSpec = PaymentSourceFilterSpec.createSpec(sampleAccounts, isAssignedTab = false)
        val assignedSpec = PaymentSourceFilterSpec.createSpec(sampleAccounts, isAssignedTab = true)

        assertEquals(PaymentSourceFilterSpec.SPEC_KEY, sourcesSpec.key)
        assertEquals(PaymentSourceFilterSpec.SPEC_KEY, assignedSpec.key)

        var state: FilterState = emptyMap()
        assertFalse(sourcesSpec.isActive(state))

        // 1. Search Query
        state = state.withSearchQuery("Bank", PaymentSourceFilterSpec.FIELD_SEARCH)
        assertEquals("Bank", PaymentSourceFilterSpec.getSearchQuery(state))

        // 2. Main Tab
        state = sourcesSpec.toggleCondition(state, PaymentSourceFilterSpec.FIELD_MAIN_TAB, PaymentSourceFilterSpec.TAB_ASSIGNED)
        assertEquals(MainPaymentSourceTab.ASSIGNED_ITEMS, PaymentSourceFilterSpec.getMainTab(state))

        state = sourcesSpec.toggleCondition(state, PaymentSourceFilterSpec.FIELD_MAIN_TAB, PaymentSourceFilterSpec.TAB_SOURCES)
        assertEquals(MainPaymentSourceTab.PAYMENT_SOURCES, PaymentSourceFilterSpec.getMainTab(state))

        // 3. Calculation Basis
        state = sourcesSpec.toggleCondition(state, PaymentSourceFilterSpec.FIELD_CALC_BASIS, PaymentSourceFilterSpec.BASIS_REMAINING)
        assertEquals(RequirementCalculationBasis.REMAINING_AMOUNT, PaymentSourceFilterSpec.getCalcBasis(state))

        state = sourcesSpec.toggleCondition(state, PaymentSourceFilterSpec.FIELD_CALC_BASIS, PaymentSourceFilterSpec.BASIS_BUDGET)
        assertEquals(RequirementCalculationBasis.BUDGET_AMOUNT, PaymentSourceFilterSpec.getCalcBasis(state))

        // 4. Source Status & Sort
        state = sourcesSpec.toggleCondition(state, PaymentSourceFilterSpec.FIELD_SOURCE_STATUS, PaymentSourceFilterSpec.STATUS_SHORTFALL)
        assertEquals(AccountStatusFilter.SHORTFALL_ONLY, PaymentSourceFilterSpec.getSourceStatus(state))

        state = state + (PaymentSourceFilterSpec.FIELD_SOURCE_SORT to FilterValue.Sort(PaymentSourceFilterSpec.SORT_SOURCE_BALANCE_DESC))
        assertEquals(PaymentSourceSortOption.BALANCE_DESC, PaymentSourceFilterSpec.getSourceSort(state))

        // 5. Assigned Section, Status, Account & Sort
        state = assignedSpec.toggleCondition(state, PaymentSourceFilterSpec.FIELD_ASSIGNED_SECTION, PaymentSourceFilterSpec.SECTION_EXPENSES)
        assertEquals(AssignedItemSectionFilter.EXPENSES, PaymentSourceFilterSpec.getAssignedSection(state))

        state = assignedSpec.toggleCondition(state, PaymentSourceFilterSpec.FIELD_ASSIGNED_STATUS, PaymentSourceFilterSpec.ASSIGNED_STATUS_SPLIT)
        assertEquals(AssignedItemStatusFilter.SPLIT_ONLY, PaymentSourceFilterSpec.getAssignedStatus(state))

        state = state + (PaymentSourceFilterSpec.FIELD_ASSIGNED_SOURCE_ACCOUNT to FilterValue.SingleSelect("1"))
        assertEquals(1L, PaymentSourceFilterSpec.getSelectedSourceAccountId(state))

        state = state + (PaymentSourceFilterSpec.FIELD_ASSIGNED_SORT to FilterValue.Sort(PaymentSourceFilterSpec.SORT_ASSIGNED_AMOUNT_DESC))
        assertEquals(AssignedItemSortOption.BUDGET_DESC, PaymentSourceFilterSpec.getAssignedSort(state))

        val chips = assignedSpec.activeChips(state, LanguageMode.ENGLISH)
        assertTrue(chips.isNotEmpty())
    }

    @Test
    fun testRmManagerFilterSpecFiltersAndToggles() {
        val spec = RmManagerFilterSpec.createSpec()
        assertEquals(RmManagerFilterSpec.SPEC_KEY, spec.key)

        var state: FilterState = emptyMap()
        assertFalse(spec.isActive(state))

        // 1. Search Query
        state = state.withSearchQuery("Office", RmManagerFilterSpec.FIELD_SEARCH)
        assertEquals("Office", RmManagerFilterSpec.getSearchQuery(state))

        // 2. Status Category
        state = spec.toggleCondition(state, RmManagerFilterSpec.FIELD_CATEGORY, RmManagerFilterSpec.CAT_PENDING_ONLY)
        assertEquals(RmFilterCategory.PENDING_ONLY, RmManagerFilterSpec.getCategory(state))

        state = spec.toggleCondition(state, RmManagerFilterSpec.FIELD_CATEGORY, RmManagerFilterSpec.CAT_HIGH_LIABILITY)
        assertEquals(RmFilterCategory.HIGH_LIABILITY, RmManagerFilterSpec.getCategory(state))

        // 3. Sort Order
        state = state + (RmManagerFilterSpec.FIELD_SORT to FilterValue.Sort(RmManagerFilterSpec.SORT_NAME_AZ))
        assertEquals(RmSortOption.NAME_AZ, RmManagerFilterSpec.getSort(state))

        val chips = spec.activeChips(state, LanguageMode.ENGLISH)
        assertTrue(chips.isNotEmpty())
    }

    @Test
    fun testMigrationFromTabFilterPreferencesForPaymentSourceAndRmManager() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tabFilterPrefs = TabFilterPreferences.getInstance(context)
        val store = FilterStore.getInstance(context)
        val prefs = context.getSharedPreferences("unified_filter_store", Context.MODE_PRIVATE)

        store.clearAllForTesting()

        // Set old payment source preferences
        tabFilterPrefs.paymentSourceSearchQuery = "Old PS Search"
        tabFilterPrefs.paymentSourceTab = MainPaymentSourceTab.ASSIGNED_ITEMS
        tabFilterPrefs.paymentSourceCalcBasis = RequirementCalculationBasis.REMAINING_AMOUNT
        tabFilterPrefs.paymentSourceAccountStatus = AccountStatusFilter.SHORTFALL_ONLY
        tabFilterPrefs.paymentSourceSortOption = PaymentSourceSortOption.REQUIRED_DESC
        tabFilterPrefs.paymentSourceAssignedSection = AssignedItemSectionFilter.EXPENSES
        tabFilterPrefs.paymentSourceAssignedStatus = AssignedItemStatusFilter.SPLIT_ONLY
        tabFilterPrefs.paymentSourceAssignedSort = AssignedItemSortOption.MOST_USED

        // Set old RM Manager preferences
        tabFilterPrefs.rmSearchQuery = "Old RM Search"
        tabFilterPrefs.rmFilterCategory = RmFilterCategory.UNRECONCILED
        tabFilterPrefs.rmSortOption = RmSortOption.MOST_REPAID

        store.migrateFromTabFilterPreferences(tabFilterPrefs)

        // Check PaymentSource migration
        val migratedPs = store.loadFilterState(PaymentSourceFilterSpec.SPEC_KEY)
        assertNull("Search must not be persisted in PaymentSource", migratedPs[PaymentSourceFilterSpec.FIELD_SEARCH])
        assertEquals(setOf(PaymentSourceFilterSpec.TAB_ASSIGNED), (migratedPs[PaymentSourceFilterSpec.FIELD_MAIN_TAB] as FilterValue.ToggleGroup).activeIds)
        assertEquals(setOf(PaymentSourceFilterSpec.BASIS_REMAINING), (migratedPs[PaymentSourceFilterSpec.FIELD_CALC_BASIS] as FilterValue.ToggleGroup).activeIds)
        assertEquals(setOf(PaymentSourceFilterSpec.STATUS_SHORTFALL), (migratedPs[PaymentSourceFilterSpec.FIELD_SOURCE_STATUS] as FilterValue.ToggleGroup).activeIds)
        assertEquals(PaymentSourceFilterSpec.SORT_SOURCE_REQUIRED_DESC, (migratedPs[PaymentSourceFilterSpec.FIELD_SOURCE_SORT] as FilterValue.Sort).sortId)
        assertEquals(setOf(PaymentSourceFilterSpec.SECTION_EXPENSES), (migratedPs[PaymentSourceFilterSpec.FIELD_ASSIGNED_SECTION] as FilterValue.ToggleGroup).activeIds)
        assertEquals(setOf(PaymentSourceFilterSpec.ASSIGNED_STATUS_SPLIT), (migratedPs[PaymentSourceFilterSpec.FIELD_ASSIGNED_STATUS] as FilterValue.ToggleGroup).activeIds)
        assertEquals(PaymentSourceFilterSpec.SORT_ASSIGNED_MOST_USED, (migratedPs[PaymentSourceFilterSpec.FIELD_ASSIGNED_SORT] as FilterValue.Sort).sortId)
        assertTrue("migrated_payment_source_v1 flag must be true", prefs.getBoolean(FilterStore.KEY_MIGRATED_PAYMENT_SOURCE, false))

        // Check RM Manager migration
        val migratedRm = store.loadFilterState(RmManagerFilterSpec.SPEC_KEY)
        assertNull("Search must not be persisted in RM Manager", migratedRm[RmManagerFilterSpec.FIELD_SEARCH])
        assertEquals(setOf(RmManagerFilterSpec.CAT_UNRECONCILED), (migratedRm[RmManagerFilterSpec.FIELD_CATEGORY] as FilterValue.ToggleGroup).activeIds)
        assertEquals(RmManagerFilterSpec.SORT_MOST_REPAID, (migratedRm[RmManagerFilterSpec.FIELD_SORT] as FilterValue.Sort).sortId)
        assertTrue("migrated_rm_manager_v1 flag must be true", prefs.getBoolean(FilterStore.KEY_MIGRATED_RM_MANAGER, false))
    }

    @Test
    fun testEveryMigratedEnumValueForPaymentSourceAndRmManagerWithDefaults() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tabFilterPrefs = TabFilterPreferences.getInstance(context)
        val store = FilterStore.getInstance(context)

        // 1. PaymentSource Main Tab
        for (tab in MainPaymentSourceTab.values()) {
            store.clearFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.paymentSourceTab = tab
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            val tabVal = (state[PaymentSourceFilterSpec.FIELD_MAIN_TAB] as FilterValue.ToggleGroup).activeIds.first()
            assertTrue(FilterStore.VALID_PAYMENT_SOURCE_TABS.contains(tabVal))
        }
        assertEquals(FilterStore.DEFAULT_PAYMENT_SOURCE_TAB, FilterStore.mapPaymentSourceTab(null))

        // 2. PaymentSource Calc Basis
        for (basis in RequirementCalculationBasis.values()) {
            store.clearFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.paymentSourceCalcBasis = basis
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            val basisVal = (state[PaymentSourceFilterSpec.FIELD_CALC_BASIS] as FilterValue.ToggleGroup).activeIds.first()
            assertTrue(FilterStore.VALID_PAYMENT_SOURCE_CALC_BASES.contains(basisVal))
        }
        assertEquals(FilterStore.DEFAULT_PAYMENT_SOURCE_CALC_BASIS, FilterStore.mapPaymentSourceCalcBasis(null))

        // 3. PaymentSource Account Status
        for (status in AccountStatusFilter.values()) {
            store.clearFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.paymentSourceAccountStatus = status
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            val statusVal = (state[PaymentSourceFilterSpec.FIELD_SOURCE_STATUS] as FilterValue.ToggleGroup).activeIds.first()
            assertTrue(FilterStore.VALID_PAYMENT_SOURCE_STATUSES.contains(statusVal))
        }
        assertEquals(FilterStore.DEFAULT_PAYMENT_SOURCE_STATUS, FilterStore.mapPaymentSourceStatus(null))

        // 4. PaymentSource Source Sort
        for (sort in PaymentSourceSortOption.values()) {
            store.clearFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.paymentSourceSortOption = sort
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            val sortVal = (state[PaymentSourceFilterSpec.FIELD_SOURCE_SORT] as FilterValue.Sort).sortId
            assertTrue(FilterStore.VALID_PAYMENT_SOURCE_SORTS.contains(sortVal))
        }
        assertEquals(FilterStore.DEFAULT_PAYMENT_SOURCE_SORT, FilterStore.mapPaymentSourceSort(null))

        // 5. PaymentSource Assigned Section
        for (sec in AssignedItemSectionFilter.values()) {
            store.clearFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.paymentSourceAssignedSection = sec
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            val secVal = (state[PaymentSourceFilterSpec.FIELD_ASSIGNED_SECTION] as FilterValue.ToggleGroup).activeIds.first()
            assertTrue(FilterStore.VALID_ASSIGNED_SECTIONS.contains(secVal))
        }
        assertEquals(FilterStore.DEFAULT_ASSIGNED_SECTION, FilterStore.mapAssignedSection(null))

        // 6. PaymentSource Assigned Status
        for (st in AssignedItemStatusFilter.values()) {
            store.clearFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.paymentSourceAssignedStatus = st
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            val stVal = (state[PaymentSourceFilterSpec.FIELD_ASSIGNED_STATUS] as FilterValue.ToggleGroup).activeIds.first()
            assertTrue(FilterStore.VALID_ASSIGNED_STATUSES.contains(stVal))
        }
        assertEquals(FilterStore.DEFAULT_ASSIGNED_STATUS, FilterStore.mapAssignedStatus(null))

        // 7. PaymentSource Assigned Sort
        for (st in AssignedItemSortOption.values()) {
            store.clearFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.paymentSourceAssignedSort = st
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(PaymentSourceFilterSpec.SPEC_KEY)
            val stVal = (state[PaymentSourceFilterSpec.FIELD_ASSIGNED_SORT] as FilterValue.Sort).sortId
            assertTrue(FilterStore.VALID_ASSIGNED_SORTS.contains(stVal))
        }
        assertEquals(FilterStore.DEFAULT_ASSIGNED_SORT, FilterStore.mapAssignedSort(null))

        // 8. RM Filter Category
        for (cat in RmFilterCategory.values()) {
            store.clearFilterState(RmManagerFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.rmFilterCategory = cat
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(RmManagerFilterSpec.SPEC_KEY)
            val catVal = (state[RmManagerFilterSpec.FIELD_CATEGORY] as FilterValue.ToggleGroup).activeIds.first()
            assertTrue(FilterStore.VALID_RM_CATEGORIES.contains(catVal))
        }
        assertEquals(FilterStore.DEFAULT_RM_CATEGORY, FilterStore.mapRmCategory(null))

        // 9. RM Sort Option
        for (sort in RmSortOption.values()) {
            store.clearFilterState(RmManagerFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.rmSortOption = sort
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(RmManagerFilterSpec.SPEC_KEY)
            val sortVal = (state[RmManagerFilterSpec.FIELD_SORT] as FilterValue.Sort).sortId
            assertTrue(FilterStore.VALID_RM_SORTS.contains(sortVal))
        }
        assertEquals(FilterStore.DEFAULT_RM_SORT, FilterStore.mapRmSort(null))

        // 10. Accounts Type Filter
        for (type in listOf(null, com.example.data.model.AccountType.ASSET, com.example.data.model.AccountType.LIABILITY)) {
            store.clearFilterState(AccountsFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.accountsTypeFilter = type
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(AccountsFilterSpec.SPEC_KEY)
            val typeVal = (state[AccountsFilterSpec.FIELD_TYPE] as FilterValue.ToggleGroup).activeIds.first()
            assertTrue(FilterStore.VALID_ACCOUNTS_TYPES.contains(typeVal))
            assertEquals(type, AccountsFilterSpec.getType(state))
        }
        assertEquals(FilterStore.DEFAULT_ACCOUNTS_TYPE, FilterStore.mapAccountsType(null))

        // 11. Accounts Hierarchy Filter
        for (h in AccountViewHierarchyFilter.values()) {
            store.clearFilterState(AccountsFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.accountsHierarchyFilter = h
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(AccountsFilterSpec.SPEC_KEY)
            val hVal = (state[AccountsFilterSpec.FIELD_HIERARCHY] as FilterValue.ToggleGroup).activeIds.first()
            assertTrue(FilterStore.VALID_ACCOUNTS_HIERARCHIES.contains(hVal))
            assertEquals(h, AccountsFilterSpec.getHierarchy(state))
        }
        assertEquals(FilterStore.DEFAULT_ACCOUNTS_HIERARCHY, FilterStore.mapAccountsHierarchy(null))

        // 12. Accounts Status Filter
        for (st in AccountActiveStatusFilter.values()) {
            store.clearFilterState(AccountsFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.accountsStatusFilter = st
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(AccountsFilterSpec.SPEC_KEY)
            val stVal = (state[AccountsFilterSpec.FIELD_STATUS] as FilterValue.ToggleGroup).activeIds.first()
            assertTrue(FilterStore.VALID_ACCOUNTS_STATUSES.contains(stVal))
            assertEquals(st, AccountsFilterSpec.getStatus(state))
        }
        assertEquals(FilterStore.DEFAULT_ACCOUNTS_STATUS, FilterStore.mapAccountsStatus(null))

        // 13. Accounts Sort Filter
        for (sort in AccountSortFilter.values()) {
            store.clearFilterState(AccountsFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.accountsSortFilter = sort
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(AccountsFilterSpec.SPEC_KEY)
            val sortVal = (state[AccountsFilterSpec.FIELD_SORT] as FilterValue.Sort).sortId
            assertTrue(FilterStore.VALID_ACCOUNTS_SORTS.contains(sortVal))
            assertEquals(sort, AccountsFilterSpec.getSort(state))
        }
        assertEquals(FilterStore.DEFAULT_ACCOUNTS_SORT, FilterStore.mapAccountsSort(null))

        // 14. Accounts Exclude Zero Balance
        store.clearFilterState(AccountsFilterSpec.SPEC_KEY)
        store.resetMigrationFlagsForTesting()
        tabFilterPrefs.accountsExcludeZeroBalance = true
        store.migrateFromTabFilterPreferences(tabFilterPrefs)
        var accState = store.loadFilterState(AccountsFilterSpec.SPEC_KEY)
        assertTrue(AccountsFilterSpec.getExcludeZeroBalance(accState))

        store.clearFilterState(AccountsFilterSpec.SPEC_KEY)
        store.resetMigrationFlagsForTesting()
        tabFilterPrefs.accountsExcludeZeroBalance = false
        store.migrateFromTabFilterPreferences(tabFilterPrefs)
        accState = store.loadFilterState(AccountsFilterSpec.SPEC_KEY)
        assertFalse(AccountsFilterSpec.getExcludeZeroBalance(accState))

        // 15. Items Tab Mode
        for (mode in listOf("EXPENSE", "ALL", "INCOME", "invalid")) {
            store.clearFilterState(ItemsFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.itemsTabMode = mode
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(ItemsFilterSpec.SPEC_KEY)
            val modeVal = (state[ItemsFilterSpec.FIELD_TYPE_MODE] as FilterValue.ToggleGroup).activeIds.first()
            assertTrue(FilterStore.VALID_ITEMS_TAB_MODES.contains(modeVal))
        }
        assertEquals(FilterStore.DEFAULT_ITEMS_TAB_MODE, FilterStore.mapItemsTabMode(null))

        // 16. Items Date Preset
        for (preset in AggregatedDatePreset.values()) {
            store.clearFilterState(ItemsFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.itemsDatePreset = preset
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(ItemsFilterSpec.SPEC_KEY)
            val pVal = (state[ItemsFilterSpec.FIELD_DATE] as FilterValue.Date).presetId
            assertTrue(FilterStore.VALID_ITEMS_DATE_PRESETS.contains(pVal))
        }
        assertEquals(FilterStore.DEFAULT_ITEMS_DATE_PRESET, FilterStore.mapItemsDatePreset(null))

        // 17. Items Sort Order
        for (sort in AggregatedSortOrder.values()) {
            store.clearFilterState(ItemsFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.itemsSortOrder = sort
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(ItemsFilterSpec.SPEC_KEY)
            val sVal = (state[ItemsFilterSpec.FIELD_SORT] as FilterValue.Sort).sortId
            assertTrue(FilterStore.VALID_ITEMS_SORT_ORDERS.contains(sVal))
            assertEquals(sort, ItemsFilterSpec.getSortOrder(state))
        }
        assertEquals(FilterStore.DEFAULT_ITEMS_SORT_ORDER, FilterStore.mapItemsSortOrder(null))

        // 18. Balance Sheet Comparison Preset
        for (preset in BalanceSheetComparisonPreset.values()) {
            store.clearFilterState(BalanceSheetFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.balanceSheetFilterState = BalanceSheetFilterState(preset = preset)
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(BalanceSheetFilterSpec.SPEC_KEY)
            val pVal = (state[BalanceSheetFilterSpec.FIELD_DATE] as FilterValue.Date).presetId
            assertTrue(FilterStore.VALID_BS_PRESETS.contains(pVal))
            assertEquals(preset, BalanceSheetFilterSpec.getDatePreset(state))
        }
        assertEquals(FilterStore.DEFAULT_BS_PRESET, FilterStore.mapBsPreset(null))

        // 19. Balance Sheet Sort Order
        for (sort in BalanceSheetSortOrder.values()) {
            store.clearFilterState(BalanceSheetFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.balanceSheetFilterState = BalanceSheetFilterState(sortOrder = sort)
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(BalanceSheetFilterSpec.SPEC_KEY)
            val sVal = (state[BalanceSheetFilterSpec.FIELD_SORT] as FilterValue.Sort).sortId
            assertTrue(FilterStore.VALID_BS_SORTS.contains(sVal))
            assertEquals(sort, BalanceSheetFilterSpec.getSortOrder(state))
        }
        assertEquals(FilterStore.DEFAULT_BS_SORT, FilterStore.mapBsSort(null))

        // 20. Balance Sheet Tab
        for (tab in listOf("ALL", "ASSETS", "LIABILITIES", "invalid")) {
            store.clearFilterState(BalanceSheetFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.balanceSheetActiveTab = tab
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(BalanceSheetFilterSpec.SPEC_KEY)
            val tVal = (state[BalanceSheetFilterSpec.FIELD_ACTIVE_TAB] as FilterValue.ToggleGroup).activeIds.first()
            assertTrue(FilterStore.VALID_BS_TABS.contains(tVal))
        }
        assertEquals(FilterStore.DEFAULT_BS_TAB, FilterStore.mapBsTab(null))

        // 21. Balance Sheet Boolean Flags
        store.clearFilterState(BalanceSheetFilterSpec.SPEC_KEY)
        store.resetMigrationFlagsForTesting()
        tabFilterPrefs.balanceSheetFilterState = BalanceSheetFilterState(
            excludeZeroAmounts = false,
            filterNonZeroGroups = true,
            showHiddenAccounts = true,
            showOnlyCurrentBalance = true,
            showOnlyAccountsWithoutGroups = true
        )
        store.migrateFromTabFilterPreferences(tabFilterPrefs)
        val bsState = store.loadFilterState(BalanceSheetFilterSpec.SPEC_KEY)
        assertFalse(BalanceSheetFilterSpec.getExcludeZero(bsState))
        assertTrue(BalanceSheetFilterSpec.getFilterNonZeroGroups(bsState))
        assertTrue(BalanceSheetFilterSpec.getShowHidden(bsState))
        assertTrue(BalanceSheetFilterSpec.getShowOnlyCurrent(bsState))
        assertTrue(BalanceSheetFilterSpec.getShowWithoutGroups(bsState))

        // 22. Cash Flow Period Preset
        for (preset in CashFlowPeriodPreset.values()) {
            store.clearFilterState(CashFlowFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.cashFlowPreset = preset
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(CashFlowFilterSpec.SPEC_KEY)
            val pVal = (state[CashFlowFilterSpec.FIELD_DATE] as FilterValue.Date).presetId
            assertTrue(FilterStore.VALID_CASH_FLOW_PRESETS.contains(pVal))
            assertEquals(preset, CashFlowFilterSpec.getPeriodPreset(state))
        }
        assertEquals(FilterStore.DEFAULT_CASH_FLOW_PRESET, FilterStore.mapCashFlowPreset(null))

        // 23. Cash Flow Tab Section
        for (sec in CashFlowTabSection.values()) {
            store.clearFilterState(CashFlowFilterSpec.SPEC_KEY)
            store.resetMigrationFlagsForTesting()
            tabFilterPrefs.cashFlowSection = sec
            store.migrateFromTabFilterPreferences(tabFilterPrefs)
            val state = store.loadFilterState(CashFlowFilterSpec.SPEC_KEY)
            val sVal = (state[CashFlowFilterSpec.FIELD_SECTION] as FilterValue.ToggleGroup).activeIds.first()
            assertTrue(FilterStore.VALID_CASH_FLOW_SECTIONS.contains(sVal))
            assertEquals(sec, CashFlowFilterSpec.getSection(state))
        }
        assertEquals(FilterStore.DEFAULT_CASH_FLOW_SECTION, FilterStore.mapCashFlowSection(null))
    }

    @Test
    fun testAccountsFilterSpecFiltersAndToggles() {
        val spec = AccountsFilterSpec.createSpec()
        assertEquals(AccountsFilterSpec.SPEC_KEY, spec.key)

        var state: FilterState = emptyMap()
        assertFalse(spec.isActive(state))

        // 1. Search Query
        state = state.withSearchQuery("Savings Bank", AccountsFilterSpec.FIELD_SEARCH)
        assertEquals("Savings Bank", AccountsFilterSpec.getSearchQuery(state))

        // 2. Type Filter
        state = AccountsFilterSpec.withType(state, com.example.data.model.AccountType.ASSET)
        assertEquals(com.example.data.model.AccountType.ASSET, AccountsFilterSpec.getType(state))

        state = AccountsFilterSpec.withType(state, com.example.data.model.AccountType.LIABILITY)
        assertEquals(com.example.data.model.AccountType.LIABILITY, AccountsFilterSpec.getType(state))

        state = AccountsFilterSpec.withType(state, null)
        assertNull(AccountsFilterSpec.getType(state))

        // 3. Hierarchy Filter
        state = AccountsFilterSpec.withHierarchy(state, AccountViewHierarchyFilter.ONLY_GROUPS)
        assertEquals(AccountViewHierarchyFilter.ONLY_GROUPS, AccountsFilterSpec.getHierarchy(state))

        state = AccountsFilterSpec.withHierarchy(state, AccountViewHierarchyFilter.EXCLUDED)
        assertEquals(AccountViewHierarchyFilter.EXCLUDED, AccountsFilterSpec.getHierarchy(state))

        // 4. Status Filter
        state = AccountsFilterSpec.withStatus(state, AccountActiveStatusFilter.ACTIVE_ONLY)
        assertEquals(AccountActiveStatusFilter.ACTIVE_ONLY, AccountsFilterSpec.getStatus(state))

        state = AccountsFilterSpec.withStatus(state, AccountActiveStatusFilter.INACTIVE_ONLY)
        assertEquals(AccountActiveStatusFilter.INACTIVE_ONLY, AccountsFilterSpec.getStatus(state))

        // 5. Exclude Zero Balance
        state = AccountsFilterSpec.withExcludeZero(state, true)
        assertTrue(AccountsFilterSpec.getExcludeZeroBalance(state))

        state = AccountsFilterSpec.withExcludeZero(state, false)
        assertFalse(AccountsFilterSpec.getExcludeZeroBalance(state))

        // 6. Sort Filter
        state = AccountsFilterSpec.withSort(state, AccountSortFilter.AMOUNT_HIGH_TO_LOW)
        assertEquals(AccountSortFilter.AMOUNT_HIGH_TO_LOW, AccountsFilterSpec.getSort(state))

        val chips = spec.activeChips(state, LanguageMode.ENGLISH)
        assertTrue(chips.isNotEmpty())
    }

    @Test
    fun testItemsFilterSpecFiltersAndToggles() {
        val categories = listOf(
            Category(id = 1L, nameEn = "Food", nameBn = "খাবার", type = CategoryType.EXPENSE)
        )
        val accounts = listOf(
            Account(id = 10L, nameEn = "Cash", nameBn = "নগদ", type = com.example.data.model.AccountType.ASSET)
        )
        val spec = ItemsFilterSpec.createSpec(categories = categories, accounts = accounts)
        assertEquals(ItemsFilterSpec.SPEC_KEY, spec.key)

        var state: FilterState = emptyMap()
        assertFalse(spec.isActive(state))

        // 1. Search Query
        state = ItemsFilterSpec.withSearchQuery(state, "Grocery")
        assertEquals("Grocery", ItemsFilterSpec.getSearchQuery(state))

        // 2. Type Mode
        state = ItemsFilterSpec.withTypeMode(state, ItemsFilterSpec.MODE_EXPENSE)
        assertEquals(ItemsFilterSpec.MODE_EXPENSE, ItemsFilterSpec.getTypeModeId(state))

        state = ItemsFilterSpec.withTypeMode(state, ItemsFilterSpec.MODE_INCOME)
        assertEquals(ItemsFilterSpec.MODE_INCOME, ItemsFilterSpec.getTypeModeId(state))

        // 3. Date bounds resolution
        val bounds = ItemsFilterSpec.resolveDateBounds(spec, state)
        assertTrue(bounds.second >= bounds.first)

        // 4. Accounts and Categories selection
        state = state + (ItemsFilterSpec.FIELD_ACCOUNTS to FilterValue.Select(setOf("10")))
        assertEquals(setOf(10L), ItemsFilterSpec.getSelectedAccountIds(state))

        state = state + (ItemsFilterSpec.FIELD_CATEGORIES to FilterValue.Select(setOf("1")))
        assertEquals(setOf(1L), ItemsFilterSpec.getSelectedCategoryIds(state))

        // 5. Status and Amount Range
        state = state + (ItemsFilterSpec.FIELD_STATUSES to FilterValue.Select(setOf("CLEARED")))
        assertEquals(setOf(TransactionStatus.CLEARED), ItemsFilterSpec.getSelectedStatuses(state))

        state = state + (ItemsFilterSpec.FIELD_AMOUNT_RANGE to FilterValue.Range(min = 100.0, max = 5000.0))
        assertEquals(100.0, ItemsFilterSpec.getMinAmount(state)!!, 0.001)
        assertEquals(5000.0, ItemsFilterSpec.getMaxAmount(state)!!, 0.001)

        // 6. Exclude Zero and Sort Order
        state = state + (ItemsFilterSpec.FIELD_EXCLUDE_ZERO to FilterValue.ToggleGroup(setOf(ItemsFilterSpec.EXCLUDE_ZERO_ID)))
        assertTrue(ItemsFilterSpec.getExcludeZero(state))

        state = ItemsFilterSpec.withSortOrder(state, AggregatedSortOrder.COUNT_DESC)
        assertEquals(AggregatedSortOrder.COUNT_DESC, ItemsFilterSpec.getSortOrder(state))

        val chips = spec.activeChips(state, LanguageMode.ENGLISH)
        assertTrue(chips.isNotEmpty())
    }

    @Test
    fun testBalanceSheetFilterSpecFiltersAndToggles() {
        val sampleAccounts = listOf(
            Account(id = 101L, nameEn = "City Bank", nameBn = "সিটি ব্যাংক", type = com.example.data.model.AccountType.ASSET, initialBalance = 50000.0),
            Account(id = 102L, nameEn = "Credit Card", nameBn = "ক্রেডিট কার্ড", type = com.example.data.model.AccountType.LIABILITY, initialBalance = -10000.0)
        )
        val spec = BalanceSheetFilterSpec.createSpec(sampleAccounts)
        assertEquals(BalanceSheetFilterSpec.SPEC_KEY, spec.key)

        var state: FilterState = emptyMap()
        assertFalse(spec.isActive(state))

        // 1. Search Query
        state = state.withSearchQuery("City Bank", BalanceSheetFilterSpec.FIELD_SEARCH)
        assertEquals("City Bank", BalanceSheetFilterSpec.getSearchQuery(state))

        // 2. Section / Active Tab
        state = BalanceSheetFilterSpec.withActiveTab(state, BalanceSheetFilterSpec.TAB_ASSETS)
        assertEquals(BalanceSheetFilterSpec.TAB_ASSETS, BalanceSheetFilterSpec.getActiveTab(state))

        state = BalanceSheetFilterSpec.withActiveTab(state, BalanceSheetFilterSpec.TAB_LIABILITIES)
        assertEquals(BalanceSheetFilterSpec.TAB_LIABILITIES, BalanceSheetFilterSpec.getActiveTab(state))

        state = BalanceSheetFilterSpec.withActiveTab(state, BalanceSheetFilterSpec.TAB_ALL)
        assertEquals(BalanceSheetFilterSpec.TAB_ALL, BalanceSheetFilterSpec.getActiveTab(state))

        // 3. Date Presets and Custom Dates
        state = BalanceSheetFilterSpec.withPreset(state, BalanceSheetComparisonPreset.END_OF_LAST_MONTH)
        assertEquals(BalanceSheetComparisonPreset.END_OF_LAST_MONTH, BalanceSheetFilterSpec.getDatePreset(state))

        val dates = BalanceSheetFilterSpec.resolveComparisonDates(spec, state)
        assertTrue(dates.first > 0L)
        assertTrue(dates.second > 0L)

        state = BalanceSheetFilterSpec.withCustomDates(state, 1700000000000L, 1705000000000L)
        assertEquals(BalanceSheetComparisonPreset.CUSTOM, BalanceSheetFilterSpec.getDatePreset(state))
        val customDates = BalanceSheetFilterSpec.resolveComparisonDates(spec, state)
        assertEquals(1700000000000L, customDates.first)
        assertEquals(1705000000000L, customDates.second)

        // 4. Accounts selection
        state = BalanceSheetFilterSpec.withAccountIds(state, setOf(101L))
        assertEquals(setOf(101L), BalanceSheetFilterSpec.getSelectedAccountIds(state))

        // 5. Statuses selection
        state = state + (BalanceSheetFilterSpec.FIELD_STATUSES to FilterValue.Select(setOf("CLEARED", "RECONCILED")))
        assertEquals(setOf(TransactionStatus.CLEARED, TransactionStatus.RECONCILED), BalanceSheetFilterSpec.getSelectedStatuses(state))

        // 6. Sort Order
        state = BalanceSheetFilterSpec.withSortOrder(state, BalanceSheetSortOrder.AMOUNT_ASC)
        assertEquals(BalanceSheetSortOrder.AMOUNT_ASC, BalanceSheetFilterSpec.getSortOrder(state))

        state = BalanceSheetFilterSpec.withSortOrder(state, BalanceSheetSortOrder.NAME_ASC)
        assertEquals(BalanceSheetSortOrder.NAME_ASC, BalanceSheetFilterSpec.getSortOrder(state))

        // 7. Boolean flags
        state = BalanceSheetFilterSpec.withExcludeZero(state, false)
        assertFalse(BalanceSheetFilterSpec.getExcludeZero(state))

        state = state + (BalanceSheetFilterSpec.FIELD_FILTER_NON_ZERO_GROUPS to FilterValue.BooleanVal(true))
        assertTrue(BalanceSheetFilterSpec.getFilterNonZeroGroups(state))

        state = state + (BalanceSheetFilterSpec.FIELD_SHOW_HIDDEN to FilterValue.BooleanVal(true))
        assertTrue(BalanceSheetFilterSpec.getShowHidden(state))

        state = state + (BalanceSheetFilterSpec.FIELD_ONLY_CURRENT to FilterValue.BooleanVal(true))
        assertTrue(BalanceSheetFilterSpec.getShowOnlyCurrent(state))

        state = state + (BalanceSheetFilterSpec.FIELD_WITHOUT_GROUPS to FilterValue.BooleanVal(true))
        assertTrue(BalanceSheetFilterSpec.getShowWithoutGroups(state))

        val chips = spec.activeChips(state, LanguageMode.ENGLISH)
        assertTrue(chips.isNotEmpty())
        val chipsBn = spec.activeChips(state, LanguageMode.BANGLA)
        assertTrue(chipsBn.isNotEmpty())
    }

    @Test
    fun testCashFlowFilterSpecFiltersAndToggles() {
        val sampleAccounts = listOf(
            Account(id = 201L, nameEn = "Cash Wallet", nameBn = "নগদ ওয়ালেট", type = com.example.data.model.AccountType.ASSET, initialBalance = 10000.0)
        )
        val spec = CashFlowFilterSpec.createSpec(sampleAccounts)
        assertEquals(CashFlowFilterSpec.SPEC_KEY, spec.key)

        var state: FilterState = emptyMap()
        assertFalse(spec.isActive(state))

        // 1. Search Query
        state = CashFlowFilterSpec.withSearchQuery(state, "Salary")
        assertEquals("Salary", CashFlowFilterSpec.getSearchQuery(state))

        // 2. Period Preset
        state = CashFlowFilterSpec.withPeriodPreset(state, CashFlowPeriodPreset.LAST_MONTH)
        assertEquals(CashFlowPeriodPreset.LAST_MONTH, CashFlowFilterSpec.getPeriodPreset(state))

        val range = CashFlowFilterSpec.resolveDateRange(spec, state)
        assertTrue(range.first > 0L)
        assertTrue(range.second > range.first)

        state = CashFlowFilterSpec.withCustomDates(state, 1710000000000L, 1715000000000L)
        assertEquals(CashFlowPeriodPreset.CUSTOM, CashFlowFilterSpec.getPeriodPreset(state))
        val customRange = CashFlowFilterSpec.resolveDateRange(spec, state)
        assertEquals(1710000000000L, customRange.first)
        assertEquals(1715000000000L, customRange.second)

        // 3. Tab Section
        state = CashFlowFilterSpec.withSection(state, CashFlowTabSection.STATEMENT)
        assertEquals(CashFlowTabSection.STATEMENT, CashFlowFilterSpec.getSection(state))

        state = CashFlowFilterSpec.withSection(state, CashFlowTabSection.ACCOUNTS)
        assertEquals(CashFlowTabSection.ACCOUNTS, CashFlowFilterSpec.getSection(state))

        state = CashFlowFilterSpec.withSection(state, CashFlowTabSection.TRANSACTIONS)
        assertEquals(CashFlowTabSection.TRANSACTIONS, CashFlowFilterSpec.getSection(state))

        state = CashFlowFilterSpec.withSection(state, CashFlowTabSection.OVERVIEW)
        assertEquals(CashFlowTabSection.OVERVIEW, CashFlowFilterSpec.getSection(state))

        // 4. Accounts selection
        state = CashFlowFilterSpec.withAccountIds(state, setOf(201L))
        assertEquals(setOf(201L), CashFlowFilterSpec.getSelectedAccountIds(state))

        // 5. Transaction Type
        state = CashFlowFilterSpec.withTxType(state, TransactionType.INCOME)
        assertEquals(TransactionType.INCOME, CashFlowFilterSpec.getTxType(state))

        state = CashFlowFilterSpec.withTxType(state, TransactionType.EXPENSE)
        assertEquals(TransactionType.EXPENSE, CashFlowFilterSpec.getTxType(state))

        state = CashFlowFilterSpec.withTxType(state, TransactionType.TRANSFER)
        assertEquals(TransactionType.TRANSFER, CashFlowFilterSpec.getTxType(state))

        state = CashFlowFilterSpec.withTxType(state, null)
        assertNull(CashFlowFilterSpec.getTxType(state))

        val chips = spec.activeChips(state, LanguageMode.ENGLISH)
        assertTrue(chips.isNotEmpty())
    }

    @Test
    fun testBalanceSheetAndCashFlowFilterStoreRoundtrip() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = FilterStore.getInstance(context)

        // Balance Sheet Save & Load
        val bsState: FilterState = mapOf(
            BalanceSheetFilterSpec.FIELD_DATE to FilterValue.Date(
                presetId = BalanceSheetFilterSpec.PRESET_LAST_30_DAYS,
                startMs = 1000L,
                endMs = 2000L
            ),
            BalanceSheetFilterSpec.FIELD_ACCOUNTS to FilterValue.Select(setOf("101", "102")),
            BalanceSheetFilterSpec.FIELD_STATUSES to FilterValue.Select(setOf("CLEARED")),
            BalanceSheetFilterSpec.FIELD_SORT to FilterValue.Sort(BalanceSheetFilterSpec.SORT_AMOUNT_ASC),
            BalanceSheetFilterSpec.FIELD_EXCLUDE_ZERO to FilterValue.BooleanVal(false),
            BalanceSheetFilterSpec.FIELD_FILTER_NON_ZERO_GROUPS to FilterValue.BooleanVal(true),
            BalanceSheetFilterSpec.FIELD_SHOW_HIDDEN to FilterValue.BooleanVal(true),
            BalanceSheetFilterSpec.FIELD_ONLY_CURRENT to FilterValue.BooleanVal(true),
            BalanceSheetFilterSpec.FIELD_WITHOUT_GROUPS to FilterValue.BooleanVal(true),
            BalanceSheetFilterSpec.FIELD_ACTIVE_TAB to FilterValue.ToggleGroup(setOf(BalanceSheetFilterSpec.TAB_LIABILITIES))
        )
        store.saveFilterState(BalanceSheetFilterSpec.SPEC_KEY, bsState)
        val loadedBs = store.loadFilterState(BalanceSheetFilterSpec.SPEC_KEY)

        assertEquals(BalanceSheetComparisonPreset.LAST_30_DAYS, BalanceSheetFilterSpec.getDatePreset(loadedBs))
        assertEquals(setOf(101L, 102L), BalanceSheetFilterSpec.getSelectedAccountIds(loadedBs))
        assertEquals(setOf(TransactionStatus.CLEARED), BalanceSheetFilterSpec.getSelectedStatuses(loadedBs))
        assertEquals(BalanceSheetSortOrder.AMOUNT_ASC, BalanceSheetFilterSpec.getSortOrder(loadedBs))
        assertFalse(BalanceSheetFilterSpec.getExcludeZero(loadedBs))
        assertTrue(BalanceSheetFilterSpec.getFilterNonZeroGroups(loadedBs))
        assertTrue(BalanceSheetFilterSpec.getShowHidden(loadedBs))
        assertTrue(BalanceSheetFilterSpec.getShowOnlyCurrent(loadedBs))
        assertTrue(BalanceSheetFilterSpec.getShowWithoutGroups(loadedBs))
        assertEquals(BalanceSheetFilterSpec.TAB_LIABILITIES, BalanceSheetFilterSpec.getActiveTab(loadedBs))

        // Cash Flow Save & Load
        val cfState: FilterState = mapOf(
            CashFlowFilterSpec.FIELD_DATE to FilterValue.Date(
                presetId = CashFlowFilterSpec.PRESET_LAST_MONTH,
                startMs = 5000L,
                endMs = 6000L
            ),
            CashFlowFilterSpec.FIELD_SECTION to FilterValue.ToggleGroup(setOf(CashFlowFilterSpec.SEC_TRANSACTIONS)),
            CashFlowFilterSpec.FIELD_ACCOUNTS to FilterValue.Select(setOf("201")),
            CashFlowFilterSpec.FIELD_TX_TYPE to FilterValue.ToggleGroup(setOf(CashFlowFilterSpec.TYPE_INCOME))
        )
        store.saveFilterState(CashFlowFilterSpec.SPEC_KEY, cfState)
        val loadedCf = store.loadFilterState(CashFlowFilterSpec.SPEC_KEY)

        assertEquals(CashFlowPeriodPreset.LAST_MONTH, CashFlowFilterSpec.getPeriodPreset(loadedCf))
        assertEquals(CashFlowTabSection.TRANSACTIONS, CashFlowFilterSpec.getSection(loadedCf))
        assertEquals(setOf(201L), CashFlowFilterSpec.getSelectedAccountIds(loadedCf))
        assertEquals(TransactionType.INCOME, CashFlowFilterSpec.getTxType(loadedCf))
    }

    @Test
    fun testSearchNeverPersistedInFilterStore() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = FilterStore.getInstance(context)

        // Accounts Search Never Persisted
        var accState: FilterState = mapOf(
            AccountsFilterSpec.FIELD_SEARCH to FilterValue.Search("secret account query"),
            AccountsFilterSpec.FIELD_SORT to FilterValue.Sort(AccountsFilterSpec.SORT_AMOUNT_DESC)
        )
        store.saveFilterState(AccountsFilterSpec.SPEC_KEY, accState)
        val loadedAcc = store.loadFilterState(AccountsFilterSpec.SPEC_KEY)
        assertEquals("", AccountsFilterSpec.getSearchQuery(loadedAcc))
        assertEquals(AccountSortFilter.AMOUNT_HIGH_TO_LOW, AccountsFilterSpec.getSort(loadedAcc))

        // Items Search Never Persisted
        var itemsState: FilterState = mapOf(
            ItemsFilterSpec.FIELD_SEARCH to FilterValue.Search("secret item query"),
            ItemsFilterSpec.FIELD_SORT to FilterValue.Sort(ItemsFilterSpec.SORT_COUNT_DESC)
        )
        store.saveFilterState(ItemsFilterSpec.SPEC_KEY, itemsState)
        val loadedItems = store.loadFilterState(ItemsFilterSpec.SPEC_KEY)
        assertEquals("", ItemsFilterSpec.getSearchQuery(loadedItems))
        assertEquals(AggregatedSortOrder.COUNT_DESC, ItemsFilterSpec.getSortOrder(loadedItems))

        // Balance Sheet Search Never Persisted
        var bsState: FilterState = mapOf(
            BalanceSheetFilterSpec.FIELD_SEARCH to FilterValue.Search("secret balance query"),
            BalanceSheetFilterSpec.FIELD_SORT to FilterValue.Sort(BalanceSheetFilterSpec.SORT_NAME_ASC)
        )
        store.saveFilterState(BalanceSheetFilterSpec.SPEC_KEY, bsState)
        val loadedBs = store.loadFilterState(BalanceSheetFilterSpec.SPEC_KEY)
        assertEquals("", BalanceSheetFilterSpec.getSearchQuery(loadedBs))
        assertEquals(BalanceSheetSortOrder.NAME_ASC, BalanceSheetFilterSpec.getSortOrder(loadedBs))

        // Cash Flow Search Never Persisted
        var cfState: FilterState = mapOf(
            CashFlowFilterSpec.FIELD_SEARCH to FilterValue.Search("secret cash flow query"),
            CashFlowFilterSpec.FIELD_SECTION to FilterValue.ToggleGroup(setOf(CashFlowFilterSpec.SEC_STATEMENT))
        )
        store.saveFilterState(CashFlowFilterSpec.SPEC_KEY, cfState)
        val loadedCf = store.loadFilterState(CashFlowFilterSpec.SPEC_KEY)
        assertEquals("", CashFlowFilterSpec.getSearchQuery(loadedCf))
        assertEquals(CashFlowTabSection.STATEMENT, CashFlowFilterSpec.getSection(loadedCf))

        // Net Earnings Search Never Persisted
        var neState: FilterState = mapOf(
            NetEarningsFilterSpec.FIELD_SEARCH to FilterValue.Search("secret net earnings query"),
            NetEarningsFilterSpec.FIELD_SORT to FilterValue.Sort(NetEarningsFilterSpec.SORT_AMOUNT_ASC)
        )
        store.saveFilterState(NetEarningsFilterSpec.SPEC_KEY, neState)
        val loadedNe = store.loadFilterState(NetEarningsFilterSpec.SPEC_KEY)
        assertEquals("", NetEarningsFilterSpec.getSearchQuery(loadedNe))
        assertEquals(NetEarningsFilterSpec.SORT_AMOUNT_ASC, NetEarningsFilterSpec.getSortOrder(loadedNe))
    }

    @Test
    fun testNetEarningsFilterSpecFiltersTogglesSort() {
        val cat1 = Category(id = 1L, nameEn = "Salary", nameBn = "বেতন", type = CategoryType.INCOME)
        val cat2 = Category(id = 2L, nameEn = "Food", nameBn = "খাবার", type = CategoryType.EXPENSE)
        val acc1 = Account(id = 10L, nameEn = "Bank", nameBn = "ব্যাংক", type = com.example.data.model.AccountType.ASSET)
        val acc2 = Account(id = 20L, nameEn = "Cash", nameBn = "নগদ", type = com.example.data.model.AccountType.ASSET)
        val labels = listOf("#bonus", "#tax")

        val spec = NetEarningsFilterSpec.createSpec(
            accounts = listOf(acc1, acc2),
            categories = listOf(cat1, cat2),
            labels = labels
        )
        assertEquals(NetEarningsFilterSpec.SPEC_KEY, spec.key)

        var state: FilterState = emptyMap()
        assertFalse(spec.isActive(state))
        assertEquals(0, spec.activeFilterCount(state))

        // 1. Search Query
        state = state.withSearchQuery("Bonus", NetEarningsFilterSpec.FIELD_SEARCH)
        assertTrue(spec.isActive(state))
        assertEquals("Bonus", NetEarningsFilterSpec.getSearchQuery(state))
        assertEquals(1, spec.activeFilterCount(state))

        // Reset search
        state = state.withSearchQuery("", NetEarningsFilterSpec.FIELD_SEARCH)
        assertFalse(spec.isActive(state))

        // 2. Date Preset
        state = state + (NetEarningsFilterSpec.FIELD_DATE to FilterValue.Date(NetEarningsFilterSpec.PRESET_LAST_MONTH))
        assertTrue(spec.isActive(state))
        assertEquals(NetEarningsFilterSpec.PRESET_LAST_MONTH, NetEarningsFilterSpec.getDatePreset(state))

        // 3. Comparison Preset
        state = state + (NetEarningsFilterSpec.FIELD_COMPARISON to FilterValue.ToggleGroup(setOf(NetEarningsFilterSpec.COMP_LAST_MONTH)))
        assertTrue(NetEarningsFilterSpec.isComparisonEnabled(state))
        assertEquals(NetEarningsFilterSpec.COMP_LAST_MONTH, NetEarningsFilterSpec.getComparisonPreset(state))

        // 4. Flow Scope
        state = state + (NetEarningsFilterSpec.FIELD_FLOW_SCOPE to FilterValue.ToggleGroup(setOf(NetEarningsFilterSpec.SCOPE_INCOME_ONLY)))
        assertEquals(NetEarningsFilterSpec.SCOPE_INCOME_ONLY, NetEarningsFilterSpec.getFlowScope(state))

        // 5. Hierarchy View
        state = state + (NetEarningsFilterSpec.FIELD_HIERARCHY to FilterValue.ToggleGroup(setOf(NetEarningsFilterSpec.HIERARCHY_ONLY_GROUPS)))
        assertEquals(NetEarningsFilterSpec.HIERARCHY_ONLY_GROUPS, NetEarningsFilterSpec.getHierarchyView(state))

        // 6. Categories & Accounts Selection
        state = state + (NetEarningsFilterSpec.FIELD_CATEGORIES to FilterValue.Select(setOf("1", "2")))
        assertEquals(setOf(1L, 2L), NetEarningsFilterSpec.getSelectedCategoryIds(state))

        state = state + (NetEarningsFilterSpec.FIELD_ACCOUNTS to FilterValue.Select(setOf("10", "20")))
        assertEquals(setOf(10L, 20L), NetEarningsFilterSpec.getSelectedAccountIds(state))

        // 7. Labels & Statuses
        state = state + (NetEarningsFilterSpec.FIELD_LABELS to FilterValue.Select(setOf("#bonus")))
        assertEquals(setOf("#bonus"), NetEarningsFilterSpec.getSelectedLabels(state))

        state = state + (NetEarningsFilterSpec.FIELD_STATUSES to FilterValue.Select(setOf("CLEARED", "RECONCILED")))
        assertEquals(setOf(TransactionStatus.CLEARED, TransactionStatus.RECONCILED), NetEarningsFilterSpec.getSelectedStatuses(state))

        // 8. Amount Range
        state = state + (NetEarningsFilterSpec.FIELD_AMOUNT_RANGE to FilterValue.Range(min = 100.0, max = 5000.0))
        assertEquals(100.0, NetEarningsFilterSpec.getMinAmount(state))
        assertEquals(5000.0, NetEarningsFilterSpec.getMaxAmount(state))

        // 9. Sort Order
        state = state + (NetEarningsFilterSpec.FIELD_SORT to FilterValue.Sort(NetEarningsFilterSpec.SORT_NAME_ASC))
        assertEquals(NetEarningsFilterSpec.SORT_NAME_ASC, NetEarningsFilterSpec.getSortOrder(state))

        // 10. Toggles
        state = state + (NetEarningsFilterSpec.FIELD_TOGGLES to FilterValue.ToggleGroup(
            setOf(
                NetEarningsFilterSpec.TOGGLE_EXCLUDE_ZERO,
                NetEarningsFilterSpec.TOGGLE_INCLUDE_TRANSFERS,
                NetEarningsFilterSpec.TOGGLE_WITHOUT_GROUPS
            )
        ))
        assertTrue(NetEarningsFilterSpec.getExcludeZero(state))
        assertFalse(NetEarningsFilterSpec.getHideEmptyGroups(state))
        assertTrue(NetEarningsFilterSpec.getIncludeTransfers(state))
        assertTrue(NetEarningsFilterSpec.getShowWithoutGroups(state))
        assertFalse(NetEarningsFilterSpec.getDisplayCurrency(state))
        assertFalse(NetEarningsFilterSpec.getDisplayCurrencySymbol(state))

        // 11. Test Predicates on Sample Transactions
        val txFieldCat = spec.fields.filterIsInstance<FilterField.SelectField<TransactionWithDetails>>().find { it.id == NetEarningsFilterSpec.FIELD_CATEGORIES }
        assertNotNull(txFieldCat)

        val tx1 = TransactionWithDetails(
            transaction = Transaction(id = 100L, type = TransactionType.INCOME, amount = 250.0, dateEpochMs = System.currentTimeMillis(), categoryId = 1L, debitAccountId = 10L, status = TransactionStatus.CLEARED, note = "Salary #bonus"),
            category = cat1,
            debitAccount = acc1
        )
        val tx2 = TransactionWithDetails(
            transaction = Transaction(id = 101L, type = TransactionType.EXPENSE, amount = 50.0, dateEpochMs = System.currentTimeMillis(), categoryId = 99L, debitAccountId = 99L, status = TransactionStatus.VOID, note = "Misc"),
            category = null,
            debitAccount = null
        )

        assertTrue(txFieldCat!!.predicate?.invoke(tx1, setOf("1")) == true)
        assertFalse(txFieldCat.predicate?.invoke(tx2, setOf("1")) == true)

        val txFieldRange = spec.fields.filterIsInstance<FilterField.RangeField<TransactionWithDetails>>().find { it.id == NetEarningsFilterSpec.FIELD_AMOUNT_RANGE }
        assertNotNull(txFieldRange)
        assertTrue(txFieldRange!!.predicate?.invoke(tx1, 100.0, 500.0) == true)
        assertFalse(txFieldRange.predicate?.invoke(tx2, 100.0, 500.0) == true)
    }

    @Test
    fun testNetEarningsFilterSpecCalculateRanges() {
        val presets = listOf(
            NetEarningsFilterSpec.PRESET_THIS_MONTH,
            NetEarningsFilterSpec.PRESET_LAST_MONTH,
            NetEarningsFilterSpec.PRESET_LAST_3_MONTHS,
            NetEarningsFilterSpec.PRESET_LAST_6_MONTHS,
            NetEarningsFilterSpec.PRESET_LAST_12_MONTHS,
            NetEarningsFilterSpec.PRESET_YEAR_TO_DATE,
            NetEarningsFilterSpec.PRESET_SAME_MONTH_LAST_YEAR,
            NetEarningsFilterSpec.PRESET_ALL_TIME,
            NetEarningsFilterSpec.PRESET_CUSTOM
        )

        for (preset in presets) {
            val state: FilterState = mapOf(
                NetEarningsFilterSpec.FIELD_DATE to FilterValue.Date(
                    presetId = preset,
                    startMs = 1700000000000L,
                    endMs = 1705000000000L
                ),
                NetEarningsFilterSpec.FIELD_COMPARISON to FilterValue.ToggleGroup(setOf(NetEarningsFilterSpec.COMP_LAST_MONTH))
            )

            val resultEn = NetEarningsFilterSpec.calculateRanges(2026, 10, state, LanguageMode.ENGLISH)
            assertTrue("Primary range valid for $preset", resultEn.primaryRange.first <= resultEn.primaryRange.second)
            assertTrue("Label not blank for $preset", resultEn.primaryLabel.isNotBlank())

            val resultBn = NetEarningsFilterSpec.calculateRanges(2026, 10, state, LanguageMode.BANGLA)
            assertTrue("Primary range valid for $preset (BN)", resultBn.primaryRange.first <= resultBn.primaryRange.second)
            assertTrue("Label not blank for $preset (BN)", resultBn.primaryLabel.isNotBlank())

            assertNotNull(resultEn.compareRange)
            assertNotNull(resultEn.compareLabel)
        }
    }

    @Test
    fun testNetEarningsFilterStoreSaveLoadRoundTrip() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = FilterStore.getInstance(context)

        val fullState: FilterState = mapOf(
            NetEarningsFilterSpec.FIELD_DATE to FilterValue.Date(
                presetId = NetEarningsFilterSpec.PRESET_LAST_6_MONTHS,
                startMs = 1000L,
                endMs = 2000L
            ),
            NetEarningsFilterSpec.FIELD_COMPARISON to FilterValue.ToggleGroup(setOf(NetEarningsFilterSpec.COMP_SAME_MONTH_LAST_YEAR)),
            NetEarningsFilterSpec.FIELD_FLOW_SCOPE to FilterValue.ToggleGroup(setOf(NetEarningsFilterSpec.SCOPE_SURPLUS_ONLY)),
            NetEarningsFilterSpec.FIELD_HIERARCHY to FilterValue.ToggleGroup(setOf(NetEarningsFilterSpec.HIERARCHY_ONLY_ITEMS)),
            NetEarningsFilterSpec.FIELD_CATEGORIES to FilterValue.Select(setOf("5", "6")),
            NetEarningsFilterSpec.FIELD_ACCOUNTS to FilterValue.Select(setOf("15", "25")),
            NetEarningsFilterSpec.FIELD_LABELS to FilterValue.Select(setOf("#tax")),
            NetEarningsFilterSpec.FIELD_STATUSES to FilterValue.Select(setOf("CLEARED")),
            NetEarningsFilterSpec.FIELD_AMOUNT_RANGE to FilterValue.Range(min = 50.0, max = 1500.0),
            NetEarningsFilterSpec.FIELD_SORT to FilterValue.Sort(NetEarningsFilterSpec.SORT_PERCENTAGE_DESC),
            NetEarningsFilterSpec.FIELD_TOGGLES to FilterValue.ToggleGroup(
                setOf(
                    NetEarningsFilterSpec.TOGGLE_EXCLUDE_ZERO,
                    NetEarningsFilterSpec.TOGGLE_HIDE_EMPTY_GROUPS
                )
            ),
            NetEarningsFilterSpec.FIELD_SEARCH to FilterValue.Search("temp query")
        )

        store.saveFilterState(NetEarningsFilterSpec.SPEC_KEY, fullState)
        val loaded = store.loadFilterState(NetEarningsFilterSpec.SPEC_KEY)

        assertEquals(NetEarningsFilterSpec.PRESET_LAST_6_MONTHS, NetEarningsFilterSpec.getDatePreset(loaded))
        assertEquals(1000L, NetEarningsFilterSpec.getCustomStartDate(loaded))
        assertEquals(2000L, NetEarningsFilterSpec.getCustomEndDate(loaded))
        assertEquals(NetEarningsFilterSpec.COMP_SAME_MONTH_LAST_YEAR, NetEarningsFilterSpec.getComparisonPreset(loaded))
        assertTrue(NetEarningsFilterSpec.isComparisonEnabled(loaded))
        assertEquals(NetEarningsFilterSpec.SCOPE_SURPLUS_ONLY, NetEarningsFilterSpec.getFlowScope(loaded))
        assertEquals(NetEarningsFilterSpec.HIERARCHY_ONLY_ITEMS, NetEarningsFilterSpec.getHierarchyView(loaded))
        assertEquals(setOf(5L, 6L), NetEarningsFilterSpec.getSelectedCategoryIds(loaded))
        assertEquals(setOf(15L, 25L), NetEarningsFilterSpec.getSelectedAccountIds(loaded))
        assertEquals(setOf("#tax"), NetEarningsFilterSpec.getSelectedLabels(loaded))
        assertEquals(setOf(TransactionStatus.CLEARED), NetEarningsFilterSpec.getSelectedStatuses(loaded))
        assertEquals(50.0, NetEarningsFilterSpec.getMinAmount(loaded))
        assertEquals(1500.0, NetEarningsFilterSpec.getMaxAmount(loaded))
        assertEquals(NetEarningsFilterSpec.SORT_PERCENTAGE_DESC, NetEarningsFilterSpec.getSortOrder(loaded))
        assertTrue(NetEarningsFilterSpec.getExcludeZero(loaded))
        assertTrue(NetEarningsFilterSpec.getHideEmptyGroups(loaded))
        assertFalse(NetEarningsFilterSpec.getDisplayCurrency(loaded))
        assertFalse(NetEarningsFilterSpec.getDisplayCurrencySymbol(loaded))
        // Search query should not be persisted
        assertEquals("", NetEarningsFilterSpec.getSearchQuery(loaded))
    }

    @Test
    fun testNetEarningsFilterStoreMigrationAllEnumsWithFallbacks() {
        // Date Presets mapping
        assertEquals(NetEarningsFilterSpec.PRESET_THIS_MONTH, FilterStore.mapNetEarningsDatePreset(BudgetDateRangePreset.THIS_MONTH))
        assertEquals(NetEarningsFilterSpec.PRESET_LAST_MONTH, FilterStore.mapNetEarningsDatePreset(BudgetDateRangePreset.LAST_MONTH))
        assertEquals(NetEarningsFilterSpec.PRESET_LAST_3_MONTHS, FilterStore.mapNetEarningsDatePreset(BudgetDateRangePreset.LAST_3_MONTHS))
        assertEquals(NetEarningsFilterSpec.PRESET_LAST_6_MONTHS, FilterStore.mapNetEarningsDatePreset(BudgetDateRangePreset.LAST_6_MONTHS))
        assertEquals(NetEarningsFilterSpec.PRESET_LAST_12_MONTHS, FilterStore.mapNetEarningsDatePreset(BudgetDateRangePreset.LAST_12_MONTHS))
        assertEquals(NetEarningsFilterSpec.PRESET_YEAR_TO_DATE, FilterStore.mapNetEarningsDatePreset(BudgetDateRangePreset.YEAR_TO_DATE))
        assertEquals(NetEarningsFilterSpec.PRESET_SAME_MONTH_LAST_YEAR, FilterStore.mapNetEarningsDatePreset(BudgetDateRangePreset.SAME_MONTH_LAST_YEAR))
        assertEquals(NetEarningsFilterSpec.PRESET_ALL_TIME, FilterStore.mapNetEarningsDatePreset(BudgetDateRangePreset.ALL_TIME))
        assertEquals(NetEarningsFilterSpec.PRESET_CUSTOM, FilterStore.mapNetEarningsDatePreset(BudgetDateRangePreset.CUSTOM))
        assertEquals(NetEarningsFilterSpec.PRESET_THIS_MONTH, FilterStore.mapNetEarningsDatePreset(null))

        // Hierarchy mapping
        assertEquals(NetEarningsFilterSpec.HIERARCHY_GROUPED, FilterStore.mapNetEarningsHierarchy(NetEarningsHierarchyView.GROUPED))
        assertEquals(NetEarningsFilterSpec.HIERARCHY_ONLY_GROUPS, FilterStore.mapNetEarningsHierarchy(NetEarningsHierarchyView.ONLY_GROUPS))
        assertEquals(NetEarningsFilterSpec.HIERARCHY_ONLY_ITEMS, FilterStore.mapNetEarningsHierarchy(NetEarningsHierarchyView.ONLY_ITEMS))
        assertEquals(NetEarningsFilterSpec.HIERARCHY_GROUPED, FilterStore.mapNetEarningsHierarchy(null))

        // Sort mapping
        assertEquals(NetEarningsFilterSpec.SORT_AMOUNT_DESC, FilterStore.mapNetEarningsSort(NetEarningsSort.AMOUNT_HIGH_TO_LOW))
        assertEquals(NetEarningsFilterSpec.SORT_AMOUNT_ASC, FilterStore.mapNetEarningsSort(NetEarningsSort.AMOUNT_LOW_TO_HIGH))
        assertEquals(NetEarningsFilterSpec.SORT_PERCENTAGE_DESC, FilterStore.mapNetEarningsSort(NetEarningsSort.PERCENTAGE_HIGH_TO_LOW))
        assertEquals(NetEarningsFilterSpec.SORT_NAME_ASC, FilterStore.mapNetEarningsSort(NetEarningsSort.NAME_A_TO_Z))
        assertEquals(NetEarningsFilterSpec.SORT_NET_DESC, FilterStore.mapNetEarningsSort(null))

        // Model sort mapping
        assertEquals(NetEarningsFilterSpec.SORT_NET_DESC, FilterStore.mapNetEarningsModelSort(NetEarningsSortOrder.NET_DESC))
        assertEquals(NetEarningsFilterSpec.SORT_NET_ASC, FilterStore.mapNetEarningsModelSort(NetEarningsSortOrder.NET_ASC))
        assertEquals(NetEarningsFilterSpec.SORT_AMOUNT_DESC, FilterStore.mapNetEarningsModelSort(NetEarningsSortOrder.AMOUNT_DESC))
        assertEquals(NetEarningsFilterSpec.SORT_AMOUNT_ASC, FilterStore.mapNetEarningsModelSort(NetEarningsSortOrder.AMOUNT_ASC))
        assertEquals(NetEarningsFilterSpec.SORT_NAME_ASC, FilterStore.mapNetEarningsModelSort(NetEarningsSortOrder.NAME_ASC))
        assertEquals(NetEarningsFilterSpec.SORT_TXN_COUNT_DESC, FilterStore.mapNetEarningsModelSort(NetEarningsSortOrder.TXN_COUNT_DESC))
        assertEquals(NetEarningsFilterSpec.SORT_DEFAULT, FilterStore.mapNetEarningsModelSort(NetEarningsSortOrder.DEFAULT))
        assertEquals(NetEarningsFilterSpec.SORT_NET_DESC, FilterStore.mapNetEarningsModelSort(null))

        // Flow scope mapping
        assertEquals(NetEarningsFilterSpec.SCOPE_ALL, FilterStore.mapNetEarningsFlowScope(NetEarningsFlowScope.ALL))
        assertEquals(NetEarningsFilterSpec.SCOPE_SURPLUS_ONLY, FilterStore.mapNetEarningsFlowScope(NetEarningsFlowScope.SURPLUS_ONLY))
        assertEquals(NetEarningsFilterSpec.SCOPE_DEFICIT_ONLY, FilterStore.mapNetEarningsFlowScope(NetEarningsFlowScope.DEFICIT_ONLY))
        assertEquals(NetEarningsFilterSpec.SCOPE_INCOME_ONLY, FilterStore.mapNetEarningsFlowScope(NetEarningsFlowScope.INCOME_ONLY))
        assertEquals(NetEarningsFilterSpec.SCOPE_EXPENSE_ONLY, FilterStore.mapNetEarningsFlowScope(NetEarningsFlowScope.EXPENSE_ONLY))
        assertEquals(NetEarningsFilterSpec.SCOPE_ALL, FilterStore.mapNetEarningsFlowScope(null))

        // Comparison preset mapping
        assertEquals(NetEarningsFilterSpec.COMP_SAME_DATE_PREV_MONTH, FilterStore.mapNetEarningsComparisonPreset(BudgetComparisonPreset.SAME_DATE_PREV_MONTH))
        assertEquals(NetEarningsFilterSpec.COMP_LAST_MONTH, FilterStore.mapNetEarningsComparisonPreset(BudgetComparisonPreset.LAST_MONTH))
        assertEquals(NetEarningsFilterSpec.COMP_SAME_MONTH_LAST_YEAR, FilterStore.mapNetEarningsComparisonPreset(BudgetComparisonPreset.SAME_MONTH_LAST_YEAR))
        assertEquals(NetEarningsFilterSpec.COMP_LAST_3_MONTHS_AVG, FilterStore.mapNetEarningsComparisonPreset(BudgetComparisonPreset.LAST_3_MONTHS_AVG))
        assertEquals(NetEarningsFilterSpec.COMP_LAST_YEAR, FilterStore.mapNetEarningsComparisonPreset(BudgetComparisonPreset.LAST_YEAR))
        assertEquals(NetEarningsFilterSpec.COMP_CUSTOM, FilterStore.mapNetEarningsComparisonPreset(BudgetComparisonPreset.CUSTOM))
        assertEquals(NetEarningsFilterSpec.COMP_NONE, FilterStore.mapNetEarningsComparisonPreset(null))

        // Test migration execution with custom TabFilterPreferences
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tabPrefs = TabFilterPreferences.getInstance(context)
        tabPrefs.reportsDatePreset = BudgetDateRangePreset.LAST_3_MONTHS
        tabPrefs.reportsHierarchyView = NetEarningsHierarchyView.ONLY_ITEMS
        tabPrefs.reportsSortOption = NetEarningsSort.NAME_A_TO_Z
        tabPrefs.reportsTabMode = "INCOME"

        val store = FilterStore.getInstance(context)
        store.migrateFromTabFilterPreferences(tabPrefs)

        val migratedState = store.loadFilterState(NetEarningsFilterSpec.SPEC_KEY)
        assertEquals(NetEarningsFilterSpec.PRESET_LAST_3_MONTHS, NetEarningsFilterSpec.getDatePreset(migratedState))
        assertEquals(NetEarningsFilterSpec.HIERARCHY_ONLY_ITEMS, NetEarningsFilterSpec.getHierarchyView(migratedState))
        assertEquals(NetEarningsFilterSpec.SORT_NAME_ASC, NetEarningsFilterSpec.getSortOrder(migratedState))
        assertEquals(NetEarningsFilterSpec.SCOPE_INCOME_ONLY, NetEarningsFilterSpec.getFlowScope(migratedState))
        // Verify default toggles were applied since no custom toggle prefs were present
        assertTrue(NetEarningsFilterSpec.getExcludeZero(migratedState))
        assertTrue(NetEarningsFilterSpec.getHideEmptyGroups(migratedState))
        assertTrue(NetEarningsFilterSpec.getDisplayCurrency(migratedState))
        assertTrue(NetEarningsFilterSpec.getDisplayCurrencySymbol(migratedState))
    }
}
