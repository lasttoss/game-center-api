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

## The request path as a picture

```mermaid
%% Source for docs/diagrams/layered-request-path.html
%% One request through a layered Spring service, and the two datastores that do different jobs.
flowchart LR
  C["game client"] -->|"Authorization: Bearer"| J["JwtAuthFilter<br/>stateless"]
  J --> CT["controllers<br/>thin, HTTP only"]
  CT --> S["services"]
  S --> R["repositories<br/>Spring Data Mongo"]
  R --> M[("MongoDB<br/>catalogue · leaderboards<br/>player data")]
  S <--> RD[("Redis<br/>caches + pub/sub")]
  CT -.->|"dto + mappers"| E["document shape<br/>never leaves"]
  CT -.->|"ApiErrorEnum + Response"| ER["one error shape<br/>for every endpoint"]
  classDef gate fill:#eef5ef,stroke:#1a6b3c,stroke-width:2px;
  class J gate;
```

One request, drawn so the layer boundaries are visible: the filter checks a stateless token, the controller
stays on transport, the service holds the decisions, and the repository is the only thing that knows Mongo.
Alongside them are the two cross-cutting pieces the README calls out — `dto`/`mappers`, so the stored
document shape never reaches the client, and `ApiErrorEnum` with one response envelope, so every endpoint
fails in the same shape.

The two datastores are drawn with the jobs they actually have: Mongo holds what has to survive a restart and
be queried by shape, Redis holds what is safe to lose — caches and the pub/sub that carries a change to the
instances that need it.

`docs/diagrams/layered-request-path.mmd` is the Mermaid source; `make diagram` exports a PNG if a browser is
present.

## The chart

`charts/gamecenter-api/` deploys the API with what a live service needs on Kubernetes: a rolling update that
does not take a replica out of the Service (`maxUnavailable: 0`), a PodDisruptionBudget that keeps one serving
through a disruption, an HPA, a NetworkPolicy whose egress names the datastores it uses instead of allowing
everything, no service-account token, and a read-only root filesystem with an `emptyDir` for `/tmp`.

```bash
make chart     # helm lint --strict + helm template
```

## Coverage

Measured with `./mvnw -B test` plus the JaCoCo plugin, twice: once as CI runs it (unit only) and once with the
integration-tagged test selected and MongoDB up.

| run | lines | branches |
|---|---|---|
| unit suite (as CI runs it) | 28 / 783 = **3.6%** | 5 / 996 = **0.5%** |
| `-Dgroups=integration` with the stack up | 28 / 783 = **3.6%** | 5 / 996 = **0.5%** |

The second row is the interesting one: it is identical. The integration test boots the whole application context
against a real MongoDB, which is worth having and is not coverage - it proves the wiring resolves, not that the
logic is exercised. So 3.6% is the whole truth about this suite rather than an artefact of a test being held back,
and it was measured both ways rather than assumed from the tag.

By package, from the same report: `utils` 34.8%, `models` 3.4%, and `services` (259 lines), `controllers`,
`repositories` and `redis` at zero.

The JaCoCo plugin is committed so both numbers can be reproduced rather than taken on trust.
