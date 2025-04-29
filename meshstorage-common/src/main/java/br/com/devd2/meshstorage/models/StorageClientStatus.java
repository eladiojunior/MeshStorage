package br.com.devd2.meshstorage.models;

import lombok.Data;

@Data
public class StorageClientStatus {
    private String idClient;
    private String serverName;
    private String storageName;
    private long totalSpaceMB;
    private long freeSpaceMB;
}