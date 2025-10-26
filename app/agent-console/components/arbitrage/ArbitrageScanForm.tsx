'use client';

import { useState, useEffect } from 'react';
import { arbitrageApi } from '@/lib/api/arbitrage';
import type { TaskCreateRequest } from '@/lib/types/arbitrage';

interface TaskCreateFormProps {
  onTaskCreated: () => void;
  onError: (error: Error) => void;
}

export default function TaskCreateForm({
  onTaskCreated,
  onError,
}: TaskCreateFormProps) {
  const [assets, setAssets] = useState<string[]>([]);
  const [loadingAssets, setLoadingAssets] = useState(true);
  const [creating, setCreating] = useState(false);

  const [formData, setFormData] = useState<TaskCreateRequest>({
    baseAsset: 'USDT',
    budget: 100,
    executionTimeMinutes: 5,
    delaySeconds: 10,
    minProfitPercent: 0.5, // Lowered from 1.0% to allow more chains to execute
    maxAssets: 20,
    chainLength: 3,
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
    
    if (creating) return;

    setCreating(true);

    try {
      const task = await arbitrageApi.createTask(formData);
      // Automatically start the task
      await arbitrageApi.startTask(task.id);
      onTaskCreated();
    } catch (error) {
      onError(error as Error);
    } finally {
      setCreating(false);
    }
  };

  const handleChange = (field: keyof TaskCreateRequest, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white shadow-md rounded-lg p-6 space-y-6">
      <h2 className="text-2xl font-bold text-gray-900">Create Arbitrage Task</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
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
          <p className="mt-1 text-sm text-gray-500">Currency to use</p>
        </div>

        {/* Budget */}
        <div>
          <label htmlFor="budget" className="block text-sm font-medium text-gray-700 mb-2">
            Budget *
          </label>
          <input
            id="budget"
            type="number"
            min="1"
            step="0.01"
            value={formData.budget}
            onChange={(e) => handleChange('budget', parseFloat(e.target.value))}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
          <p className="mt-1 text-sm text-gray-500">Available funds</p>
        </div>

        {/* Min Profit % */}
        <div>
          <label htmlFor="minProfitPercent" className="block text-sm font-medium text-gray-700 mb-2">
            Min Profit % *
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
          <p className="mt-1 text-sm text-gray-500">Minimum profit threshold</p>
        </div>

        {/* Execution Time */}
        <div>
          <label htmlFor="executionTimeMinutes" className="block text-sm font-medium text-gray-700 mb-2">
            Execution Time (minutes) *
          </label>
          <input
            id="executionTimeMinutes"
            type="number"
            min="1"
            max="1440"
            value={formData.executionTimeMinutes}
            onChange={(e) => handleChange('executionTimeMinutes', parseInt(e.target.value))}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
          <p className="mt-1 text-sm text-gray-500">How long to run</p>
        </div>

        {/* Delay */}
        <div>
          <label htmlFor="delaySeconds" className="block text-sm font-medium text-gray-700 mb-2">
            Delay (seconds) *
          </label>
          <input
            id="delaySeconds"
            type="number"
            min="1"
            max="300"
            value={formData.delaySeconds}
            onChange={(e) => handleChange('delaySeconds', parseInt(e.target.value))}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
          <p className="mt-1 text-sm text-gray-500">Delay between scans</p>
        </div>

        {/* Max Assets */}
        <div>
          <label htmlFor="maxAssets" className="block text-sm font-medium text-gray-700 mb-2">
            Max Assets *
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
          <p className="mt-1 text-sm text-gray-500">Assets to analyze</p>
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
          <p className="mt-1 text-sm text-gray-500">Trades per chain</p>
        </div>
      </div>

      {/* Submit Button */}
      <div className="flex justify-end">
        <button
          type="submit"
          disabled={creating || loadingAssets}
          className={`px-6 py-3 rounded-md font-semibold text-white transition-colors ${
            creating || loadingAssets
              ? 'bg-gray-400 cursor-not-allowed'
              : 'bg-green-600 hover:bg-green-700 active:bg-green-800'
          }`}
        >
          {creating ? (
            <span className="flex items-center">
              <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Creating Task...
            </span>
          ) : (
            '▶ Create and Start Task'
          )}
        </button>
      </div>
    </form>
  );
}

