package br.com.devd2.meshstorage.models.messages;

import lombok.Data;

@Data
public class FileRegisterMessage extends GenericMessage {
    private String idFile;
    private String fileName;
    private String applicationStorageFolder;
    private String dataBase64;

    public FileRegisterMessage() {
        this.setType("FILE_REGISTER");
    }
}
