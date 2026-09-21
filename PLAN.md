# Implementation Plan: Interactive Complications & Deep-Link Intent Shortcuts

**Target Version:** v1.1.7 (versionCode 18)  
**Notion Task:** SBY-10 (Interactive complications and direct app deep-links) & SBY-14 (Add safe system intent launch helper for clock, battery, and calendar)  
**Objective:** Transform passive StandBy ambient displays into an interactive control surface: tapping any complication or widget tile smoothly and safely routes the user directly to the native Android system app or settings (Battery Usage, Google Clock / Alarm, Google Calendar / Agenda, Native Weather, Wi-Fi, Bluetooth, Cellular, and Storage).

---

## 1. Problem Analysis & Opportunity

Currently, StandBy Android widgets are predominantly ambient and non-interactive (except for media playback controls, the Celsius/Fahrenheit toggle in Weather, and brightness sliders in System Bento).
When a user docks their device on a nightstand or desk and glances at the display:
1. **Battery Tile:** Shows live charge percentage, voltage, and charging speed, but tapping does nothing. Users expect to tap the battery tile to inspect Android Battery Usage or Power Saver settings.
2. **Alarm Complication (Clock Dials & Big Digital Clock):** Displays the next scheduled alarm (e.g., "6:30 AM" or bell icon), but tapping does not open the system alarm manager or clock app.
3. **Calendar & Agenda Widgets:** Displays upcoming meetings, relative dates, and day grids, but tapping an event or date cell does not open the event details or day in Google Calendar.
4. **Weather Widget:** Tapping anywhere currently toggles between °C and °F. Users want to be able to tap weather conditions, the city label, or the 7-day forecast to launch the system Weather app/forecast, while retaining temperature unit toggling.
5. **System Bento Widget:** Connectivity pills (Wi-Fi, Bluetooth, Cellular) only re-fetch telemetry. Tapping them should deep-link directly into Android Wi-Fi, Bluetooth, and Network settings.

### Safety & Crash-Proof Design Requirements:
- **`ActivityNotFoundException` Resilience:** Custom ROMs, emulators, tablets, and minimal Android installs may lack default packages (e.g. Google Calendar or specific OEM clock apps). All intent dispatches must be wrapped in safe fallback chains that never crash the app.
- **Context Handling (`FLAG_ACTIVITY_NEW_TASK`):** If invoked from a non-Activity context (e.g. `DreamService` or Application Context), `startActivity` must include `Intent.FLAG_ACTIVITY_NEW_TASK`.
- **Zero Layout & Animation Regression:** Adding click listeners must not introduce layout shifts, recomposition loops, or interfere with top navigation menu auto-hiding or vertical pager scrolling.

---

## 2. Architecture & Implementation Plan

### Phase 1: Safe Intent Launch Helper (`SystemIntents.kt`)
Create `com.hoandesign.standby.util.SystemIntents` object with robust methods:
- `launchBatterySettings(context: Context): Boolean`: Launches `Intent.ACTION_POWER_USAGE_SUMMARY`, falling back to `Settings.ACTION_BATTERY_SAVER_SETTINGS` and `Settings.ACTION_SETTINGS`.
- `launchAlarmClock(context: Context): Boolean`: Launches `AlarmClock.ACTION_SHOW_ALARMS`, falling back to `AlarmClock.ACTION_SET_ALARM`, OEM clock package launchers (`com.google.android.deskclock`, `com.sec.android.app.clockpackage`, `com.android.deskclock`), and `Settings.ACTION_DATE_SETTINGS`.
- `launchSystemCalendar(context: Context, epochMillis: Long? = null): Boolean`: Launches target timestamp in calendar view (`content://com.android.calendar/time/<millis>`), falling back to `Intent.CATEGORY_APP_CALENDAR` or `com.google.android.calendar`.
- `launchCalendarEvent(context: Context, eventId: Long): Boolean`: Launches specific event details via `CalendarContract.Events.CONTENT_URI`, falling back to `launchSystemCalendar`.
- `launchWeather(context: Context, cityName: String? = null, lat: Double? = null, lon: Double? = null): Boolean`: Launches Google Weather activity, OEM weather packages, or geocoded weather search.
- `launchWifiSettings(context: Context): Boolean`: Launches `Settings.ACTION_WIFI_SETTINGS`.
- `launchBluetoothSettings(context: Context): Boolean`: Launches `Settings.ACTION_BLUETOOTH_SETTINGS`.
- `launchCellularSettings(context: Context): Boolean`: Launches `Settings.ACTION_NETWORK_OPERATOR_SETTINGS`.
- `launchStorageSettings(context: Context): Boolean`: Launches `Settings.ACTION_INTERNAL_STORAGE_SETTINGS`.
- `launchTimer(context: Context): Boolean`: Launches `AlarmClock.ACTION_SET_TIMER`.

### Phase 2: Widget Integration
- **`BatteryWidget.kt`**: Make compact and fullscreen cards clickable to launch battery settings.
- **`BigDigitalClockWidget.kt`**: Make `AlarmIndicatorChip` launch alarm clock, and `DateBannerChip` launch calendar.
- **`RadialClockWidget.kt`**: Add tap zone detection on Canvas dial (left quadrant -> alarm clock, right quadrant -> calendar).
- **`ScheduleWidget.kt`**: Wire `onEventClick` default to `launchCalendarEvent` and empty banner to `launchSystemCalendar`.
- **`MonthCalendarWidget.kt`**: Wire `onDateClick` default to `launchSystemCalendar(dateMillis)` and month title to `launchSystemCalendar()`.
- **`WeatherWidget.kt`**: Tapping temperature number toggles °C/°F; tapping city, icon, condition, or daily range launches native Weather app/search.
- **`SystemBentoWidget.kt`**: Wire Wi-Fi, Bluetooth, and Cell pills to launch respective settings; wire storage card to launch storage settings.
- **Other Clocks (`AnalogClockWidget.kt`, `RectangleAnalogClockWidget.kt`, `RetroFlipClockWidget.kt`)**: Dial tap launches clock/alarms.

### Phase 3: Unit Testing (`SystemIntentsTest.kt`)
- Verify intent construction for each launcher.
- Verify fallback chains when primary intent fails.
- Verify non-Activity context automatically adds `FLAG_ACTIVITY_NEW_TASK`.
- Verify exception handling returns `false` without throwing.

### Phase 4: Local Verification & Adversarial Audit
- Run `./gradlew test` to ensure 100% pass rate.
- Run `python3 scripts/audit_linter.py`.
- Bump `versionCode = 18`, `versionName = "1.1.7"` in `app/build.gradle.kts`.
- Spawn fresh blind adversarial auditor (`independent_code_auditor`) using `scripts/auditor_prompt_template.md`.
- Ensure auditor scorecard is $\ge 9.5/10$ with PASS verdict.

### Phase 5: Automated Turn Pipeline & Reporting
- Execute `scripts/turn_runner.sh` to compile release AAB/APK, upload to Google Play Internal testing track, and push to GitHub `main`.
- Update Notion Linear Hub tasks (SBY-10 and SBY-14) to `Done`.
- Send full report to caller agent ("parent").

---

## 3. Live Task Checklist

- [✔] **Task 1: SystemIntents Helper (`SystemIntents.kt`):** Implement crash-proof intent dispatchers with robust fallbacks.
- [✔] **Task 2: Unit Test Suite (`SystemIntentsTest.kt`):** Author comprehensive test suite verifying intent resolution, flags, and exception resilience.
- [✔] **Task 3: Battery Widget Deep-Link:** Wire `launchBatterySettings` into `BatteryWidget.kt` scoped to complication pill and ring.
- [✔] **Task 4: Clock & Alarm Deep-Links:** Wire `launchAlarmClock` and `launchSystemCalendar` into `BigDigitalClockWidget.kt` and `RadialClockWidget.kt` with discrete non-blocking touch targets.
- [✔] **Task 5: Calendar & Agenda Deep-Links:** Wire calendar event and day launchers into `ScheduleWidget.kt` and `MonthCalendarWidget.kt`.
- [✔] **Task 6: Weather & System Bento Deep-Links:** Wire weather app deep-linking into `WeatherWidget.kt` and settings into `SystemBentoWidget.kt`, with robust `WeatherDefaults` fallback logic.
- [✔] **Task 7: Bump Version & Audit Report:** Bump to versionCode 18 (v1.1.7), run auditor subagent, remediate feedback, and generate certified `audits/v18_audit.md` (score 9.78/10, PASS).
- [✔] **Task 8: End-of-Turn Deployment Pipeline:** Run `scripts/turn_runner.sh` (96 tests pass, R8 release build, Google Play internal rollout edit id 04962324698052513078).
- [✔] **Task 9: Notion & Final Report:** Update SBY-10 and SBY-14 to Done in Notion Linear Hub, deliver final summary to caller agent.
