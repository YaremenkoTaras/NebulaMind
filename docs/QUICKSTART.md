# NebulaMind Quick Start Guide

## 🚀 Швидкий запуск за 5 хвилин

### Передумови
- **Java 21** (через jenv або інший менеджер версій)
- **Maven 3.9+**
- **Docker** (опціонально)

### Варіант 1: Локальний запуск (без Docker)

```bash
# 1. Клонувати репозиторій
git clone https://github.com/your-username/NebulaMind.git
cd NebulaMind

# 2. Налаштувати Java 21 (якщо використовуєте jenv)
jenv local 21

# 3. Скопіювати env.example в .env
cp cicd/local/env.example .env
# Відредагуйте .env, якщо потрібно (для sandbox mode не обов'язково)

# 4. Запустити всі сервіси
./cicd/local/run-local.sh
```

Після запуску доступні:
- **Trading Core**: http://localhost:8081/api/core/health
- **Agent Builder**: http://localhost:8082/api/agent/health

### Варіант 2: Запуск через Docker Compose

```bash
# 1. Клонувати та перейти в директорію
git clone https://github.com/your-username/NebulaMind.git
cd NebulaMind

# 2. Скопіювати env.example в .env
cp cicd/local/env.example .env

# 3. Запустити через Docker Compose
cd cicd/local
docker compose up --build
```

## 📝 Тестування API

### Отримати портфель

```bash
curl http://localhost:8081/api/core/portfolio
```

Відповідь:
```json
{
  "timestamp": "2025-10-25T09:00:00Z",
  "totalEquity": 10000.0,
  "freeBalance": 10000.0,
  "lockedBalance": 0.0,
  "positions": [],
  "currency": "USDT"
}
```

### Розмістити ордер

```bash
curl -X POST http://localhost:8082/api/agent/orders/place \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "BTCUSDT",
    "side": "BUY",
    "qty": 0.01,
    "riskPolicy": {
      "maxPctEquity": 2.0,
      "stopLossPct": 1.5,
      "takeProfitPct": 3.0
    },
    "reason": "Testing order placement"
  }'
```

Відповідь:
```json
{
  "clientOrderId": "ORDER_abc123",
  "orderId": "1234567890",
  "symbol": "BTCUSDT",
  "side": "BUY",
  "status": "FILLED",
  "origQty": 0.01,
  "executedQty": 0.01,
  "avgPrice": 50000.0,
  "timestamp": "2025-10-25T09:01:00Z",
  "message": "Order processed successfully"
}
```

### Отримати сигнал

```bash
curl http://localhost:8081/api/core/signal?symbol=BTCUSDT
```

Відповідь:
```json
{
  "symbol": "BTCUSDT",
  "action": "HOLD",
  "confidence": 0.5,
  "reason": "No clear trend detected (sandbox mode)",
  "timestamp": "2025-10-25T09:02:00Z"
}
```

### Скасувати ордер

```bash
curl -X POST http://localhost:8082/api/agent/orders/cancel/ORDER_abc123
```

## 🧪 Запуск тестів

### Trading Core
```bash
cd app/trading-core
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn test
```

### Agent Builder
```bash
cd app/agent-builder
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn test
```

### Всі тести разом
```bash
./cicd/local/run-tests.sh
```

## 🛑 Зупинка сервісів

### Якщо запускали через run-local.sh
```bash
./cicd/local/stop-local.sh
```

### Якщо запускали через Docker Compose
```bash
cd cicd/local
docker compose down
```

## 🔧 Налаштування

### Режими роботи

1. **Sandbox mode** (за замовчуванням)
   - Симуляція торгівлі в пам'яті
   - Не потребує API ключів
   - Початковий баланс: 10,000 USDT

2. **Development mode** (Binance Testnet)
   ```bash
   # В .env файлі:
   SPRING_PROFILES_ACTIVE=development
   BINANCE_API_KEY=your_testnet_key
   BINANCE_API_SECRET=your_testnet_secret
   ```

3. **Production mode** (Real Binance)
   ```bash
   # В .env файлі:
   SPRING_PROFILES_ACTIVE=production
   BINANCE_API_KEY=your_real_key
   BINANCE_API_SECRET=your_real_secret
   ```

### Risk параметри

Змініть у `.env`:
```bash
RISK_MAX_PCT_EQUITY=5.0          # Макс % від equity на угоду
RISK_STOP_LOSS_PCT=2.0           # Рекомендований stop loss %
RISK_DAILY_LOSS_LIMIT_PCT=10.0  # Денний ліміт втрат
```

## 📊 Моніторинг

### Логи

```bash
# Trading Core
tail -f logs/trading-core.log

# Agent Builder
tail -f logs/agent-builder.log

# Всі разом (якщо запускали через run-local.sh)
tail -f logs/*.log
```

### Health Checks

```bash
# Trading Core
curl http://localhost:8081/api/core/health

# Agent Builder
curl http://localhost:8082/api/agent/health
```

### Actuator Endpoints (Trading Core)

```bash
# Health detailed
curl http://localhost:8081/actuator/health

# Metrics
curl http://localhost:8081/actuator/metrics

# Info
curl http://localhost:8081/actuator/info
```

## 🐛 Troubleshooting

### Проблема: Java 21 not found
```bash
# Встановіть Java 21 через jenv або SDKMAN
jenv install java 21

# Або перевірте JAVA_HOME
echo $JAVA_HOME
```

### Проблема: Port already in use
```bash
# Знайти процес на порті
lsof -ti:8081
lsof -ti:8082

# Вбити процес
kill $(lsof -ti:8081)
```

### Проблема: Maven build fails
```bash
# Очистити Maven кеш
mvn clean
rm -rf ~/.m2/repository/com/nebulamind

# Перезбудувати
cd app/trading-core && mvn clean install
```

## 📚 Наступні кроки

1. Прочитайте [ARCHITECTURE.md](./ARCHITECTURE.md) для розуміння дизайну
2. Перегляньте [API Documentation](./API.md) для детальних endpoint описів
3. Налаштуйте LLM integration (OpenAI ключ в `.env`)
4. Розгорніть на Fly.io за інструкцією в [DEPLOY-SPEC-FLYIO.md](./DEPLOY-SPEC-FLYIO.md)

## 💡 Корисні команди

```bash
# Перезапустити один сервіс
cd app/trading-core && mvn spring-boot:run

# Збудувати JAR файли
mvn clean package -DskipTests

# Запустити з профілем
java -jar target/trading-core-0.1.0-SNAPSHOT.jar --spring.profiles.active=development

# Переглянути всі порти
lsof -i :8081-8083
```

## 🎯 Приклади use cases

### Use Case 1: Симуляція торгівлі

```bash
# 1. Отримати поточний портфель
curl http://localhost:8081/api/core/portfolio

# 2. Отримати сигнал
curl http://localhost:8081/api/core/signal?symbol=BTCUSDT

# 3. Розмістити ордер
curl -X POST http://localhost:8082/api/agent/orders/place \
  -H "Content-Type: application/json" \
  -d @examples/buy-order.json

# 4. Перевірити портфель після угоди
curl http://localhost:8081/api/core/portfolio
```

### Use Case 2: Risk Management тест

```bash
# Спробувати розмістити занадто велику позицію
curl -X POST http://localhost:8082/api/agent/orders/place \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "BTCUSDT",
    "side": "BUY",
    "qty": 1.0,
    "riskPolicy": {
      "maxPctEquity": 50.0,
      "stopLossPct": 1.5
    },
    "reason": "Testing risk limits"
  }'

# Очікується помилка 403: Risk limit exceeded
```

---

**Потрібна допомога?** Створіть issue на GitHub або перегляньте [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)

