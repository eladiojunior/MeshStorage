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
export type Application = typeof applications;

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
export type Client = typeof clients;

// System Alert schema and types
export const systemAlerts = {
    id: 0,
    type: 'info', // "warning", "error", "success", "info"
    title: 'Mensagem',
    message: '',
    timestamp: new Date(),
    read: false,
};
export type SystemAlert = typeof systemAlerts;

// Storage History schema and types
export const storageHistory = {
    id: 0,
    timestamp: new Date(),
    usedStorage: 0, // in GB
};
export type StorageHistory = typeof storageHistory;

// File Type Distribution schema and types
export const fileTypeDistribution = {
    id: 0,
    fileType: '',
    percentage: 0,
    color: '',
};
export type FileTypeDistribution = typeof fileTypeDistribution;