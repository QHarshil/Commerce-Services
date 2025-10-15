#!/bin/bash

set -euo pipefail

echo "🛑 Stopping Commerce Services..."

COMPOSE_PROJECT="commerce-services-dev"

# Stop Java processes launched directly
if [ -f logs/inventory.pid ]; then
    kill "$(cat logs/inventory.pid)" 2>/dev/null && echo "✅ Inventory Service stopped"
    rm logs/inventory.pid
fi

if [ -f logs/order.pid ]; then
    kill "$(cat logs/order.pid)" 2>/dev/null && echo "✅ Order Service stopped"
    rm logs/order.pid
fi

if [ -f logs/payment.pid ]; then
    kill "$(cat logs/payment.pid)" 2>/dev/null && echo "✅ Payment Service stopped"
    rm logs/payment.pid
fi

if [ -f logs/checkout.pid ]; then
    kill "$(cat logs/checkout.pid)" 2>/dev/null && echo "✅ Checkout Service stopped"
    rm logs/checkout.pid
fi

if [ -f logs/frontend.pid ]; then
    kill "$(cat logs/frontend.pid)" 2>/dev/null && echo "✅ Frontend stopped"
    rm logs/frontend.pid
fi

# Stop fallback standalone containers (if they were used)
docker stop commerce_postgres commerce_redis >/dev/null 2>&1 || true
docker rm commerce_postgres commerce_redis >/dev/null 2>&1 || true

# Bring down docker-compose stack if running
COMPOSE_SERVICES=("frontend" "checkout-service" "payment-service" "order-service" "inventory-service" "postgres" "redis" "kafka")
COMPOSE_RUNNING=false
for svc in "${COMPOSE_SERVICES[@]}"; do
    if COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT}" docker compose ps -q "$svc" >/dev/null 2>&1 && \
       [ -n "$(COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT}" docker compose ps -q "$svc" 2>/dev/null)" ]; then
        COMPOSE_RUNNING=true
        break
    fi
done

if [ "${COMPOSE_RUNNING}" = true ]; then
    echo "🐳 Docker Compose services detected – shutting them down..."
    COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT}" docker compose down
fi

echo ""
echo "🎉 All services stopped!"
