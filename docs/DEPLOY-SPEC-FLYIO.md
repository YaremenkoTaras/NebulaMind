# DEPLOY-SPEC-FLYIO.md — Повна специфікація: Торговий бот на базі Agent Builder + деплой на Fly.io

> **Мета:** створити один репозиторій із модульною архітектурою, який реалізує **LLM‑керованого торгового бота** (через Agent‑Builder) для крипторинку, з можливістю запуску локально та в хмарі (Fly.io). Документ містить:
> 1. технічні вимоги до реалізації самого бота;  
> 2. вимоги до агентного рівня;  
> 3. опис CI/CD і деплою на Fly.io;  
> 4. коміт‑план і структуру документації.

---

## 1️⃣ Основна ідея

Створюється **LLM‑керований торговий бот**, де:
- **LLM (Agent Builder)** виступає як мозок‑оркестратор: аналізує ринок, ухвалює рішення, викликає інструменти;  
- **Trading Core** реалізує всі низькорівневі функції: доступ до біржі (через інтерфейси), ордер‑менеджмент, ризики, симуляція;  
- **Agent Console (UI)** — браузерний фронт для контролю, логів і експериментів;  
- **Fly.io CI/CD** забезпечує автодеплой після кожного пушу в `main`.

---

## 2️⃣ Вимоги до реалізації торгового бота

### 2.1 Архітектура та модулі
```
repo-root/
├─ app/
│  ├─ trading-core/        # Java 21 / Spring Boot 3
│  ├─ agent-builder/       # Agent Builder runtime (Java або Node)
│  └─ agent-console/       # Next.js або React + Vite
│
├─ docs/                   # документація
├─ cicd/                   # CI/CD і деплой
└─ README.md
```

### 2.2 Порти/адаптери Trading Core
- **ExchangeGateway** — REST‑клієнт до Binance (пізніше — Futures/Perp).  
- **MarketDataFeed** — WebSocket стріми (trades, klines, depth).  
- **RiskManager** — guardrails перед кожним ордером.  
- **OrderRouter** — валідація → ризик → виконання.  
- **Sandbox реалізація** для локального тесту (емуляція ордербука, latency, fee).

### 2.3 Логіка ризику
- max % equity per trade
- daily loss limit
- forbid average‑down
- mandatory SL/TP
- auto‑pause при 429/418/time‑skew.

### 2.4 Trading Flow (узагальнений)
```
LLM → [evaluateSignals()] → Signal
LLM → [placeOrderSafe()] → RiskManager.validate() → ExchangeGateway.placeOrder()
User → UI → LLM або Core API
```

### 2.5 Agent Builder інтеграція
- Інструменти (`tools`):
  - `getPortfolio`
  - `placeOrderSafe`
  - `cancelOrder`
  - `evaluateSignals`
- Guardrails (policy): не дозволяє угоди без SL/TP чи понад % equity.  
- Agent працює як локальний або контейнерний runtime, що спілкується з `trading-core` по HTTP (`/api/core/*`).

Приклад декларації агент‑інструментів (Java DSL):
```java
Agent tradingAgent = AgentBuilder.create("TradingAgent")
  .systemPrompt("You are a cautious trading assistant.")
  .addTool("getPortfolio", ctx -> coreApi.getPortfolio())
  .addTool("placeOrderSafe", ctx -> coreApi.placeOrder(ctx.args()))
  .policy(new RiskGuard(maxExposurePct=5, stopLossPct=2))
  .memory(new LocalMemory())
  .logger(new AuditLogger("llm-trades.log"))
  .build();
```

### 2.6 Навчання та тестування
- **Sandbox Mode**: локальна пісочниця без реальних грошей.  
- **Test Signals**: історичні дані (CSV/Parquet) → бектест.  
- **LLM‑Evaluation Loop**: зберігати рішення, промпти, PnL у логах.  
- **Unit/Contract тести** для портів і стратегій.

---

## 3️⃣ CI/CD і деплой (Fly.io Variant A)

### 3.1 Концепція
- один контейнер із трьома процесами: `trading-core`, `agent-builder`, `agent-console`.  
- Caddy → маршрутизує запити:
  - `/api/core/*` → 8081
  - `/api/agent/*` → 8082
  - `/` → 8083 (UI)

### 3.2 Dockerfile
(ідентичний до попередньої версії: Java 21 + Node + Caddy + Supervisor)

### 3.3 Fly.io config (`cicd/fly/fly.toml`)
- app name, region, internal_port 8080
- env vars (ключі, профілі)
- healthcheck (GET /)

### 3.4 GitHub Action (`cicd/github-actions/deploy-flyio.yml`)
- build core (JAR) + agent (JAR) + console (build)
- build Docker image → push → fly deploy
- після деплою додає URL у Job Summary

### 3.5 Secrets
`FLY_API_TOKEN`, `BINANCE_API_KEY`, `BINANCE_API_SECRET`, `OPENAI_API_KEY` зберігаються у GitHub Secrets.

### 3.6 Ціна інфраструктури
- Fly.io 1 GB RAM — ≈ $5–6/міс.  
- Static IP (опц.) — $2/міс.  
- Разом MVP ≈ **$7–8/міс.**  

---

## 4️⃣ Локальний запуск (розробка)

### 4.1 Сценарій без Docker
```bash
cp cicd/local/env.example .env
./gradlew :app:trading-core:bootJar :app:agent-builder:shadowJar
(cd app/agent-console && npm i && npm run build)
./cicd/local/run-local.sh
```
UI — <http://localhost:8083>

### 4.2 Через Docker Compose
```bash
docker compose -f cicd/local/docker-compose.yml up --build
```
UI — <http://localhost:8080>

---

## 5️⃣ Коміт‑план (Cursor)

| № | Тип | Опис |
|:-:|:-|:-|
| 1 | feat(repo) | scaffold монорепо, `README`, `CHANGELOG` |
| 2 | feat(core) | init Spring Boot core (sandbox/binance) |
| 3 | feat(agent) | init agent-builder runtime, базові інструменти |
| 4 | feat(console) | базовий UI (Next/React) |
| 5 | feat(core) | доменні порти, sandbox‑адаптери |
| 6 | feat(agent) | registry tools → core API |
| 7 | feat(console) | інтеграція UI з tools + метрики |
| 8 | infra(docker) | Dockerfile + supervisord + Caddy |
| 9 | ci(local) | docker-compose + run-local.sh |
|10 | ci(fly) | fly.toml + GitHub Action |
|11 | docs | заповнити DEPLOY‑SPEC‑FLYIO.md, README, CHANGELOG |

---

## 6️⃣ Definition of Done (MVP)

- [ ] локальний запуск (sandbox) працює;
- [ ] LLM‑агент викликає core API через tools;
- [ ] деплой Fly.io генерує URL у Job Summary;
- [ ] усі секрети у GitHub Secrets;
- [ ] документація й CHANGELOG заповнені.

---

## 7️⃣ Короткий висновок

Ця специфікація об’єднує вимоги до **LLM-керованого торгового бота** (через Agent Builder) і просту, бюджетну схему деплою на Fly.io.  

👉 Розробка стартує локально (sandbox), CI/CD автоматизує перехід до хмари після стабілізації.

---

## 8️⃣ Додаток A — Приклади коду Agent Builder (Java DSL)

> Мета: дати Cursor готові шаблони класів для швидкого старту.

### A.1 `app/agent-builder/src/main/java/agent/AgentConfig.java`
```java
package agent;

import agent.sdk.AgentBuilder;
import agent.sdk.Agent;
import agent.tools.ToolsRegistry;
import agent.guard.RiskGuard;
import agent.mem.LocalMemory;
import agent.log.AuditLogger;

public final class AgentConfig {
  public static Agent build(ToolsRegistry tools) {
    return AgentBuilder.create("TradingAgent")
      .systemPrompt("""
        You are a cautious, deterministic trading orchestrator.
        - Never place orders without riskPolicy (SL required).
        - Never exceed configured maxPctEquity.
        - Prefer sandbox profile unless explicitly told otherwise.
        - Explain decisions briefly.
      """)
      .addTool(tools.getPortfolio())
      .addTool(tools.placeOrderSafe())
      .addTool(tools.cancelOrder())
      .addTool(tools.evaluateSignals())
      .policy(new RiskGuard(/* maxPctEquity=5, stopLossPct=2 */))
      .memory(new LocalMemory())
      .logger(new AuditLogger("/var/log/llm-trades.log"))
      .build();
  }
}
```

### A.2 `app/agent-builder/src/main/java/agent/tools/ToolsRegistry.java`
```java
package agent.tools;

import agent.sdk.Tool;
import agent.sdk.schema.JsonSchema;
import http.CoreApiClient; // ваш HTTP клієнт до trading-core

public final class ToolsRegistry {
  private final CoreApiClient core;
  public ToolsRegistry(CoreApiClient core) { this.core = core; }

  public Tool getPortfolio() {
    return Tool.builder("getPortfolio")
      .description("Return portfolio snapshot with equity/free/positions")
      .input(JsonSchema.empty())
      .handler(ctx -> core.get("/api/core/portfolio"))
      .build();
  }

  public Tool placeOrderSafe() {
    return Tool.builder("placeOrderSafe")
      .description("Place order with risk policy pre-checks")
      .input(JsonSchema.parse("""
        {
          "type":"object",
          "required":["symbol","side","qty","riskPolicy","reason"],
          "properties":{
            "symbol":{"type":"string"},
            "side":{"enum":["BUY","SELL"]},
            "qty":{"type":"number","minimum":0},
            "limitPrice":{"type":["number","null"]},
            "riskPolicy":{
              "type":"object",
              "required":["maxPctEquity","stopLossPct"],
              "properties":{
                "maxPctEquity":{"type":"number","minimum":0,"maximum":100},
                "stopLossPct":{"type":"number","minimum":0,"maximum":100},
                "takeProfitPct":{"type":["number","null"],"minimum":0,"maximum":100}
              }
            },
            "reason":{"type":"string","minLength":3}
          }
        }
      """))
      .handler(ctx -> core.postJson("/api/core/orders/placeSafe", ctx.args()))
      .build();
  }

  public Tool cancelOrder() {
    return Tool.builder("cancelOrder")
      .description("Cancel order by clientOrderId")
      .input(JsonSchema.parse("""
        {"type":"object","required":["clientOrderId"],
         "properties":{"clientOrderId":{"type":"string","minLength":8}}}
      """))
      .handler(ctx -> core.postJson("/api/core/orders/cancel", ctx.args()))
      .build();
  }

  public Tool evaluateSignals() {
    return Tool.builder("evaluateSignals")
      .description("Ask core for deterministic signal suggestion for a symbol")
      .input(JsonSchema.parse("""
        {"type":"object","required":["symbol"],
         "properties":{"symbol":{"type":"string"},"asOf":{"type":"string"}}}
      """))
      .handler(ctx -> core.get("/api/core/signal?symbol=" + ctx.args().get("symbol")))
      .build();
  }
}
```

### A.3 `app/trading-core/src/main/java/core/api/CoreControllers.java`
```java
@RestController
@RequestMapping("/api/core")
public class CoreControllers {
  @GetMapping("/portfolio")
  public PortfolioDto portfolio() { /* ... */ }

  @PostMapping("/orders/placeSafe")
  public OrderIdDto placeOrderSafe(@RequestBody PlaceOrderReq req) {
    // 1) normalize to steps (exchangeInfo)
    // 2) risk.validate(req)
    // 3) exchange.placeOrder()
    // 4) return clientOrderId
  }

  @PostMapping("/orders/cancel")
  public CancelResultDto cancel(@RequestBody CancelReq req) { /* ... */ }

  @GetMapping("/signal")
  public PlannedOrderDto signal(@RequestParam String symbol, @RequestParam(required=false) Instant asOf) {
    // deterministic signal from StrategyEngine
  }
}
```

### A.4 Guardrails приклад
```java
public final class RiskGuard implements Policy {
  private final double maxPctEquity; // напр. 5
  private final double stopLossPct;  // напр. 2
  public RiskGuard(double maxPctEquity, double stopLossPct){ this.maxPctEquity=maxPctEquity; this.stopLossPct=stopLossPct; }

  @Override
  public PolicyDecision beforeToolCall(String toolName, Map<String,Object> args) {
    if (toolName.equals("placeOrderSafe")) {
      Map<String,Object> rp = (Map<String,Object>) args.get("riskPolicy");
      if (rp == null || !rp.containsKey("stopLossPct"))
        return PolicyDecision.block("SL is mandatory");
      double max = ((Number) rp.getOrDefault("maxPctEquity", maxPctEquity)).doubleValue();
      if (max > maxPctEquity) return PolicyDecision.block("maxPctEquity too high");
    }
    return PolicyDecision.allow();
  }
}
```

---

## 9️⃣ Додаток B — Тестові промпти та golden‑тести

### B.1 System Prompt (для агента)
```
You are TradingAgent. Objectives:
- Use only registered tools.
- Sandbox first. Do not assume secrets.
- Never place orders without riskPolicy (stopLossPct required).
- Keep messages concise; output tool calls when appropriate.
```

### B.2 Приклади user‑запитів (integration smoke)
1. `"Show portfolio snapshot"` → очікуємо виклик `getPortfolio`.
2. `"Open a small long on BTCUSDT, 0.5% risk, with 1% SL"` → `placeOrderSafe` з `maxPctEquity<=0.5` і `stopLossPct=1`.
3. `"Cancel last order with id X"` → `cancelOrder` з `clientOrderId=X`.

### B.3 Golden‑test формат (JSON)
```json
{
  "name": "place-long-with-sl",
  "input": "Open a small long on BTCUSDT at market, 0.5% risk, SL 1%",
  "expectTool": "placeOrderSafe",
  "expectArgs": {
    "symbol": "BTCUSDT", "side": "BUY",
    "riskPolicy": {"maxPctEquity": {"lte": 0.5}, "stopLossPct": {"eq": 1}}
  }
}
```

---

## 🔟 Додаток C — Стартовий промпт для Cursor (Bootstrap)

> Скопіюй цей блок у **Cursor** як початкову інструкцію до репозиторію.

```
Використовуй файл docs/DEPLOY-SPEC-FLYIO.md як джерело істини.
Виконай послідовно коміт‑план (розділ 5️⃣), після кожного кроку:
- створи/зміни файли згідно специфікації;
- запусти локально (run-local.sh або docker-compose) і переконайся, що UI і API відповідають;
- додай запис у docs/CHANGELOG.md з датою та описом;
- коміть із повідомленням за шаблоном.

Особливі вимоги:
- Згенеруй класи з Додатка A (Agent Builder, ToolsRegistry, CoreControllers) як заглушки, але з валідною компіляцією.
- Додай System Prompt (Додаток B.1) у agent-builder як ресурс `resources/system-prompt.txt` і підключи в AgentConfig.
- Додай мінімальні тести: один golden‑test для `placeOrderSafe` (Додаток B.3) і один smoke‑тест для `/api/core/portfolio`.
- Підготуй артефакти для Docker (Dockerfile, supervisord.conf, Caddyfile, entrypoint.sh) і локального запуску.
- Налаштуй GitHub Action для Fly.io, щоб у Job Summary виводився URL.
```

---

## 1️⃣1️⃣ Примітки з безпеки
- Agent Builder не має доступу до біржових ключів; лише core API.
- Логи інструмент‑викликів містять correlation‑id без секретів.
- Для публічного стенду використати простий Bearer‑token (ENV `ADMIN_TOKEN`).

