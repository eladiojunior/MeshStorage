export class MeshStorageAPIClient {
    private baseURL: string;
    private apiKey?: string;

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
            headers
        });

        if (!response.ok) {
            throw new Error(`API Error: ${response.status} ${response.statusText}`);
        }

        return response.json();
    }

    // System Status
    async getSystemStatus() {
        return this.request('/system/status');
    }

    // Clients (Storage nodes)
    async getClients() {
        return this.request('/clients');
    }

    async getClient(id: string) {
        return this.request(`/clients/${id}`);
    }

    // Applications
    async getApplications() {
        return this.request('/applications');
    }

    async getApplication(id: string) {
        return this.request(`/applications/${id}`);
    }

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
}

export const meshStorageAPI = new MeshStorageAPIClient();
