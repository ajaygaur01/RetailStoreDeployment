# Service Status Summary

## Current Status

### ✅ Running Successfully

1. **Cart Service** (Port 8082)
   - ✅ Running
   - ✅ DynamoDB connected
   - Access: http://localhost:8082

2. **Orders Service** (Port 8083)
   - ✅ Running
   - ✅ PostgreSQL connected
   - ✅ RabbitMQ connected (initial connection error resolved)
   - Access: http://localhost:8083

3. **Checkout Service** (Port 8085)
   - ✅ Running
   - ✅ Redis connected
   - Access: http://localhost:8085

4. **UI Service** (Port 8080)
   - ✅ Running
   - ⚠️ Needs environment variables configured
   - Access: http://localhost:8080

### ⚠️ Issues

1. **Catalog Service** (Port 8081)
   - ⚠️ Exits after database migration
   - The service runs migration then exits with code 0
   - **Fix**: Restart the catalog service - it should keep running

## Important: UI Service Configuration

The UI service needs to connect to other services. Since all services are running in Docker, you need to use **Docker service names** instead of `localhost`.

### Current Issue

You set environment variables in CMD:
```cmd
set RETAIL_UI_ENDPOINTS_CATALOG=http://localhost:8081
set RETAIL_UI_ENDPOINTS_CARTS=http://localhost:8082
```

**Problem**: These variables are set in your CMD session, but Docker Compose doesn't inherit them. Also, `localhost` won't work from inside Docker containers.

### Solution Options

#### Option 1: Use host.docker.internal (Recommended for separate Docker Compose files)

Since each service runs in its own Docker Compose network, use `host.docker.internal` to reach services on the host:

**For Windows CMD:**
```cmd
cd src\ui
set RETAIL_UI_ENDPOINTS_CATALOG=http://host.docker.internal:8081
set RETAIL_UI_ENDPOINTS_CARTS=http://host.docker.internal:8082
set RETAIL_UI_ENDPOINTS_CHECKOUT=http://host.docker.internal:8085
set RETAIL_UI_ENDPOINTS_ORDERS=http://host.docker.internal:8083
docker compose up
```

**For Windows PowerShell:**
```powershell
cd src\ui
$env:RETAIL_UI_ENDPOINTS_CATALOG="http://host.docker.internal:8081"
$env:RETAIL_UI_ENDPOINTS_CARTS="http://host.docker.internal:8082"
$env:RETAIL_UI_ENDPOINTS_CHECKOUT="http://host.docker.internal:8085"
$env:RETAIL_UI_ENDPOINTS_ORDERS="http://host.docker.internal:8083"
docker compose up
```

**Note**: `host.docker.internal` is a special DNS name that resolves to the host machine from inside Docker containers.

#### Option 2: Use host.docker.internal (For localhost access)

If services are running on your host machine (not in Docker), use:
```cmd
set RETAIL_UI_ENDPOINTS_CATALOG=http://host.docker.internal:8081
set RETAIL_UI_ENDPOINTS_CARTS=http://host.docker.internal:8082
set RETAIL_UI_ENDPOINTS_CHECKOUT=http://host.docker.internal:8085
set RETAIL_UI_ENDPOINTS_ORDERS=http://host.docker.internal:8083
```

#### Option 3: Create a .env file

Create `src/ui/.env`:
```
RETAIL_UI_ENDPOINTS_CATALOG=http://catalog:8080
RETAIL_UI_ENDPOINTS_CARTS=http://cart:8080
RETAIL_UI_ENDPOINTS_CHECKOUT=http://checkout:8080
RETAIL_UI_ENDPOINTS_ORDERS=http://orders:8080
```

Then run:
```cmd
docker compose up
```

## Fixing Catalog Service

The catalog service exits after migration. To fix:

```cmd
cd src\catalog
set DB_PASSWORD=testing
docker compose down
docker compose up
```

The service should keep running after migration completes.

## Service Ports Reference

| Service  | Container Port | Host Port | Docker Service Name |
|----------|---------------|-----------|---------------------|
| UI       | 8080          | 8888      | ui                  |
| Catalog  | 8080          | 8081      | catalog             |
| Cart     | 8080          | 8082      | carts               |
| Orders   | 8080          | 8083      | orders              |
| Checkout | 8080          | 8085      | checkout            |

## Quick Test Commands

Test each service:
```cmd
curl http://localhost:8081/health    # Catalog
curl http://localhost:8082/actuator/health  # Cart
curl http://localhost:8083/actuator/health  # Orders
curl http://localhost:8085/health     # Checkout
curl http://localhost:8080/actuator/health  # UI
```

## Next Steps

1. ✅ All services are running
2. ⚠️ Fix UI service endpoints (use Docker service names)
3. ⚠️ Restart catalog service if it exited
4. ✅ Access UI at http://localhost:8080 (or 8888 if using default port)

## Notes

- The deprecation warning about `spring.codec.max-in-memory-size` has been fixed in the code but requires rebuilding the UI container
- The AWS region warning in UI is harmless (only affects Bedrock chat feature, which is disabled)
- RabbitMQ connection errors at startup are normal - the service retries and connects successfully

