'use client';

import { useState } from 'react';
import type { ArbitrageChain, ExecutionResult } from '@/lib/types/arbitrage';
import { arbitrageApi, ArbitrageApiError } from '@/lib/api/arbitrage';

interface ExecuteChainDialogProps {
  chain: ArbitrageChain | null;
  onClose: () => void;
}

export default function ExecuteChainDialog({ chain, onClose }: ExecuteChainDialogProps) {
  // Use chain's minRequiredAmount as default, but at least 10
  const defaultAmount = chain ? Math.max(10, chain.minRequiredAmount || 10) : 10;
  const [baseAmount, setBaseAmount] = useState(defaultAmount);
  const [confirmed, setConfirmed] = useState(false);
  const [executing, setExecuting] = useState(false);
  const [result, setResult] = useState<ExecutionResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  if (!chain) return null;

  const expectedProfit = (baseAmount * chain.profitPercent) / 100;
  const expectedFinalAmount = baseAmount + expectedProfit;

  const handleExecute = async () => {
    if (!confirmed) {
      alert('Please confirm that you understand the risks');
      return;
    }

    if (baseAmount < chain.minRequiredAmount) {
      alert(`Amount must be at least ${chain.minRequiredAmount} ${chain.baseAsset}`);
      return;
    }

    setExecuting(true);
    setError(null);

    try {
      const executionResult = await arbitrageApi.executeChain(chain.id, baseAmount);
      setResult(executionResult);
    } catch (err) {
      if (err instanceof ArbitrageApiError) {
        setError(err.message);
      } else {
        setError('An unexpected error occurred');
      }
      setExecuting(false);
    }
  };

  const handleClose = () => {
    if (!executing) {
      setResult(null);
      setError(null);
      setConfirmed(false);
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-medium text-gray-900">
              {result ? 'Execution Complete' : 'Execute Arbitrage Chain'}
            </h3>
            <button
              onClick={handleClose}
              disabled={executing}
              className="text-gray-400 hover:text-gray-500 disabled:cursor-not-allowed"
            >
              <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        {/* Body */}
        <div className="px-6 py-4 space-y-4">
          {!result && !executing && (
            <>
              {/* Chain Summary */}
              <div className="bg-gray-50 rounded-lg p-4">
                <h4 className="text-sm font-medium text-gray-700 mb-2">Chain Summary</h4>
                <div className="space-y-1 text-sm text-gray-600">
                  <div>Base Asset: <span className="font-semibold">{chain.baseAsset}</span></div>
                  <div>Steps: <span className="font-semibold">{chain.steps.length}</span></div>
                  <div>Expected Profit: <span className="font-semibold text-green-600">+{chain.profitPercent.toFixed(2)}%</span></div>
                  <div>Min Amount: <span className="font-semibold">{chain.minRequiredAmount.toFixed(2)} {chain.baseAsset}</span></div>
                </div>
              </div>

              {/* Amount Input */}
              <div>
                <label htmlFor="baseAmount" className="block text-sm font-medium text-gray-700 mb-2">
                  Amount to Trade ({chain.baseAsset})
                </label>
                <input
                  id="baseAmount"
                  type="number"
                  min={chain.minRequiredAmount}
                  step="0.01"
                  value={baseAmount}
                  onChange={(e) => setBaseAmount(parseFloat(e.target.value))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <p className="mt-1 text-sm text-gray-500">
                  Expected final amount: {expectedFinalAmount.toFixed(2)} {chain.baseAsset} 
                  <span className="text-green-600 font-semibold ml-2">
                    (+{expectedProfit.toFixed(2)} {chain.baseAsset})
                  </span>
                </p>
              </div>

              {/* Risk Warning */}
              <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                      <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-yellow-800">Risk Warning</h3>
                    <div className="mt-2 text-sm text-yellow-700">
                      <ul className="list-disc list-inside space-y-1">
                        <li>Market rates may change during execution</li>
                        <li>Actual profit may differ from estimated</li>
                        <li>This operation is irreversible once started</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>

              {/* Confirmation Checkbox */}
              <div className="flex items-center">
                <input
                  id="confirm"
                  type="checkbox"
                  checked={confirmed}
                  onChange={(e) => setConfirmed(e.target.checked)}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label htmlFor="confirm" className="ml-2 block text-sm text-gray-900">
                  I understand the risks and want to proceed
                </label>
              </div>

              {error && (
                <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-sm text-red-700">
                  {error}
                </div>
              )}
            </>
          )}

          {executing && (
            <div className="flex flex-col items-center justify-center py-8">
              <svg className="animate-spin h-12 w-12 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <p className="mt-4 text-lg font-medium text-gray-900">Executing trades...</p>
              <p className="mt-2 text-sm text-gray-500">This may take a few moments</p>
            </div>
          )}

          {result && (
            <div className="space-y-4">
              {/* Status Banner */}
              <div className={`rounded-lg p-4 ${
                result.status === 'COMPLETED' ? 'bg-green-50 border border-green-200' :
                result.status === 'FAILED' ? 'bg-red-50 border border-red-200' :
                'bg-yellow-50 border border-yellow-200'
              }`}>
                <div className="flex items-center">
                  {result.status === 'COMPLETED' && (
                    <svg className="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  )}
                  {result.status === 'FAILED' && (
                    <svg className="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  )}
                  <div className="ml-3">
                    <h4 className={`text-sm font-medium ${
                      result.status === 'COMPLETED' ? 'text-green-800' :
                      result.status === 'FAILED' ? 'text-red-800' :
                      'text-yellow-800'
                    }`}>
                      {result.status === 'COMPLETED' && 'Execution Successful!'}
                      {result.status === 'FAILED' && 'Execution Failed'}
                      {result.status === 'CANCELLED' && 'Execution Cancelled'}
                    </h4>
                  </div>
                </div>
              </div>

              {/* Result Details */}
              {result.status === 'COMPLETED' && (() => {
                const initialAmt = result.initialAmount ?? baseAmount;
                const finalAmt = result.finalAmount ?? 0;
                const actualProfit = finalAmt - initialAmt;
                const actualProfitPercent = result.profitPercent ?? 0;
                const isProfitable = actualProfit >= 0;
                
                return (
                  <div className="bg-gray-50 rounded-lg p-4 space-y-2">
                    <div className="grid grid-cols-2 gap-4 text-sm">
                      <div>
                        <span className="text-gray-600">Initial Amount:</span>
                        <span className="ml-2 font-semibold">
                          {initialAmt.toFixed(2)} {chain.baseAsset}
                        </span>
                      </div>
                      <div>
                        <span className="text-gray-600">Final Amount:</span>
                        <span className="ml-2 font-semibold">
                          {finalAmt.toFixed(2)} {chain.baseAsset}
                        </span>
                      </div>
                      <div>
                        <span className="text-gray-600">Actual Profit:</span>
                        <span className={`ml-2 font-semibold ${isProfitable ? 'text-green-600' : 'text-red-600'}`}>
                          {isProfitable ? '+' : ''}{actualProfit.toFixed(2)} {chain.baseAsset}
                        </span>
                      </div>
                      <div>
                        <span className="text-gray-600">Actual Profit %:</span>
                        <span className={`ml-2 font-semibold ${isProfitable ? 'text-green-600' : 'text-red-600'}`}>
                          {isProfitable ? '+' : ''}{actualProfitPercent.toFixed(2)}%
                        </span>
                      </div>
                    </div>
                  </div>
                );
              })()}

              {result.errorMessage && (
                <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-sm text-red-700">
                  {result.errorMessage}
                </div>
              )}

              {/* Trade Steps */}
              {result.steps && result.steps.length > 0 && (
                <div>
                  <h4 className="text-sm font-medium text-gray-700 mb-2">Trade Steps</h4>
                  <div className="space-y-2">
                    {result.steps.map((step, i) => (
                      <div key={i} className="flex items-center justify-between bg-gray-50 rounded p-3 text-sm">
                        <span className="font-medium">{i + 1}. {step.symbol}</span>
                        <span className="text-gray-600">
                          {step.amount != null ? step.amount.toFixed(8) : 'N/A'}
                        </span>
                        <span className={`px-2 py-1 rounded text-xs font-semibold ${
                          step.status === 'FILLED' ? 'bg-green-100 text-green-800' : 'bg-gray-200 text-gray-800'
                        }`}>
                          {step.status || 'PENDING'}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
          {!result && !executing && (
            <>
              <button
                onClick={handleClose}
                className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={handleExecute}
                disabled={!confirmed}
                className={`px-4 py-2 rounded-md text-sm font-medium text-white ${
                  confirmed
                    ? 'bg-blue-600 hover:bg-blue-700'
                    : 'bg-gray-400 cursor-not-allowed'
                }`}
              >
                Execute
              </button>
            </>
          )}
          {result && (
            <button
              onClick={handleClose}
              className="px-4 py-2 bg-blue-600 text-white rounded-md text-sm font-medium hover:bg-blue-700"
            >
              Close
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

