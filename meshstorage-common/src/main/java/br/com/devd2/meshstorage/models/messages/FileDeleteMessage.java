package br.com.devd2.meshstorage.models.messages;

import lombok.Data;

@Data
public class FileDeleteMessage extends GenericMessage {
    private String idFile;
    private String fileName;
    public FileDeleteMessage() {
        this.setType("FILE_DELETE");
    }
}