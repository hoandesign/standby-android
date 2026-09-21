# Implementation Plan: Notion Task & Project Tracking System (Linear Model)

## 1. Overview & Objective
Establish an automated task and project tracking system in **Notion** modeled after **Linear App**.
This enables autonomous AI agents and Hoàn to track project roadmap, sprint issues, task statuses, estimates, and priorities seamlessly across sessions and turns.

The system utilizes:
1. **Notion API & Integration** (`Antigravity` Bot) for programmatic querying, creation, and state updates.
2. **cmux Browser (Computer-Use)** (`surface:5`) for real-time visual inspection and user interaction inside the terminal workspace.
3. **`scripts/notion_tracker.py`** CLI utility for agent workflows, turn runners, and developers.

---

## 2. Linear Data Model in Notion

### A. Projects Database (`Projects`)
Represents high-level products, apps, or initiatives.
- **Name** (`title`): e.g. `StandBy Android`
- **Identifier / Key** (`rich_text`): e.g. `SBY`
- **Status** (`select`):
  - `Planned` (Gray)
  - `In Progress` (Blue)
  - `Paused` (Yellow)
  - `Completed` (Green)
  - `Canceled` (Red)
- **Priority** (`select`):
  - `Urgent 🔴`
  - `High 🟠`
  - `Medium 🟡`
  - `Low 🔵`
  - `No Priority ⚪`
- **Target Date** (`date`): Estimated completion or release milestone
- **Summary** (`rich_text`): High-level mission and scope
- **Tasks** (`relation`): Dual-direction link to the Tasks database

### B. Tasks Database (`Tasks / Issues`)
Represents individual atomic issues, features, bugs, or refactors.
- **Task Title** (`title`): Clear, actionable issue name
- **Identifier** (`rich_text`): Linear-style key e.g. `SBY-1`, `SBY-2`
- **Project** (`relation`): Link to parent project in Projects DB
- **Status** (`select`):
  - `Backlog` (Gray)
  - `Todo` (Default)
  - `In Progress` (Blue)
  - `In Review` (Purple)
  - `Done` (Green)
  - `Canceled` (Red)
- **Priority** (`select`):
  - `Urgent 🔴`
  - `High 🟠`
  - `Medium 🟡`
  - `Low 🔵`
  - `No Priority ⚪`
- **Assignee** (`select`): `Hoàn Đỗ`, `AI Agent`, `Unassigned`
- **Estimate** (`number`): Fibonacci story points (1, 2, 3, 5, 8)
- **Labels** (`multi_select`):
  - `Feature` (Purple)
  - `Bug` (Red)
  - `Design / UI` (Pink)
  - `Refactor` (Orange)
  - `Infra / CI` (Blue)
  - `Docs` (Gray)
- **Due Date** (`date`): Target completion date
- **Parent Task** (`relation`): Self-relation for subtasks / epics

---

## 3. Implementation Steps

### Phase 1: Create Hub Page & Databases in Notion
- Create root parent page: **`📐 Linear Hub (Agent Task Tracker)`**
- Create **`Projects`** database.
- Create **`Tasks`** database with full Linear properties.
- Link **`Tasks`** and **`Projects`** via bidirectional relation.

### Phase 2: Seed StandBy Android & Active Tasks
- Seed Project: `StandBy Android` (`SBY`)
  - Status: `In Progress`
  - Priority: `High 🟠`
  - Summary: `Ambient iOS 18-inspired smart display for Android with zero-emoji Material iconography, continuous OLED protection, and responsive aspect ratios.`
- Seed Initial Tasks:
  1. `SBY-1`: Swipe Indicator Smart Outer-Edge Anchoring & Capsule Backplates (`Done`)
  2. `SBY-2`: Rectangle Tank Analog Clock 12% Corner Radius & Constant Baton Hands (`Done`)
  3. `SBY-3`: Zero-Emoji Mandate & Google Material Design Vector Icon Pack (`Done`)
  4. `SBY-4`: Firebase CLI Tooling Integration & Test Lab Automated Pipeline (`Done`)
  5. `SBY-5`: Notion Linear Task & Project Tracking Hub Setup (`In Progress`)
  6. `SBY-6`: Weather Station Auto-Refresh & Live GPS Telemetry (`Todo`)
  7. `SBY-7`: Health Connect Concentric Activity Rings Live Telemetry (`Backlog`)

### Phase 3: Build CLI Automation Tool (`scripts/notion_tracker.py`)
Provide a lightweight Python tool with zero external dependencies:
- `list-projects`: Show all active projects and progress.
- `list-tasks [--project SBY] [--status ...]`: Filter and list issues.
- `create-task --title "..." --project "..." --priority "..."`: Create a new issue.
- `update-task --id "..." --status "..."`: Move issue across status board.
- `sync`: Quick verification of Notion connectivity and sync status.

### Phase 4: Workflow Integration & Documentation
- Add **Rule 14** to `WORKFLOW.md`:
  - Every non-trivial task or turn must create or update its corresponding issue in the Notion Linear Hub.
- Document setup and CLI usage in `README.md`.
- Display live view in cmux browser split (`surface:5`).

---

## 4. Verification & Testing
- Verify Notion API responses for all CRUD operations.
- Inspect rendered databases visually in cmux browser `surface:5`.
- Validate CLI commands `python3 scripts/notion_tracker.py list-tasks`.
