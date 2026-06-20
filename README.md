# Franchise Management API

A reactive REST API for managing franchises, their branches, and the products available at each branch. Built with Spring WebFlux and Clean Architecture principles.

## What it does

The API models a three-level hierarchy:

- **Franchise** — a top-level brand or company
- **Branch** — a physical location that belongs to a franchise
- **Product** — an item with a stock level available at a branch

It lets you create and update entities at each level, and query which product has the highest stock at each branch for a given franchise.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 3 + Spring WebFlux (reactive) |
| Database | PostgreSQL 16 (via R2DBC) |
| Build tool | Gradle |
| Containerisation | Docker / Docker Compose |

## Prerequisites

- Java 25
- Docker and Docker Compose (for the recommended setup)
- PostgreSQL 16 (if running locally without Docker)

## Environment variables

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | Database host |
| `DB_PORT` | `5432` | Database port |
| `DB_NAME` | `franchise_db` | Database name |
| `DB_USER` | `franchise_user` | Database user |
| `DB_PASSWORD` | `franchise_pass` | Database password |

Copy the included `.env` file and adjust values as needed — Docker Compose reads it automatically.

```bash
cp .env .env.local  # optional: keep your own values separate
```

## Running with Docker Compose (recommended)

This starts both the application and a PostgreSQL instance:

```bash
docker compose up --build
```

The API will be available at `http://localhost:8080`.

To stop and remove containers:

```bash
docker compose down
```

To also remove the database volume:

```bash
docker compose down -v
```

## Running locally

1. Make sure a PostgreSQL instance is running and reachable.
2. Export the environment variables (or rely on the defaults):

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=franchise_db
export DB_USER=franchise_user
export DB_PASSWORD=franchise_pass
```

3. Build and run:

```bash
./gradlew bootRun
```

## Running tests

```bash
./gradlew test
```

## Building the JAR

```bash
./gradlew bootJar -x test
```

The output is placed at `applications/app-service/build/libs/`.

## API endpoints

Base path: `/api/v1/franchises`

### Franchises

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/franchises` | Create a franchise |
| `GET` | `/api/v1/franchises/{id}` | Get a franchise by ID |
| `PATCH` | `/api/v1/franchises/{id}` | Update a franchise name |

### Branches

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/franchises/{franchiseId}/branches` | Add a branch to a franchise |
| `GET` | `/api/v1/franchises/{franchiseId}/branches/{id}` | Get a branch by ID |
| `PATCH` | `/api/v1/franchises/{franchiseId}/branches/{id}` | Update a branch name |

### Products

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products` | Add a product to a branch |
| `GET` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{id}` | Get a product by ID |
| `PATCH` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{id}/name` | Update a product name |
| `PATCH` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{id}/stock` | Update a product stock |
| `DELETE` | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{id}` | Remove a product from a branch |

### Reports

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/franchises/{franchiseId}/branches/products/top-stock` | Get the product with the highest stock per branch for a franchise |

### Request bodies

**Create / update franchise or branch:**
```json
{ "name": "My Franchise" }
```

**Create product:**
```json
{ "name": "Coffee", "stock": 150 }
```

**Update product name:**
```json
{ "name": "Espresso" }
```

**Update product stock:**
```json
{ "stock": 200 }
```

## Project structure

The project follows Clean Architecture, organized into Gradle modules:

```
domain/model          — entities and business rules (no framework dependencies)
domain/usecase        — application use cases
infrastructure/
  driven-adapters/    — database adapters (R2DBC repositories)
  entry-points/       — HTTP handlers and router (Spring WebFlux)
applications/         — application wiring and Spring Boot entry point
```

## CI

GitHub Actions runs compile and test on every push and pull request targeting `main`, `develop`, `feature/**`, `release/**`, and `hotfix/**` branches.
