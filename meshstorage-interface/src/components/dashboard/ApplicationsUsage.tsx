import { Application } from '@/lib/schema';

interface ApplicationsUsageProps {
  data: Application[];
  isLoading?: boolean;
}

export function ApplicationsUsage({ data, isLoading = false }: ApplicationsUsageProps) {
  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow">
        <div className="p-4 border-b border-gray-200">
          <div className="flex justify-between items-center">
            <h3 className="font-medium text-gray-700">Aplicações</h3>
            <div className="flex items-center space-x-2">
              <button className="p-1 rounded hover:bg-gray-100">
                <span className="material-icons text-gray-500">refresh</span>
              </button>
              <button className="p-1 rounded hover:bg-gray-100">
                <span className="material-icons text-gray-500">more_vert</span>
              </button>
            </div>
          </div>
        </div>
        
        <div className="overflow-hidden">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="px-4 py-3 hover:bg-gray-50 animate-pulse">
              <div className="flex justify-between items-center mb-2">
                <div className="flex items-center">
                  <div className="w-8 h-8 bg-gray-200 rounded-lg mr-3"></div>
                  <div>
                    <div className="h-4 bg-gray-200 rounded w-24 mb-1"></div>
                    <div className="h-3 bg-gray-200 rounded w-32"></div>
                  </div>
                </div>
                <div className="h-4 bg-gray-200 rounded w-16"></div>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-1.5 mb-1"></div>
              <div className="flex justify-between">
                <div className="h-3 bg-gray-200 rounded w-20"></div>
                <div className="h-3 bg-gray-200 rounded w-8"></div>
              </div>
            </div>
          ))}
        </div>
        
        <div className="p-4 border-t border-gray-200">
          <button className="text-sm text-primary font-medium hover:text-indigo-700">
            Todas as Aplicações
          </button>
        </div>
      </div>
    );
  }
  
  return (
    <div className="bg-white rounded-lg shadow">
      <div className="p-4 border-b border-gray-200">
        <div className="flex justify-between items-center">
          <h3 className="font-medium text-gray-700">Aplicações</h3>
          <div className="flex items-center space-x-2">
            <button className="p-1 rounded hover:bg-gray-100">
              <span className="material-icons text-gray-500">refresh</span>
            </button>
            <button className="p-1 rounded hover:bg-gray-100">
              <span className="material-icons text-gray-500">more_vert</span>
            </button>
          </div>
        </div>
      </div>
      
      <div className="overflow-hidden">
        {data.map((app) => (
          <div key={app.id} className="px-4 py-3 hover:bg-gray-50">
            <div className="flex justify-between items-center mb-1">
              <div className="flex items-center">
                <div className={`w-8 h-8 ${app.iconColor} rounded-lg flex items-center justify-center mr-3`}>
                  <span className="material-icons">{app.icon}</span>
                </div>
                <div>
                  <h4 className="text-sm font-medium text-gray-900">{app.name}</h4>
                  <p className="text-xs text-gray-500">{app.description}</p>
                </div>
              </div>
              <p className="text-sm font-medium text-gray-900">{app.storageUsed.toFixed(1)} GB</p>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-1.5">
              <div 
                className={app.iconColor.replace('bg-', 'bg-').replace('text-', '')} 
                style={{ width: `${app.percentage}%` }}
              ></div>
            </div>
            <div className="flex justify-between mt-1">
              <p className="text-xs text-gray-500">{app.fileCount.toLocaleString()} files</p>
              <p className="text-xs text-gray-500">{app.percentage}%</p>
            </div>
          </div>
        ))}
      </div>
      
      <div className="p-4 border-t border-gray-200">
        <button className="text-sm text-primary font-medium hover:text-indigo-700">
          Todas as Aplicações
        </button>
      </div>
    </div>
  );
}
