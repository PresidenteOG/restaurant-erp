# Deploying the demo

The live demo runs the app with the `demo` Spring profile, which strips out every
external dependency so it fits on a free hosting tier:

| Normally | In the demo |
|---|---|
| MySQL (primary database) | In-memory H2, schema generated from the entities, re-seeded on every start |
| MongoDB (holds the UI translations) | Not used — the UI falls back to a bundled English string set (`src/main/resources/messages.properties`) |
| Telegram bot (staff alerts + a "link your account" gate on login) | Turned off (`telegram.enabled=false`) |
| Real staff accounts | A seeded restaurant: 5 users, 3 zones, 8 tables, a small menu, an open till and a few live orders |

`src/main/java/cat/copernic/restaurantus_erp/config/DemoDataSeeder.java` builds the
sample data with the JPA entity setters, so it works on any SQL dialect.

## Sign in

| User | Password | Role |
|---|---|---|
| `admin` | `demo1234` | Administrator (everything) |
| `marta.gerente` | `demo1234` | Manager |
| `pau.cambrer` | `demo1234` | Waiter + till |
| `nadia.cuina` | `demo1234` | Head chef |
| `jordi.barra` | `demo1234` | Till |

## Render (Docker blueprint)

1. Push this repo to GitHub.
2. Render dashboard → **New → Blueprint**, point it at the repo. It reads
   `render.yaml`: one free Docker web service, health check on `/login`, and
   `SPRING_PROFILES_ACTIVE=demo`.
3. Deploy. First build takes a few minutes (Maven downloads dependencies).
4. Paste the live URL into the root `README.md` and the profile README row.

The free tier sleeps after ~15 minutes idle; the first request after that takes
30–50 seconds while the container wakes and re-seeds.

## Run it locally without Docker

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
# or
./mvnw package -DskipTests
java -jar target/restaurantus-erp-*.jar --spring.profiles.active=demo
```

Then open <http://localhost:8080/login>. Requires JDK 25.

## Run it locally with Docker

```bash
docker build -t restaurant-erp .
docker run --rm -p 8080:8080 restaurant-erp
```
