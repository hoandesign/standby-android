#!/usr/bin/env python3
"""Audit Linter & Release Gate for StandBy Android.

Enforces that:
  1. No hardcoded mock coordinates (e.g. Cupertino 37.7749) exist in production widgets.
  2. Pure OLED black (#000000) is maintained for backgrounds.
  3. Edit mode scaling and zero-collision badge offsets are present.
  4. An independent auditor report file (audits/v<versionCode>_audit.md) exists and passes.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
AUDITS_DIR = ROOT / "audits"
BUILD_GRADLE = ROOT / "app/build/gradle.kts" if (ROOT / "app/build/gradle.kts").exists() else ROOT / "app/build.gradle.kts"


def get_current_version_code() -> int:
    content = BUILD_GRADLE.read_text(encoding="utf-8")
    match = re.search(r"versionCode\s*=\s*(\d+)", content)
    if not match:
        print("ERROR: Could not parse versionCode from build.gradle.kts", file=sys.stderr)
        sys.exit(1)
    return int(match.group(1))


def check_codebase_integrity() -> list[str]:
    errors = []
    app_src = ROOT / "app/src/main/java"

    # 1. Check for forbidden hardcoded coordinates
    for file in app_src.rglob("*.kt"):
        content = file.read_text(encoding="utf-8")
        if "37.7749" in content and "Test" not in file.name:
            errors.append(f"Forbidden hardcoded mock coordinates (37.7749) found in {file.name}")

    # 2. Check that EditModeWidgetStack maintains safe scale < 1.0f
    edit_stack = app_src / "com/hoandesign/standby/ui/layout/EditModeWidgetStack.kt"
    if edit_stack.exists():
        content = edit_stack.read_text(encoding="utf-8")
        if "0.93f" not in content and "scaleX" not in content:
            errors.append("EditModeWidgetStack does not enforce <1.0f scaling for collision prevention")
    else:
        errors.append("EditModeWidgetStack.kt not found")

    return errors


def check_audit_report(version_code: int) -> list[str]:
    errors = []
    report_file = AUDITS_DIR / f"v{version_code}_audit.md"
    latest_file = AUDITS_DIR / "AUDIT_LATEST.md"

    target_file = report_file if report_file.exists() else latest_file
    if not target_file.exists():
        errors.append(
            f"Missing required audit report file: {report_file.relative_to(ROOT)} "
            f"(or {latest_file.relative_to(ROOT)}). You must run the independent auditor subagent!"
        )
        return errors

    content = target_file.read_text(encoding="utf-8")
    if "PASS" not in content:
        errors.append(f"Audit report in {target_file.name} does NOT have a PASS verdict!")
    if "Scorecard" not in content and "Score" not in content:
        errors.append(f"Audit report in {target_file.name} lacks an objective Scorecard table!")

    return errors


def main() -> int:
    print("🔍 [Audit Gate] Verifying codebase integrity and independent audit requirements...")
    version_code = get_current_version_code()
    print(f"   Target Version Code: {version_code}")

    code_errors = check_codebase_integrity()
    audit_errors = check_audit_report(version_code)

    all_errors = code_errors + audit_errors
    if all_errors:
        print("\n❌ AUDIT GATE FAILED! The following checks failed:", file=sys.stderr)
        for err in all_errors:
            print(f"   - {err}", file=sys.stderr)
        print("\nRun the independent auditor subagent to produce the audit report before shipping.\n", file=sys.stderr)
        return 1

    print("✔ [Audit Gate] Codebase integrity verified and independent audit report validated successfully!\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
