# Thief and Police (Chor-Police) — project context

## What this is
A small in-person multiplayer guessing game (Bangladeshi "Raja-Rani-Chor-Police"),
built as a learning + portfolio project. Players sit together in a room and each
use their own phone. A host creates a game session with a 4-letter code, players join.
Phones replace the paper chits and the score notebook. Talking and bluffing happen
in real life, NOT in the app (no chat, no emoji reactions, no PWA).

## About the developer (important)
- Experienced frontend/web developer (18 years), mother tongue Bengali.
- BEGINNER in Java and Spring Boot. Basic knowledge of Node.js.
- Goal is to LEARN, and to be able to explain every part in an interview.
- ALWAYS explain in simple Bengali (code and identifiers stay in English).
- Work in small steps. Explain what each new file/annotation does before or after writing it.
- Do not write the core game logic (start/deal roles, reveal king, reveal police,
  guess, scoring, next round) unless I explicitly ask. I will write it myself and
  ask you to review. For those parts: hint, review, explain, don't solve.
- Do not add new dependencies, run sudo, or pipe downloaded scripts into a shell
  without asking me first.
- Suggest a git commit message after each finished step.

## Game rules (MVP: exactly 4 players, no Dakat/Babu yet)
Roles and points per round: KING 1000, QUEEN 800, POLICE 500, THIEF 0.
Round flow (phases): LOBBY -> KING_REVEAL -> POLICE_REVEAL -> GUESSING -> RESULT
-> next round or GAME_OVER.
1. Host starts; server randomly deals one role per player. Each player sees only their own role.
2. King taps a button in the app; everyone's screen (host included) shows who the king is.
3. King asks aloud "Who is my police?"; Police taps a button to reveal; everyone is notified.
4. Police questions people aloud, then picks one player in the app as the thief.
5. RESULT: all roles are revealed on all screens and scores are added.
Scoring: King and Queen always get their points. If police guesses the thief
correctly: police keeps 500, thief 0. If wrong: police 0, thief gets 500.
Host chooses the number of rounds (10 or 20) when creating the game. After all
rounds, highest total score wins. The host is NOT a player (a laptop or phone
showing status); the host screen never shows secrets before RESULT.
Design the role/point config so other roles or themes can be added later.

## Architecture decisions
- Backend: Java 21, Spring Boot 4.x, Maven. Package: com.example.thiefandpolice.
- State: in-memory (ConcurrentHashMap in GameService). No database. Lost on restart; acceptable.
- Auth: no login. Random UUID tokens: hostToken (host) and a token per player. Sent in the X-Token header.
- Server is the single source of truth and validates everything (who may act, in which phase).
- Secrets: the server never sends other players' roles or tokens. Responses are
  filtered per viewer (player view / host view / result view). DTOs live in GameDtos.java.
- Realtime: client POLLING via TanStack Query (refetchInterval ~1-1.5s). Possible later upgrade: SSE.
  Not WebSocket.
- Frontend: React + TypeScript + Vite, TanStack Query (server state), Zustand (client UI state:
  token, name, card flipped, sound), React Router, Tailwind CSS v4. Mobile-first web app, not a PWA.
  Vite dev proxy forwards /api to http://localhost:8080.
- Game code: 4 letters (no I and O), case-insensitive.

## Repo layout
thiefandpolice/
  backend/   Spring Boot (Maven wrapper: ./mvnw spring-boot:run, port 8080)
  frontend/  React + Vite (npm run dev, port 5173)

## API (current and planned)
Done:
- GET  /api/health
- POST /api/games                       -> { code, hostToken }      body: { totalRounds }
- POST /api/games/{code}/join           -> { code, playerId, name, token }   body: { name }
- GET  /api/games/{code}/state          header X-Token, filtered view, viewerType HOST or PLAYER
Planned: POST start, reveal-king, reveal-police, guess, next-round (all validated server-side).

## Current status
Done: environment setup, backend health endpoint, React + Vite + Tailwind + TanStack Query
connected to the backend, game create/join/state endpoints (Phase, Role, Player, Game,
GameService, GameDtos, GameController).
Next: game logic in Game.java (start/deal roles -> reveal king -> reveal police -> guess
-> score) written by me; then frontend home, lobby and player/host screens.
Later: Kanban board demo project, README, deployment.