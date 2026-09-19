# StandBy Android

> **Turn your Android device into an intelligent, ambient smart display while charging.**

StandBy Android transforms any Android smartphone, foldable, or tablet into an ambient smart display whenever it is docked or charging at a desk, bedside table, or workspace. Drawing inspiration from modern ambient display paradigms and iOS 18 StandBy mode, StandBy Android pairs a pitch-black OLED aesthetic with an adaptive layout engine tailored for next-generation hardware.

---

## Key Highlights

- **Pure OLED Pitch-Black Aesthetic:** `#000000` deep true black canvas turns off unused pixels on AMOLED/OLED displays to minimize power draw and heat.
- **Adaptive Screen Ratio Engine:** Dynamically calculates window aspect ratio ($W/H$) to eliminate black pillarboxing across ultra-tall screens (21:9 to 22.1:9), squarish foldables (~1.08:1), and tall vertical portrait stands (9:20).
- **Dual-Axis Fluid Navigation:** Horizontal swipe switches between primary screens (**Dual Widgets**, **Hero Clock**, and **Now Playing**). Independent vertical swipe cycles widgets within left/right stacks.
- **Full 14-Module Widget Catalog:** High-fidelity widgets for timekeeping, calendar schedules, productivity, weather, battery telemetry, lo-fi relaxation, and ambient photography.
- **Bedside Night Mode:** Seamless ambient light sensor integration (< 5 lux) activates a monochromatic ruby-red filter that preserves night vision and sleep quality.
- **Continuous OLED Pixel-Shifter:** Invisible micro-translation algorithm continuously shifts the UI by ±1 to ±3 pixels over subtle intervals to prevent burn-in during prolonged docking.
- **Native Android System Docking & Screen Saver:** Auto-launches upon magnetic/wireless or cable dock connection, and fully integrates with Android's system `DreamService` (Settings > Display > Screen Saver).

---

## Adaptive Screen Ratio Engine

Unlike traditional apps that assume static 16:9 or 20:9 dimensions, StandBy Android computes real-time aspect ratios and maps them to 4 distinct archetypes:

```
+-------------------------------------------------------------------------------------+
| Ratio Archetype         | Example Devices                 | Layout Strategy         |
+-------------------------------------------------------------------------------------+
| 1. Ultra-Tall Landscape | Galaxy Z Fold 8 Cover (22.1:9), | Symmetrical dual-stack  |
|    (W/H >= 1.9)         | Sony Xperia (21:9), 20:9 phones | with card expansion     |
|                         |                                 | and ambient gutters     |
+-------------------------------------------------------------------------------------+
| 2. Standard Landscape   | Android Tablets (16:10, 3:2),   | Symmetrical dual cards  |
|    (1.4 <= W/H < 1.9)   | Tri-fold devices, Fold inner    | with balanced margins   |
+-------------------------------------------------------------------------------------+
| 3. Squarish Foldables   | OnePlus Open (~1.08:1),         | Quad-Bento 2x2 grid     |
|    (0.85 <= W/H < 1.4)  | Galaxy Z Fold inner (1.16:1),   | or vertical split cards |
|                         | Honor Magic V3 inner (1.08:1)   | with high data density  |
+-------------------------------------------------------------------------------------+
| 4. Tall Portrait Stand  | 9:19.5, 9:20, 9:22 wireless     | Top / Bottom 4:5 cards  |
|    (W/H < 0.85)         | desktop charging stands         | filling vertical height |
+-------------------------------------------------------------------------------------+
```

---

## Full 14-Module Widget Catalog

StandBy Android features 14 handcrafted modules that can be mixed and matched across dual stacks:

### 1. Clock & Timekeeping Suite
1. **Analog Bauhaus Clock:** Canvas-rendered Swiss/Bauhaus dial with 12 numeral markers, 60 precision tick lines, white hour/minute hands, continuous sweeping second hand, and custom city indicator.
2. **Big Digital Clock:** Giant bold typography (iOS 18 style), next alarm indicator, date badge, and custom accent tint selector.
3. **Retro Flip Clock:** Vintage split-flap mechanical flip clock with smooth 3D card folding transitions on minute changes.
4. **Solar Arc Clock:** Dynamic celestial arc tracking real-time sun elevation, elapsed daylight percentage, and exact sunrise/sunset times.
5. **Radial Clock:** Outstretched 12, 3, 6, 9 numerals with angled beam tick marks and horizontal sweeping arrangement.

### 2. Calendar & Productivity
6. **Month Calendar:** Bold accent month header, `S M T W T F S` column grid, current day circle highlight, and weekend dimming.
7. **Agenda & Upcoming Events:** Meeting countdown timer, event title, conference room/location badge, and colored category bar with offline fallback support.
8. **Desk Focus Timer:** Pomodoro work/rest timer and stopwatch featuring 25m, 15m, and 5m quick presets, circular animated progress ring, and interactive Start / Pause / Reset controls.

### 3. System, Weather & Media
9. **Real-Time Weather:** Live temperature, condition icon/emoji, daily high/low range, AQI status, and precipitation probability powered by Open-Meteo with cached offline persistence.
10. **Battery & Fast Charging Monitor:** Live battery level via `BatteryManager`, charging mode telemetry ("⚡ Fast Charging", "⚡ Wireless Qi"), health condition, and estimated time to full.
11. **Music Player:** Now Playing widget featuring high-resolution album artwork, animated spinning vinyl disc, track title, artist, scrubbable progress bar, and playback controls.
12. **System Bento:** Bento-grid dashboard displaying Wi-Fi/Bluetooth status badges, disk storage meter (e.g. `153 GB remain`), RAM utilization gauge, and brightness controls.

### 4. Ambient Experience
13. **Vibes Ambient Soundscapes:** Built-in bedside sound generator (Gentle Rain, Cozy Campfire, Night Wind) with soothing sleep timer.
14. **Ambient Photo Frame:** Photo gallery frame with subtle clock overlay and gentle pan-and-zoom motion.

---

## Bedside Health & Hardware Protection

### Red Night Mode
When bedside ambient light drops below 5 lux (detected via `Sensor.TYPE_LIGHT`), StandBy Android transitions the entire UI into a monochromatic ruby-red color palette (`#FF3B30` / `#8B0000`). Red light does not disrupt melatonin production or dark-adapted vision. Quick settings also allow overriding to **Auto**, **Always Red**, or **Disabled**.

### Continuous OLED Pixel-Shifter
To protect OLED panels during hundreds of hours on charging stands, the `Modifier.pixelShift` engine shifts content by small offsets (±1 to ±3 dp) along both axes every 60 seconds. The transition is below human visual acuity threshold while constantly reallocating emitter stress across adjacent subpixels.

---

## System Integration

### Docking Auto-Launch (`ChargingReceiver`)
Monitors `ACTION_POWER_CONNECTED`. When charging begins while the device is in landscape orientation (or on a charging dock), StandBy Android automatically opens, bringing up your personalized dashboard without unlocking or touching the screen.

### Android Screen Saver (`DreamService`)
Implements `StandbyDreamService`, enabling StandBy Android to be set as the official system screensaver:
- Go to Android **Settings > Display > Screen saver**.
- Select **StandBy Android**.
- Choose when to activate: **While charging**, **While docked**, or **Either**.

---

## Quick Settings & Customization

- **Tap Gear Icon** (top right) or **Tap Bottom Pill Indicator** to summon the quick settings overlay.
- **Accent Color Palette:** Curated Apple/Material palette including Green, Orange, Amber, Cyan, Purple, and Crisp White.
- **Double Tap / Single Tap:** Tap any widget card in dual-mode to toggle between dual-split view and full-screen focused card. Tap the Hero Clock to switch between Radial and Digital clock faces.

---

## Development & Build Instructions

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 17
- Android SDK 35 (`compileSdk = 35`, `minSdk = 26`)

### Build Commands

```bash
# Run unit tests
./gradlew test

# Build debug APK
./gradlew :app:assembleDebug

# Build release APK (automatically falls back to debug signing if keystore is absent)
./gradlew :app:assembleRelease

# Build release App Bundle (AAB) for Google Play
./gradlew :app:bundleRelease
```

---

## Automated Google Play Deployment

StandBy Android includes automated publishing scripts using Google's Android Publisher API v3.

### 1. Signing Configuration
In `app/build.gradle.kts`, release builds look for `~/Documents/Projects/my-moves-signing/keystore.properties`:
```properties
storeFile=/path/to/upload-keystore.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```
If this file is not found, the build gracefully falls back to debug signing, ensuring build portability across any developer machine or CI/CD runner.

### 2. One-Command Ship to Play Console Internal Testing

```bash
# Build release AAB and upload to Internal testing track
./scripts/ship_play_internal.sh
```

Or execute the Python script directly:
```bash
python3 scripts/play_upload_internal.py [optional/path/to/app-release.aab]
```

---

## Architecture & Code Structure

```
standby-android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/hoandesign/standby/
│   │   │   ├── MainActivity.kt               # Entry Activity & Window Insets Setup
│   │   │   ├── StandbyDreamService.kt        # System Screen Saver (DreamService)
│   │   │   ├── data/                         # Data layer (Battery, Calendar, Weather)
│   │   │   ├── model/                        # Models (ScreenRatio, NightMode, Clock)
│   │   │   ├── receiver/ChargingReceiver.kt  # Dock & Power Connected Broadcast Receiver
│   │   │   └── ui/
│   │   │       ├── MainStandbyScreen.kt      # Root Compose Container & Quick Settings
│   │   │       ├── components/               # PixelShifter, NightModeFilter, Indicators
│   │   │       ├── layout/                   # AdaptiveLayoutEngine, DualStack, Bento
│   │   │       ├── theme/                    # OLED Palette, Typography, Dimming
│   │   │       └── widgets/                  # 14-Module Widget Implementations
│   │   └── res/                              # Drawables, XML metadata, App Icons
│   └── build.gradle.kts                      # Signing config & Android dependencies
├── scripts/
│   ├── play_upload_internal.py               # Google Play Publisher API upload script
│   └── ship_play_internal.sh                 # One-click build & release pipeline
└── docs/                                     # Architecture & design specifications
```

---

## License

Copyright © 2026 Hoan Do (`hoandesign`). All rights reserved.
Licensed under the Apache License, Version 2.0.
