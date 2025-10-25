# Contributing to NebulaMind

Дякуємо за інтерес до проєкту NebulaMind! 🚀

Ми раді будь-яким внескам: від виправлення друкарських помилок до нових функцій.

## 📋 Зміст

- [Code of Conduct](#code-of-conduct)
- [Як я можу допомогти?](#як-я-можу-допомогти)
- [Процес розробки](#процес-розробки)
- [Git Workflow](#git-workflow)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

Цей проєкт дотримується принципів поваги та професіоналізму. Участь у проєкті означає, що ви погоджуєтесь:

- Бути ввічливими та поважати інших учасників
- Приймати конструктивну критику
- Фокусуватись на тому, що найкраще для проєкту
- Проявляти емпатію до інших учасників

## Як я можу допомогти?

### 🐛 Повідомлення про баги

Знайшли баг? Створіть issue з такою інформацією:

1. **Назва**: Коротке описання проблеми
2. **Опис**: Детальний опис що пішло не так
3. **Кроки відтворення**: Як відтворити баг
4. **Очікувана поведінка**: Що має відбуватись
5. **Фактична поведінка**: Що відбувається насправді
6. **Середовище**:
   - OS (macOS, Linux, Windows)
   - Java version
   - Node.js version (для console)
7. **Логи**: Будь-які релевантні логи або error messages

**Шаблон:**

```markdown
## Опис
[Опишіть баг]

## Кроки відтворення
1. ...
2. ...
3. ...

## Очікувана поведінка
[Що має відбуватись]

## Фактична поведінка
[Що відбувається]

## Середовище
- OS: macOS 14.0
- Java: 21
- Maven: 3.9.5

## Логи
```
[вставте логи тут]
```
```

### 💡 Пропозиції нових функцій

Маєте ідею? Створіть issue з такою інформацією:

1. **Назва**: Коротка назва функції
2. **Опис**: Детальний опис функціональності
3. **Мотивація**: Чому це потрібно?
4. **Приклади використання**: Як це буде працювати?
5. **Альтернативи**: Які альтернативні рішення розглядали?

### 📝 Покращення документації

Знайшли помилку в документації або хочете щось додати? Просто створіть PR!

Документація знаходиться в:
- `README.md` - загальний огляд
- `docs/` - детальна документація
- `PROJECT_STATUS.md` - статус проєкту
- `CHANGELOG.md` - історія змін

### 🧑‍💻 Написання коду

Хочете додати нову функцію або виправити баг? Чудово!

**Основні напрямки для контрибуції:**

1. **Trading Core**:
   - Інтеграція з Binance API
   - Нові стратегії та індикатори
   - Покращення risk management
   - Оптимізація продуктивності

2. **Agent Builder**:
   - Інтеграція з LLM (OpenAI, Anthropic)
   - Нові tools для агента
   - Покращення guardrails
   - Аудит та логування

3. **Agent Console**:
   - UI/UX покращення
   - Нові charts та візуалізації
   - Real-time updates
   - Mobile responsiveness

4. **Infrastructure**:
   - CI/CD оптимізація
   - Docker improvements
   - Monitoring та alerting
   - Performance optimization

## Процес розробки

### 1. Налаштування середовища

#### Вимоги

- **Java 21+** (використовуємо jenv для управління версіями)
- **Maven 3.9+**
- **Node.js 18+** (для agent-console)
- **Docker** (опціонально)
- **Git**

#### Встановлення

```bash
# Clone repository
git clone https://github.com/your-username/NebulaMind.git
cd NebulaMind

# Set Java version (якщо використовуєте jenv)
jenv local 21

# Build all modules
cd app/trading-core
./mvnw clean install

cd ../agent-builder
./mvnw clean install

# Install console dependencies (коли буде реалізовано)
cd ../agent-console
npm install
```

#### Запуск локально

```bash
# Використовуйте скрипт для локального запуску
./cicd/local/run-local.sh

# Або використовуйте Docker Compose
docker compose -f cicd/local/docker-compose.yml up
```

### 2. Вибір задачі

1. Перегляньте [Issues](https://github.com/your-username/NebulaMind/issues)
2. Шукайте issues з label `good first issue` для початку
3. Прокоментуйте issue що ви хочете над ним працювати
4. Чекайте підтвердження від maintainers

### 3. Створення гілки

Детально див. [docs/GIT_WORKFLOW.md](docs/GIT_WORKFLOW.md)

```bash
# Оновіть main
git checkout main
git pull origin main

# Створіть feature гілку
git checkout -b feature/your-feature-name

# Або bugfix гілку
git checkout -b bugfix/issue-123
```

### 4. Розробка

```bash
# Робіть зміни
# Пишіть тести
# Запускайте тести локально
./cicd/local/run-tests.sh

# Комітьте зміни з правильним форматом
git add <files>
git commit -m "feat(core): add new feature"
```

### 5. Тестування

```bash
# Unit tests
cd app/trading-core
./mvnw test

cd app/agent-builder
./mvnw test

# Integration tests
./mvnw verify

# Всі тести
./cicd/local/run-tests.sh
```

### 6. Створення Pull Request

```bash
# Push гілки
git push origin feature/your-feature-name

# Створіть PR через GitHub UI
```

## Git Workflow

Ми використовуємо структурований Git workflow. Детально читайте в [docs/GIT_WORKFLOW.md](docs/GIT_WORKFLOW.md).

**Коротко:**

- Використовуйте [Conventional Commits](https://www.conventionalcommits.org/)
- Створюйте feature branches від `main`
- Робіть атомарні коміти
- Синхронізуйтесь з `main` регулярно
- Не робіть force push до `main`

## Coding Standards

### Java Code Style

Ми дотримуємось [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) з деякими модифікаціями:

#### Форматування

```java
// ✅ ДОБРЕ
public class OrderService {
    private final OrderRepository orderRepository;
    private final RiskManager riskManager;
    
    public OrderService(OrderRepository orderRepository, RiskManager riskManager) {
        this.orderRepository = orderRepository;
        this.riskManager = riskManager;
    }
    
    public Order placeOrder(PlaceOrderRequest request) {
        // Validate request
        validateRequest(request);
        
        // Check risk
        riskManager.validateOrder(request);
        
        // Place order
        return orderRepository.save(createOrder(request));
    }
}
```

#### Naming Conventions

```java
// Classes: PascalCase
public class OrderService { }
public class RiskManager { }

// Methods: camelCase
public void placeOrder() { }
public Order findById() { }

// Variables: camelCase
private String orderId;
private double orderAmount;

// Constants: UPPER_SNAKE_CASE
public static final int MAX_RETRIES = 3;
public static final String DEFAULT_CURRENCY = "USDT";

// Packages: lowercase
package com.nebulamind.tradingcore.service;
```

#### Lombok Usage

```java
// ✅ ДОБРЕ - використовуйте для DTOs
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private String id;
    private String symbol;
    private double amount;
}

// ✅ ДОБРЕ - використовуйте для domain models з обережністю
@Getter
@Setter
@Builder
public class Order {
    private String id;
    private String symbol;
    private double amount;
    
    // Custom methods
    public boolean isValid() {
        return amount > 0 && symbol != null;
    }
}

// ❌ ПОГАНО - не використовуйте @Data для entities з логікою
@Data
public class Order {
    // equals/hashCode/toString можуть створити проблеми
}
```

#### Documentation

```java
/**
 * Service for managing trading orders with risk validation
 * 
 * <p>This service coordinates order placement with risk management
 * and ensures all orders comply with trading rules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    /**
     * Place a new trading order with risk validation
     * 
     * @param request Order placement request with symbol, amount, and risk policy
     * @return Placed order with execution details
     * @throws RiskValidationException if order violates risk rules
     * @throws InsufficientBalanceException if account balance is too low
     */
    public Order placeOrder(PlaceOrderRequest request) {
        // Implementation
    }
}
```

### Spring Boot Best Practices

#### Dependency Injection

```java
// ✅ ДОБРЕ - Constructor injection
@Service
@RequiredArgsConstructor  // Lombok generates constructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final RiskManager riskManager;
}

// ❌ ПОГАНО - Field injection
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RiskManager riskManager;
}
```

#### REST Controllers

```java
@RestController
@RequestMapping("/api/core/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Place a new order
     */
    @PostMapping("/place")
    public ResponseEntity<OrderDto> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        log.info("POST /api/core/orders/place: {}", request);
        
        Order order = orderService.placeOrder(request);
        OrderDto dto = OrderMapper.toDto(order);
        
        return ResponseEntity.ok(dto);
    }
    
    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable String orderId) {
        log.info("GET /api/core/orders/{}", orderId);
        
        return orderService.findById(orderId)
                .map(OrderMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

#### Exception Handling

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RiskValidationException.class)
    public ResponseEntity<ErrorResponse> handleRiskValidation(RiskValidationException ex) {
        log.error("Risk validation failed: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
                .error("RISK_VALIDATION_FAILED")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

### Testing Standards

#### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private RiskManager riskManager;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    void placeOrder_validRequest_success() {
        // Given
        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .symbol("BTCUSDT")
                .amount(1000.0)
                .build();
        
        Order expectedOrder = Order.builder()
                .id("order-123")
                .symbol("BTCUSDT")
                .amount(1000.0)
                .build();
        
        when(orderRepository.save(any())).thenReturn(expectedOrder);
        
        // When
        Order result = orderService.placeOrder(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("order-123");
        assertThat(result.getSymbol()).isEqualTo("BTCUSDT");
        
        verify(riskManager).validateOrder(request);
        verify(orderRepository).save(any());
    }
    
    @Test
    void placeOrder_riskViolation_throwsException() {
        // Given
        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .symbol("BTCUSDT")
                .amount(10000.0)  // Too large
                .build();
        
        doThrow(new RiskValidationException("Amount exceeds limit"))
                .when(riskManager).validateOrder(request);
        
        // When & Then
        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(RiskValidationException.class)
                .hasMessage("Amount exceeds limit");
        
        verify(riskManager).validateOrder(request);
        verify(orderRepository, never()).save(any());
    }
}
```

#### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void placeOrder_validRequest_returns200() throws Exception {
        // Given
        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .symbol("BTCUSDT")
                .amount(1000.0)
                .build();
        
        // When & Then
        mockMvc.perform(post("/api/core/orders/place")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BTCUSDT"))
                .andExpect(jsonPath("$.amount").value(1000.0));
    }
}
```

## Testing Guidelines

### Test Coverage

Мінімальні вимоги:
- **Unit tests**: 80% coverage для service layer
- **Integration tests**: критичні API endpoints
- **E2E tests**: основні user flows

### Running Tests

```bash
# Всі тести
./cicd/local/run-tests.sh

# Тільки unit tests
cd app/trading-core && ./mvnw test
cd app/agent-builder && ./mvnw test

# З coverage report
./mvnw test jacoco:report
# Report: target/site/jacoco/index.html
```

### Test Naming

```java
// Pattern: methodName_condition_expectedBehavior

placeOrder_validRequest_success()
placeOrder_invalidAmount_throwsException()
placeOrder_riskViolation_throwsException()
findById_existingId_returnsOrder()
findById_nonExistingId_returnsEmpty()
```

## Pull Request Process

### 1. Pre-submission Checklist

Перед створенням PR переконайтесь:

- [ ] Код компілюється без помилок
- [ ] Всі тести проходять
- [ ] Додані тести для нової функціональності
- [ ] Код відповідає style guide
- [ ] Документація оновлена
- [ ] CHANGELOG.md оновлено (для значних змін)
- [ ] Коміти відповідають Conventional Commits format
- [ ] Гілка синхронізована з main

```bash
# Checklist команди
./mvnw clean install          # Build успішний?
./cicd/local/run-tests.sh     # Тести проходять?
git log --oneline -5           # Коміти правильні?
```

### 2. PR Title and Description

**Title:** Використовуйте Conventional Commits format

```
feat(core): implement arbitrage chain finder
fix(agent): resolve timeout in portfolio tool
docs: update contribution guidelines
```

**Description:** Використовуйте шаблон

```markdown
## Зміни
- [опишіть зміни]

## Мотивація
[Чому це потрібно?]

## Тестування
- [ ] Unit tests
- [ ] Integration tests
- [ ] Manual testing

## Checklist
- [ ] Code compiles
- [ ] Tests pass
- [ ] Documentation updated
- [ ] CHANGELOG updated

## Related Issues
Closes #123
```

### 3. Code Review

- Адресуйте всі коментарі reviewer
- Будьте відкритими до feedback
- Робіть додаткові коміти в тій же гілці
- Після approval, можна мержити

### 4. Після Merge

```bash
# Оновіть local main
git checkout main
git pull origin main

# Видаліть feature branch
git branch -d feature/your-feature-name
git push origin --delete feature/your-feature-name
```

## Питання?

Якщо у вас є питання:

1. Перегляньте [документацію](docs/)
2. Пошукайте в [Issues](https://github.com/your-username/NebulaMind/issues)
3. Створіть нове issue з label `question`

## Ліцензія

Роблячи внесок у цей проєкт, ви погоджуєтесь що ваш код буде під MIT ліцензією.

---

**Дякуємо за ваш внесок у NebulaMind! 🎉**

