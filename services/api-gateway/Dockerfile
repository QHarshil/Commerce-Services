# Multi-stage build for optimized production image
FROM openjdk:11-jdk-slim as builder

WORKDIR /app

# Copy Maven files
COPY pom.xml ./
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:11-jre-slim

WORKDIR /app

# Create non-root user for security
RUN groupadd -r commerce && useradd -r -g commerce commerce

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown commerce:commerce app.jar

# Switch to non-root user
USER commerce

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "-Xmx512m", "-Xms256m", "app.jar"]
