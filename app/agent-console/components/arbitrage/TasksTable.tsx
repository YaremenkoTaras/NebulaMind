'use client';

import { Task, TaskStatus } from '@/lib/types/arbitrage';

interface TasksTableProps {
  tasks: Task[];
  selectedTaskId: string | null;
  onSelectTask: (taskId: string) => void;
  onStartTask: (taskId: string) => void;
  onStopTask: (taskId: string) => void;
  onDeleteTask: (taskId: string) => void;
}

export default function TasksTable({
  tasks,
  selectedTaskId,
  onSelectTask,
  onStartTask,
  onStopTask,
  onDeleteTask,
}: TasksTableProps) {
  const getStatusBadge = (status: TaskStatus) => {
    const badges = {
      PENDING: 'bg-gray-200 text-gray-800',
      RUNNING: 'bg-green-100 text-green-800',
      STOPPED: 'bg-yellow-100 text-yellow-800',
      COMPLETED: 'bg-blue-100 text-blue-800',
      FAILED: 'bg-red-100 text-red-800',
    };
    return badges[status] || badges.PENDING;
  };

  const formatDuration = (minutes: number) => {
    if (minutes < 60) return `${minutes}m`;
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;
  };

  const formatProfit = (profit: number) => {
    if (profit > 0) return `+${profit.toFixed(2)}`;
    return profit.toFixed(2);
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleString('uk-UA', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (tasks.length === 0) {
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
            d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
          />
        </svg>
        <h3 className="mt-2 text-sm font-medium text-gray-900">No tasks yet</h3>
        <p className="mt-1 text-sm text-gray-500">
          Create your first arbitrage task to get started
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white shadow-md rounded-lg overflow-hidden">
      <div className="px-6 py-4 border-b border-gray-200">
        <h3 className="text-lg font-medium text-gray-900">Arbitrage Tasks</h3>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Asset / Budget
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Duration / Delay
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Profit/Loss
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Executions
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Created
              </th>
              <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {tasks.map((task, index) => {
              const isSelected = task.id === selectedTaskId;
              const netProfit = task.totalProfit - task.totalLoss;
              
              return (
                <tr
                  key={task.id}
                  onClick={() => onSelectTask(task.id)}
                  className={`cursor-pointer transition-colors ${
                    isSelected ? 'bg-blue-50' : index % 2 === 0 ? 'bg-white hover:bg-gray-50' : 'bg-gray-50 hover:bg-gray-100'
                  }`}
                >
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusBadge(task.status)}`}>
                      {task.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">{task.baseAsset}</div>
                    <div className="text-sm text-gray-500">
                      {task.currentBudget.toFixed(2)} / {task.budget.toFixed(2)}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{formatDuration(task.executionTimeMinutes)}</div>
                    <div className="text-sm text-gray-500">{task.delaySeconds}s delay</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className={`text-sm font-medium ${netProfit >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                      {formatProfit(netProfit)} {task.baseAsset}
                    </div>
                    <div className="text-xs text-gray-500">
                      +{task.totalProfit.toFixed(2)} / -{task.totalLoss.toFixed(2)}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {task.executionsCount}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {formatDate(task.createdAt)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
                    {task.status === 'PENDING' && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          onStartTask(task.id);
                        }}
                        className="inline-flex items-center px-2 py-1 border border-transparent text-xs font-medium rounded text-white bg-green-600 hover:bg-green-700"
                      >
                        ▶ Start
                      </button>
                    )}
                    {task.status === 'RUNNING' && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          onStopTask(task.id);
                        }}
                        className="inline-flex items-center px-2 py-1 border border-transparent text-xs font-medium rounded text-white bg-yellow-600 hover:bg-yellow-700"
                      >
                        ⏸ Stop
                      </button>
                    )}
                    {(task.status === 'STOPPED' || task.status === 'COMPLETED' || task.status === 'FAILED') && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          if (confirm('Are you sure you want to delete this task?')) {
                            onDeleteTask(task.id);
                          }
                        }}
                        className="inline-flex items-center px-2 py-1 border border-transparent text-xs font-medium rounded text-white bg-red-600 hover:bg-red-700"
                      >
                        🗑 Delete
                      </button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

