#!/usr/bin/env python3
"""Upload a signed AAB to Google Play Internal testing via Android Publisher API.

Prerequisites (one-time, manual in Play Console):
  1. Create app with package name matching PACKAGE_NAME (com.hoandesign.standby)
  2. Invite SERVICE_ACCOUNT_EMAIL as a user with permission to manage releases/tracks
  3. Complete required Play declarations enough for Internal testing
  4. Often: manually upload the first AAB once (draft apps), then this script works for updates

Usage:
  python3 scripts/play_upload_internal.py [/path/to/app-release.aab]
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError
from googleapiclient.http import MediaFileUpload

PACKAGE_NAME = "com.hoandesign.standby"
TRACK = "internal"
SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]

SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parent
DEFAULT_AAB = PROJECT_ROOT / "app/build/outputs/bundle/release/app-release.aab"

import os

SERVICE_ACCOUNT_FILE = (
    Path(os.environ["PLAY_SERVICE_ACCOUNT_KEY"])
    if os.environ.get("PLAY_SERVICE_ACCOUNT_KEY") and Path(os.environ["PLAY_SERVICE_ACCOUNT_KEY"]).is_file()
    else (
        Path("/tmp/play-service-account.json")
        if Path("/tmp/play-service-account.json").is_file()
        else Path.home() / "Documents/Projects/my-moves-signing/play-service-account.json"
    )
)


def main() -> int:
    aab = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else DEFAULT_AAB
    if not SERVICE_ACCOUNT_FILE.is_file():
        print(f"Missing service account JSON: {SERVICE_ACCOUNT_FILE}", file=sys.stderr)
        return 1
    if not aab.is_file():
        print(f"Missing AAB: {aab}", file=sys.stderr)
        print("Build first: ./gradlew :app:bundleRelease", file=sys.stderr)
        return 1

    sa = json.loads(SERVICE_ACCOUNT_FILE.read_text())
    print(f"Service account: {sa.get('client_email')}")
    print(f"Package: {PACKAGE_NAME}")
    print(f"AAB: {aab} ({aab.stat().st_size:,} bytes)")
    print(f"Track: {TRACK}")

    creds = service_account.Credentials.from_service_account_file(
        str(SERVICE_ACCOUNT_FILE), scopes=SCOPES
    )
    service = build("androidpublisher", "v3", credentials=creds, cache_discovery=False)
    edits = service.edits()

    try:
        edit = edits.insert(body={}, packageName=PACKAGE_NAME).execute()
    except HttpError as e:
        print("\nFAILED to start Play edit.", file=sys.stderr)
        print(e, file=sys.stderr)
        print(
            "\nUsually means:\n"
            f"  • App {PACKAGE_NAME} does not exist yet in Play Console, OR\n"
            f"  • {sa.get('client_email')} is not invited under\n"
            "    Play Console → Users and permissions\n",
            file=sys.stderr,
        )
        return 2

    edit_id = edit["id"]
    print(f"Edit id: {edit_id}")

    try:
        media = MediaFileUpload(str(aab), mimetype="application/octet-stream", resumable=True)
        bundle = (
            edits.bundles()
            .upload(packageName=PACKAGE_NAME, editId=edit_id, media_body=media)
            .execute()
        )
        version_code = bundle["versionCode"]
        print(f"Uploaded bundle versionCode={version_code}")

        # "completed" = rolled out on the track
        release_status = "completed"
        track_body = {
            "track": TRACK,
            "releases": [
                {
                    "name": f"StandBy {version_code} (Internal)",
                    "status": release_status,
                    "versionCodes": [str(version_code)],
                }
            ],
        }
        edits.tracks().update(
            packageName=PACKAGE_NAME,
            editId=edit_id,
            track=TRACK,
            body=track_body,
        ).execute()
        print(
            f"Assigned versionCode={version_code} to track '{TRACK}' "
            f"with status={release_status}"
        )

        edits.commit(packageName=PACKAGE_NAME, editId=edit_id).execute()
        print("Committed edit successfully (Internal testing rolled out).")
        return 0
    except HttpError as e:
        print("\nFAILED during upload/track/commit — aborting edit.", file=sys.stderr)
        print(e, file=sys.stderr)
        try:
            edits.delete(packageName=PACKAGE_NAME, editId=edit_id).execute()
        except HttpError:
            pass
        return 3


if __name__ == "__main__":
    raise SystemExit(main())
