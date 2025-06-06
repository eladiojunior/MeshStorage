import { LineProps, PieProps } from 'recharts';
import { cn } from '@/lib/utils';

export interface ChartProps {
  title: string;
  children: React.ReactNode;
  className?: string;
  actions?: React.ReactNode;
}

export function Chart({ title, children, className, actions }: ChartProps) {
  return (
    <div className={cn("bg-white rounded-lg shadow p-4", className)}>
      <div className="flex justify-between items-center mb-4">
        <h3 className="font-medium text-gray-700">{title}</h3>
        {actions && <div className="flex space-x-2">{actions}</div>}
      </div>
      <div className="h-64">
        {children}
      </div>
    </div>
  );
}

export interface ChartTimeRangeButtonProps {
  label: string;
  active?: boolean;
  onClick?: () => void;
}

export function ChartTimeRangeButton({ 
  label, 
  active, 
  onClick 
}: ChartTimeRangeButtonProps) {
  return (
    <button 
      onClick={onClick}
      className={cn(
        "px-2 py-1 text-xs rounded border border-gray-300",
        active ? "bg-gray-100" : "hover:bg-gray-50"
      )}
    >
      {label}
    </button>
  );
}

export interface LineChartTooltipProps {
  active?: boolean;
  payload?: any[];
  label?: string;
  formatter?: (value: number) => string;
  labelFormatter?: (label: string) => string;
}

export function LineChartTooltip({ 
  active, 
  payload, 
  label,
  formatter = (value) => `${value} GB`,
  labelFormatter = (label) => label,
}: LineChartTooltipProps) {
  if (!active || !payload || !payload.length) return null;

  return (
    <div className="bg-white p-2 border border-gray-200 shadow-sm rounded">
      <p className="text-xs font-medium">{labelFormatter(label || '')}</p>
      {payload.map((entry, index) => (
        <p key={`item-${index}`} className="text-xs" style={{ color: entry.color }}>
          {entry.name}: {formatter(entry.value)}
        </p>
      ))}
    </div>
  );
}

export interface PieChartTooltipProps {
  active?: boolean;
  payload?: any[];
}

export function PieChartTooltip({ active, payload }: PieChartTooltipProps) {
  if (!active || !payload || !payload.length) return null;

  return (
    <div className="bg-white p-2 border border-gray-200 shadow-sm rounded">
      <p className="text-xs font-medium" style={{ color: payload[0].payload.fill }}>
        {payload[0].name}
      </p>
      <p className="text-xs">{payload[0].value}%</p>
    </div>
  );
}
