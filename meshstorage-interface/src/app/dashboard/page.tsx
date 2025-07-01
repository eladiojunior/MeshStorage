import InfoCard from "@/components/InfoCard";
import { meshStorageAPI } from '@/server/api/api-client';
import {SystemStatus, Storage, Application} from "@/server/api/api-types";

export default async function DashboardPage() {
    const systemStatus = await meshStorageAPI.getSystemStatus();
    const storages = await meshStorageAPI.getStorages();
    const applications = await meshStorageAPI.getApplications();

    return (
        <div>
            <div className="row g-4 mb-4">
                <SystemStatusList status={systemStatus}/>
            </div>
            <div className="row g-4">
                <div className="col-lg-6">
                    <StoragesList clients={storages}/>
                </div>
                <div className="col-lg-6">
                    <ApplicationManager applications={applications}/>
                </div>
            </div>
        </div>
    );
}

function SystemStatusList({status}: { status: SystemStatus }) {
    return (<>
            <div className="col-xl-3 col-md-6">
                <InfoCard
                    title="Total Storages"
                    value={status.totalStorage}
                    icon="dashboard"
                    status="primary"
                />
            </div>
            <div className="col-xl-3 col-md-6">
                <InfoCard
                    title="Storages Conectados"
                    value={status.connectedClients}
                    icon="storage"
                    status="success"
                />
            </div>
            <div className="col-xl-3 col-md-6">
                <InfoCard
                    title="Total Arquivos"
                    value={status.totalFiles}
                    icon="description"
                    status="primary"
                />
            </div>
            <div className="col-xl-3 col-md-6">
                <InfoCard
                    title="System Health"
                    value={status.health === 'healthy' ? 'Saudável' : status.health === 'warning' ? 'Atenção' : 'Erro'}
                    icon="monitor_heart"
                    status={status.health === 'healthy' ? 'success' : status.health === 'warning' ? 'warning' : 'danger'}
                />
            </div>
        </>
    );

}

function StoragesList({clients}: { clients: Storage[] }) {
    return (
        <div className="card">
            <div className="card-header d-flex justify-content-between align-items-center">
                <h5 className="card-title mb-0">Storages Conectados</h5>
                <span className="badge bg-secondary" style={{fontSize: '1.2em'}}>{clients.length}</span>
            </div>
            <div className="card-body">
                <div className="row g-3">
                    {clients.map((client) => {
                        const percentage = Math.round((client.storageUsed / client.storageCapacity) * 100);
                        const isWarning = percentage >= 80;
                        const isOffline = (client.status === 'offline');
                        const bgStatus =
                            (client.status === 'active' ? 'bg-success' : client.status === 'warning' ? 'bg-warning' : client.status === 'offline' ? 'bg-secondary' : 'bg-danger');
                        const bgProgressBar =
                            (isOffline ? 'bg-secondary' : isWarning ? 'bg-warning' : 'bg-primary');
                        return (
                            <div key={client.id} className="col-12">
                                <div className={`border rounded p-3 ${isOffline ? 'bg-offline' : ''}`}>
                                    <div className="d-flex justify-content-between align-items-center mb-2">
                                        <div>
                                            <h4 className="mb-1">{client.name}</h4>
                                            <small className="text-muted">IP: {client.ipAddress} - SO: {client.osName}</small>
                                        </div>
                                        <span className={`badge ${bgStatus}`}>
                                            {client.status === 'active' ? 'Ativo' : client.status === 'warning' ? 'Atenção' : client.status === 'offline' ? 'Desativado' : 'Erro'}</span>
                                    </div>
                                    <div className="d-flex justify-content-between small text-muted mb-1">
                                        <span>Total {client.storageCapacity.toFixed(1)} GB e {client.storageUsed.toFixed(1)} GB usados</span>
                                        <span>{percentage}%</span>
                                    </div>
                                    <div className="progress storage-progress">
                                        <div
                                            className={`progress-bar ${bgProgressBar}`}
                                            style={{width: `${percentage}%`}}>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}

// Lista de Applications
function ApplicationManager({applications}: { applications: Application[] }) {
    return (
        <div className="card">
            <div className="card-header d-flex justify-content-between align-items-center">
                <h5 className="card-title mb-0">Aplicações</h5>
                <span className="badge bg-secondary" style={{fontSize: '1.2em'}}>{applications.length}</span>
            </div>
            <div className="card-body">
                <div className="row g-3">
                    {applications.map((app) => (
                        <div key={app.id} className="col-12">
                            <div className="app-item border rounded p-3">
                                <div className="d-flex align-items-center justify-content-between">
                                    <div className="d-flex align-items-center">
                                        <div className="me-3">
                                            <div
                                                className="rounded p-2 bg-primary bg-opacity-10 text-primary div-with-icon">
                                                <span className="material-icons">{app.icon}</span>
                                            </div>
                                        </div>
                                        <div>
                                            <h6 className="mb-1">{app.name}</h6>
                                            <small
                                                className="text-muted">{app.fileCount.toLocaleString()} arquivos</small>
                                        </div>
                                    </div>
                                    <div className="d-flex">
                                        <button className="btn btn-sm btn-outline-primary me-2 btn-with-icon"
                                                title={'Localizar arquivo da aplicação.'}>
                                            <span className="material-icons">search</span>
                                        </button>
                                        <button className="btn btn-sm btn-outline-danger btn-with-icon"
                                                title={'Desativar aplicação.'}>
                                            <span className="material-icons">delete</span>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}