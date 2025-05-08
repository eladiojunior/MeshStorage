package br.com.devd2.meshstorage.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FileStorageClientStatus {

    @JsonProperty("idFile")
    private String idFile;

    @JsonProperty("isError")
    private boolean isError;

    @JsonProperty("messageError")
    private String messageError;

    @JsonProperty("fileStatusCode")
    private Integer fileStatusCode;
}
