package br.com.devd2.meshstorageserver.models.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatusMeshStorageModel {
    private String systemHealth;
    private String messageStatus;
    private LocalDateTime dateTimeAvailable;
}