package br.com.devd2.meshstorage.models.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PartFileRegisterMessage extends GenericMessage {
    @JsonProperty("idFile")
    private String idFile;
    @JsonProperty("fileName")
    private String fileName;
    @JsonProperty("applicationStorageFolder")
    private String applicationStorageFolder;
    @JsonProperty("dataBase64")
    private String dataBase64;
    @JsonProperty("partFile")
    private int partFile;
    @JsonProperty("lastPartFile")
    private boolean lastPartFile;
    public PartFileRegisterMessage() {
        this.setType("PART_FILE_REGISTER");
    }
}
