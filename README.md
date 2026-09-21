# StandBy Android

[![Release](https://img.shields.io/badge/Release-v1.1.6%20(Build%2017)-000000?style=for-the-badge&logo=android&logoColor=white)](https://play.google.com/apps/testing/com.hoandesign.standby)
[![Android SDK](https://img.shields.io/badge/Target%20SDK-Android%2016%20(API%2036)-34A853?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/about/versions/16)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20OLED%20Black-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material Design](https://img.shields.io/badge/Icons-Google%20Material%20Vectors-EA4335?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![Firebase](https://img.shields.io/badge/Cloud%20Test-Firebase%20Test%20Lab-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/docs/test-lab)
[![Audit Gate](https://img.shields.io/badge/Auditor%20Gate-PASS%20(10%2F10)-28A745?style=for-the-badge)](audits/AUDIT_LATEST.md)

> **Transform your Android device into an intelligent, ambient smart display while charging.**

**StandBy Android** transforms any Android smartphone, foldable, or tablet into an ambient smart display whenever it is docked or charging at a desk, bedside table, or workspace. Drawing inspiration from modern ambient computing paradigms and Apple StandBy mode, StandBy Android pairs a pitch-black OLED aesthetic (`#000000`) with an adaptive layout engine tailored for next-generation mobile hardware.

---

## Table of Contents

- [Key Highlights](#key-highlights)
- [Modern 4-Screen Hierarchy](#modern-4-screen-hierarchy)
- [Signature Clock & Design Features](#signature-clock--design-features)
- [Adaptive Screen Ratio Engine](#adaptive-screen-ratio-engine)
- [Full 15-Module Widget Catalog](#full-15-module-widget-catalog)
- [Zero-Emoji Architectural Vector Standard](#zero-emoji-architectural-vector-standard)
- [Bedside Health & Hardware Protection](#bedside-health--hardware-protection)
- [System Integrations & Telemetry](#system-integrations--telemetry)
- [Developer Setup & Workflow Guide](#developer-setup--workflow-guide)
  - [1. Prerequisites & Tooling](#1-prerequisites--tooling)
  - [2. Firebase CLI & Project Binding](#2-firebase-cli--project-binding)
  - [3. Signing & Google Play Setup](#3-signing--google-play-setup)
  - [4. Core Development Commands](#4-core-development-commands)
  - [5. Automated Turn Runner Pipeline](#5-automated-turn-runner-pipeline)
  - [6. Firebase Test Lab Physical Device Testing](#6-firebase-test-lab-physical-device-testing)
- [Autonomous Development & Adversarial Audit Contract](#autonomous-development--adversarial-audit-contract)
- [Architecture & Directory Structure](#architecture--directory-structure)
- [License](#license)

---

## Key Highlights

- **Pure OLED Pitch-Black Canvas:** `#000000` deep true black canvas turns off individual self-emissive pixels on AMOLED/OLED panels to eliminate light bleed, minimize power draw, and keep device temperatures cool during overnight docking.
- **4-Screen Navigation Hierarchy:** Seamless horizontal paging across **Bento Mode**, **Single Module Mode**, **Hero Clocks**, and **Now Playing**, with persistent auto-hiding top navigation tabs.
- **Full-Bleed Tank / Rounded-Rectangle Bauhaus Clock:** Hardware-inspired squircle dial utilizing 100% of container space with exact analytic edge normal tick projection and 12% corner radius.
- **Zero-Footprint Swipe Indicator Capsules:** Frosted dark glass indicator capsules anchored to outer gutters (`Alignment.CenterEnd` for vertical bento stacks, `Alignment.BottomCenter` for root screen pager) that auto-hide with zero layout push on content cards.
- **Zero-Emoji Material Design Iconography:** Complete eradication of OS cartoon emojis. All telemetry rendered with crisp Google Material Design vector graphics (`Icons.Rounded.*`) or Canvas vector geometry.
- **Reactive °C / °F Temperature Engine:** Instant switching between Celsius and Fahrenheit in Quick Settings or by tapping weather cards, automatically propagating across all compact and fullscreen displays.
- **Full 15-Module Widget Catalog:** High-fidelity widgets for Swiss clocks, calendar agendas, productivity timers, live weather, battery metrics, lo-fi relaxation, and ambient photography.
- **Bedside Red Night Mode:** Ambient light sensor integration (< 5 lux) triggers a monochromatic ruby-red filter (`#FF3B30` / `#8B0000`) preserving melatonin production and dark-adapted vision.
- **Continuous OLED Pixel-Shifter:** Monotonic subpixel Lissajous micro-translation algorithm continuously shifts the UI by $\pm 1.5\text{dp}$ during discrete 118s sleep / 2s shift intervals to prevent display burn-in.
- **Native Android Screen Saver (`DreamService`):** Integrates directly with Android's system screensaver settings (**Settings > Display > Screen Saver**) and auto-launches upon magnetic or cable charging dock connection.

---

## Modern 4-Screen Hierarchy

StandBy Android organizes ambient presentations into 4 dedicated screen levels navigable via horizontal swiping or the auto-hiding top navigation menu:

```
[ Bento Mode ]  ◄──────►  [ Single Module ]  ◄──────►  [ Hero Clocks ]  ◄──────►  [ Now Playing ]
    (Page 0)                  (Page 1)                   (Page 2)                  (Page 3)
```

1. **Page 0: Bento Mode (Smart Dual-Stack):**
   - Symmetrical split dual-slot dashboard with independent vertical stacks for left and right slots.
   - Jiggle Edit Mode with Apple-style long-press elevation, haptics, and drag-to-reorder.
   - Non-colliding outer-gutter vertical indicators anchored at `Alignment.CenterEnd` with 8dp padding.
2. **Page 1: Single Module Mode (Ambient Fullscreen):**
   - Edge-to-edge vertical pager cycling through all active widgets in their rich, bespoke fullscreen layouts.
   - Ideal for focused desk work or dedicated full-bleed telemetry dashboards.
3. **Page 2: Hero Clocks:**
   - Fullscreen ambient timekeeping suite featuring the **Rectangle Tank Bauhaus Clock**, **Radial Clock**, and **Big Digital Clock**.
   - Includes dynamic **OLED Wireframe Mode** (`Stroke(4f)`) that triggers during Night Mode to protect display emitters.
4. **Page 3: Now Playing:**
   - Immersive edge-to-edge music station with high-resolution album artwork, spinning vinyl disc animation, live track metadata, progress scrubbing, and transport controls.

---

## Signature Clock & Design Features

### Full-Bleed Tank / Rounded-Rectangle Bauhaus Clock (`RectangleAnalogClockWidget`)
On widescreen (20:9) displays and squarish bento slots, traditional circular dials leave over 35% of the surface area dead in the corners. The **Rectangle Analog Clock** maximizes available space:
- **Analytic Edge Normal Tick Projection:** Projects 60 perimeter ticks along a rounded rectangular track with true perpendicular normals: vertical ticks along horizontal edges, horizontal ticks along vertical edges, and radial rays in the 12% rounded corners.
- **Optical Cardinal Alignment:** Bold Bauhaus numerals (`12, 3, 6, 9`) anchored with proportional internal margins.
- **Constant-Radius Baton Hands:** Clean white hands with subtle drop shadows that rotate smoothly without rubber-band stretching.

### Zero-Footprint Swipe Indicator System
- **Directional Congruence:** Vertical widget stacks use vertical indicator capsules; the horizontal screen pager uses a horizontal indicator capsule.
- **Outer-Gutter Anchoring:** Slot indicators float at `Alignment.CenterEnd` with 8dp padding, completely freeing card centers and titles ("12", "SEPTEMBER", "Cupertino") from visual clutter.
- **Frosted Capsule Backplates:** Subtle translucent dark pill (`Color(0x55000000)`) with 0.5dp border guarantees clear visibility against bright photo frames or dark dials.
- **Zero Layout Push (`navTopInset = 0.dp`):** Floating Z-index overlays ensure widget cards occupy 100% full width and height with zero content squeezing.
- **Transient Auto-Hide:** Automatically hides after 4.5 seconds of inactivity; illuminates immediately during active scrolling (`pagerState.isScrollInProgress`).

---

## Adaptive Screen Ratio Engine

StandBy Android dynamically computes real-time window aspect ratios ($W/H$) and maps them to 4 distinct hardware archetypes:

| Ratio Archetype | Aspect Ratio ($W/H$) | Example Devices | Layout Strategy |
| :--- | :--- | :--- | :--- |
| **1. Ultra-Tall Landscape** | $W/H \ge 1.85$ | Galaxy Z Fold cover (22.1:9), Sony Xperia (21:9), 20:9 phones | Symmetrical dual-stack with card expansion and ambient center gutters |
| **2. Standard Landscape** | $1.35 \le W/H < 1.85$ | Android Tablets (16:10, 3:2), Tri-fold displays, Fold inner screens | Symmetrical dual cards with balanced margins |
| **3. Squarish Foldables** | $0.85 \le W/H < 1.35$ | OnePlus Open (~1.08:1), Galaxy Z Fold inner (1.16:1), Honor Magic V3 | Quad-Bento 2x2 grid or high-density vertical split cards |
| **4. Tall Portrait Stand** | $W/H < 0.85$ | 9:19.5, 9:20, 9:22 vertical desktop wireless charging stands | Symmetrical top/bottom 4:5 vertical split cards |

---

## Full 15-Module Widget Catalog

Every widget supports both a **Compact Bento Card** layout and a **Bespoke Fullscreen Ambient** layout:

### 1. Clock & Timekeeping Suite
1. **Rectangle Tank Bauhaus Clock:** Full-bleed rounded-rectangle dial with 60 perpendicular index ticks, bold cardinal numerals (`12, 3, 6, 9`), live date complication, and continuous second sweep.
2. **Circular Swiss Bauhaus Clock:** Canvas-rendered Swiss/Bauhaus circular dial with 12 numeral markers, 60 precision tick lines, white hour/minute hands, continuous sweeping second hand, and custom city indicator.
3. **Big Digital Clock:** Monumental 100sp+ typography, next alarm indicator, date badge, and custom accent tint selector. Automatically switches to hollow wireframe stroke in Night Mode.
4. **Retro Flip Clock:** Vintage split-flap mechanical flip clock with smooth 3D card folding transitions on minute changes.
5. **Solar Arc Clock:** Dynamic celestial arc tracking real-time sun elevation, elapsed daylight percentage, and exact sunrise/sunset times.
6. **Radial Clock:** Outstretched 12, 3, 6, 9 numerals with angled beam tick marks and horizontal sweeping arrangement with drop shadow clearance.

### 2. Calendar & Productivity
7. **Month Calendar:** Bold accent month header, `S M T W T F S` column grid, current day circle highlight, and weekend dimming.
8. **Agenda & Upcoming Events:** Meeting countdown timer, event title, conference room/location badge, relative day tags (`[ TODAY ]`, `[ TOMORROW ]`), and Direct Boot keyguard protection.
9. **Desk Focus Timer:** Pomodoro work/rest timer and stopwatch featuring 25m, 15m, and 5m quick presets, circular animated progress ring, and interactive Start / Pause / Reset controls.

### 3. System, Weather & Media Telemetry
10. **Real-Time Weather:** Live temperature, Material condition icons, daily high/low range, AQI status, and precipitation probability powered by Open-Meteo with cached offline persistence and reactive °C / °F switching.
11. **Battery & Fast Charging Monitor:** Live battery level via `BatteryManager`, charging mode telemetry ("Fast Charging", "Wireless Qi"), health condition, and voltage/temperature metrics.
12. **Music Player:** Now Playing widget featuring live system session streaming via `StandbyMediaListenerService` (`NotificationListenerService`), album artwork, animated spinning vinyl disc, track title, artist, scrubbable progress bar, and transport controls.
13. **System Bento:** Hardware telemetry dashboard displaying Wi-Fi/Bluetooth status badges, disk storage meter (via `StatFs`), RAM utilization gauge (via `ActivityManager`), and display brightness controls.

### 4. Ambient Experience
14. **Vibes Ambient Soundscapes:** Built-in bedside sound generator (Gentle Rain, Cozy Campfire, Night Wind) with soothing sleep timer.
15. **Ambient Photo Frame:** Photo gallery frame with subtle clock overlay and gentle pan-and-zoom motion powered by Android's native Photo Picker (`ActivityResultContracts.PickVisualMedia`).

---

## Zero-Emoji Architectural Vector Standard

StandBy Android strictly enforces a **Zero-Emoji Mandate** across both views and data layers:
- Raw OS cartoon emojis (⚡, 🌧️, 🔥, 🍃, 📶, ᛒ, ☀️, ⏰, 📍, 🎵, ⛅, 💧) are strictly prohibited.
- All weather conditions, battery charging states, media indicators, and system badges are rendered using official Google Material Design vector graphics (`Icons.Rounded.WbSunny`, `Icons.Rounded.WaterDrop`, `Icons.Rounded.Thunderstorm`, `Icons.Rounded.MusicNote`, etc.) or custom Canvas vector paths with crisp 1.5–2dp architectural strokes.

---

## Bedside Health & Hardware Protection

### Red Night Mode
When ambient light drops below 5 lux (detected via `Sensor.TYPE_LIGHT`), StandBy Android transitions the entire UI into a monochromatic ruby-red color palette (`#FF3B30` / `#8B0000`). Red wavelengths prevent melatonin suppression and protect dark-adapted night vision. Quick Settings allow toggling between **Auto**, **Always Red**, and **Disabled**.

### Continuous OLED Pixel-Shifter
To protect OLED panels during hundreds of hours on charging stands, the `Modifier.pixelShift` engine shifts content by small offsets ($\pm 1.5\text{dp}$) along orthogonal Lissajous curves ($\sin(t), \cos(2t)$).
- Driven by monotonic `android.os.SystemClock.elapsedRealtime()` (immune to wall-clock or NTP adjustments).
- Cycles between **118 seconds of static sleep** (0% CPU/GPU overhead) and **2 seconds of micro-translation**.

---

## System Integrations & Telemetry

- **Docking Auto-Launch (`ChargingReceiver`):** Monitors `ACTION_POWER_CONNECTED`. Automatically launches StandBy Android when connected to power in landscape orientation.
- **System Screen Saver (`StandbyDreamService`):** Registered as an official Android `DreamService`. Configure under **Settings > Display > Screen saver**.
- **System Media Sessions (`StandbyMediaListenerService`):** Listens to system-wide media playback via `MediaSessionManager` / `MediaControllerCompat` with automatic `onListenerDisconnected -> requestRebind` resilience.
- **Location & Weather (`LocationHelper`):** Uses Google Play Services `FusedLocationProviderClient` with `PRIORITY_BALANCED_POWER_ACCURACY` and fallback to `LocationManager` to reverse geocode city names.
- **Calendar Keyguard Guard:** Protects calendar queries against locked device states (`keyguardManager.isDeviceLocked`) during Direct Boot.

---

## Developer Setup & Workflow Guide

### 1. Prerequisites & Tooling

Ensure your development workstation has the following tools installed:

| Tool | Minimum Version | Installation / Path |
| :--- | :--- | :--- |
| **JDK** | Java 17+ (Azul Zulu or OpenJDK) | `/Library/Java/JavaVirtualMachines/...` |
| **Android SDK** | API 36 (`compileSdk = 36`) | Android Studio or SDK command-line tools |
| **Node.js & npx** | Node v18+ / npx v10+ | Homebrew (`brew install node`) |
| **Firebase CLI** | v15.30+ | `npx -y firebase-tools@latest` |
| **Google Cloud SDK** | `gcloud` v450+ | `/opt/homebrew/bin/gcloud` |
| **Python** | Python 3.10+ | System or virtual environment |

---

### 2. Firebase CLI & Project Binding

StandBy Android uses Firebase CLI for configuration automation and Firebase Test Lab for physical hardware verification.

> [!IMPORTANT]
> **Tooling Rule:** Never invoke the naked `firebase` binary directly, as standalone binaries on macOS can suffer from architecture mismatches (`bad CPU type`). Always run commands using `npx -y firebase-tools@latest <command>`.

#### 1. Verify Installation
```bash
npx -y firebase-tools@latest --version
```

#### 2. Authenticate
```bash
# Standard interactive login:
npx -y firebase-tools@latest login

# Headless / remote terminal login (no local browser):
npx -y firebase-tools@latest login --no-localhost
```

#### 3. Confirm Project Context
The repository includes `.firebaserc` and `firebase.json` binding the project to `standby-8589f`:
```bash
npx -y firebase-tools@latest use
# Outputs: standby-8589f
```

#### 4. Automated `google-services.json` Retrieval
Never manually download configuration files from the web console. Retrieve it programmatically via:
```bash
npx -y firebase-tools@latest apps:sdkconfig ANDROID 1:861595657257:android:3dc85022c35bfad97ea838 -o app/google-services.json
```

---

### 3. Signing & Google Play Setup

#### Keystore Configuration
Release builds in `app/build.gradle.kts` look for `~/Documents/Projects/my-moves-signing/keystore.properties`:
```properties
storeFile=/path/to/my-moves-upload.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```
*Graceful Fallback:* If `keystore.properties` is absent, the build automatically falls back to debug signing, allowing `assembleRelease` and `bundleRelease` to compile successfully on any machine.

#### Google Play Console Publishing
Automated deployments use the Android Publisher API v3 with a service account JSON located at:
`~/Documents/Projects/my-moves-signing/play-service-account.json`

#### macOS TCC Permissions
If executing terminal commands inside `~/Documents` produces `Operation not permitted`, ensure your terminal application (e.g. Ghostty or Terminal) has **Full Disk Access** enabled under **macOS System Settings > Privacy & Security > Full Disk Access**.

---

### 4. Core Development Commands

```bash
# Run all unit tests (26 test suites)
./gradlew test

# Run the codebase audit linter gate
python3 scripts/audit_linter.py

# Build debug APK
./gradlew :app:assembleDebug

# Build R8-optimized release APK and App Bundle (AAB)
./gradlew :app:bundleRelease :app:assembleRelease
```

---

### 5. Automated Turn Runner Pipeline

StandBy Android enforces an autonomous end-of-turn delivery pipeline via `scripts/turn_runner.sh`:

```bash
bash scripts/turn_runner.sh
```

This single command executes the complete 6-stage delivery cycle:
1. **Verifies Firebase & Tooling Environment:** Diagnoses Firebase CLI version and ensures `app/google-services.json` is synced.
2. **Enforces Auditor Linter Gate:** Validates that the latest independent audit report in `audits/` awards a passing score ($\ge 9.5/10$).
3. **Runs Unit Test Suite:** Executes all 26 test suites to guarantee zero regressions.
4. **Compiles Release Artifacts:** Builds R8-minified AAB bundle and APK.
5. **Uploads to Google Play Console:** Publishes the AAB directly to the Google Play `internal` testing track using the Android Publisher API.
6. **Emits Turn Summary:** Outputs version code, track, bundle size, and Edit ID for distribution.

---

### 6. Firebase Test Lab Physical Device Testing

Validate builds on real physical Google Pixel 9 hardware running Android 16 (API 36) in landscape and portrait orientations:

```bash
/opt/homebrew/bin/gcloud firebase test android run \
  --type=robo \
  --app=app/build/outputs/apk/release/app-release.apk \
  --device=model=tokay,version=36,locale=en,orientation=landscape \
  --device=model=tokay,version=36,locale=en,orientation=portrait \
  --project=standby-8589f \
  --timeout=90s
```

Test results, execution logs, and captured screenshots are accessible through the web matrix link generated in the terminal output.

### 7. Notion Linear Task Tracker (Cross-Turn Issue Tracking)

StandBy Android tracks long-running projects, features, and bug fixes across agent turns via a dedicated **Linear-Clone Hub** in Notion:
* **Hub Page:** [Linear Hub (Agent Task Tracker)](https://app.notion.com/p/Linear-Hub-Agent-Task-Tracker-3e280e72304c81b8b7c3dc9ef85d8ad3)
* **Projects Database:** High-level apps and initiatives (`StandBy Android` [SBY]), with repository tracking via `GitHub URL`.
* **Tasks Database:** Granular issues with statuses (`Backlog`, `Todo`, `In Progress`, `In Review`, `Done`, `Canceled`), priorities, assignees, estimates, multi-select labels, `Pull Requests` tracking, and self-relation `Parent Task` / `Sub-tasks`.
* **Task Page Body:** Every task maintains an inline **🎯 Implementation Plan** and **📋 Subtasks Checklist** with progress checkboxes.

#### Developer CLI Commands (`scripts/notion_tracker.py`)
```bash
# List all tracked projects with repository URLs
python3 scripts/notion_tracker.py list-projects

# View all active issues on the Linear board with PR references
python3 scripts/notion_tracker.py list-tasks

# View full details of a task, including linked subtasks, PRs, and page body plan
python3 scripts/notion_tracker.py get-task SBY-5

# Create a new issue with plan description and subtasks checklist
python3 scripts/notion_tracker.py create-task \
  --title "Live Solar Arc Clock Equinox Telemetry" \
  --priority "High 🟠" \
  --labels "Feature,Design / UI" \
  --estimate 3 \
  --plan "Calculate solar altitude and azimuth dynamically using geographic coordinates" \
  --subtasks "Coordinate math, Canvas rendering, Equinox markers"

# Add a child subtask linked to a parent task
python3 scripts/notion_tracker.py add-subtask \
  --parent SBY-5 \
  --title "Codify Subtask & Plan Rules in WORKFLOW.md"

# Add or append an implementation plan and checklist to an existing task
python3 scripts/notion_tracker.py add-plan SBY-6 \
  --plan "Auto-refresh weather telemetry on 30-minute intervals" \
  --subtasks "Timer loop, Network check, Error chip"

# Update an issue status, record a PR, or move to Done
python3 scripts/notion_tracker.py update-task SBY-5 --status "Done" --pr "PR #21"

# Check Notion API connectivity and database links
python3 scripts/notion_tracker.py sync
```

---

## Autonomous Development & Adversarial Audit Contract

Every code or UI iteration follows the **Autonomous Continuous Improvement Protocol** defined in [`WORKFLOW.md`](WORKFLOW.md):

```mermaid
flowchart TD
    A["Step 1: Plan & Task List First (PLAN.md)"] --> B["Step 2: Live Task Tracking & Modular Coding"]
    B --> C["Step 3: Local Build & Unit Verification (./gradlew test)"]
    C --> D["Step 4: Physical Hardware Testing (Firebase Test Lab)"]
    D --> E["Step 5: Blind Adversarial Auditor Grill Loop"]
    E -->|Defects Found / Score < 9.5 (FAIL)| B
    E -->|Verified PASS (>= 9.5)| F["Step 6: Automated Play Store Rollout & Git Push"]
```

### The 5-Pillar Scorecard
Independent auditor subagents (`independent_code_auditor`) ruthlessly evaluate builds against 5 core pillars:
1. **Aesthetics & Visual Polish (0–10):** Apple StandBy fidelity, negative tracking, zero-emoji compliance, and OLED contrast.
2. **Architecture & Code Quality (0–10):** File modularity (< 500 lines), separation of concerns, zero god-objects.
3. **Gesture Handling & UX (0–10):** Tap interception (`PointerEventPass.Initial`), transient auto-hiding indicators, non-blocking navigation.
4. **Battery Efficiency & OLED Protection (0–10):** Strict 0 fps recomposition during clock sweeps, monotonic pixel shifting, sleep intervals.
5. **Production Readiness (0–10):** 100% passing tests, R8 minification, offline data resilience, real hardware verification.

---

## Architecture & Directory Structure

```
standby-android/
├── .firebaserc                           # Firebase project binding (standby-8589f)
├── firebase.json                         # Firebase root configuration
├── WORKFLOW.md                           # Autonomous development & audit contract
├── PLAN.md                               # Live implementation plan & task tracker
├── README.md                             # Project overview & developer guide
├── app/
│   ├── google-services.json              # Firebase Android SDK configuration
│   ├── build.gradle.kts                  # Signing config, R8 rules & dependencies
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml       # Permissions, DreamService & Receivers
│   │   │   ├── java/com/hoandesign/standby/
│   │   │   │   ├── MainActivity.kt               # Edge-to-edge window insets & sensors
│   │   │   │   ├── StandbyDreamService.kt        # Android system screensaver service
│   │   │   │   ├── data/                         # Battery, Calendar, Weather repositories
│   │   │   │   ├── model/                        # ScreenRatio, NightMode, Weather, Clock models
│   │   │   │   ├── receiver/ChargingReceiver.kt  # Dock & power connected broadcast receiver
│   │   │   │   ├── service/                      # StandbyMediaListenerService
│   │   │   │   └── ui/
│   │   │   │       ├── MainStandbyScreen.kt      # Root Compose container & Quick Settings
│   │   │   │       ├── components/               # PixelShifter, NightMode, PagerIndicators
│   │   │   │       ├── layout/                   # AdaptiveLayoutEngine, MainStandbyPager, DynamicSlotCard
│   │   │   │       ├── theme/                    # OLED palette, StandbyFonts, Theme
│   │   │   │       └── widgets/                  # 14-Module compact & fullscreen widgets
│   │   │   └── res/                              # Vectors, drawables, app icons
│   │   └── test/java/com/hoandesign/standby/     # 26 Unit test suites
├── audits/
│   ├── AUDIT_LATEST.md                   # Symlink / copy of latest validated audit
│   └── v17_audit.md                      # Build 17 adversarial audit report (PASS 10/10)
└── scripts/
    ├── audit_linter.py                   # Integrity gate & markdown audit validator
    ├── auditor_prompt_template.md        # Adversarial auditor prompt template
    ├── notion_config.json                # Notion database and hub configuration
    ├── notion_tracker.py                 # Linear-model cross-turn task tracker CLI
    ├── play_upload_internal.py           # Google Play Publisher API upload script
    ├── ship_play_internal.sh             # Legacy manual upload script
    └── turn_runner.sh                    # End-of-turn autonomous pipeline runner
```

---

## License

Copyright © 2026 Hoan Do ([`hoandesign`](https://github.com/hoandesign)). All rights reserved.  
Licensed under the [Apache License, Version 2.0](LICENSE).
