'use client'
import InfoCard from "@/components/InfoCard";
import {StorageClient, ApplicationStorage} from "@/lib/schema";

export default function DashboardPage() {

    // Calcula métricas
    const totalStorage = '256/500 GB';
    const connectedClients = '1';
    const totalFiles = '1';
    const systemHealth = 'healthy';

    //Lista de Storages...
    const clients: StorageClient[] = [
        {id:1, name: 'Storage_01', ipAddress: '192.167.21.1', so: 'Windows Server', storageCapacity: 500, storageUsed: 480, fileCount: 10, status: 'warning', lastConnected: new Date()},
        {id:2, name: 'Storage_02', ipAddress: '192.167.21.2', so: 'Linux', storageCapacity: 100, storageUsed: 2, fileCount: 55, status: 'active', lastConnected: new Date()},
        {id:3, name: 'Storage_03', ipAddress: '192.167.21.3', so: 'Linux', storageCapacity: 150, storageUsed: 50, fileCount: 800, status: 'offline', lastConnected: new Date()}
    ];
    const applications: ApplicationStorage[] = [
        {id:1, name: 'APP_01', icon: 'apps', storageUsed: 1, description: 'Aplicação de Teste 01', fileCount: 101, iconColor: '', percentage: 0},
        {id:2, name: 'APP_02', icon: 'apps', storageUsed: 1, description: 'Aplicação de Teste 02', fileCount: 102, iconColor: '', percentage: 0}
    ];

    return (
        <div>
            <div className="row g-4 mb-4">
                <div className="col-xl-3 col-md-6">
                    <InfoCard
                        title="Total Storages"
                        value={totalStorage}
                        icon="dashboard"
                        status="primary"
                    />
                </div>
                <div className="col-xl-3 col-md-6">
                    <InfoCard
                        title="Storages Conectados"
                        value={connectedClients}
                        icon="storage"
                        status="success"
                    />
                </div>
                <div className="col-xl-3 col-md-6">
                    <InfoCard
                        title="Total Arquivos"
                        value={totalFiles}
                        icon="description"
                        status="primary"
                    />
                </div>
                <div className="col-xl-3 col-md-6">
                    <InfoCard
                        title="System Health"
                        value={systemHealth === 'healthy' ? 'Saudável' : systemHealth === 'warning' ? 'Atenção' : 'Erro'}
                        icon="monitor_heart"
                        status={systemHealth === 'healthy' ? 'success' : systemHealth === 'warning' ? 'warning' : 'danger'}
                    />
                </div>
            </div>
            <div className="row g-4">
                <div className="col-lg-6">
                    <StoragesList clients={clients}/>
                </div>
                <div className="col-lg-6">
                    <ApplicationManager applications={applications}/>
                </div>
            </div>
        </div>
    );
}

// Lista de Storages
function StoragesList({clients}: { clients: StorageClient[] }) {
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
                                            <small className="text-muted">IP: {client.ipAddress} - SO: {client.so}</small>
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
function ApplicationManager({applications}: { applications: ApplicationStorage[] }) {
    return (
        <div className="card">
            <div className="card-header d-flex justify-content-between align-items-center">
                <h5 className="card-title mb-0">Aplicações</h5>
                <button
                    onClick={() => console.log('Nova aplicação.')}
                    className="btn btn-primary btn-sm btn-with-icon-and-text">
                        <span className="material-icons">add</span>
                        Nova Aplicação
                </button>
            </div>
            <div className="card-body">
                <div className="row g-3">
                    {applications.map((app) => (
                        <div key={app.id} className="col-12">
                            <div className="app-item border rounded p-3">
                                <div className="d-flex align-items-center justify-content-between">
                                    <div className="d-flex align-items-center">
                                        <div className="me-3">
                                            <div className="rounded p-2 bg-primary bg-opacity-10 text-primary div-with-icon">
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
                                        <button className="btn btn-sm btn-outline-primary me-2 btn-with-icon" title={'Localizar arquivo da aplicação.'}>
                                            <span className="material-icons">search</span>
                                        </button>
                                        <button className="btn btn-sm btn-outline-danger btn-with-icon" title={'Desativar aplicação.'}>
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