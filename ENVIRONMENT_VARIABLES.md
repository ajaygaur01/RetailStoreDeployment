# Environment Variables Guide

This document lists all environment variables needed to run the Retail Store Sample App services.

## Overview

The application consists of 5 microservices, each with its own database/persistence requirements:

- **Cart Service** - Uses DynamoDB (or in-memory)
- **Catalog Service** - Uses MySQL (or in-memory)
- **Orders Service** - Uses PostgreSQL (or in-memory)
- **Checkout Service** - Uses Redis (or in-memory)
- **UI Service** - Frontend that connects to other services

## Quick Answer: Do You Need Database URLs?

**Yes, if you want to use persistent storage.** However, all services can run in **`in-memory` mode** for development/testing without any database setup.

## Environment Variables by Service

### 1. Cart Service (Java - Port 8082)

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `PORT` | Server port | `8080` | No |
| `RETAIL_CART_PERSISTENCE_PROVIDER` | `in-memory` or `dynamodb` | `in-memory` | No |
| `RETAIL_CART_PERSISTENCE_DYNAMODB_ENDPOINT` | DynamoDB endpoint URL | `""` | Yes (if using DynamoDB) |
| `RETAIL_CART_PERSISTENCE_DYNAMODB_TABLE_NAME` | DynamoDB table name | `Items` | No |
| `RETAIL_CART_PERSISTENCE_DYNAMODB_CREATE_TABLE` | Auto-create table | `false` | No |
| `AWS_ACCESS_KEY_ID` | AWS access key | - | Yes (if using DynamoDB) |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | - | Yes (if using DynamoDB) |

**Example for DynamoDB:**
```bash
RETAIL_CART_PERSISTENCE_PROVIDER=dynamodb
RETAIL_CART_PERSISTENCE_DYNAMODB_ENDPOINT=http://localhost:8000  # or AWS endpoint
RETAIL_CART_PERSISTENCE_DYNAMODB_CREATE_TABLE=true
AWS_ACCESS_KEY_ID=your-key
AWS_SECRET_ACCESS_KEY=your-secret
```

### 2. Catalog Service (Go - Port 8081)

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `PORT` | Server port | `8080` | No |
| `RETAIL_CATALOG_PERSISTENCE_PROVIDER` | `in-memory` or `mysql` | `in-memory` | No |
| `RETAIL_CATALOG_PERSISTENCE_ENDPOINT` | MySQL endpoint (host:port) | `""` | Yes (if using MySQL) |
| `RETAIL_CATALOG_PERSISTENCE_DB_NAME` | Database name | `catalogdb` | No |
| `RETAIL_CATALOG_PERSISTENCE_USER` | Database user | `catalog_user` | No |
| `RETAIL_CATALOG_PERSISTENCE_PASSWORD` | Database password | `""` | Yes (if using MySQL) |
| `RETAIL_CATALOG_PERSISTENCE_CONNECT_TIMEOUT` | Connection timeout (seconds) | `5` | No |

**Example for MySQL:**
```bash
RETAIL_CATALOG_PERSISTENCE_PROVIDER=mysql
RETAIL_CATALOG_PERSISTENCE_ENDPOINT=localhost:3306
RETAIL_CATALOG_PERSISTENCE_DB_NAME=catalogdb
RETAIL_CATALOG_PERSISTENCE_USER=catalog_user
RETAIL_CATALOG_PERSISTENCE_PASSWORD=your-password
```

### 3. Orders Service (Java - Port 8083)

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `PORT` | Server port | `8080` | No |
| `RETAIL_ORDERS_PERSISTENCE_PROVIDER` | `in-memory` or `postgres` | `in-memory` | No |
| `RETAIL_ORDERS_PERSISTENCE_POSTGRES_ENDPOINT` | PostgreSQL endpoint (host:port) | `""` | Yes (if using PostgreSQL) |
| `RETAIL_ORDERS_PERSISTENCE_POSTGRES_NAME` | Database name | `""` | Yes (if using PostgreSQL) |
| `RETAIL_ORDERS_PERSISTENCE_POSTGRES_USERNAME` | Database username | `""` | Yes (if using PostgreSQL) |
| `RETAIL_ORDERS_PERSISTENCE_POSTGRES_PASSWORD` | Database password | `""` | Yes (if using PostgreSQL) |
| `RETAIL_ORDERS_MESSAGING_PROVIDER` | `in-memory`, `sqs`, or `rabbitmq` | `in-memory` | No |
| `RETAIL_ORDERS_MESSAGING_SQS_TOPIC` | SQS topic name | `""` | Yes (if using SQS) |
| `RETAIL_ORDERS_MESSAGING_RABBITMQ_ADDRESSES` | RabbitMQ endpoints (host:port) | `""` | Yes (if using RabbitMQ) |
| `RETAIL_ORDERS_MESSAGING_RABBITMQ_USERNAME` | RabbitMQ username | `""` | Yes (if using RabbitMQ) |
| `RETAIL_ORDERS_MESSAGING_RABBITMQ_PASSWORD` | RabbitMQ password | `""` | Yes (if using RabbitMQ) |

**Example for PostgreSQL:**
```bash
RETAIL_ORDERS_PERSISTENCE_PROVIDER=postgres
RETAIL_ORDERS_PERSISTENCE_POSTGRES_ENDPOINT=localhost:5432
RETAIL_ORDERS_PERSISTENCE_POSTGRES_NAME=orders
RETAIL_ORDERS_PERSISTENCE_POSTGRES_USERNAME=orders_user
RETAIL_ORDERS_PERSISTENCE_POSTGRES_PASSWORD=your-password
```

### 4. Checkout Service (Node.js - Port 8085)

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `PORT` | Server port | `8080` | No |
| `RETAIL_CHECKOUT_PERSISTENCE_PROVIDER` | `in-memory` or `redis` | `in-memory` | No |
| `RETAIL_CHECKOUT_PERSISTENCE_REDIS_URL` | Redis connection URL | `""` | Yes (if using Redis) |
| `RETAIL_CHECKOUT_ENDPOINTS_ORDERS` | Orders API endpoint | `""` | No (uses mock if empty) |
| `RETAIL_CHECKOUT_SHIPPING_NAME_PREFIX` | Shipping option prefix | `""` | No |

**Example for Redis:**
```bash
RETAIL_CHECKOUT_PERSISTENCE_PROVIDER=redis
RETAIL_CHECKOUT_PERSISTENCE_REDIS_URL=redis://localhost:6379
RETAIL_CHECKOUT_ENDPOINTS_ORDERS=http://orders:8080
```

### 5. UI Service (Java - Port 8080)

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `PORT` | Server port | `8080` | No |
| `RETAIL_UI_ENDPOINTS_CATALOG` | Catalog service URL | - | No (uses defaults) |
| `RETAIL_UI_ENDPOINTS_CARTS` | Cart service URL | - | No (uses defaults) |
| `RETAIL_UI_ENDPOINTS_CHECKOUT` | Checkout service URL | - | No (uses defaults) |
| `RETAIL_UI_ENDPOINTS_ORDERS` | Orders service URL | - | No (uses defaults) |
| `RETAIL_UI_THEME` | UI theme | `default` | No |
| `RETAIL_UI_CHAT_ENABLED` | Enable chat feature | `false` | No |
| `RETAIL_UI_CHAT_PROVIDER` | Chat provider (mock, bedrock, openai) | - | No |

**Example:**
```bash
RETAIL_UI_ENDPOINTS_CATALOG=http://catalog:8080
RETAIL_UI_ENDPOINTS_CARTS=http://cart:8080
RETAIL_UI_ENDPOINTS_CHECKOUT=http://checkout:8080
RETAIL_UI_ENDPOINTS_ORDERS=http://orders:8080
```

## Running with Docker Compose

The easiest way to run the project is using Docker Compose. Each service has a `docker-compose.yml` file that includes database containers.

### Example: Running Cart Service

```bash
cd src/cart
docker compose up
```

This will automatically:
- Start DynamoDB Local
- Configure the cart service with the correct environment variables
- Set up health checks

### Example: Running All Services Locally

For local development, you can use the `in-memory` mode (no database needed):

```bash
# Cart Service - No DB needed
RETAIL_CART_PERSISTENCE_PROVIDER=in-memory

# Catalog Service - No DB needed
RETAIL_CATALOG_PERSISTENCE_PROVIDER=in-memory

# Orders Service - No DB needed
RETAIL_ORDERS_PERSISTENCE_PROVIDER=in-memory

# Checkout Service - No DB needed
RETAIL_CHECKOUT_PERSISTENCE_PROVIDER=in-memory
```

## Common Database Connection Strings

### DynamoDB (Cart Service)
- **Local**: `http://localhost:8000`
- **AWS**: `https://dynamodb.<region>.amazonaws.com`

### MySQL (Catalog Service)
- **Format**: `host:port` (e.g., `localhost:3306`)
- **Local**: `localhost:3306`
- **Docker**: `catalog-db:3306`

### PostgreSQL (Orders Service)
- **Format**: `host:port` (e.g., `localhost:5432`)
- **Local**: `localhost:5432`
- **Docker**: `orders-db:5432`

### Redis (Checkout Service)
- **Format**: `redis://host:port` (e.g., `redis://localhost:6379`)
- **Local**: `redis://localhost:6379`
- **Docker**: `redis://checkout-redis:6379`

## Summary

**For Quick Testing (No Database Setup):**
- Set all `*_PERSISTENCE_PROVIDER` variables to `in-memory`
- No database URLs needed
- Data will be lost on restart

**For Production/Development with Persistence:**
- Set `*_PERSISTENCE_PROVIDER` to the appropriate database type
- Provide database connection URLs/endpoints
- Provide database credentials
- Ensure databases are running and accessible

