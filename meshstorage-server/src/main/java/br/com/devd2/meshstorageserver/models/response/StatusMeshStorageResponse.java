package br.com.devd2.meshstorageserver.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatusMeshStorageResponse {
    private String systemHealth;
    private String messageStatus;
    private long totalSpaceStorages; //MB
    private long totalFreeStorages;  //MB
    private long totalClientsConnected;
    private long totalFilesStorages;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeAvailable;
}