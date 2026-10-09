# game-center-api

A **Game Center REST API** for a portfolio of mobile games: device authentication with JWT,
player data save/load, leaderboards, match records, a game catalogue and categories - with the
API surface documented through OpenAPI/Swagger.

```bash
make up
# api      : http://localhost:8080/api/v1
# swagger  : http://localhost:8080/swagger-ui.html
```

## What this demonstrates

- **Layered Spring Boot service**: `controllers` (thin, HTTP only) → `services` → `repositories`
  (Spring Data Mongo) with `dto` request/response types and `mappers` between them, so the
  persisted document shape never leaks to the client.
- **Two datastores with different jobs**: MongoDB keeps the game catalogue, categories,
  leaderboards and player data; Redis carries the caches and the pub/sub plumbing created in
  `configs/redis`. Both are wired through their own `@Configuration`, which keeps connection
  concerns out of the business code.
- **Stateless auth**: a JWT filter (`configs/jwt/JwtAuthFilter` + `JWTSecurityConfig`) guards the
  API, so an instance can be replaced or scaled out without losing sessions.
- **Error handling as data**: `constants/ApiErrorEnum` plus a single response envelope
  (`dto/responses/Response`) means every endpoint fails in the same shape.
- **Paging and HATEOAS**: `PagingRequest` and `spring-boot-starter-hateoas` are in place for the
  list endpoints, which is what a game client needs to page through a catalogue.
- **Containerised end to end**: `Dockerfile` (maven build stage → temurin runtime) and a
  `docker-compose.yml` that brings up mongo + redis + the api with health-gated startup order.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/auth/register` | create an account |
| POST | `/api/v1/auth/login` | sign in, returns the JWT |
| POST | `/api/v1/auth/renew` | exchange a token for a fresh one |
| GET | `/api/v1/categories`, `/api/v1/categories/{id}` | category list / detail |
| GET | `/api/v1/games`, `/api/v1/games/{id}` | game catalogue list / detail |
| GET | `/api/v1/games/routers/{nameRouter}` | resolve a game by its router name |
| GET | `/api/v1/leaderboard/{gameId}/{type}` | leaderboard for a game and a board type |
| POST | `/api/v1/matches`, PUT `/api/v1/matches/{matchId}` | create / update a match record |
| POST | `/api/v1/users/data` | save player data |

## Quickstart

```bash
git clone https://github.com/lasttoss/game-center-api.git
cd game-center-api
make up            # mongo + redis + api, then browse to /swagger-ui.html
make logs
make down
```

Ports taken? Copy `.env.example` to `.env` and change `API_PORT` / `MONGO_PORT` / `REDIS_PORT`.

## Configuration

| environment | default | used for |
|---|---|---|
| `SERVER_PORT` | `8080` | http port |
| `SPRING_DATA_MONGO_URI` | `mongodb://localhost:27017/gamecenter` | Mongo connection (`spring.data.mongo.uri`) |
| `SPRING_DATA_REDIS_HOST` / `_PORT` / `_DATABASE` | `localhost` / `6379` / `1` | Redis |
| `JWT_SECRET` | `dev-secret-change-me` | JWT signing key - replace outside a laptop |

## Fixed while preparing this repository

1. **The application could not start from a clean clone.** `application.properties` was committed
   as `.bak`, so Spring Boot failed on the missing datasource properties. It is now a real file
   with environment placeholders.
2. **The compose file was committed as `.bak` and was incomplete** - it defined only the API
   container and expected MongoDB and Redis to already exist somewhere. The stack now starts
   both of them, with healthchecks and a health-gated start order.
3. **A signing key was committed in the `.bak` file.** Both the old value and the hardcoded
   development secret are gone; the key comes from `JWT_SECRET` and the `.bak` files are deleted
   from the repository.

## Notes / limitations

- There are no automated tests yet; the CI job builds, runs `mvn test` (currently empty) and
  builds the image.
- `quarkus-*`-style native compilation is not set up here: this is a plain JVM service.

## License

MIT - see [LICENSE](LICENSE).
