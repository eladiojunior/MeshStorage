package br.com.devd2.meshstorageserver.models;

import lombok.Data;

@Data
public class ServerStorageModel {
    private String idClient;
    private String serveName;
    private String ipServer;
    private String storageName;
    private long totalSpace;
    private long freeSpace;
}