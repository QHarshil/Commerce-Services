#!/bin/bash

echo "🛠️  Building Commerce Services..."
echo ""

# Build all services
services=("inventory-service" "checkout-service" "order-service" "payment-service")

for service in "${services[@]}"; do
    echo "📦 Building $service..."
    cd services/$service
    mvn clean package -DskipTests -q
    if [ $? -eq 0 ]; then
        echo "✅ $service built successfully"
    else
        echo "❌ $service build failed"
        exit 1
    fi
    cd ../..
done

echo ""
echo "🎉 All services built successfully!"
echo "🚀 Run 'docker-compose up' to start the system"
