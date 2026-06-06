# Autosalon Backend Lab 1

Backend domain model for a private multibrand car dealership.

## Implemented

- Three-layer structure: domain, repository, service, presentation.
- In-memory repositories based on standard Java collections.
- Car catalog with filtering by price, brand, model, body, fuel, engine, gearbox, drive and color.
- Car configurator with required component validation, component compatibility checks and final price calculation.
- In-stock car orders, custom configuration orders and test-drive requests.
- Domain exceptions: `DomainValidationException`, `IncompatibleComponentException`, `EntityNotFoundException`.
- Unit tests with JaCoCo coverage verification above 70%.
- Docker build and run support.

## Local commands

```bash
./gradlew clean test
./gradlew run
./gradlew jacocoTestReport
```

The HTML coverage report is generated at `build/reports/jacoco/test/html/index.html`.

## IntelliJ IDEA

Open `/Users/fateddd/IdeaProjects/autosalon-backend-lab1` as an existing Gradle project.
Use JDK 21 or newer, wait for Gradle sync, then run `AutosalonApplication`.

## Docker

```bash
docker build -t autosalon-backend-lab1 .
docker run --rm autosalon-backend-lab1
```

## Docker Compose / Make

Pretty one-command Docker run:

```bash
make docker-run
```

Build and run through Compose:

```bash
docker compose up --build --abort-on-container-exit --exit-code-from autosalon-app
```

Clean stopped Compose containers:

```bash
make docker-clean
```
