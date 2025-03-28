package br.com.devd2.meshstorageclient.config;

import br.com.devd2.meshstorageclient.models.StorageClient;

public class StorageConfig {
    private static StorageConfig instance;
    private StorageClient client;
    public static StorageConfig get() {
        if (instance == null)
            instance = new StorageConfig();
        return instance;
    }
    private StorageConfig() {
        client = new StorageClient();
    }
    public StorageClient getClient() {
        return client;
    }
}