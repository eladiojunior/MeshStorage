package br.com.devd2.meshstorageserver.models.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ListFilesFilterRequest {
    private String applicationCode;
    private String fileLogicName;
    private String[] fileContentType;
    private int pageNumber = 1;
    private int recordsPerPage = 15;
    private boolean filesSentForBackup = false;
    private boolean filesRemoved = false;
}