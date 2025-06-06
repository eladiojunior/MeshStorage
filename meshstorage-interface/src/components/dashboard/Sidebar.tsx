import { useState } from 'react';
import { Link, useLocation } from 'wouter';
import { cn } from '@/lib/utils';

type SidebarItem = {
  label: string;
  icon: string;
  href: string;
  isActive: boolean;
};

interface SidebarProps {
  className?: string;
}

export function Sidebar({ className }: SidebarProps) {
  const [location] = useLocation();
  const [isSidebarVisible, setIsSidebarVisible] = useState(false);
  
  const sidebarItems: SidebarItem[] = [
    {
      label: 'Page',
      icon: 'dashboard',
      href: '/',
      isActive: location === '/',
    },
    {
      label: 'Clients',
      icon: 'devices',
      href: '/clients',
      isActive: location === '/clients',
    },
    {
      label: 'Applications',
      icon: 'apps',
      href: '/applications',
      isActive: location === '/applications',
    },
    {
      label: 'Analytics',
      icon: 'analytics',
      href: '/analytics',
      isActive: location === '/analytics',
    },
    {
      label: 'Settings',
      icon: 'settings',
      href: '/settings',
      isActive: location === '/settings',
    },
  ];
  
  return (
    <>
      {/* Desktop sidebar */}
      <div 
        id="sidebar" 
        className={cn(
          "bg-gray-800 text-white w-64 flex-shrink-0 hidden md:block transition-all duration-300 h-screen fixed z-40",
          isSidebarVisible ? "left-0" : "-left-64",
          className
        )}
      >
        <div className="p-4 flex items-center justify-between border-b border-gray-700">
          <div className="flex items-center space-x-2">
            <span className="material-icons text-primary">storage</span>
            <h1 className="font-bold text-xl">MeshStorage</h1>
          </div>
          <button 
            id="collapse-sidebar" 
            className="md:hidden"
            onClick={() => setIsSidebarVisible(false)}
          >
            <span className="material-icons">menu_open</span>
          </button>
        </div>
        
        <nav className="p-4">
          <ul className="space-y-2">
            {sidebarItems.map((item) => (
              <li key={item.href}>
                <Link href={item.href} 
                  className={cn(
                    "flex items-center space-x-2 p-2 rounded-lg",
                    item.isActive ? "bg-gray-700" : "hover:bg-gray-700"
                  )}
                >
                  <span className={cn(
                    "material-icons",
                    item.isActive ? "text-primary" : ""
                  )}>{item.icon}</span>
                  <span>{item.label}</span>
                </Link>
              </li>
            ))}
          </ul>
        </nav>
        
        <div className="absolute bottom-0 w-64 p-4 border-t border-gray-700">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-full bg-gray-600 flex items-center justify-center">
              <span className="material-icons text-sm">person</span>
            </div>
            <div>
              <p className="text-sm font-medium">Admin User</p>
              <p className="text-xs text-gray-400">admin@meshstorage.com</p>
            </div>
          </div>
        </div>
      </div>
      
      {/* Mobile sidebar toggle */}
      <div id="sidebar-mobile" className="bg-gray-800 text-white w-16 flex-shrink-0 md:hidden fixed z-40 h-screen">
        <div className="p-4 flex justify-center">
          <button 
            id="expand-sidebar"
            onClick={() => setIsSidebarVisible(true)}
          >
            <span className="material-icons">menu</span>
          </button>
        </div>
        <nav className="p-2">
          <ul className="space-y-4">
            {sidebarItems.map((item) => (
              <li key={item.href} className="flex justify-center">
                <Link href={item.href}
                  className={cn(
                    "p-2 rounded-lg flex items-center justify-center",
                    item.isActive ? "bg-gray-700" : "hover:bg-gray-700"
                  )}
                >
                  <span className={cn(
                    "material-icons",
                    item.isActive ? "text-primary" : ""
                  )}>{item.icon}</span>
                </Link>
              </li>
            ))}
          </ul>
        </nav>
      </div>
    </>
  );
}
