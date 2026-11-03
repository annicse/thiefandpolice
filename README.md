# Thief and Police

A small multiplayer guessing game for people sitting in the same room, each using their own phone. It is a digital version of **Raja-Rani-Chor-Police** (King-Queen-Thief-Police), a classic paper-chit game played by children and families across Bangladesh.

> **Status: work in progress.** The backend can create a game, let players join with a code and report the lobby state. The game rounds and the frontend screens are still being built. See the [roadmap](#roadmap).

## Background

In the original game, four players draw folded paper chits, each marked with a secret role and a number of points. The King announces himself, the King asks for his Police, the Police reveals himself, and then the Police has to work out which of the remaining two players is the Thief by watching faces and listening to bluffs. A notebook keeps the score.

This project replaces the chits and the notebook with phones, and keeps everything else in real life. There is no chat and no emoji reactions on purpose: the talking, staring and bluffing is the fun part, so the app only handles what paper was doing before (secret roles, reveals, scoring).

I built it for two reasons: to learn Java and Spring Boot properly by building something with real state and rules, and to have a small, finished, demo-able project that people can actually play.

## How the game works

Each round has four players. The host creates a game with a 4-letter code and the players join with that code on their phones.

| Role | Points per round |
|---|---|
| King | 1000 |
| Queen | 800 |
| Police | 500 |
| Thief | 0 |

1. The server deals one random role to each player. Everyone sees only their own role.
2. The King reveals himself in the app, and everyone's screen shows who the King is.
3. The King asks out loud, "Who is my Police?" The Police taps a button to reveal himself.
4. The Police questions the others out loud, then picks one player in the app as the Thief.
5. All roles are revealed and points are added.

If the Police picks the real Thief, the Police keeps 500 points and the Thief gets 0. If the Police picks wrong, the Police gets 0 and the Thief takes the 500. The King and Queen always keep their points. After the chosen number of rounds, the highest total wins.

The host is not a player. The host screen (a laptop or a phone) shows the game state and scoreboard but never shows secrets before the reveal.

## Tech stack

| Layer | Choice |
|---|---|
| Backend | Java 21, Spring Boot 4, Maven |
| Frontend | React, TypeScript, Vite, Tailwind CSS |
| Server state | TanStack Query (polling) |
| Client state | Zustand |
| Routing | React Router |
| Storage | In-memory (no database) |

## Approach and design decisions

**The server is the single source of truth.** Clients only send intentions ("I am the King and I want to reveal", "I guess player X"). The server checks who is allowed to do what in the current phase and rejects everything else.

**Secrets never leave the server.** Roles and tokens are not part of any response meant for other players. Each viewer gets a filtered view of the same game state: a player sees their own role, the host sees only public information, and everything is revealed only at the end of a round. Hiding data in the frontend would not be enough, because anyone can open the browser's network tab.

**No accounts, no JWT.** The server already remembers the whole game, and there are no user accounts, so stateless tokens would add complexity without a benefit. The host and each player get a random opaque token (a UUID) when they create or join a game. It is sent in an `X-Token` header, and the server looks it up in its own memory.

**Polling instead of WebSockets.** Everyone is in the same room and all actions go through normal REST calls, so the only thing the clients need is to hear that the state has changed. The frontend polls the state endpoint every second or so with TanStack Query. A possible later upgrade is Server-Sent Events, which would only change the trigger and not the rest of the code.

**In-memory state.** A game lives in a `ConcurrentHashMap` and disappears when the server restarts. That is fine for a party game, and it keeps the project focused on game logic rather than persistence. Joining is synchronized so two people tapping "join" at the same moment cannot exceed four players.

**Roles and points as data.** Roles and scoring are kept simple and separate from the game flow so that more roles (Babu, Dakat) or other themes can be added later without rewriting the rules.

## API

| Method | Endpoint | Who | Description |
|---|---|---|---|
| GET | `/api/health` | anyone | Server health check |
| POST | `/api/games` | host | Create a game, returns the code and host token |
| POST | `/api/games/{code}/join` | player | Join with a name, returns the player token |
| GET | `/api/games/{code}/state` | host or player | Filtered game state, requires the `X-Token` header |

More endpoints are planned for starting a game, the reveals, the guess and the next round.

## Running locally

You need Java 21 and a recent LTS version of Node.js.

```bash
# Backend (http://localhost:8080)
cd backend
./mvnw spring-boot:run

# Frontend (http://localhost:5173), in a second terminal
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` requests to the backend, so no CORS setup is needed during development.

Quick check of the backend with curl:

```bash
curl -s -X POST http://localhost:8080/api/games \
  -H "Content-Type: application/json" -d '{"totalRounds":10}'
```

## Project structure

```
thiefandpolice/
├── backend/    Spring Boot API and game logic
└── frontend/   React app for the host and player screens
```

## Roadmap

- [x] Project setup, health endpoint, frontend connected to the backend
- [x] Create game, join with a code, lobby state with filtered views
- [ ] Start game and deal roles
- [ ] King reveal, Police reveal, guess and scoring
- [ ] Rounds, scoreboard and game over
- [ ] Frontend: home, lobby, player and host screens
- [ ] Automated tests for the game rules
- [ ] Deployment with a live demo link
- [ ] Optional: Server-Sent Events, a second theme, Bengali and English UI

## What I'm learning from this

Spring Boot REST APIs and validation, modelling a small state machine, keeping secrets on the server, TanStack Query for server state versus Zustand for UI state, and thinking through which parts of a game belong in code and which are better left to people.