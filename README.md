# 🏦 Banking Microservices Platform

## 🚀 Project Overview
> A scalable, secure banking platform using a **microservices** architecture.

**Built With:**
- Java 17 (Spring Boot 3)
- ReactJS (frontend)
- KrakenD (API Gateway)
- RabbitMQ (messaging)
- PostgreSQL (databases)
- Docker Compose (deployment)
- Redis (caching and transaction locking)

## 🏛️ Architecture Diagram
![Architecture Diagram](./Architecture.png)

## 🧩 Microservices Overview
| Service | Description |
|---------|-------------|
| **Profile Service** | Manage customer profiles, authentication (JWT), RBAC |
| **Store of Value Service** | Manage bank accounts (creation, update, balance inquiry) |
| **Payment Service** | Handle withdrawals, topups, transfers (async) |
| **Events Service** | Send notifications after transactions |
| **API Gateway (KrakenD)** | Central routing, JWT validation, aggregation |
| **Frontend (ReactJS)** | Customer portal (balances, transfers, notifications) |

## 🗂️ Directory Structure
```
/banking-platform
├── /profile-service
├── /store-of-value-service
├── /payment-service
├── /events-service
├── /gateway (krakend)
├── /frontend (react)
├── docker-compose.yml
├── README.md
└── Architecture.png
```

## 📦 Services Breakdown

<details>
<summary>🔐 Profile Service</summary>

- **Endpoints**:
    - `POST /api/auth/register`
    - `POST /api/auth/login`
    - `PUT /api/profile/update`
- **Security**:
    - JWT-based authentication
    - Role-based authorization
</details>

<details>
<summary>🏦 Store of Value Service</summary>

- **Endpoints**:
    - `POST /api/accounts`
    - `GET /api/accounts/{id}`
    - `PUT /api/accounts/{id}/activate`
- **Database**:
    - PostgreSQL table linked to profile IDs
</details>

<details>
<summary>💸 Payment Service</summary>

- **Endpoints**:
    - `POST /api/transactions/topup`
    - `POST /api/transactions/withdraw`
    - `POST /api/transactions/transfer`
- **Transactions**:
    - Event-driven via RabbitMQ
    - Strong idempotency and concurrency handling
    - Transaction caching with Redis
    - Distributed locking via Redis for transaction integrity
</details>

<details>
<summary>📢 Events Service</summary>

- **Listens To**:
    - `TransactionCompletedEvent`
- **Sends**:
    - Email/SMS notifications (mocked)
</details>

<details>
<summary>🛡️ API Gateway (KrakenD)</summary>

- **Routes**:
    - `/api/me/accounts`
    - `/api/transactions`
- **Security**:
    - JWT validation
    - Aggregates backend services
</details>

<details>
<summary>🖥️ Frontend (ReactJS)</summary>

- **Features**:
    - Registration/Login
    - View balances
    - Initiate transfers
    - Receive notifications
</details>

## ⚙️ Setup Instructions

### Docker Setup (Recommended)
The easiest way to run the entire system is using Docker Compose:

1. **Clone Repository**:
   ```bash
   git clone <repo-url>
   cd banking-platform
   ```

2. **Build and Start Services**:
   ```bash
   docker-compose build
   docker-compose up -d
   ```

3. **Verify Services**:
   ```bash
   docker-compose ps
   ```

4. **View Logs** (optional):
   ```bash
   # View logs from all services
   docker-compose logs -f
   
   # View logs from a specific service
   docker-compose logs -f payment-service
   ```

5. **Stop Services**:
   ```bash
   docker-compose down
   ```

### Local Development Setup
For development work on individual services:

1. **Clone Repository**:
   ```bash
   git clone <repo-url>
   cd banking-platform
   ```

2. **Start Dependencies** (databases, message broker, Redis):
   ```bash
   docker-compose up -d redis rabbitmq profile-db accounts-db transactions-db events-db
   ```

3. **Build and Run Individual Services**:
   ```bash
   # For each service (example with payment-service)
   cd payment-service
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access Services**:
    - Frontend: `http://localhost:3000`
    - API Gateway: `http://localhost:8080`
    - Profile Service: `http://localhost:8081`
    - Payment Service: `http://localhost:8082`
    - Store of Value Service: `http://localhost:8083`
    - Events Service: `http://localhost:8084`
    - Redis: `localhost:6379`
    - RabbitMQ Management: `http://localhost:15672` (guest/guest)

> **Important:** In the production-like setup, all client requests to backend services are routed through the KrakenD API Gateway (`http://localhost:8080`). While individual services can be accessed directly during development, the gateway handles authentication, routing, and request aggregation in the integrated system. The frontend application is configured to communicate with the backend exclusively through this gateway.

## 🧪 Testing Strategy

- Unit tests: **JUnit 5**
- Mocking services: **Mockito**
- Database tests: **Testcontainers**
- Edge Cases Covered:
    - Insufficient balance
    - Invalid account numbers
    - Duplicate transactions

## �� API Documentation

> **Note:** The endpoints listed in each service description are sample endpoints only. For a complete list of all available endpoints and their authorization requirements, please refer to the Postman collections included with each service.

Each service implements its own API documentation:
- Profile Service: `http://localhost:8081/swagger-ui.html`
- Store of Value Service: `http://localhost:8083/swagger-ui.html`
- Payment Service: `http://localhost:8082/swagger-ui.html`

### Role-Based API Access
All services implement role-based access control (RBAC). The Postman collections categorize endpoints as:
- **Public**: No authentication required
- **Authenticated**: Requires valid JWT token
- **Role-Specific**: Requires specific roles (USER, ADMIN, SYSTEM)

Refer to each service's documentation for detailed permission requirements per endpoint.

## ✨ Technical Highlights

- Microservice-first architecture
- Asynchronous transaction processing (RabbitMQ)
- JWT-based secure authentication
- API Gateway aggregation (KrakenD)
- Redis caching for improved transaction performance
- Distributed transaction locking with Redis to prevent race conditions
- Docker Compose orchestration for local dev
- Built for horizontal scaling and resilience

# 🎯 Done!