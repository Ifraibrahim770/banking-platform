# KrakenD API Gateway Configuration

This directory contains the configuration files for the KrakenD API Gateway, which serves as the entry point for the banking system microservices.

## Configuration Files

- `krakend.json`: The main configuration used for Docker containerized environments.
- `krakend.dev.json`: A development configuration for local development that uses localhost URLs.

## Services and Endpoints

The gateway proxies requests to the following services:

| Service | Container Name | Container Port | Local Port | API Endpoints |
|---------|---------------|----------------|------------|---------------|
| Profile Service | profile-service | 8081 | 8081 | `/api/auth/*`, `/api/profile/*` |
| Store of Value Service | store-of-value-service | 8082 | 8083 | `/api/accounts/*` |
| Payment Service | payment-service | 8083 | 8082 | `/api/payments/*`, `/api/transactions/*` |
| Events Service | events-service | 8084 | 8084 | `/api/notifications/*` |

## API Versioning

All endpoints are versioned in the KrakenD configuration. For example:
- External: `/api/profile/v1/me`
- Internal: `/api/profile/me`

## Authentication

JWT authentication is configured for all protected endpoints. The JWT token must be provided in the Authorization header.

## Running KrakenD

### In Docker Compose

The KrakenD service is already configured in the `docker-compose.yml` file and will start automatically with:

```bash
docker-compose up
```

### Local Development

To run KrakenD locally for development:

1. Install KrakenD: [https://www.krakend.io/docs/overview/installing/](https://www.krakend.io/docs/overview/installing/)
2. Run the gateway with the development configuration:

```bash
krakend run -c krakend/krakend.dev.json
```

## Security Configuration

The gateway is configured with:
- JWT validation for protected endpoints
- CORS settings to allow frontend applications to communicate with the API
- Debug logging for easier development

Remember to replace the JWT secret (`YourVeryLongAndSecureSecretKeyHere_Replace_This_With_Actual_Secret`) with your actual secret before deploying to production. 