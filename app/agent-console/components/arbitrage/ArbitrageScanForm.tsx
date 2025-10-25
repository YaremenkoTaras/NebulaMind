'use client';

import { useState, useEffect } from 'react';
import { arbitrageApi } from '@/lib/api/arbitrage';
import type { ArbitrageScanRequest } from '@/lib/types/arbitrage';

interface ArbitrageScanFormProps {
  onScanStart: () => void;
  onScanComplete: (data: any) => void;
  onScanError: (error: Error) => void;
}

export default function ArbitrageScanForm({
  onScanStart,
  onScanComplete,
  onScanError,
}: ArbitrageScanFormProps) {
  const [assets, setAssets] = useState<string[]>([]);
  const [loadingAssets, setLoadingAssets] = useState(true);
  const [scanning, setScanning] = useState(false);

  const [formData, setFormData] = useState<ArbitrageScanRequest>({
    baseAsset: 'USDT',
    maxAssets: 20,
    chainLength: 3,
    minProfitPercent: 0.5,
    reasoning: '',
  });

  useEffect(() => {
    loadAssets();
  }, []);

  const loadAssets = async () => {
    try {
      const data = await arbitrageApi.getAssets();
      setAssets(data);
      setLoadingAssets(false);
    } catch (error) {
      console.error('Failed to load assets:', error);
      setLoadingAssets(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (scanning) return;

    setScanning(true);
    onScanStart();

    try {
      const result = await arbitrageApi.scan(formData);
      onScanComplete(result);
    } catch (error) {
      onScanError(error as Error);
    } finally {
      setScanning(false);
    }
  };

  const handleChange = (field: keyof ArbitrageScanRequest, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white shadow-md rounded-lg p-6 space-y-6">
      <h2 className="text-2xl font-bold text-gray-900">Triangular Arbitrage Scanner</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Base Asset */}
        <div>
          <label htmlFor="baseAsset" className="block text-sm font-medium text-gray-700 mb-2">
            Base Asset *
          </label>
          {loadingAssets ? (
            <div className="animate-pulse bg-gray-200 h-10 rounded"></div>
          ) : assets.length > 0 ? (
            <select
              id="baseAsset"
              value={formData.baseAsset}
              onChange={(e) => handleChange('baseAsset', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            >
              {assets.map((asset) => (
                <option key={asset} value={asset}>
                  {asset}
                </option>
              ))}
            </select>
          ) : (
            <input
              id="baseAsset"
              type="text"
              value={formData.baseAsset}
              onChange={(e) => handleChange('baseAsset', e.target.value.toUpperCase())}
              placeholder="USDT"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          )}
          <p className="mt-1 text-sm text-gray-500">
            Currency you already have in your account (e.g., USDT, BTC, ETH)
          </p>
        </div>

        {/* Max Assets */}
        <div>
          <label htmlFor="maxAssets" className="block text-sm font-medium text-gray-700 mb-2">
            Max Assets to Analyze *
          </label>
          <input
            id="maxAssets"
            type="number"
            min="3"
            max="50"
            value={formData.maxAssets}
            onChange={(e) => handleChange('maxAssets', parseInt(e.target.value))}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
          <p className="mt-1 text-sm text-gray-500">
            Number of currencies to analyze (min: 3)
          </p>
        </div>

        {/* Chain Length */}
        <div>
          <label htmlFor="chainLength" className="block text-sm font-medium text-gray-700 mb-2">
            Chain Length *
          </label>
          <input
            id="chainLength"
            type="number"
            min="3"
            max="5"
            value={formData.chainLength}
            onChange={(e) => handleChange('chainLength', parseInt(e.target.value))}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
          <p className="mt-1 text-sm text-gray-500">
            Number of trades in the chain (min: 3)
          </p>
        </div>

        {/* Min Profit % */}
        <div>
          <label htmlFor="minProfitPercent" className="block text-sm font-medium text-gray-700 mb-2">
            Min Profit %
          </label>
          <input
            id="minProfitPercent"
            type="number"
            min="0"
            max="100"
            step="0.1"
            value={formData.minProfitPercent}
            onChange={(e) => handleChange('minProfitPercent', parseFloat(e.target.value))}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
          <p className="mt-1 text-sm text-gray-500">
            Minimum profit percentage to display
          </p>
        </div>
      </div>

      {/* Reasoning (optional) */}
      <div>
        <label htmlFor="reasoning" className="block text-sm font-medium text-gray-700 mb-2">
          Reasoning (Optional)
        </label>
        <textarea
          id="reasoning"
          value={formData.reasoning}
          onChange={(e) => handleChange('reasoning', e.target.value)}
          rows={3}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="Why are you scanning now? (for LLM context and audit logs)"
        />
      </div>

      {/* Submit Button */}
      <div className="flex justify-end">
        <button
          type="submit"
          disabled={scanning || loadingAssets}
          className={`px-6 py-3 rounded-md font-semibold text-white transition-colors ${
            scanning || loadingAssets
              ? 'bg-gray-400 cursor-not-allowed'
              : 'bg-blue-600 hover:bg-blue-700 active:bg-blue-800'
          }`}
        >
          {scanning ? (
            <span className="flex items-center">
              <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Scanning...
            </span>
          ) : (
            '🔍 Scan for Opportunities'
          )}
        </button>
      </div>
    </form>
  );
}

