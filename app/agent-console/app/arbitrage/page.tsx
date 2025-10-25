'use client';

import { useState } from 'react';
import ArbitrageScanForm from '@/components/arbitrage/ArbitrageScanForm';
import ArbitrageResultsTable from '@/components/arbitrage/ArbitrageResultsTable';
import ExecuteChainDialog from '@/components/arbitrage/ExecuteChainDialog';
import type { ArbitrageScanResponse, ArbitrageChain } from '@/lib/types/arbitrage';

export default function ArbitragePage() {
  const [scanning, setScanning] = useState(false);
  const [results, setResults] = useState<ArbitrageScanResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [selectedChain, setSelectedChain] = useState<ArbitrageChain | null>(null);

  const handleScanStart = () => {
    setScanning(true);
    setError(null);
    setResults(null);
  };

  const handleScanComplete = (data: ArbitrageScanResponse) => {
    setResults(data);
    setScanning(false);
  };

  const handleScanError = (err: Error) => {
    setError(err.message);
    setScanning(false);
  };

  const handleExecute = (chain: ArbitrageChain) => {
    setSelectedChain(chain);
  };

  const handleCloseDialog = () => {
    setSelectedChain(null);
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Triangular Arbitrage</h1>
          <p className="mt-2 text-sm text-gray-600">
            Find and execute profitable arbitrage opportunities through multi-currency chains
          </p>
        </div>

        {/* Scan Form */}
        <div className="mb-6">
          <ArbitrageScanForm
            onScanStart={handleScanStart}
            onScanComplete={handleScanComplete}
            onScanError={handleScanError}
          />
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg
                  className="h-5 w-5 text-red-400"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                    clipRule="evenodd"
                  />
                </svg>
              </div>
              <div className="ml-3">
                <h3 className="text-sm font-medium text-red-800">Error</h3>
                <div className="mt-2 text-sm text-red-700">{error}</div>
              </div>
            </div>
          </div>
        )}

        {/* Loading State */}
        {scanning && (
          <div className="mb-6 bg-white shadow-md rounded-lg p-12">
            <div className="flex flex-col items-center justify-center">
              <svg
                className="animate-spin h-12 w-12 text-blue-600"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                ></circle>
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                ></path>
              </svg>
              <p className="mt-4 text-lg font-medium text-gray-900">
                Scanning for arbitrage opportunities...
              </p>
              <p className="mt-2 text-sm text-gray-500">
                Analyzing market rates and finding profitable chains
              </p>
            </div>
          </div>
        )}

        {/* Results Table */}
        {results && !scanning && (
          <div className="mb-6">
            <ArbitrageResultsTable
              chains={results.chains}
              summary={results.summary}
              onExecute={handleExecute}
            />
          </div>
        )}

        {/* Execute Dialog */}
        {selectedChain && (
          <ExecuteChainDialog chain={selectedChain} onClose={handleCloseDialog} />
        )}
      </div>
    </div>
  );
}

