package br.com.devd2.meshstorage.models;

import lombok.Data;

@Data
public class FileStorageClientStatus {
    private String idFile;
    private boolean isError;
    private String messageError;
    private Integer fileStatusCode;
}
