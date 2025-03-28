package br.com.devd2.meshstorageclient.models;

import lombok.Data;

@Data
public class StorageClient {
    private String serverName;
    private String ipServer;
    private String storageName;
    private String osServer;
}