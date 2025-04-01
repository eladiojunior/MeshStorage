package br.com.devd2.meshstorageclient.models;

import lombok.Data;

@Data
public class StorageClient {
    private String serverName;
    private String ipServer;
    private String osName;

    private String storageName;

    private long totalSpaceMB;
    private long freeSpaceMB;
}