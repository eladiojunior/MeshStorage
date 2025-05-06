package br.com.devd2.meshstorage.models.messages;

import lombok.Data;

@Data
public class FileDownloadMessage extends GenericMessage {
    private String idFile;
    private String fileName;
    public FileDownloadMessage() {
        this.setType("FILE_DOWNLOAD");
    }
}
