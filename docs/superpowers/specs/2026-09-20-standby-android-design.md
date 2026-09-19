# StandBy Android: System Architecture & Product Design Specification

**Date:** 2026-09-20  
**Project:** StandBy Android (`standby-android`)  
**Target Platform:** Android (API 26+, optimized for Android 14–16+)  
**Repository:** `hoandesign/standby-android`  
**Author:** Hoan Do & Antigravity  

---

## 1. Executive Summary & Vision

**StandBy Android** transforms an Android phone, foldable, or tablet into an intelligent, ambient smart display whenever it is docked or charging at a desk, bedside table, or workspace.

Drawing inspiration from iOS 17/18 StandBy mode and leading ambient display paradigms, StandBy Android delivers:
1. **Dual-Axis Fluid Navigation:** Horizontal swiping between primary ambient views; independent vertical swiping within dual-widget stacks.
2. **Adaptive Dynamic Ratio Engine:** Tailor-made for 2026 device diversity—from ultra-tall 22:9 cover displays to ~1.08:1 inner foldable screens (Galaxy Z Fold 8, Honor Magic V3, OnePlus Open), tablets, and tall vertical portrait charging docks.
3. **Comprehensive Widget Catalog:** 5 distinct clock aesthetics (Analog, Big Digital, Retro Flip, Solar Arc, Radial), Calendar, Events Agenda, Desk Timer, Weather, Battery & Fast Charging Monitor, Media Player, Ambient Soundscapes ("Vibes"), System Bento, and Photo Frame.
4. **Bedside Health & Hardware Protection:** Automatic OLED Red Night Mode triggered by the ambient light sensor, and an invisible continuous OLED Pixel-Shifter preventing screen burn-in.

---

## 2. Device Form-Factors & Adaptive Screen Ratio Engine

Unlike traditional apps that assume a fixed 16:9 or 20:9 ratio, StandBy Android dynamically calculates the window aspect ratio ($W/H$) and renders according to 4 adaptive archetypes:

```
+-----------------------------------------------------------------------------------+
| Ratio Archetype          | Examples                        | Layout Strategy      |
|--------------------------|---------------------------------|----------------------|
| 1. Ultra-Tall Landscape  | Galaxy Z Fold 8 Cover (22.1:9), | Dual-stack with wide |
|    (W/H >= 1.9)          | Sony Xperia (21:9), 20:9 phones | card expansion &     |
|                          |                                 | ambient gutter       |
|--------------------------|---------------------------------|----------------------|
| 2. Standard Landscape    | 16:9 / 16:10 / 3:2 tablets,     | Symmetrical dual     |
|    (1.4 <= W/H < 1.9)    | Unfolded tri-fold (Mate XT)     | cards, generous      |
|                          |                                 | typography spacing   |
|--------------------------|---------------------------------|----------------------|
| 3. Squarish Foldables    | OnePlus Open (~1.08:1),         | Quad-Bento 2x2 grid  |
|    (0.85 <= W/H < 1.4)   | Galaxy Z Fold inner (1.16:1),   | or 2 large vertical  |
|                          | Honor Magic V3 inner (1.08:1)   | split cards          |
|--------------------------|---------------------------------|----------------------|
| 4. Tall Portrait Stand   | 9:19.5, 9:20, 9:22 vertical     | Top/Bottom 4:5 cards |
|    (W/H < 0.85)          | wireless desk charging stands   | filling full height  |
+-----------------------------------------------------------------------------------+
```

### Eliminating Dead Space
* **No Black Pillarboxing:** Card dimensions flex with `BoxWithConstraints`.
* **Proportional Content Density:** On tall screens, cards expand their height from 1:1 squares to 4:5 rectangles, rendering secondary contextual rows (e.g., upcoming agenda items below the calendar grid, or local city and next alarm below the analog clock face).

---

## 3. Architecture & Tech Stack

```
+-------------------------------------------------------------------+
|                        StandBy Android App                        |
+-------------------------------------------------------------------+
                                  |
        +-------------------------+-------------------------+
        |                                                   |
+-------v-------------------------+       +-----------------v-----------------+
|   System Triggers & Services    |       |     Jetpack Compose UI Engine     |
| - ChargingReceiver (USB/Qi)     |       | - Dual-Axis Pager System          |
| - DreamService (Screensaver)    |       | - Adaptive Ratio BoxConstraints   |
| - SensorManager (Ambient Light) |       | - OLED Pixel Shifter Engine       |
| - WakeLock & KeepScreenOn       |       | - Theme & Tint Palette            |
+---------------------------------+       +-----------------------------------+
        |                                                   |
        +-------------------------+-------------------------+
                                  |
+---------------------------------v---------------------------------+
|                    Widget Data Providers Layer                    |
| - Clock/AlarmProvider     - WeatherProvider (Open-Meteo)          |
| - BatteryProvider         - CalendarAgendaProvider                |
| - MediaSessionProvider    - AmbientSoundEngine (Vibes)            |
+-------------------------------------------------------------------+
```

### Core Technologies
* **Language:** Kotlin 2.x
* **UI Toolkit:** Jetpack Compose with Material 3
* **State Management:** Modern Kotlin `StateFlow` and Compose immutable state
* **Background & System Services:**
  * `BroadcastReceiver` listening to `ACTION_POWER_CONNECTED` and `ACTION_CONFIGURATION_CHANGED` (rotation to landscape).
  * `DreamService` integration enabling native selection as device screen saver.
  * `SensorEventListener` for ambient light readings (`Sensor.TYPE_LIGHT`).
  * `MediaSessionManager` for music track metadata and playback controls.

---

## 4. UI Navigation & Gesture System

### 1. Horizontal Swiping (Screen-Level Transitions)
Swiping left/right slides between 3 primary modes:
1. **Dual Widget View:** Two customizable widgets side-by-side (in landscape) or top-to-bottom (in portrait). On squarish foldables, toggles into a 2x2 Quad Bento.
2. **Hero Clock View:** Fullscreen oversized clock face with zero distractions.
3. **Now Playing Media View:** Immersive media player with album artwork, animated spinning vinyl disc, and scrubbable timeline.

### 2. Vertical Swiping (Widget-Level Stacking)
* Inside the Dual Widget View, the **Left Slot** and **Right Slot** are independent vertical carousels.
* Flicking up/down swaps the active widget within that slot.
* Includes haptic snap feedback and subtle page indicator dots.

### 3. Quick-Toggle Single / Dual Mode
* Double-tapping or long-pressing any active widget expands it to a full-screen **Single Widget Mode**.
* Long-pressing opens the **Widget Customization Sheet** to choose accent colors or reorder modules.

---

## 5. Comprehensive Widget Modules Catalog

### 1. Clock Faces
1. **Classic Bauhaus/Swiss Analog Clock:**
   - Smooth 60fps sweeping second hand, tick marks, customizable city timezone label.
2. **Oversized Digital Typography Clock (iOS 18 style):**
   - Huge bold numerals (e.g. `09:41`), next system alarm indicator, and date banner.
   - Tintable in 6 palette accents: Emerald Green, Sunset Orange, Solar Amber, Cyan, Purple, and Crisp White.
3. **Retro Mechanical Flip Clock:**
   - 3D split-flap card animation when hours and minutes turn.
4. **Solar Horizon Arc Clock:**
   - Visual sky arc mapping real-time sun elevation, daylight progress, sunrise, and sunset times.
5. **Radial Extended Horizon Clock:**
   - Wide-stretched 12, 3, 6, 9 numerals with angled beam tick marks (matching screenshot #2).

### 2. Productivity & Daily Utilities
6. **Monthly Calendar Grid:**
   - Full calendar month grid with red title header and circle highlight on today (matching screenshot #1).
7. **Upcoming Schedule & Agenda:**
   - Next meeting countdown, event title, room location, and timeline markers.
   - Queries Android `CalendarContract`, with seamless built-in sample events when calendar access is not granted.
8. **Fullscreen Desk Timer & Stopwatch:**
   - Quick pomodoro presets (25m, 15m, 5m), live circular progress ring, audio chime on completion.
9. **Live Weather & Hourly Conditions:**
   - Current temperature, condition icon (sunny, cloudy, rain, thunderstorm), high/low range, AQI, and rain chance.
   - Powered by Open-Meteo API (instant, high accuracy, zero user API key needed).
10. **Battery & Fast Charging Hub:**
    - Live battery meter percentage, charging speed indicator ("Fast Charging", "Wireless Qi", "Super Fast 65W"), and calculated time remaining until 100%.
11. **Now Playing Music Player (iOS 18 style):**
    - Listens to active Spotify, YouTube Music, Apple Music playback via `MediaSession`.
    - Shows album art, animated spinning vinyl disc, track progress, Play/Pause/Skip controls.
    - Includes built-in lo-fi sample track for instant visual delight when no media app is running.
12. **Vibes Ambient Relaxation Soundscape:**
    - Calming bedside audio (Gentle Rain, Warm Hearth Fire, Deep Night Forest) with auto-off sleep timer.
13. **System & Connectivity Bento:**
    - Wi-Fi and Bluetooth connection status badges, device storage usage ring (free/used GB), RAM usage gauge, and quick brightness slider (matching screenshot #4).
14. **Ambient Photo Frame:**
    - Ambient picture slideshow from device gallery or curated landscape photography with clock overlay.

---

## 6. Bedside OLED Protection & Night Mode

### 1. Automatic OLED Red Night Mode
* Automatically switches into deep monochromatic red (`#ff3b30`) / amber glow when room brightness drops below 5 lux (detected via ambient light sensor).
* Eliminates blue-light emission, prevents sleep disruption, and avoids blinding bedside glare.
* Can also be scheduled manually by time (e.g. 10:00 PM – 7:00 AM).

### 2. Continuous OLED Pixel-Shifter
* Static elements on OLED displays run the risk of burn-in if left on for 8+ hours every night.
* StandBy Android includes an invisible pixel-shifter that nudges the entire canvas by 1–2 pixels along a Lissajous curve every 2 minutes.
* Completely imperceptible to the human eye, but ensures individual subpixels never remain continuously active.

### 3. Power & Battery Safeguards
* **Charger-Only Guard:** Optional mode where StandBy automatically sleeps if the charger is disconnected, avoiding accidental battery drainage.
* **Proximity / Wave to Wake:** Screen can idle into ultra-dim standby and brighten when motion or hand wave is detected near the top sensor.

---

## 7. Graceful Permissions & Offline Fallbacks

StandBy Android prioritizes an **instant out-of-the-box experience**:
* **No Permissions Required for Baseline Usage:** Clocks, timer, battery status, weather, and simulated music/calendar work immediately upon first launch.
* **Zero-Permission Fallbacks:**
  * Calendar: Falls back to a clean mock design agenda if system calendar permission is skipped.
  * Media: Plays offline relaxing audio preview if Notification Listener / MediaSession permission is skipped.
  * Weather: Uses auto-location if granted, or defaults to user-customizable city (e.g., Ho Chi Minh City, New York, Tokyo, London).

---

## 8. Git Repository Structure & CI/Build Setup

The codebase will reside in `/Users/lap16030-local/Documents/Github/standby-android/` with standard Gradle/Kotlin multi-file modularity:

```
standby-android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/hoandesign/standby/
│   │   │   ├── MainActivity.kt
│   │   │   ├── StandbyDreamService.kt
│   │   │   ├── receiver/ChargingReceiver.kt
│   │   │   ├── ui/
│   │   │   │   ├── theme/ (Color, Type, Theme)
│   │   │   │   ├── components/ (PixelShifter, NavigationPagers)
│   │   │   │   ├── layout/ (AdaptiveLayoutEngine, BentoGrid)
│   │   │   │   ├── widgets/
│   │   │   │   │   ├── clock/ (AnalogClock, DigitalClock, FlipClock, SolarClock, RadialClock)
│   │   │   │   │   ├── calendar/ (MonthCalendar, AgendaWidget)
│   │   │   │   │   ├── weather/ (WeatherWidget)
│   │   │   │   │   ├── battery/ (BatteryWidget)
│   │   │   │   │   ├── media/ (MusicPlayerWidget)
│   │   │   │   │   ├── timer/ (DeskTimerWidget)
│   │   │   │   │   ├── system/ (SystemBentoWidget)
│   │   │   │   │   └── vibes/ (VibesWidget)
│   │   │   └── data/ (WeatherRepository, CalendarRepository, MediaManager)
│   │   └── res/ (drawable, values, mipmap)
│   └── build.gradle.kts
├── docs/superpowers/specs/2026-09-20-standby-android-design.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
└── README.md
```

---

## 9. Verification & Acceptance Criteria

1. **Orientation & Ratio Flexibility:** Tested across standard landscape (16:9, 20:9), ultra-wide (22:9), squarish inner foldable (~1.08:1), and vertical portrait (9:20). Zero blank dead space.
2. **Gesture Responsiveness:** 60fps+ smooth horizontal swipe between main views; fluid vertical swipe between stacked widgets.
3. **Module Completeness:** All 14 listed widget variations present, functional, and visually faithful to pitch-black OLED aesthetics.
4. **Bedside Night Mode:** Instant, smooth color grading shift to deep `#ff3b30` red in darkness.
5. **Git & GitHub:** Properly committed with clean history and pushed to `hoandesign/standby-android`.
