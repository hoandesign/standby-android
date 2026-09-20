#!/usr/bin/env bash
# ==============================================================================
# StandBy Android — Autonomous Turn Pipeline Runner
# ==============================================================================
# This script executes the mandatory end-of-turn delivery pipeline:
#   1. Verifies Firebase & Cloud environment diagnostics (Rule 13).
#   2. Enforces auditor report & codebase integrity gate (scripts/audit_linter.py).
#   3. Runs all unit tests to guarantee zero regressions.
#   4. Builds R8-minified release bundle (AAB) & APK.
#   5. Uploads the release bundle to Google Play Console (Internal track).
#   6. Emits a structured delivery report for Hoàn.
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
VENV_PY="${HOME}/Documents/Projects/my-moves-signing/.venv/bin/python"

cd "$ROOT"

echo "========================================================"
echo " [1/6] Verifying Firebase & Tooling Environment..."
echo "========================================================"
FIREBASE_VERSION=$(npx -y firebase-tools@latest --version 2>/dev/null | tail -n 1 | tr -d '\r' || echo "N/A")
ACTIVE_PROJECT=$(npx -y firebase-tools@latest use 2>/dev/null | tail -n 1 | tr -d '\r' || echo "unbound")

echo "Firebase CLI version: $FIREBASE_VERSION"
echo "Active Firebase project: $ACTIVE_PROJECT"

# Ensure app/google-services.json is present; if missing, fetch programmatically via Firebase CLI
if [[ ! -f "$ROOT/app/google-services.json" ]] && [[ "$ACTIVE_PROJECT" != "unbound" ]]; then
    echo "Notice: app/google-services.json not found. Fetching programmatically via Firebase CLI..."
    npx -y firebase-tools@latest apps:sdkconfig ANDROID 1:861595657257:android:3dc85022c35bfad97ea838 --project "$ACTIVE_PROJECT" -o "$ROOT/app/google-services.json" || true
fi

echo "========================================================"
echo " [2/6] Enforcing Mandatory Auditor Gate & Linter..."
echo "========================================================"
python3 scripts/audit_linter.py

echo "========================================================"
echo " [3/6] Running Unit Test Suite..."
echo "========================================================"
./gradlew test --quiet

echo "========================================================"
echo " [4/6] Building R8 Release Bundle (AAB & APK)..."
echo "========================================================"
./gradlew :app:bundleRelease :app:assembleRelease --quiet

AAB_FILE="$ROOT/app/build/outputs/bundle/release/app-release.aab"
APK_FILE="$ROOT/app/build/outputs/apk/release/app-release.apk"

if [[ ! -f "$AAB_FILE" ]]; then
    echo "ERROR: Release bundle not found at $AAB_FILE" >&2
    exit 1
fi

AAB_SIZE=$(ls -lh "$AAB_FILE" | awk '{print $5}')
APK_SIZE=$(ls -lh "$APK_FILE" 2>/dev/null | awk '{print $5}' || echo "N/A")

VERSION_CODE=$(grep -E 'versionCode = [0-9]+' app/build.gradle.kts | tr -dc '0-9' || echo "unknown")
VERSION_NAME=$(grep -E 'versionName = "[^"]+"' app/build.gradle.kts | cut -d'"' -f2 || echo "unknown")

echo "Bundle built successfully: $VERSION_NAME (code $VERSION_CODE)"
echo "AAB size: $AAB_SIZE | APK size: $APK_SIZE"

echo "========================================================"
echo " [5/6] Uploading to Google Play Console (Internal Track)..."
echo "========================================================"
UPLOAD_OUTPUT=""
if [[ -x "$VENV_PY" ]]; then
    UPLOAD_OUTPUT=$("$VENV_PY" scripts/play_upload_internal.py "$AAB_FILE")
elif command -v uv &>/dev/null; then
    UPLOAD_OUTPUT=$(uv run --with google-api-python-client,google-auth python3 scripts/play_upload_internal.py "$AAB_FILE")
else
    UPLOAD_OUTPUT=$(python3 scripts/play_upload_internal.py "$AAB_FILE")
fi

echo "$UPLOAD_OUTPUT"

EDIT_ID=$(echo "$UPLOAD_OUTPUT" | grep -E 'Edit id:' | awk '{print $3}' || echo "N/A")

echo "========================================================"
echo " [6/6] Pipeline Complete! Summary for Turn Response:"
echo "========================================================"
cat <<EOF
### Play Store Deployment & Audit Summary
- **App:** com.hoandesign.standby
- **Version:** $VERSION_NAME (versionCode: $VERSION_CODE)
- **Track:** internal
- **AAB Size:** $AAB_SIZE (APK: $APK_SIZE)
- **Play Edit ID:** $EDIT_ID
- **Firebase Project:** $ACTIVE_PROJECT (CLI v$FIREBASE_VERSION)
- **Auditor Gate:** PASS (verified by scripts/audit_linter.py -> audits/v${VERSION_CODE}_audit.md)
- **Status:** COMPLETED (Rolled out to internal testers)
EOF
