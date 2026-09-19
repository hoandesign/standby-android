# StandBy Android Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete, modern Android StandBy mode application inspired by iOS 17/18 StandBy and leading smart displays, featuring dual-axis gesture navigation, adaptive layouts for foldables/tall screens, 14 rich widget modules, OLED burn-in protection, ambient red night mode, and automated Google Play Console deployment.

**Architecture:** Built entirely in Kotlin and Jetpack Compose with Material 3. Implements an `AdaptiveLayoutEngine` that dynamically transitions between landscape side-by-side stacks, squarish foldable 2x2 Quad-Bento grids, and vertical portrait docks. Background services include `ChargingReceiver` for automatic docking wake-up and `StandbyDreamService` for native Android screen saver integration.

**Tech Stack:** Kotlin 2.0+, Jetpack Compose, Material 3, AndroidX Lifecycle & ViewModel, StateFlow, Android BatteryManager, SensorManager (Ambient Light), MediaSession API, Open-Meteo API, Gradle Kotlin DSL, Android Publisher API v3 for Play Console.

---

### File Structure Map

```
standby-android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/hoandesign/standby/
│   │   │   ├── MainActivity.kt
│   │   │   ├── StandbyDreamService.kt
│   │   │   ├── receiver/ChargingReceiver.kt
│   │   │   ├── model/ (WidgetType, ScreenRatio, NightModeState, WeatherData, BatteryState, MediaState)
│   │   │   ├── ui/
│   │   │   │   ├── theme/ (Color.kt, Type.kt, Theme.kt)
│   │   │   │   ├── components/
│   │   │   │   │   ├── PixelShifter.kt
│   │   │   │   │   ├── PagerIndicators.kt
│   │   │   │   │   └── NightModeOverlay.kt
│   │   │   │   ├── layout/
│   │   │   │   │   ├── AdaptiveLayoutEngine.kt
│   │   │   │   │   ├── DualStackContainer.kt
│   │   │   │   │   └── QuadBentoContainer.kt
│   │   │   │   └── widgets/
│   │   │   │       ├── clock/
│   │   │   │       │   ├── AnalogClockWidget.kt
│   │   │   │       │   ├── BigDigitalClockWidget.kt
│   │   │   │       │   ├── RetroFlipClockWidget.kt
│   │   │   │       │   ├── SolarArcClockWidget.kt
│   │   │   │       │   └── RadialClockWidget.kt
│   │   │   │       ├── calendar/
│   │   │   │       │   ├── MonthCalendarWidget.kt
│   │   │   │       │   └── AgendaWidget.kt
│   │   │   │       ├── weather/WeatherWidget.kt
│   │   │   │       ├── battery/BatteryWidget.kt
│   │   │   │       ├── media/MusicPlayerWidget.kt
│   │   │   │       ├── timer/DeskTimerWidget.kt
│   │   │   │       ├── system/SystemBentoWidget.kt
│   │   │   │       ├── vibes/VibesWidget.kt
│   │   │   │       └── photo/PhotoFrameWidget.kt
│   │   │   └── data/
│   │   │       ├── BatteryMonitor.kt
│   │   │       ├── LightSensorMonitor.kt
│   │   │       ├── WeatherRepository.kt
│   │   │       └── MediaPlaybackManager.kt
│   │   └── res/ (drawable, values, mipmap, xml)
│   └── build.gradle.kts
├── scripts/
│   ├── play_upload_internal.py
│   └── ship_play_internal.sh
├── docs/superpowers/
│   ├── specs/2026-09-20-standby-android-design.md
│   └── plans/2026-09-20-standby-android-implementation.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

### Task 1: Project Scaffolding & Gradle Build Setup

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`

- [ ] **Step 1: Create `settings.gradle.kts`**
  Declare root project name `standby-android`, include `:app`, and configure pluginManagement & dependencyResolutionManagement pointing to Google Maven and Maven Central.

- [ ] **Step 2: Create root `build.gradle.kts` & `gradle.properties`**
  Set up Android Application plugin and Kotlin Android plugin. Configure JVM args (`-Xmx2048m`) and AndroidX flags.

- [ ] **Step 3: Create `app/build.gradle.kts`**
  Configure `namespace = "com.hoandesign.standby"`, `compileSdk = 35`, `minSdk = 26`, `targetSdk = 35`, compose compiler enabled, and dependencies:
  - Compose BOM, Foundation, Material 3, Material Icons Extended
  - Lifecycle Runtime Compose & ViewModel
  - Activity Compose
  - Play Services Location (for optional weather auto-detect)
  - Coroutines & Testing dependencies

- [ ] **Step 4: Create `AndroidManifest.xml`**
  Declare `MainActivity` with orientation sensor handling, `StandbyDreamService` with `android.service.dreams.DreamService` intent filter, `ChargingReceiver` for `ACTION_POWER_CONNECTED`, and permissions (`RECEIVE_BOOT_COMPLETED`, `BATTERY_STATS`, `INTERNET`, `ACCESS_COARSE_LOCATION`).

- [ ] **Step 5: Verify Gradle configuration compiles**
  Run: `./gradlew tasks --dry-run` or check project descriptor.

- [ ] **Step 6: Commit**
  `git add . && git commit -m "chore: scaffold Android project structure and Gradle build configuration"`

---

### Task 2: OLED Design System, Night Mode & Pixel-Shifter

**Files:**
- Create: `app/src/main/java/com/hoandesign/standby/ui/theme/Color.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/theme/Type.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/components/PixelShifter.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/components/NightModeOverlay.kt`
- Create: `app/src/main/java/com/hoandesign/standby/model/NightModeState.kt`

- [ ] **Step 1: Create Color, Type and Theme**
  Define pitch-black OLED background (`#000000`), card backgrounds (`#0d0d0f`, `#151518`), border lines (`#28282c`), and accent palettes:
  - Red Night Mode: `#ff453a`, `#ff3b30`
  - Emerald Green: `#30d158`
  - Amber Gold: `#ff9f0a`, `#ffd60a`
  - Cyan Sky: `#64d2ff`, `#0a84ff`
  - Deep Violet: `#bf5af2`

- [ ] **Step 2: Implement `PixelShifter.kt`**
  Build a reusable Compose container modifier that calculates small `(dx, dy)` offsets (-1.5px to +1.5px) based on `(currentTimeMillis / 120_000) % 8` using a smooth non-repeating Lissajous path. Prevents static OLED subpixel burn-in during overnight charging.

- [ ] **Step 3: Implement `NightModeState.kt` and `NightModeOverlay.kt`**
  Support 3 modes: `Auto` (switches when ambient light < 5 lux), `AlwaysOn`, `Disabled`. In night mode, applies a monochromatic color filter transformation shifting all foreground colors to deep red (`#ff3b30`) and dimming brightness to 15%.

- [ ] **Step 4: Commit**
  `git add . && git commit -m "feat(ui): add OLED design system, PixelShifter burn-in protection, and NightMode"`

---

### Task 3: Adaptive Screen Ratio Engine

**Files:**
- Create: `app/src/main/java/com/hoandesign/standby/model/ScreenRatio.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/layout/AdaptiveLayoutEngine.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/layout/DualStackContainer.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/layout/QuadBentoContainer.kt`

- [ ] **Step 1: Define `ScreenRatio` Archetypes**
  - `UltraTallLandscape` ($W/H \ge 1.9$, e.g. 21:9, 22:9)
  - `StandardLandscape` ($1.4 \le W/H < 1.9$, e.g. 16:9, 16:10, 3:2)
  - `SquarishFoldable` ($0.85 \le W/H < 1.4$, e.g. 1.08:1 OnePlus Open, Fold 8 inner)
  - `TallPortrait` ($W/H < 0.85$, e.g. 9:19.5, 9:20, 9:22 vertical desk stands)

- [ ] **Step 2: Implement `AdaptiveLayoutEngine.kt`**
  Uses `BoxWithConstraints` to compute the active archetype dynamically on window resize or rotation. Routes to:
  - `DualStackContainer` in Landscape (side-by-side with ratio-scaled card width)
  - `DualStackContainer` in Portrait (top-to-bottom with 4:5 stretched cards)
  - `QuadBentoContainer` on Squarish Foldables (2x2 grid filling 4 quadrants)

- [ ] **Step 3: Implement `DualStackContainer.kt` & `QuadBentoContainer.kt`**
  Slots left/right (or top/bottom) widgets into styled rounded containers with OLED hairline borders, padding scaling with screen density, and zero letterboxing.

- [ ] **Step 4: Commit**
  `git add . && git commit -m "feat(layout): implement AdaptiveLayoutEngine supporting foldables and tall screens"`

---

### Task 4: Dual-Axis Swiping Navigation Engine

**Files:**
- Create: `app/src/main/java/com/hoandesign/standby/model/StandbyScreen.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/components/PagerIndicators.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/layout/MainStandbyPager.kt`

- [ ] **Step 1: Horizontal Pager**
  Using Compose `HorizontalPager`, implement smooth paging between:
  1. `DualWidgetScreen`
  2. `HeroClockScreen`
  3. `NowPlayingMediaScreen`

- [ ] **Step 2: Vertical Stack Pagers**
  Using Compose `VerticalPager`, implement independent vertical paging within Left Slot and Right Slot.
  Add snap haptics using `LocalHapticFeedback.current` and subtle vertical page dot indicators.

- [ ] **Step 3: Single vs. Dual Quick-Toggle**
  Add double-tap or long-press gesture detector on any widget to expand it into full-screen single widget mode with smooth spring animation.

- [ ] **Step 4: Commit**
  `git add . && git commit -m "feat(nav): add dual-axis pager system and single-dual widget toggle"`

---

### Task 5: Complete Clock Faces Suite (5 Styles)

**Files:**
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/clock/AnalogClockWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/clock/BigDigitalClockWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/clock/RetroFlipClockWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/clock/SolarArcClockWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/clock/RadialClockWidget.kt`

- [ ] **Step 1: `AnalogClockWidget.kt`**
  Canvas-drawn Swiss/Bauhaus dial with 12 numeral markers, 60 tick lines, white hour & minute hands, orange continuous sweeping second hand, and custom city label.

- [ ] **Step 2: `BigDigitalClockWidget.kt`**
  Massive bold typography (iOS 18 style, e.g. `09:41`), next system alarm indicator, date badge, and tint color selector.

- [ ] **Step 3: `RetroFlipClockWidget.kt`**
  Vintage split-flap flip clock with 3D card flipping animation on minute transitions.

- [ ] **Step 4: `SolarArcClockWidget.kt`**
  Visual sky arc tracking sun position, daylight elapsed percentage, sunrise and sunset times.

- [ ] **Step 5: `RadialClockWidget.kt`**
  Replicate reference screenshot #2: outstretched 12, 3, 6, 9 numerals with angled beam tick marks and horizontal layout.

- [ ] **Step 6: Commit**
  `git add . && git commit -m "feat(widgets): implement complete 5-style clock suite"`

---

### Task 6: Calendar, Events & Productivity Modules

**Files:**
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/calendar/MonthCalendarWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/calendar/AgendaWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/timer/DeskTimerWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/data/CalendarRepository.kt`

- [ ] **Step 1: `MonthCalendarWidget.kt`**
  Replicate reference screenshot #1: bold red month title, `S M T W T F S` headers, current day circle highlight, and weekend dimming.

- [ ] **Step 2: `AgendaWidget.kt`**
  Upcoming meeting countdown, event title, room/location, and colored vertical category bar. Seamless fallback to built-in offline agenda if calendar permission is not granted.

- [ ] **Step 3: `DeskTimerWidget.kt`**
  Pomodoro focus timer & stopwatch with quick presets (25m, 15m, 5m), circular animated progress ring, and Start / Pause / Reset controls.

- [ ] **Step 4: Commit**
  `git add . && git commit -m "feat(widgets): add MonthCalendar, Agenda, and DeskTimer widgets"`

---

### Task 7: Weather, Battery, Media, System Bento, Vibes & Photo Frame

**Files:**
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/weather/WeatherWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/battery/BatteryWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/media/MusicPlayerWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/system/SystemBentoWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/vibes/VibesWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/ui/widgets/photo/PhotoFrameWidget.kt`
- Create: `app/src/main/java/com/hoandesign/standby/data/WeatherRepository.kt`
- Create: `app/src/main/java/com/hoandesign/standby/data/BatteryMonitor.kt`

- [ ] **Step 1: `WeatherWidget.kt` & `WeatherRepository.kt`**
  Real-time temperature, condition emoji/icon, high/low range, AQI, and rain chance using Open-Meteo API with offline cached defaults.

- [ ] **Step 2: `BatteryWidget.kt` & `BatteryMonitor.kt`**
  Live percentage via `BatteryManager`, charging speed ("⚡ Fast Charging", "⚡ Wireless Qi"), battery health, wattage, and estimated time to full.

- [ ] **Step 3: `MusicPlayerWidget.kt`**
  Full-screen & compact now playing with album artwork, animated rotating vinyl record, track title, artist, scrub bar, and media controls (Play/Pause/Next/Prev) with built-in lo-fi sample track.

- [ ] **Step 4: `SystemBentoWidget.kt`**
  Replicate reference screenshot #4: Wi-Fi/Bluetooth status badges, disk storage ring (e.g. `153 GB remain`), RAM gauge, and brightness slider.

- [ ] **Step 5: `VibesWidget.kt` & `PhotoFrameWidget.kt`**
  Bedside relaxation soundscape (Rain, Campfire, Night Wind) with auto-sleep timer, and ambient photo frame with clock overlay.

- [ ] **Step 6: Commit**
  `git add . && git commit -m "feat(widgets): add Weather, Battery, MusicPlayer, SystemBento, Vibes, and PhotoFrame"`

---

### Task 8: System Docking Activation & DreamService

**Files:**
- Create: `app/src/main/java/com/hoandesign/standby/receiver/ChargingReceiver.kt`
- Create: `app/src/main/java/com/hoandesign/standby/StandbyDreamService.kt`
- Create: `app/src/main/java/com/hoandesign/standby/MainActivity.kt`
- Create: `app/src/main/res/xml/standby_dream.xml`

- [ ] **Step 1: `ChargingReceiver.kt`**
  Listens for `ACTION_POWER_CONNECTED`. Checks if device is in landscape orientation; if user setting is enabled, launches `MainActivity` automatically.

- [ ] **Step 2: `StandbyDreamService.kt`**
  Extends Android `DreamService` and sets content to Compose `MainStandbyScreen`. Enables the app to be selected in Android `Settings > Display > Screen Saver`.

- [ ] **Step 3: `MainActivity.kt`**
  Sets edge-to-edge window insets, `FLAG_KEEP_SCREEN_ON` while docked, registers ambient light sensor listener, and hosts the full StandBy Compose UI.

- [ ] **Step 4: Commit**
  `git add . && git commit -m "feat(system): implement ChargingReceiver and StandbyDreamService"`

---

### Task 9: Play Console Automated Upload & GitHub Repository Init

**Files:**
- Create: `scripts/play_upload_internal.py`
- Create: `scripts/ship_play_internal.sh`
- Create: `README.md`

- [ ] **Step 1: Configure Play Console automated upload**
  Adapt `play_upload_internal.py` pointing to verified service account `/Users/lap16030-local/Documents/Projects/my-moves-signing/play-service-account.json` for `com.hoandesign.standby` (or `com.hoan.standby`).

- [ ] **Step 2: Add `ship_play_internal.sh`**
  One-command automated release build (`./gradlew :app:bundleRelease`) followed by automated track upload to Play Console Internal Testing.

- [ ] **Step 3: Push to GitHub repository**
  Add remote `git@github.com:hoandesign/standby-android.git` (or HTTPS) and push `main` branch.

- [ ] **Step 4: Commit**
  `git add . && git commit -m "chore: add Play Console automated deployment scripts and documentation"`
