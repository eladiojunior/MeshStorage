package br.com.devd2.meshstorageserver.models.response;

import lombok.Data;

@Data
public class FileStorageResponse {
    private String idFile; //Chave de identificação externa do arquivo.
    private String fileLogicName;
    private String fileFisicalName;
    private String fileType;
    private long fileLength;
}