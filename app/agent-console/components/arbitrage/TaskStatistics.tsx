'use client';

import { TaskStatistics as TaskStats, ArbitrageExecution } from '@/lib/types/arbitrage';

interface TaskStatisticsProps {
  statistics: TaskStats | null;
}

export default function TaskStatistics({ statistics }: TaskStatisticsProps) {
  if (!statistics) {
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
            d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
          />
        </svg>
        <h3 className="mt-2 text-sm font-medium text-gray-900">No task selected</h3>
        <p className="mt-1 text-sm text-gray-500">
          Select a task from the table above to view its statistics
        </p>
      </div>
    );
  }

  const { task, profitableExecutions, lossExecutions, totalProfit, totalLoss, netProfit } = statistics;

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleString('uk-UA', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  };

  const ExecutionTable = ({ executions, title, type }: { executions: ArbitrageExecution[], title: string, type: 'profit' | 'loss' }) => {
    if (executions.length === 0) {
      return (
        <div className="bg-white rounded-lg border border-gray-200 p-4 text-center">
          <p className="text-sm text-gray-500">No {type === 'profit' ? 'profitable' : 'loss'} executions yet</p>
        </div>
      );
    }

    return (
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="px-4 py-3 bg-gray-50 border-b border-gray-200">
          <h4 className="text-sm font-medium text-gray-900">{title}</h4>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
            <tr>
              <th scope="col" className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Time
              </th>
              <th scope="col" className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Chain
              </th>
              <th scope="col" className="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Invested
              </th>
              <th scope="col" className="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Expected
              </th>
              <th scope="col" className="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actual
              </th>
              <th scope="col" className="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                %
              </th>
            </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {executions.map((exec, index) => {
                const expectedProfit = (exec.initialAmount * exec.expectedProfitPercent) / 100;
                const isProfitable = exec.profitAmount >= 0;
                
                return (
                  <tr key={exec.id} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                    <td className="px-4 py-2 whitespace-nowrap text-xs text-gray-500">
                      {formatDate(exec.timestamp)}
                    </td>
                    <td className="px-4 py-2 text-xs text-gray-900">
                      <div className="truncate max-w-xs" title={exec.chain.steps.join(' → ')}>
                        {exec.chain.steps.slice(0, 2).join(' → ')}...
                      </div>
                    </td>
                    <td className="px-4 py-2 whitespace-nowrap text-right text-xs text-gray-900">
                      {exec.initialAmount.toFixed(2)}
                    </td>
                    <td className="px-4 py-2 whitespace-nowrap text-right text-xs text-blue-600">
                      +{expectedProfit.toFixed(2)}
                    </td>
                    <td className={`px-4 py-2 whitespace-nowrap text-right text-xs font-medium ${
                      isProfitable ? 'text-green-600' : 'text-red-600'
                    }`}>
                      {isProfitable ? '+' : ''}{exec.profitAmount.toFixed(2)}
                    </td>
                    <td className={`px-4 py-2 whitespace-nowrap text-right text-xs font-medium ${
                      isProfitable ? 'text-green-600' : 'text-red-600'
                    }`}>
                      {isProfitable ? '+' : ''}{exec.profitPercent.toFixed(2)}%
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header with Overall Statistics */}
      <div className="bg-gradient-to-r from-blue-500 to-blue-600 rounded-lg shadow-md p-6 text-white">
        <h3 className="text-2xl font-bold mb-4">Task Statistics</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-white bg-opacity-20 rounded-lg p-4">
            <div className="text-sm opacity-90">Available Budget</div>
            <div className="text-2xl font-bold">
              {task.currentBudget.toFixed(2)} {task.baseAsset}
            </div>
            <div className="text-xs opacity-75 mt-1">
              of {task.budget.toFixed(2)} {task.baseAsset}
            </div>
          </div>
          <div className="bg-white bg-opacity-20 rounded-lg p-4">
            <div className="text-sm opacity-90">Total Profit</div>
            <div className="text-2xl font-bold text-green-300">
              +{totalProfit.toFixed(2)} {task.baseAsset}
            </div>
            <div className="text-xs opacity-75 mt-1">
              {profitableExecutions.length} profitable trades
            </div>
          </div>
          <div className="bg-white bg-opacity-20 rounded-lg p-4">
            <div className="text-sm opacity-90">Total Loss</div>
            <div className="text-2xl font-bold text-red-300">
              -{totalLoss.toFixed(2)} {task.baseAsset}
            </div>
            <div className="text-xs opacity-75 mt-1">
              {lossExecutions.length} loss trades
            </div>
          </div>
        </div>
        <div className="mt-4 pt-4 border-t border-white border-opacity-30">
          <div className="flex justify-between items-center">
            <span className="text-lg">Net Profit/Loss:</span>
            <span className={`text-3xl font-bold ${netProfit >= 0 ? 'text-green-300' : 'text-red-300'}`}>
              {netProfit >= 0 ? '+' : ''}{netProfit.toFixed(2)} {task.baseAsset}
            </span>
          </div>
        </div>
      </div>

      {/* Execution Tables */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div>
          <ExecutionTable
            executions={profitableExecutions}
            title={`Profitable Executions (${profitableExecutions.length})`}
            type="profit"
          />
        </div>
        <div>
          <ExecutionTable
            executions={lossExecutions}
            title={`Loss Executions (${lossExecutions.length})`}
            type="loss"
          />
        </div>
      </div>

      {/* Notes Section */}
      <div className="bg-white rounded-lg border border-gray-200 p-4">
        <h4 className="text-sm font-medium text-gray-900 mb-2">Notes</h4>
        <p className="text-sm text-gray-500 italic">This section is reserved for future use</p>
      </div>
    </div>
  );
}

