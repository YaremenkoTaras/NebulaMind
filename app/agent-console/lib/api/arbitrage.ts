/**
 * API Client for Arbitrage operations
 */

import {
  ArbitrageScanRequest,
  ArbitrageScanResponse,
  ExecutionResult,
} from '../types/arbitrage';

const API_BASE_URL = process.env.NEXT_PUBLIC_AGENT_BUILDER_URL || 'http://localhost:8082';

class ArbitrageApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public details?: any
  ) {
    super(message);
    this.name = 'ArbitrageApiError';
  }
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new ArbitrageApiError(
      error.message || `API Error: ${response.status}`,
      response.status,
      error
    );
  }
  return response.json();
}

export const arbitrageApi = {
  /**
   * Scan for arbitrage opportunities
   */
  async scan(request: ArbitrageScanRequest): Promise<ArbitrageScanResponse> {
    const response = await fetch(`${API_BASE_URL}/api/agent/arbitrage/scan`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    return handleResponse<ArbitrageScanResponse>(response);
  },

  /**
   * Get available assets for trading
   */
  async getAssets(): Promise<string[]> {
    const response = await fetch(`${API_BASE_URL}/api/agent/arbitrage/assets`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    return handleResponse<string[]>(response);
  },

  /**
   * Execute arbitrage chain
   */
  async executeChain(chainId: string, baseAmount: number): Promise<ExecutionResult> {
    const response = await fetch(
      `${API_BASE_URL}/api/agent/arbitrage/chains/${chainId}/execute?baseAmount=${baseAmount}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );

    return handleResponse<ExecutionResult>(response);
  },
};

export { ArbitrageApiError };

