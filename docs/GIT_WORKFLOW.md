# Git Workflow та GitHub правила для NebulaMind

## 📋 Загальні принципи

### Робота з гілками

**Основні гілки:**
- `main` - production-ready код, завжди має бути стабільним
- `develop` - інтеграційна гілка для поточної розробки (якщо використовується)
- `feature/*` - гілки для нових функцій
- `bugfix/*` - гілки для виправлення багів
- `hotfix/*` - термінові виправлення для production

### Naming Convention для гілок

```bash
# Нові функції
feature/triangular-arbitrage
feature/llm-integration
feature/binance-adapter

# Виправлення багів
bugfix/risk-manager-validation
bugfix/order-execution-timeout

# Hotfix для production
hotfix/critical-security-patch

# Експериментальні гілки
experiment/new-algorithm-test
```

## 🔄 Git Flow

### 1. Створення нової гілки

```bash
# Переконайтеся що main актуальний
git checkout main
git pull origin main

# Створіть гілку від main
git checkout -b feature/your-feature-name

# Або від develop (якщо використовується)
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name
```

### 2. Робота в гілці

```bash
# Робіть коміти часто, але логічно
git add <files>
git commit -m "feat: add arbitrage chain validator"

# Синхронізуйтесь з main регулярно
git fetch origin
git rebase origin/main

# Або merge якщо rebase неможливий
git merge origin/main
```

### 3. Push та Pull Request

```bash
# Push вашої гілки
git push origin feature/your-feature-name

# Створіть Pull Request через GitHub UI
# Заповніть шаблон PR з описом змін
```

## 📝 Commit Messages

Використовуємо [Conventional Commits](https://www.conventionalcommits.org/) format:

### Формат

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat:` - нова функціональність
- `fix:` - виправлення бага
- `docs:` - зміни в документації
- `style:` - форматування коду (не впливає на логіку)
- `refactor:` - рефакторинг коду
- `perf:` - поліпшення продуктивності
- `test:` - додавання або виправлення тестів
- `build:` - зміни в build системі або залежностях
- `ci:` - зміни в CI/CD конфігурації
- `chore:` - інші зміни (оновлення залежностей тощо)
- `revert:` - відміна попереднього коміту

### Scope (опціонально)

- `core` - trading-core модуль
- `agent` - agent-builder модуль
- `console` - agent-console модуль
- `cicd` - CI/CD конфігурація
- `docs` - документація

### Приклади

```bash
# Додавання нової функції
git commit -m "feat(core): implement triangular arbitrage chain finder"

# Виправлення бага
git commit -m "fix(agent): resolve null pointer in portfolio tool"

# Рефакторинг
git commit -m "refactor(core): extract risk validation to separate service"

# Документація
git commit -m "docs: add git workflow guidelines"

# Тести
git commit -m "test(core): add unit tests for ArbitrageChain validator"

# CI/CD
git commit -m "ci: add automated deployment workflow for fly.io"

# Багаторядковий коміт з деталями
git commit -m "feat(core): implement arbitrage opportunity finder

- Add ArbitrageAnalyzer port interface
- Implement SandboxArbitrageAnalyzer adapter
- Add graph-based chain finding algorithm
- Support configurable chain length and profit threshold

Closes #42"
```

## 🔀 Pull Request процес

### 1. Перед створенням PR

```bash
# Переконайтеся що всі тести проходять
./cicd/local/run-tests.sh

# Перевірте код стиль
cd app/trading-core && ./mvnw spotless:check
cd app/agent-builder && ./mvnw spotless:check

# Синхронізуйтесь з main
git fetch origin
git rebase origin/main
git push origin feature/your-feature-name --force-with-lease
```

### 2. Створення PR

**Назва PR:** Використовуйте той самий формат що і для commits

```
feat(core): implement triangular arbitrage
fix(agent): resolve timeout in order execution
docs: update deployment guide
```

**Опис PR має включати:**

```markdown
## Зміни
- Короткий список змін
- Що було додано/виправлено/змінено

## Мотивація
Чому ці зміни необхідні?

## Тестування
- [ ] Unit tests додані/оновлені
- [ ] Integration tests пройшли
- [ ] Мануальне тестування виконано

## Screenshots (якщо UI зміни)
[додайте screenshots тут]

## Checklist
- [ ] Код дотримується style guide
- [ ] Тести проходять локально
- [ ] Документація оновлена
- [ ] CHANGELOG.md оновлено (якщо потрібно)

## Related Issues
Closes #123
Relates to #456
```

### 3. Code Review

**Для автора PR:**
- Відповідайте на коментарі швидко
- Не беріть негативний фідбек особисто
- Робіть коміти для виправлень у тій же гілці
- Після approval, зробіть squash непотрібних "fix review" комітів

**Для reviewer:**
- Будьте конструктивними та ввічливими
- Питайте, а не наказуйте
- Фокусуйтесь на коді, а не на людині
- Схвалюйте PR тільки якщо впевнені

### 4. Merge стратегії

**Для feature branches:**
- **Squash and merge** - для простих features
- **Rebase and merge** - для складних features з логічними комітами
- **Merge commit** - якщо потрібна історія всіх комітів

**Для hotfixes:**
- **Merge commit** - для збереження історії

```bash
# Після merge
git checkout main
git pull origin main
git branch -d feature/your-feature-name  # Видалити локальну гілку
git push origin --delete feature/your-feature-name  # Видалити remote гілку (якщо не видалена автоматично)
```

## 🚫 Заборонені практики

### ❌ НІКОЛИ не робіть:

1. **Force push до main/develop**
   ```bash
   # ❌ ЗАБОРОНЕНО
   git push origin main --force
   ```

2. **Commit без повідомлення**
   ```bash
   # ❌ ЗАБОРОНЕНО
   git commit -m "fix"
   git commit -m "update"
   git commit -m "wip"
   ```

3. **Commit sensitive data**
   ```bash
   # ❌ ЗАБОРОНЕНО - не комітьте:
   - API keys, passwords, tokens
   - .env файли з секретами
   - Private keys
   ```

4. **Працювати безпосередньо в main**
   ```bash
   # ❌ ЗАБОРОНЕНО
   git checkout main
   # робити зміни тут
   git commit -m "changes"
   ```

5. **Merge без review**
   - Завжди чекайте на принаймні 1 approval

6. **Пропускати CI/CD перевірки**
   - Не використовуйте `--no-verify`
   - Не мержте якщо тести не проходять

## ✅ Best Practices

### 1. Атомарні коміти

Кожен коміт має бути логічною одиницею зміни:

```bash
# ✅ ДОБРЕ - один коміт на одну зміну
git commit -m "feat(core): add ArbitrageChain domain model"
git commit -m "feat(core): add ArbitrageAnalyzer port interface"
git commit -m "test(core): add tests for ArbitrageChain"

# ❌ ПОГАНО - всі зміни в одному коміті
git commit -m "add arbitrage feature with tests and docs"
```

### 2. Синхронізація з main

```bash
# Щодня оновлюйте вашу гілку
git fetch origin
git rebase origin/main

# Або якщо є конфлікти
git merge origin/main
git commit -m "merge: sync with main"
```

### 3. Чистий git history

Перед створенням PR, "почистіть" історію:

```bash
# Інтерактивний rebase для останніх 5 комітів
git rebase -i HEAD~5

# Виберіть операції:
# pick - залишити коміт як є
# squash - з'єднати з попереднім комітом
# reword - змінити commit message
# drop - видалити коміт
```

### 4. Використовуйте .gitignore

Переконайтеся що `.gitignore` налаштований правильно:

```bash
# Перевірте що не комітите зайве
git status

# Додайте до .gitignore якщо потрібно
echo "*.log" >> .gitignore
git add .gitignore
git commit -m "chore: update gitignore"
```

## 🔧 Корисні Git команди

### Перегляд історії

```bash
# Показати останні 10 комітів
git log --oneline -10

# Показати зміни в файлах
git log --stat

# Показати графічну історію
git log --graph --oneline --all
```

### Скасування змін

```bash
# Скасувати незакомічені зміни в файлі
git checkout -- <file>

# Скасувати останній коміт (зберегти зміни)
git reset --soft HEAD~1

# Скасувати останній коміт (видалити зміни)
git reset --hard HEAD~1

# Створити revert коміт
git revert <commit-hash>
```

### Робота з stash

```bash
# Зберегти поточні зміни
git stash save "work in progress"

# Показати список stash
git stash list

# Застосувати останній stash
git stash apply

# Застосувати і видалити stash
git stash pop

# Видалити stash
git stash drop stash@{0}
```

### Виправлення помилок

```bash
# Змінити останній commit message
git commit --amend -m "new message"

# Додати файли до останнього коміту
git add forgotten_file.java
git commit --amend --no-edit

# Змінити автора останнього коміту
git commit --amend --author="Name <email@example.com>"
```

## 🤝 GitHub Features

### Issues

Використовуйте GitHub Issues для:
- Bug reports
- Feature requests
- Технічні питання
- Планування роботи

**Labels:**
- `bug` - баг
- `enhancement` - нова функція
- `documentation` - документація
- `good first issue` - для новачків
- `help wanted` - потрібна допомога
- `priority: high` - високий пріоритет

### Projects

Використовуйте GitHub Projects для планування:
- Backlog
- In Progress
- In Review
- Done

### Milestones

Групуйте issues за версіями:
- v0.1.0 - MVP
- v0.2.0 - Binance Integration
- v0.3.0 - LLM Integration

## 🔐 Security

### Secrets Management

```bash
# НІКОЛИ не комітьте секрети
# Використовуйте .env файли (додані в .gitignore)
cp cicd/local/env.example .env
# Редагуйте .env з вашими секретами

# Для GitHub Actions використовуйте Secrets
# Settings -> Secrets and variables -> Actions
```

### Sensitive Data в історії

Якщо випадково закомітили секрети:

```bash
# 1. Змініть скомпрометовані credentials НЕГАЙНО
# 2. Видаліть з історії (якщо ще не запушили)
git reset --hard HEAD~1

# 3. Якщо вже запушили, використовуйте git filter-branch
# або BFG Repo-Cleaner (рекомендовано)
```

## 📊 Метрики якості

Перед merge перевірте:

- [ ] Всі тести проходять
- [ ] Code coverage не зменшився
- [ ] Немає linter warnings
- [ ] Документація оновлена
- [ ] CHANGELOG.md оновлено
- [ ] Коміти мають правильний формат
- [ ] PR має опис та checklist

## 🚀 Continuous Integration

### GitHub Actions

Автоматично виконується при:
- Push до будь-якої гілки
- Створенні Pull Request
- Merge до main

**Checks:**
- Build всіх модулів
- Unit tests
- Integration tests
- Code style перевірка
- Security scan

**Deployment:**
- Автоматичний deploy до Fly.io при merge до main

## 📚 Додаткові ресурси

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Git Flow](https://nvie.com/posts/a-successful-git-branching-model/)
- [GitHub Flow](https://guides.github.com/introduction/flow/)
- [Writing Good Commit Messages](https://chris.beams.io/posts/git-commit/)

---

**Пам'ятайте:** Чистий git history = щаслива команда! 🎉

