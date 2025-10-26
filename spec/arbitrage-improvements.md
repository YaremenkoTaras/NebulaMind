# Покращення системи автоматичного трикутного арбітражу

## Огляд

Документ описує покращення системи автоматичного трикутного арбітражу на основі аналізу реальних результатів виконання.

## Аналіз поточної ситуації

### Виконана задача: 3d4fe0a3-b043-4efc-8d52-35881cc6d4a9

**Параметри запуску:**
- Budget: 200 USDT
- Execution Time: 3 хвилини  
- Delay: 2 секунди
- Min Profit: 0.5%
- Max Assets: 40
- Chain Length: 3

**Результати:**
- Виконано операцій: 36
- Успішних: 22 (61%)
- Невдалих: 14 (39%)
- Початковий бюджет: 200.00 USDT
- Кінцевий бюджет: 200.08 USDT
- Загальний прибуток: 1.01 USDT
- Загальний збиток: 0.93 USDT
- Чистий прибуток: 0.08 USDT (0.04%)

**ROI за 3 хвилини: +0.04% (або ~0.8% на годину)**

### Критичні проблеми

#### 1. Високий відсоток невдач (39%)
Найчастіші помилки:
- `500 Internal Server Error` - 14 випадків
- Проблемні пари: ETHBTC, BNBBTC, LTCBTC, SOLBTC, DOTBTC
- Всі пари з BTC як проміжною валютою фейляться

#### 2. Високий slippage

| Метрика | Значення |
|---------|----------|
| Середній очікуваний прибуток | 0.7-1.3% |
| Середній фактичний прибуток | -0.5% до +0.7% |
| Середній slippage | ~1.0-2.0% |

**Приклади:**
- Expected: 1.01%, Actual: 0.57% → Slippage: 0.44%
- Expected: 0.86%, Actual: -0.13% → Slippage: 0.99%
- Expected: 1.31%, Actual: 0.57% → Slippage: 0.74%

#### 3. Відсутність адаптації
- Система повторює одні й ті ж невдалі ланцюжки
- Немає blacklist для проблемних пар
- Не враховується історія виконання

#### 4. Ризикове управління капіталом
- Використовується весь доступний бюджет на одну операцію
- Немає захисту від великих втрат
- Відсутні emergency stop умови

## Покращення

### Phase 1: Критичні (пріоритет: HIGH)

#### 1.1 Circuit Breaker Pattern

**Мета:** Запобігти повторенню невдалих операцій

**Реалізація:**
- Blacklist для ланцюжків після 3 невдач підряд
- Тимчасова блокування на 5 хвилин
- Автоматичне очищення blacklist після успішного виконання

**Конфігурація:**
```yaml
arbitrage:
  circuit-breaker:
    failure-threshold: 3
    blacklist-duration-minutes: 5
    enabled: true
```

**Метрики:**
- Кількість заблокованих ланцюжків
- Час блокування
- Причини блокування

#### 1.2 Adaptive Slippage Protection

**Мета:** Фільтрувати операції з високою ймовірністю slippage

**Реалізація:**
- Параметр `slippageTolerance` (default: 1.0%)
- Мінімальний безпечний прибуток = minProfit + slippageTolerance
- Не виконувати якщо expected < safe threshold

**Формула:**
```
safeProfit = minProfitPercent + slippageTolerance
execute = (expectedProfit >= safeProfit)
```

**Приклад:**
```
minProfit = 1.0%
slippageTolerance = 1.0%
safeProfit = 2.0%

Chain A: expected = 2.5% → EXECUTE (2.5% >= 2.0%)
Chain B: expected = 1.8% → SKIP (1.8% < 2.0%)
```

#### 1.3 Backend Error Handling

**Мета:** Зменшити кількість 500 помилок

**Реалізація:**
- Retry logic з exponential backoff
- Валідація перед викликом API
- Детальне логування помилок

**Retry стратегія:**
- Max attempts: 2
- Backoff: 1 секунда
- Retry тільки для 5xx помилок

### Phase 2: Важливі (пріоритет: MEDIUM)

#### 2.1 Chain Success Tracking

**Мета:** Аналіз історичної успішності ланцюжків

**Метрики на ланцюжок:**
- Pattern (наприклад, "ETHUSDT→MATICETH→MATICUSDT")
- Total executions
- Successful executions
- Success rate
- Average slippage
- Average profit

**Збереження:**
- В пам'яті під час виконання задачі
- В файл для персистентності між рестартами

#### 2.2 Smart Order Sizing

**Мета:** Розумне управління капіталом

**Правила:**
- Maximum per trade: 20% поточного бюджету
- Для нових ланцюжків: мінімальна сума
- Для перевірених ланцюжків: до 20% бюджету

**Формула:**
```java
if (successRate < 0.5 || executions < 3) {
    amount = min(minRequired, currentBudget * 0.2)
} else {
    amount = min(currentBudget * 0.2, currentBudget)
}
```

#### 2.3 Emergency Stop Conditions

**Мета:** Захист від великих втрат

**Умови зупинки:**
- Total loss > 5% початкового бюджету
- Net profit < -2% початкового бюджету  
- 5 збитків підряд
- Single loss > 1% бюджету

**Дії:**
- Автоматична зупинка задачі
- Статус STOPPED з причиною
- Notification в логах

### Phase 3: Оптимізації (пріоритет: LOW)

#### 3.1 Scan Result Caching

**Мета:** Зменшити навантаження на API

**Реалізація:**
- Cache validity: 20 секунд
- Використовувати кеш якщо актуальний
- Інвалідація при значних змінах цін

#### 3.2 Adaptive Profit Threshold

**Мета:** Динамічна адаптація до ринкових умов

**Логіка:**
```
adaptiveThreshold = baseThreshold + (recentLosses * 0.5%)
```

Після 3 збитків підряд: threshold збільшується з 1% до 2.5%
Після 5 прибутків підряд: threshold повертається до базового

#### 3.3 Detailed Execution Logging

**Мета:** Кращий моніторинг та аналіз

**Логування:**
- Причини відхилення ланцюжків
- Час виконання кожної операції
- Detailed error messages
- Статистика по типах помилок

#### 3.4 Pair-specific Configuration

**Мета:** Гнучка конфігурація для різних пар

**Конфігурація:**
```yaml
arbitrage:
  pairs:
    BTC:
      min-amount: 60.0
      slippage-tolerance: 1.5
      enabled: true
    ETH:
      min-amount: 10.0
      slippage-tolerance: 1.0
      enabled: true
```

## Нові параметри задачі

### TaskCreateRequest (доповнення)

```java
private Double slippageTolerance = 1.0; // Default 1%
private Double maxLossPerTrade = 1.0;   // Max 1% loss per trade
private Boolean enableCircuitBreaker = true;
private Boolean enableSmartSizing = true;
```

### Task Domain (доповнення)

```java
private Integer consecutiveLosses = 0;
private Integer consecutiveWins = 0;
private Double maxDrawdown = 0.0;
private String stoppedReason; // For emergency stops
```

## Очікувані результати

### Метрики до покращень

| Метрика | Значення |
|---------|----------|
| Success Rate | 61% |
| Failure Rate | 39% |
| Net Profit (3 min) | +0.04% |
| ROI per hour | ~0.8% |
| Avg Slippage | 1.0-2.0% |

### Очікувані метрики після Phase 1

| Метрика | Поточне | Очікуване | Покращення |
|---------|---------|-----------|------------|
| Success Rate | 61% | 85-90% | +40% |
| Failure Rate | 39% | 10-15% | -62% |
| Net Profit (3 min) | +0.04% | +0.3-0.5% | 7-12x |
| ROI per hour | 0.8% | 6-10% | 7-12x |

### Очікувані метрики після всіх Phase

| Метрика | Поточне | Очікуване | Покращення |
|---------|---------|-----------|------------|
| Success Rate | 61% | 90-95% | +50% |
| Failure Rate | 39% | 5-10% | -74% |
| Net Profit (3 min) | +0.04% | +0.5-1.0% | 12-25x |
| ROI per hour | 0.8% | 10-20% | 12-25x |
| Max Drawdown | -0.5% | -0.2% | -60% |

## План тестування

### Test Case 1: Circuit Breaker
1. Створити задачу з параметрами що спричиняють невдачі
2. Перевірити що після 3 невдач ланцюжок блокується
3. Перевірити що блокування діє 5 хвилин
4. Перевірити автоматичне розблокування

### Test Case 2: Slippage Protection
1. Створити задачу з slippageTolerance = 1.0%
2. Перевірити що виконуються тільки ланцюжки з profit >= 2.0%
3. Перевірити логи про пропущені ланцюжки

### Test Case 3: Smart Sizing
1. Створити задачу з бюджетом 100 USDT
2. Перевірити що перша операція <= 20 USDT
3. Перевірити адаптацію розміру після успіхів
4. Перевірити мінімальний розмір для нових ланцюжків

### Test Case 4: Emergency Stop
1. Створити задачу що генерує збитки
2. Перевірити автоматичну зупинку при loss > 5%
3. Перевірити корректний статус і причину

## Моніторинг

### Key Performance Indicators (KPI)

1. **Success Rate**: % успішних операцій
2. **Net Profit Rate**: чистий прибуток / час виконання
3. **Average Slippage**: середнє відхилення від передбачення
4. **Blacklisted Chains**: кількість заблокованих ланцюжків
5. **Emergency Stops**: кількість автоматичних зупинок

### Dashboards

Рекомендовані метрики для відображення:
- Real-time P&L
- Success/Failure ratio
- Top performing chains
- Blacklisted chains list
- Current slippage threshold

## Міграція

### Existing Tasks

Існуючі задачі продовжать працювати з параметрами за замовчуванням:
- slippageTolerance = 1.0%
- enableCircuitBreaker = true
- enableSmartSizing = true

### Database Schema

Не потрібні зміни в JSON структурі, нові поля опційні.

## Конфігурація

### application.yml

```yaml
arbitrage:
  circuit-breaker:
    enabled: true
    failure-threshold: 3
    blacklist-duration-minutes: 5
    
  slippage:
    default-tolerance: 1.0
    enabled: true
    
  smart-sizing:
    enabled: true
    max-per-trade-percent: 20
    min-executions-for-full-size: 3
    
  emergency-stop:
    enabled: true
    max-loss-percent: 5.0
    max-drawdown-percent: 2.0
    consecutive-losses-threshold: 5
    
  execution:
    retry-enabled: true
    retry-max-attempts: 2
    retry-backoff-ms: 1000
```

## Changelog

### Version 1.1.0 (планується)

**Added:**
- Circuit Breaker Pattern for failed chains
- Adaptive Slippage Protection
- Retry logic with exponential backoff
- Chain Success Tracking
- Smart Order Sizing
- Emergency Stop Conditions

**Changed:**
- TaskCreateRequest: додані нові параметри
- Task domain: додані поля для tracking
- ArbitrageTaskExecutor: повністю переписана логіка виконання

**Fixed:**
- Високий відсоток невдач (39% → 10-15%)
- Проблеми з slippage (втрати до 2%)
- Відсутність захисту від втрат

## Автори та внески

- Аналіз поточної системи: 25.10.2025
- Розробка плану покращень: 25.10.2025
- Реалізація: в процесі

## Додатки

### A. Приклади логів

**До покращень:**
```
2025-10-25 17:50:23 ERROR Failed to execute chain for task: 500 Internal Server Error
2025-10-25 17:50:28 INFO  Successfully executed chain: profit=-0.08 (-0.13%)
```

**Після покращень:**
```
2025-10-25 18:00:15 INFO  Chain ETHBTC→BTCUSDT skipped: profit 0.95% < safe threshold 2.0%
2025-10-25 18:00:20 INFO  Successfully executed ETHUSDT→MATICETH: profit=+0.45% (expected 1.01%, slippage 0.56%)
2025-10-25 18:00:25 WARN  Chain BNBBTC blacklisted after 3 consecutive failures
```

### B. Статистика тестування

Буде доповнено після реалізації та тестування.

