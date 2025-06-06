import React from 'react';
import { cn } from '@/lib/utils';

export interface Column<T> {
  key: string;
  header: string;
  cell: (item: T) => React.ReactNode;
  className?: string;
}

export interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  totalItems: number;
  itemsPerPage: number;
}

export interface SearchProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

export interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  pagination?: PaginationProps;
  search?: SearchProps;
  title?: string;
  actions?: React.ReactNode;
  isLoading?: boolean;
  emptyState?: React.ReactNode;
}

export function DataTable<T>({
  data,
  columns,
  pagination,
  search,
  title,
  actions,
  isLoading,
  emptyState
}: DataTableProps<T>) {
  return (
    <div className="bg-white rounded-lg shadow">
      {(title || search || actions) && (
        <div className="p-4 border-b border-gray-200">
          <div className="flex justify-between items-center">
            {title && <h3 className="font-medium text-gray-700">{title}</h3>}
            <div className="flex items-center space-x-2">
              {search && (
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 flex items-center pl-2">
                    <span className="material-icons text-gray-400 text-sm">search</span>
                  </span>
                  <input
                    type="text"
                    value={search.value}
                    onChange={(e) => search.onChange(e.target.value)}
                    placeholder={search.placeholder || "Search..."}
                    className="pl-8 pr-4 py-1 text-sm rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent"
                  />
                </div>
              )}
              {actions}
            </div>
          </div>
        </div>
      )}
      
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              {columns.map((column) => (
                <th
                  key={column.key}
                  scope="col"
                  className={cn(
                    "px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider",
                    column.className
                  )}
                >
                  {column.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {isLoading ? (
              <tr>
                <td className="px-6 py-4 whitespace-nowrap text-center" colSpan={columns.length}>
                  Loading...
                </td>
              </tr>
            ) : data.length === 0 ? (
              <tr>
                <td className="px-6 py-4 whitespace-nowrap text-center" colSpan={columns.length}>
                  {emptyState || "No data available"}
                </td>
              </tr>
            ) : (
              data.map((item, index) => (
                <tr key={index}>
                  {columns.map((column) => (
                    <td key={column.key} className={cn("px-6 py-4 whitespace-nowrap", column.className)}>
                      {column.cell()}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      
      {pagination && (
        <div className="px-4 py-3 border-t border-gray-200 sm:px-6">
          <div className="flex items-center justify-between">
            <div className="text-sm text-gray-700">
              Showing <span className="font-medium">{((pagination.currentPage - 1) * pagination.itemsPerPage) + 1}</span> to{' '}
              <span className="font-medium">
                {Math.min(pagination.currentPage * pagination.itemsPerPage, pagination.totalItems)}
              </span>{' '}
              of <span className="font-medium">{pagination.totalItems}</span> items
            </div>
            <div>
              <nav className="relative z-0 inline-flex -space-x-px" aria-label="Pagination">
                <button
                  onClick={() => pagination.onPageChange(pagination.currentPage - 1)}
                  disabled={pagination.currentPage === 1}
                  className={cn(
                    "relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium",
                    pagination.currentPage === 1 
                      ? "text-gray-300 cursor-not-allowed" 
                      : "text-gray-500 hover:bg-gray-50"
                  )}
                >
                  <span className="material-icons text-sm">chevron_left</span>
                </button>
                
                {[...Array(pagination.totalPages)].map((_, i) => (
                  <button
                    key={i}
                    onClick={() => pagination.onPageChange(i + 1)}
                    className={cn(
                      "relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium",
                      pagination.currentPage === i + 1
                        ? "bg-primary text-white"
                        : "bg-white text-gray-700 hover:bg-gray-50"
                    )}
                  >
                    {i + 1}
                  </button>
                ))}
                
                <button
                  onClick={() => pagination.onPageChange(pagination.currentPage + 1)}
                  disabled={pagination.currentPage === pagination.totalPages}
                  className={cn(
                    "relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium",
                    pagination.currentPage === pagination.totalPages 
                      ? "text-gray-300 cursor-not-allowed" 
                      : "text-gray-500 hover:bg-gray-50"
                  )}
                >
                  <span className="material-icons text-sm">chevron_right</span>
                </button>
              </nav>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
