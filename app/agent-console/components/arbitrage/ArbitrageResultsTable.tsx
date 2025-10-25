'use client';

import { useState } from 'react';
import type { ArbitrageChain, ScanSummary } from '@/lib/types/arbitrage';

interface ArbitrageResultsTableProps {
  chains: ArbitrageChain[];
  summary: ScanSummary;
  onExecute: (chain: ArbitrageChain) => void;
}

export default function ArbitrageResultsTable({
  chains,
  summary,
  onExecute,
}: ArbitrageResultsTableProps) {
  const [expandedChainId, setExpandedChainId] = useState<string | null>(null);

  if (chains.length === 0) {
    return (
      <div className="bg-white shadow-md rounded-lg p-8 text-center">
        <svg
          className="mx-auto h-12 w-12 text-gray-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
        <h3 className="mt-2 text-sm font-medium text-gray-900">No profitable chains found</h3>
        <p className="mt-1 text-sm text-gray-500">
          Try adjusting your parameters (lower min profit %, increase max assets, or change base asset)
        </p>
      </div>
    );
  }

  const getProfitColorClass = (profit: number) => {
    if (profit >= 1.0) return 'text-green-600 font-bold';
    if (profit >= 0.5) return 'text-yellow-600 font-semibold';
    return 'text-gray-600';
  };

  const formatChainSteps = (steps: string[]) => {
    // Convert steps to readable format: BTCUSDT -> BTC/USDT
    return steps.map(step => {
      const match = step.match(/^([A-Z]+)([A-Z]+)$/);
      if (match) {
        return `${match[1]}/${match[2]}`;
      }
      return step;
    }).join(' → ');
  };

  return (
    <div className="bg-white shadow-md rounded-lg overflow-hidden">
      {/* Summary Header */}
      <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-medium text-gray-900">
              Found {summary.profitableChainsFound} profitable chain{summary.profitableChainsFound !== 1 ? 's' : ''}
            </h3>
            <p className="mt-1 text-sm text-gray-500">
              Analyzed {summary.analyzedAssets.length} assets • Best profit: {summary.bestProfitPercent.toFixed(2)}%
            </p>
          </div>
          <div className="text-right">
            <div className="text-2xl font-bold text-green-600">
              {summary.bestProfitPercent.toFixed(2)}%
            </div>
            <div className="text-xs text-gray-500">Max Profit</div>
          </div>
        </div>
      </div>

      {/* Chains Table */}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Chain
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Steps
              </th>
              <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Profit %
              </th>
              <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Min Amount
              </th>
              <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {chains.map((chain, index) => {
              const isExpanded = expandedChainId === chain.id;
              
              return (
                <tr key={chain.id} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <button
                        onClick={() => setExpandedChainId(isExpanded ? null : chain.id)}
                        className="mr-2 text-gray-400 hover:text-gray-600"
                      >
                        {isExpanded ? '▼' : '▶'}
                      </button>
                      <div>
                        <div className="text-sm font-medium text-gray-900">
                          #{index + 1}
                        </div>
                        <div className="text-xs text-gray-500" title={chain.id}>
                          {chain.id.substring(0, 8)}...
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    {isExpanded ? (
                      <div className="text-sm text-gray-900 space-y-1">
                        {chain.steps.map((step, i) => (
                          <div key={i}>
                            {i + 1}. {step}
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="text-sm text-gray-900">
                        {chain.baseAsset} → ... → {chain.baseAsset} ({chain.steps.length} steps)
                      </div>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right">
                    <div className={`text-sm ${getProfitColorClass(chain.profitPercent)}`}>
                      +{chain.profitPercent.toFixed(2)}%
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-900">
                    {chain.minRequiredAmount.toFixed(2)} {chain.baseAsset}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button
                      onClick={() => onExecute(chain)}
                      className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    >
                      Execute
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Analyzed Assets Footer */}
      <div className="bg-gray-50 px-6 py-3 border-t border-gray-200">
        <div className="text-xs text-gray-500">
          <span className="font-medium">Analyzed assets:</span> {summary.analyzedAssets.join(', ')}
        </div>
      </div>
    </div>
  );
}

