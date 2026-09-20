# Implementation Plan: Firebase CLI Workflow Improvements & Comprehensive README Overhaul

**Target Version:** v1.1.6 (versionCode 17)  
**Objective:** Integrate the newly installed Firebase CLI and Firebase Agent Skills into the StandBy Android workflow, codify new tooling rules, and comprehensively overhaul `README.md` to reflect all latest features and provide a clear setup guide for the automated development workflow.

---

## 1. Problem Analysis & Opportunities

### A. Firebase CLI & Skills Integration
1. **Execution Binary Portability:**
   - The standalone `/usr/local/bin/firebase` binary on macOS can fail due to architecture mismatch (`bad CPU type in executable: firebase`).
   - The official Google Firebase Agent Skill (`firebase-basics`) explicitly mandates using `npx -y firebase-tools@latest` for all CLI commands.
2. **Project Context Automation:**
   - The repository lacked `.firebaserc` and `firebase.json`, requiring manual `--project standby-8589f` flags.
   - Adding `.firebaserc` and `firebase.json` scopes all Firebase operations cleanly to `standby-8589f`.
   - The Android client app was registered and `app/google-services.json` fetched programmatically via `npx -y firebase-tools@latest apps:sdkconfig ANDROID <APP_ID>`.
3. **Workflow Automation (`scripts/turn_runner.sh`):**
   - The end-of-turn runner should include a quick Firebase environment diagnostic step to ensure Firebase CLI and Google Cloud authentication are healthy before deployment.

### B. `README.md` Deficiencies
1. **Outdated Version & SDK Target:**
   - Stated `compileSdk = 35` instead of `compileSdk = 36` (Android 16 / VanillaIceCream / Baklava readiness).
   - Stated old 3-page structure instead of the new 4-page hierarchy (Bento, Single Module, Clocks, Now Playing).
2. **Outdated Feature Catalog:**
   - Referenced emojis in weather ("condition icon/emoji") and battery ("⚡ Fast Charging"), violating the Zero-Emoji Material Design standard implemented in v1.1.4.
   - Omitted the new Full-Bleed Tank / Rounded-Rectangle Bauhaus Clock (`RectangleAnalogClockWidget.kt`) with analytic edge normal tick projection.
   - Omitted the redesigned Zero-Footprint Swipe Indicator Capsules with frosted glass backplates.
   - Omitted the reactive °C / °F temperature switcher.
3. **Missing Setup & Workflow Guide:**
   - Lacked a complete, step-by-step developer setup guide for running the turn runner workflow, configuring Firebase CLI (`npx -y firebase-tools@latest`), signing keys, and Google Play Publisher API credentials.

---

## 2. Proposed Architecture & Solutions

1. **Codify Rule 13 (Firebase CLI & Tooling Integration Mandate) in `WORKFLOW.md` & `~/.gemini/LESSONS-LEARNED.md`:**
   - Mandatory invocation: Always prepend with `npx -y firebase-tools@latest` (never use naked `firebase`).
   - Project binding: Maintain `.firebaserc` and `firebase.json` in the root directory.
   - Programmatic configuration: Use `npx -y firebase-tools@latest apps:sdkconfig` instead of directing users to the Firebase Console.
   - Firebase Test Lab & Crashlytics best practices.
2. **Upgrade `scripts/turn_runner.sh`:**
   - Add Step 0: Firebase & Tooling Pre-flight Verification (`npx -y firebase-tools@latest --version` and active project check).
3. **Comprehensive `README.md` Overhaul:**
   - Update Hero & Badges (v1.1.6, API 36, Zero-Emoji, OLED True Black).
   - Document the 4-Page Navigation Hierarchy.
   - Document the Full-Bleed Tank / Rounded-Rectangle Bauhaus Clock.
   - Document the Zero-Footprint Swipe Indicator System.
   - Document the Reactive °C / °F Temperature Engine.
   - Provide a complete **Developer Setup & Workflow Guide**:
     - System prerequisites (JDK 17, Android SDK 36, Node.js/npx, `gcloud`, Python venv).
     - Environment setup (Firebase CLI login, `.firebaserc`, `google-services.json`).
     - Keystore & Play Console API credentials.
     - Common development commands (`./gradlew test`, `python3 scripts/audit_linter.py`, `bash scripts/turn_runner.sh`).
     - Firebase Test Lab physical device testing guide.

---

## 3. Live Task List

- [✔] **Task 1: Plan & Task List Authoring (Rule 12):** Author concrete implementation plan and roadmap in `PLAN.md`.
- [✔] **Task 2: Codify Rule 13 in `WORKFLOW.md` and `~/.gemini/LESSONS-LEARNED.md`:** Document Firebase CLI mandates, headless login handling, and programmatic configuration workflows.
- [✔] **Task 3: Integrate Firebase Pre-flight in `scripts/turn_runner.sh`:** Add pre-flight environment verification checking Firebase CLI and project binding.
- [✔] **Task 4: Comprehensive `README.md` Overhaul:** Rewrite README to document v1.1.6 features, zero-emoji Material design, full-bleed tank clock, swipe indicators, and complete developer setup guide.
- [✔] **Task 5: Verification & Testing:** Run `python3 scripts/audit_linter.py`, `./gradlew test`, and verify markdown integrity.
- [✔] **Task 6: Independent Subagent Review:** Deploy subagent to audit workflow scripts, documentation accuracy, and completeness. (Completed: Subagent issued VERDICT: PASS 10/10 after all 3 remediation items were verified).
- [▶] **Task 7: Commit, Push & Delivery:** Commit all improvements to GitHub `main` and present a simple, non-technical summary to Hoàn.
