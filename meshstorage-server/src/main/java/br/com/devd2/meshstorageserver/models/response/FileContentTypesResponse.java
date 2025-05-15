package br.com.devd2.meshstorageserver.models.response;

import lombok.Data;

@Data
public class FileContentTypesResponse {
    private int code;
    private String nameEnum;
    private String extension;
    private String description;
    private String contentType;
}