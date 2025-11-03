package br.com.devd2.meshstorageserver.models;

import lombok.Data;

@Data
public class FileUploadModel {
    private String originalFilename;
    private byte[] bytes;
    private String contentType;

    public FileUploadModel(String originalFilename, String contentType, byte[] bytes) {
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.bytes = bytes;
    }

    public boolean isEmpty() {
        return bytes == null  || bytes.length == 0;
    }
}
