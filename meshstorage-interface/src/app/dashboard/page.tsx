'use client'
import InfoCard from "@/components/InfoCard";
import StorageCard from "@/components/StorageCard";
import ApplicationCard from "@/components/ApplicationCard";
//import { HardDrive, Users, Files, Activity, Plus, Upload, Trash2 } from 'lucide-react';

export default function DashboardPage() {
  return (
      <div className="flex h-screen overflow-hidden bg-gray-50">
        <div className="flex-1 overflow-y-auto md:ml-64 ml-16">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <InfoCard icon="storage" title="Total Storage" value="256.4/500 GB" subtitle="" />
            <InfoCard icon="group" title="Connected Clients" value="12" subtitle="Storages ativos" />
            <InfoCard icon="description" title="Total Files" value="24.578" />
            <InfoCard icon="monitor_heart" title="System Health" value="Saudável" />
          </div>
          <div className="row mt-4">
            <div className="col-lg-6 mb-4">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <h5 className="mb-0">Storages Conectados</h5>
                <small className="text-muted">3 total</small>
              </div>
              <StorageCard name="Client-A" ip="192.168.1.101" used="45.2 GB" usage={65} status="active" />
              <StorageCard name="Client-B" ip="192.168.1.102" used="28.7 GB" usage={41} status="active" />
              <StorageCard name="Client-C" ip="192.168.1.103" used="62.1 GB" usage={89} status="warning" />
            </div>
            <div className="col-lg-6 mb-4">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <h5 className="mb-0">Aplicações</h5>
                <button className="btn btn-primary btn-sm">+ Nova Aplicação</button>
              </div>
              <ApplicationCard name="Development IDE" icon="code" files={16520} color="secondary" />
              <ApplicationCard name="Media Library" icon="image" files={5245} color="primary" />
              <ApplicationCard name="Document Manager" icon="description" files={2813} color="success" />
              <ApplicationCard name="Backup System" icon="cloud" files={45} color="danger" />
            </div>
          </div>
        </div>
      </div>
  );
}