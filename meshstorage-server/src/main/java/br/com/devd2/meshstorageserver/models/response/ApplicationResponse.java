package br.com.devd2.meshstorageserver.models.response;

import lombok.Data;

@Data
public class ApplicationResponse {
    private Long id;
    private String applicationName;
    private String applicationDescription;
    private long maximumFileSize;
    private String[] allowedFileTypes;
}