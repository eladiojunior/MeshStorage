type InfoCardProps = {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  status?: 'success' | 'warning' | 'error';
  subtitle?: string;
};

export default function InfoCard({ title, value, icon, status = 'success', subtitle }: InfoCardProps) {
  const statusColors = {
    success: 'border-green-200 bg-green-50',
    warning: 'border-yellow-200 bg-yellow-50',
    error: 'border-red-200 bg-red-50'
  };
  const iconColors = {
    success: 'text-green-600',
    warning: 'text-yellow-600',
    error: 'text-red-600'
  };
  return (
      <div className={`rounded-lg border p-6 ${statusColors[status]}`}>
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-gray-600">{title}</p>
            <p className="text-3xl font-bold text-gray-900">{value}</p>
            {subtitle && (
                <p className="text-sm text-gray-500 mt-1">{subtitle}</p>
            )}
          </div>
          <div className={`p-3 rounded-full bg-white ${iconColors[status]}`}>
            {icon}
          </div>
        </div>
      </div>
  );
}
