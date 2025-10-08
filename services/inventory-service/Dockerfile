# Simple single-stage build
FROM openjdk:11-jre-slim

WORKDIR /app

# Copy pre-built JAR (will be built locally)
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
