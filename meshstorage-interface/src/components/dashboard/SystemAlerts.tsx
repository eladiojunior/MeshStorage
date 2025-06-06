import { SystemAlert } from '@/lib/schema';

interface SystemAlertsProps {
  data: SystemAlert[];
  isLoading?: boolean;
  onMarkAsRead?: (id: number) => void;
}

export function SystemAlerts({ data, isLoading = false, onMarkAsRead }: SystemAlertsProps) {
  // Format the timestamp
  const formatTimestamp = (timestamp: string) => {
    const alertDate = new Date(timestamp);
    const now = new Date();
    const diffMinutes = Math.floor((now.getTime() - alertDate.getTime()) / (1000 * 60));
    
    if (diffMinutes < 1) return 'Just now';
    if (diffMinutes === 1) return '1 min ago';
    if (diffMinutes < 60) return `${diffMinutes} mins ago`;
    
    const diffHours = Math.floor(diffMinutes / 60);
    if (diffHours === 1) return '1 hour ago';
    if (diffHours < 24) return `${diffHours} hours ago`;
    
    const diffDays = Math.floor(diffHours / 24);
    if (diffDays === 1) return '1 day ago';
    return `${diffDays} days ago`;
  };
  
  // Get icon and color based on alert type
  const getAlertIcon = (type: string) => {
    switch (type) {
      case 'warning':
        return { icon: 'warning', color: 'text-warning' };
      case 'error':
        return { icon: 'error', color: 'text-danger' };
      case 'success':
        return { icon: 'check_circle', color: 'text-success' };
      default:
        return { icon: 'info', color: 'text-primary' };
    }
  };
  
  const handleMarkAsRead = (id: number) => {
    if (onMarkAsRead) {
      onMarkAsRead(id);
    }
  };
  
  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow mb-6">
        <div className="p-4 border-b border-gray-200">
          <div className="flex justify-between items-center">
            <h3 className="font-medium text-gray-700">Recent System Alerts</h3>
            <div className="flex items-center space-x-2">
              <button className="text-xs px-2 py-1 bg-gray-100 rounded-lg text-gray-600 hover:bg-gray-200">
                Mark All as Read
              </button>
              <button className="p-1 rounded hover:bg-gray-100">
                <span className="material-icons text-gray-500">more_vert</span>
              </button>
            </div>
          </div>
        </div>
        
        <div className="divide-y divide-gray-200">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="p-4 hover:bg-gray-50 animate-pulse">
              <div className="flex items-start">
                <div className="flex-shrink-0 mt-0.5">
                  <div className="h-5 w-5 bg-gray-200 rounded-full"></div>
                </div>
                <div className="ml-3 flex-1">
                  <div className="flex justify-between items-center mb-1">
                    <div className="h-4 bg-gray-200 rounded w-1/3"></div>
                    <div className="h-3 bg-gray-200 rounded w-16"></div>
                  </div>
                  <div className="h-3 bg-gray-200 rounded w-3/4 mb-2"></div>
                  <div className="flex">
                    <div className="h-3 bg-gray-200 rounded w-16 mr-3"></div>
                    <div className="h-3 bg-gray-200 rounded w-16"></div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
        
        <div className="p-4 border-t border-gray-200">
          <button className="text-sm text-primary font-medium hover:text-indigo-700">
            View All Alerts
          </button>
        </div>
      </div>
    );
  }
  
  return (
    <div className="bg-white rounded-lg shadow mb-6">
      <div className="p-4 border-b border-gray-200">
        <div className="flex justify-between items-center">
          <h3 className="font-medium text-gray-700">Recent System Alerts</h3>
          <div className="flex items-center space-x-2">
            <button 
              className="text-xs px-2 py-1 bg-gray-100 rounded-lg text-gray-600 hover:bg-gray-200"
              onClick={() => data.forEach(alert => !alert.read && onMarkAsRead?.(alert.id))}
            >
              Mark All as Read
            </button>
            <button className="p-1 rounded hover:bg-gray-100">
              <span className="material-icons text-gray-500">more_vert</span>
            </button>
          </div>
        </div>
      </div>
      
      <div className="divide-y divide-gray-200">
        {data.length === 0 ? (
          <div className="p-8 text-center">
            <span className="material-icons text-gray-400 text-4xl mb-2">notifications_none</span>
            <p className="text-gray-500">No alerts at this time</p>
          </div>
        ) : (
          data.map((alert) => {
            const { icon, color } = getAlertIcon(alert.type);
            return (
              <div key={alert.id} className="p-4 hover:bg-gray-50">
                <div className="flex items-start">
                  <div className="flex-shrink-0 mt-0.5">
                    <span className={`material-icons ${color}`}>{icon}</span>
                  </div>
                  <div className="ml-3 flex-1">
                    <div className="flex justify-between items-center">
                      <p className="text-sm font-medium text-gray-900">{alert.title}</p>
                      <p className="text-xs text-gray-500">{formatTimestamp(alert.timestamp.toISOString())}</p>
                    </div>
                    <p className="text-sm text-gray-500 mt-1">{alert.message}</p>
                    <div className="mt-2">
                      <button className="text-xs text-primary font-medium mr-3 hover:text-indigo-700">
                        {alert.type === 'warning' ? 'Investigate' : 
                         alert.type === 'error' ? 'Troubleshoot' : 
                         'View Details'}
                      </button>
                      <button 
                        className="text-xs text-gray-500 font-medium hover:text-gray-700"
                        onClick={() => handleMarkAsRead(alert.id)}
                      >
                        {alert.read ? 'Dismissed' : 'Dismiss'}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
      
      <div className="p-4 border-t border-gray-200">
        <button className="text-sm text-primary font-medium hover:text-indigo-700">
          View All Alerts
        </button>
      </div>
    </div>
  );
}
