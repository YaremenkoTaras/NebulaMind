# Multi-stage Dockerfile for Fly.io deployment
# Builds trading-core and agent-builder in a single container
# Uses supervisord to run both services

FROM maven:3.9-amazoncorretto-21 AS build-trading-core
WORKDIR /app
COPY app/trading-core/pom.xml ./
COPY app/trading-core/src ./src
COPY app/trading-core/.mvn ./.mvn
RUN mvn clean package -DskipTests

FROM maven:3.9-amazoncorretto-21 AS build-agent-builder
WORKDIR /app
COPY app/agent-builder/pom.xml ./
COPY app/agent-builder/src ./src
COPY app/agent-builder/.mvn ./.mvn
RUN mvn clean package -DskipTests

# Runtime stage
FROM amazoncorretto:21-alpine

# Install supervisord, caddy and curl
RUN apk add --no-cache supervisor caddy curl

WORKDIR /app

# Copy built JARs
COPY --from=build-trading-core /app/target/trading-core-0.1.0-SNAPSHOT.jar /app/trading-core.jar
COPY --from=build-agent-builder /app/target/agent-builder-0.1.0-SNAPSHOT.jar /app/agent-builder.jar

# Create logs directory
RUN mkdir -p /app/logs

# Copy supervisord configuration
COPY cicd/fly/supervisord.conf /etc/supervisord.conf

# Copy Caddyfile
COPY cicd/fly/Caddyfile /etc/caddy/Caddyfile

# Expose port 8080 (Caddy will listen here)
EXPOSE 8080

# Start supervisord (manages trading-core, agent-builder, and caddy)
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]

