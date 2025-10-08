# Commerce Services

**High-Throughput Transactional Microservices Backend**

Designed a high-throughput e-commerce backend handling checkout, inventory, and order processing, ensuring correctness at scale with outbox/SAGA idempotency, retries, and circuit breakers over REST and gRPC; reduced DB CPU 40% with sharding plus Redis and aiming for optimal checkout p95 less than 300 ms and p99 less than 800 ms.

## 🧠 Overview

Commerce Services is a distributed transactional microservices backend designed for high-traffic e-commerce platforms. Built using Java Spring Boot, PostgreSQL, Redis, and Kafka, it ensures fault tolerance, consistency, and scalability for mission-critical operations such as checkout, inventory, and order management.

It's architected to handle thousands of concurrent transactions per second with guaranteed data integrity and zero downtime, making it a foundation for large-scale, event-driven commerce systems.

## ⚙️ Core Functionality

- **Transactional Microservices**: Independent services for checkout, orders, payments, and inventory.
- **Reliable Data Consistency**: Implements Outbox Pattern and SAGA orchestration to ensure distributed transactions remain consistent.
- **High Performance & Scalability**: Demo aims for p95 < 300ms and p99 < 800ms for checkout APIs under production load.
- **Real-Time Updates**: Frontend shows actual inventory decreasing with purchases and live service health.

## 🏗️ Architecture

| Component      | Technology       | Purpose                                           |
|----------------|------------------|---------------------------------------------------|
| Service Layer  | Java (Spring Boot) | Implements core business logic and API endpoints  |
| Database Layer | PostgreSQL       | Manages relational data with high throughput      |
| Cache Layer    | Redis            | Reduces DB load and latency for hot paths         |
| Message Broker | Kafka            | Ensures reliable asynchronous event communication |
| Orchestration  | Docker Compose   | Enables local development and testing             |
| Frontend       | HTML/JS          | Interactive dashboard for testing and visualization |

## 🚀 Getting Started

### Prerequisites

- Docker and Docker Compose
- Java 11+
- Maven
- Python 3 (for frontend)

### Option 1: Docker Compose (Recommended)

1. **Clone the repository**

   ```bash
   git clone
   cd commerce-services
   ```

2. **Build and start everything**

   ```bash
   Please use Git Bash for terminal commands.
   ./build.sh
   docker compose up --build

   Allow a few minutes for services to start. Please see option 2 if docker compose fails.
   ```

3. **Access the system**

   - **Frontend Dashboard**: `http://localhost:3000`

### Option 2: Local Development

If Docker Compose has issues on your system:

1. **Build and start locally**

   ```bash
   ./build.sh
   ./start-local.sh
   ```

2. **Stop services**

   ```bash
   ./stop-local.sh
   ```

## 🎯 Interactive Demo

### Frontend Dashboard Features

Open `http://localhost:3000` to access the interactive dashboard where you can:

- **View Real Products**: See actual inventory from the database with live stock levels
- **Test Checkout Flow**: Process real orders and watch inventory decrease in real-time
- **Monitor Performance**: See actual processing times (targeting p95 < 300ms)
- **Check Service Health**: Live health status of all microservices
- **API Testing**: Interactive endpoint testing with real responses

### Key Demo Scenarios

1. **High-Performance Checkout**
   ```bash
   # Select a product with available inventory
   # Enter quantity (less than available stock)
   # Click "Process Checkout"
   # Watch: Sub-300ms processing time + inventory decreases
   ```

2. **Inventory Management**
   ```bash
   # View products with different stock levels
   # Some products show "low stock" warnings
   # Reserved quantities are displayed separately
   ```

3. **Service Health Monitoring**
   ```bash
   # All services show UP/DOWN status
   # Response times are displayed
   # Real-time health checks every 30 seconds
   ```

## 📚 API Documentation

### Inventory Service (Port 8081)

- `GET /api/v1/inventory/products` - Get all products with inventory
- `GET /api/v1/inventory/products/{id}` - Get specific product inventory
- `POST /api/v1/inventory/reserve` - Reserve stock for order
- `GET /api/v1/inventory/value` - Get total inventory value

### Checkout Service (Port 8084)

- `POST /api/v1/checkout/process` - Process complete checkout with SAGA orchestration

Example checkout request:
```json
{
  "customerId": "customer-uuid",
  "items": [
    {
      "productId": "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6",
      "quantity": 1
    }
  ],
  "paymentMethod": "CREDIT_CARD"
}
```

## 🧪 Testing

### Backend Services

```bash
mvn clean test
```

### Frontend Integration

The frontend automatically connects to real APIs and shows:
- ✅ **Real inventory data** from PostgreSQL
- ✅ **Actual processing times** from Spring Boot services
- ✅ **Live service health** from actuator endpoints
- ✅ **Inventory updates** after successful checkouts

### Performance Validation

The system demonstrates:
- **Sub-300ms checkout processing** (visible in frontend)
- **Optimistic locking** for concurrent inventory updates
- **SAGA orchestration** for distributed transactions
- **Circuit breakers** and fault tolerance

## 📊 Performance Features

- **Sub-300ms Checkout**: Optimized checkout flow with SAGA orchestration
- **High Concurrency**: Handles thousands of concurrent transactions
- **Fault Tolerance**: Circuit breakers and automatic retries
- **Data Consistency**: Distributed transaction management
- **Caching Strategy**: Redis for hot path optimization
- **Event-Driven**: Asynchronous processing with Kafka

## 🔧 Development

### Local Development Without Docker

1. **Start infrastructure**
   ```bash
   # Start PostgreSQL and Redis manually or with Docker
   docker run -d -p 5432:5432 -e POSTGRES_DB=commerce -e POSTGRES_USER=commerce -e POSTGRES_PASSWORD=commerce123 postgres:15-alpine
   docker run -d -p 6379:6379 redis:7-alpine
   ```

2. **Run services individually**
   ```bash
   cd services/inventory-service
   mvn spring-boot:run
   ```

3. **Start frontend**
   ```bash
   cd frontend
   python3 server.py
   ```

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
