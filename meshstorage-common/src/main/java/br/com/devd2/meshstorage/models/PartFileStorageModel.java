package br.com.devd2.meshstorage.models;

import lombok.Data;

@Data
public class PartFileStorageModel {
    private int numberPart;
    private String dataBase64;

    public PartFileStorageModel(int partFile, String dataBase64) {
        this.numberPart = partFile;
        this.dataBase64 = dataBase64;
    }
}