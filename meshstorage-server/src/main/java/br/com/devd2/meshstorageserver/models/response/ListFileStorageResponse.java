package br.com.devd2.meshstorageserver.models.response;

import lombok.Data;

import java.util.List;

@Data
public class ListFileStorageResponse {
    private long totalRecords;
    private List<FileStorageResponse> files;
}