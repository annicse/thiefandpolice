# Thief and Police

[![CI](https://github.com/annicse/thiefandpolice/actions/workflows/ci.yml/badge.svg)](https://github.com/annicse/thiefandpolice/actions/workflows/ci.yml)

A real-time multiplayer guessing game for people in the same room, each on their own phone. It is a digital version of **Raja-Rani-Chor-Police** (King-Queen-Thief-Police), a classic paper-chit game from Bangladesh.

**[Live demo](https://thiefandpolice.vercel.app/)** (the backend runs on a free tier and may take ~50 seconds to wake up)

<!-- Add a screenshot or GIF: ![Gameplay](docs/demo.gif) -->

The phones replace the paper chits and the score notebook. Talking, staring and bluffing stay in real life, so there is no chat by design.

## How it works

One host creates a game and shares a 4-letter code. Four players join with their phones. The host screen is not a player: it shows the game state but never any secrets.

| Role | Points |
|---|---|
| King | 1000 |
| Queen | 800 |
| Police | 500 |
| Thief | 0 |

1. The server deals one random role to each player. Everyone sees only their own.
2. The King reveals himself. Everyone sees who the King is.
3. The Police reveals himself, questions the others out loud, then picks the Thief in the app.
4. All roles are revealed and points are added. If the guess is right the Police keeps 500, otherwise the Police gets 0 and the Thief takes 500.
5. After 10 or 20 rounds, the highest total wins (ties produce co-winners).

## Tech stack

| Layer | Choice |
|---|---|
| Backend | Java 21, Spring Boot 4, Maven |
| Frontend | React, TypeScript, Vite, Tailwind CSS v4 |
| State | TanStack Query (server state, polling), Zustand (UI state) |
| Storage | In-memory, no database |
| Tests / CI | JUnit 5, MockMvc, GitHub Actions |
| Hosting | Render (Docker) and Vercel |

## Design decisions

- **Server is the source of truth.** Clients send intentions; the server validates who may act in which phase and answers 403/409 otherwise.
- **Secrets never leave the server.** Each viewer gets a filtered view of the game. Other players' roles and all tokens are never sent, and tests check the raw JSON for leaks.
- **No accounts or JWT.** A random UUID token per player, sent in the `X-Token` header and looked up in server memory.
- **Polling over WebSockets.** Everyone shares a room and actions are plain REST calls, so a 1.2 s poll is simple and enough. SSE is a possible upgrade.
- **Concurrency.** Game methods are `synchronized`, games live in a `ConcurrentHashMap`, and idle games are evicted after 3 hours by a scheduled job.

## API

| Method | Endpoint | Who |
|---|---|---|
| POST | `/api/games` | host creates a game |
| POST | `/api/games/{code}/join` | player joins |
| GET | `/api/games/{code}/state` | filtered view (needs `X-Token`) |
| POST | `/api/games/{code}/start` | host |
| POST | `/api/games/{code}/reveal-king` | king |
| POST | `/api/games/{code}/reveal-police` | police |
| POST | `/api/games/{code}/guess` | police |
| POST | `/api/games/{code}/next-round` | host |

## Run locally

Requires Java 21 and Node.js (LTS).

```bash
# Backend, http://localhost:8080
cd backend && ./mvnw spring-boot:run

# Frontend, http://localhost:5173 (second terminal)
cd frontend && npm install && npm run dev

# Tests
cd backend && ./mvnw test
```

The Vite dev server proxies `/api` to the backend. To play alone, join with dummy players: `node scripts/bots.mjs GAMECODE 3`.

## Known limitations

- Games are in memory, so a server restart (or a free-tier sleep) ends running games.
- No reconnect handling beyond the saved session in the browser tab.
- Exactly 4 players; extra roles (Babu, Dakat) are not implemented yet.

## Project structure

```
backend/    Spring Boot API, game logic, tests
frontend/   React app (host and player screens)
scripts/    Bots for solo testing
```
