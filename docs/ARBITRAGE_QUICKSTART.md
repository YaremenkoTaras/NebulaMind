# Triangular Arbitrage - Quick Start Guide

## 🚀 Quick Test (5 minutes)

### 1. Start Backend Services

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
curl http://localhost:8081/api/core/health    # Should return: Trading Core is running
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
- Enter amount: 100
- Check "I understand the risks"
- Click "Execute"
- Should see success with actual profit

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
curl -X POST "http://localhost:8082/api/agent/arbitrage/chains/{chainId}/execute?baseAmount=100"
```

---

## 🧪 Run Tests

```bash
# Backend Tests (agent-builder)
cd /Users/tyaremenko/work/NebulaMind/app/agent-builder
./mvnw-java21.sh test

# Should see: Tests run: 9, Failures: 0, Errors: 0
# Including 7 ArbitrageToolsController tests

# Backend Tests (trading-core)  
cd /Users/tyaremenko/work/NebulaMind/app/trading-core
./mvnw-java21.sh test

# Should see: Tests run: 7, Failures: 0, Errors: 0
# Including 5 ArbitrageService tests
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

# Kill if needed
kill -9 <PID>
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

---

## ✅ Success Checklist

- [ ] Trading Core starts on :8081
- [ ] Agent Builder starts on :8082
- [ ] Agent Console starts on :3000
- [ ] Can access /arbitrage page
- [ ] Scan finds 3+ chains
- [ ] Results table shows data
- [ ] Can click Execute button
- [ ] Execution dialog opens
- [ ] All backend tests pass (16/16)

---

## 📚 Next Steps

- Read [User Guide](USER_GUIDE_ARBITRAGE.md) for detailed usage
- Read [Implementation Plan](TRIANGULAR_ARBITRAGE_IMPLEMENTATION.md) for technical details
- Check [Architecture](ARCHITECTURE.md) for system overview

---

**Need Help?** Check logs:
- Trading Core: `app/trading-core/logs/trading-core.log`
- Agent Builder: `app/agent-builder/logs/agent-builder.log`
- Frontend: Browser console (F12)

