package br.com.devd2.meshstorageserver.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatusMeshStorageModel {
    private String systemHealth;
    private String messageStatus;
    private long totalSpaceStorages; //MB
    private long totalFreeStorages;  //MB
    private long totalClientsConnected;
    private long totalFilesStorages;
    private LocalDateTime dateTimeAvailable;
}