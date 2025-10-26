/**
 * TypeScript types for Arbitrage API
 */

export interface ArbitrageScanRequest {
  baseAsset: string;
  maxAssets: number;
  chainLength: number;
  minProfitPercent: number;
  reasoning?: string;
}

export interface ArbitrageChain {
  id: string;
  baseAsset: string;
  steps: string[]; // List of symbols
  profitPercent: number;
  minRequiredAmount: number;
  timestamp: string;
}

export interface ScanSummary {
  totalChainsFound: number;
  profitableChainsFound: number;
  bestProfitPercent: number;
  analyzedAssets: string[];
}

export interface ArbitrageScanResponse {
  chains: ArbitrageChain[];
  summary: ScanSummary;
}

export interface ExecutionResult {
  chainId: string;
  status: 'EXECUTING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  initialAmount: number;
  finalAmount?: number;
  profitPercent?: number;
  steps?: Array<{
    symbol: string;
    amount: number;
    status: string;
  }>;
  errorMessage?: string;
}

// Task-related types
export type TaskStatus = 'PENDING' | 'RUNNING' | 'STOPPED' | 'COMPLETED' | 'FAILED';

export interface TaskCreateRequest {
  baseAsset: string;
  budget: number; // default 100
  executionTimeMinutes: number; // default 5
  delaySeconds: number; // default 10
  minProfitPercent: number; // default 1.0
  maxAssets: number;
  chainLength: number;
  slippageTolerance?: number; // default 1.0
  maxLossPerTrade?: number; // default 1.0
  enableCircuitBreaker?: boolean; // default true
  enableSmartSizing?: boolean; // default true
}

export interface ArbitrageExecution {
  id: string;
  chainId: string;
  chain: ArbitrageChain;
  timestamp: string;
  initialAmount: number;
  finalAmount: number;
  profitAmount: number;
  profitPercent: number;
  expectedProfitPercent: number; // Predicted profit at scan time
  status: 'COMPLETED' | 'FAILED';
  errorMessage?: string;
}

export interface Task {
  id: string;
  status: TaskStatus;
  baseAsset: string;
  budget: number;
  currentBudget: number;
  executionTimeMinutes: number;
  delaySeconds: number;
  minProfitPercent: number;
  maxAssets: number;
  chainLength: number;
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
  totalProfit: number;
  totalLoss: number;
  executionsCount: number;
  executions: ArbitrageExecution[];
  // Advanced settings
  slippageTolerance?: number;
  maxLossPerTrade?: number;
  enableCircuitBreaker?: boolean;
  enableSmartSizing?: boolean;
  // Runtime tracking
  consecutiveLosses?: number;
  consecutiveWins?: number;
  maxDrawdown?: number;
  stoppedReason?: string;
}

export interface TaskStatistics {
  task: Task;
  profitableExecutions: ArbitrageExecution[];
  lossExecutions: ArbitrageExecution[];
  totalProfit: number;
  totalLoss: number;
  netProfit: number;
}

