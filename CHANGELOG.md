# Changelog

All notable changes to the NebulaMind project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added - 2025-10-26 (Arbitrage System Improvements)
- **Circuit Breaker Pattern** for failed arbitrage chains
  * Automatic blacklisting after 3 consecutive failures
  * 5-minute timeout before retry
  * Prevents repeated execution of problematic chains
- **Adaptive Slippage Protection**
  * Configurable slippage tolerance (default 1.0%)
  * Only executes chains with profit > (minProfit + slippageTolerance)
  * Reduces losses from market slippage
- **Smart Order Sizing**
  * Maximum 20% of budget per trade
  * Smaller amounts for new/untested chains
  * Larger amounts for proven profitable chains
- **Chain Success Tracking**
  * Real-time statistics per chain pattern
  * Success rate, average slippage tracking
  * Informs smart sizing decisions
- **Emergency Stop Conditions**
  * Auto-stop when total loss > 5% of budget
  * Auto-stop when net profit < -2%
  * Auto-stop after 5 consecutive losses
  * Protects against large drawdowns
- **Retry Logic with Exponential Backoff**
  * Automatic retry for 5xx server errors
  * Max 2 attempts with 1 second delay
  * Better handling of temporary failures
- **Enhanced Task Tracking**
  * Consecutive wins/losses counter
  * Maximum drawdown tracking
  * Stopped reason for emergency stops
- **Advanced Task Parameters**: slippageTolerance, maxLossPerTrade, enableCircuitBreaker, enableSmartSizing
- **Detailed specification document**: spec/arbitrage-improvements.md

### Changed - 2025-10-26 (Arbitrage System Improvements)
- **ArbitrageTaskExecutor** completely rewritten with intelligent filtering and risk management
- **Task entity** enhanced with runtime tracking fields (consecutiveLosses, consecutiveWins, maxDrawdown, stoppedReason)
- **Frontend TaskStatistics** enhanced with 4-column dashboard including Streak/Drawdown widget
- **Improved logging** with detailed execution metrics (expected vs actual profit, slippage calculations)

### Fixed - 2025-10-26 (Arbitrage System Improvements)
- High failure rate (39% → expected 10-15% with improvements)
- Excessive slippage losses (1-2% per trade)
- Lack of protection against repeated failures
- No risk management for capital allocation

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
  * docs/QUICKSTART.md with quick start guide
  * docs/GIT_WORKFLOW.md with Git workflow and GitHub best practices
  * CONTRIBUTING.md with contribution guidelines
  * PROJECT_STATUS.md with current project status
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

