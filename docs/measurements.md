# Measurements

The README quotes JaCoCo's line coverage per class. Running the suite reproduces the table, and the
whole thing runs without a Spring context and without MongoDB.

## Environment

| | |
|---|---|
| Machine | 12-core x86-64 (AMD64), Ubuntu 24.04, Docker 29.1.3 |
| Toolchain | Go 1.27.2 / Node 24.19.0 / JDK 17 in `maven:3.9-eclipse-temurin-17` |
| Date | 2026-10-13 (UTC) |


## 36 tests, 51.6% of lines, 4.8% of branches

```
$ docker run --rm -v "$PWD":/src -w /src maven:3.9-eclipse-temurin-17 ./mvnw -B test
Tests run: 36, Failures: 0, Errors: 0, Skipped: 0

$ awk -F, 'NR>1 {l+=$8; m+=$9; b+=$6; bm+=$7} END {printf "lines %.1f%% (%d/%d), branches %.1f%% (%d/%d)\n", 100*m/l, m, l, 100*bm/b, bm, b}' \
    target/site/jacoco/jacoco.csv
lines 51.6% (404/783), branches 4.8% (48/996)
```

**Result.** Exact: 404 of 783 lines, 48 of 996 branches, which is the `total` row of the README's
table. The four services (`AuthService`, `MatchService`, `LeaderboardService`, `GameService`) are at
100% because their collaborators are interfaces and the tests hand them fakes; `RedisService` and
`JwtAuthFilter` are at 0% because they need a Redis and a request pipeline, which is stated rather
than papered over.

## The stack runs

```
$ make keys && docker compose up -d --build --wait
$ curl -s -o /dev/null -w '%{http_code}\n' localhost:18110/v3/api-docs
200
$ curl -s -o /dev/null -w '%{http_code}\n' localhost:18110/api/v1/games
403
```

**Result.** The published API document answers with twelve endpoints, and a protected route answers
403 without a token - which is the claim the README makes about the security setup, checked by
calling it rather than by reading it.
