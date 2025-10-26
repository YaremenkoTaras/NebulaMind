# Triangular Arbitrage - Quick Start Guide

## 🚀 Quick Test (5 minutes)

### 1. Start All Services (Fastest Method)

```bash
# One command to start everything!
cd /Users/tyaremenko/work/NebulaMind
./cicd/local/restart-local.sh
```

This script will:
- Build both backend services
- Start Trading Core (port 8081)
- Start Agent Builder (port 8082)
- Wait for health checks
- Show you the status

**Alternative: Manual Start**

```bash
# Terminal 1: Trading Core
cd /Users/tyaremenko/work/NebulaMind/app/trading-core
./mvnw-java21.sh spring-boot:run

# Terminal 2: Agent Builder  
cd /Users/tyaremenko/work/NebulaMind/app/agent-builder
./mvnw-java21.sh spring-boot:run
```

Wait for both services to start (look for "Started ... in X seconds").

**Verify:**
```bash
curl http://localhost:8081/actuator/health    # Should return: {"status":"UP"}
curl http://localhost:8082/api/agent/health   # Should return: Agent Builder is running
```

### 2. Start Frontend

```bash
# Terminal 3: Agent Console
cd /Users/tyaremenko/work/NebulaMind/app/agent-console
npm run dev
```

Open browser: http://localhost:3000/arbitrage

### 3. Test the Feature

**Step 1: Scan for Opportunities**
- Base Asset: USDT
- Max Assets: 10
- Chain Length: 3
- Min Profit %: 0.5
- Click "🔍 Scan for Opportunities"

**Step 2: View Results**
- Should see 3-5 profitable chains
- Profit % ranges from 0.5% to 2%
- Each chain shows trading steps

**Step 3: Execute a Chain** (optional)
- Click "Execute" on highest profit chain
- **Check minimum required amount** (shown in dialog)
- Enter amount: 100 (or the minimum required)
- Check "I understand the risks"
- Click "Execute"
- Should see:
  - ✅ Execution Complete status
  - Initial and Final amounts
  - Actual Profit (in USDT and %)
  - Step-by-step execution details with quantities and status (FILLED)

---

## 📊 API Testing (cURL)

### Scan for Opportunities

```bash
curl -X POST http://localhost:8082/api/agent/arbitrage/scan \
  -H "Content-Type: application/json" \
  -d '{
    "baseAsset": "USDT",
    "maxAssets": 10,
    "chainLength": 3,
    "minProfitPercent": 0.5,
    "reasoning": "Testing API"
  }'
```

### Get Available Assets

```bash
curl http://localhost:8082/api/agent/arbitrage/assets
```

### Execute Chain (replace {chainId})

```bash
curl -X POST "http://localhost:8082/api/agent/arbitrage/chains/{chainId}/execute" \
  -H "Content-Type: application/json" \
  -d '{"baseAmount": 100.0}'
```

**Response includes:**
- `status`: COMPLETED, FAILED, or EXECUTING
- `initialAmount`: Amount used for execution
- `finalAmount`: Amount received after all steps
- `profitPercent`: Actual profit percentage
- `steps[]`: Array with execution details for each step
  - `amount`: Quantity executed
  - `status`: FILLED, PENDING, or FAILED

---

## 🧪 Run Tests

```bash
# Backend Tests (agent-builder)
cd /Users/tyaremenko/work/NebulaMind/app/agent-builder
./mvnw-java21.sh test

# Should see: Tests run: 9, Failures: 0, Errors: 0
# Including ArbitrageToolsController tests

# Backend Tests (trading-core)  
cd /Users/tyaremenko/work/NebulaMind/app/trading-core
./mvnw-java21.sh test

# Should see: Tests run: 10, Failures: 0, Errors: 0
# Including ArbitrageService integration tests with:
# - Chain registration tests
# - Scan and execute flow tests
# - Quantity calculation tests
```

**Quick test script:**
```bash
cd /Users/tyaremenko/work/NebulaMind
./cicd/local/run-tests.sh
```

---

## 📖 API Documentation

**Swagger UI:** http://localhost:8082/swagger-ui.html

Endpoints:
- `POST /api/agent/arbitrage/scan` - Scan for chains
- `GET /api/agent/arbitrage/assets` - List assets
- `POST /api/agent/arbitrage/chains/{id}/execute` - Execute chain

---

## 🐛 Troubleshooting

### Backend won't start
```bash
# Check Java version
java -version  # Should be 21+

# Check ports
lsof -i :8081  # trading-core
lsof -i :8082  # agent-builder

# Kill and restart all services
cd /Users/tyaremenko/work/NebulaMind
./cicd/local/restart-local.sh

# Or restart just one service:
./cicd/local/restart-local.sh core   # Only trading-core
./cicd/local/restart-local.sh agent  # Only agent-builder
```

**View logs:**
```bash
tail -f /tmp/trading-core.log
tail -f /tmp/agent-builder.log
```

### Frontend won't start
```bash
# Install dependencies
cd /Users/tyaremenko/work/NebulaMind/app/agent-console
npm install

# Check Node version
node -v  # Should be 18+

# Clear cache
rm -rf .next node_modules
npm install
```

### No chains found
- Lower `minProfitPercent` to 0.1
- Increase `maxAssets` to 20
- Try different `baseAsset` (BTC instead of USDT)

### API connection errors
- Verify all 3 services running
- Check `.env.local` has correct URL
- Clear browser cache (F5)

### "Insufficient amount" error during execution
- **Problem**: Chain requires more than you're trying to use
- **Solution**: Check the **Min Required Amount** shown in the execution dialog
- UI shows minimum required amount for each chain
- Error message will tell you exactly how much you need
- Example: "Please use at least 50 USDT (currently using 10 USDT)"

---

## ✅ Success Checklist

- [ ] Trading Core starts on :8081
- [ ] Agent Builder starts on :8082
- [ ] Agent Console starts on :3000
- [ ] Can access /arbitrage page
- [ ] Scan finds 3+ chains
- [ ] Results table shows data with profit percentages
- [ ] Can click Execute button
- [ ] Execution dialog opens and shows:
  - [ ] Chain summary with min required amount
  - [ ] Amount input with validation
  - [ ] Expected profit calculation
- [ ] Execution completes successfully showing:
  - [ ] Initial and Final amounts
  - [ ] Actual Profit (in USDT and %)
  - [ ] Step-by-step execution details (qty + status)
- [ ] All backend tests pass (19/19 total, 10 in trading-core)

---

## 📚 Next Steps

- Read [User Guide](USER_GUIDE_ARBITRAGE.md) for detailed usage
- Read [Implementation Plan](TRIANGULAR_ARBITRAGE_IMPLEMENTATION.md) for technical details
- Check [Architecture](ARCHITECTURE.md) for system overview

---

**Need Help?** Check logs:
- Trading Core: `/tmp/trading-core.log` (or `tail -f /tmp/trading-core.log`)
- Agent Builder: `/tmp/agent-builder.log` (or `tail -f /tmp/agent-builder.log`)
- Frontend: Browser console (F12)

**Quick Commands:**
```bash
# Restart all services
./cicd/local/restart-local.sh

# View live logs
tail -f /tmp/trading-core.log /tmp/agent-builder.log
```

