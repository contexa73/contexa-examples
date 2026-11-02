# Contexa Examples

Seven Spring Boot applications that show how Contexa is added to an application, from the smallest setup to a legacy
system that keeps its own login. Each module runs on its own.

| Module | Port | Who owns sign-in | What it shows |
|---|---|---|---|
| `contexa-example-quickstart` | 8081 | Contexa | The smallest setup: `@EnableAISecurity` and one `@Protectable` endpoint |
| `contexa-example-protectable` | 8082 | Contexa | The four ways to use `@Protectable` (asynchronous, owner check, synchronous, both) |
| `contexa-example-identity-dsl` | 8083 | Contexa | The authentication DSL: a form flow and a REST (JSON) flow side by side |
| `contexa-example-identity-mfa` | 8087 | Contexa | MFA with your own login, one-time code and passkey pages |
| `contexa-example-identity-mfa-multi` | 8088 | Contexa | Two independent MFA flows under `/admin` and `/user` |
| `contexa-example-identity-oauth2` | 8089 | Contexa | OAuth2 tokens after a form login, called with a Bearer token |
| `contexa-example-legacy-system` | 9090 | The application | A legacy login kept as it is, with Contexa watching `@Protectable` APIs |

The documentation of every example: <https://docs.ctxa.ai/docs/install/examples.html>

## What you need

| | |
|---|---|
| Java | 21 or later |
| PostgreSQL | 15 or later, with the `vector` extension (pgvector) |
| Ollama | with the models `qwen3:8b` (decisions) and `mxbai-embed-large` (past decision records) |

```bash
ollama pull qwen3:8b
ollama pull mxbai-embed-large
```

### The Contexa database

Every example keeps Contexa's data in a dedicated PostgreSQL database. By default it is
`jdbc:postgresql://localhost:5432/contexa` with the user `contexa` and the password `contexa`:

```sql
CREATE USER contexa WITH PASSWORD 'contexa';
CREATE DATABASE contexa OWNER contexa;
\c contexa
CREATE EXTENSION IF NOT EXISTS vector;
```

To use another database, set `CONTEXA_DB_URL`, `CONTEXA_DB_USERNAME` and `CONTEXA_DB_PASSWORD`. On the first start
Contexa creates its tables and default data there. Give each example its own database if you run several of them.

### The Contexa starter

The modules depend on `ai.ctxa:spring-boot-starter-contexa:0.1.0`, resolved from your local Maven repository first
and then Maven Central. If your build cannot find it, publish it from the
[Contexa repository](https://github.com/contexa-security/contexa):

```bash
./gradlew publishToMavenLocal
```

## Running an example

```bash
./gradlew :contexa-example-quickstart:bootRun
```

Replace the module name with any of the seven. The Ollama address defaults to `http://localhost:11434`; set
`OLLAMA_BASE_URL` to use another one. A local model answers slower than a hosted one, so the examples give the
Layer 2 analysis 120 seconds (`contexa.security.tiered.layer2.timeout-ms`, 30 seconds by default).

### Local values only

So that the examples start as they are, their `application.yml` carries values that must never reach a deployment:

| Value | In the examples | For anything beyond a local run |
|---|---|---|
| Sample accounts | `admin`, `manager`, `developer`, `user`, `finance` with the password `1111` (`contexa.iam.seed.accounts.enabled: true`) | Leave the setting off |
| Evidence signing key | A fixed example key | Set `CONTEXA_EVIDENCE_KEY` (Base64, 32 bytes or more) |
| Token signing keys | `example-jwk.p12` in the resources | Set `CONTEXA_JWK_KEY_STORE` and `CONTEXA_JWK_KEY_STORE_PASSWORD` |
| OAuth2 client secret (identity-oauth2) | `example-local-client-secret` | Set `CONTEXA_OAUTH2_CLIENT_SECRET` |

## Trying each example

Sign in with `admin` / `1111` unless noted.

- **quickstart** — open <http://localhost:8081/test/quickstart>, then `GET /api/hello`. The decision is recorded in
  SHADOW mode: Contexa decides, records and does not block.
- **protectable** — open <http://localhost:8082/test/protectable>. `GET /api/orders` and
  `GET /api/orders/customer/{id}` are decided after the response; `POST /api/orders` and `DELETE /api/orders/{id}`
  wait for the decision.
- **identity-dsl** — the form flow at <http://localhost:8083/login>, or the REST flow:
  `POST /api/login` with `{"username":"admin","password":"1111"}`, then `GET /api/profile`.
- **identity-mfa** — <http://localhost:8087/customLogin>; after the password, the one-time code page follows.
- **identity-mfa-multi** — <http://localhost:8088/>: administrators sign in at `/admin/mfa/login` (passkey, or a
  one-time code before a passkey is registered), users at `/user/mfa/login` (one-time code).
- **identity-oauth2** — <http://localhost:8089/login>; after sign-in the token test page `/test/token` calls
  `/api/token-test/*` with the access token. The keys are published at `/oauth2/jwks`.
- **legacy-system** — <http://localhost:9090/legacy/login> with the legacy accounts (`admin` / `admin123`,
  `kim_manager` / `manager123`, `park_dev` / `dev123`, `lee_analyst` / `analyst123`). `GET /api/customers` is decided
  after the response and `GET /api/customers/export` waits for the decision; both are `@Protectable`.

## Build

```bash
./gradlew build
```
