import { StatusCard } from '../ui/status-card';
import type { SystemStatus } from '@/lib/schema';

interface StatusSummaryProps {
  data?: SystemStatus;
  isLoading?: boolean;
}

export function StatusSummary({ data, isLoading = false }: StatusSummaryProps) {
  if (isLoading || !data) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        {[...Array(4)].map((_, i) => (
          <div key={i} className="bg-white rounded-lg shadow p-4 animate-pulse">
            <div className="flex items-center justify-between mb-2">
              <div className="h-4 bg-gray-200 rounded w-1/3"></div>
              <div className="h-6 w-6 bg-gray-200 rounded-full"></div>
            </div>
            <div className="h-8 bg-gray-200 rounded w-1/2 mb-2"></div>
            <div className="h-2 bg-gray-200 rounded w-full mb-1"></div>
            <div className="h-3 bg-gray-200 rounded w-1/4"></div>
          </div>
        ))}
      </div>
    );
  }

  const { 
    totalStorage, 
    usedStorage, 
    totalClients, 
    connectedClients, 
    totalFiles, 
    newFilesToday, 
    filesChangePercentage, 
    healthStatus, 
    lastCheck, 
    uptime 
  } = data;

  // Calculate percentage
  const storagePercentage = Math.round((usedStorage / totalStorage) * 100);
  const clientsPercentage = Math.round((connectedClients / totalClients) * 100);

  // Format the lastCheck time
  const formatLastCheck = () => {
    const lastCheckDate = new Date(lastCheck);
    const now = new Date();
    const diffMinutes = Math.floor((now.getTime() - lastCheckDate.getTime()) / (1000 * 60));
    
    if (diffMinutes < 1) return 'Just now';
    if (diffMinutes === 1) return '1 min ago';
    if (diffMinutes < 60) return `${diffMinutes} mins ago`;
    
    const diffHours = Math.floor(diffMinutes / 60);
    if (diffHours === 1) return '1 hour ago';
    return `${diffHours} hours ago`;
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <StatusCard
        title="Total Storage"
        value={`${usedStorage.toFixed(1)} GB`}
        icon="storage"
        iconColor="text-primary"
        suffix={`/ ${totalStorage} GB`}
        progress={storagePercentage}
        progressLabel={`${storagePercentage}% utilized`}
      />
      
      <StatusCard
        title="Connected Clients"
        value={connectedClients}
        icon="devices"
        iconColor="text-secondary"
        suffix={`/ ${totalClients}`}
        progress={clientsPercentage}
        progressLabel={`${clientsPercentage}% connected`}
      />
      
      <StatusCard
        title="Total Files"
        value={totalFiles.toLocaleString()}
        icon="insert_drive_file"
        iconColor="text-accent"
        metricLeft={`+${newFilesToday} today`}
        metricRight={`↑ ${filesChangePercentage}%`}
      />
      
      <StatusCard
        title="System Health"
        value={healthStatus === 'healthy' ? 'Healthy' : healthStatus === 'warning' ? 'Warning' : 'Error'}
        icon="check_circle"
        iconColor="text-success"
        status={healthStatus === 'healthy' ? 'success' : healthStatus === 'warning' ? 'warning' : 'danger'}
        metricLeft={`Last check: ${formatLastCheck()}`}
        metricRight={`Uptime: ${uptime}%`}
      />
    </div>
  );
}
