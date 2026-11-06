# Backend (Spring Boot)

Java 21, Spring Boot 4, Maven. Game state is in memory (no database). The server is the single source of truth.

## Commands

Run from `backend/`:

```bash
./mvnw spring-boot:run   # API on http://localhost:8080 (PORT env overrides)
./mvnw test              # all tests
./mvnw test -Dtest=GameFlowTest#methodName   # one test
```

## Structure (`com.example.thiefandpolice`)

| File | Responsibility |
|---|---|
| `Game.java` | One game: players, phases, dealing, scoring. All rules live here. Public methods are `synchronized`. |
| `GameService.java` | Holds games in a `ConcurrentHashMap`, creates games, finds them by code, evicts idle games on a schedule. |
| `GameController.java` | REST endpoints under `/api/games`. Thin: validate input, call the service. |
| `GameDtos.java` | Request and response records. Responses are built per viewer. |
| `Role.java`, `Phase.java`, `Player.java` | Roles (with points), phases, player data. |
| `HealthController.java` | `GET /api/health`. |

## Rules for changes

- **Never trust the client.** Every action is checked on the server: who is allowed to act and in which phase. Wrong actor means 403, wrong phase or state means 409.
- **Secrets never leave the server.** Other players' roles and all tokens must never appear in a response. Role is sent only in `RESULT` and `GAME_OVER`. `Game.toPublicView` is the only place a `PlayerView` is built; keep it that way.
- Tokens are random UUIDs sent in the `X-Token` header. Do not add tokens to any response body other than create and join.
- Keep game rules and scoring in `Game` and `Role`, not in controllers.
- Do not change game rules or scoring unless asked.

## Testing

- `GameTest`, `GameFlowTest`: rules, scoring, secret filtering, dealing fairness.
- `GameServiceTest`: idle game eviction.
- `GameControllerTest`: HTTP layer with MockMvc, including a full round that checks the raw JSON for leaked roles and tokens.
- Tests that check for secret leaks must keep passing. Add a test for every new endpoint or rule.
- CI (`.github/workflows/ci.yml`) runs `./mvnw -B test` on every push and pull request.

## Deployment

`Dockerfile` builds the jar and runs it on a JRE image. Hosted on Render (free tier, sleeps when idle, so running games are lost). The port comes from the `PORT` env variable.
