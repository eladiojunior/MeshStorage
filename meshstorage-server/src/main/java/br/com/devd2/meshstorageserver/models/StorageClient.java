package br.com.devd2.meshstorageserver.models;

import lombok.Data;

@Data
public class StorageClient {

    private String idClient;
    private String serverName;
    private String ipServer;
    private String osName;

    private String storageName;
    private String storagePath;

    private long totalSpaceMB;
    private long freeSpaceMB;

}