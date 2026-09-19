#!/usr/bin/env bash
# Ship current StandBy Android code to Play Internal testing.
# Usage (from anywhere):
#   ~/Documents/Github/standby-android/scripts/ship_play_internal.sh
# Or create an alias:
#   alias standby-ship='~/Documents/Github/standby-android/scripts/ship_play_internal.sh'
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
VENV_PY="${HOME}/Documents/Projects/my-moves-signing/.venv/bin/python"

cd "$ROOT"

echo "→ Building release AAB for StandBy Android…"
./gradlew :app:bundleRelease

echo "→ Uploading to Google Play Internal testing…"
if [[ -x "$VENV_PY" ]]; then
    "$VENV_PY" scripts/play_upload_internal.py "$@"
else
    echo "Notice: Virtualenv python not found at $VENV_PY. Falling back to system python3."
    python3 scripts/play_upload_internal.py "$@"
fi

# Print the active version info
echo "→ Deployed Version Info:"
grep -E 'versionCode|versionName' app/build.gradle.kts || true
echo "✔ Done. StandBy update dispatched to Play Console Internal Testing."
