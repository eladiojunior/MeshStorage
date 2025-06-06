import { useState } from 'react';
import { DataTable, Column } from '../ui/data-table';
import { Client } from '@/lib/schema';

interface ConnectedClientsProps {
  data: Client[];
  isLoading?: boolean;
}

export function ConnectedClients({ data, isLoading = false }: ConnectedClientsProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 3;
  
  // Filter clients based on search term
  const filteredClients = data.filter(client => 
    client.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    client.ipAddress.toLowerCase().includes(searchTerm.toLowerCase())
  );
  
  // Calculate pagination
  const totalItems = filteredClients.length;
  const totalPages = Math.ceil(totalItems / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const paginatedClients = filteredClients.slice(startIndex, startIndex + itemsPerPage);
  
  const columns: Column<Client>[] = [
    {
      key: 'client',
      header: 'Client',
      cell: (client) => (
        <div className="flex items-center">
          <div className="flex-shrink-0 h-8 w-8 rounded-full bg-gray-200 flex items-center justify-center">
            <span className="material-icons text-gray-500 text-sm">máquina</span>
          </div>
          <div className="ml-4">
            <div className="text-sm font-medium text-gray-900">{client.name}</div>
            <div className="text-xs text-gray-500">{client.ipAddress}</div>
          </div>
        </div>
      )
    },
    {
      key: 'status',
      header: 'Status',
      cell: (client) => {
        let statusClass = '';
        switch (client.status) {
          case 'active':
            statusClass = 'bg-green-100 text-green-800';
            break;
          case 'warning':
            statusClass = 'bg-yellow-100 text-yellow-800';
            break;
          case 'error':
            statusClass = 'bg-red-100 text-red-800';
            break;
          default:
            statusClass = 'bg-gray-100 text-gray-800';
        }
        
        return (
          <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${statusClass}`}>
            {client.status.charAt(0).toUpperCase() + client.status.slice(1)}
          </span>
        );
      }
    },
    {
      key: 'storage',
      header: 'Storage Used',
      cell: (client) => {
        const percentage = Math.round((client.storageUsed / client.storageCapacity) * 100);
        let barColor = 'bg-primary';
        
        if (percentage >= 85) {
          barColor = 'bg-warning';
        }
        
        return (
          <div>
            <div className="text-sm text-gray-900">{client.storageUsed.toFixed(1)} GB</div>
            <div className="w-24 bg-gray-200 rounded-full h-1.5 mt-1">
              <div 
                className={`${barColor} rounded-full h-1.5`} 
                style={{ width: `${percentage}%` }}
              ></div>
            </div>
          </div>
        );
      }
    },
    {
      key: 'files',
      header: 'Files',
      cell: (client) => (
        <div className="text-sm text-gray-500">{client.fileCount.toLocaleString()}</div>
      )
    },
    {
      key: 'actions',
      header: 'Actions',
      cell: () => (
        <div className="text-right text-sm font-medium">
          <button className="text-indigo-600 hover:text-indigo-900 mr-2">View</button>
          <button className="text-gray-600 hover:text-gray-900">
            <span className="material-icons text-sm">more_vert</span>
          </button>
        </div>
      )
    }
  ];
  
  return (
    <DataTable
      data={paginatedClients}
      columns={columns}
      isLoading={isLoading}
      title="Connected Clients"
      search={{
        value: searchTerm,
        onChange: setSearchTerm,
        placeholder: "Search clients..."
      }}
      actions={
        <button className="p-1 rounded hover:bg-gray-100">
          <span className="material-icons text-gray-500">filter_list</span>
        </button>
      }
      pagination={{
        currentPage,
        totalPages,
        onPageChange: setCurrentPage,
        totalItems,
        itemsPerPage
      }}
      emptyState={
        <div className="py-8 text-center">
          <span className="material-icons text-gray-400 text-4xl mb-2">devices_off</span>
          <p className="text-gray-500">No connected clients found</p>
        </div>
      }
    />
  );
}
