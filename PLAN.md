# Implementation Plan: Swipe Indicator Positioning & Design Overhaul

**Target Version:** v1.1.6 (versionCode 17)  
**Objective:** Grill and improve the positioning, aesthetics, and gesture responsiveness of swipe indicators across all screens and slots in StandBy Android.

---

## 1. Problem Analysis: Why It Looks Bad Now

When indicators were moved to `Alignment.TopCenter`, several severe design and UX issues emerged:
1. **Header & Clock Numeral Collisions:**
   - On `RectangleAnalogClockWidget` (left slot), `TopCenter` places indicator dots directly on top of the "12" numeral and cardinal anchor ray.
   - On `MonthCalendarWidget` (right slot), `TopCenter` directly collides with the bold red month header (`"SEPTEMBER"`).
   - On `WeatherWidget` (right slot), `TopCenter` floats awkwardly above left-aligned city names (`"Cupertino"`).
2. **Gesture-Direction Incongruity:**
   - Slot cards swipe **vertically** (up/down), but rendering horizontal dots at the top creates cognitive dissonance. Vertical stacks are naturally represented by vertical indicators.
3. **Screen Pager vs Slot Pager Disconnection:**
   - The root horizontal screen indicator at `top = 58.dp` clashes with slot-level indicators when both appear, creating a double-row of dots at the top of the screen.
4. **Lack of Contrast & Polish:**
   - Indicator dots lack subtle glassmorphic backdrop pills or blur, making them hard to read against diverse widget content (e.g. PhotoFrame or bright album art).

---

## 2. Proposed Architecture & Design Solution

1. **Screen-Level Pager Indicator (Horizontal: Bento | Single | Clocks | Music):**
   - Position: Floating overlay at `Alignment.TopCenter`.
   - When Top Navigation Menu is open: neatly integrated as subtle sub-indicator or aligned at `top = 54.dp` with balanced spacing.
   - When Top Navigation Menu is hidden (during active swipe): floats cleanly at `top = 16.dp` with a subtle frosted glass pill capsule (`background = Color(0x33000000)`, `border = 0.5.dp Color(0x22FFFFFF)`).
   - Auto-hides after 4.5s of inactivity.
2. **Slot Card Pagers (Vertical Widget Stacks in Bento Mode):**
   - Direction: Pure vertical indicator (`VerticalPagerIndicator`) to match the vertical swipe motion.
   - Position: Floating overlay on the **outer edge / gutter** of each card:
     - Left Slot: anchored at `Alignment.CenterStart` (padding start = 6.dp) or `Alignment.TopEnd` (padding top = 12.dp, end = 12.dp).
     - Right Slot: anchored at `Alignment.CenterEnd` (padding end = 6.dp) or `Alignment.TopEnd` (padding top = 12.dp, end = 12.dp).
   - Crucially: It is a **true floating Z-index overlay (`Modifier.zIndex(10f)`) with ZERO layout push/squeezing** on the card content! The card retains 100% full width and height.
   - Auto-hides completely after 4.5s or when idle; illuminates instantly on drag (`pagerState.isScrollInProgress`).
3. **Single Module Page Indicator (Vertical Pager on Page 1):**
   - Position: Floating subtle vertical hairline pill on the right gutter (`Alignment.CenterEnd`, padding end = 12.dp), with frosted glass capsule so it never collides with centered clocks or full-bleed weather telemetry.
4. **Visual Refinement:**
   - Apple-style micro-pill geometry: active dot extends to a smooth rounded pill, inactive dots are subtle 0.25 alpha circular beads.
   - Backed by an ultra-subtle ambient blur or translucent dark capsule to guarantee contrast against any widget background.

---

## 3. Live Task List

- [✔] **Task 1: Subagent Design & Code Grill:** Deploy a specialized subagent to audit indicator placements across all 14 widgets in both landscape and portrait orientations. (Completed: Subagent `383b1d22-16b9-497b-b1d9-f698246efed6` audited layout collisions and confirmed vertical outer-gutter overlay architecture).
- [✔] **Task 2: PagerIndicators Component Polish:** Upgrade `PagerIndicators.kt` with pill capsules, smooth spring transitions, and ambient contrast backplates. (Completed: Implemented frosted capsule backplate, `CircleShape` border, bouncy spring morphing, and contrast isolation).
- [✔] **Task 3: DynamicSlotCard Indicator Architecture:** Refactor `DynamicSlotCard.kt` to place non-colliding floating vertical indicators on outer gutters with zero layout push. (Completed: Converted to transient `VerticalPagerIndicator` at `Alignment.CenterEnd` with capsule backdrop, zero content push).
- [✔] **Task 4: MainStandbyPager Indicator Architecture:** Refactor `MainStandbyPager.kt` screen indicator and single-module indicator for seamless overlay integration. (Completed: Single module converted to vertical indicator at `CenterEnd`; root horizontal indicator made transient at `top = 16.dp` with zero collision with top pill handle or cards).
- [✔] **Task 5: Rectangle Analog Clock Full-Bleed & Corner Radius Rework:** (Completed: Removed square constraint so clock uses 100% full width and height with tight 8dp optical margin. Replaced superellipse with authentic modern rounded-rectangle geometry, analytic ray intersection with perpendicular edge normals, and proportional numeral/hand scaling).
- [✔] **Task 6: Adversarial Auditor Grill Loop:** Run independent auditor to stress-test the new indicator system and reworked rectangle clock until a clean PASS is achieved. (Completed: Auditor issued definitive VERDICT: PASS (10/10) after all 4 failure domains were verified and audit_linter.py passed).
- [✔] **Task 7: Build 17 Release & Deployment:** Run `turn_runner.sh` to compile R8 bundle, upload to Google Play internal track, and commit to GitHub. (Completed: AAB v1.1.6 Code 17 uploaded to Google Play Internal Track Edit ID 15816687478392299351 and pushed to GitHub).
