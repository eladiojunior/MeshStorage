package br.com.devd2.meshstorage.models;

import lombok.Data;

@Data
public class FileStorageClientStatus {
    private Long idFile;
    private boolean isErrorWrite;
    private String messageErrorWrite;
}
