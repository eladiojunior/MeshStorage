package br.com.devd2.meshstorage.models;

import lombok.Data;

@Data
public class FileStorageClient {
    private Long idFile;
    private String fileName;
    private String dataBase64;
    private String pathStorage;
}
