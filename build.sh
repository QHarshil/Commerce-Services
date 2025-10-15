#!/bin/bash

set -euo pipefail

echo "🛠️  Building Commerce Services..."
echo ""

if ! command -v docker >/dev/null 2>&1; then
    echo "❌ Docker is required to build with Java 21. Please install Docker Desktop."
    exit 1
fi

USE_LOCAL_MAVEN=true
if ! command -v mvn >/dev/null 2>&1 || ! command -v java >/dev/null 2>&1; then
    USE_LOCAL_MAVEN=false
fi

if [ "${USE_LOCAL_MAVEN}" = true ]; then
    echo "📦 Using local Maven installation"
    mvn -B clean package -DskipTests
else
    echo "🐳 Local Java/Maven not detected – running containerised build (maven:3.9.9-eclipse-temurin-21)"

    WORKSPACE_PATH="$PWD"
    if command -v cygpath >/dev/null 2>&1; then
        # Convert to Windows path for Docker Desktop when running via Git Bash
        WORKSPACE_PATH="$(cygpath -w "$PWD")"
    fi

    docker run --rm \
        -v "${WORKSPACE_PATH}:/workspace" \
        -w /workspace \
        maven:3.9.9-eclipse-temurin-21 \
        mvn -B clean package -DskipTests
fi

echo ""
echo "🎉 All services built successfully!"
echo "📦 Artifacts available under services/*/target"
