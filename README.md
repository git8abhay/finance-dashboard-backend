# 💹 Finance Dashboard — Backend API

A production-grade, containerized REST API for processing financial transactions, enforcing hierarchical role-based access control, and serving real-time analytics. Built with security-first principles on **Java 21 & Spring Boot 3.2+**.

---

## 📑 Table of Contents

- [Project Overview](#-project-overview)
- [Architecture at a Glance](#-architecture-at-a-glance)
- [Tech Stack & Rationale](#-tech-stack--rationale)
- [Core Features](#-core-features)
- [Assumptions & Technical Trade-offs](#-assumptions--technical-trade-offs)
- [API Endpoint Reference](#-api-endpoint-reference)
- [Getting Started (Docker)](#-getting-started-docker)
- [Test Credentials](#-test-credentials)
- [Project Structure](#-project-structure)

---

## 🧭 Project Overview

The Finance Dashboard Backend API is a secure, stateless REST service engineered to be the backbone of an internal financial management platform. It handles the full lifecycle of financial transaction data — from ingestion and mutation (with a preserved audit trail) to role-gated retrieval and aggregated analytics.

This is **not** a generic CRUD application. Every architectural decision — from token validation strategy to caching scope to numeric type selection — was made deliberately to reflect the constraints and accountability requirements of a real-world financial system.

---

## 🏛️ Architecture at a Glance

```text
┌─────────────────────────────────────────────────────┐
│                   Client (Frontend / Swagger)        │
└────────────────────────┬────────────────────────────┘
                         │ HTTPS + Bearer JWT
                         ▼
┌─────────────────────────────────────────────────────┐
│              Spring Security Filter Chain            │
│   JwtFilter → AuthenticationManager → RBAC           │
│          (@PreAuthorize on each endpoint)            │
└────────────────────────┬────────────────────────────┘
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
   AuthController  TransactionController  DashboardController
          │              │              │
          ▼              ▼              ▼
   JwtUtil      TransactionService  DashboardService
          │              │              │
          └──────────────┼──────────────┘
                         │
              Spring Data JPA / Hibernate
                         │
                         ▼
              ┌─────────────────────┐
              │   PostgreSQL 15      │  ← Dockerized
              └─────────────────────┘
```

---

## ⚙️ Tech Stack & Rationale

| Technology | Version | Why This Choice |
|---|---|---|
| **Java** | 21 (LTS) | Virtual threads, modern records, sealed classes, long-term support. |
| **Spring Boot** | 3.2+ | Native Jakarta EE namespace, improved auto-configuration. |
| **PostgreSQL** | 15 | ACID compliance is non-negotiable for financial data; superior JSON/aggregate support. |
| **Spring Security** | 6.x | Method-level security via `@PreAuthorize`, modern `SecurityFilterChain` DSL. |
| **JWT (JJWT)** | Latest | Stateless auth — no server-side session storage, horizontally scalable by default. |
| **Spring Data JPA** | — | Strongly-typed queries, lazy loading control, and `@Query` for complex JPQL/Pagination. |
| **Spring Cache** | — | `@Cacheable` for targeted caching of expensive aggregation queries without memory risk. |
| **Docker + Compose** | — | One-command reproducibility; eliminates "works on my machine" failures. |
| **JUnit 5 + Mockito** | — | Industry-standard; `@ExtendWith(MockitoExtension)` for isolated unit tests. |
| **Lombok & SLF4J** | — | Eliminates boilerplate; SLF4J provides a production-ready logging facade for audit trails. |

---

## 🚀 Core Features

### 🔐 Hierarchical Role-Based Access Control (RBAC)

Three roles form a strict access hierarchy enforced at the method level:

- **`ROLE_ADMIN`**: Full read/write + soft-delete + global filtering + analytics + user management.
- **`ROLE_ANALYST`**: Global read + create/update transactions + global filtering + analytics.
- **`ROLE_VIEWER`**: Read-only access to their own transactions (`/me`) and the dashboard summary.

### 🛡️ Stateless JWT Authentication

Authentication is fully stateless. The server holds **zero session state**. On every request:

1. `JwtFilter` extracts the `Authorization: Bearer <token>` header.
2. The token is validated (signature, expiry, username).
3. A `UsernamePasswordAuthenticationToken` is injected into the `SecurityContextHolder`.

### 🔍 Dynamic Filtering & Pagination

`GET /api/transactions/filter` accepts dynamic combinations of `type`, `category`, `startDate`, and `endDate`.

All filtering logic lives in a single custom `@Query` method in the repository. The response is a standardized `Page<TransactionResponseDTO>`, enabling seamless frontend pagination without crashing server memory on massive datasets.

### 📊 Dashboard Analytics

`GET /api/dashboard/summary` returns:

- **Total Income**
- **Total Expenses**
- **Net Balance**
- **Spending by Category** (Extracted via custom `GROUP BY` SQL projections).

---

## 🧠 Assumptions & Technical Trade-offs

### 1. 🚫 No Public Registration (Admin-Provisioned Only)

Registration was intentionally omitted. As an internal financial tool, allowing open user registration would introduce an uncontrolled attack surface. Instead, a dedicated **User Management API** (`/api/users`) is exposed strictly for `ADMIN` users to provision and manage Analyst and Viewer accounts.

### 2. 🌱 Automated Data Seeding via `CommandLineRunner`

Data is seeded at startup via `DataSeeder.java`, not via fragile `.sql` scripts. It checks whether the database is empty before safely inserting test users. Passwords are BCrypt-hashed immediately.

### 3. 🗃️ Soft Deletes Over Hard Deletes

Records are never physically deleted. An `isDeleted = true` flag is toggled instead. In financial systems, audit trails are non-negotiable. Soft-deleted records are automatically excluded from all standard queries.

### 4. 🎯 Selective Caching Strategy

Caching (`@Cacheable`) is strictly applied to `/dashboard/summary` because `SUM()` and `GROUP BY` operations are computationally heavy. However, caching was **intentionally omitted** from `/filter` and `/all`. Because filters have infinite combinations and utilize fast DB-level `LIMIT/OFFSET` pagination, caching them would cause unbounded RAM bloat (OOM risks). The cache is intelligently flushed (`@CacheEvict`) upon any transaction mutation.

### 5. 💰 `BigDecimal` for All Monetary Values

`Double` and `Float` are prohibited for currency representation to avoid IEEE 754 floating-point rounding errors. `BigDecimal` is mapped to PostgreSQL's `NUMERIC` type, preserving exact precision end-to-end.

### 6. 📉 Negative Net Balance by Design

The system assumes an "expense tracker" model (where users might log credit card debt or log an expense before their income), therefore the **Net Balance is explicitly permitted to drop below zero**.

---

## 📡 API Endpoint Reference

### Auth

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | Public | Authenticates user and returns JWT + Role. |

### User Management (Admin Operations)

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `GET` | `/api/users` | ADMIN | View all registered users. |
| `POST` | `/api/users` | ADMIN | Create a new user (Analyst/Viewer). |
| `PATCH` | `/api/users/{id}/status` | ADMIN | Toggle user active/inactive status. |

### Transactions

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `GET` | `/api/transactions/all` | ADMIN, ANALYST | Paginated list of all system transactions. |
| `GET` | `/api/transactions/me` | Authenticated | Paginated list of the logged-in user's transactions. |
| `GET` | `/api/transactions/filter` | ADMIN, ANALYST | Dynamic filtered & paginated list. |
| `POST` | `/api/transactions` | ADMIN, ANALYST | Create new transaction. |
| `PUT` | `/api/transactions/{id}` | ADMIN, ANALYST | Update existing transaction. |
| `DELETE` | `/api/transactions/{id}` | ADMIN Only | Soft-delete transaction. |

### Dashboard

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `GET` | `/api/dashboard/summary` | VIEWER, ANALYST, ADMIN | Cached totals + category trend map. |

---

## 🐳 Getting Started (Docker)

**Prerequisites:** Docker Desktop installed. No local Java, Maven, or PostgreSQL installation is required.

**One-Command Startup:**

```bash
# Clone the repository
git clone https://github.com/git8abhay/finance-dashboard-backend.git
cd finance-dashboard-backend

# Build and start both services (PostgreSQL + Spring Boot Backend)
docker-compose up --build
```

**Interactive API Docs (Swagger):**

Navigate to: 👉 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🔑 Test Credentials

These users are automatically seeded on first startup.

| Role | Email | Password | Access Level |
|---|---|---|---|
| **ADMIN** | `admin@test.com` | `admin123` | Full access — read, write, filter, delete, manage users. |
| **VIEWER** | `viewer@test.com` | `viewer123` | Read-only — `/me` and `/dashboard/summary` only. |

> To authenticate in Swagger, use the `/login` endpoint, copy the token string, and paste it into the green **"Authorize"** padlock at the top of the page.

---

## 📁 Project Structure

```plaintext
src/
├── main/
│   ├── java/com/abhay/finance_backend/
│   │   ├── config/
│   │   │   ├── DataSeeder.java                  # CommandLineRunner for test data
│   │   │   ├── SecurityConfig.java              # SecurityFilterChain, Global CORS
│   │   │   └── SwaggerConfig.java               # OpenAPI 3 definitions
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── DashboardController.java
│   │   │   ├── TransactionController.java
│   │   │   └── UserController.java              # Admin-only User Management
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── LoginRequestDTO.java
│   │   │   │   └── TransactionRequestDTO.java
│   │   │   └── response/
│   │   │       ├── AuthResponseDTO.java
│   │   │       ├── DashboardSummaryDTO.java
│   │   │       └── TransactionResponseDTO.java
│   │   ├── entity/
│   │   │   ├── BaseEntity.java                  # @MappedSuperclass with Timestamps
│   │   │   ├── Transaction.java
│   │   │   └── User.java
│   │   ├── enums/
│   │   │   ├── Role.java
│   │   │   └── TransactionType.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java      # @ControllerAdvice structured errors
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── UnauthorizedAccessException.java
│   │   ├── repository/
│   │   │   ├── TransactionRepository.java       # @Query aggregations & filtering
│   │   │   └── UserRepository.java
│   │   ├── security/
│   │   │   ├── CustomUserDetails.java
│   │   │   ├── CustomUserDetailsService.java
│   │   │   ├── JwtFilter.java                   # OncePerRequestFilter
│   │   │   └── JwtUtil.java
│   │   ├── service/
│   │   │   ├── DashboardService.java
│   │   │   ├── TransactionService.java
│   │   │   └── impl/
│   │   │       ├── DashboardServiceImpl.java    # @Cacheable analytics
│   │   │       └── TransactionServiceImpl.java  # @Slf4j audit logs, @Transactional
│   │   └── FinanceBackendApplication.java
│   └── resources/
│       └── application.yml
└── test/java/com/abhay/finance_backend/
    ├── FinanceBackendApplicationTests.java
    └── service/impl/
        ├── DashboardServiceImplTest.java
        └── TransactionServiceImplTest.java
```

---

> **Built with precision. Engineered for production.**
>
> *Developed by Abhay Chauhan*
