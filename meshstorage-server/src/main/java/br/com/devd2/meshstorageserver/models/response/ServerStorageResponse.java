package br.com.devd2.meshstorageserver.models.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServerStorageResponse {
    private String idClient;
    private String serverName;
    private String storageName;
    private long totalSpace;
    private long freeSpace;
    private boolean available;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateTimeAvailable;

}