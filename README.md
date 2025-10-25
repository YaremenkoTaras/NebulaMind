# NebulaMind 🤖💹

> **LLM-керований торговий бот** для крипторинку на базі Agent Builder з підтримкою деплою на Fly.io

## 🎯 Огляд проєкту

**NebulaMind** — це модульна система для автоматизованої торгівлі криптовалютами, де:

- **LLM (Agent Builder)** виступає як інтелектуальний оркестратор, що аналізує ринок і приймає рішення
- **Trading Core** (Spring Boot 3 / Java 21) реалізує всю торгову логіку, ризик-менеджмент та інтеграцію з біржами
- **Agent Console** (React/Next.js) надає зручний UI для моніторингу та контролю
- **CI/CD pipeline** забезпечує автоматичний деплой на Fly.io

## 🏗️ Архітектура

```
NebulaMind/
├─ app/
│  ├─ trading-core/        # Java 21 / Spring Boot 3 — торгова логіка
│  ├─ agent-builder/       # Agent Builder runtime — LLM оркестрація
│  └─ agent-console/       # React/Next.js — UI для контролю
│
├─ docs/                   # Документація проєкту
├─ cicd/                   # CI/CD конфігурації та скрипти
│  ├─ local/              # Локальний запуск (docker-compose, scripts)
│  ├─ github-actions/     # GitHub Actions workflows
│  └─ fly/                # Fly.io конфігурація
│
├─ spec/                   # Технічні специфікації
└─ README.md
```

## 🚀 Швидкий старт

### Вимоги

- **Java 21+** (для trading-core та agent-builder)
- **Node.js 18+** (для agent-console)
- **Docker** (опціонально, для контейнерного запуску)
- **Maven або Gradle** (для збірки Java модулів)

### Локальний запуск (без Docker)

```bash
# 1. Налаштувати змінні середовища
cp cicd/local/env.example .env

# 2. Зібрати всі модулі
./gradlew :app:trading-core:bootJar :app:agent-builder:shadowJar
(cd app/agent-console && npm install && npm run build)

# 3. Запустити всі сервіси
./cicd/local/run-local.sh

# UI доступний на http://localhost:8083
```

### Локальний запуск (через Docker Compose)

```bash
docker compose -f cicd/local/docker-compose.yml up --build

# UI доступний на http://localhost:8080
```

## 🔧 Основні компоненти

### Trading Core (порт 8081)

Реалізує торгову логіку з використанням чистої архітектури:

- **ExchangeGateway** — інтеграція з Binance (Spot/Futures)
- **MarketDataFeed** — WebSocket стріми (trades, klines, depth)
- **RiskManager** — guardrails та валідація перед кожним ордером
- **OrderRouter** — маршрутизація та виконання ордерів
- **Sandbox Mode** — емуляція торгівлі без реальних коштів

#### Логіка ризику

- Max % equity per trade
- Daily loss limit
- Заборона на усереднення позицій
- Обов'язкові SL/TP
- Auto-pause при помилках біржі (429/418/time-skew)

### Agent Builder (порт 8082)

LLM runtime з інструментами для взаємодії з Trading Core:

- `getPortfolio` — отримати поточний портфель
- `placeOrderSafe` — розмістити ордер з перевіркою ризиків
- `cancelOrder` — скасувати активний ордер
- `evaluateSignals` — отримати торговий сигнал

**Guardrails:**
- Не дозволяє угоди без SL/TP
- Обмежує розмір позиції (% від equity)
- Логує всі рішення для аудиту

### Agent Console (порт 8083)

Веб-інтерфейс для:

- Моніторингу портфеля та позицій
- Перегляду історії торгів
- Контролю LLM-агента
- Перегляду логів та метрик

## 📊 Trading Flow

```
User/Scheduler → LLM Agent → evaluateSignals() → Signal
                           ↓
                  placeOrderSafe() → RiskManager.validate()
                           ↓
                  ExchangeGateway.placeOrder() → Binance API
```

## 🧪 Тестування

```bash
# Unit тести
./gradlew test

# Integration тести (з sandbox)
./gradlew integrationTest

# E2E тести
npm run test:e2e
```

## 🚢 Деплой на Fly.io

### Автоматичний деплой (через GitHub Actions)

Push до `main` автоматично запускає деплой:

```bash
git push origin main
```

GitHub Action зібере всі модулі, створить Docker образ та задеплоїть на Fly.io.

### Ручний деплой

```bash
cd cicd/fly
fly deploy
```

### Необхідні секрети (GitHub Secrets)

- `FLY_API_TOKEN` — токен для Fly.io CLI
- `BINANCE_API_KEY` — ключ API Binance
- `BINANCE_API_SECRET` — секрет API Binance
- `OPENAI_API_KEY` — ключ OpenAI (для LLM)

## 💰 Вартість інфраструктури

- Fly.io 1 GB RAM: **~$5-6/міс**
- Static IP (опціонально): **$2/міс**
- **Разом для MVP: ~$7-8/міс**

## 📚 Документація

- [Повна специфікація](spec/spec_tradin_bot_with_cicd.md) — детальні вимоги та архітектура
- [CHANGELOG](CHANGELOG.md) — історія змін проєкту
- [DEPLOY-SPEC-FLYIO](docs/DEPLOY-SPEC-FLYIO.md) — інструкції з деплою

## 🔐 Безпека

- Agent Builder не має прямого доступу до ключів біржі
- Всі запити проходять через Core API з валідацією
- Логи не містять секретних даних (correlation-id only)
- Bearer token для публічного стенду (`ADMIN_TOKEN`)

## 🛣️ Roadmap

- [x] Scaffold проєкту та базова структура
- [ ] Trading Core з sandbox mode
- [ ] Agent Builder runtime та tools
- [ ] Agent Console UI
- [ ] Інтеграція з Binance API
- [ ] Деплой на Fly.io
- [ ] Backtesting framework
- [ ] Advanced ризик-менеджмент
- [ ] Multi-exchange support

## 📝 Ліцензія

MIT

## 🤝 Внесок

Проєкт знаходиться в активній розробці. Pull requests вітаються!

---

**⚠️ Disclaimer:** Торгівля криптовалютами несе ризики. Використовуйте цей бот на власний ризик. Починайте з sandbox mode та тестових рахунків.

