# 🏘️ Society Maintenance Tracking System

A **Spring Boot** REST API for managing residential society operations — covering member & flat management, maintenance payment tracking, complaint handling, and community announcements, secured with JWT authentication.

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Authentication](#authentication)
- [License](#license)

---

## ✨ Features

- 👤 **Member & Flat Management** — Register residents, assign flats, manage occupancy
- 💰 **Payment Tracking** — Generate maintenance bills, record payments, track pending dues
- 🛠️ **Complaint Management** — Raise, assign, and resolve maintenance complaints
- 📢 **Notice Board** — Post and manage society-wide announcements
- 🔐 **Role-Based Access** — Separate permissions for `ADMIN` and `RESIDENT`
- 🔒 **JWT Authentication** — Secure, stateless API access

---

## 🛠️ Tech Stack

| Layer       | Technology                   |
|-------------|------------------------------|
| Language    | Java 17+                     |
| Framework   | Spring Boot 3.x              |
| Security    | Spring Security + JWT        |
| Database    | PostgreSQL                   |
| ORM         | Spring Data JPA (Hibernate)  |
| Build Tool  | Maven                        |
| API Docs    | Swagger / OpenAPI 3          |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL
- Git

### Setup

```bash
# 1. Clone the repo
git clone https://github.com/your-username/society-maintenance-system.git
cd society-maintenance-system

# 2. Create the database
psql -U postgres -c "CREATE DATABASE society_db;"

# 3. Configure application.properties
```

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/society_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT
jwt.secret=your_jwt_secret_key
jwt.expiration=86400000
```

```bash
# 4. Run
mvn spring-boot:run
```

- **Base URL:** `http://localhost:8080/api`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## 📡 API Endpoints

> All protected routes require the header:
> `Authorization: Bearer <token>`

---

### 🔐 Authentication

| Method | Endpoint         | Description           | Access |
|--------|------------------|-----------------------|--------|
| POST   | `/auth/register` | Register a new user   | Public |
| POST   | `/auth/login`    | Login and receive JWT | Public |

**Login Request Body**
```json
{
  "email": "admin@society.com",
  "password": "yourpassword"
}
```

**Login Response**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "ADMIN",
  "expiresIn": 86400000
}
```

---

### 👤 Members

| Method | Endpoint        | Description           | Access |
|--------|-----------------|-----------------------|--------|
| GET    | `/members`      | List all members      | ADMIN  |
| GET    | `/members/{id}` | Get member by ID      | ADMIN  |
| POST   | `/members`      | Add a new member      | ADMIN  |
| PUT    | `/members/{id}` | Update member details | ADMIN  |
| DELETE | `/members/{id}` | Remove a member       | ADMIN  |

**POST `/members` — Request Body**
```json
{
  "name": "Rahul Sharma",
  "email": "rahul@example.com",
  "phone": "9876543210",
  "flatId": 3
}
```

---

### 🏠 Flats

| Method | Endpoint      | Description         | Access |
|--------|---------------|---------------------|--------|
| GET    | `/flats`      | List all flats      | ADMIN  |
| GET    | `/flats/{id}` | Get flat by ID      | ADMIN  |
| POST   | `/flats`      | Add a new flat      | ADMIN  |
| PUT    | `/flats/{id}` | Update flat details | ADMIN  |
| DELETE | `/flats/{id}` | Remove a flat       | ADMIN  |

**POST `/flats` — Request Body**
```json
{
  "flatNumber": "B-204",
  "floor": 2,
  "type": "2BHK",
  "isOccupied": true
}
```

---

### 💰 Payment Tracking

| Method | Endpoint                      | Description                   | Access      |
|--------|-------------------------------|-------------------------------|-------------|
| GET    | `/payments`                   | List all payment records      | ADMIN       |
| GET    | `/payments/{id}`              | Get payment by ID             | ADMIN       |
| GET    | `/payments/member/{memberId}` | Get all payments for a member | ADMIN, SELF |
| GET    | `/payments/pending`           | List all unpaid dues          | ADMIN       |
| POST   | `/payments`                   | Record a new payment          | ADMIN       |
| PUT    | `/payments/{id}`              | Update payment status         | ADMIN       |

**POST `/payments` — Request Body**
```json
{
  "memberId": 5,
  "flatId": 3,
  "amount": 2500.00,
  "month": "MAY",
  "year": 2026,
  "paymentMode": "UPI",
  "status": "PAID"
}
```

**GET `/payments/pending` — Sample Response**
```json
[
  {
    "id": 12,
    "memberName": "Rahul Sharma",
    "flatNumber": "B-204",
    "amount": 2500.00,
    "month": "APRIL",
    "year": 2026,
    "dueDate": "2026-04-10",
    "status": "PENDING"
  }
]
```

**Payment Status Values:** `PAID` · `PENDING` · `OVERDUE`

---

### 🛠️ Complaints

| Method | Endpoint                        | Description             | Access      |
|--------|---------------------------------|-------------------------|-------------|
| GET    | `/complaints`                   | List all complaints     | ADMIN       |
| GET    | `/complaints/{id}`              | Get complaint by ID     | ADMIN, SELF |
| GET    | `/complaints/member/{memberId}` | Get complaints by member | ADMIN, SELF |
| POST   | `/complaints`                   | Raise a new complaint   | RESIDENT    |
| PUT    | `/complaints/{id}/status`       | Update complaint status | ADMIN       |
| DELETE | `/complaints/{id}`              | Delete a complaint      | ADMIN       |

**POST `/complaints` — Request Body**
```json
{
  "memberId": 5,
  "category": "PLUMBING",
  "description": "Water leakage in bathroom since 3 days.",
  "priority": "HIGH"
}
```

**PUT `/complaints/{id}/status` — Request Body**
```json
{
  "status": "RESOLVED",
  "remarks": "Pipe fixed by technician on 13-May-2026"
}
```

**Complaint Status Values:** `OPEN` · `IN_PROGRESS` · `RESOLVED` · `CLOSED`

---

### 📢 Notice Board

| Method | Endpoint        | Description       | Access |
|--------|-----------------|-------------------|--------|
| GET    | `/notices`      | Get all notices   | ALL    |
| GET    | `/notices/{id}` | Get notice by ID  | ALL    |
| POST   | `/notices`      | Post a new notice | ADMIN  |
| PUT    | `/notices/{id}` | Update a notice   | ADMIN  |
| DELETE | `/notices/{id}` | Delete a notice   | ADMIN  |

**POST `/notices` — Request Body**
```json
{
  "title": "Water Supply Shutdown",
  "content": "Water supply will be unavailable on 15-May-2026 from 10AM to 2PM due to maintenance.",
  "category": "MAINTENANCE",
  "postedBy": "Admin"
}
```

---

## 🔐 Authentication

This project uses **JWT (JSON Web Tokens)** for stateless authentication.

1. Call `POST /api/auth/login` to receive a token.
2. Pass the token in every subsequent request:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Roles & Access:**

| Role       | Permissions                                                         |
|------------|---------------------------------------------------------------------|
| `ADMIN`    | Full access — manage members, flats, payments, complaints, notices  |
| `RESIDENT` | View own data, raise complaints, read notices                       |

---

## 📄 License



---

> 💡 *Have a suggestion or found a bug? [Open an issue]