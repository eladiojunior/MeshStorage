package br.com.devd2.meshstorage.models;

import lombok.Data;

@Data
public class StorageClient {

    private String idClient;
    private String urlWebsocketServer;
    private String serverName;
    private String ipServer;
    private String osName;

    private String storageName;
    private String storagePath;

    private long totalSpaceMB;
    private long freeSpaceMB;

}