export type SystemStatus = {
    totalStorage: string;
    connectedClients: number;
    totalFiles: number;
    health: string;
    messageStatus: string;
};

export type Storage = {
    id:number;
    idClient:string;
    name: string;
    ipAddress: string;
    osName: string;
    storageCapacity: number;
    storageUsed: number;
    fileCount: number;
    status: 'active' | 'warning' | 'offline';
    lastConnected: string;
};

export type Application = {
    id: number;
    name: string;
    description: string;
    icon: string;
    fileCount: number;
};