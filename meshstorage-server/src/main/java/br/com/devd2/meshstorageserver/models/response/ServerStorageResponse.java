package br.com.devd2.meshstorageserver.models.response;

import lombok.Data;

@Data
public class ServerStorageResponse {
    private Long id;
    private String serverName;
    private String storageName;
    private long totalSpace;
    private long freeSpace;
}