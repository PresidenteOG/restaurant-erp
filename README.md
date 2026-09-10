# Restaurantus ERP

![Java](https://img.shields.io/badge/Java-25-007396?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?style=flat&logo=springboot&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=flat&logo=thymeleaf&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=mysql&logoColor=white)

A back-office ERP for running a restaurant: the menu and its ingredients, table layout,
reservations, the order-to-kitchen flow, cash reconciliation with PDF invoices, staff incidents,
and an internal chat that also bridges to Telegram. Server-rendered with Thymeleaf, one Spring
Boot app.

Built for a DAM (software development) course module — a team project covering Spring Security,
multi-database access, WebSockets and a third-party bot integration.

## What it covers

| Area | What's in it |
|---|---|
| **Carta** | Dishes, drinks and desserts; ingredients, lots (`Lote`) and stock requests; menus that bundle dishes. |
| **Sala** | Table layout by location (`UbicacionMesa`), table state, reservations. |
| **Pedidos** | Take an order, send it to the kitchen, track it through `EstadoPedido`, close it out. |
| **Cocina** | A live pending-orders board for kitchen staff. |
| **Finanzas** | Cash-drawer reconciliation (`ArqueoCaja`), consumption tracking, PDF invoices (OpenPDF). |
| **Incidencias** | Staff incident reports with a resolution workflow. |
| **Usuarios** | Roles, login-attempt lockout, password reset, an activity log per client. |
| **Chat** | Internal staff chat over WebSocket/STOMP, messages stored AES-encrypted, mirrored to a Telegram bot. |
| **i18n** | UI strings live in MongoDB; the language switches at runtime. |

## Two databases

- **MySQL** — everything transactional (JPA, wired manually in `MultiDbConfig` so a second
  datasource can coexist).
- **MongoDB** — only the translation catalogue (`dao/mongo`), read by a custom
  `MongoMessageSource`.

## Run it locally

Needs JDK 25, a MySQL database and a MongoDB instance. All configuration is environment
variables — there are no credentials in the repo:

```bash
export DB_URL="jdbc:mysql://localhost:3306/restaurantus?useSSL=false&allowPublicKeyRetrieval=true"
export DB_USERNAME=root DB_PASSWORD=yourpassword
export MONGODB_URI="mongodb://localhost:27017/restaurantus"
export RESTAURANTUS_CHAT_KEY="0123456789abcdef"   # exactly 16 chars — AES-128 key for chat
export TELEGRAM_TOKEN_ENV="123:abc"               # a BotFather token; the Telegram features need it

./mvnw spring-boot:run
```

Then open `http://localhost:8080`. Hibernate creates the schema (`ddl-auto` default). The
Telegram link-account gate (`TelegramCheckInterceptor`) redirects staff to `/telegram/vincular`
until they connect a Telegram account — see the notes below to loosen that for a demo.

## Notes for a public demo

The full app expects MySQL + MongoDB + a Telegram bot. To run it as a lightweight demo:

- swap MySQL for the bundled H2 (the `h2` dependency is already on the classpath),
- point `MONGODB_URI` at a free MongoDB Atlas tier, or stub `MongoMessageSource`,
- disable `TelegramCheckInterceptor` so the Telegram gate doesn't block every page.

## Architecture

See [ARCHITECTURE.md](./ARCHITECTURE.md) for the layering, the multi-DB wiring and the chat path.

## Contributors

A group project for the DAM course. Handles are GitLab, where the coursework was hosted.

- **Ismael Rosillo** (@rosillodev) — lead
- **Daniel Adanegbe** (@dadanegbe)
- **Zakaria Elmtiouy** (@elmitouy.zakaria)
- **Jack** (@jacka)

## License

PolyForm Noncommercial 1.0.0 ([LICENSE](./LICENSE)). Personal, non-commercial use only.
