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
  actualProfit?: number;
  actualProfitPercent?: number;
  steps?: Array<{
    symbol: string;
    amount: number;
    status: string;
  }>;
  errorMessage?: string;
}

