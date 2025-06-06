'use client'
import { useEffect, useState } from 'react';
import { MetricCard } from '@/components/dashboard/MetricCard';

import useWebSocket from '@/hooks/useWebSocket';

import type { SystemStatus } from '@/lib/schema'
import {Activity, Files, HardDrive, Users} from "lucide-react";

export default function Page() {

  const [systemStatus, setSystemStatus] = useState<SystemStatus | undefined>(undefined);

  // Add Material Icons (make sure it's loaded in the dashboard)
  useEffect(() => {
    // Check if Material Icons is already loaded
    if (!document.querySelector('link[href*="fonts.googleapis.com/icon"]')) {
      const link = document.createElement('link');
      link.href = 'https://fonts.googleapis.com/icon?family=Material+Icons';
      link.rel = 'stylesheet';
      document.head.appendChild(link);
      return () => {
        const element = document.querySelector('link[href*="fonts.googleapis.com/icon"]');
        if (element) document.head.removeChild(element);
      };
    }
    const fetchSystemStatus = async () => {
      const response = await fetch('/api/system-status');
      if (response.ok) {
        const data: SystemStatus = await response.json();
        setSystemStatus(data);
      }
    };
    fetchSystemStatus().then(r => console.log('Result: ' + r));
  }, []);

  // WebSocket connection for real-time updates
  const {} = useWebSocket({
    onMessage: (data) => {
      console.log('WebSocket message received:', data);
    }
  });

  return (
    <div className="flex h-screen overflow-hidden mt-5">
      <div className="flex-1 overflow-y-auto md:ml-64 ml-16">
        <main className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <MetricCard
                title="Total Storage"
                value={systemStatus?.totalStorage}
                icon={<HardDrive size={24} />}
                status="success"
            />
            <MetricCard
                title="Connected Clients"
                value={systemStatus?.connectedClients}
                icon={<Users size={24} />}
                status="success"
                subtitle="Storages ativos"
            />
            <MetricCard
                title="Total Files"
                value={systemStatus?.totalFiles}
                icon={<Files size={24} />}
                status="success"
            />
            <MetricCard
                title="System Health"
                value={systemStatus?.healthStatus}
                icon={<Activity size={24} />}
                status={systemStatus?.healthStatus === 'healthy' ? 'success' : systemStatus?.healthStatus === 'warning' ? 'warning' : 'error'}
            />
          </div>
        </main>
      </div>
    </div>
  );
}
