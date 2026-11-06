# Frontend (React + Vite)

React, TypeScript, Vite, Tailwind CSS v4, TanStack Query (server state, polling), Zustand (UI state), React Router.

## Commands

Run from `frontend/`:

```bash
npm install
npm run dev      # http://localhost:5173, proxies /api to http://localhost:8080
npm run lint     # oxlint, includes jsx-a11y rules
npm run build    # tsc -b && vite build (this is also the type check)
```

CI runs `npm ci`, `npm run lint` and `npm run build`.

## Structure (`src/`)

| Path | Responsibility |
|---|---|
| `api.ts` | Types matching the backend DTOs, one `request()` function, the `api` object, `ApiError`. All calls use relative `/api` paths. |
| `store.ts` | Zustand store: session (code, token, name, isHost) and card-flipped state. Persisted in `sessionStorage`, so each browser tab is its own player. |
| `pages/Home.tsx` | Create or join a game. |
| `pages/GamePage.tsx` | Polls the game state (about every 1.2 s) and picks the screen by phase and viewer type. Also owns the live announcements and focus handling. |
| `components/` | One component per screen or part: `Lobby`, `PlayerRound`, `PlayerResult`, `HostScreen`, `GameOver`, `Scoreboard`, `RoleCard`, `RoundSummary`, `ServerStatus`. |
| `announcements.ts` | Pure functions for the screen reader announcements, phase headings and winners. |
| `roles.ts` | How each role is displayed. |

## Conventions

- All text shown to players is English.
- The server decides everything. The UI only shows what the state response contains and sends intentions. Do not compute secrets or hide data on the client; if the server does not send it, the client does not have it.
- Server data goes through TanStack Query. Zustand is only for client UI state. Do not copy server data into Zustand.
- TypeScript settings to respect: `erasableSyntaxOnly` (no enums or constructor parameter properties), `verbatimModuleSyntax` (use `import type` for types), `noUnusedLocals`.
- Do not put the session token in URLs, logs or error reports.

## Accessibility rules

- Use real `button`, `label`, `h1` to `h3` and landmarks. Every page has one `h1`.
- Keep the visible focus ring, touch targets of at least 44px, and enough contrast (4.5:1 for text).
- Never rely on colour alone to show a role or a selected state; add text.
- Announcements use public information only. Never announce another player's role, and never announce the player's own role automatically.
- `npm run lint` must stay clean.

## Deployment

Vercel, root directory `frontend`. `vercel.json` rewrites `/api/*` to the Render backend and sends every other path to `index.html` for client-side routing. There are no environment variables.
