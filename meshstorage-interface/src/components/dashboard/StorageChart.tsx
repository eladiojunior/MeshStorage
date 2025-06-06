import { useState, useCallback } from 'react';
import { Chart, ChartTimeRangeButton } from '../ui/chart';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, ResponsiveContainer } from 'recharts';
import { StorageHistory } from '@/lib/schema';

interface StorageChartProps {
  data: StorageHistory[];
  isLoading?: boolean;
}

type TimeRange = 'daily' | 'weekly' | 'monthly';

export function StorageChart({ data, isLoading = false }: StorageChartProps) {
  const [timeRange, setTimeRange] = useState<TimeRange>('weekly');
  
  const formatDate = useCallback((timestamp: string | Date) => {
    const date = timestamp instanceof Date ? timestamp : new Date(timestamp);
    
    if (timeRange === 'daily') {
      return date.toLocaleDateString(undefined, { weekday: 'short', day: 'numeric' });
    } else if (timeRange === 'weekly') {
      return `Week ${Math.ceil((date.getDate()) / 7)}`;
    } else {
      return date.toLocaleDateString(undefined, { month: 'short' });
    }
  }, [timeRange]);
  
  if (isLoading) {
    return (
      <Chart 
        title="Storage Utilization (Last 30 Days)" 
        className="lg:col-span-2"
        actions={
          <>
            <ChartTimeRangeButton label="Daily" active={timeRange === 'daily'} />
            <ChartTimeRangeButton label="Weekly" active={timeRange === 'weekly'} />
            <ChartTimeRangeButton label="Monthly" active={timeRange === 'monthly'} />
          </>
        }
      >
        <div className="flex items-center justify-center h-full">
          <div className="animate-pulse flex flex-col items-center">
            <div className="h-48 w-full bg-gray-200 rounded"></div>
            <div className="h-4 w-3/4 bg-gray-200 rounded mt-4"></div>
          </div>
        </div>
      </Chart>
    );
  }
  
  const formattedData = data.map(item => ({
    name: formatDate(item.timestamp),
    storage: item.usedStorage,
    timestamp: item.timestamp,
  }));
  
  return (
    <Chart 
      title="Storage Utilization (Last 30 Days)" 
      className="lg:col-span-2"
      actions={
        <>
          <ChartTimeRangeButton 
            label="Daily" 
            active={timeRange === 'daily'} 
            onClick={() => setTimeRange('daily')}
          />
          <ChartTimeRangeButton 
            label="Weekly" 
            active={timeRange === 'weekly'} 
            onClick={() => setTimeRange('weekly')}
          />
          <ChartTimeRangeButton 
            label="Monthly" 
            active={timeRange === 'monthly'} 
            onClick={() => setTimeRange('monthly')}
          />
        </>
      }
    >
      <ResponsiveContainer width="100%" height="100%">
        <LineChart
          data={formattedData}
          margin={{
            top: 5,
            right: 10,
            left: 10,
            bottom: 5,
          }}
        >
          <CartesianGrid strokeDasharray="3 3" vertical={false} />
          <XAxis 
            dataKey="name" 
            axisLine={false}
            tickLine={false}
          />
          <YAxis 
            axisLine={false}
            tickLine={false}
            tickFormatter={(value) => `${value}`}
            domain={['auto', 'auto']}
          />

          <Line
            type="monotone"
            dataKey="storage"
            name="Used Storage"
            stroke="rgba(59, 130, 246, 1)"
            strokeWidth={2}
            dot={false}
            activeDot={{ r: 6 }}
            fill="rgba(59, 130, 246, 0.1)"
          />
        </LineChart>
      </ResponsiveContainer>
    </Chart>
  );
}
