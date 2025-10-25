#!/bin/bash
# Script to run NebulaMind services locally without Docker

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ENV_FILE="${PROJECT_ROOT}/.env"
TRADING_CORE_DIR="${PROJECT_ROOT}/app/trading-core"
AGENT_BUILDER_DIR="${PROJECT_ROOT}/app/agent-builder"
LOGS_DIR="${PROJECT_ROOT}/logs"

# Create logs directory
mkdir -p "${LOGS_DIR}"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   NebulaMind Local Startup${NC}"
echo -e "${GREEN}========================================${NC}"

# Check if .env file exists
if [ ! -f "${ENV_FILE}" ]; then
    echo -e "${YELLOW}Warning: .env file not found${NC}"
    echo -e "${YELLOW}Creating .env from env.example...${NC}"
    cp "${PROJECT_ROOT}/cicd/local/env.example" "${ENV_FILE}"
    echo -e "${YELLOW}Please edit .env file and add your API keys${NC}"
fi

# Load environment variables
if [ -f "${ENV_FILE}" ]; then
    echo -e "${GREEN}Loading environment variables from .env${NC}"
    export $(grep -v '^#' "${ENV_FILE}" | xargs)
fi

# Set JAVA_HOME to Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
echo -e "${GREEN}Using Java: $JAVA_HOME${NC}"

# Build trading-core
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Building trading-core...${NC}"
echo -e "${GREEN}========================================${NC}"
cd "${TRADING_CORE_DIR}"
mvn clean package -DskipTests

# Build agent-builder
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Building agent-builder...${NC}"
echo -e "${GREEN}========================================${NC}"
cd "${AGENT_BUILDER_DIR}"
mvn clean package -DskipTests

# Start trading-core in background
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Starting trading-core (port 8081)...${NC}"
echo -e "${GREEN}========================================${NC}"
cd "${TRADING_CORE_DIR}"
java -jar target/trading-core-0.1.0-SNAPSHOT.jar \
    --server.port=${CORE_PORT:-8081} \
    > "${LOGS_DIR}/trading-core.log" 2>&1 &
TRADING_CORE_PID=$!
echo -e "${GREEN}Trading Core PID: ${TRADING_CORE_PID}${NC}"

# Wait for trading-core to start
echo "Waiting for trading-core to be ready..."
for i in {1..30}; do
    if curl -s http://localhost:${CORE_PORT:-8081}/api/core/health > /dev/null 2>&1; then
        echo -e "${GREEN}Trading Core is ready!${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}Trading Core failed to start${NC}"
        kill $TRADING_CORE_PID 2>/dev/null || true
        exit 1
    fi
    echo -n "."
    sleep 1
done

# Start agent-builder in background
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Starting agent-builder (port 8082)...${NC}"
echo -e "${GREEN}========================================${NC}"
cd "${AGENT_BUILDER_DIR}"
java -jar target/agent-builder-0.1.0-SNAPSHOT.jar \
    --server.port=${AGENT_PORT:-8082} \
    > "${LOGS_DIR}/agent-builder.log" 2>&1 &
AGENT_BUILDER_PID=$!
echo -e "${GREEN}Agent Builder PID: ${AGENT_BUILDER_PID}${NC}"

# Wait for agent-builder to start
echo "Waiting for agent-builder to be ready..."
for i in {1..30}; do
    if curl -s http://localhost:${AGENT_PORT:-8082}/api/agent/health > /dev/null 2>&1; then
        echo -e "${GREEN}Agent Builder is ready!${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}Agent Builder failed to start${NC}"
        kill $TRADING_CORE_PID $AGENT_BUILDER_PID 2>/dev/null || true
        exit 1
    fi
    echo -n "."
    sleep 1
done

# Print status
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   All services started successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Trading Core:    ${NC}http://localhost:${CORE_PORT:-8081}"
echo -e "${GREEN}Agent Builder:   ${NC}http://localhost:${AGENT_PORT:-8082}"
echo -e "${YELLOW}Logs directory:  ${NC}${LOGS_DIR}"
echo ""
echo -e "${YELLOW}To stop services, press Ctrl+C or run:${NC}"
echo -e "${YELLOW}  kill ${TRADING_CORE_PID} ${AGENT_BUILDER_PID}${NC}"
echo ""

# Save PIDs to file for cleanup script
echo $TRADING_CORE_PID > "${PROJECT_ROOT}/.trading-core.pid"
echo $AGENT_BUILDER_PID > "${PROJECT_ROOT}/.agent-builder.pid"

# Wait for Ctrl+C
trap "echo -e '\n${YELLOW}Stopping services...${NC}'; kill $TRADING_CORE_PID $AGENT_BUILDER_PID 2>/dev/null; rm -f '${PROJECT_ROOT}/.trading-core.pid' '${PROJECT_ROOT}/.agent-builder.pid'; echo -e '${GREEN}Services stopped${NC}'" INT TERM

# Tail logs
echo -e "${YELLOW}Tailing logs... (press Ctrl+C to stop)${NC}"
tail -f "${LOGS_DIR}/trading-core.log" "${LOGS_DIR}/agent-builder.log" &
TAIL_PID=$!

wait $TAIL_PID

