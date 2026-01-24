# Retail Store Sample App - ChatGPT Deployment Guide

## Quick Project Summary for ChatGPT

**Project**: Retail Store Sample App - A microservices-based e-commerce application

**Current Status**: Running successfully with Docker Compose on Windows

**Goal**: Deploy to Minikube (local Kubernetes cluster)

---

## Project Architecture

### 5 Microservices:

1. **UI Service** (Java/Spring Boot)
   - Port: 8080
   - Frontend web application
   - Calls other services via HTTP
   - No database (stateless)

2. **Catalog Service** (Go/Gin)
   - Port: 8080
   - Product catalog API
   - Database: MySQL (optional, can use in-memory)

3. **Cart Service** (Java/Spring Boot)
   - Port: 8080
   - Shopping cart management
   - Database: DynamoDB (optional, can use in-memory)

4. **Checkout Service** (Node.js/NestJS)
   - Port: 8080
   - Checkout orchestration
   - Database: Redis (optional, can use in-memory)

5. **Orders Service** (Java/Spring Boot)
   - Port: 8080
   - Order management
   - Database: PostgreSQL
   - Messaging: RabbitMQ

### Key Feature: Dual Persistence Mode

**Each service supports TWO modes:**

1. **In-Memory Mode** (Default):
   - No database required
   - Set `*_PERSISTENCE_PROVIDER=in-memory`
   - Data lost on restart
   - Perfect for quick deployment

2. **Database Mode**:
   - Requires database connection
   - Set `*_PERSISTENCE_PROVIDER=<db-type>`
   - Provide database endpoint and credentials
   - Persistent storage

---

## Current Docker Compose Setup

**Services are running on these ports:**
- UI: `localhost:8888`
- Catalog: `localhost:8081`
- Cart: `localhost:8082`
- Orders: `localhost:8083`
- Checkout: `localhost:8085`

**Each service has:**
- `Dockerfile` - Container image
- `docker-compose.yml` - Local setup with databases
- `chart/` directory - Helm charts for Kubernetes
- Environment variable configuration

---

## What ChatGPT Needs to Know

### 1. Service Discovery in Kubernetes

Services communicate using Kubernetes service names:
- UI → Catalog: `http://catalog-service:8080`
- UI → Cart: `http://cart-service:8080`
- UI → Checkout: `http://checkout-service:8080`
- UI → Orders: `http://orders-service:8080`

### 2. Environment Variables Needed

**UI Service:**
```yaml
RETAIL_UI_ENDPOINTS_CATALOG=http://catalog-service:8080
RETAIL_UI_ENDPOINTS_CARTS=http://cart-service:8080
RETAIL_UI_ENDPOINTS_CHECKOUT=http://checkout-service:8080
RETAIL_UI_ENDPOINTS_ORDERS=http://orders-service:8080
```

**Catalog Service (if using MySQL):**
```yaml
RETAIL_CATALOG_PERSISTENCE_PROVIDER=mysql
RETAIL_CATALOG_PERSISTENCE_ENDPOINT=mysql-service:3306
RETAIL_CATALOG_PERSISTENCE_DB_NAME=catalogdb
RETAIL_CATALOG_PERSISTENCE_USER=catalog_user
RETAIL_CATALOG_PERSISTENCE_PASSWORD=<from-secret>
```

**Cart Service (if using DynamoDB):**
```yaml
RETAIL_CART_PERSISTENCE_PROVIDER=dynamodb
RETAIL_CART_PERSISTENCE_DYNAMODB_ENDPOINT=http://dynamodb-service:8000
RETAIL_CART_PERSISTENCE_DYNAMODB_CREATE_TABLE=true
```

**Checkout Service (if using Redis):**
```yaml
RETAIL_CHECKOUT_PERSISTENCE_PROVIDER=redis
RETAIL_CHECKOUT_PERSISTENCE_REDIS_URL=redis://redis-service:6379
```

**Orders Service (if using PostgreSQL):**
```yaml
RETAIL_ORDERS_PERSISTENCE_PROVIDER=postgres
RETAIL_ORDERS_PERSISTENCE_POSTGRES_ENDPOINT=postgres-service:5432
RETAIL_ORDERS_PERSISTENCE_POSTGRES_NAME=orders
RETAIL_ORDERS_PERSISTENCE_POSTGRES_USERNAME=orders_user
RETAIL_ORDERS_PERSISTENCE_POSTGRES_PASSWORD=<from-secret>
RETAIL_ORDERS_MESSAGING_PROVIDER=rabbitmq
RETAIL_ORDERS_MESSAGING_RABBITMQ_ADDRESSES=rabbitmq-service:5672
```

### 3. For In-Memory Mode (Simplest Deployment)

Set all services to in-memory:
```yaml
RETAIL_CATALOG_PERSISTENCE_PROVIDER=in-memory
RETAIL_CART_PERSISTENCE_PROVIDER=in-memory
RETAIL_CHECKOUT_PERSISTENCE_PROVIDER=in-memory
RETAIL_ORDERS_PERSISTENCE_PROVIDER=in-memory
RETAIL_ORDERS_MESSAGING_PROVIDER=in-memory
```

**No databases needed!** Perfect for quick Minikube deployment.

### 4. Helm Charts Available

Each service has Helm charts in `src/<service>/chart/`:
- `Chart.yaml` - Chart metadata
- `values.yaml` - Default values
- `templates/` - Kubernetes manifests

### 5. Health Checks

All services expose:
- Java services: `/actuator/health`
- Go service: `/health`
- Node service: `/health`

Use these for Kubernetes liveness/readiness probes.

---

## Deployment Strategy Options

### Option A: In-Memory Mode (Recommended for First Deployment)
- **Pros**: No database setup, fastest deployment
- **Cons**: Data lost on restart
- **Steps**:
  1. Deploy all 5 services
  2. Set all `*_PERSISTENCE_PROVIDER=in-memory`
  3. Use ClusterIP services for inter-service communication
  4. Expose UI via Ingress or NodePort

### Option B: With Databases (Production-Like)
- **Pros**: Persistent data, realistic setup
- **Cons**: More complex, requires StatefulSets or external DBs
- **Steps**:
  1. Deploy databases (MySQL, PostgreSQL, Redis, DynamoDB Local)
  2. Create Secrets for passwords
  3. Deploy services with database configuration
  4. Configure service discovery

---

## Key Files to Reference

1. **Helm Charts**: `src/<service>/chart/` - Already configured for Kubernetes
2. **Dockerfiles**: `src/<service>/Dockerfile` - Container images
3. **docker-compose.yml**: `src/<service>/docker-compose.yml` - Reference for env vars
4. **application.yml**: `src/<service>/src/main/resources/application.yml` - Default configs

---

## What to Ask ChatGPT

1. **"Help me deploy this microservices app to Minikube. Each service has Helm charts. Should I use the Helm charts or create Kubernetes manifests manually?"**

2. **"The app supports in-memory mode (no databases). What's the simplest way to deploy all 5 services to Minikube with in-memory persistence?"**

3. **"How do I configure service discovery in Kubernetes so the UI service can reach Catalog, Cart, Checkout, and Orders services?"**

4. **"The services need environment variables. Should I use ConfigMaps or set them in the Helm values.yaml files?"**

5. **"How do I expose the UI service externally in Minikube so I can access it from my browser?"**

6. **"If I want to add databases later, how should I deploy MySQL, PostgreSQL, Redis, and DynamoDB Local to Minikube?"**

---

## Current Working Configuration

**The app is currently running with:**
- All services in Docker containers
- Databases running in separate containers
- UI accessible at `http://localhost:8888`
- Services communicating via `host.docker.internal`

**For Minikube, we need to:**
- Replace `host.docker.internal` with Kubernetes service names
- Deploy services as Kubernetes Deployments
- Use Kubernetes Services for inter-service communication
- Expose UI via Ingress or NodePort

---

## Additional Context

- **Languages**: Java 21, Go 1.23+, Node.js 16+
- **Frameworks**: Spring Boot 3.5, Gin, NestJS
- **Container Images**: Already built, can be pushed to registry or built in Minikube
- **Observability**: Health endpoints, Prometheus metrics, OpenTelemetry support
- **Chaos Engineering**: Built-in endpoints for testing resilience

---

## Expected Minikube Architecture

```
Minikube Cluster
├── Namespace: retail-store (or default)
├── Deployments:
│   ├── ui-deployment
│   ├── catalog-deployment
│   ├── cart-deployment
│   ├── checkout-deployment
│   └── orders-deployment
├── Services (ClusterIP):
│   ├── ui-service:8080
│   ├── catalog-service:8080
│   ├── cart-service:8080
│   ├── checkout-service:8080
│   └── orders-service:8080
└── Ingress:
    └── ui-ingress (exposes UI externally)
```

**Optional (if using databases):**
- StatefulSets for MySQL, PostgreSQL, Redis
- Services for databases
- Secrets for passwords
- PersistentVolumes for data

---

This document provides ChatGPT with all the context needed to help deploy the application to Minikube!

