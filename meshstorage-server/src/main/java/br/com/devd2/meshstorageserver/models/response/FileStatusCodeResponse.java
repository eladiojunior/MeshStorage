package br.com.devd2.meshstorageserver.models.response;

import lombok.Data;

@Data
public class FileStatusCodeResponse {
    private int code;
    private String nameEnum;
    private String description;
}