# Retail Store Sample App - Architecture & Deployment Guide

## Project Overview

The **Retail Store Sample App** is a modern, cloud-native e-commerce application built using microservices architecture. It demonstrates best practices for containerized applications, multi-language microservices, and Kubernetes deployment. The application simulates a retail store where users can browse products, add items to cart, and complete purchases.

### Key Features

- **Multi-language microservices**: Java, Go, and Node.js services
- **Flexible persistence**: Supports both in-memory and database-backed storage
- **Event-driven architecture**: Uses message queues for order processing
- **Cloud-native design**: Built for Kubernetes deployment
- **Health monitoring**: Includes health checks, metrics, and observability endpoints
- **Chaos engineering**: Built-in endpoints for testing resilience

---

## Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Service                           │
│                    (Java/Spring Boot)                       │
│                      Port: 8080                             │
└────────────┬────────────┬────────────┬────────────┬────────┘
             │            │            │            │
    ┌────────▼────┐ ┌────▼────┐ ┌────▼────┐ ┌────▼────┐
    │  Catalog    │ │   Cart   │ │ Checkout│ │  Orders │
    │  Service    │ │  Service │ │ Service  │ │ Service │
    │   (Go)      │ │  (Java)  │ │ (Node)   │ │ (Java)  │
    │  Port:8081  │ │ Port:8082│ │Port:8085 │ │Port:8083│
    └──────┬──────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘
           │             │             │             │
    ┌──────▼──────┐ ┌───▼──────┐ ┌───▼──────┐ ┌───▼──────┐
    │   MySQL     │ │ DynamoDB │ │  Redis   │ │PostgreSQL│
    │  (MariaDB)  │ │   Local   │ │          │ │          │
    └─────────────┘ └───────────┘ └──────────┘ └────┬──────┘
                                                    │
                                            ┌───────▼───────┐
                                            │   RabbitMQ    │
                                            │  (Messaging)  │
                                            └───────────────┘
```

### Service Communication Flow

1. **User Request Flow**:
   - User accesses UI → UI calls Catalog Service for products
   - User adds items → UI calls Cart Service
   - User checks out → UI calls Checkout Service
   - Checkout Service orchestrates → Calls Orders Service
   - Orders Service publishes events → RabbitMQ

2. **Data Flow**:
   - **Catalog**: Products stored in MySQL (or in-memory)
   - **Cart**: Cart items stored in DynamoDB (or in-memory)
   - **Checkout**: Session data in Redis (or in-memory)
   - **Orders**: Orders in PostgreSQL, events via RabbitMQ

---

## Microservices Breakdown

### 1. UI Service (Java/Spring Boot)
- **Port**: 8080 (mapped to 8888 in Docker)
- **Technology**: Spring Boot 3.5, Spring WebFlux (Reactive)
- **Purpose**: Frontend web application
- **Dependencies**: 
  - Calls Catalog, Cart, Checkout, and Orders services
  - No database (stateless)
- **Key Features**:
  - Reactive web interface
  - Product browsing
  - Shopping cart management
  - Checkout process
  - Order tracking

### 2. Catalog Service (Go)
- **Port**: 8080 (mapped to 8081 in Docker)
- **Technology**: Go 1.23+, Gin framework, GORM
- **Purpose**: Product catalog API
- **Persistence Options**:
  - **In-Memory**: Default, no database needed
  - **MySQL**: Requires MySQL/MariaDB connection
- **Database**: MySQL/MariaDB (optional)
- **Key Features**:
  - Product listing
  - Product details
  - Tag filtering
  - Database migrations (when using MySQL)

### 3. Cart Service (Java/Spring Boot)
- **Port**: 8080 (mapped to 8082 in Docker)
- **Technology**: Spring Boot 3.5, Spring MVC
- **Purpose**: Shopping cart management
- **Persistence Options**:
  - **In-Memory**: Default, no database needed
  - **DynamoDB**: Requires DynamoDB endpoint
- **Database**: Amazon DynamoDB (optional, can use DynamoDB Local)
- **Key Features**:
  - Add/remove items from cart
  - Cart retrieval
  - Cart updates
  - Auto-creates DynamoDB table if configured

### 4. Checkout Service (Node.js/NestJS)
- **Port**: 8080 (mapped to 8085 in Docker)
- **Technology**: Node.js 16+, NestJS, TypeScript
- **Purpose**: Checkout orchestration
- **Persistence Options**:
  - **In-Memory**: Default, no database needed
  - **Redis**: Requires Redis connection
- **Database**: Redis (optional)
- **Key Features**:
  - Checkout session management
  - Shipping options
  - Order submission orchestration
  - Can use mock Orders service if Orders service unavailable

### 5. Orders Service (Java/Spring Boot)
- **Port**: 8080 (mapped to 8083 in Docker)
- **Technology**: Spring Boot 3.5, Spring Data JPA, Flyway
- **Purpose**: Order management and processing
- **Persistence Options**:
  - **In-Memory**: Default, no database needed
  - **PostgreSQL**: Requires PostgreSQL connection
- **Database**: PostgreSQL (optional)
- **Messaging Options**:
  - **In-Memory**: Default
  - **RabbitMQ**: Requires RabbitMQ connection
  - **SQS**: AWS SQS (for cloud deployment)
- **Key Features**:
  - Order creation
  - Order retrieval
  - Event publishing (order created, cancelled)
  - Database migrations (when using PostgreSQL)

---

## How It Works Without Database URLs

### The Magic: Dual Persistence Providers

Each service supports **two persistence modes**:

1. **In-Memory Mode** (Default):
   - No database required
   - Data stored in application memory
   - Perfect for development/testing
   - Data lost on restart
   - Zero configuration needed

2. **Database Mode**:
   - Requires database connection
   - Persistent storage
   - Production-ready
   - Requires configuration

### Configuration Pattern

Each service uses environment variables to switch between modes:

```bash
# In-Memory Mode (Default - No DB needed)
RETAIL_CART_PERSISTENCE_PROVIDER=in-memory
RETAIL_CATALOG_PERSISTENCE_PROVIDER=in-memory
RETAIL_ORDERS_PERSISTENCE_PROVIDER=in-memory
RETAIL_CHECKOUT_PERSISTENCE_PROVIDER=in-memory

# Database Mode (Requires DB connection)
RETAIL_CART_PERSISTENCE_PROVIDER=dynamodb
RETAIL_CART_PERSISTENCE_DYNAMODB_ENDPOINT=http://dynamodb:8000

RETAIL_CATALOG_PERSISTENCE_PROVIDER=mysql
RETAIL_CATALOG_PERSISTENCE_ENDPOINT=mysql:3306
RETAIL_CATALOG_PERSISTENCE_PASSWORD=password
```

### Implementation Details

**Java Services (Cart, Orders, UI)**:
- Uses Spring Boot's `@ConditionalOnProperty` annotation
- Automatically switches between implementations based on configuration
- Example: `InMemoryCartService` vs `DynamoDBCartService`

**Go Service (Catalog)**:
- Uses factory pattern based on configuration
- Checks `RETAIL_CATALOG_PERSISTENCE_PROVIDER` environment variable
- Instantiates appropriate repository implementation

**Node.js Service (Checkout)**:
- Uses NestJS dependency injection
- Factory provider pattern
- Conditionally creates Redis or InMemory repository

---

## Technology Stack

### Languages & Frameworks
- **Java 21**: Cart, Orders, UI services
- **Go 1.23+**: Catalog service
- **Node.js 16+**: Checkout service
- **TypeScript**: Checkout service

### Frameworks & Libraries
- **Spring Boot 3.5**: Java services
- **Gin**: Go web framework
- **NestJS**: Node.js framework
- **GORM**: Go ORM
- **Spring Data JPA**: Java ORM
- **Flyway**: Database migrations

### Databases & Storage
- **MySQL/MariaDB**: Catalog service
- **DynamoDB**: Cart service
- **PostgreSQL**: Orders service
- **Redis**: Checkout service

### Messaging
- **RabbitMQ**: Order events
- **AWS SQS**: Cloud alternative

### Containerization
- **Docker**: Container images
- **Docker Compose**: Local development
- **Helm**: Kubernetes deployment

### Observability
- **Spring Actuator**: Health checks, metrics
- **Prometheus**: Metrics collection
- **OpenTelemetry**: Distributed tracing (optional)

---

## Deployment Architecture

### Current Setup (Docker Compose)

Each service runs in its own Docker container with optional database containers:

```
Service Containers:
├── ui-ui-1 (UI Service)
├── catalog-catalog-1 (Catalog Service)
├── catalog-catalog-db-1 (MySQL)
├── cart-cart-1 (Cart Service)
├── cart-carts-db-1 (DynamoDB Local)
├── checkout-checkout-1 (Checkout Service)
├── checkout-checkout-redis-1 (Redis)
├── orders-orders-1 (Orders Service)
├── orders-orders-db-1 (PostgreSQL)
└── orders-rabbitmq-1 (RabbitMQ)
```

### Network Architecture

- Each service has its own Docker network (created by docker-compose)
- Services communicate via:
  - **Docker service names** (when in same network)
  - **host.docker.internal** (to reach host machine)
  - **localhost** (when running directly, not in Docker)

---

## Minikube Deployment Architecture

### Kubernetes Components Needed

1. **Deployments**: One for each microservice
2. **Services**: ClusterIP services for inter-service communication
3. **ConfigMaps**: For non-sensitive configuration
4. **Secrets**: For database passwords, API keys
5. **StatefulSets**: For databases (optional, or use external DBs)
6. **Ingress**: For external access to UI service

### Recommended Minikube Setup

```
┌─────────────────────────────────────────────────────────────┐
│                      Minikube Cluster                       │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ UI Pod       │  │ Catalog Pod  │  │ Cart Pod     │     │
│  │ (Deployment) │  │ (Deployment) │  │ (Deployment) │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
│         │                  │                  │             │
│  ┌──────▼──────────────────▼──────────────────▼───────┐   │
│  │         Kubernetes Service (ClusterIP)             │   │
│  │  - ui-service:8080                                  │   │
│  │  - catalog-service:8080                            │   │
│  │  - cart-service:8080                               │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐                       │
│  │ Checkout Pod │  │ Orders Pod   │                       │
│  │ (Deployment) │  │ (Deployment) │                       │
│  └──────┬───────┘  └──────┬───────┘                       │
│         │                  │                               │
│  ┌──────▼──────────────────▼───────────────────────┐     │
│  │         Kubernetes Service (ClusterIP)           │     │
│  │  - checkout-service:8080                         │     │
│  │  - orders-service:8080                           │     │
│  └──────────────────────────────────────────────────┘     │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ MySQL Pod    │  │ Redis Pod    │  │ PostgreSQL   │     │
│  │(StatefulSet) │  │(StatefulSet) │  │(StatefulSet) │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Ingress Controller                     │   │
│  │         (NGINX or Minikube Ingress)                │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Service Discovery in Kubernetes

In Kubernetes, services discover each other using:
- **Service DNS**: `http://service-name.namespace.svc.cluster.local:port`
- **Short form**: `http://service-name:port` (same namespace)
- **Example**: UI calls Catalog via `http://catalog-service:8080`

### Environment Variables for Minikube

```yaml
# UI Service
RETAIL_UI_ENDPOINTS_CATALOG=http://catalog-service:8080
RETAIL_UI_ENDPOINTS_CARTS=http://cart-service:8080
RETAIL_UI_ENDPOINTS_CHECKOUT=http://checkout-service:8080
RETAIL_UI_ENDPOINTS_ORDERS=http://orders-service:8080

# Catalog Service
RETAIL_CATALOG_PERSISTENCE_PROVIDER=mysql
RETAIL_CATALOG_PERSISTENCE_ENDPOINT=mysql-service:3306
RETAIL_CATALOG_PERSISTENCE_PASSWORD=<from-secret>

# Cart Service
RETAIL_CART_PERSISTENCE_PROVIDER=dynamodb
RETAIL_CART_PERSISTENCE_DYNAMODB_ENDPOINT=http://dynamodb-service:8000

# Checkout Service
RETAIL_CHECKOUT_PERSISTENCE_PROVIDER=redis
RETAIL_CHECKOUT_PERSISTENCE_REDIS_URL=redis://redis-service:6379

# Orders Service
RETAIL_ORDERS_PERSISTENCE_PROVIDER=postgres
RETAIL_ORDERS_PERSISTENCE_POSTGRES_ENDPOINT=postgres-service:5432
RETAIL_ORDERS_MESSAGING_PROVIDER=rabbitmq
RETAIL_ORDERS_MESSAGING_RABBITMQ_ADDRESSES=rabbitmq-service:5672
```

---

## Key Design Patterns

### 1. **Provider Pattern**
Each service uses a provider pattern to switch between implementations:
- In-memory provider (default)
- Database provider (when configured)

### 2. **Configuration-Driven Behavior**
All behavior controlled via environment variables:
- No code changes needed to switch modes
- Easy to configure for different environments

### 3. **Health Checks**
All services expose health endpoints:
- `/health` or `/actuator/health`
- Used by Kubernetes for liveness/readiness probes

### 4. **Chaos Engineering**
Built-in endpoints for testing resilience:
- `/chaos/status/{code}` - Force HTTP errors
- `/chaos/latency/{ms}` - Add artificial delay
- `/chaos/health` - Fail health checks

### 5. **Graceful Degradation**
- Checkout service can use mock Orders service if Orders unavailable
- Services can fall back to in-memory mode if database unavailable

---

## Data Flow Example: Complete Purchase Flow

1. **User browses products**:
   ```
   Browser → UI Service → Catalog Service → MySQL (or in-memory)
   ```

2. **User adds to cart**:
   ```
   Browser → UI Service → Cart Service → DynamoDB (or in-memory)
   ```

3. **User initiates checkout**:
   ```
   Browser → UI Service → Checkout Service → Redis (or in-memory)
   ```

4. **Checkout submits order**:
   ```
   Checkout Service → Orders Service → PostgreSQL (or in-memory)
   ```

5. **Order event published**:
   ```
   Orders Service → RabbitMQ → (Future: Notification Service)
   ```

---

## Port Mappings

| Service  | Container Port | Host Port (Docker) | Kubernetes Service Port |
|----------|---------------|-------------------|-------------------------|
| UI       | 8080          | 8888              | 8080                    |
| Catalog  | 8080          | 8081              | 8080                    |
| Cart     | 8080          | 8082              | 8080                    |
| Orders   | 8080          | 8083              | 8080                    |
| Checkout | 8080          | 8085              | 8080                    |

---

## Deployment Considerations for Minikube

### Option 1: In-Memory Mode (Simplest)
- **Pros**: No database setup needed, quick deployment
- **Cons**: Data lost on pod restart, not production-ready
- **Use Case**: Development, testing, demos

### Option 2: Database Mode with StatefulSets
- **Pros**: Persistent data, production-like
- **Cons**: Requires persistent volumes, more complex
- **Use Case**: Production-like testing

### Option 3: External Databases
- **Pros**: Managed databases, better performance
- **Cons**: Requires external setup
- **Use Case**: Production deployment

### Recommended Minikube Setup Steps

1. **Start Minikube**:
   ```bash
   minikube start
   ```

2. **Enable Ingress** (for external access):
   ```bash
   minikube addons enable ingress
   ```

3. **Deploy Databases** (if using persistent mode):
   - MySQL StatefulSet
   - PostgreSQL StatefulSet
   - Redis StatefulSet
   - DynamoDB Local (or use external DynamoDB)

4. **Deploy Services**:
   - Create ConfigMaps for configuration
   - Create Secrets for passwords
   - Deploy services using Helm charts or manifests

5. **Configure Service Discovery**:
   - Use Kubernetes service names
   - Set environment variables accordingly

6. **Expose UI Service**:
   - Use Ingress or NodePort
   - Access via `minikube service ui-service`

---

## Project Structure

```
retail-store-sample-app/
├── src/
│   ├── ui/              # UI Service (Java)
│   ├── catalog/         # Catalog Service (Go)
│   ├── cart/            # Cart Service (Java)
│   ├── orders/          # Orders Service (Java)
│   └── checkout/        # Checkout Service (Node.js)
├── terraform/           # Infrastructure as Code
├── argocd/              # GitOps configurations
└── docs/                # Documentation
```

Each service contains:
- `Dockerfile` - Container image definition
- `docker-compose.yml` - Local development setup
- `chart/` - Helm charts for Kubernetes
- `src/` - Source code
- `README.md` - Service-specific documentation

---

## Summary

This project demonstrates:
- **Microservices architecture** with multiple languages
- **Flexible persistence** (in-memory or database)
- **Cloud-native design** (containerized, Kubernetes-ready)
- **Service communication** via HTTP REST APIs
- **Event-driven patterns** with message queues
- **Observability** with health checks and metrics

The application can run **completely without databases** using in-memory mode, making it perfect for quick demos and development. For production or persistent storage, simply configure the database endpoints via environment variables.

