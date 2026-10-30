# Implementation Plan: App Open Loading Animation

Introduce an engaging, high-fidelity app launch loading animation featuring Budgeter's signature emerald green and gold Bangladeshi Taka emblem, smooth pulsing shimmer, dynamic loading status indicators, and an immediate seamless transition into the app once initial data loads (with a minimum presentation time of 800ms).

## 1. User Alignment & Design Specification
- **Visual Aesthetic**:
  - Emerald green circular emblem (`#059669` / `#10B981`) with golden crescent accent (`#F59E0B`) and white Bengali Taka symbol (`৳`).
  - Smooth rhythmic pulse animation combined with subtle radial glow / shimmer around the emblem.
  - Distinct app title typography ("Budgeter" / "বাজেটার") with clean subtitle tagline ("Smart Personal Finance & Budget Tracker" / "ব্যক্তিগত বাজেট ও ব্যয় ট্র্যাকার").
- **Dynamic Status Progression**:
  - Cycling animated status hints simulating smooth readiness (e.g., "Initializing workspace...", "Loading your budget...", "Preparing accounts & categories...", "Ready!").
  - Subtle animated indeterminate progress line with emerald-to-gold gradient sweep.
- **Timing & Transition**:
  - Respects minimum 800ms presentation time so the animation feels intentional and polished rather than jittery.
  - Automatically transitions out via crossfade alpha transition once database state flows (`allAccounts`, `allCategories`, `monthlyBudgets`, `transactionsWithDetails`) are warm and ready.
  - If App Lock (PIN screen) is enabled, the loading screen precedes it seamlessly before presenting the secure lock screen.

---

## 2. Proposed Architecture & Component Design

### New Composable Component
- **`com.example.ui.components.AppSplashScreen`**:
  - Full-screen container with status bar & navigation bar handling (`fillMaxSize()`, edge-to-edge aware).
  - Background: Rich deep theme-aware dark/light gradient (subtle emerald tint).
  - Emblem: Custom-rendered circular split-tone emerald container with golden crescent and crisp white Taka (`৳`) symbol with `rememberInfiniteTransition` scale and glow pulse.
  - Shimmer: Horizontal animated gradient sweep over loading status text.
  - Dynamic Status: Coroutine-backed sequential state flow reflecting actual initialization steps with localized strings (Bangla / English support).
  - Animated exit transition: `AnimatedVisibility` with `fadeOut(animationSpec = tween(350))` overlaying `MainAppContainer`.

### State & Integration in `MainAppContainer.kt` / `MainActivity.kt`
- Introduce `isInitialLoading` state in `MainAppContainer` (or a dedicated splash gate in `MainActivity`).
- Trigger launch timer (`LaunchedEffect(Unit)`) ensuring `minDisplayTime = 800L`.
- Check data readiness (`allAccounts != null && allCategories != null && monthlyBudgets != null`).
- Once conditions are met and timer expires, smoothly dismiss splash with crossfade reveal of the main dashboard / app content.

---

## 3. Verification & Validation Plan
1. **Compilation Verification**:
   - Run `compile_applet` to ensure zero compilation or build errors.
2. **Automated Unit & Local Testing**:
   - Run `gradle :app:testDebugUnitTest` to ensure all existing unit tests pass.
   - Verify splash dismiss logic behaves deterministically under fast state emissions.
3. **Visual & UI Checks**:
   - Verify the emerald-gold emblem pulses smoothly without dropped frames.
   - Verify dynamic status text cycles cleanly in both English and Bangla.
   - Verify smooth 350ms fade-out transition into Dashboard without flicker.
   - Verify proper auto-versioning bump to `versionCode = 124` and `versionName = "4.12"`.
