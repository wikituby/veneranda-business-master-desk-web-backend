# ================================
# Build stage
# ================================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy Maven files first for better layer caching
COPY hospital/pom.xml ./hospital/

WORKDIR /app/hospital

# Download dependencies (cached layer if pom.xml doesn't change)
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY hospital/src ./src

# Build for Render: PostgreSQL reactive client + prod Quarkus profile
RUN mvn clean package -DskipTests -B -Preactive-pg,'!reactive-mysql' -Dquarkus.profile=prod

# ================================
# Runtime stage
# ================================
FROM eclipse-temurin:21-jre-jammy

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/hospital/target/*-runner.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port (Render will use this)
EXPOSE 8080

# Render health check: /health_care/health
# Set QUARKUS_PROFILE=prod and datasource env vars on the service

# Run with production-optimized JVM settings
# MaxRAMPercentage ensures JVM respects container memory limits
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", \
     "-jar", \
     "app.jar"]
