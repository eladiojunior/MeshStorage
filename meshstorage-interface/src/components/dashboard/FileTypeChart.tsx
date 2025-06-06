import { Chart } from '../ui/chart';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend } from 'recharts';
import { FileTypeDistribution } from '@/lib/schema';

interface FileTypeChartProps {
  data: FileTypeDistribution[];
  isLoading?: boolean;
}

export function FileTypeChart({ data, isLoading = false }: FileTypeChartProps) {
  if (isLoading) {
    return (
      <Chart title="File Type Distribution">
        <div className="flex items-center justify-center h-full">
          <div className="animate-pulse flex flex-col items-center">
            <div className="h-48 w-48 bg-gray-200 rounded-full"></div>
            <div className="h-4 w-3/4 bg-gray-200 rounded mt-4"></div>
          </div>
        </div>
      </Chart>
    );
  }
  
  return (
    <Chart title="File Type Distribution">
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius="65%"
            outerRadius="85%"
            dataKey="percentage"
            nameKey="fileType"
            labelLine={false}
            paddingAngle={2}
          >
            {data.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={entry.color} />
            ))}
          </Pie>
          <Legend 
            layout="horizontal" 
            verticalAlign="bottom" 
            align="center"
            wrapperStyle={{ fontSize: '11px', paddingTop: '15px' }}
            iconSize={12}
            iconType="circle"
          />

        </PieChart>
      </ResponsiveContainer>
    </Chart>
  );
}
