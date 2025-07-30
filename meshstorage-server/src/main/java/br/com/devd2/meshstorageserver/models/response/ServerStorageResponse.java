package br.com.devd2.meshstorageserver.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServerStorageResponse {
    private Long id;
    private String idClient;
    private String serverName;
    private String storageName;
    private Long totalSpace;
    private Long freeSpace;
    private Long totalFiles;
    private String ipServer;
    private String osServer;
    private Integer statusCode;
    private String statusDescription;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeRegistered;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeRemoved;

}