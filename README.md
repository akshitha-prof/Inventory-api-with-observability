# Inventory & Lending System API

A production-ready REST API built with **Spring Boot 3.2** for managing inventory items and lending transactions, deployed on **AWS Elastic Beanstalk** with **RDS PostgreSQL**, instrumented with **AWS X-Ray** distributed tracing and **CloudWatch** custom metrics.

## Architecture

```
┌─────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Client     │───▶│  Controller  │───▶│   Service    │───▶│  Repository  │
│  (Swagger)   │    │  (REST API)  │    │  (Business)  │    │  (JPA/DB)    │
└─────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
       │                  │                   │                     │
       │            JWT Auth Filter     MetricsAspect         PostgreSQL (RDS)
       │                  │                   │
       │            X-Ray Servlet       BusinessMetrics ──▶ CloudWatch
       │              Filter                  │               Dashboards
       │                  │                   │               & Alarms
       │                  ▼                   ▼
       │           ┌──────────────────────────────┐
       │           │     AWS X-Ray Console         │
       │           │  (Distributed Trace Viewer)   │
       │           └──────────────────────────────┘
```

## Tech Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Data JPA** + PostgreSQL (AWS RDS)
- **Spring Security** + JWT Authentication
- **SpringDoc OpenAPI** (Swagger UI)
- **AWS X-Ray** distributed tracing (request-level + SQL tracing)
- **Micrometer** + **CloudWatch** custom business metrics
- **Spring AOP** auto-instrumented service layer metrics
- **JUnit 5** + **Mockito** + **Testcontainers**
- **JaCoCo** code coverage (85%+ target)
- **GitHub Actions** CI/CD → AWS Elastic Beanstalk

## Key Features

### Core Business Logic
- Full CRUD on Items, Categories, Users, Transactions
- Business rules: max borrows limit (5 active), availability checks, category deletion protection
- Pagination + sorting on all list endpoints
- Role-based access (USER vs ADMIN)
- Input validation with detailed error responses
- Auto-generated API documentation

### Observability (AWS-native)
- **X-Ray Tracing**: every HTTP request traced end-to-end including SQL queries
- **Custom Business Metrics**: borrows/returns counters, active loan gauge, borrow latency (p50/p95/p99), user registration rate
- **AOP Instrumentation**: service methods auto-timed and traced — zero changes to business code
- **CloudWatch Dashboard**: 6-widget operational dashboard (JSON template included)
- **CloudWatch Alarms**: 5xx error rate and high latency alarms with SNS notifications

## Project Structure

```
src/main/java/com/inventory/
├── config/            # Security config, JWT filter, JWT utility
├── controller/        # REST endpoints (Auth, Category, Item, Transaction)
├── dto/               # Request/response DTOs with validation
├── exception/         # Global exception handler, custom exceptions
├── model/             # JPA entities (Item, Category, AppUser, Transaction)
├── observability/
│   ├── config/        # XRayConfig — servlet filter for request tracing
│   ├── metrics/       # BusinessMetrics — custom counters/gauges/timers
│   │                  # MetricsAspect — AOP auto-instrumentation
│   └── tracing/       # XRayTracingInterceptor — subsegments for service/repo
├── repository/        # Spring Data JPA repositories
└── service/           # Business logic layer
cloudwatch/
├── dashboard.json     # Import into CloudWatch for operational dashboard
└── alarm.yaml         # CloudFormation template for alerts + SNS topic
```

## Running Locally

```bash
# Prerequisites: Java 17, Maven, PostgreSQL running locally
cp src/main/resources/application.yml src/main/resources/application-local.yml
# Edit application-local.yml with your local DB credentials

mvn spring-boot:run -Dspring-boot.run.profiles=local
```

API docs available at: `http://localhost:8080/swagger-ui.html`

Note: X-Ray and CloudWatch metrics are only active under the `aws` profile. Locally, Actuator still exposes all Micrometer metrics at `/actuator/metrics`.

## Running Tests

```bash
mvn clean verify -Dspring.profiles.active=test
# Coverage report: target/site/jacoco/index.html
```

## AWS Deployment

1. Create RDS PostgreSQL instance (free tier)
2. Create Elastic Beanstalk environment (Java 17 platform)
3. Set environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`
4. Set active profile: `SPRING_PROFILES_ACTIVE=aws`
5. Configure GitHub Secrets for CI/CD
6. Push to `main` — auto-deploys via GitHub Actions

### Setting Up Observability

1. **X-Ray**: Enable X-Ray on the Elastic Beanstalk environment (Configuration → Software → X-Ray daemon)
2. **CloudWatch Dashboard**: Import `cloudwatch/dashboard.json` via CloudWatch Console → Dashboards → Create → JSON tab
3. **CloudWatch Alarms**: Deploy `cloudwatch/alarm.yaml` via CloudFormation — creates 5xx and latency alarms with SNS email notification

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/auth/register | Register user | Public |
| POST | /api/auth/login | Login | Public |
| GET | /api/categories | List categories | Public |
| POST | /api/categories | Create category | Authenticated |
| DELETE | /api/categories/{id} | Delete category | Admin |
| GET | /api/items | List items (paginated) | Public |
| GET | /api/items/search?q= | Search items | Public |
| POST | /api/items | Create item | Authenticated |
| DELETE | /api/items/{id} | Delete item | Admin |
| POST | /api/transactions/borrow | Borrow item | Authenticated |
| POST | /api/transactions/{id}/return | Return item | Authenticated |

## Observability Metrics Emitted

| Metric | Type | Description |
|--------|------|-------------|
| `inventory.borrows.total` | Counter | Total borrow transactions |
| `inventory.returns.total` | Counter | Total return transactions |
| `inventory.loans.active` | Gauge | Currently active loans |
| `inventory.borrow.latency` | Timer | End-to-end borrow processing time |
| `inventory.registrations.total` | Counter | New user registrations |
