#!/bin/bash

echo "🛑 Stopping Commerce Services..."

# Stop Java services
if [ -f logs/inventory.pid ]; then
    kill $(cat logs/inventory.pid) 2>/dev/null && echo "✅ Inventory Service stopped"
    rm logs/inventory.pid
fi

if [ -f logs/order.pid ]; then
    kill $(cat logs/order.pid) 2>/dev/null && echo "✅ Order Service stopped"
    rm logs/order.pid
fi

if [ -f logs/payment.pid ]; then
    kill $(cat logs/payment.pid) 2>/dev/null && echo "✅ Payment Service stopped"
    rm logs/payment.pid
fi

if [ -f logs/checkout.pid ]; then
    kill $(cat logs/checkout.pid) 2>/dev/null && echo "✅ Checkout Service stopped"
    rm logs/checkout.pid
fi

if [ -f logs/frontend.pid ]; then
    kill $(cat logs/frontend.pid) 2>/dev/null && echo "✅ Frontend stopped"
    rm logs/frontend.pid
fi

# Stop Docker containers
docker stop commerce_postgres commerce_redis 2>/dev/null
docker rm commerce_postgres commerce_redis 2>/dev/null

echo ""
echo "🎉 All services stopped!"
