# Changelog

All notable changes to the NebulaMind project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial project structure and monorepo scaffold
- README.md with project overview and quick start guide
- CHANGELOG.md for tracking project changes
- .gitignore with comprehensive exclusions for Java, Node, Docker
- Directory structure: app/ (trading-core, agent-builder, agent-console), docs/, cicd/
- Trading Core module (Java 21 / Spring Boot 3):
  * REST API for portfolio, orders, signals
  * Risk management service with configurable limits
  * DTO classes for API communication
  * Unit tests for controllers
  * Configuration profiles: sandbox, development, production
  * Custom exceptions for validation and risk management
- Agent Builder module (Java 21 / Spring Boot 3):
  * TradingCoreClient for HTTP communication with Trading Core
  * TradingTools with 4 core tools: getPortfolio, placeOrderSafe, cancelOrder, evaluateSignals
  * Guardrails implementation (mandatory risk policy, stop loss, reasoning)
  * REST API controller for agent operations
  * Configuration for LLM integration (OpenAI)
  * Unit tests for controllers
- Local infrastructure:
  * docker-compose.yml for running all services in containers
  * Dockerfile.trading-core with multi-stage build
  * Dockerfile.agent-builder with multi-stage build
  * run-local.sh script for running without Docker
  * stop-local.sh script for graceful shutdown
  * Health checks for all services
- Documentation:
  * docs/ARCHITECTURE.md with detailed system design
  * docs/DEPLOY-SPEC-FLYIO.md with deployment specification
  * cicd/local/env.example with configuration examples
- Java 21 configuration via jenv for local development
- Maven settings for using Maven Central repository

### Changed
- N/A

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- N/A

---

## Version History

<!-- Future releases will be documented here -->

## Notes

- **Date format**: YYYY-MM-DD
- **Commit convention**: feat/fix/docs/style/refactor/test/chore
- Each entry should include: what changed, why it changed (if non-obvious), and any migration notes

