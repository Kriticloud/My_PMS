# Property Management System (PMS) with POS Integration

A complete, production-ready Property Management System integrated with a Point of Sale (POS) system.

## Tech Stack

| Layer    | Technology                     |
| -------- | ------------------------------ |
| Backend  | Java 17 + Spring Boot 3.2      |
| Frontend | Vue 3 (Composition API) + Vite |
| Database | PostgreSQL 16                  |
| ORM      | JPA / Hibernate                |
| Auth     | JWT (jjwt 0.12)                |
| UI       | Tailwind CSS                   |
| State    | Pinia                          |
| HTTP     | Axios                          |
| Deploy   | Docker + Docker Compose        |

## Project Structure

```
My_PMS/
├── database/
│   ├── schema.sql          # PostgreSQL schema
│   └── seed.sql            # Initial seed data
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/pms/
│       ├── PmsApplication.java
│       ├── config/          # Security config
│       ├── controller/      # REST controllers
│       ├── dto/             # Data transfer objects
│       ├── entity/          # JPA entities
│       ├── exception/       # Exception handling
│       ├── repository/      # JPA repositories
│       ├── security/        # JWT auth components
│       └── service/         # Business logic
├── frontend/
│   ├── package.json
│   ├── Dockerfile
│   ├── vite.config.js
│   └── src/
│       ├── api/             # Axios instance
│       ├── layouts/         # App layout
│       ├── router/          # Vue Router + guards
│       ├── stores/          # Pinia stores
│       └── views/           # Page components
└── docker-compose.yml
```

## Features

### PMS (Property Management)

- Room management (CRUD, status tracking)
- Guest management with ID verification
- Booking system (create, check-in, check-out, cancel)
- Date-based room availability
- Combined billing & invoicing

### POS (Point of Sale)

- Menu item catalog by category
- Cart-based ordering system
- Room service / restaurant / walk-in orders
- Auto-inventory reduction on order
- POS charges linked to room invoices

### Billing & Payments

- Auto-generated combined invoices (room + POS)
- Multiple payment modes (Cash, Card, UPI)
- Payment tracking with partial payment support
- 18% GST auto-calculation

### Reports

- Dashboard with real-time stats
- Occupancy report
- Daily revenue report
- POS sales report

### Security

- JWT authentication
- Role-Based Access Control (RBAC)
- 3 roles: ADMIN, FRONT_DESK, RESTAURANT_STAFF

---

## Quick Start

### Option 1: Docker Compose (Recommended)

```bash
docker-compose up --build
```

- Frontend: http://localhost
- Backend API: http://localhost:8080
- PostgreSQL: localhost:5432

### Option 2: Manual Setup

#### Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 16+
- Maven

#### Database

```bash
# Create database
psql -U postgres -c "CREATE DATABASE pms_db;"

# Run schema and seed
psql -U postgres -d pms_db -f database/schema.sql
psql -U postgres -d pms_db -f database/seed.sql
```

#### Backend

```bash
cd backend
mvn spring-boot:run
```

#### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## Default Login Credentials

| Username   | Password | Role             |
| ---------- | -------- | ---------------- |
| admin      | admin123 | ADMIN            |
| frontdesk1 | admin123 | FRONT_DESK       |
| waiter1    | admin123 | RESTAURANT_STAFF |

---

## API Endpoints

### Auth

| Method | Endpoint          | Description     |
| ------ | ----------------- | --------------- |
| POST   | `/api/auth/login` | Login & get JWT |

### Rooms

| Method | Endpoint                 | Description   |
| ------ | ------------------------ | ------------- |
| GET    | `/api/rooms`             | All rooms     |
| GET    | `/api/rooms/{id}`        | Room by ID    |
| GET    | `/api/rooms/types`       | Room types    |
| POST   | `/api/rooms`             | Create room   |
| PUT    | `/api/rooms/{id}`        | Update room   |
| PATCH  | `/api/rooms/{id}/status` | Update status |
| DELETE | `/api/rooms/{id}`        | Delete room   |

### Bookings

| Method | Endpoint                       | Description    |
| ------ | ------------------------------ | -------------- |
| GET    | `/api/bookings`                | All bookings   |
| POST   | `/api/bookings`                | Create booking |
| POST   | `/api/bookings/{id}/check-in`  | Check-in       |
| POST   | `/api/bookings/{id}/check-out` | Check-out      |
| POST   | `/api/bookings/{id}/cancel`    | Cancel         |

### POS

| Method | Endpoint                      | Description   |
| ------ | ----------------------------- | ------------- |
| GET    | `/api/menu/items`             | Menu items    |
| GET    | `/api/menu/categories`        | Categories    |
| POST   | `/api/pos/orders`             | Create order  |
| PATCH  | `/api/pos/orders/{id}/status` | Update status |

### Billing

| Method | Endpoint                                     | Description      |
| ------ | -------------------------------------------- | ---------------- |
| POST   | `/api/billing/invoices/generate/{bookingId}` | Generate invoice |
| POST   | `/api/billing/payments`                      | Process payment  |

### Reports

| Method | Endpoint                 | Description      |
| ------ | ------------------------ | ---------------- |
| GET    | `/api/reports/dashboard` | Dashboard stats  |
| GET    | `/api/reports/occupancy` | Occupancy report |
| GET    | `/api/reports/revenue`   | Revenue report   |
| GET    | `/api/reports/pos-sales` | POS sales report |
