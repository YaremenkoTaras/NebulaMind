#!/bin/bash
# Script to run all tests

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

echo -e "${GREEN}Running all tests for NebulaMind...${NC}\n"

# Test trading-core
echo -e "${GREEN}Testing trading-core...${NC}"
cd "${PROJECT_ROOT}/app/trading-core"
if mvn test; then
    echo -e "${GREEN}✓ Trading Core tests passed${NC}\n"
else
    echo -e "${RED}✗ Trading Core tests failed${NC}"
    exit 1
fi

# Test agent-builder
echo -e "${GREEN}Testing agent-builder...${NC}"
cd "${PROJECT_ROOT}/app/agent-builder"
if mvn test; then
    echo -e "${GREEN}✓ Agent Builder tests passed${NC}\n"
else
    echo -e "${RED}✗ Agent Builder tests failed${NC}"
    exit 1
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}All tests passed successfully!${NC}"
echo -e "${GREEN}========================================${NC}"

