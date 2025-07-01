import {Application, Storage, SystemStatus} from "@/server/api/api-types";

type SystemStatusApiResponse = {
    systemHealth: string;
    messageStatus: string;
    totalSpaceStorages: number;
    totalFreeStorages: number;
    totalClientsConnected: number;
    totalFilesStorages: number;
    dateTimeAvailable: string;
};
type ApplicationApiResponse = {
    id: number;
    applicationName: string;
    applicationDescription: string;
    maximumFileSize: number;
    allowedFileTypes: string[];
    compressFileContent: boolean;
    applyOcrFileContent: boolean;
    allowDuplicateFile: boolean;
    totalFiles: number;
    dateTimeApplication: string;
};
type StorageApiResponse = {
    id: number;
    idClient: string;
    serverName: string;
    storageName: string;
    totalSpace: number;
    freeSpace: number;
    ipServer: string;
    osServer: string;
    available: boolean;
    dateTimeAvailable: string;
};

function formatMBtoGB(valueMB: number): number {
    return Math.round((valueMB / 1024) * 10) / 10; // arredonda com 1 casa decimal
}

function mapSystemStatus(response: SystemStatusApiResponse): SystemStatus {
    const totalSpase = response.totalSpaceStorages;
    const totalFreed = response.totalFreeStorages;
    const totalUsed = totalSpase - totalFreed;
    const formattedStorage = `${formatMBtoGB(totalSpase)}/${formatMBtoGB(totalUsed)} GB`;
    return {
        totalStorage: formattedStorage,
        connectedClients: response.totalClientsConnected,
        totalFiles: response.totalFilesStorages,
        health: response.systemHealth,
        messageStatus: response.messageStatus
    };
}

function mapApplication(response: ApplicationApiResponse): Application {
    return {
        id: response.id,
        name: response.applicationName,
        description: response.applicationDescription,
        icon: 'apps',
        fileCount: response.totalFiles
    };
}

function mapStorage(response: StorageApiResponse): Storage {
    const total = response.totalSpace;
    const free = response.freeSpace;
    const used = total - free;
    return {
        id: response.id,
        idClient: response.idClient,
        name: `${response.storageName} [${response.serverName}]`,
        ipAddress: response.ipServer,
        osName: response.osServer,
        storageCapacity: formatMBtoGB(response.totalSpace),
        storageUsed: formatMBtoGB(used),
        fileCount: 0,
        status: response.available ? 'active' : 'offline',
        lastConnected: response.dateTimeAvailable,
    };
}
export class ApiClient {
    private readonly baseURL: string;
    private readonly apiKey?: string;

    constructor() {
        this.baseURL = process.env.MESHSTORAGE_API_URL || 'http://localhost:3001/api/v1';
        this.apiKey = process.env.MESHSTORAGE_API_KEY;
    }

    private async request(endpoint: string, options: RequestInit = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const headers: Record<string, string> = {
            'Content-Type': 'application/json',
            ...options.headers as Record<string, string>
        };

        if (this.apiKey) {
            headers['Authorization'] = `Bearer ${this.apiKey}`;
        }

        const response = await fetch(url, {
            ...options,
            headers,
            cache: 'no-store',
        });

        if (!response.ok) {
            throw new Error(`API Error: ${response.status} ${response.statusText}`);
        }

        return response.json();
    }

    // System Status
    async getSystemStatus(): Promise<SystemStatus> {
        const data: SystemStatusApiResponse = await this.request('/system/status');
        return mapSystemStatus(data);
    }

    // Clients (Storage nodes)
    async getStorages(): Promise<Storage[]>  {
        const data: StorageApiResponse[] = await this.request('/storage/list?available=false');
        return data.map(mapStorage);
    }

    // Applications
    async getApplications(): Promise<Application[]> {
        const data: ApplicationApiResponse[] = await this.request('/application/list');
        return data.map(mapApplication);
    }

    /*
    async createApplication(applicationData: any) {
        return this.request('/applications', {
            method: 'POST',
            body: JSON.stringify(applicationData)
        });
    }

    async updateApplication(id: string, applicationData: any) {
        return this.request(`/applications/${id}`, {
            method: 'PUT',
            body: JSON.stringify(applicationData)
        });
    }

    async deleteApplication(id: string) {
        return this.request(`/applications/${id}`, {
            method: 'DELETE'
        });
    }

    // Content Types
    async getContentTypes() {
        return this.request('/content-types');
    }

    // System Alerts
    async getSystemAlerts() {
        return this.request('/alerts');
    }

    async markAlertAsRead(id: string) {
        return this.request(`/alerts/${id}/read`, {
            method: 'PATCH'
        });
    }

    // File operations
    async uploadFile(applicationId: string, file: FormData) {
        return this.request(`/applications/${applicationId}/files`, {
            method: 'POST',
            body: file,
            headers: {} // Let browser set Content-Type for FormData
        });
    }

    async deleteFile(applicationId: string, fileId: string) {
        return this.request(`/applications/${applicationId}/files/${fileId}`, {
            method: 'DELETE'
        });
    }
    */
}
export const meshStorageAPI = new ApiClient();