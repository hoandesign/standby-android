# Audit & Refactoring Plan

## 1. Plan & Task List (Planning Skill)
- [x] Analyze reference image and complain list.
- [x] Identify UI collision issues for Swipe Indicators in `MainStandbyPager.kt` and `DynamicSlotCard.kt`.
- [x] Fix RectangleAnalogClock bounds constraint issue (remove `minDim` constraint from hands, use `halfW` and `halfH` appropriately) and reduce corner radius to ~18%.
- [x] Ensure dynamic typography bounds (`maxLines = 1`, `TextOverflow.Ellipsis`) are set everywhere (Weather, Clock, etc.).
- [x] Investigate Edge Cases / Boundary data (0 degrees, missing permissions) in data models.
- [x] Refactor codebase based on findings.
- [x] Write Audit Report (v17_audit.md).

## Progress
All critical flaws identified and fixed. Audit generated and copied to `AUDIT_LATEST.md`.
