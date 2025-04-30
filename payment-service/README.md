# Payment Service API

This is the Payment Service component of the Banking System, responsible for processing payment transactions including deposits, withdrawals, and transfers.

## API Documentation

### Using the Postman Collection

I've included a Postman collection and environment to help you test the API:

1. Import both files into Postman:
   - `payment-service-api.postman_collection.json` 
   - `payment-service-api.postman_environment.json`

2. Select the "Payment Service Environment" environment in Postman.

3. Authentication:
   - Execute the "Get SoV Auth Token" request in the "Store of Value Service" folder first to authenticate with the Store of Value service. This will automatically set the `sov_token` variable in your environment.
   - Then execute the "Get Local Auth Token" request in the "Authentication" folder to get a token for the Payment Service API. Save the token value to the `auth_token` environment variable.

4. All other requests will automatically use the authentication tokens.

### API Endpoints

> **Note:** The endpoints listed below are samples only. For the complete list of endpoints, please refer to the Postman collection. The collection categorizes endpoints as public or authenticated and indicates which user roles can access each endpoint.

#### Authentication
- `POST /api/auth/signin` - Authentication endpoint to get a JWT token

#### Transactions
- `POST /api/transactions/deposit` - Create a deposit transaction
- `POST /api/transactions/withdrawal` - Create a withdrawal transaction
- `POST /api/transactions/transfer` - Create a transfer transaction between accounts
- `GET /api/transactions/{reference}` - Get transaction details by reference
- `GET /api/transactions/user/{userId}` - Get all transactions for a user
- `GET /api/transactions/account/{accountId}` - Get all transactions for an account

### Role-Based Access Control

The API implements role-based access control (RBAC) with the following roles:
- **USER**: Regular customer access (view own transactions, initiate transfers)
- **ADMIN**: Administrative access (view all transactions, perform administrative operations)
- **SYSTEM**: Internal system access (for inter-service communication)

Each endpoint in the Postman collection is tagged with the role(s) required to access it. You must obtain a token for a user with the appropriate role to access protected endpoints.

## Technical Details

### Architecture

The Payment Service uses a microservices architecture and communicates with:
- Store of Value Service: For account operations (debit/credit)
- RabbitMQ: For asynchronous transaction processing
- Redis: For caching transaction data and distributed locking to ensure transaction integrity

### Message Queues
- `payment.deposit.queue` - For deposit transactions
- `payment.withdrawal.queue` - For withdrawal transactions
- `payment.transfer.queue` - For transfer transactions
- `notification.queue` - For sending transaction notifications

### Dead Letter Queues (DLQs)
- `payment.deposit.dlq` - Failed deposit transactions
- `payment.withdrawal.dlq` - Failed withdrawal transactions
- `payment.transfer.dlq` - Failed transfer transactions
- `notification.dlq` - Failed notification attempts

### Caching Strategy
- Redis is used to cache transaction data
- Improves read performance for frequently accessed transactions
- Reduces database load for high-volume transaction lookups
- Cache TTL configured for optimal performance

### Transaction Locking
- Redis-based distributed locking mechanism
- Prevents concurrent modifications to the same account
- Ensures transaction atomicity in distributed environments
- Implements lock acquisition with timeouts and automatic release
- Protects against race conditions in high-throughput scenarios

## Transaction Flow

1. User sends a transaction request via the REST API
2. Service validates inputs and creates a PENDING transaction
3. Service acquires a distributed lock on affected accounts via Redis
4. Transaction is published to the appropriate RabbitMQ queue
5. Consumer processes the transaction asynchronously
6. Consumer calls the Store of Value service to update accounts
7. Transaction status is updated (COMPLETED or FAILED)
8. Distributed lock is released
9. Notification is published about the transaction result
10. Completed transactions are cached in Redis for faster retrieval

## Setup Instructions

### Docker Setup
Run the payment service as part of the entire system:

```bash
# From the root of the project
docker-compose build payment-service
docker-compose up -d payment-service
```

This will start the payment service along with its dependencies (Redis, PostgreSQL, RabbitMQ).

### Local Development Setup

1. **Start Dependencies**:
   ```bash
   # From the root of the project
   docker-compose up -d redis rabbitmq transactions-db
   ```

2. **Build and Run**:
   ```bash
   # From the payment-service directory
   mvn clean install
   mvn spring-boot:run
   ```

3. **Environment Variables**:
   When running locally, ensure these environment variables are set:
   ```
   SERVER_PORT=8082
   SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5435/transactionsdb
   SPRING_DATASOURCE_USERNAME=transactions_user
   SPRING_DATASOURCE_PASSWORD=transactions_password
   RABBITMQ_HOST=localhost
   RABBITMQ_PORT=5672
   RABBITMQ_USERNAME=guest
   RABBITMQ_PASSWORD=guest
   REDIS_HOST=localhost
   REDIS_PORT=6379
   REDIS_PASSWORD=password
   STORE_OF_VALUE_SERVICE_URL=http://localhost:8083
   AUTH_SERVICE_URL=http://localhost:8081
   ```

4. **Accessing the Service**:
   - API: `http://localhost:8082`
   - Swagger UI: `http://localhost:8082/swagger-ui.html`

> **Note:** In the production environment, client applications do not access the Payment Service directly. All requests are routed through the KrakenD API Gateway (`http://localhost:8080`), which handles authentication, request routing, and communication between client applications and backend services. The direct endpoints are primarily for development and testing purposes.

## API Integration with KrakenD Gateway

The Payment Service's endpoints are exposed to client applications through the KrakenD API Gateway with the following URL patterns:

- **Direct Service URL:** `http://payment-service:8082/api/transactions/...`
- **Gateway URL:** `http://localhost:8080/api/transactions/v1/...`

The gateway handles:
- JWT validation
- Request routing to the appropriate microservice
- Error handling and response standardization
- Rate limiting and request throttling

See the main project README and KrakenD configuration for complete gateway routing details. 