# NebulaMind Architecture

> Детальний опис архітектури LLM-керованого торгового бота

## Загальна схема

```
┌─────────────────────────────────────────────────────────────┐
│                        User / Scheduler                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Agent Console (UI)                        │
│                   React / Next.js (8083)                     │
│  • Portfolio monitoring                                      │
│  • Trade history                                             │
│  • LLM decision logs                                         │
│  • Risk metrics dashboard                                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   Agent Builder (8082)                       │
│                   LLM Orchestrator Layer                     │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  System Prompt: "You are TradingAgent..."             │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Tools Registry:                                       │ │
│  │  • getPortfolio()                                      │ │
│  │  • placeOrderSafe(symbol, side, qty, riskPolicy, ...) │ │
│  │  • cancelOrder(clientOrderId)                          │ │
│  │  • evaluateSignals(symbol)                             │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Guardrails / Policy:                                  │ │
│  │  • Validate SL/TP presence                             │ │
│  │  • Check max % equity                                  │ │
│  │  • Block unsafe operations                             │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Memory & Audit Logger                                 │ │
│  └────────────────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP API
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Trading Core (8081)                       │
│                  Spring Boot 3 / Java 21                     │
│                                                               │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  REST Controllers (/api/core/*)                        │ │
│  │  • GET  /portfolio                                     │ │
│  │  • POST /orders/placeSafe                              │ │
│  │  • POST /orders/cancel                                 │ │
│  │  • GET  /signal?symbol=BTCUSDT                         │ │
│  └────────────────────────────────────────────────────────┘ │
│                         │                                     │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Application Services                                  │ │
│  │  • OrderService                                        │ │
│  │  • PortfolioService                                    │ │
│  │  • SignalService                                       │ │
│  └────────────────────────────────────────────────────────┘ │
│                         │                                     │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Domain Layer (Ports & Adapters)                       │ │
│  │                                                         │ │
│  │  Ports (Interfaces):                                   │ │
│  │  • ExchangeGateway                                     │ │
│  │  • MarketDataFeed                                      │ │
│  │  • RiskManager                                         │ │
│  │  • OrderRouter                                         │ │
│  │                                                         │ │
│  │  Domain Models:                                        │ │
│  │  • Order, Position, Portfolio                          │ │
│  │  • Signal, RiskPolicy                                  │ │
│  └────────────────────────────────────────────────────────┘ │
│                         │                                     │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Infrastructure Adapters                               │ │
│  │                                                         │ │
│  │  Sandbox Adapter:                                      │ │
│  │  • FakeBinanceGateway (in-memory orderbook)            │ │
│  │  • MockMarketData (synthetic prices)                   │ │
│  │                                                         │ │
│  │  Real Binance Adapter:                                 │ │
│  │  • BinanceRestClient (API integration)                 │ │
│  │  • BinanceWebSocketFeed (real-time data)               │ │
│  └────────────────────────────────────────────────────────┘ │
│                         │                                     │
└─────────────────────────┼─────────────────────────────────────┘
                          │
                          ▼
                ┌──────────────────┐
                │   Binance API    │
                │  (Spot/Futures)  │
                └──────────────────┘
```

## Модулі та відповідальності

### 1. Trading Core (Java 21 / Spring Boot 3)

**Мета**: Реалізація всієї торгової логіки, незалежно від LLM.

#### Шари:

**API Layer (Controllers)**
- Expose REST endpoints для Agent Builder
- Request/Response validation
- Error handling (@ControllerAdvice)

**Application Layer (Services)**
- Business logic orchestration
- Transaction management
- Event publishing

**Domain Layer**
- Pure business models (Order, Position, Signal, etc.)
- Port interfaces (ExchangeGateway, RiskManager, etc.)
- Domain validation rules

**Infrastructure Layer (Adapters)**
- Concrete implementations of ports
- Binance API client (REST + WebSocket)
- Sandbox/Mock implementations
- Database repositories (if needed)

#### Key Components:

##### ExchangeGateway (Port)
```java
public interface ExchangeGateway {
    OrderResult placeOrder(OrderRequest request);
    void cancelOrder(String clientOrderId);
    Portfolio getPortfolio();
    ExchangeInfo getExchangeInfo(String symbol);
}
```

Implementations:
- `SandboxExchangeGateway` — для локального тестування
- `BinanceExchangeGateway` — для роботи з реальною біржею

##### RiskManager (Port)
```java
public interface RiskManager {
    ValidationResult validate(OrderRequest order, Portfolio portfolio);
    boolean isDailyLimitExceeded();
    void recordTrade(TradeResult trade);
}
```

Перевіряє:
- Max % equity per trade
- Stop-loss presence
- Daily loss limit
- Position size limits

##### OrderRouter
Координує процес виконання ордеру:
1. Normalize (lot size, price precision)
2. Validate via RiskManager
3. Submit to ExchangeGateway
4. Record result & emit events

##### MarketDataFeed (Port)
```java
public interface MarketDataFeed {
    Observable<Kline> subscribeKlines(String symbol, String interval);
    Observable<Trade> subscribeTrades(String symbol);
    Observable<OrderBook> subscribeDepth(String symbol);
}
```

##### SignalService
Генерує торгові сигнали на основі:
- Technical indicators
- Market data analysis
- Risk-adjusted recommendations

---

### 2. Agent Builder (Java або Node.js)

**Мета**: LLM runtime, який оркеструє виклики до Trading Core.

#### Components:

**Agent Runtime**
- Ініціалізація LLM з system prompt
- Execution loop: user input → LLM → tool calls → response

**Tools Registry**
Реєстр інструментів, доступних LLM:

```typescript
{
  "getPortfolio": {
    "description": "Get current portfolio snapshot",
    "input_schema": {},
    "handler": () => coreApi.get('/api/core/portfolio')
  },
  "placeOrderSafe": {
    "description": "Place order with risk checks",
    "input_schema": { /* JSON Schema */ },
    "handler": (args) => coreApi.post('/api/core/orders/placeSafe', args)
  },
  // ... інші tools
}
```

**Guardrails (Policy Layer)**
Pre-validation перед викликом tools:
- Перевіряє наявність riskPolicy в placeOrderSafe
- Блокує небезпечні параметри
- Логує всі спроби

**Memory Module**
- Короткострокова пам'ять (session context)
- Довгострокова пам'ять (trade history, lessons learned)

**Audit Logger**
- Логує всі LLM рішення
- Correlation ID для трейсинга
- Не логує секрети

---

### 3. Agent Console (React / Next.js)

**Мета**: Web UI для моніторингу та контролю.

#### Pages/Features:

**Dashboard**
- Portfolio summary (equity, free balance, positions)
- Current P&L
- Active orders table
- Real-time price charts

**Trading History**
- List of executed trades
- Filters: symbol, date range, side
- P&L per trade
- LLM reasoning logs

**LLM Control Panel**
- Send custom prompts to agent
- View conversation history
- Override/pause agent decisions

**Risk Metrics**
- Daily P&L chart
- Max drawdown
- Win rate, average win/loss
- Risk limit utilization

**Settings**
- Configure risk parameters
- API key management (masked)
- Profile switching (sandbox/live)

---

## Trading Flow детально

### Scenario 1: LLM ініціює угоду

```
1. User: "Check BTCUSDT and open a long if conditions are good"
2. LLM: [calls evaluateSignals({symbol: "BTCUSDT"})]
3. Agent Builder → GET /api/core/signal?symbol=BTCUSDT
4. Trading Core → SignalService.evaluate("BTCUSDT")
   - Fetch market data
   - Run technical analysis
   - Return Signal { action: BUY, confidence: 0.75, ... }
5. Trading Core → Response: { "action": "BUY", "qty": 0.01, "reason": "..." }
6. Agent Builder ← receives signal
7. LLM: analyzes signal, decides to place order
8. LLM: [calls placeOrderSafe({
     symbol: "BTCUSDT",
     side: "BUY",
     qty: 0.01,
     riskPolicy: { maxPctEquity: 2, stopLossPct: 1.5 },
     reason: "Strong uptrend on 4h chart"
   })]
9. Agent Builder → Guardrails: validate riskPolicy presence ✓
10. Agent Builder → POST /api/core/orders/placeSafe
11. Trading Core → OrderService.placeOrderSafe(...)
    a. Normalize qty/price (lot size, tick size)
    b. RiskManager.validate() → check limits ✓
    c. ExchangeGateway.placeOrder() → Binance API
    d. Record trade in audit log
12. Trading Core → Response: { "clientOrderId": "abc123", "status": "FILLED" }
13. Agent Builder ← receives order result
14. LLM → formats response to user
15. User ← "Order placed successfully. ClientOrderId: abc123. ..."
```

### Scenario 2: User через UI скасовує ордер

```
1. User clicks "Cancel" on order XYZ in UI
2. Agent Console → POST /api/agent/cancel { clientOrderId: "XYZ" }
3. Agent Builder → calls tool cancelOrder({clientOrderId: "XYZ"})
4. Agent Builder → POST /api/core/orders/cancel
5. Trading Core → OrderService.cancel("XYZ")
   - ExchangeGateway.cancelOrder("XYZ")
6. Trading Core → Response: { "status": "CANCELED" }
7. Agent Console ← updates order list (WebSocket event or polling)
```

---

## Configuration Profiles

### Sandbox Profile (default for local dev)
- `FakeBinanceGateway` (in-memory orderbook)
- Mock market data (deterministic prices)
- No real API keys needed
- Fast feedback loop

### Development Profile
- Binance Testnet (https://testnet.binance.vision)
- Real API, fake money
- Full flow testing

### Production Profile
- Real Binance API (https://api.binance.com)
- Real funds (⚠️ use with caution)
- Enhanced monitoring & alerts

---

## Data Flow: Market Data

```
Binance WebSocket
    │
    ▼
MarketDataFeed (adapter)
    │
    ├─→ Cache (recent klines, trades)
    │
    ├─→ SignalService (technical analysis)
    │
    └─→ WebSocket to Agent Console (real-time charts)
```

---

## Security Model

### Principle: Least Privilege

1. **Agent Builder** НЕ має прямого доступу до Binance API keys.
2. Всі торгові операції проходять через **Trading Core API** з валідацією.
3. **RiskManager** виконує guardrails на рівні Core (backup до LLM guardrails).
4. **Audit Logger** записує всі рішення LLM (для post-mortem аналізу).

### Authentication

- **Local/Dev**: немає auth (localhost only)
- **Fly.io/Production**: Bearer token (`ADMIN_TOKEN` в env) для UI та Agent endpoints
- Binance API keys в `trading-core` only, не передаються зовні

---

## Deployment Architecture (Fly.io)

### Single Container Approach

```
┌─────────────────────────────────────────────┐
│          Fly.io VM (1 GB RAM)               │
│                                             │
│  ┌────────────────────────────────────────┐│
│  │         Caddy (Port 8080)              ││
│  │  Reverse Proxy:                        ││
│  │  • /api/core/*  → localhost:8081       ││
│  │  • /api/agent/* → localhost:8082       ││
│  │  • /*           → localhost:8083 (UI)  ││
│  └────────────────────────────────────────┘│
│                    │                        │
│  ┌─────────────────┼──────────────────────┐│
│  │    Supervisord  │                      ││
│  │    ┌────────────▼──────┐              ││
│  │    │ trading-core      │ (Java)       ││
│  │    │ Port 8081         │              ││
│  │    └───────────────────┘              ││
│  │    ┌───────────────────┐              ││
│  │    │ agent-builder     │ (Java)       ││
│  │    │ Port 8082         │              ││
│  │    └───────────────────┘              ││
│  │    ┌───────────────────┐              ││
│  │    │ agent-console     │ (Node/serve) ││
│  │    │ Port 8083         │              ││
│  │    └───────────────────┘              ││
│  └───────────────────────────────────────┘│
└─────────────────────────────────────────────┘
```

### CI/CD Pipeline

```
Developer push to main
    ↓
GitHub Actions trigger
    ↓
Build all modules (Maven + npm)
    ↓
Build Docker image (multi-stage)
    ↓
Push to Fly.io registry
    ↓
Deploy to Fly.io
    ↓
Health check
    ↓
Job Summary with URL
```

---

## Technology Stack Summary

| Layer            | Technology                  |
|------------------|-----------------------------|
| Trading Core     | Java 21, Spring Boot 3      |
| Agent Builder    | Java 21 (or Node.js)        |
| Agent Console    | React 18 / Next.js 14       |
| Build (Java)     | Maven / Gradle              |
| Build (Node)     | npm / pnpm                  |
| Reverse Proxy    | Caddy                       |
| Process Manager  | Supervisord                 |
| Container        | Docker                      |
| CI/CD            | GitHub Actions              |
| Deployment       | Fly.io                      |
| LLM              | OpenAI GPT-4 (or compatible)|

---

## Performance Considerations

### Latency Budget

- LLM inference: ~1-3s (depends on model, prompt size)
- Core API response: <100ms (local) or <300ms (to Binance)
- WebSocket latency: <50ms (market data)

### Optimization Strategies

1. **Caching**: Market data, exchange info, recent signals
2. **Async Processing**: Non-blocking I/O for API calls
3. **Rate Limiting**: Respect Binance limits (1200 req/min)
4. **Batch Operations**: Multiple signals/checks in one cycle

---

## Monitoring & Observability

### Metrics to Track

- Orders placed/canceled per hour
- Win rate, avg P&L per trade
- LLM tool call frequency
- API error rate (Binance, OpenAI)
- System resources (CPU, memory)

### Logging Levels

- **ERROR**: Critical failures (API down, risk breach)
- **WARN**: Recoverable issues (rate limit approaching)
- **INFO**: Business events (order placed, signal generated)
- **DEBUG**: Detailed trace (for development)

### Tools

- Spring Boot Actuator (health, metrics)
- Structured logs (JSON format)
- Correlation IDs for request tracing

---

## Testing Strategy

### Unit Tests
- Domain models validation
- RiskManager logic
- SignalService calculations
- No external dependencies (mocked)

### Integration Tests
- Full Trading Core with Sandbox adapter
- Agent Builder tools calling Core API
- End-to-end order placement flow

### Golden Tests
- LLM input/output assertions
- Predefined prompts → expected tool calls
- Regression prevention

### Load Tests
- Concurrent order placements
- WebSocket subscription limits
- Memory leak detection

---

## Future Enhancements

- Multi-exchange support (Bybit, OKX)
- Advanced strategies (grid, DCA, arbitrage)
- Backtesting framework (historical replay)
- Portfolio optimization (Kelly criterion)
- Sentiment analysis (Twitter, news)
- Multi-agent collaboration (different strategies)

---

## Conclusion

NebulaMind архітектура забезпечує:
- **Модульність**: кожен компонент може розвиватись незалежно
- **Безпеку**: multiple layers of validation (LLM guardrails + Core risk manager)
- **Тестованість**: sandbox mode, мокування портів
- **Масштабованість**: легко додати нові біржі, стратегії, агентів
- **Спостережуваність**: детальні логи, метрики, аудит

Чиста архітектура (Ports & Adapters) дозволяє легко замінювати інфраструктурні компоненти без зміни бізнес-логіки.

