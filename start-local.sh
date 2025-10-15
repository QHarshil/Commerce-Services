#!/bin/bash

set -euo pipefail

echo "🚀 Starting Commerce Services Locally"
echo ""

if ! command -v docker >/dev/null 2>&1; then
    echo "❌ Docker not found. Please install Docker Desktop first."
    exit 1
fi

USE_COMPOSE_RUNTIME=false
if ! command -v java >/dev/null 2>&1; then
    echo "⚠️  Java runtime not detected – backend services will run via Docker Compose."
    USE_COMPOSE_RUNTIME=true
fi

COMPOSE_PROJECT="commerce-services-dev"

echo "📦 Building all services..."
./build.sh
echo ""

if [ "${USE_COMPOSE_RUNTIME}" = true ]; then
    echo "🐳 Bringing up microservices and frontend via Docker Compose..."
    COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT}" docker compose up -d --build \
        inventory-service \
        order-service \
        payment-service \
        checkout-service \
        frontend
    echo ""
    echo "ℹ️  Services are running inside containers. Use 'COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT} docker compose logs -f' to follow logs."
else
    mkdir -p logs

    echo "🗄️  Ensuring infrastructure services are running..."

    COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT}" docker compose up -d postgres redis kafka >/dev/null 2>&1 || {
        echo "⚠️  Docker Compose failed for infra. Attempting standalone containers..."

        WORKDIR_PATH="$PWD"
        if command -v cygpath >/dev/null 2>&1; then
            WORKDIR_PATH="$(cygpath -w "$PWD")"
        fi

        docker run -d --name commerce_postgres \
            -e POSTGRES_DB=commerce \
            -e POSTGRES_USER=commerce \
            -e POSTGRES_PASSWORD=commerce123 \
            -p 5432:5432 \
            -v "${WORKDIR_PATH}/infrastructure/init-db.sql:/docker-entrypoint-initdb.d/init-db.sql" \
            postgres:15-alpine >/dev/null 2>&1 || echo "⚠️  PostgreSQL container may already be running"

        docker run -d --name commerce_redis \
            -p 6379:6379 \
            redis:7-alpine >/dev/null 2>&1 || echo "⚠️  Redis container may already be running"
    }

    echo "⏳ Waiting for data stores to be ready..."
    sleep 10

    echo ""
    echo "🎯 Starting Commerce Services..."

    echo "🧠 Using local Java runtime"

    pushd services/inventory-service >/dev/null
    java -jar target/*.jar --server.port=8081 --spring.profiles.active=docker > ../../logs/inventory.log 2>&1 &
    INVENTORY_PID=$!
    popd >/dev/null

    pushd services/order-service >/dev/null
    java -jar target/*.jar --server.port=8082 --spring.profiles.active=docker > ../../logs/order.log 2>&1 &
    ORDER_PID=$!
    popd >/dev/null

    pushd services/payment-service >/dev/null
    java -jar target/*.jar --server.port=8083 --spring.profiles.active=docker > ../../logs/payment.log 2>&1 &
    PAYMENT_PID=$!
    popd >/dev/null

    pushd services/checkout-service >/dev/null
    java -jar target/*.jar --server.port=8084 --spring.profiles.active=docker > ../../logs/checkout.log 2>&1 &
    CHECKOUT_PID=$!
    popd >/dev/null

    pushd frontend >/dev/null
    python3 server.py > ../logs/frontend.log 2>&1 &
    FRONTEND_PID=$!
    popd >/dev/null

    echo "${INVENTORY_PID}" > logs/inventory.pid
    echo "${ORDER_PID}" > logs/order.pid
    echo "${PAYMENT_PID}" > logs/payment.pid
    echo "${CHECKOUT_PID}" > logs/checkout.pid
    echo "${FRONTEND_PID}" > logs/frontend.pid
fi

echo ""
echo "⏳ Waiting for services to start..."
sleep 25

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
curl -s http://localhost:8081/actuator/health >/dev/null && echo "✅ Inventory Service: UP" || echo "❌ Inventory Service: DOWN"
curl -s http://localhost:8082/actuator/health >/dev/null && echo "✅ Order Service: UP" || echo "❌ Order Service: DOWN"
curl -s http://localhost:8083/actuator/health >/dev/null && echo "✅ Payment Service: UP" || echo "❌ Payment Service: DOWN"
curl -s http://localhost:8084/actuator/health >/dev/null && echo "✅ Checkout Service: UP" || echo "❌ Checkout Service: DOWN"
curl -s http://localhost:3000 >/dev/null && echo "✅ Frontend: UP" || echo "❌ Frontend: DOWN"

echo ""
echo "🛑 To stop all services, run: ./stop-local.sh"
echo "📋 Dockerised logs: COMPOSE_PROJECT_NAME=${COMPOSE_PROJECT} docker compose logs -f"
echo ""
echo "🌐 Open http://localhost:3000 to start testing!"
