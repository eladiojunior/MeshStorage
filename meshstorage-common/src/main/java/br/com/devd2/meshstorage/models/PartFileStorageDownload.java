package br.com.devd2.meshstorage.models;

import lombok.Data;

@Data
public class PartFileStorageDownload {
    private int numberPart;
    private String dataBase64;

    public PartFileStorageDownload(int partFile, String dataBase64) {
        this.numberPart = partFile;
        this.dataBase64 = dataBase64;
    }
}