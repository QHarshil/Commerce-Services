#!/bin/bash

echo "🚀 Starting Commerce Services Locally"
echo ""

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please install Docker first."
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 11+ first."
    exit 1
fi

# Build services first
echo "📦 Building all services..."
./build.sh
if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

echo ""
echo "🗄️  Starting infrastructure services..."

# Start PostgreSQL and Redis with Docker
docker compose up -d postgres redis 2>/dev/null || {
    echo "⚠️  Docker Compose failed. Trying alternative approach..."
    
    # Alternative: Start individual containers
    docker run -d --name commerce_postgres \
        -e POSTGRES_DB=commerce \
        -e POSTGRES_USER=commerce \
        -e POSTGRES_PASSWORD=commerce123 \
        -p 5432:5432 \
        -v $(pwd)/infrastructure/init-db.sql:/docker-entrypoint-initdb.d/init-db.sql \
        postgres:15-alpine 2>/dev/null || echo "⚠️  PostgreSQL container may already be running"
    
    docker run -d --name commerce_redis \
        -p 6379:6379 \
        redis:7-alpine 2>/dev/null || echo "⚠️  Redis container may already be running"
}

echo "⏳ Waiting for database to be ready..."
sleep 10

echo ""
echo "🎯 Starting Commerce Services..."

# Start services in background
echo "📦 Starting Inventory Service (port 8081)..."
cd services/inventory-service
java -jar target/*.jar --server.port=8081 --spring.profiles.active=docker > ../../logs/inventory.log 2>&1 &
INVENTORY_PID=$!
cd ../..

echo "📦 Starting Order Service (port 8082)..."
cd services/order-service
java -jar target/*.jar --server.port=8082 --spring.profiles.active=docker > ../../logs/order.log 2>&1 &
ORDER_PID=$!
cd ../..

echo "📦 Starting Payment Service (port 8083)..."
cd services/payment-service
java -jar target/*.jar --server.port=8083 --spring.profiles.active=docker > ../../logs/payment.log 2>&1 &
PAYMENT_PID=$!
cd ../..

echo "📦 Starting Checkout Service (port 8084)..."
cd services/checkout-service
java -jar target/*.jar --server.port=8084 --spring.profiles.active=docker > ../../logs/checkout.log 2>&1 &
CHECKOUT_PID=$!
cd ../..

echo "📦 Starting Frontend (port 3000)..."
cd frontend
python3 server.py > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

# Create logs directory
mkdir -p logs

# Save PIDs for cleanup
echo $INVENTORY_PID > logs/inventory.pid
echo $ORDER_PID > logs/order.pid
echo $PAYMENT_PID > logs/payment.pid
echo $CHECKOUT_PID > logs/checkout.pid
echo $FRONTEND_PID > logs/frontend.pid

echo ""
echo "⏳ Waiting for services to start..."
sleep 15

echo ""
echo "🎉 Commerce Services Started!"
echo ""
echo "📊 Service URLs:"
echo "   Frontend Dashboard: http://localhost:3000"
echo "   Inventory Service:  http://localhost:8081"
echo "   Order Service:      http://localhost:8082"
echo "   Payment Service:    http://localhost:8083"
echo "   Checkout Service:   http://localhost:8084"
echo ""
echo "🔍 Health Check:"
curl -s http://localhost:8081/api/v1/health 2>/dev/null && echo "✅ Inventory Service: UP" || echo "❌ Inventory Service: DOWN"
curl -s http://localhost:8082/api/v1/health 2>/dev/null && echo "✅ Order Service: UP" || echo "❌ Order Service: DOWN"
curl -s http://localhost:8083/api/v1/health 2>/dev/null && echo "✅ Payment Service: UP" || echo "❌ Payment Service: DOWN"
curl -s http://localhost:8084/api/v1/checkout/health 2>/dev/null && echo "✅ Checkout Service: UP" || echo "❌ Checkout Service: DOWN"
curl -s http://localhost:3000 2>/dev/null && echo "✅ Frontend: UP" || echo "❌ Frontend: DOWN"

echo ""
echo "🛑 To stop all services, run: ./stop-local.sh"
echo "📋 To view logs, check the logs/ directory"
echo ""
echo "🌐 Open http://localhost:3000 to start testing!"
