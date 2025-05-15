package br.com.devd2.meshstorage.models.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FileDownloadMessage extends GenericMessage {
    @JsonProperty("idFile")
    private String idFile;
    @JsonProperty("fileName")
    private String fileName;
    @JsonProperty("applicationStorageFolder")
    private String applicationStorageFolder;
    public FileDownloadMessage() {
        this.setType("FILE_DOWNLOAD");
    }
}
