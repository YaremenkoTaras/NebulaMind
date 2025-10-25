#!/bin/bash
# Script to restart NebulaMind services locally
# Usage: 
#   ./restart-local.sh          - restart all services
#   ./restart-local.sh core     - restart only trading-core
#   ./restart-local.sh agent    - restart only agent-builder
#   ./restart-local.sh both     - restart both services (same as no args)

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TRADING_CORE_DIR="${PROJECT_ROOT}/app/trading-core"
AGENT_BUILDER_DIR="${PROJECT_ROOT}/app/agent-builder"

# Parse arguments
RESTART_CORE=true
RESTART_AGENT=true

if [ "$1" == "core" ]; then
    RESTART_AGENT=false
elif [ "$1" == "agent" ]; then
    RESTART_CORE=false
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   NebulaMind Service Restart${NC}"
echo -e "${BLUE}========================================${NC}"

# Stop services
echo -e "${YELLOW}Stopping services...${NC}"
pkill -f "trading-core-0.1.0-SNAPSHOT.jar" 2>/dev/null || echo "Trading Core not running"
pkill -f "agent-builder-0.1.0-SNAPSHOT.jar" 2>/dev/null || echo "Agent Builder not running"
sleep 2

# Build and restart trading-core
if [ "$RESTART_CORE" = true ]; then
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Building trading-core...${NC}"
    echo -e "${GREEN}========================================${NC}"
    cd "${TRADING_CORE_DIR}"
    ./mvnw-java21.sh package -DskipTests -q
    
    echo -e "${GREEN}Starting trading-core (port 8081)...${NC}"
    java -jar target/trading-core-0.1.0-SNAPSHOT.jar \
        --server.port=8081 \
        > /tmp/trading-core.log 2>&1 &
    CORE_PID=$!
    echo $CORE_PID > /tmp/trading-core.pid
    echo -e "${GREEN}✓ Trading Core started (PID: ${CORE_PID})${NC}"
    
    # Wait for trading-core
    echo -n "Waiting for trading-core to be ready"
    for i in {1..20}; do
        if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
            echo -e " ${GREEN}✓${NC}"
            break
        fi
        if [ $i -eq 20 ]; then
            echo -e " ${RED}✗ Failed to start${NC}"
            exit 1
        fi
        echo -n "."
        sleep 1
    done
fi

# Build and restart agent-builder
if [ "$RESTART_AGENT" = true ]; then
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Building agent-builder...${NC}"
    echo -e "${GREEN}========================================${NC}"
    cd "${AGENT_BUILDER_DIR}"
    ./mvnw-java21.sh package -DskipTests -q
    
    echo -e "${GREEN}Starting agent-builder (port 8082)...${NC}"
    java -jar target/agent-builder-0.1.0-SNAPSHOT.jar \
        --server.port=8082 \
        > /tmp/agent-builder.log 2>&1 &
    AGENT_PID=$!
    echo $AGENT_PID > /tmp/agent-builder.pid
    echo -e "${GREEN}✓ Agent Builder started (PID: ${AGENT_PID})${NC}"
    
    # Wait for agent-builder
    echo -n "Waiting for agent-builder to be ready"
    for i in {1..20}; do
        if curl -s http://localhost:8082/api/agent/health > /dev/null 2>&1; then
            echo -e " ${GREEN}✓${NC}"
            break
        fi
        if [ $i -eq 20 ]; then
            echo -e " ${RED}✗ Failed to start${NC}"
            exit 1
        fi
        echo -n "."
        sleep 1
    done
fi

# Print status
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   Services restarted successfully!${NC}"
echo -e "${GREEN}========================================${NC}"

if [ "$RESTART_CORE" = true ]; then
    echo -e "${GREEN}Trading Core:${NC}    http://localhost:8081"
    echo -e "${YELLOW}  Logs:${NC}         tail -f /tmp/trading-core.log"
fi

if [ "$RESTART_AGENT" = true ]; then
    echo -e "${GREEN}Agent Builder:${NC}   http://localhost:8082"
    echo -e "${YELLOW}  Logs:${NC}         tail -f /tmp/agent-builder.log"
fi

echo -e "${BLUE}========================================${NC}"

