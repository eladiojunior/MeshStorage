package br.com.devd2.meshstorage.models.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileDeleteMessage extends GenericMessage {
    private String idFile;
    private String fileName;
    private String applicationStorageFolder;

    public FileDeleteMessage() {
        this.setType("FILE_DELETE");
    }
}