# Juleaften

Backend for a Christmas Eve (Danish: *Juleaften*) event registration service. It is built for a heart-warming gathering for Ukrainians at FriKirken: guests can sign up for the event, bring companions, and sit on a waitlist when the venue is full.

This repository is the **REST API**. A separate Vue 3 + Vite SPA (default `http://localhost:5173`) is the public client.

## What it does

- Registers guests for named events (a default **Juleaften** event with capacity 50 is created on startup).
- Tracks companionship type (single, couple, family, friends, mixed) and companion count.
- Fills seats from registered visits; extra sign-ups go on a **waiting** list when capacity is reached.
- Creates local accounts on first registration, emails a generated password plus a 24-hour verification link, and activates the account after the guest confirms email.
- Lets guests sign in with email/password or Google (OIDC).
- Issues an HTTP-only JWT cookie (`ACCESS_TOKEN`) for subsequent API calls.
- Lets administrators create new events.

## How it works

```
Vue SPA (Vite :5173)  ──CORS + cookies──►  Spring Boot API
                                              │
                     ┌────────────────────────┼────────────────────────┐
                     │                        │                        │
                     ▼                        ▼                        ▼
              MySQL (JPA)              Gmail SMTP               Google OIDC
```

### Guest registration

1. The client posts to `POST /visits/register` with name, email, companionship, companion count, optional note, and event name. Authentication is **not** required.
2. If the guest is already logged in, the visit is attached to that profile. Otherwise the API looks up the email or creates a local `User` + `UserProfile`.
3. New local users get a generated password (BCrypt-hashed), `verified=false` / `active=false`, and a hashed email-verification token (24h).
4. Thymeleaf renders `email_templates/email_verify.html`. Spring Mail sends it via Gmail SMTP. The link points at `{CLIENT_URL_BASE}/verify-email?token=…`.
5. `POST /profiles/verify` consumes the token, activates the account, and sets the JWT cookie.
6. Visit status is `REGISTERED` while the event still has seats, otherwise `WAITING`. Seat usage is registered visits plus companion counts.

A guest can only have **one visit per event**. Duplicate registrations are ignored.

### Authentication

| Path | Behaviour |
|---|---|
| `POST /auth` | Email/password login via Spring Security `AuthenticationManager`. Sets `ACCESS_TOKEN`. |
| `GET /oauth2/authorization/google` | Google OIDC login. On success the user is provisioned (`AuthenticationMethod.GOOGLE`, already verified), JWT cookie is set, then redirect to the SPA. |
| `GET /auth/me` | Current user (requires valid cookie). |
| `POST /auth/logout` | Clears the cookie and invalidates the HTTP session. |

JWT is HMAC-signed (JJWT). The filter reads the cookie, validates the token, and loads `UserProfile` as `UserDetails`. CSRF is disabled; CORS allows the configured SPA origin with credentials.

Two filter chains:

1. OAuth2 paths (`/oauth2/**`, `/login/oauth2/**`) — session allowed for the Google handshake.
2. Everything else — JWT cookie filter, form/basic/OAuth login disabled on this chain.

### Roles

Profiles start with role `USER`. Creating events (`POST /events`) requires `ADMIN`.

## Domain

| Entity | Role |
|---|---|
| **Event** | Named gathering with capacity. Unique name. |
| **User** | Person (first name, last name, unique email). |
| **UserProfile** | Credentials, roles, verification, signup method (`LOCAL` / `GOOGLE`). Implements Spring `UserDetails`. |
| **Visit** | One registration of a profile for an event, plus companionship, companion count, note, and status. |

Visit statuses: `REGISTERED`, `WAITING`, `CANCELLED`, `INACTIVE`.

## HTTP API

Responses are wrapped in `ApiResponse` (`message` + optional payload). Interactive docs: [Swagger UI](http://localhost:8080/swagger-ui.html) (`/v3/api-docs`).

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/visits/register` | public | Register a visit |
| `GET` | `/visits/{eventName}` | cookie | Current user’s visit for an event |
| `GET` | `/events/statistics/{eventName}` | public | Capacity, filled seats, full flag |
| `GET` | `/events/user/detailed?eventName=` | cookie | Profile + statistics + visit |
| `POST` | `/events` | `ADMIN` | Create event |
| `POST` | `/profiles` | public | Create local profile |
| `POST` | `/profiles/verify` | public | Confirm email, set cookie |
| `PUT` | `/profiles/password` | (body) | Change password |
| `POST` | `/auth` | public | Login |
| `GET` | `/auth/me` | cookie | Session user |
| `POST` | `/auth/logout` | public | Logout |

## Software stack

| Layer | Choice |
|---|---|
| Language | Java 25 |
| Runtime / framework | Spring Boot 4.1.2 (Web MVC) |
| Security | Spring Security 6, OAuth2/OIDC client (Google), BCrypt |
| Tokens | JJWT 0.13 (HMAC) in an HTTP-only cookie |
| Persistence | Spring Data JPA, Hibernate, MySQL (`mysql-connector-j`) |
| Validation | Jakarta Bean Validation |
| Email | Spring Mail (SMTP) + Thymeleaf HTML templates |
| API docs | springdoc-openapi 3.2 (Swagger UI) |
| Boilerplate | Lombok |
| Build | Apache Maven (wrapper in `.mvn/`) |
| Client (separate repo) | Vue 3, Vite, Axios, vue-i18n |

Layout is a typical Spring layered app: controllers → services → JPA repositories, with JWT/OAuth under `security/` and DTOs under `dto/`.

## Prerequisites

- JDK 25
- Maven 3.9+ (or `./mvnw` / `mvnw.cmd`)
- MySQL 8 with a database matching `MYSQL_DB_URL`
- Gmail account with an **app password** for SMTP
- Google Cloud OAuth 2.0 client (Web) with redirect URI  
  `http://localhost:8080/login/oauth2/code/google` (adjust host/port in production)

## Configuration

Secrets belong in the environment, not in Git. Production settings are placeholders in `src/main/resources/application.yml`.

| Variable | Purpose | Default |
|---|---|---|
| `GOOGLE_USERNAME` | SMTP user and `email.from` | — |
| `GOOGLE_SECRET` | Gmail app password | — |
| `MYSQL_DB_URL` | JDBC URL | — |
| `MYSQL_DB_USERNAME` | DB user | — |
| `MYSQL_DB_PASSWORD` | DB password | — |
| `GOOGLE_CLIENT_ID` | OAuth client id | — |
| `GOOGLE_CLIENT_SECRET` | OAuth client secret | — |
| `JWT_SECRET` | HMAC key (must be long enough for HS256) | — |
| `DEFAULT_PASSWORD_SIZE` | Length of generated passwords | `16` |
| `EXPIRATION_MILLIS` | JWT / cookie lifetime | `86400000` (24h) |
| `HTTP_SECURE` | `Secure` flag on cookies | `false` |
| `CLIENT_URL_BASE` | SPA origin (CORS, email links, OAuth redirect) | `http://localhost:5173` |

Hibernate `ddl-auto` is `update`. The first run creates tables and seeds the default Juleaften event.

Local development can use Spring profile `dev` (`src/main/resources/application-dev.yml`). That file is listed in `.gitignore` so credentials stay off the remote.

## Run

```bash
# from this directory
./mvnw spring-boot:run
# Windows:
mvnw.cmd spring-boot:run
```

Optional profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API listens on port **8080**. Point the Vue app at this origin and keep `CLIENT_URL_BASE` aligned with the SPA.

Tests:

```bash
./mvnw test
```

## Related client

The public UI lives in the sibling frontend project (`juleaften-front`): Vue 3 pages for welcome, registration, login, and email verification, talking to this API with Axios and cookies.
