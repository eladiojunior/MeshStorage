// System Status schema and types
export type SystemStatus = {
    id: number,
    totalStorage: number, // in GB
    usedStorage: number, // in GB
    totalClients: number,
    connectedClients: number,
    totalFiles: number,
    newFilesToday: number,
    filesChangePercentage: 0,
    healthStatus: string, // "healthy", "warning", "error"
    lastCheck: Date,
    uptime: number, // percentage
};

// Application schema and types
export const applications = {
    id:0,
    name: '',
    description: '',
    icon: '', // Material icon name
    iconColor: '', // Color class for the icon background
    storageUsed: 0, // in GB
    percentage: 0, // Percentage of total storage
    fileCount: 0,
};
export type ApplicationStorage = typeof applications;

// Client schema and types
export const clients = {
    id: 0,
    name: '',
    ipAddress: '0.0.0.0',
    status: 'offline', // "active", "warning", "error", "offline"
    storageUsed: 0, // in GB
    storageCapacity: 0, // in GB
    fileCount: 0,
    lastConnected: new Date(),
};
export type StorageClient = typeof clients;