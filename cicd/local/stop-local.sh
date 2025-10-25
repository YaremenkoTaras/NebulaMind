#!/bin/bash
# Script to stop NebulaMind services started by run-local.sh

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

echo -e "${YELLOW}Stopping NebulaMind services...${NC}"

# Read PIDs from files
if [ -f "${PROJECT_ROOT}/.trading-core.pid" ]; then
    TRADING_CORE_PID=$(cat "${PROJECT_ROOT}/.trading-core.pid")
    echo -e "${YELLOW}Stopping trading-core (PID: ${TRADING_CORE_PID})...${NC}"
    kill $TRADING_CORE_PID 2>/dev/null && echo -e "${GREEN}Trading Core stopped${NC}" || echo -e "${RED}Trading Core not running${NC}"
    rm -f "${PROJECT_ROOT}/.trading-core.pid"
fi

if [ -f "${PROJECT_ROOT}/.agent-builder.pid" ]; then
    AGENT_BUILDER_PID=$(cat "${PROJECT_ROOT}/.agent-builder.pid")
    echo -e "${YELLOW}Stopping agent-builder (PID: ${AGENT_BUILDER_PID})...${NC}"
    kill $AGENT_BUILDER_PID 2>/dev/null && echo -e "${GREEN}Agent Builder stopped${NC}" || echo -e "${RED}Agent Builder not running${NC}"
    rm -f "${PROJECT_ROOT}/.agent-builder.pid"
fi

# Also try to stop by port (fallback)
if lsof -ti:8081 > /dev/null 2>&1; then
    echo -e "${YELLOW}Found process on port 8081, stopping...${NC}"
    kill $(lsof -ti:8081) 2>/dev/null && echo -e "${GREEN}Port 8081 freed${NC}"
fi

if lsof -ti:8082 > /dev/null 2>&1; then
    echo -e "${YELLOW}Found process on port 8082, stopping...${NC}"
    kill $(lsof -ti:8082) 2>/dev/null && echo -e "${GREEN}Port 8082 freed${NC}"
fi

echo -e "${GREEN}All services stopped${NC}"

