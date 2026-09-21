#!/usr/bin/env python3
"""
Notion Linear Task Tracker CLI
Enables AI agents, CI/CD runners, and developers to track tasks, subtasks, plans, PRs, and projects across turns.
Modeled after Linear App (Projects + Issues + Subtasks + PRs schema).
"""

import argparse
import json
import os
import sys
import urllib.error
import urllib.request

def get_notion_token():
    # 1. Environment variable
    env_token = os.environ.get("NOTION_API_KEY")
    if env_token:
        return env_token

    # 2. Local MCP config in user home
    mcp_cfg_path = os.path.expanduser("~/.gemini/config/mcp_config.json")
    if os.path.exists(mcp_cfg_path):
        try:
            with open(mcp_cfg_path, "r") as f:
                data = json.load(f)
                headers_str = data.get("mcpServers", {}).get("notion-mcp-server", {}).get("env", {}).get("OPENAPI_MCP_HEADERS", "")
                if headers_str:
                    h = json.loads(headers_str)
                    auth = h.get("Authorization", "")
                    if auth.startswith("Bearer "):
                        return auth.replace("Bearer ", "").strip()
        except Exception:
            pass

    return ""

CONFIG_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "notion_config.json")

def get_headers():
    token = get_notion_token()
    if not token:
        print("Error: NOTION_API_KEY is not set and could not be discovered from ~/.gemini/config/mcp_config.json", file=sys.stderr)
        sys.exit(1)
    return {
        "Authorization": f"Bearer {token}",
        "Notion-Version": "2022-06-28",
        "Content-Type": "application/json"
    }

def load_config():
    if not os.path.exists(CONFIG_PATH):
        print(f"Error: Configuration file not found at {CONFIG_PATH}", file=sys.stderr)
        sys.exit(1)
    with open(CONFIG_PATH, "r") as f:
        return json.load(f)

def make_request(url, data=None, method=None):
    body = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(url, data=body, headers=get_headers(), method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            return json.load(resp)
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode("utf-8")
        print(f"Notion API Error ({e.code}): {err_msg}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"Network Error: {e}", file=sys.stderr)
        sys.exit(1)

# Color styling for CLI
COLOR_RESET = "\033[0m"
COLOR_BOLD = "\033[1m"
COLOR_GREEN = "\033[32m"
COLOR_BLUE = "\033[34m"
COLOR_YELLOW = "\033[33m"
COLOR_RED = "\033[31m"
COLOR_PURPLE = "\033[35m"
COLOR_CYAN = "\033[36m"
COLOR_GRAY = "\033[90m"

def status_badge(status):
    if status == "Done":
        return f"{COLOR_GREEN}● Done{COLOR_RESET}"
    elif status == "In Progress":
        return f"{COLOR_BLUE}◐ In Progress{COLOR_RESET}"
    elif status == "In Review":
        return f"{COLOR_PURPLE}◑ In Review{COLOR_RESET}"
    elif status == "Todo":
        return f"{COLOR_YELLOW}○ Todo{COLOR_RESET}"
    elif status == "Backlog":
        return f"{COLOR_GRAY}◌ Backlog{COLOR_RESET}"
    elif status == "Canceled":
        return f"{COLOR_RED}✕ Canceled{COLOR_RESET}"
    return status

def priority_badge(p):
    if "Urgent" in p:
        return f"{COLOR_RED}Urgent 🔴{COLOR_RESET}"
    elif "High" in p:
        return f"{COLOR_YELLOW}High 🟠{COLOR_RESET}"
    elif "Medium" in p:
        return f"{COLOR_BLUE}Medium 🟡{COLOR_RESET}"
    elif "Low" in p:
        return f"{COLOR_GRAY}Low 🔵{COLOR_RESET}"
    return "None ⚪"

def cmd_list_projects(args):
    cfg = load_config()
    db_id = cfg["projects_database_id"]
    res = make_request(f"https://api.notion.com/v1/databases/{db_id}/query", data={})
    print(f"\n{COLOR_BOLD}=== PROJECTS (Notion Linear Hub) ==={COLOR_RESET}")
    for p in res.get("results", []):
        props = p.get("properties", {})
        title_list = props.get("Project Name", {}).get("title", [])
        title = title_list[0]["plain_text"] if title_list else "Untitled"
        key_list = props.get("Key", {}).get("rich_text", [])
        key = key_list[0]["plain_text"] if key_list else ""
        status = props.get("Status", {}).get("select", {}).get("name", "Unknown")
        priority = props.get("Priority", {}).get("select", {}).get("name", "None")
        github_url = props.get("GitHub URL", {}).get("url") or "Not set"
        print(f"[{key}] {COLOR_BOLD}{title}{COLOR_RESET} | {status_badge(status)} | {priority_badge(priority)}")
        print(f"     GitHub: {COLOR_CYAN}{github_url}{COLOR_RESET} (ID: {p['id']})")
    print()

def find_task_by_key_or_id(identifier_or_id):
    cfg = load_config()
    db_id = cfg["tasks_database_id"]
    
    if len(identifier_or_id) == 36 and "-" in identifier_or_id:
        return identifier_or_id

    query = {
        "filter": {
            "property": "Identifier",
            "rich_text": {"equals": identifier_or_id}
        }
    }
    res = make_request(f"https://api.notion.com/v1/databases/{db_id}/query", data=query)
    results = res.get("results", [])
    if not results:
        return None
    return results[0]["id"]

def cmd_list_tasks(args):
    cfg = load_config()
    db_id = cfg["tasks_database_id"]
    query_body = {"sorts": [{"property": "Identifier", "direction": "ascending"}]}
    res = make_request(f"https://api.notion.com/v1/databases/{db_id}/query", data=query_body)
    
    print(f"\n{COLOR_BOLD}=== TASKS / ISSUES (Linear Board) ==={COLOR_RESET}")
    print(f"{'ID':<8} {'STATUS':<20} {'PRIORITY':<16} {'TITLE':<48} {'PRs':<18} {'EST'}")
    print("-" * 120)
    for t in res.get("results", []):
        props = t.get("properties", {})
        id_list = props.get("Identifier", {}).get("rich_text", [])
        key = id_list[0]["plain_text"] if id_list else "N/A"
        title_list = props.get("Task Name", {}).get("title", [])
        title = title_list[0]["plain_text"] if title_list else "Untitled"
        status = props.get("Status", {}).get("select", {}).get("name", "Unknown")
        priority = props.get("Priority", {}).get("select", {}).get("name", "None")
        estimate = props.get("Estimate", {}).get("number") or "-"
        pr_list = props.get("Pull Requests", {}).get("rich_text", [])
        prs = pr_list[0]["plain_text"] if pr_list else "-"

        # Filter if requested
        if args.status and status.lower() != args.status.lower():
            continue

        print(f"{COLOR_BOLD}{key:<8}{COLOR_RESET} {status_badge(status):<29} {priority_badge(priority):<24} {title[:46]:<48} {prs[:16]:<18} {estimate}")
    print()

def append_plan_and_subtasks(page_id, plan_text=None, subtasks=None):
    blocks = []
    if plan_text:
        blocks.append({
            "type": "heading_2",
            "heading_2": {
                "rich_text": [{"type": "text", "text": {"content": "🎯 Implementation Plan"}}]
            }
        })
        blocks.append({
            "type": "callout",
            "callout": {
                "icon": {"type": "emoji", "emoji": "📝"},
                "rich_text": [{"type": "text", "text": {"content": plan_text}}]
            }
        })

    if subtasks:
        blocks.append({
            "type": "heading_3",
            "heading_3": {
                "rich_text": [{"type": "text", "text": {"content": "📋 Subtasks Checklist"}}]
            }
        })
        for st in subtasks:
            blocks.append({
                "type": "to_do",
                "to_do": {
                    "rich_text": [{"type": "text", "text": {"content": st.strip()}}],
                    "checked": False
                }
            })

    if blocks:
        make_request(
            f"https://api.notion.com/v1/blocks/{page_id}/children",
            data={"children": blocks},
            method="PATCH"
        )

def cmd_create_task(args):
    cfg = load_config()
    db_id = cfg["tasks_database_id"]
    projects_db_id = cfg["projects_database_id"]
    
    # Auto-generate next key if not provided
    key = args.key
    if not key:
        res = make_request(f"https://api.notion.com/v1/databases/{db_id}/query", data={})
        next_num = len(res.get("results", [])) + 1
        key = f"SBY-{next_num}"

    properties = {
        "Task Name": {"title": [{"type": "text", "text": {"content": args.title}}]},
        "Identifier": {"rich_text": [{"type": "text", "text": {"content": key}}]},
        "Status": {"select": {"name": args.status}},
        "Priority": {"select": {"name": args.priority}},
        "Assignee": {"select": {"name": args.assignee}}
    }

    # Optional Project link
    if args.project:
        q_proj = {"filter": {"property": "Key", "rich_text": {"equals": args.project}}}
        p_res = make_request(f"https://api.notion.com/v1/databases/{projects_db_id}/query", data=q_proj)
        if p_res.get("results"):
            properties["Project"] = {"relation": [{"id": p_res["results"][0]["id"]}]}

    # Optional Parent Task link
    if args.parent:
        parent_id = find_task_by_key_or_id(args.parent)
        if parent_id:
            properties["Parent Task"] = {"relation": [{"id": parent_id}]}
        else:
            print(f"Warning: Parent task '{args.parent}' not found. Skipping relation.", file=sys.stderr)

    # Optional PR
    if args.pr:
        properties["Pull Requests"] = {"rich_text": [{"type": "text", "text": {"content": args.pr}}]}

    if args.estimate is not None:
        properties["Estimate"] = {"number": args.estimate}
    if args.labels:
        labels_list = [l.strip() for l in args.labels.split(",")]
        properties["Labels"] = {"multi_select": [{"name": l} for l in labels_list]}

    payload = {
        "parent": {"database_id": db_id},
        "properties": properties
    }
    res = make_request("https://api.notion.com/v1/pages", data=payload)
    task_page_id = res["id"]
    print(f"✓ Created issue [{key}] '{args.title}' in Notion (Page ID: {task_page_id})")

    # Add Plan & Subtasks to page body if specified
    subtasks_list = [s.strip() for s in args.subtasks.split(",")] if args.subtasks else None
    if args.plan or subtasks_list:
        append_plan_and_subtasks(task_page_id, plan_text=args.plan, subtasks=subtasks_list)
        print(f"  ↳ Added Plan & {len(subtasks_list or [])} Subtask(s) to task page body.")

def cmd_add_subtask(args):
    cfg = load_config()
    parent_id = find_task_by_key_or_id(args.parent)
    if not parent_id:
        print(f"Error: Parent task '{args.parent}' not found.", file=sys.stderr)
        sys.exit(1)

    # Create child task
    args.key = None
    args.project = "SBY"
    args.status = args.status or "Todo"
    args.priority = args.priority or "Medium 🟡"
    args.assignee = args.assignee or "AI Agent"
    args.labels = args.labels or "Feature"
    args.pr = None
    args.plan = None
    args.subtasks = None

    cmd_create_task(args)
    print(f"✓ Linked as Subtask of {args.parent}")

def cmd_add_plan(args):
    page_id = find_task_by_key_or_id(args.task)
    if not page_id:
        print(f"Error: Task '{args.task}' not found.", file=sys.stderr)
        sys.exit(1)

    subtasks_list = [s.strip() for s in args.subtasks.split(",")] if args.subtasks else None
    append_plan_and_subtasks(page_id, plan_text=args.plan, subtasks=subtasks_list)
    print(f"✓ Plan and {len(subtasks_list or [])} checklist item(s) added to {args.task} page body.")

def cmd_update_task(args):
    page_id = find_task_by_key_or_id(args.task)
    if not page_id:
        print(f"Error: Task '{args.task}' not found.", file=sys.stderr)
        sys.exit(1)

    properties = {}
    if args.status:
        properties["Status"] = {"select": {"name": args.status}}
    if args.priority:
        properties["Priority"] = {"select": {"name": args.priority}}
    if args.assignee:
        properties["Assignee"] = {"select": {"name": args.assignee}}
    if args.estimate is not None:
        properties["Estimate"] = {"number": args.estimate}
    if args.pr:
        properties["Pull Requests"] = {"rich_text": [{"type": "text", "text": {"content": args.pr}}]}
    if args.parent:
        parent_id = find_task_by_key_or_id(args.parent)
        if parent_id:
            properties["Parent Task"] = {"relation": [{"id": parent_id}]}

    if not properties:
        print("No updates specified.", file=sys.stderr)
        return

    res = make_request(f"https://api.notion.com/v1/pages/{page_id}", data={"properties": properties}, method="PATCH")
    print(f"✓ Task {args.task} updated successfully! Status: {args.status or 'unchanged'}")

def cmd_get_task(args):
    page_id = find_task_by_key_or_id(args.task)
    if not page_id:
        print(f"Error: Task '{args.task}' not found.", file=sys.stderr)
        sys.exit(1)

    page = make_request(f"https://api.notion.com/v1/pages/{page_id}")
    props = page.get("properties", {})
    id_list = props.get("Identifier", {}).get("rich_text", [])
    key = id_list[0]["plain_text"] if id_list else "N/A"
    title_list = props.get("Task Name", {}).get("title", [])
    title = title_list[0]["plain_text"] if title_list else "Untitled"
    status = props.get("Status", {}).get("select", {}).get("name", "Unknown")
    priority = props.get("Priority", {}).get("select", {}).get("name", "None")
    assignee = props.get("Assignee", {}).get("select", {}).get("name", "Unassigned")
    estimate = props.get("Estimate", {}).get("number") or "-"
    prs = props.get("Pull Requests", {}).get("rich_text", [])
    pr_text = prs[0]["plain_text"] if prs else "None"
    parent_rel = props.get("Parent Task", {}).get("relation", [])
    subtasks_rel = props.get("Sub-tasks", {}).get("relation", [])

    print(f"\n{COLOR_BOLD}=== TASK DETAILS: [{key}] {title} ==={COLOR_RESET}")
    print(f"Status:       {status_badge(status)}")
    print(f"Priority:     {priority_badge(priority)}")
    print(f"Assignee:     {assignee}")
    print(f"Estimate:     {estimate} pts")
    print(f"PR List:      {COLOR_CYAN}{pr_text}{COLOR_RESET}")
    print(f"Parent Task:  {len(parent_rel)} linked")
    print(f"Sub-tasks:    {len(subtasks_rel)} linked")
    print(f"Notion URL:   {page.get('url')}\n")

    # Fetch page body blocks
    blocks_res = make_request(f"https://api.notion.com/v1/blocks/{page_id}/children")
    blocks = blocks_res.get("results", [])
    if blocks:
        print(f"{COLOR_BOLD}--- Task Page Body Content ---{COLOR_RESET}")
        for b in blocks:
            btype = b.get("type")
            content = b.get(btype, {})
            text = ""
            if "rich_text" in content:
                text = "".join([t.get("plain_text", "") for t in content["rich_text"]])
            
            if btype.startswith("heading"):
                print(f"\n{COLOR_BOLD}# {text}{COLOR_RESET}")
            elif btype == "callout":
                print(f"  💡 {text}")
            elif btype == "to_do":
                checked = content.get("checked", False)
                mark = f"{COLOR_GREEN}[✔]{COLOR_RESET}" if checked else "[ ]"
                print(f"  {mark} {text}")
            elif btype == "paragraph" and text:
                print(f"  {text}")
        print()

def cmd_sync(args):
    cfg = load_config()
    print(f"\n{COLOR_BOLD}=== NOTION LINEAR TRACKER STATUS ==={COLOR_RESET}")
    print(f"Hub Page:       {cfg['hub_page_url']}")
    print(f"Projects DB:    {cfg['projects_database_url']}")
    print(f"Tasks DB:       {cfg['tasks_database_url']}")
    
    # Ping API
    res_p = make_request(f"https://api.notion.com/v1/databases/{cfg['projects_database_id']}/query", data={"page_size": 1})
    res_t = make_request(f"https://api.notion.com/v1/databases/{cfg['tasks_database_id']}/query", data={"page_size": 1})
    print(f"API Connectivity: {COLOR_GREEN}✓ Connected & Verified{COLOR_RESET}\n")

def main():
    parser = argparse.ArgumentParser(description="Notion Linear Task Tracker CLI")
    subparsers = parser.add_subparsers(dest="subcommand", required=True)

    # list-projects
    p_lp = subparsers.add_parser("list-projects", help="List all tracked projects with GitHub URLs")
    p_lp.set_defaults(func=cmd_list_projects)

    # list-tasks
    p_lt = subparsers.add_parser("list-tasks", help="List all issues / tasks with PRs")
    p_lt.add_argument("--status", help="Filter by status (Backlog, Todo, In Progress, Done, etc.)")
    p_lt.set_defaults(func=cmd_list_tasks)

    # get-task
    p_gt = subparsers.add_parser("get-task", help="View full task details, PRs, and page body plan")
    p_gt.add_argument("task", help="Task Identifier (e.g. SBY-5)")
    p_gt.set_defaults(func=cmd_get_task)

    # create-task
    p_ct = subparsers.add_parser("create-task", help="Create a new task")
    p_ct.add_argument("--title", required=True, help="Task title")
    p_ct.add_argument("--key", help="Linear key (e.g. SBY-8). Auto-generated if omitted.")
    p_ct.add_argument("--project", default="SBY", help="Project Key (e.g. SBY)")
    p_ct.add_argument("--status", default="Todo", help="Status (Backlog, Todo, In Progress, In Review, Done)")
    p_ct.add_argument("--priority", default="Medium 🟡", help="Priority (Urgent 🔴, High 🟠, Medium 🟡, Low 🔵)")
    p_ct.add_argument("--assignee", default="AI Agent", help="Assignee (AI Agent, Hoàn Đỗ, Unassigned)")
    p_ct.add_argument("--estimate", type=int, help="Fibonacci estimate (1, 2, 3, 5, 8)")
    p_ct.add_argument("--labels", help="Comma-separated labels (Feature, Bug, Design / UI, Refactor, Infra / CI, Docs)")
    p_ct.add_argument("--parent", help="Parent Task identifier (e.g. SBY-5)")
    p_ct.add_argument("--pr", help="Pull Request URL or reference (e.g. #21)")
    p_ct.add_argument("--plan", help="Initial plan description to append to page body")
    p_ct.add_argument("--subtasks", help="Comma-separated subtasks checklist to append to page body")
    p_ct.set_defaults(func=cmd_create_task)

    # add-subtask
    p_as = subparsers.add_parser("add-subtask", help="Add a subtask to a parent task")
    p_as.add_argument("--parent", required=True, help="Parent Task identifier (e.g. SBY-5)")
    p_as.add_argument("--title", required=True, help="Subtask title")
    p_as.add_argument("--estimate", type=int, default=1, help="Estimate points")
    p_as.add_argument("--assignee", default="AI Agent", help="Assignee")
    p_as.add_argument("--status", default="Todo", help="Status")
    p_as.add_argument("--priority", default="Medium 🟡", help="Priority")
    p_as.add_argument("--labels", default="Feature", help="Labels")
    p_as.set_defaults(func=cmd_add_subtask)

    # add-plan
    p_ap = subparsers.add_parser("add-plan", help="Add/append implementation plan & checklist to task page")
    p_ap.add_argument("task", help="Task identifier (e.g. SBY-6)")
    p_ap.add_argument("--plan", required=True, help="Plan description")
    p_ap.add_argument("--subtasks", help="Comma-separated checklist subtasks")
    p_ap.set_defaults(func=cmd_add_plan)

    # update-task
    p_ut = subparsers.add_parser("update-task", help="Update an existing task")
    p_ut.add_argument("task", help="Task Identifier (e.g. SBY-5) or Notion Page UUID")
    p_ut.add_argument("--status", help="New status (Backlog, Todo, In Progress, In Review, Done, Canceled)")
    p_ut.add_argument("--priority", help="New priority")
    p_ut.add_argument("--assignee", help="New assignee")
    p_ut.add_argument("--estimate", type=int, help="New estimate")
    p_ut.add_argument("--pr", help="Pull Request URL or reference")
    p_ut.add_argument("--parent", help="Parent Task identifier")
    p_ut.set_defaults(func=cmd_update_task)

    # sync
    p_sync = subparsers.add_parser("sync", help="Check sync connectivity and URLs")
    p_sync.set_defaults(func=cmd_sync)

    parsed = parser.parse_args()
    parsed.func(parsed)

if __name__ == "__main__":
    main()
