# Performance Optimization Plan: Smooth Startup, Tab Caching & Transaction Pagination

## Overview
This plan addresses the UI lag and latency reported during app startup, tab swiping, and list scrolling. Because tab data does not change unless user transactions or accounts are modified, tab UI states can be computed once on background threads and cached, eliminating heavy runtime calculations and eager recompositions.

---

## 1. Root Cause Analysis

1. **Heavy Tab Recomposition During HorizontalPager Swiping**:
   - `HorizontalPager` in `MainAppContainer.kt` was performing real-time page offset fractions and graphics scale/alpha transformations in composable scope on every touch gesture frame.
   - Child screens inside `ScreenRouter` collect multiple raw database flows simultaneously, triggering cascade recompositions while the pager is animating.
2. **Startup & Eager Data Evaluation**:
   - When `BudgetViewModel` initializes, it maps and combines multiple raw Room Flows (`transactions`, `categories`, `accounts`, `labels`, `budgets`) on the main dispatcher without deferring or memoizing intermediate groupings.
   - All tabs compete for CPU cycles at startup to calculate totals, category distributions, daily running balances, and monthly summaries.
3. **Unpaginated Transaction Lists**:
   - `TransactionsScreen` and dashboard transaction lists render all filtered and unfiltered transactions without chunking or pagination. For larger transaction databases, this creates hundreds of Compose items with expensive date-header dividers and balance computations.
4. **Repeated Filtering & Aggregation Math**:
   - Filter operations (date ranges, labels, categories, accounts) re-iterate all transactions sequentially on state changes without background memoization.

---

## 2. Proposed Architecture & Solutions

### A. Pre-Computed Tab State Caching in ViewModel
- **Background StateFlows**: Move all heavy data aggregations (Totals, Net Earnings summary, Items Summary, Category breakdowns, and Account balances) into dedicated `StateFlow` holders with `.stateIn(viewModelScope, SharingStarted.Eagerly, ...)`, dispatched via `Dispatchers.Default`.
- **Cached Tab Models**: Introduce immutable, stable data models for each tab:
  - `DashboardTabState` (pre-computed summary stats, recent 20 transactions, alert badges)
  - `TransactionsTabState` (paginated transaction list chunks, active filters, quick filter counters)
  - `BudgetsTabState` (pre-computed budget progress, monthly limits, category status)
  - `NetEarningsTabState` (pre-aggregated monthly/yearly earnings and cash flows)
  - `ReportsTabState` (pre-binned chart data, distribution buckets)
- When a transaction or account updates, only the relevant cached flow recomputes off the main thread; swiping between tabs simply reads already-materialized states with zero computation overhead.

### B. HorizontalPager Swiping Smoothness
- **Simplify Pager Transformations**: Replace per-frame composition-level math with zero-recomposition draw-phase modifiers (`Modifier.graphicsLayer { ... }` using lambdas that do not read state during composition).
- **Controlled Page Retention**: Set `beyondViewportPageCount = 1` so neighboring tabs are retained in memory with their cached view state, preventing layout re-inflation when swiping left or right.
- **Stable ScreenRouter Lambdas**: Ensure all navigation callbacks and action lambdas passed to child screens are wrapped in `remember` or method references to eliminate redundant child recompositions.

### C. Transaction List Pagination & Windowing
- **Paging / Windowed Lazy Loading**:
  - Implement a chunked windowed display (e.g., loading the first 30 transactions initially, and appending subsequent chunks of 30 as the user scrolls near the bottom using `derivedStateOf`).
  - Pre-group transactions by date on `Dispatchers.Default` rather than grouping inside the Composable function body.
- **LazyColumn Enhancements**:
  - Supply explicit `contentType = { ... }` for headers vs transaction rows to maximize Compose view recycling.
  - Ensure stable unique `key = { it.id }` for every transaction item.
  - Implement item height estimation to avoid layout jumps during fast fling scrolling.

### D. Startup Optimization
- **Staggered Initialization**: Prioritize loading the initial active tab (Dashboard) first. Defer secondary tabs (Reports, Net Earnings, Wishlist, Budget Maker) until after initial frame render using lazy Flow collection or coroutine start modes.
- **Fast Room Database Queries**: Use Room indexed queries and projection views where only required columns are queried for summary metrics.

---

## 3. Step-by-Step Implementation Strategy

1. **Step 1: Background Computation & Caching Layer in `BudgetViewModel`**
   - Create memoized flows for each tab's display model using `Dispatchers.Default`.
   - Pre-compute grouped transactions by date map off the UI thread.
2. **Step 2: Smooth Pager Rendering in `MainAppContainer.kt`**
   - Streamline `HorizontalPager` modifier structure.
   - Retain adjacent pages (`beyondViewportPageCount = 1`) with cached views.
   - Decouple top/bottom chrome hiding from pager scroll gestures to prevent stutter.
3. **Step 3: Chunked Pagination in `TransactionsScreen.kt`**
   - Introduce infinite scrolling / chunked pagination (loads in blocks of 30).
   - Add `contentType` and stable keys to `LazyColumn`.
   - Offload search and multi-criteria filter predicates to background coroutines.
4. **Step 4: Verification & Build Check**
   - Execute `compile_applet` to verify compilation.
   - Validate version code and name bump per user rules.
