# Management Service
A Spring Boot management service for IoT resources, providing REST APIs for managing **clusters**, **devices**, and **users** with JWT-based security.


## Tech Stack
- **Java 21**
- **Spring Boot** (Spring MVC, Spring Data JDBC)
- **Jakarta EE**
- **Lombok**
- **Flyway** – database migrations
- **JWT** – authentication via public-key verification
- **Maven** – build tool (with Maven Wrapper)


## Project Structure
cloud.chlora.management
├─ common # Shared configuration, enums, error handling, mappers, and error response DTO
├─ iot 
│ ├─ cluster # Cluster CRUD – controller, domain, DTOs, repository, service
│ └─ device # Device CRUD – controller, domain, DTOs, repository, service 
├─ security # JWT authentication config, properties, and cookie-based auth utilities 
├─ user # User CRUD – controller, domain, DTOs, repository, service 
└─ ManagementServiceApplication.java


## Prerequisites

- **JDK 21+**
- A relational database supported by Spring Data JDBC (e.g., PostgreSQL)
- A valid RSA **public key** for JWT verification (placed in `src/main/resources/keys/public-key.pem`)


## Getting Started
### 1. Clone the repository
```bash git clone <repository-url> cd management-service```

### 2. Configure the application
Edit `src/main/resources/application.yml` to set your database connection, server port, and any other environment-specific properties.

### 3. Build
```bash ./mvnw clean package```

### 4. Run
```bash ./mvnw spring-boot:run```
Or run the packaged JAR:
```bash java -jar target/management-service-*.jar```


## Database Migrations
Flyway migrations are located in `src/main/resources/db.migration/`:

| Migration                    | Description                |
|------------------------------|----------------------------|
| `V1_0__create_sequences.sql` | Creates database sequences |
| `V1_1__create_tables.sql`    | Creates application tables |
| `V1_2__create_triggers.sql`  | Creates database triggers  |



## API Overview

| Domain       | Base Path (typical) | Operations                                |
|--------------|---------------------|-------------------------------------------|
| **Clusters** | `/api/clusters`     | Create, Get, List (paged), Update         |
| **Devices**  | `/api/devices`      | Create, Get, List (paged), Update         |
| **Users**    | `/api/users`        | Create, Get, List (paged), Update, Delete |

All endpoints are secured via JWT. The service validates tokens using the configured RSA public key.


## Security
- **JWT authentication** – tokens are verified against an RSA public key (`public-key.pem`).
- **Cookie-based auth** – the `CookieAuth` utility supports extracting tokens from HTTP cookies.
- Security configuration is managed in `SecurityConfig` with JWT properties defined in `JwtProperties`.
