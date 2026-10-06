# Restaurant Ordering System

Spring Boot restaurant ordering app with Thymeleaf UI, REST APIs, Spring Security, and MySQL.

## Requirements

- Java 17+
- Maven 3.9+
- MySQL 8

## Configure MySQL

Create a database (Hibernate can also create it):

```sql
CREATE DATABASE IF NOT EXISTS restaurant_ordering;
```

Environment variables (defaults shown):

```text
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=restaurant_ordering
MYSQL_USER=root
MYSQL_PASSWORD=root
```

Optional JWT secret:

```text
JWT_SECRET=change-this-to-a-long-random-secret
```

## Run

```bash
mvn spring-boot:run
```

Open http://localhost:8080

Seeded accounts:

| Role | Email | Password |
| --- | --- | --- |
| ADMIN | admin@restaurant.com | Admin@123 |
| STAFF | staff@restaurant.com | Staff@123 |
| CUSTOMER | customer@restaurant.com | Customer@123 |

## REST APIs

Import `postman/Restaurant-Ordering-System.postman_collection.json`.

Login:

```http
POST /api/auth/login
```

Use the returned Bearer token for protected endpoints.

## Tests

```bash
mvn test
```
