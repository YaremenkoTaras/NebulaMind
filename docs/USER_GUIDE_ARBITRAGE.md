# Triangular Arbitrage - User Guide

## 📚 Table of Contents

1. [What is Triangular Arbitrage?](#what-is-triangular-arbitrage)
2. [How to Use](#how-to-use)
3. [Understanding Parameters](#understanding-parameters)
4. [Reading Results](#reading-results)
5. [Executing Trades](#executing-trades)
6. [Risk Management](#risk-management)
7. [Troubleshooting](#troubleshooting)
8. [FAQ](#faq)

---

## What is Triangular Arbitrage?

Triangular arbitrage is a trading strategy that exploits price differences across three different currencies or assets in a circular trading path.

### Example:
Imagine you have **100 USDT**:

1. **Buy BTC** with USDT → You get 0.002 BTC (@ 50,000 USDT/BTC)
2. **Buy ETH** with BTC → You get 0.05 ETH (@ 0.04 BTC/ETH)  
3. **Sell ETH** for USDT → You get 101.25 USDT (@ 2,025 USDT/ETH)

**Result:** You started with 100 USDT and ended with 101.25 USDT = **+1.25 USDT profit (+1.25%)**

This profit comes from temporary price inefficiencies in the market.

---

## How to Use

### Step 1: Access the Scanner

Navigate to **Arbitrage** section in the NebulaMind console:
```
http://localhost:3000/arbitrage
```

(or `http://localhost:3001/arbitrage` if port 3000 is occupied)

### Step 2: Configure Scan Parameters

Fill in the form with your desired parameters:

**Required Parameters:**
- **Base Asset**: The currency you currently hold (e.g., USDT, BTC)
- **Max Assets**: How many currencies to analyze (3-50)
- **Chain Length**: Number of trades in the chain (min 3 for triangular)
- **Min Profit %**: Minimum profit threshold to display

**Optional:**
- **Reasoning**: Note why you're scanning (for audit logs)

### Step 3: Scan for Opportunities

Click **"🔍 Scan for Opportunities"** button.

The system will:
- Analyze market rates for all trading pairs
- Find all possible circular chains
- Calculate profit for each chain
- Filter by your minimum profit threshold
- Sort results by profitability (highest first)

### Step 4: Review Results

Results are displayed in a table showing:
- **Chain steps**: The trading path (A → B → C → A)
- **Profit %**: Expected profit percentage
- **Min Amount**: Minimum amount required to execute
- **Timestamp**: When the opportunity was detected

### Step 5: Execute a Chain

Click **"Execute"** on a chain you want to trade.

A dialog will appear showing:
1. **Chain Summary**:
   - Base Asset
   - Number of steps
   - Expected Profit %
   - **⚠️ Minimum Required Amount** (IMPORTANT!)

2. **Amount Input**:
   - Enter amount to trade (must be ≥ minimum required)
   - See real-time expected profit calculation
   - UI validates your input against minimum

3. **Risk Confirmation**:
   - Check "I understand the risks" checkbox
   - Click "Execute" button

4. **Execution Progress**:
   - Shows "Executing trades..." with loading spinner
   - Executes all trades step-by-step

5. **Execution Results**:
   - ✅ **Status**: COMPLETED (or FAILED if error)
   - **Initial Amount**: What you started with
   - **Final Amount**: What you received
   - **Actual Profit**: In USDT and % (green if profitable, red if loss)
   - **Trade Steps**: Detailed execution for each step:
     - Symbol (e.g., BTCUSDT)
     - Quantity executed (e.g., 0.002 BTC)
     - Status (FILLED, PENDING, or FAILED)

---

## Understanding Parameters

### Base Asset
- **What it is**: The currency you start and end with
- **Requirements**: Must be a currency you actually have in your account
- **Examples**: USDT, BTC, ETH, BNB
- **Tip**: USDT is most common as a base asset

### Max Assets to Analyze
- **What it is**: Number of currencies to include in the analysis
- **Range**: 3 - 50
- **Trade-off**: 
  - **Lower (5-10)**: Faster scan, fewer opportunities
  - **Higher (20-50)**: Slower scan, more opportunities
- **Recommended**: Start with 10-20

### Chain Length
- **What it is**: Number of trades in the arbitrage chain
- **Minimum**: 3 (for triangular arbitrage)
- **Maximum**: 5 (for more complex paths)
- **Trade-off**:
  - **Length 3**: Simpler, fewer opportunities, lower profit
  - **Length 4-5**: More complex, more opportunities, higher potential profit BUT more fees
- **Recommended**: Start with 3

### Min Profit %
- **What it is**: Threshold to filter out low-profit chains
- **Range**: 0% and up
- **Considerations**:
  - Account for trading fees (~0.1% per trade)
  - Account for slippage (~0.1-0.5%)
  - For 3-step chain: aim for min 0.5-1% profit
- **Recommended**: 0.5% for beginners

---

## Reading Results

### Results Table Columns

#### Chain ID
- Unique identifier for this opportunity
- Used for execution tracking
- Shortened in UI (full ID visible on hover)

#### Steps
- Shows the trading path
- Format: `BTCUSDT → ETHBTC → ETHUSDT`
- Click ▶ to expand and see details

#### Profit %
- Expected profit percentage
- **Color coding**:
  - 🟢 **Green** (≥1%): High profit, good opportunity
  - 🟡 **Yellow** (0.5-1%): Moderate profit, acceptable
  - ⚪ **White** (<0.5%): Low profit, risky after fees

#### Min Amount
- Minimum base asset amount required
- Based on exchange quantity limits
- You need at least this much to execute

#### Timestamp
- When this opportunity was detected
- **Important**: Rates change constantly!
- Old opportunities (>1 min) may no longer be valid

### Summary Section
- **Total chains found**: All profitable chains matching criteria
- **Best profit**: Highest profit % in results
- **Analyzed assets**: Which currencies were included

---

## Executing Trades

### Before Executing

✅ **Checklist:**
- [ ] Chain profit > trading fees (usually 0.3% total for 3 trades)
- [ ] Timestamp is recent (<1 minute old)
- [ ] You have enough balance (>= Min Amount)
- [ ] Market is not highly volatile
- [ ] You understand the risks

### Execution Dialog

When you click "Execute", you'll see:

1. **Chain Summary**
   - Base asset
   - Number of steps
   - Expected profit %
   - Minimum required amount

2. **Amount Input**
   - Enter how much to trade
   - System calculates expected final amount
   - Shows expected profit in $ and %

3. **Risk Warning** ⚠️
   - Rates may change during execution
   - Actual profit may differ
   - Operation is irreversible

4. **Confirmation**
   - Must check "I understand the risks"
   - Click "Execute" to proceed

### During Execution

- **Spinner** appears with "Executing trades..."
- Trades are executed sequentially
- Usually takes 2-10 seconds

### After Execution

**Success Screen** shows:
- ✅ **Execution Successful!** message (green background)
- **Initial Amount**: What you started with (e.g., 100.00 USDT)
- **Final Amount**: What you ended with (e.g., 100.78 USDT)
- **Actual Profit**: Difference in USDT (e.g., +0.78 USDT)
- **Actual Profit %**: Percentage gain/loss (e.g., +0.78%)
- **Color coding**: Green for profit, Red for loss

**Trade Steps Detail**:
Each step shows:
- ✅ **Success**: Final amount, actual profit, all trade details
- ❌ **Failure**: Error message, what went wrong
- ⚠️ **Cancelled**: If you cancelled mid-execution

---

## Risk Management

### Understanding Risks

1. **Market Risk**
   - Prices can change between scan and execution
   - High volatility increases risk
   - "Actual profit may differ from expected"

2. **Execution Risk**
   - One trade might fail mid-chain
   - You could be left holding an unwanted asset
   - Order might not fill completely

3. **Fee Risk**
   - Trading fees eat into profit
   - Slippage on large orders
   - Minimum profit should cover fees

4. **Liquidity Risk**
   - Low liquidity = high slippage
   - Large orders move the price
   - Check market depth before large trades

### Best Practices

✅ **DO:**
- **Always check Minimum Required Amount** before execution
- Start with small amounts (10-100 USDT) but above minimum
- Target profits ≥ 1% for safety margin
- Execute chains with recent timestamps (<1 minute old)
- Use liquid major assets (BTC, ETH, BNB, USDT)
- Monitor your first few executions closely
- Read error messages carefully (they tell you exactly what's needed)

❌ **DON'T:**
- Don't trade during extreme volatility
- Don't use your entire balance
- Don't chase tiny profits (<0.5%)
- Don't execute old opportunities (>2 min)
- Don't ignore the risk warnings
- **Don't try to execute with less than minimum required amount**
  - Error: "Please use at least X USDT (currently using Y USDT)"
  - Solution: Increase your amount or choose different chain

### Position Sizing

**Conservative:**
- Use 1-5% of your total portfolio per trade
- Example: $1,000 portfolio → max $50 per trade

**Moderate:**
- Use 5-10% of portfolio
- Example: $1,000 portfolio → max $100 per trade

**Aggressive:**
- Use 10-20% of portfolio  
- ⚠️ Only for experienced traders

---

## Troubleshooting

### "No profitable chains found"

**Possible causes:**
- Min profit % is too high → Try lowering it
- Max assets is too low → Try increasing to 20-30
- Market is efficient (no arbitrage) → Try different base asset
- Wrong base asset → Try USDT instead

**Solutions:**
```
1. Lower minProfitPercent to 0.3%
2. Increase maxAssets to 30
3. Try different base asset (USDT, BTC)
4. Wait for market volatility
```

### "Execution failed"

**Common reasons:**
- Insufficient balance
- Rate changed (chain no longer profitable)
- Exchange connectivity issue
- Order size below minimum

**What to do:**
1. Check error message for details
2. Verify your balance
3. Try scanning again for fresh opportunities
4. Check exchange status

### "Base amount must be positive"

**Cause:** You entered 0 or negative amount

**Solution:** Enter a positive number ≥ minimum required amount

### "Insufficient amount" / "Invalid quantity"

**Error example:**
```
Insufficient amount for BTCUSDT: quantity 0.00020017 is below minimum 0.001.
Please use at least 50 USDT (currently using 10 USDT)
```

**Cause:** The amount you entered is too small for the exchange's minimum quantity limits

**Solutions:**
1. **Check Min Required Amount** in the execution dialog (shows before you enter amount)
2. **Increase your amount** to at least the minimum shown
3. **Choose different chain** if the minimum is too high for your budget
4. **UI Protection**: The input field shows the minimum and validates your entry

**Why this happens:**
- Each trading pair has minimum order sizes (e.g., 0.001 BTC)
- Your base amount gets converted through multiple steps
- If any step falls below its minimum, execution fails
- System now calculates and shows you the required minimum upfront

### API Connection Errors

**Symptoms:**
- "Failed to load assets"
- "Scan request failed"
- Timeout errors

**Solutions:**
1. **Restart all services** (easiest):
   ```bash
   cd /Users/tyaremenko/work/NebulaMind
   ./cicd/local/restart-local.sh
   ```

2. Check backend services are running:
   ```bash
   # Check trading-core
   curl http://localhost:8081/actuator/health
   
   # Check agent-builder  
   curl http://localhost:8082/api/agent/health
   ```

3. View logs for errors:
   ```bash
   tail -f /tmp/trading-core.log
   tail -f /tmp/agent-builder.log
   ```

2. Verify `.env.local` has correct URL:
   ```
   NEXT_PUBLIC_AGENT_BUILDER_URL=http://localhost:8082
   ```

3. Check browser console for errors (F12)

---

## FAQ

### Q: How often should I scan?

**A:** Every 1-5 minutes during active trading hours. Market conditions change constantly.

### Q: What's a good profit percentage?

**A:** For 3-step chains:
- **0.5-1%**: Acceptable, covers fees
- **1-2%**: Good opportunity
- **>2%**: Excellent, but verify it's real!

### Q: Can I lose money?

**A:** Yes, if:
- Prices move against you during execution
- Fees exceed profit
- One trade fails mid-chain

Always start small to test.

### Q: How long do opportunities last?

**A:** Usually 10-60 seconds. Execute quickly or they disappear.

### Q: Should I use LLM asset selection?

**A:** Optional feature:
- **Pros**: AI selects most promising assets
- **Cons**: Requires OpenAI API key, slower scan
- **Recommendation**: Manual selection works fine for start

### Q: What's the minimum amount to start?

**A:** 10-20 USDT is safe for testing. Exchange minimums vary (typically 5-10 USDT).

### Q: Can I run multiple chains simultaneously?

**A:** Not recommended - you might not have enough balance for all, and it increases risk.

### Q: How do I maximize profit?

**A:**
1. Scan frequently (every 2-3 minutes)
2. Execute quickly (within 30 seconds of scan)
3. Use liquid assets (BTC, ETH, BNB)
4. Increase max assets (20-30)
5. Monitor during high volatility periods

### Q: Is this profitable in real trading?

**A:** It can be, but:
- Competition is high (bots are faster)
- Fees reduce profit significantly
- Real slippage differs from estimates
- **Start in sandbox mode first!**

---

## Support

**Documentation:**
- [Architecture Overview](ARCHITECTURE.md)
- [Technical Implementation](TRIANGULAR_ARBITRAGE_IMPLEMENTATION.md)
- [API Reference](http://localhost:8082/swagger-ui.html)

**Need Help?**
- Check logs: `/app/agent-builder/logs/agent-builder.log`
- Report issues on GitHub
- Contact: support@nebulamind.io

---

**⚠️ Disclaimer:** Cryptocurrency trading carries risk. This tool is for educational purposes. Always use sandbox mode first. Never trade with money you can't afford to lose.

