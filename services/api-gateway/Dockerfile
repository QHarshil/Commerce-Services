# Multi-stage build for optimized production image
FROM eclipse-temurin:21-jdk-alpine as builder

WORKDIR /app

# Copy Maven files
COPY pom.xml ./
COPY src ./src

# Install Maven
RUN apk add --no-cache maven

# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S commerce && adduser -S commerce -G commerce

# Install curl for health checks
RUN apk add --no-cache curl

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
