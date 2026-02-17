# Book Service (book-service)

A RESTful microservice for managing a book catalog — supports CRUD operations for books, authors, and title, with Kafka-based event publishing for downstream consumers.

This repository contains a Maven-based Spring Boot application (Java 17) with JPA, Jersey, Thymeleaf, Kafka support, and observability dependencies (Micrometer / Prometheus). The project includes a Dockerfile for building and running the service in a container.

## Architecture

This project was built as a hands-on exploration of Kafka-based 
inter-service communication.

book-service  -->  [Kafka Topic]  -->  pricing-service
(Producer)                             (Consumer)

- book-service publishes an event when a book is created/updated
- pricing-service consumes the event and updates its own database

"See also: pricing-service — the Kafka consumer counterpart to this service." 

## Project metadata (from repository)
- ArtifactId: `book-service`
- GroupId: `com.bookService`
- Version: `0.0.1`
- Java: 17 (see `pom.xml`)
- Spring Boot parent: 3.3.4

## Key files
- `src/main/java/com/learn/bookService/Application.java` - Spring Boot main class
- `pom.xml` - Maven build file
- `Dockerfile` - Multi-stage Docker build (build with Maven, run on Eclipse Temurin 17 JRE)
- `HELP.md` - local notes and links
- `data/` - project-local database files (examples)
- `logs/` - runtime log files

## Prerequisites
- Java 17 JDK (or JRE for running the packaged jar)
- Maven 3.6+ (the Dockerfile uses Maven 3.9.6 image for build)
- Docker (optional, to build and run the container)

Note: This README assumes you're on Windows using `cmd.exe`. Commands shown below use Windows path separators and quoting suitable for cmd.

## Quick start (build & run locally)
1. Build with Maven (skip tests for a faster local package):

```cmd
mvn -DskipTests clean package
```

2. Run the packaged jar (artifact name follows `artifactId-version.jar`):

```cmd
java -jar target\book-service-0.0.1.jar
```

If your artifact name differs (for example, because of build plugins), use a wildcard:

```cmd
java -jar target\*.jar
```

## Run from IDE
- Import the project as a Maven project in your IDE (Eclipse/IntelliJ).
- Run `com.learn.bookService.Application` as a Java application (Spring Boot run configuration).

## API Endpoints
The `BookController` exposes the following HTTP endpoints under the base path `/api/v1/books`.

| Method | Path | Description |
|---|---|---|
| GET | /api/v1/books | List all books |
| GET | /api/v1/books/search?title={title} | Search books by title (query parameter `title`) |
| GET | /api/v1/books/{id} | Retrieve a book by id |
| POST | /api/v1/books | Create a new book (returns 201 Created) |
| PUT | /api/v1/books/{id} | Replace/update an existing book (full payload required) |
| PATCH | /api/v1/books/{id} | Partial update (accepts `BookPatchDTO`) |
| DELETE | /api/v1/books/{id} | Delete a book by id |


## Run with Maven (dev mode)
Start the application directly from Maven (useful in development):

```cmd
mvn spring-boot:run
```

This runs the app with the same JVM used by Maven; to pass JVM args or system properties use `-Dspring-boot.run.jvmArguments="-Xmx512m"` etc.

## Docker
The repository includes a multi-stage Dockerfile that builds the application using Maven and then packages it into an Eclipse Temurin JRE image.

Build the image (run from project root):

```cmd
docker build -t book-service:local .
```

Run the container and map ports (Dockerfile exposes 8081):

```cmd
docker run --rm -p 8081:8081 --name book-service book-service:local
```

Note: The Dockerfile sets EXPOSE 8081 and copies the assembled jar to `app.jar`. When running the jar directly (not in Docker), the active port is determined by Spring Boot configuration (default 8080 unless overridden in `application.yaml` or with `--server.port`).

## Configuration
Application configuration files (if present) may be under `src/main/resources` or the `target/classes` output. Common config files used by this project (per repository layout):
- `application.yaml` or `application.properties`
- `application-docker.yaml` (used for Docker-specific overrides)

Set datasource and other properties using environment variables or standard Spring mechanisms. Example environment variables (as used by Spring Boot):

- `SPRING_DATASOURCE_URL` (e.g. `jdbc:postgresql://host:5432/db`)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_PROFILES_ACTIVE` (e.g. `docker`, `dev`, `prod`)

Alternatively pass properties on the command line:

```cmd
java -jar target\*.jar --spring.datasource.url=jdbc:postgresql://db:5432/books --spring.datasource.username=user --spring.datasource.password=pass
```

## Database
- The project declares a PostgreSQL JDBC driver in `pom.xml` (runtime scope). Ensure a PostgreSQL instance is accessible when starting the service in a non-embedded mode.
- There are `data/` files in the repository (for local/dev usage). These may be H2 files left from earlier development; the H2 dependency is commented out in `pom.xml`.

If you want a quick in-memory DB for local development, enable H2 (add dependency or profile) or run PostgreSQL locally and point the app to it.

## Logging
- Logs are written to the `logs/` folder (example files: `application.log`). The project uses Logback (Spring Boot's default).
- Use a `logback-spring.xml` file (placed in `src/main/resources`) to customize appenders, rotation, and logging levels. Example configuration is provided in `src/main/resources/logback-spring.xml`.

## Observability
- The project includes Spring Boot Actuator and Micrometer Prometheus registry dependencies.
- If enabled by configuration, actuator endpoints (e.g. `/actuator/metrics`, `/actuator/prometheus`) will be available.

## OpenAPI / Swagger UI
- `springdoc-openapi-starter-webmvc-ui` is included in `pom.xml` and exposes Swagger UI (commonly under `/swagger-ui.html` or `/swagger-ui/index.html`) when the application is running and not disabled by configuration.

## Tests
Run tests with Maven:

```cmd
mvn test
```

The project includes testing dependencies such as `spring-boot-starter-test` and `rest-assured` for API tests.

## Common commands (Windows, cmd.exe)

- Clean and build:

```cmd
mvn clean package
```

- Run locally:

```cmd
mvn spring-boot:run
```
or
```cmd
java -jar target\*.jar
```

- Run with a different port:

```cmd
java -jar target\*.jar --server.port=8081
```

- Build Docker image:

```cmd
docker build -t book-service:local .
```

- Run Docker container (maps container 8081 to host 8081):

```cmd
docker run --rm -p 8081:8081 book-service:local
```

## Troubleshooting
- Missing configuration / database connection errors: verify `SPRING_DATASOURCE_URL`, `username`, and `password` are correct and DB is reachable.
- Port already in use: change `--server.port` or stop the conflicting process.
- Maven build failures: run `mvn -X clean package` to get a verbose stacktrace and fix dependency or compilation issues.

## Assumptions and notes
- Dockerfile exposes port 8081; however Spring Boot's default port is 8080 unless overridden. The effective port for the jar will be the one set in configuration or via `--server.port`.
- The repository contains `data/` and `logs/` folders; treat these as runtime artifacts and back them up if needed.

## Contributing
- Open a pull request with a descriptive title and tests for new features or bug fixes.
- Run `mvn test` before submitting a PR.

## License
No license information was provided in the project POM. Add a LICENSE file to this repository to make the licensing explicit.