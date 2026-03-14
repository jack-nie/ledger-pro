# Progress Log

Use this file as the handoff record for unfinished work.

## Current Goal

- Goal: Keep `ledger-pro` locally runnable, switch the workspace to tab-based views, and make handoff/resume easier for future Codex sessions.
- Requested by: user
- Branch: `main`

## Status

- State: `ready_for_review`
- Summary: The tabbed workspace UI is implemented, local login `403` caused by `127.0.0.1` CORS was fixed, and a reusable progress handoff template has been added and pushed to GitHub.
- Last updated: 2026-03-14

## Completed

- Verified the project can run locally with non-conflicting ports: frontend `5173`, backend `8081`, MySQL `3308`, Redis `6381`.
- Fixed backend CORS to allow `http://localhost:5173`, `http://127.0.0.1:5173`, and `http://[::1]:5173`.
- Changed the workspace from a single long page to sidebar-driven tab views.
- Split budget status and account view into dedicated frontend panels.
- Added `PROGRESS.md` as a standard handoff file and pushed the changes to GitHub.

## Changed Files

- `.gitignore` - ignore local frontend/backend runtime log files.
- `backend/src/main/java/com/example/ledgerpro/config/CorsConfig.java` - support multiple allowed local origins.
- `backend/src/main/resources/application.yml` - configure local allowed origins list.
- `frontend/src/App.jsx` - add `activeTab` state, hash sync, and tab-based conditional rendering.
- `frontend/src/components/Sidebar.jsx` - replace anchor navigation with tab buttons.
- `frontend/src/components/TransactionSection.jsx` - support separate `entry` and `transactions` views.
- `frontend/src/components/OverviewPanels.jsx` - keep overview focused on summary content only.
- `frontend/src/components/BudgetStatusPanel.jsx` - new dedicated budget execution panel.
- `frontend/src/components/AccountsPanel.jsx` - new dedicated accounts panel.
- `frontend/src/styles.css` - add tab button and tab layout styles.

## Current Blockers

- Blocker: None at the code/build level.
- Needed to unblock: Manual browser pass is still useful to confirm all tabs and edit flows feel correct end-to-end.

## Next Steps

1. Open the app in a browser and click through all tabs: `overview`, `entry`, `transactions`, `budgets`, `recurring`, `accounts`.
2. Manually test login plus edit flows that should switch tabs automatically, especially transaction edit and recurring rule edit.
3. If the UI behavior looks correct, continue with the next product request and update this file again before stopping mid-task.

## Commands To Resume

```bash
git status
git log --oneline -5
Get-Content PROGRESS.md
```

```bash
# current local runtime access
# frontend: http://127.0.0.1:5173
# backend:  http://127.0.0.1:8081

# if services need to be restarted later:
docker ps
```

## Verification

- Ran: `backend\mvnw.cmd -q -DskipTests package`
- Ran: `npm run build`
- Result: Both backend packaging and frontend production build passed.
- Not yet verified: Full manual browser click-through of all tabs after the tabbed layout change.

## Resume Prompt For Codex

```text
Read PROGRESS.md first, then check git status and the latest commits. Assume the tabbed workspace and local CORS fixes are already done, avoid repeating completed work, and start with the manual browser verification or the next user request.
```

## Notes

- Latest pushed commits:
  - `715ee8b docs: add progress handoff template`
  - `89b0354 feat: switch ledger workspace to tabbed views`
- At the time of this update, local services are still running on non-default ports:
  - frontend `127.0.0.1:5173`
  - backend `127.0.0.1:8081`
  - MySQL container `3308`
  - Redis container `6381`
- The repository worktree was clean when this file was updated.
