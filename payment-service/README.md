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

#### Authentication
- `POST /api/auth/signin` - Authentication endpoint to get a JWT token

#### Transactions
- `POST /api/transactions/deposit` - Create a deposit transaction
- `POST /api/transactions/withdrawal` - Create a withdrawal transaction
- `POST /api/transactions/transfer` - Create a transfer transaction between accounts
- `GET /api/transactions/{reference}` - Get transaction details by reference
- `GET /api/transactions/user/{userId}` - Get all transactions for a user
- `GET /api/transactions/account/{accountId}` - Get all transactions for an account

## Technical Details

### Architecture

The Payment Service uses a microservices architecture and communicates with:
- Store of Value Service: For account operations (debit/credit)
- RabbitMQ: For asynchronous transaction processing

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

## Transaction Flow

1. User sends a transaction request via the REST API
2. Service validates inputs and creates a PENDING transaction
3. Transaction is published to the appropriate RabbitMQ queue
4. Consumer processes the transaction asynchronously
5. Consumer calls the Store of Value service to update accounts
6. Transaction status is updated (COMPLETED or FAILED)
7. Notification is published about the transaction result 