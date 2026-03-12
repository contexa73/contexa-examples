# Contexa Examples

Contexa AI Native Zero Trust Security Platform의 실전 예제 모음.
각 모듈은 독립적인 Spring Boot 애플리케이션으로, 가장 단순한 설정부터 고급 구성까지 단계적으로 학습할 수 있다.

## Prerequisites

| Requirement | Version |
|-------------|---------|
| Java | 21+ |
| PostgreSQL | 15+ (pgvector extension) |
| Ollama | latest |
| Gradle | 8.x (Wrapper included) |

### Database Setup

```sql
CREATE DATABASE contexa;
\c contexa
CREATE EXTENSION IF NOT EXISTS vector;
```

### Ollama Models

```bash
ollama pull qwen2.5:7b
ollama pull mxbai-embed-large
```

### Dependency

All modules depend on `spring-boot-starter-contexa` from Maven Local:

```
implementation 'io.contexa:spring-boot-starter-contexa:0.1.0'
```

---

## Modules

### Get Started

| Module | Port | Description |
|--------|------|-------------|
| `contexa-example-quickstart` | 8081 | Minimal setup: `@EnableAISecurity` + YAML |
| `contexa-example-protectable` | 8082 | `@Protectable` method-level AI security |
| `contexa-example-identity-dsl` | 8083 | Identity DSL: custom auth flows |
| `contexa-example-shadow-enforce` | 8084 | Shadow/Enforce mode transition |

### Identity

| Module | Port | Description |
|--------|------|-------------|
| `contexa-example-identity-rest` | 8085 | REST API authentication (JSON, SPA/Mobile) |
| `contexa-example-identity-ott` | 8086 | One-Time Token / Magic Link |
| `contexa-example-identity-mfa` | 8087 | Advanced MFA with custom pages |
| `contexa-example-identity-asep` | 8088 | ASEP security exception annotations |
| `contexa-example-identity-oauth2` | 8089 | OAuth2 JWT stateless authentication |

### IAM *(coming soon)*

### AI Engine *(coming soon)*

---

## Running Examples

Each module runs independently:

```bash
# Quickstart (port 8081)
./gradlew :contexa-example-quickstart:bootRun

# Protectable (port 8082)
./gradlew :contexa-example-protectable:bootRun

# Identity DSL (port 8083)
./gradlew :contexa-example-identity-dsl:bootRun

# Shadow Mode (port 8084)
./gradlew :contexa-example-shadow-enforce:bootRun --args='--spring.profiles.active=shadow'

# Enforce Mode (port 8084)
./gradlew :contexa-example-shadow-enforce:bootRun --args='--spring.profiles.active=enforce'

# Identity REST (port 8085)
./gradlew :contexa-example-identity-rest:bootRun

# Identity OTT (port 8086)
./gradlew :contexa-example-identity-ott:bootRun

# Identity MFA (port 8087)
./gradlew :contexa-example-identity-mfa:bootRun

# Identity ASEP (port 8088)
./gradlew :contexa-example-identity-asep:bootRun

# Identity OAuth2 (port 8089)
./gradlew :contexa-example-identity-oauth2:bootRun
```

---

## Common Configuration

All modules share the following base configuration:

```yaml
contexa:
  infrastructure:
    mode: standalone

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/contexa
    username: ${DB_USERNAME:admin}
    password: ${DB_PASSWORD:1111}
    driver-class-name: org.postgresql.Driver
  jpa:
    database: POSTGRESQL
    hibernate:
      ddl-auto: update
  ai:
    chat:
      model:
        priority: ollama
    ollama:
      base-url: http://127.0.0.1:11434
      chat:
        options:
          model: qwen2.5:7b
          keep-alive: "24h"
      embedding:
        enabled: true
        model: mxbai-embed-large
    vectorstore:
      pgvector:
        table-name: vector_store
        index-type: HNSW
        distance-type: COSINE_DISTANCE
        dimensions: 1024
        initialize-schema: true
```

Environment variables `DB_USERNAME` and `DB_PASSWORD` can override database credentials.

---

## Build

```bash
# Compile all modules
./gradlew compileJava

# Build all modules
./gradlew build
```
