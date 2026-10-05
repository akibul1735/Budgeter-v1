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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.ui.components.filter.FilterField
import com.example.ui.dialogs.AggregatedDatePreset
import com.example.ui.dialogs.AggregatedSortOrder

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
}
