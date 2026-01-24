# How to Run the Retail Store Sample App

This guide covers different ways to run the project, from simple local development to full production deployment.

## Table of Contents

1. [Quick Start - Run Individual Services](#quick-start---run-individual-services)
2. [Run All Services Locally with Docker Compose](#run-all-services-locally-with-docker-compose)
3. [Run Services Locally (Without Docker)](#run-services-locally-without-docker)
4. [Deploy to Kubernetes (Production)](#deploy-to-kubernetes-production)

---

## Quick Start - Run Individual Services

The easiest way to get started is to run each service individually using Docker Compose. Each service includes its database.

### Option 1: Run Cart Service

```bash
cd src/cart
docker compose up
```

**Access:** http://localhost:8082

**What it includes:**
- Cart service (Java/Spring Boot)
- DynamoDB Local database
- Auto-configured environment variables

### Option 2: Run Catalog Service

**For Linux/Mac/Git Bash:**
```bash
cd src/catalog
DB_PASSWORD="testing" docker compose up
```

**For Windows PowerShell:**
```powershell
cd src/catalog
$env:DB_PASSWORD="testing"
docker compose up
```

**For Windows CMD:**
```cmd
cd src/catalog
set DB_PASSWORD=testing
docker compose up
```

**Access:** http://localhost:8081

**What it includes:**
- Catalog service (Go)
- MySQL database
- Auto-configured environment variables

### Option 3: Run Orders Service

**For Linux/Mac/Git Bash:**
```bash
cd src/orders
DB_PASSWORD="testing" docker compose up
```

**For Windows PowerShell:**
```powershell
cd src/orders
$env:DB_PASSWORD="testing"
docker compose up
```

**For Windows CMD:**
```cmd
cd src/orders
set DB_PASSWORD=testing
docker compose up
```

**Access:** http://localhost:8083

**What it includes:**
- Orders service (Java/Spring Boot)
- PostgreSQL database
- RabbitMQ message broker
- Auto-configured environment variables

### Option 4: Run Checkout Service

```bash
cd src/checkout
docker compose up
```

**Access:** http://localhost:8085

**What it includes:**
- Checkout service (Node.js/NestJS)
- Redis cache
- Auto-configured environment variables

### Option 5: Run UI Service

```bash
cd src/ui
docker compose up
```

**Access:** http://localhost:8080

**What it includes:**
- UI service (Java/Spring Boot)
- Frontend application

---

## Run All Services Locally with Docker Compose

To run the complete application, you need to start all services. Here's a step-by-step guide:

### Prerequisites

- **Docker** and **Docker Compose** installed
- **Ports available:** 8080, 8081, 8082, 8083, 8085, 3306, 5432, 6379, 8000

### Step-by-Step Instructions

#### 1. Start Catalog Service (MySQL)

**Linux/Mac/Git Bash:**
```bash
cd src/catalog
DB_PASSWORD="testing" docker compose up -d
```

**Windows PowerShell:**
```powershell
cd src/catalog
$env:DB_PASSWORD="testing"
docker compose up -d
```

**Windows CMD:**
```cmd
cd src/catalog
set DB_PASSWORD=testing
docker compose up -d
```

Wait for it to be healthy, then verify:
```bash
curl http://localhost:8081/health
```

#### 2. Start Cart Service (DynamoDB)

```bash
cd src/cart
docker compose up -d
```

Verify:
```bash
curl http://localhost:8082/actuator/health
```

#### 3. Start Orders Service (PostgreSQL + RabbitMQ)

**Linux/Mac/Git Bash:**
```bash
cd src/orders
DB_PASSWORD="testing" docker compose up -d
```

**Windows PowerShell:**
```powershell
cd src/orders
$env:DB_PASSWORD="testing"
docker compose up -d
```

**Windows CMD:**
```cmd
cd src/orders
set DB_PASSWORD=testing
docker compose up -d
```

Verify:
```bash
curl http://localhost:8083/actuator/health
```

#### 4. Start Checkout Service (Redis)

```bash
cd src/checkout
docker compose up -d
```

Verify:
```bash
curl http://localhost:8085/health
```

#### 5. Start UI Service

**Important:** Configure UI to connect to other services.

Create a `.env` file in `src/ui/` or set environment variables:

```bash
cd src/ui
```

Set environment variables:

**Windows PowerShell:**
```powershell
$env:RETAIL_UI_ENDPOINTS_CATALOG="http://localhost:8081"
$env:RETAIL_UI_ENDPOINTS_CARTS="http://localhost:8082"
$env:RETAIL_UI_ENDPOINTS_CHECKOUT="http://localhost:8085"
$env:RETAIL_UI_ENDPOINTS_ORDERS="http://localhost:8083"
```

**Windows CMD:**
```cmd
set RETAIL_UI_ENDPOINTS_CATALOG=http://localhost:8081
set RETAIL_UI_ENDPOINTS_CARTS=http://localhost:8082
set RETAIL_UI_ENDPOINTS_CHECKOUT=http://localhost:8085
set RETAIL_UI_ENDPOINTS_ORDERS=http://localhost:8083
```

**Linux/Mac/Git Bash:**
```bash
export RETAIL_UI_ENDPOINTS_CATALOG=http://localhost:8081
export RETAIL_UI_ENDPOINTS_CARTS=http://localhost:8082
export RETAIL_UI_ENDPOINTS_CHECKOUT=http://localhost:8085
export RETAIL_UI_ENDPOINTS_ORDERS=http://localhost:8083
```

Then start UI:
```bash
docker compose up
```

**Access the application:** http://localhost:8080

### Stop All Services

```bash
# Stop each service
cd src/catalog && docker compose down
cd src/cart && docker compose down
cd src/orders && docker compose down
cd src/checkout && docker compose down
cd src/ui && docker compose down
```

Or stop all at once:
```bash
docker ps -q --filter "name=catalog\|cart\|orders\|checkout\|ui" | xargs docker stop
```

---

## Run Services Locally (Without Docker)

If you prefer to run services directly without Docker, follow these instructions:

### Prerequisites

- **Java 21** (for Cart, Orders, UI services)
- **Go 1.21+** (for Catalog service)
- **Node.js 16+** and **Yarn** (for Checkout service)
- **MySQL** (for Catalog service)
- **PostgreSQL** (for Orders service)
- **Redis** (for Checkout service)
- **DynamoDB Local** (for Cart service) - Optional, can use in-memory mode

### 1. Cart Service (Java)

```bash
cd src/cart

# Option A: In-memory mode (no database needed)
./mvnw spring-boot:run

# Option B: With DynamoDB Local
# First, start DynamoDB Local:
docker run -d -p 8000:8000 amazon/dynamodb-local:1.20.0

# Then run with environment variables:
export RETAIL_CART_PERSISTENCE_PROVIDER=dynamodb
export RETAIL_CART_PERSISTENCE_DYNAMODB_ENDPOINT=http://localhost:8000
export RETAIL_CART_PERSISTENCE_DYNAMODB_CREATE_TABLE=true
export AWS_ACCESS_KEY_ID=dummy
export AWS_SECRET_ACCESS_KEY=dummy
./mvnw spring-boot:run
```

**Access:** http://localhost:8080

### 2. Catalog Service (Go)

```bash
cd src/catalog

# Option A: In-memory mode (no database needed)
export RETAIL_CATALOG_PERSISTENCE_PROVIDER=in-memory
go run main.go

# Option B: With MySQL
# Linux/Mac:
export RETAIL_CATALOG_PERSISTENCE_PROVIDER=mysql
export RETAIL_CATALOG_PERSISTENCE_ENDPOINT=localhost:3306
export RETAIL_CATALOG_PERSISTENCE_DB_NAME=catalogdb
export RETAIL_CATALOG_PERSISTENCE_USER=catalog_user
export RETAIL_CATALOG_PERSISTENCE_PASSWORD=your-password
go run main.go

# Windows PowerShell:
$env:RETAIL_CATALOG_PERSISTENCE_PROVIDER="mysql"
$env:RETAIL_CATALOG_PERSISTENCE_ENDPOINT="localhost:3306"
$env:RETAIL_CATALOG_PERSISTENCE_DB_NAME="catalogdb"
$env:RETAIL_CATALOG_PERSISTENCE_USER="catalog_user"
$env:RETAIL_CATALOG_PERSISTENCE_PASSWORD="your-password"
go run main.go
```

**Access:** http://localhost:8080

### 3. Orders Service (Java)

```bash
cd src/orders

# Option A: In-memory mode (no database needed)
./mvnw spring-boot:run

# Option B: With PostgreSQL
export RETAIL_ORDERS_PERSISTENCE_PROVIDER=postgres
export RETAIL_ORDERS_PERSISTENCE_POSTGRES_ENDPOINT=localhost:5432
export RETAIL_ORDERS_PERSISTENCE_POSTGRES_NAME=orders
export RETAIL_ORDERS_PERSISTENCE_POSTGRES_USERNAME=orders_user
export RETAIL_ORDERS_PERSISTENCE_POSTGRES_PASSWORD=your-password
./mvnw spring-boot:run
```

**Access:** http://localhost:8080

### 4. Checkout Service (Node.js)

```bash
cd src/checkout

# Install dependencies (first time only)
yarn install

# Option A: In-memory mode (no database needed)
yarn start

# Option B: With Redis
export RETAIL_CHECKOUT_PERSISTENCE_PROVIDER=redis
export RETAIL_CHECKOUT_PERSISTENCE_REDIS_URL=redis://localhost:6379
yarn start
```

**Access:** http://localhost:8080

### 5. UI Service (Java)

```bash
cd src/ui

# Set service endpoints
# Linux/Mac:
export RETAIL_UI_ENDPOINTS_CATALOG=http://localhost:8081
export RETAIL_UI_ENDPOINTS_CARTS=http://localhost:8082
export RETAIL_UI_ENDPOINTS_CHECKOUT=http://localhost:8085
export RETAIL_UI_ENDPOINTS_ORDERS=http://localhost:8083

# Windows PowerShell:
$env:RETAIL_UI_ENDPOINTS_CATALOG="http://localhost:8081"
$env:RETAIL_UI_ENDPOINTS_CARTS="http://localhost:8082"
$env:RETAIL_UI_ENDPOINTS_CHECKOUT="http://localhost:8085"
$env:RETAIL_UI_ENDPOINTS_ORDERS="http://localhost:8083"

./mvnw spring-boot:run
```

**Access:** http://localhost:8080

---

## Deploy to Kubernetes (Production)

For production deployment on AWS EKS, follow the main README instructions:

### Prerequisites

- AWS CLI configured
- Terraform installed
- kubectl installed
- Helm installed

### Steps

1. **Configure AWS:**
   ```bash
   aws configure
   ```

2. **Deploy Infrastructure:**
   ```bash
   cd terraform
   terraform init
   terraform apply --auto-approve
   ```

3. **Update kubeconfig:**
   ```bash
   aws eks update-kubeconfig --name retail-store --region <your-region>
   ```

4. **Access Application:**
   ```bash
   kubectl get svc -n ingress-nginx
   ```
   Use the EXTERNAL-IP to access the application.

For detailed instructions, see the main [README.md](./README.md).

---

## Service Ports Summary

| Service  | Port | Database Port | Database Type |
|----------|------|---------------|---------------|
| UI       | 8080 | -             | -             |
| Catalog  | 8081 | 3306          | MySQL         |
| Cart     | 8082 | 8000          | DynamoDB      |
| Orders   | 8083 | 5432          | PostgreSQL    |
| Checkout | 8085 | 6379          | Redis         |

---

## Troubleshooting

### Port Already in Use

If a port is already in use, either:
1. Stop the service using that port
2. Change the port mapping in `docker-compose.yml`

### Services Can't Connect

- Ensure all services are running
- Check that environment variables are set correctly
- Verify database containers are healthy: `docker ps`

### Database Connection Errors

- Wait for databases to be fully started (check health endpoints)
- Verify database credentials match
- Check network connectivity between containers

### Health Check Failures

Check service logs:
```bash
docker compose logs <service-name>
```

---

## Quick Reference Commands

```bash
# Start a service
cd src/<service-name>
docker compose up

# Start in background
docker compose up -d

# View logs
docker compose logs -f

# Stop service
docker compose down

# Rebuild and start
docker compose up --build

# Check service health
curl http://localhost:<port>/health
# or
curl http://localhost:<port>/actuator/health  # for Java services
```

---

## Next Steps

- Read [ENVIRONMENT_VARIABLES.md](./ENVIRONMENT_VARIABLES.md) for detailed configuration
- Check individual service READMEs in `src/<service>/README.md`
- Explore the API endpoints using the OpenAPI specs in each service

