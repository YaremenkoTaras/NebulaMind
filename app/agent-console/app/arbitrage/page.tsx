'use client';

import { useState, useEffect } from 'react';
import TaskCreateForm from '@/components/arbitrage/ArbitrageScanForm';
import TasksTable from '@/components/arbitrage/TasksTable';
import TaskStatistics from '@/components/arbitrage/TaskStatistics';
import { arbitrageApi } from '@/lib/api/arbitrage';
import type { Task, TaskStatistics as TaskStats } from '@/lib/types/arbitrage';

export default function ArbitragePage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [selectedTaskId, setSelectedTaskId] = useState<string | null>(null);
  const [statistics, setStatistics] = useState<TaskStats | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  // Load tasks on mount and set up polling
  useEffect(() => {
    loadTasks();
    const interval = setInterval(loadTasks, 3000); // Refresh every 3 seconds
    return () => clearInterval(interval);
  }, []);

  // Load statistics when task is selected
  useEffect(() => {
    if (selectedTaskId) {
      loadTaskStatistics(selectedTaskId);
    } else {
      setStatistics(null);
    }
  }, [selectedTaskId]);

  const loadTasks = async () => {
    try {
      const data = await arbitrageApi.getTasks();
      setTasks(data);
      setLoading(false);
    } catch (error) {
      console.error('Failed to load tasks:', error);
      setLoading(false);
    }
  };

  const loadTaskStatistics = async (taskId: string) => {
    try {
      const data = await arbitrageApi.getTaskStatistics(taskId);
      setStatistics(data);
    } catch (error) {
      console.error('Failed to load task statistics:', error);
    }
  };

  const handleTaskCreated = () => {
    setError(null);
    loadTasks();
  };

  const handleError = (err: Error) => {
    setError(err.message);
  };

  const handleStartTask = async (taskId: string) => {
    try {
      await arbitrageApi.startTask(taskId);
      loadTasks();
    } catch (error) {
      setError((error as Error).message);
    }
  };

  const handleStopTask = async (taskId: string) => {
    try {
      await arbitrageApi.stopTask(taskId);
      loadTasks();
    } catch (error) {
      setError((error as Error).message);
    }
  };

  const handleDeleteTask = async (taskId: string) => {
    try {
      await arbitrageApi.deleteTask(taskId);
      if (selectedTaskId === taskId) {
        setSelectedTaskId(null);
      }
      loadTasks();
    } catch (error) {
      setError((error as Error).message);
    }
  };

  const handleSelectTask = (taskId: string) => {
    setSelectedTaskId(taskId);
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Triangular Arbitrage - Automated Tasks</h1>
          <p className="mt-2 text-sm text-gray-600">
            Create automated arbitrage tasks that continuously scan and execute profitable opportunities
          </p>
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
              <button
                onClick={() => setError(null)}
                className="ml-auto flex-shrink-0 text-red-400 hover:text-red-500"
              >
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
            </div>
          </div>
        )}

        {/* Task Creation Form */}
        <div className="mb-6">
          <TaskCreateForm
            onTaskCreated={handleTaskCreated}
            onError={handleError}
          />
        </div>

        {/* Tasks Table */}
        {loading ? (
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
              <p className="mt-4 text-lg font-medium text-gray-900">Loading tasks...</p>
            </div>
          </div>
        ) : (
          <div className="mb-6">
            <TasksTable
              tasks={tasks}
              selectedTaskId={selectedTaskId}
              onSelectTask={handleSelectTask}
              onStartTask={handleStartTask}
              onStopTask={handleStopTask}
              onDeleteTask={handleDeleteTask}
            />
          </div>
        )}

        {/* Task Statistics */}
        <div className="mb-6">
          <TaskStatistics statistics={statistics} />
        </div>
      </div>
    </div>
  );
}

